import yaml from "js-yaml";

export default function(data) {
	const json = yaml.load(data);
	const outbound = json.outbound;
	const addr = outbound.addr;
	const index = addr.lastIndexOf(':');
	const server = addr.substring(0, index);
	const port = addr.substring(index + 1);
	delete outbound.addr;
	outbound.server = server;
	outbound.port = port;
	return [outbound];
}
