package cn.xyz.repository.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KKeyConstant;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.AESUtil;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ValueUtil;
import cn.xyz.mianshi.example.UserExample;
import cn.xyz.mianshi.example.UserQueryExample;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.User.UserSettings;
import cn.xyz.repository.UserRepository;
import cn.xyz.service.KXMPPServiceImpl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

@Service
public class UserRepositoryImpl extends BaseRepositoryImpl<User, Integer> implements UserRepository {

	public static UserRepositoryImpl getInstance() {
		return new UserRepositoryImpl();
	}

	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRoom")
	protected Datastore dsForRoom;

	@Override
	public Map<String, Object> addUser(int userId, UserExample example) {
		BasicDBObject jo = new BasicDBObject();
		jo.put("_id", userId);// 索引
		jo.put("userKey", DigestUtils.md5Hex(example.getPhone()));// 索引
		jo.put("username", "");
		jo.put("password", example.getPassword());
		
		//新增 头像
		jo.put("portrait", example.getPortrait());
		
		jo.put("userType", ValueUtil.parse(example.getUserType()));
		jo.put("lsId", example.getPhone());// 罗斯Id
		jo.put("isRename", 0);// 是否修改过罗斯号,默认为0
		jo.put("isCS", 0);// 是否绑定公众号
		jo.put("telephone", example.getTelephone());// 索引
		jo.put("phone", example.getPhone());// 索引
		jo.put("areaCode", example.getAreaCode());// 索引
		jo.put("name", ValueUtil.parse(example.getName()));// 索引
		jo.put("nickname", ValueUtil.parse(example.getNickname()));// 索引
		jo.put("description", ValueUtil.parse(example.getDescription()));
		jo.put("birthday", ValueUtil.parse(example.getBirthday()));// 索引
		jo.put("sex", ValueUtil.parse(example.getSex()));// 索引
		jo.put("loc", new BasicDBObject("lng", example.getLongitude()).append("lat", example.getLatitude()));// 索引

		jo.put("countryId", ValueUtil.parse(example.getCountryId()));
		jo.put("provinceId", ValueUtil.parse(example.getProvinceId()));
		jo.put("cityId", ValueUtil.parse(example.getCityId()));
		jo.put("areaId", ValueUtil.parse(example.getAreaId()));

		jo.put("balance", 0);
		jo.put("totalRecharge", 0);

		jo.put("level", 0);
		jo.put("vip", 0);

		jo.put("friendsCount", 0);
		jo.put("fansCount", 0);
		jo.put("attCount", 0);
		jo.put("msgNum", 0);

		jo.put("createTime", DateUtil.currentTimeSeconds());
		jo.put("modifyTime", DateUtil.currentTimeSeconds());

		jo.put("idcard", "");
		jo.put("idcardUrl", "");

		jo.put("isAuth", 0);
		jo.put("status", 1);
		jo.put("onlinestate", 1);
		// 初始化登录日志
		jo.put("loginLog", User.LoginLog.init(example, true));
		// 初始化用户设置
		jo.put("settings", User.UserSettings.getDefault());

		// 1、新增用户
		dsForRW.getDB().getCollection("user").save(jo);

		try {
			// 2、缓存用户认证数据到
			Map<String, Object> data = saveAT(userId, jo.getString("userKey"));
			data.put("userId", userId);
			data.put("nickname", jo.getString("nickname"));
			data.put("portrait", jo.getString("portrait"));
			data.put("name", jo.getString("name"));
			data.put("lsId", jo.getString("lsId"));
			// 3、缓存用户数据

			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public void addUser(User user) {
		dsForRW.save(user);
	}

	@Override
	public void addUser(int userId, String password) {
		BasicDBObject jo = new BasicDBObject();
		jo.put("_id", userId);// 索引
		jo.put("userKey", DigestUtils.md5Hex(userId + ""));// 索引
		jo.put("username", String.valueOf(userId));
		jo.put("password", DigestUtils.md5Hex(password));

		jo.put("lsId", String.valueOf(userId));// 罗斯Id
		jo.put("isRename", 0);// 是否修改过罗斯号,默认为0

		jo.put("userType", ValueUtil.parse(1));
		jo.put("companyId", ValueUtil.parse(0));
		jo.put("telephone", "86" + String.valueOf(userId));// 索引
		jo.put("areaCode", "86");// 索引
		jo.put("name", String.valueOf(userId));// 索引
		jo.put("nickname", String.valueOf(userId));// 索引
		jo.put("description", String.valueOf(userId));
		jo.put("birthday", ValueUtil.parse(userId));// 索引
		jo.put("sex", ValueUtil.parse(userId));// 索引
		jo.put("loc", new BasicDBObject("lng", 10.00).append("lat", 10.00));// 索引

		jo.put("countryId", ValueUtil.parse(0));
		jo.put("provinceId", ValueUtil.parse(0));
		jo.put("cityId", ValueUtil.parse(400300));
		jo.put("areaId", ValueUtil.parse(0));

		jo.put("money", 0);
		jo.put("moneyTotal", 0);

		jo.put("level", 0);
		jo.put("vip", 0);

		jo.put("friendsCount", 0);
		jo.put("fansCount", 0);
		jo.put("attCount", 0);

		jo.put("createTime", DateUtil.currentTimeSeconds());
		jo.put("modifyTime", DateUtil.currentTimeSeconds());

		jo.put("idcard", "");
		jo.put("idcardUrl", "");

		jo.put("isAuth", 0);
		jo.put("status", 1);
		jo.put("onlinestate", 1);
		// 初始化登录日志
		// jo.put("loginLog", User.LoginLog.init(example, true));
		// 初始化用户设置
		jo.put("settings", User.UserSettings.getDefault());

		// 1、新增用户
		dsForRW.getDB().getCollection("user").save(jo);

	}

	@Override
	public List<User> findByTelephone(List<String> telephoneList) {
		Query<User> query = dsForRW.createQuery(User.class).filter("telephone in", telephoneList);
		return query.asList();
	}

	@Override
	public Map<String, Object> getAT(int userId, String userKey) {
		Jedis resource = jedisPool.getResource();

		try {
			String atKey = KKeyConstant.atKey(userKey);
			String accessToken = resource.get(atKey);
			// long expires_in = resource.ttl(atKey);
			if (StringUtil.isEmpty(accessToken)) {
				// return saveAT(userId, userKey);
			} else {
				// HashMap<String, Object> data = new HashMap<String, Object>();
				//
				// data.put("access_token", accessToken);
				// data.put("expires_in", expires_in);
				//
				// return data;
				String userIdKey = KKeyConstant.userIdKey(accessToken);
				resource.del(userIdKey);
				resource.del(atKey);
			}
		} finally {
			jedisPool.returnResource(resource);
		}
		return saveAT(userId, userKey);
	}

	@Override
	public long getCount(String telephone) {
		return dsForRW.createQuery(User.class).field("telephone").equal(telephone).countAll();
	}

	@Override
	public User.LoginLog getLogin(int userId) {
		try {
			return dsForRW.createQuery(User.class).field("_id").equal(userId).get().getLoginLog();
		} catch (NullPointerException e) {
			throw new ServiceException("帐号不存在");
		}

	}

	@Override
	public User.UserSettings getSettings(int userId) {
		UserSettings settings = null;
		User user = null;
		user = dsForRW.createQuery(User.class).field("_id").equal(userId).get();
		if (null == user)
			return null;
		settings = user.getSettings();
		return null != settings ? settings : new UserSettings();

	}

	@Override
	public User getUser(int userId) {
		Query<User> query = dsForRW.createQuery(User.class).field("_id").equal(userId);
		return query.get();
	}

	@Override
	public User getUser(String telephone) {
		Query<User> query = dsForRW.createQuery(User.class).field("telephone").equal(telephone);

		return query.get();
	}

	@Override
	public User getUser(String areaCode, String userKey, String password) {
		Query<User> query = dsForRW.createQuery(User.class);
		if (!StringUtil.isEmpty(userKey))
			query.field("areaCode").equal(areaCode);
		if (!StringUtil.isEmpty(userKey))
			query.field("userKey").equal(userKey);
		if (!StringUtil.isEmpty(password))
			query.field("password").equal(password);

		return query.get();
	}

	@Override
	public User getUserv1(String userKey, String password) {
		Query<User> query = dsForRW.createQuery(User.class);
		if (!StringUtil.isEmpty(userKey))
			query.field("userKey").equal(userKey);
		if (!StringUtil.isEmpty(password))
			query.field("password").equal(password);

		return query.get();
	}

	@Override
	public List<DBObject> queryUser(UserQueryExample example) {
		List<DBObject> list = Lists.newArrayList();
		// Query<User> query = mongoDs.find(User.class);
		// Query<User> query =mongoDs.createQuery(User.class);
		// query.filter("_id<", param.getUserId());
		DBObject ref = new BasicDBObject();
		if (null != example.getUserId())
			ref.put("_id", new BasicDBObject("$lt", example.getUserId()));
		if (!StringUtil.isEmpty(example.getNickname()))
			ref.put("nickname", Pattern.compile(example.getNickname()));
		if (null != example.getSex())
			ref.put("sex", example.getSex());
		if (null != example.getStartTime())
			ref.put("birthday", new BasicDBObject("$gte", example.getStartTime()));
		if (null != example.getEndTime())
			ref.put("birthday", new BasicDBObject("$lte", example.getEndTime()));
		DBObject fields = new BasicDBObject();
		fields.put("userKey", 0);
		fields.put("password", 0);
		fields.put("money", 0);
		fields.put("moneyTotal", 0);
		fields.put("status", 0);
		DBCursor cursor = dsForRW.getDB().getCollection("user").find(ref, fields).sort(new BasicDBObject("_id", -1))
				.limit(example.getPageSize());
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			obj.put("userId", obj.get("_id"));
			obj.removeField("_id");

			list.add(obj);
		}

		return list;
	}

	@Override
	public List<DBObject> findUser(int pageIndex, int pageSize) {
		List<DBObject> list = Lists.newArrayList();
		DBObject fields = new BasicDBObject();
		fields.put("userKey", 0);
		fields.put("password", 0);
		fields.put("money", 0);
		fields.put("moneyTotal", 0);
		fields.put("status", 0);
		DBCursor cursor = dsForRW.getDB().getCollection("user").find(null, fields).sort(new BasicDBObject("_id", -1))
				.skip(pageIndex * pageSize).limit(pageSize);
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			obj.put("userId", obj.get("_id"));
			obj.removeField("_id");

			list.add(obj);
		}

		return list;
	}

