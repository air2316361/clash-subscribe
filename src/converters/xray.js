export default function(data) {
	const proxies = data.outbounds;
	const result = [];
	for (const proxy of proxies) {
		if (proxy.tag !== "proxy") {
			continue;
		}
		const protocol =  proxy.protocol;
		const vnexts = proxy.settings.vnext;
		const streamSettings = proxy.streamSettings;
		const realitySettings = streamSettings.realitySettings;
		for (const vnext of vnexts) {
			const address = vnext.address;
			const user = vnext.users[0];
			result.push({
				"type": protocol,
				"server": address,
				"port": vnext.port,
				"username": user.id,
				"network": streamSettings.network,
				"tls": true,
				"flow": user.flow,
				"servername": realitySettings.serverName,
				"reality-opts": {
					"public-key": realitySettings.publicKey,
					"short-id": realitySettings.shortId
				},
				"client-fingerprint": realitySettings.fingerprint
			});
		}
	}
	return result;
}
