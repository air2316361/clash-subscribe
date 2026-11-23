import axios from 'axios';
import template from './template';
import yaml from 'js-yaml';
import getConverter from './converter';

import { proxyKey } from './proxy_key';

export default async function (env) {
	try {
		// [修复5] KV.list() 分页取全，收集所有 key（避免只读首页丢数据）
		const allKeys = await listAllKeys(env);

		const keySet = new Set();
		for (const key of allKeys) {
			const keyName = key.name;
			if (keyName === proxyKey) {
				continue;
			}
			keySet.add(keyName);
		}

		let updateKey = undefined;
		let converter = undefined;
		let urls = undefined;

		// 优先：从配置里找一个 KV 中尚不存在的新 key
		for (const envName of env.ENV_NAMES) {
			const config = env[envName];
			for (const key in config) {
				if (keySet.has(key)) {
					continue;
				}
				converter = getConverter(key);
				if (converter) {
					updateKey = key;
					urls = config[key];
					break;
				}
			}
			if (updateKey) {
				break;
			}
		}

		// fallback：没有新 key 时，挑“最早过期”的已有 key 轮转刷新
		if (!updateKey) {
			// [修复3] 空 KV 判断应基于真实 key 列表长度，而非 list 对象的 length
			if (allKeys.length === 0) {
				return;
			}
			let minExpiration = Infinity;
			for (const key of allKeys) {
				const keyName = key.name;
				if (keyName === proxyKey) {
					continue;
				}
				// [修复4] expiration 可能为 undefined/0（未设 TTL），归一为 Infinity，
				// 让永不过期的 key 排在轮转最后，且比较结果不再依赖遍历顺序
				const exp = (key.expiration === undefined || key.expiration === null || key.expiration === 0)
					? Infinity
					: key.expiration;
				if (exp >= minExpiration) {
					continue;
				}
				const c = getConverter(keyName);
				if (!c) {
					continue;
				}
				minExpiration = exp;
				converter = c;
				updateKey = keyName;
				// [修复1-核心] fallback 路径必须补上 urls：按选中的 key 反查配置取订阅地址
				urls = findUrls(env, keyName);
			}
		}

		if (!updateKey || !converter) {
			return;
		}

		// [修复1] urls 仍缺失（配置里查不到该 key 的地址）时直接返回，避免 request(undefined) 崩溃
		if (!urls) {
			console.log('skip: no urls for ' + updateKey);
			return;
		}

		const proxyStr = await env.KV.get(updateKey);
		// [修复2] 空串是 falsy，原判断 (proxyStr && proxyStr.length === 0) 恒为 false，
		// 失败标记永远命中不了；改为显式判断“非 null 且长度为 0”
		if (proxyStr !== null && proxyStr.length === 0) {
			return;
		}

		const res = await request(urls);
		if (!res) {
			await env.KV.put(updateKey, "", {
				expirationTtl: 86400
			});
			return;
		}
		const updateProxy = converter(res);
		await env.KV.put(updateKey, JSON.stringify(updateProxy), {
			expirationTtl: 86400
		});
		console.log('updated: ' + updateKey);
		// [修复9] 复用主流程已分页取回的 allKeys，generateProxy 不再二次 KV.list()
		await generateProxy(env, updateKey, updateProxy, allKeys);
	} catch (err) {
		// [修复6] 主流程兜底：单次 cron 任一环节抛错都不应让 Worker 直接崩溃
		console.error('update failed: ' + (err && err.stack ? err.stack : err));
	}
}

// [修复5] 分页拉取 KV 全部 key
async function listAllKeys(env) {
	const allKeys = [];
	let list = await env.KV.list();
	allKeys.push(...list.keys);
	while (!list.list_complete) {
		list = await env.KV.list({ cursor: list.cursor });
		allKeys.push(...list.keys);
	}
	return allKeys;
}

// [修复1] 按 key 名在 env.ENV_NAMES 配置里反查订阅地址数组
function findUrls(env, keyName) {
	for (const envName of env.ENV_NAMES) {
		const config = env[envName];
		if (config && Object.prototype.hasOwnProperty.call(config, keyName)) {
			return config[keyName];
		}
	}
	return undefined;
}

async function request(urls) {
	// [修复7] 校验 urls 是非空数组，避免 request(undefined) 或 urls[0] 越界
	if (!Array.isArray(urls) || urls.length === 0) {
		console.log('request: invalid urls');
		return undefined;
	}
	let result = undefined;
	try {
		console.log('request: ' + urls[0]);
		let res = await axios.get(urls[0]);
		if ((!res || res.status !== 200) && urls.length > 1) {
			res = await axios.get(urls[1]);
		}
		result = res && res.data;
	} catch (err) {
		// [修复7] 仅当存在备用地址时才请求备用地址，且对备用请求做兜底保护
		if (urls.length > 1) {
			try {
				console.log('request(fallback): ' + urls[1]);
				const res = await axios.get(urls[1]);
				if (res && res.status === 200) {
					result = res.data;
				}
			} catch (err2) {
				console.error('request fallback failed: ' + err2);
			}
		}
	}
	return result;
}

