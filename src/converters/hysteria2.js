export default function(data) {
	const server = data.server;
	let address;
	let port;
	if (server.startsWith('[')) {
		const serverSplit = server.split(']');
		address = serverSplit[0].substring(1);
		port = serverSplit[1].substring(1);
	} else {
		const serverSplit = server.split(':');
		address = serverSplit[0];
		port = serverSplit[1];
	}
	const tls = data.tls;
	const proxy = {
		"type": "hysteria2",
		"server": address,
		"password": data.auth,
		"sni": tls.sni,
		"skip-cert-verify": tls.insecure,
		"protocol": data.protocol,
		"recv-window-conn": data["recv_window_conn"],
		"recv-window": data["recv_window"],
	}
	const portSplit = server[1].split(',');
	proxy.port = portSplit[0];
	if (portSplit.length > 1) {
	} else {
		proxy.ports = portSplit[1];
	}
	return [proxy];
}