	@Override
	public Map<String, Object> saveAT(int userId, String userKey) {
		Jedis resource = jedisPool.getResource();
		Pipeline pipeline = resource.pipelined();

		try {
			String accessToken = StringUtil.randomUUID();
			// 密钥key
			String secretKey = String.format(KSessionUtil.GET_SECRET_KEY, userId);
			// 生成信息加密密钥
			String secret = userId + StringUtil.randomUUID().substring(0, 8);
			int expire = KConstants.Expire.DAY7 * 5;
			String atKey = KKeyConstant.atKey(userKey);
			pipeline.set(atKey, accessToken);
			pipeline.expire(atKey, expire);

			pipeline.set(secretKey, secret);
			pipeline.expire(secretKey, expire);
			
			String userIdKey = KKeyConstant.userIdKey(accessToken);
			pipeline.set(userIdKey, String.valueOf(userId));
			pipeline.expire(userIdKey, expire);

			pipeline.syncAndReturnAll();
			//获取加密密钥的秘钥
			String k=Md5Util.md5Hex(accessToken+AESUtil.key).substring(0, 16);
			// 对密钥加密
			String mdSecret = AESUtil.encrypt(secret,k);
			HashMap<String, Object> data = new HashMap<String, Object>();

			data.put("access_token", accessToken);
			data.put("AESSecret", mdSecret);
			data.put("expires_in", expire);
			// data.put("userId", userId);
			// data.put("nickname", user.getNickname());

			return data;
		} finally {
			jedisPool.returnResource(resource);
		}
	}

	
	public Map<String, Object> saveATWeb(int userId, String userKey) {
		Jedis resource = jedisPool.getResource();
		Pipeline pipeline = resource.pipelined();

		try {
			String accessToken = StringUtil.randomUUID();
			int expire = KConstants.Expire.DAY7 * 5;
			String atKey = KKeyConstant.atKey(userKey);
			pipeline.set(atKey, accessToken);
			pipeline.expire(atKey, expire);

			String userIdKey = KKeyConstant.userIdKey(accessToken);
			pipeline.set(userIdKey, String.valueOf(userId));
			pipeline.expire(userIdKey, expire);

			pipeline.syncAndReturnAll();
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("access_token", accessToken);
			data.put("expires_in", expire);
			return data;
		} finally {
			jedisPool.returnResource(resource);
		}
	}
	
	
	@Override
	public void updateLogin(int userId, String serial) {
		DBObject value = new BasicDBObject();
		// value.put("isFirstLogin", 0);
		// value.put("loginTime", DateUtil.currentTimeSeconds());
		// value.put("apiVersion", example.getApiVersion());
		// value.put("osVersion", example.getOsVersion());
		// value.put("model", example.getModel());
		value.put("serial", serial);
		// value.put("latitude", example.getLatitude());
		// value.put("longitude", example.getLongitude());
		// value.put("location", example.getLocation());
		// value.put("address", example.getAddress());

		DBObject q = new BasicDBObject("_id", userId);
		DBObject o = new BasicDBObject("$set", new BasicDBObject("loginLog", value));
		dsForRW.getDB().getCollection("user").update(q, o);
	}

