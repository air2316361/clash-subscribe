import yaml from "js-yaml";

export default function(data) {
	const json = yaml.load(data);
	return json.proxies;
}
