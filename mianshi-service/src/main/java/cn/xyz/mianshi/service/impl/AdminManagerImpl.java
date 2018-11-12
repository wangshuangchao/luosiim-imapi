package cn.xyz.mianshi.service.impl;


import javax.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.xyz.commons.utils.BeanUtils;
import cn.xyz.mianshi.service.AdminManager;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.repository.MongoRepository;
import cn.xyz.service.KXMPPServiceImpl;


@Service()
public class AdminManagerImpl extends MongoRepository implements AdminManager {

	
	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Autowired
	private UserManager userManager;



	@Override
	public Config getConfig() {
		Config config=null;
		config=KSessionUtil.getConfig();
		if(null==config){
			config = dsForRW.createQuery(Config.class).field("_id").notEqual(null).get();
			if(null==config)
				config=initConfig();
			KSessionUtil.setConfig(config);
			KSessionUtil.setEncrypt(config.getEncryptEnabled());
		}
		return config;
	}

	

	@Override
	public Config initConfig() {	
		Config config=new Config();
		try {
			config.XMPPDomain="im.shiku.co";
			config.XMPPHost="im.shiku.co";
			config.setApiUrl("http://imapi.shiku.co/");
			config.setDownloadAvatarUrl("http://file.shiku.co/");
			config.setDownloadUrl("http://file.shiku.co/");
			config.setUploadUrl("http://upload.shiku.co/");
			config.setLiveUrl("rtmp://v1.one-tv.com:1935/live/");
			config.setFreeswitch("120.24.211.24");
			config.setMeetingHost("120.24.211.24");
			config.setShareUrl("");
			config.setSoftUrl("");
			config.setHelpUrl("");
			config.setVideoLen("20");
			config.setAudioLen("20");
			config.setEncryptEnabled(0);
			dsForRW.save(config);
				
				userManager.addUser(10000, "10000");
				userManager.addUser(10005, "10005");
				userManager.addUser(10006, "10006");
				userManager.addUser(10007, "10007");
				userManager.addUser(10008, "10008");
				userManager.addUser(10009, "10009");
				userManager.addUser(10010, "10010");
				userManager.addUser(10011, "10011");
				userManager.addUser(10012, "10012");
				userManager.addUser(10013, "10013");
				userManager.addUser(10014, "10014");
				userManager.addUser(10015, "10015");
				KXMPPServiceImpl.getInstance().register("10005", DigestUtils.md5Hex("10005"));
				KXMPPServiceImpl.getInstance().register("10000", DigestUtils.md5Hex("10000"));
				KXMPPServiceImpl.getInstance().register("10006", DigestUtils.md5Hex("10006"));
				KXMPPServiceImpl.getInstance().register("10007", DigestUtils.md5Hex("10007"));
				KXMPPServiceImpl.getInstance().register("10008", DigestUtils.md5Hex("10008"));
				KXMPPServiceImpl.getInstance().register("10009", DigestUtils.md5Hex("10009"));
				KXMPPServiceImpl.getInstance().register("10010", DigestUtils.md5Hex("10010"));
				KXMPPServiceImpl.getInstance().register("10011", DigestUtils.md5Hex("10011"));
				KXMPPServiceImpl.getInstance().register("10012", DigestUtils.md5Hex("10012"));
				KXMPPServiceImpl.getInstance().register("10013", DigestUtils.md5Hex("10013"));
				KXMPPServiceImpl.getInstance().register("10014", DigestUtils.md5Hex("10014"));
				KXMPPServiceImpl.getInstance().register("10015", DigestUtils.md5Hex("10015"));
				return config;
			} catch (Exception e) {
				e.printStackTrace();
				return null==config?null:config;
			}
	}

	
	@Override
	public void setConfig(Config config) {
		/*DBCollection dbColl = dsForRW.getDB().getCollection("config");
		DBObject q = new BasicDBObject();
		// q.put("_id", new ObjectId("55b20f2bc6054581a0e3d7e9"));
		DBObject o = new BasicDBObject();
		o.put("$set", dbObj);*/
		Config dest=getConfig();
		BeanUtils.copyProperties(config,dest);
		dsForRW.save(dest);
		KSessionUtil.setConfig(dest);
		KSessionUtil.setEncrypt(config.getEncryptEnabled());
		
		
	}
	