	@Override
	public void updateLogin(int userId, UserExample example) {
		BasicDBObject loc = new BasicDBObject(2);
		loc.put("loc.lng", example.getLongitude());
		loc.put("loc.lat", example.getLatitude());

		DBObject values = new BasicDBObject();
		values.put("loginLog.isFirstLogin", 0);
		values.put("loginLog.loginTime", DateUtil.currentTimeSeconds());
		values.put("loginLog.apiVersion", example.getApiVersion());
		values.put("loginLog.osVersion", example.getOsVersion());
		values.put("loginLog.model", example.getModel());
		values.put("loginLog.serial", example.getSerial());
		values.put("loginLog.latitude", example.getLatitude());
		values.put("loginLog.longitude", example.getLongitude());
		values.put("loginLog.location", example.getLocation());
		values.put("loginLog.address", example.getAddress());
		values.put("loc.lng", example.getLongitude());
		values.put("loc.lat", example.getLatitude());
		values.put("appId", example.getAppId());

		DBObject q = new BasicDBObject("_id", userId);
		DBObject o = new BasicDBObject("$set", values);
		// ("loginLog",
		// loginLog)).append
		dsForRW.getCollection(User.class).update(q, o);

	}

	@Override
	public User updateUser(int userId, UserExample example) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		boolean updateName = false;
		boolean updatePortrait = false;
		if (null != example.getUserType())
			ops.set("userType", example.getUserType());

