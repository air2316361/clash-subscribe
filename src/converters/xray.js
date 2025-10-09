export default function(data) {
	const proxies = data.outbounds;
	const result = [];
	for (const proxy of proxies) {
		if (proxy.tag !== 'proxy') {
			continue;
		}
		const protocol = proxy.protocol;
		const vnexts = proxy.settings.vnext;
		const streamSettings = proxy.streamSettings;
		const realitySettings = streamSettings.realitySettings;
		for (const vnext of vnexts) {
			const address = vnext.address;
			const user = vnext.users[0];
			result.push({
				'type': protocol,
				'server': address,
				'port': vnext.port,
				'uuid': user.id,
				'flow': 'xtls-rprx-vision',
				'packet-encoding': 'xudp',
				'tls': true,
				'servername': realitySettings.serverName,
				'alpn': ['h2', 'http/1.1'],
				'client-fingerprint': realitySettings.fingerprint,
				"skip-cert-verify": true,
				'reality-opts': {
					'public-key': realitySettings.publicKey,
					'short-id': realitySettings.shortId
				},
				'network': streamSettings.network
			});
		}
	}
	return result;
}