async function generateProxy(env, updateKey, updateProxy, allKeys) {
	const proxyConfig = {
		...template,
		'proxies': [],
		'proxy-groups': [
			{
				'name': '\ud83d\ude80 \u8282\u70b9\u9009\u62e9',
				'type': 'select',
				'proxies': [
					'\u267b\ufe0f \u81ea\u52a8\u9009\u62e9',
					'DIRECT'
				]
			},
			{
				'name': '\u267b\ufe0f \u81ea\u52a8\u9009\u62e9',
				'type': 'url-test',
				'url': 'https://www.gstatic.com/generate_204',
				'interval': 300,
				'tolerance': 50,
				'proxies': []
			}
		]
	};
	const servers = new Set();
	const keyNameSerials = new Map();

	// 读取上次结果，建立“去掉序号前缀的节点名 -> 节点对象”缓存
	const lastData = await env.KV.get(proxyKey);
	const proxyTemp = {};
	if (lastData && lastData.length > 0) {
		try {
			// [修复6] 上次数据损坏时 yaml.load 抛错不应中断整个聚合
			const lastConfig = yaml.load(lastData);
			(lastConfig && lastConfig.proxies ? lastConfig.proxies : []).forEach(proxy => {
				let proxyName = proxy.name;
				// [修复8] 序号前缀是加在开头的 (i)，应从开头按格式剥离，
				// 而不是用 lastIndexOf 右括号（节点名含右括号会裁错）
				proxyName = proxyName.replace(/^\(\d+\)/, '');
				proxyTemp[proxyName] = proxy;
			});
		} catch (err) {
			console.error('parse last proxy failed: ' + err);
		}
	}

	// [修复9] 复用传入的 allKeys，不再二次 KV.list()
	for (const key of allKeys) {
		const keyName = key.name;
		let proxies;
		if (keyName === proxyKey) {
			continue;
		} else if (keyName === updateKey) {
			proxies = updateProxy;
		} else {
			const proxyStr = await env.KV.get(keyName);
			if (!proxyStr || proxyStr.length === 0) {
				// 该源数据为空（被删/失败）：尝试复用上次的节点
				let serial = 0;
				while (true) {
					++serial;
					const proxyName = keyName + '_' + serial;
					const proxy = proxyTemp[proxyName];
					if (!proxy) {
						break;
					}
					const serverName = proxy.server + '|' + proxy.port;
					if (servers.has(serverName)) {
						continue;
					}
					servers.add(serverName);
					proxyConfig.proxies.push(proxy);
					proxyConfig['proxy-groups'].forEach(group => {
						group.proxies.push(proxyName);
					});
				}
				continue;
			}
			try {
				// [修复6] 单源数据损坏时跳过该 key，而非中断聚合
				proxies = JSON.parse(proxyStr);
			} catch (err) {
				console.error('parse key failed: ' + keyName + ' ' + err);
				continue;
			}
		}
		for (const proxy of proxies) {
			const serverName = proxy.server + '|' + proxy.port;
			if (servers.has(serverName)) {
				continue;
			}
			servers.add(serverName);
			let serial = keyNameSerials.get(keyName);
			if (serial) {
				++serial;
			} else {
				serial = 1;
			}
			keyNameSerials.set(keyName, serial);
			const proxyName = keyName + '_' + serial;
			proxy.name = proxyName;
			// [修复11-次要] 速率保留默认值，但优先沿用节点自带速率
			if (!proxy.up) {
				proxy.up = '20 Mbps';
			}
			if (!proxy.down) {
				proxy.down = '80 Mbps';
			}
			proxyConfig.proxies.push(proxy);
			proxyConfig['proxy-groups'].forEach(group => {
				group.proxies.push(proxyName);
			});
		}
	}

	// [修复10] 标注序号：用“旧名 -> 新名”映射回填 groups，彻底摆脱脆弱的 i+2 下标耦合
	const nameRename = new Map();
	for (let i = 0; i < proxyConfig.proxies.length; ++i) {
		const oldName = proxyConfig.proxies[i].name;
		const newName = '(' + i + ')' + oldName;
		proxyConfig.proxies[i].name = newName;
		nameRename.set(oldName, newName);
	}
	proxyConfig['proxy-groups'].forEach(group => {
		group.proxies = group.proxies.map(n => nameRename.has(n) ? nameRename.get(n) : n);
	});

	await env.KV.put(proxyKey, yaml.dump(proxyConfig));
}