		if (!StringUtil.isEmpty(example.getNickname())) {
			ops.set("nickname", example.getNickname());
			updateName = true;
		}

		if (!StringUtil.isEmpty(example.getTelephone())) {
			ops.set("userKey", DigestUtils.md5Hex(example.getPhone()));
			ops.set("telephone", example.getTelephone());
		}
		if (!StringUtil.isEmpty(example.getPhone()))
			ops.set("phone", example.getPhone());

		if (!StringUtil.isEmpty(example.getPassword()) && example.getPassword().length() < 32) {
			System.out.println("修改密码");
			ops.set("password", example.getPassword());
			// 修改tigase中的用户信息
			KXMPPServiceImpl.getInstance().updateToTig(userId, example.getPassword());
		}

		if (!StringUtil.isEmpty(example.getDescription()))
			ops.set("description", example.getDescription());
		if (null != example.getBirthday())
			ops.set("birthday", example.getBirthday());
		if (null != example.getSex())
			ops.set("sex", example.getSex());
		if (null != example.getCountryId())
			ops.set("countryId", example.getCountryId());
		if (null != example.getProvinceId())
			ops.set("provinceId", example.getProvinceId());
		if (null != example.getCityId())
			ops.set("cityId", example.getCityId());
		if (null != example.getAreaId())
			ops.set("areaId", example.getAreaId());
		
		
		//修改头像相关
		if (null != example.getPortrait()){//头像信息不为空
			ops.set("portrait", example.getPortrait());
			updatePortrait=true;
		}

