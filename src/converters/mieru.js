export default function(data) {
	const profiles = data.profiles;
	const result = [];
	for (const profile of profiles) {
		const user = profile.user;
		const servers = profile.servers;
		for (const server of servers) {
			result.push({
				"type": "mieru",
				"server": server.ipAddress,
				"port": server.portBindings[0].port,
				"transport": "tcp",
				"username": user.name,
				"password": user.password
			});
		}
	}
	return result;
}
