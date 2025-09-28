export default function(data) {
	const server = data.server.split(':');
	const address = server[0];
	const proxy = {
		"type": "hysteria",
		"server": address,
		"auth-str": data["auth_str"],
		"sni": data["server_name"],
		"skip-cert-verify": data["disable_mtu_discovery"],
		"protocol": data.protocol,
		"recv-window-conn": data["recv_window_conn"],
		"recv-window": data["recv_window"],
	}
	const port = server[1].split(',');
	proxy.port = port[0];
	if (port.length > 1) {
	} else {
		proxy.ports = port[1];
	}
	const alpn = data.alpn;
	if (alpn) {
		proxy.alpn = [alpn];
	}
	return [proxy];
}
