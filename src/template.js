export default {
	"secret": "dongtaiwang.com",
	"mixed-port": 7890,
	"allow-lan": false,
	"log-level": "info",
	"dns": {
		"enabled": true,
		"nameserver": [
			"119.29.29.29",
			"223.5.5.5"
		],
		"fallback-filter": {
			"geoip": false,
			"ipcidr": [
				"240.0.0.0/4",
				"0.0.0.0/32"
			]
		}
	},
	"rule-providers": {
		"LocalAreaNetwork": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/LocalAreaNetwork.list",
			"path": "./ruleset/LocalAreaNetwork.yaml",
			"interval": 86400
		},
		"UnBan": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/UnBan.list",
			"path": "./ruleset/UnBan.yaml",
			"interval": 86400
		},
		"GameServer": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/air2316361/rules@master/My-Direct.txt",
			"path": "./ruleset/GameServer.yaml",
			"interval": 86400
		},
		"BanAD": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/BanAD.list",
			"path": "./ruleset/BanAD.yaml",
			"interval": 86400
		},
		"BanProgramAD": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/BanProgramAD.list",
			"path": "./ruleset/BanProgramAD.yaml",
			"interval": 86400
		},
		"BanLog": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/air2316361/rules@master/My-Reject.txt",
			"path": "./ruleset/BanLog.yaml",
			"interval": 86400
		},
		"GoogleCN": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/GoogleCN.list",
			"path": "./ruleset/GoogleCN.yaml",
			"interval": 86400
		},
		"SteamCN": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/SteamCN.list",
			"path": "./ruleset/SteamCN.yaml",
			"interval": 86400
		},
		"Telegram": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/Telegram.list",
			"path": "./ruleset/Telegram.yaml",
			"interval": 86400
		},
		"ProxyMedia": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/ProxyMedia.list",
			"path": "./ruleset/ProxyMedia.yaml",
			"interval": 86400
		},
		"ProxyLite": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/ProxyLite.list",
			"path": "./ruleset/ProxyLite.yaml",
			"interval": 86400
		},
		"ChinaDomain": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/ChinaDomain.list",
			"path": "./ruleset/ChinaDomain.yaml",
			"interval": 86400
		},
		"ChinaCompanyIp": {
			"type": "http",
			"behavior": "classical",
			"format": "text",
			"url": "https://cdn.jsdelivr.net/gh/ACL4SSR/ACL4SSR@master/Clash/ChinaCompanyIp.list",
			"path": "./ruleset/ChinaCompanyIp.yaml",
			"interval": 86400
		}
	},
	"rules": [
		"RULE-SET,LocalAreaNetwork,DIRECT",
		"RULE-SET,UnBan,DIRECT",
		"RULE-SET,GameServer,DIRECT",
		"RULE-SET,BanAD,REJECT",
		"RULE-SET,BanProgramAD,REJECT",
		"RULE-SET,BanLog,REJECT",
		"RULE-SET,GoogleCN,DIRECT",
		"RULE-SET,SteamCN,DIRECT",
		"RULE-SET,Telegram,🚀 节点选择",
		"RULE-SET,ProxyMedia,🚀 节点选择",
		"RULE-SET,ProxyLite,🚀 节点选择",
		"RULE-SET,ChinaDomain,DIRECT",
		"RULE-SET,ChinaCompanyIp,DIRECT",
		"GEOIP,LAN,DIRECT",
		"GEOIP,CN,DIRECT",
		"MATCH,🚀 节点选择"
	]
}
