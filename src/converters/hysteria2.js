export default function(data) {
	const server = data.server;
	const index = server.lastIndexOf(':');
	let address = server.substring(0, index);
	if (address.startsWith('[')) {
		address = address.substring(1, address.length - 1);
	}
	const port = server.substring(index + 1);
	const tls = data.tls;
	const proxy = {
		'type': 'hysteria2',
		'server': address,
		'password': data.auth,
		'sni': tls.sni,
		'skip-cert-verify': tls.insecure,
		'protocol': data.protocol,
		'recv-window-conn': data['recv_window_conn'],
		'recv-window': data['recv_window']
	};
	const portSplit = port.split(',');
	proxy.port = portSplit[0];
	if (portSplit.length > 1) {
		proxy.ports = portSplit[1];
	}
	return [proxy];
}
