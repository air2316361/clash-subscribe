import axios from 'axios';
import template from './template';
import yaml from 'js-yaml';
import getConverter from './converter';

import proxyKey from './proxy_key';

export default async function(env) {
	const list = await env.KV.list();
	const keySet = new Set();
	for (const key of list.keys) {
		const keyName = key.name;
		if (keyName === proxyKey) {
			continue;
		}
		keySet.add(keyName);
	}
	let updateKey = undefined;
	let converter = undefined;
	let urls = undefined;
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
	}
	if (!updateKey) {
		if (list.length === 0) {
			return;
		}
		let minExpiration = Infinity;
		for (const key of list.keys) {
			const keyName = key.name;
			if (keyName === proxyKey) {
				continue;
			}
			if (key.expiration >= minExpiration) {
				continue;
			}
			converter = getConverter(keyName);
			if (!converter) {
				continue;
			}
			minExpiration = key.expiration;
			updateKey = key.name;
		}
	}
	if (!updateKey || !converter) {
		return;
	}
	const res = await request(urls);
	if (!res) {
		return;
	}
	const updateProxy = converter(res);
	await env.KV.put(updateKey, JSON.stringify(updateProxy), {
		expirationTtl: 3600
	});
	console.log("updated: " + updateKey);
	await generateProxy(env, updateKey, updateProxy);
}

async function request(urls) {
	let result = undefined;
	try {
		console.log("request: " + urls)
		let res = await axios.get(urls[0]);
		if (!res || res.status !== 200) {
			res = await axios.get(urls[1]);
		}
		result = res.data;
	} catch (err) {
		let res = await axios.get(urls[1]);
		if (res && res.status === 200) {
			result = res.data;
		}
	}
	return result;
}

async function generateProxy(env, updateKey, updateProxy) {
	const proxyConfig = {
		...template,
		"proxies": [],
		'proxy-groups': [
			{
				"name": "🚀 节点选择",
				"type": "select",
				"proxies": [
					"♻️ 自动选择",
					"DIRECT"
				]
			},
			{
				"name": "♻️ 自动选择",
				"type": "url-test",
				"url": "https://www.gstatic.com/generate_204",
				"interval": 300,
				"tolerance": 50,
				"proxies": []
			}
		]
	};
	const servers = new Set();
	const keyNameSerials = new Map();
	const lastData = await env.KV.get(proxyKey);
	// 缓存上一次的
	const proxyTemp = {};
	if (lastData && lastData.length > 0) {
		yaml.load(lastData).proxies.forEach(proxy => {
			let proxyName = proxy.name;
			proxyName = proxyName.substring(proxyName.lastIndexOf(')') + 1);
			proxyTemp[proxyName] = proxy;
		});
	}
	// 遍历KV中所有key
	const list = await env.KV.list();
	for (const key of list.keys) {
		const keyName = key.name;
		let proxies
		if (keyName === proxyKey) {
			// key为最终结果，直接跳过
			continue;
		} else if (keyName === updateKey) {
			// key为要更新的key
			proxies = updateProxy;
		} else {
			// key为不需要更新的key，直接从上一次缓存中拿
			const proxyStr = await env.KV.get(keyName);
			if (!proxyStr || proxyStr.length === 0) {
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
					proxyConfig["proxy-groups"].forEach(group => {
						group.proxies.push(proxyName);
					});
				}
				continue;
			}
			proxies = JSON.parse(proxyStr);
		}
		// 组装结果
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
			proxy.up = "20 Mbps";
			proxy.down = "80 Mbps";
			proxyConfig.proxies.push(proxy);
			proxyConfig['proxy-groups'].forEach(group => {
				group.proxies.push(proxyName);
			});
		}
	}
	// 标注序号
	for (let i = 0; i < proxyConfig.proxies.length; ++i) {
		const name = '(' + i + ')' + proxyConfig.proxies[i].name;
		proxyConfig.proxies[i].name = name;
		proxyConfig['proxy-groups'][0].proxies[i + 2] = name;
		proxyConfig['proxy-groups'][1].proxies[i] = name;
	}
	await env.KV.put(proxyKey, yaml.dump(proxyConfig), {
		expirationTtl: 3600
	});
}
