export default function(data) {
	const proxies = data.outbounds;
	const result = [];
	for (const proxy of proxies) {
		const tls = proxy.tls;
		result.push({
			"type": proxy.type,
			"server": proxy.server,
			"port": proxy["server_port"],
			"auth-str": proxy["auth_str"],
			"obfs": proxy.obfs,
			"sni": tls["server_name"],
			"alpn": tls.alpn,
			"skip-cert-verify": true
		});
	}
	return result;
}