		if (null != example.getName())
			ops.set("name", example.getName());

		if (null != example.getIdcard())
			ops.set("idcard", example.getIdcard());
		if (null != example.getIdcardUrl())
			ops.set("idcardUrl", example.getIdcardUrl());
		ops.set("loc.lng", example.getLongitude());
		ops.set("loc.lat", example.getLatitude());

		User user = dsForRW.findAndModify(q, ops);
		// 删除redis中的用户
		KSessionUtil.deleteUserByUserId(userId);
		// 修改用户昵称时 同步该用户创建的群主昵称
		if (updateName) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					BasicDBObject quserId = new BasicDBObject().append("userId", userId);
					DBObject values = new BasicDBObject();
					values.put("nickname", example.getNickname());
					DBObject q = new BasicDBObject("$set", values);
					BasicDBObject qtoUserId = new BasicDBObject().append("toUserId", userId);
					DBObject values1 = new BasicDBObject();
					values1.put("toNickname", example.getNickname());
					DBObject o = new BasicDBObject("$set", values1);
					// 修改群组中的创建人名称//修改nickname
					dsForRoom.getCollection(Room.class).update(quserId, q, false, true);
					dsForRoom.getCollection(Member.class).update(quserId, q, false, true);
					// 修改好友名称
					dsForRW.getCollection(Friends.class).update(qtoUserId, o, false, true);
					// 修改朋友圈中的用户名称
					DBObject p = new BasicDBObject("$set", values);
					dsForRW.getCollection(Msg.class).update(quserId, p, false, true);
				}

			}).start();

		}
		//如果修改了头像,则同步,好友列表,房间信息中的头像信息
		if (updatePortrait) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					BasicDBObject quserId = new BasicDBObject().append("userId", userId);
					DBObject values = new BasicDBObject();
					values.put("portrait", example.getPortrait());
					DBObject q = new BasicDBObject("$set", values);
					BasicDBObject qtoUserId = new BasicDBObject().append("toUserId", userId);
					DBObject values1 = new BasicDBObject();
					values1.put("toPortrait", example.getPortrait());
					DBObject o = new BasicDBObject("$set", values1);
					// 修改群组中的创建人名称//修改nickname
					dsForRoom.getCollection(Room.class).update(quserId, q, false, true);
					dsForRoom.getCollection(Member.class).update(quserId, q, false, true);
					// 修改好友名称
					dsForRW.getCollection(Friends.class).update(qtoUserId, o, false, true);
					// 修改朋友圈中的用户名称
					DBObject p = new BasicDBObject("$set", values);
					dsForRW.getCollection(Msg.class).update(quserId, p, false, true);
				}

			}).start();

		}
		return user;

	}

	@Override
	public User updateSettings(int userId, User.UserSettings userSettings) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		if (null != new Integer(userSettings.getAllowAtt()))
			ops.set("settings.allowAtt", userSettings.getAllowAtt());
		if (null != new Integer(userSettings.getAllowGreet()))
			ops.set("settings.allowGreet", userSettings.getAllowGreet());
		if (null != new Integer(userSettings.getAllowAtt()))
			ops.set("settings.friendsVerify", userSettings.getFriendsVerify());
		// 是否开启客服模式
		if (null != new Integer(userSettings.getAllowAtt())) {
			ops.set("settings.openService", userSettings.getOpenService());
		}
		User user = getUser(userId);
		user.setSettings(userSettings);
		KSessionUtil.saveUserByUserId(userId, user); // 数据缓存到redis
		return dsForRW.findAndModify(q, ops);
	}

	@Override
	public User updateUser(User user) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(user.getUserId());
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);

		if (!StringUtil.isNullOrEmpty(user.getTelephone())) {
			ops.set("userKey", DigestUtils.md5Hex(user.getTelephone()));
			ops.set("telephone", user.getTelephone());
		}
		if (!StringUtil.isNullOrEmpty(user.getUsername()))
			ops.set("username", user.getUsername());
		/*
		 * if (!StringUtil.isNullOrEmpty(user.getPassword()))
		 * ops.set("password", user.getPassword());
		 */

		if (null != user.getUserType())
			ops.set("userType", user.getUserType());

		if (!StringUtil.isNullOrEmpty(user.getName()))
			ops.set("name", user.getName());
		if (!StringUtil.isNullOrEmpty(user.getNickname()))
			ops.set("nickname", user.getNickname());
		if (!StringUtil.isNullOrEmpty(user.getDescription()))
			ops.set("description", user.getDescription());
		if (!StringUtil.isNullOrEmpty(user.getPortrait()))
			ops.set("portrait", user.getPortrait());
		if (null != user.getBirthday())
			ops.set("birthday", user.getBirthday());
		if (null != user.getSex())
			ops.set("sex", user.getSex());

		if (null != user.getCountryId())
			ops.set("countryId", user.getCountryId());
		if (null != user.getProvinceId())
			ops.set("provinceId", user.getProvinceId());
		if (null != user.getCityId())
			ops.set("cityId", user.getCityId());
		if (null != user.getAreaId())
			ops.set("areaId", user.getAreaId());

		if (null != user.getLevel())
			ops.set("level", user.getLevel());
		if (null != user.getVip())
			ops.set("vip", user.getVip());
		if (null != user.getActive()) {
			ops.set("active", user.getActive());
		}
		// if (null != user.getFriendsCount())
		// ops.set("friendsCount", user.getFriendsCount());
		// if (null != user.getFansCount())
		// ops.set("fansCount", user.getFansCount());
		// if (null != user.getAttCount())
		// ops.set("attCount", user.getAttCount());

		// ops.set("createTime", null);
		ops.set("modifyTime", DateUtil.currentTimeSeconds());

		if (!StringUtil.isNullOrEmpty(user.getIdcard()))
			ops.set("idcard", user.getIdcard());
		if (!StringUtil.isNullOrEmpty(user.getIdcardUrl()))
			ops.set("idcardUrl", user.getIdcardUrl());

		if (null != user.getIsAuth())
			ops.set("isAuth", user.getIsAuth());
		if (null != user.getStatus())
			ops.set("status", user.getStatus());

		return dsForRW.findAndModify(q, ops);
	}

	@Override
	public void updatePassword(String telephone, String password) {
		Query<User> q = dsForRW.createQuery(User.class).field("telephone").equal(telephone);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("password", password);
		dsForRW.findAndModify(q, ops);
	}

	@Override
	public void updatePassowrd(int userId, String password) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("password", password);
		dsForRW.findAndModify(q, ops);
	}

	@Override
	public User getByPhone(String phone) {
		Query<User> query = dsForRW.createQuery(User.class);
		query.field("telephone").equal(phone);
		User user = query.get();
		return user;
	}

	@Override
	public User getByLs(String lsId) {
		Query<User> query = dsForRW.createQuery(User.class);
		query.field("lsId").equal(lsId);
		User user = query.get();
		return user;
	}

	@Override
	public void changePhone(int userId, String phone, String areaCode) {
		Query<User> q = dsForRW.createQuery(User.class).field("userId").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("areaCode", areaCode);
		ops.set("phone", phone);
		ops.set("userKey", DigestUtils.md5Hex(phone));
		ops.set("telephone", areaCode+phone);
		dsForRW.findAndModify(q, ops);
		
		// 更新redis
		User user = get(userId);
		KSessionUtil.saveUserByUserId(userId, user);
	}

	@Override
	public void rename(int userId, String lsId) {
		Query<User> q = dsForRW.createQuery(User.class).field("userId").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("lsId", lsId);
		ops.set("isRename", 1);
		dsForRW.findAndModify(q, ops);
		// 更新redis
		User user = get(userId);
		KSessionUtil.saveUserByUserId(userId, user);
	}

	public Map<String, Object> getAtWeb(int userId, String userKey) {
		Jedis resource = jedisPool.getResource();
		try {
			String atKey = KKeyConstant.atKey(userKey+"web");
			String accessToken = resource.get(atKey);
			if (StringUtil.isEmpty(accessToken)) {
				//如果为空则生成
				return saveATWeb(userId, userKey+"web");
			} else {
				//如果存在则返回
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("access_token", accessToken);
				return data;
			}
		} finally {
			jedisPool.returnResource(resource);
		}
	}

}
