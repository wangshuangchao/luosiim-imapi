package cn.xyz.commons.autoconfigure;

import java.util.List;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import cn.xyz.mianshi.vo.Language;

@Component
@ConfigurationProperties(prefix = "im")
public class KApplicationProperties {
	// ,locations="classpath:application-test.properties" //外网测试环境
	// ,locations="classpath:application-local.properties" //本地测试环境
	//// application

	public KApplicationProperties() {
		// TODO Auto-generated constructor stub
	}
	private PoolProperties poolConfig;
	private MongoConfig mongoConfig;
	private RedisConfig redisConfig;
	private XMPPConfig xmppConfig;
	private MySqlConfig mySqlConfig;
	private AppConfig appConfig;
	private SmsConfig smsConfig;
	private ALiSmsConfig aLiSmsConfig;
	
	/*private WXConfig wxConfig;*/

	public MySqlConfig getMySqlConfig() {
		return mySqlConfig;
	}

	public void setMySqlConfig(MySqlConfig mySqlConfig) {
		this.mySqlConfig = mySqlConfig;
	}

	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

	public PoolProperties getPoolConfig() {
		return poolConfig;
	}

	public void setPoolConfig(PoolProperties poolConfig) {
		this.poolConfig = poolConfig;
	}

	public MongoConfig getMongoConfig() {
		return mongoConfig;
	}

	public void setMongoConfig(MongoConfig mongoConfig) {
		this.mongoConfig = mongoConfig;
	}

	public RedisConfig getRedisConfig() {
		return redisConfig;
	}

	public void setRedisConfig(RedisConfig redisConfig) {
		this.redisConfig = redisConfig;
	}

	public XMPPConfig getXmppConfig() {
		return xmppConfig;
	}

	public void setXmppConfig(XMPPConfig xmppConfig) {
		this.xmppConfig = xmppConfig;
	}

	

	public SmsConfig getSmsConfig() {
		return smsConfig;
	}

	public void setSmsConfig(SmsConfig smsConfig) {
		this.smsConfig = smsConfig;
	}

	
	/*public WXConfig getWxConfig() {
		return wxConfig;
	}

	public void setWxConfig(WXConfig wxConfig) {
		this.wxConfig = wxConfig;
	}*/

	public ALiSmsConfig getaLiSmsConfig() {
		return aLiSmsConfig;
	}

	public void setaLiSmsConfig(ALiSmsConfig aLiSmsConfig) {
		this.aLiSmsConfig = aLiSmsConfig;
	}


	public static class MongoConfig {
		private List<String> host;
		private List<Integer> port;
		private String dbName;
		private String roomDbName;
		// 闁板秶鐤嗛弰顖氭儊娴ｈ法鏁ら梿鍡欏參濡�崇础 鐠囪鍟撻崚鍡欘瀲 0 閸楁洘婧� 濡�崇础 1閿涙岸娉︾紘銈喣佸锟�
		private int cluster = 0;
		private String username;
		private String password;
		private String url;

		public List<String> getHost() {
			return host;
		}

		public void setHost(List<String> host) {
			this.host = host;
		}

		public List<Integer> getPort() {
			return port;
		}

		public void setPort(List<Integer> port) {
			this.port = port;
		}

		public String getDbName() {
			return dbName;
		}

		public void setDbName(String dbName) {
			this.dbName = dbName;
		}

		public String getRoomDbName() {
			return roomDbName;
		}

		public void setRoomDbName(String roomDbName) {
			this.roomDbName = roomDbName;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public int getCluster() {
			return cluster;
		}

		public void setCluster(int cluster) {
			this.cluster = cluster;
		}

	}

	public static class RedisConfig {
		private int database = 0;
		private String host;
		private int port;
		private String password;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public int getDatabase() {
			return database;
		}