	private void initDB(){
		// return (BasicDBObject) dsForRW
					// .getDB()
					// .getCollection("config")
					// .findOne(
					// new BasicDBObject("_id", new ObjectId(
					// "55b20f2bc6054581a0e3d7e9")));

					// for (String name : dsForRW.getDB().getCollectionNames()) {
					// if (name.contains("system.indexes"))
					// continue;
					// System.out.println("RESET COLLECTION：" + name);
					// DBCollection dbColl = dsForRW.getDB().getCollection(name);
					// dbColl.update(new BasicDBObject(), new BasicDBObject("$set",
					// new BasicDBObject("className", "")), false, true);
					// dbColl.update(new BasicDBObject(), new BasicDBObject("$unset",
					// new BasicDBObject("className", "")), false, true);
					// }
					//
					// for (String name : dsForTigase.getDB().getCollectionNames()) {
					// if (name.contains("system.indexes"))
					// continue;
					// System.out.println("RESET COLLECTION：" + name);
					// DBCollection dbColl = dsForTigase.getDB().getCollection(name);
					// dbColl.update(new BasicDBObject(), new BasicDBObject("$set",
					// new BasicDBObject("className", "")), false, true);
					// dbColl.update(new BasicDBObject(), new BasicDBObject("$unset",
					// new BasicDBObject("className", "")), false, true);
					// }
		
		
		
		
		
		/*Config obj = dbColl.findOne();
		if (null == obj) {
			
			BasicDBObject dbObj = new BasicDBObject();
			// dbObj.put("_id", new ObjectId("55b20f2bc6054581a0e3d7e9"));
			dbObj.put("XMPPDomain", "www.shiku.co");
			dbObj.put("XMPPHost", "www.shiku.co");
		
			dbObj.put("apiUrl", "http://imapi.youjob.co/");
			dbObj.put("downloadAvatarUrl", "http://file.shiku.co/");
			dbObj.put("downloadUrl", "http://file.shiku.co/");
			dbObj.put("uploadUrl", "http://upload.shiku.co/");
			
			dbObj.put("freeswitch", "120.24.211.24");
			dbObj.put("meetingHost", "120.24.211.24");
			
			dbObj.put("shareUrl", "");
			dbObj.put("softUrl", "");
			dbObj.put("helpUrl", "http,//www.youjob.co/wap/help");
			dbObj.put("videoLen", "");
			dbObj.put("audioLen", "");
			
			
			  DBObject versionInf = BasicDBObjectBuilder.start("disableVersion", "").add("version", "")
					.add("versionRemark", "").add("message", "").get();
			dbObj.put("ftpHost", "");
			dbObj.put("ftpPassword", "");
			dbObj.put("ftpUsername", "");
			dbObj.put("android", versionInf);
			dbObj.put("ios", versionInf);
			dbObj.put("buyUrl", "");
			dbObj.put("money",
					BasicDBObjectBuilder.start("isCanChange", 0).add("Login", 0).add("Share", 0).add("Intro", 0).get());
			dbObj.put("resumeBaseUrl", "http,//www.youjob.co/resume/wap");
			dbObj.put("aboutUrl", "");
			dbObj.put("website", "http,//www.shiku.co/");
			
			
			
			
			

			dbColl.save(dbObj);

			try {
				userManager.addUser(10000,"10000");
				userManager.addUser(10005,"10005");
				KXMPPServiceImpl.getInstance().register("10005", DigestUtils.md5Hex("10005"));
				KXMPPServiceImpl.getInstance().register("10000", DigestUtils.md5Hex("10000"));
				return dbColl.findOne();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else return  obj;*/
	}


}
