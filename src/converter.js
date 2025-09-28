import clashmeta from './converters/clashmeta';
import xray from './converters/xray';
import hysteria from './converters/hysteria';
import hysteria2 from './converters/hysteria2';
import singbox from './converters/singbox';
import mieru from './converters/mieru';

export default async function(key) {
	const method = key.substring(0, key.indexOf('_'));
	switch (method) {
		case 'clashmeta':
		case 'quick':
			return clashmeta;
		case 'xray':
			return xray;
		case 'hysteria':
			return hysteria;
		case 'hysteria2':
			return hysteria2;
		case 'singbox':
			return singbox;
		case 'mieru':
			return mieru;
		default:
			return undefined;
	}
}
