import update from './update';
import proxyKey from "./proxy_key";

export default {
	async fetch(request, env, ctx) {
		const value = await env.KV.get(proxyKey);
		return new Response(value);
	},
	async scheduled(controller, env, ctx) {
		await update(env);
	}
};
