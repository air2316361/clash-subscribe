import axios from 'axios';
import template from './template';
import yaml from 'js-yaml';
import getConverter from './converter';

import proxyKey from './proxy_key';

export default async function(env) {
	const config = env.PROXY_CONFIG;
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
	for (const key in config) {
		if (keySet.has(key)) {
			continue;
		}
		converter = await getConverter(key);
		if (converter) {
			updateKey = key;
			break;
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
			converter = await getConverter(keyName);
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
	console.log("updating " + updateKey);
	const res = await request(config[updateKey]);
	await kv.put(updateKey, JSON.stringify(converter(res)), {
		expirationTtl: 1800
	});
	await generateProxy(kv);
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

async function generateProxy(kv) {
	const proxyConfig = { ...template };
	proxyConfig.proxies = [];
	const proxyGroups = proxyConfig['proxy-groups'];
	proxyGroups[0].proxies = ["♻️ 自动选择", "DIRECT"];
	proxyGroups[1].proxies = [];
	const servers = new Set();
	const list = await kv.list();
	for (const key of list.keys) {
		const keyName = key.name;
		if (keyName === proxyKey) {
			continue;
		}
		const proxyStr = await kv.get(keyName);
		const proxies = JSON.parse(proxyStr);
		for (const proxy of proxies) {
			const serverName = proxy.server + "|" + proxy.port;
			if (servers.has(serverName)) {
				continue;
			}
			servers.add(serverName);
			proxy.name = keyName;
			proxyConfig.proxies.push(proxy);
			proxyGroups.forEach(group => {
				group.proxies.push(serverName);
			});
		}
	}
	await kv.put(proxyKey, yaml.dump(proxyConfig), {
		expirationTtl: 360
	});
}