		public void setDatabase(int database) {
			this.database = database;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public static class XMPPConfig {
		private String host;
		private int port;
		private String username;
		private String password;
		private String dbhost;
		private int dbport;
		/** tigase数据库名 **/
		private String dbName;
		private String dbUsername;
		private String dbPassword;
		/** room数据库名 **/
		private String roomDbName;

		public String getDbhost() {
			return dbhost;
		}

		public void setDbhost(String dbhost) {
			this.dbhost = dbhost;
		}

		public int getDbport() {
			return dbport;
		}

		public void setDbport(int dbport) {
			this.dbport = dbport;
		}

		public String getDbName() {
			return dbName;
		}

		public void setDbName(String dbName) {
			this.dbName = dbName;
		}

		public String getDbUsername() {
			return dbUsername;
		}

		public void setDbUsername(String dbUsername) {
			this.dbUsername = dbUsername;
		}

		public String getDbPassword() {
			return dbPassword;
		}

		public void setDbPassword(String dbPassword) {
			this.dbPassword = dbPassword;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getRoomDbName() {
			return roomDbName;
		}

		public void setRoomDbName(String roomDbName) {
			this.roomDbName = roomDbName;
		}

		
	}

	public static class Open189Config {
		private String app_id;
		private String app_secret;
		private String app_template_id_invite;
		private String app_template_id_random;
		private String template_id;

		public String getApp_id() {
			return app_id;
		}

		public void setApp_id(String app_id) {
			this.app_id = app_id;
		}

		public String getApp_secret() {
			return app_secret;
		}

		public void setApp_secret(String app_secret) {
			this.app_secret = app_secret;
		}

		public String getApp_template_id_invite() {
			return app_template_id_invite;
		}

		public void setApp_template_id_invite(String app_template_id_invite) {
			this.app_template_id_invite = app_template_id_invite;
		}

		public String getApp_template_id_random() {
			return app_template_id_random;
		}

		public void setApp_template_id_random(String app_template_id_random) {
			this.app_template_id_random = app_template_id_random;
		}

		public String getTemplate_id() {
			return template_id;
		}

		public void setTemplate_id(String template_id) {
			this.template_id = template_id;
		}
	}

	public static class MySqlConfig {
		private String url;
		private String user;
		private String password;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public static class AppConfig {
		private String uploadDomain = "http://upload.shiku.co";// 娑撳﹣绱堕張宥呭閸ｃ劌鐓欓崥锟�
		private String apns;
		private List<Language> languages; // 鐠囶叀鈻�
		private int openTask = 1;// 閺勵垰鎯佸锟介崥顖氱暰閺冩湹鎹㈤崝锟�
		private int distance = 20;

		public String getUploadDomain() {
			return uploadDomain;
		}

		public void setUploadDomain(String uploadDomain) {
			this.uploadDomain = uploadDomain;
		}

		public String getApns() {
			return apns;
		}

		public void setApns(String apns) {
			this.apns = apns;
		}

		public int getOpenTask() {
			return openTask;
		}

		public void setOpenTask(int openTask) {
			this.openTask = openTask;
		}

		public List<Language> getLanguages() {
			return languages;
		}

		public void setLanguages(List<Language> languages) {
			this.languages = languages;
		}

		public int getDistance() {
			return distance;
		}

		public void setDistance(int distance) {
			this.distance = distance;
		}
	}

	public static class ALiSmsConfig{
		private int openALiSMS = 1;// 是否启用
		private String accessId;
		private String accessKey;
		private String codeTemplate;
		private String signName;
		public int getOpenALiSMS() {
			return openALiSMS;
		}
		public void setOpenALiSMS(int openALiSMS) {
			this.openALiSMS = openALiSMS;
		}
		public String getAccessId() {
			return accessId;
		}
		public void setAccessId(String accessId) {
			this.accessId = accessId;
		}
		public String getAccessKey() {
			return accessKey;
		}
		public void setAccessKey(String accessKey) {
			this.accessKey = accessKey;
		}
		public String getCodeTemplate() {
			return codeTemplate;
		}
		public void setCodeTemplate(String codeTemplate) {
			this.codeTemplate = codeTemplate;
		}
		public String getSignName() {
			return signName;
		}
		public void setSignName(String signName) {
			this.signName = signName;
		}
		
		
		
	}
	
	
	public static class SmsConfig {

		private int openSMS = 1;// 閺勵垰鎯侀崣鎴︼拷浣虹叚娣囷繝鐛欑拠浣虹垳
		private String host;
		private int port;
		private String api;
		private String username;// 閻厺淇婇獮鍐插酱閻€劍鍩涢崥锟�
		private String password;//// 閻厺淇婇獮鍐插酱鐎靛棛鐖�

		public int getOpenSMS() {
			return openSMS;
		}

		public void setOpenSMS(int openSMS) {
			this.openSMS = openSMS;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getApi() {
			return api;
		}

		public void setApi(String api) {
			this.api = api;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

	}

	public static class WXConfig {
		// 閸忣兛绱拹锕�褰縜ppid
		private String appid;
		// 閸熷棙鍩涢崣锟�
		private String mchid;

		private String secret;

		public String getAppid() {
			return appid;
		}

		public void setAppid(String appid) {
			this.appid = appid;
		}

		public String getMchid() {
			return mchid;
		}

		public void setMchid(String mchid) {
			this.mchid = mchid;
		}

		public String getSecret() {
			return secret;
		}

		public void setSecret(String secret) {
			this.secret = secret;
		}

	}
}
