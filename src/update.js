import axios from 'axios';
import template from './template';
import yaml from 'js-yaml';
import getConverter from './converter';

import proxyKey from './proxy_key';

export default async function(env) {
	const kv = env.KV;
	const list = await kv.list();
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
	foreachConfig(env, (key, value) => {
		if (keySet.has(key)) {
			return;
		}
		converter = getConverter(key);
		if (converter) {
			updateKey = key;
			urls = value;
			return true;
		}
	});
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
	await kv.put(updateKey, JSON.stringify(updateProxy), {
		expirationTtl: 1800
	});
	await generateProxy(kv, updateKey, updateProxy);
}

function foreachConfig(env, handler) {
	for (const envName of env.ENV_NAMES) {
		const config = env[envName];
		for (const key in config) {
			if (handler(key, config[key])) {
				return;
			}
		}
	}
}

async function request(urls) {
	let result = undefined;
	try {
		let res = await axios.get(urls[0]);
		if (!res || res.status !== 200) {
			res = await axios.get(urls[1]);
		}
		result = res.data;
	} catch (err) {
		let res = await axios.get(urls[1]);
		if (res) {
			result = res.data;
		}
	}
	return result;
}

async function generateProxy(kv, updateKey, updateProxy) {
	const proxyConfig = { ...template };
	proxyConfig.proxies = [];
	const proxyGroups = proxyConfig['proxy-groups'];
	proxyGroups[0].proxies = ['♻️ 自动选择', 'DIRECT'];
	proxyGroups[1].proxies = [];
	const servers = new Set();
	const keyNameSerials = new Map();
	const list = await kv.list();
	for (const key of list.keys) {
		const keyName = key.name;
		let proxies
		if (keyName === proxyKey) {
			continue;
		} else if (keyName === updateKey) {
			proxies = updateProxy;
		} else {
			const proxyStr = await kv.get(keyName);
			proxies = JSON.parse(proxyStr);
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
			proxyConfig.proxies.push(proxy);
			proxyGroups.forEach(group => {
				group.proxies.push(proxyName);
			});
		}
	}
	await kv.put(proxyKey, yaml.dump(proxyConfig), {
		expirationTtl: 360
	});
}
