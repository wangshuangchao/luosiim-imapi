package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import ch.qos.logback.classic.pattern.Util;
import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.constants.KKeyConstant;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.jedis.JedisCallback;
import cn.xyz.commons.support.jedis.JedisCallbackVoid;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.AESUtil;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.Correspondence;
import cn.xyz.commons.vo.CorrespondenceVo;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.example.UserExample;
import cn.xyz.mianshi.example.UserQueryExample;
import cn.xyz.mianshi.service.CompanyManager;
import cn.xyz.mianshi.service.FriendsManager;
import cn.xyz.mianshi.service.RoomManager;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.Course;
import cn.xyz.mianshi.vo.CourseMessage;
import cn.xyz.mianshi.vo.Emoji;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.InviteListVo;
import cn.xyz.mianshi.vo.LiveRoom;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.Report;
import cn.xyz.mianshi.vo.UPublicNum;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.User.UserSettings;
import cn.xyz.mianshi.vo.UserVo;
import cn.xyz.mianshi.vo.WxUser;
import cn.xyz.repository.UserRepository;
import cn.xyz.repository.mongo.PublicNumRepositoryImpl;
import cn.xyz.repository.mongo.UPublicNumRepositoryImpl;
import cn.xyz.repository.mongo.UserRepositoryImpl;
import cn.xyz.service.KSMSServiceImpl;
import cn.xyz.service.KXMPPServiceImpl;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Service(UserManagerImpl.BEAN_ID)
@Slf4j
public class UserManagerImpl extends MongoRepository<User, Integer> implements UserManager {
	public static final String BEAN_ID = "UserManagerImpl";

	@Autowired
	private CompanyManager companyManager;
	@Autowired
	private FriendsManager friendsManager;

	@Autowired
	private UserRepositoryImpl userRepository;

	@Autowired
	private UPublicNumRepositoryImpl uPublicNumRepository;

	@Autowired
	private PublicNumRepositoryImpl publicNumRepository;

	@Autowired
	private KSMSServiceImpl smsService;
	@Resource(name = RoomManager.BEAN_ID)
	private RoomManager roomManager;

	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRoom")
	protected Datastore dsForRoom;
	@Resource(name = "xmppConfig")
	private XMPPConfig xmppConfig;

	@Override
	public User createUser(String telephone, String password) {
		User user = new User();
		user.setUserId(createUserId());
		user.setUserKey(DigestUtils.md5Hex(telephone));
		user.setPassword(DigestUtils.md5Hex(password));
		user.setTelephone(telephone);

		userRepository.addUser(user);

		return user;
	}

	@Override
	public void createUser(User user) {
		userRepository.addUser(user);

	}

	@Override
	public User.UserSettings getSettings(int userId) {
		UserSettings settings = null;
		User user = null;
		user = getUser(userId);
		if (null == user)
			return null;
		settings = user.getSettings();
		return null != settings ? settings : new UserSettings();
	}

	@Override
	public User getUser(int userId) {
		// 先从 Redis 缓存中获取
		User user = KSessionUtil.getUserByUserId(userId);
		if (null == user) {
			user = userRepository.getUser(userId);
			if (null == user) {
				System.out.println("id为" + userId + "的用户不存在");
				return null;
			}

			KSessionUtil.saveUserByUserId(userId, user);
		}

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.xyz.mianshi.service.UserManager#getNickName(int)
	 */
	@Override
	public String getNickName(int userId) {
		User user = KSessionUtil.getUserByUserId(userId);
		if (user != null)
			return user.getNickname();
		List names = distinct("nickname", new BasicDBObject("_id", userId));
		if (null == names || 0 == names.size())
			return "";
		return names.get(0).toString();
	}

	public int getMsgNum(int userId) {
		List names = distinct("msgNum", new BasicDBObject("_id", userId));
		if (null == names || 0 == names.size()) {
			updateAttributeByIdAndKey(userId, "msgNum", 0);
			return 0;
		}

		return Integer.parseInt(names.get(0).toString());
	}

	public void changeMsgNum(int userId, int num) {
		UpdateOperations<User> operations = createUpdateOperations();

		operations.set("msgNum", num);
		updateAttributeByOps(userId, operations);
	}
	/*
	 * public String getUserName(int userId) { List distinct =
	 * userRepository.getCollection().distinct("nickname", new
	 * BasicDBObject("_id", userId)); if(null!=distinct&&0<distinct.size())
	 * return distinct.get(0).toString(); return null; }
	 */

	// 不经过Redis 直接从数据库获取数据
	public User getUserFromDB(int userId) {
		// 先从 Redis 缓存中获取
		User user = userRepository.getUser(userId);
		if (null == user) {
			System.out.println("id为" + userId + "的用户不存在");
			return null;
		} else
			KSessionUtil.saveUserByUserId(userId, user);

		return user;
	}

	@Override
	public User getUser(int userId, int toUserId) {
		User user = getUser(toUserId);

		Friends friends = friendsManager.getFriends(new Friends(userId, toUserId));
		user.setFriends(null == friends ? null : friends);

		// if (userId == toUserId) {
		// List<ResumeV2> resumeList = resumeManager.selectByUserId(toUserId);
		// user.setResumeList(resumeList);
		// }

		return user;
	}

	@Override
	public User getUser(String telephone) {
		// Integer userId=KSessionUtil.getUserIdByTelephone(telephone);

		return userRepository.getUser(telephone);
	}

	@Override
	public int getUserId(String accessToken) {
		return 0;
	}

	@Override
	public boolean isRegister(String telephone) {
		return 1 == userRepository.getCount(telephone);
	}

	@Override
	public User login(String telephone, String password) {
		String userKey = DigestUtils.md5Hex(telephone);

		User user = userRepository.getUserv1(userKey, null);

		if (null == user) {
			throw new ServiceException("帐号不存在");
		} else {
			String _md5 = DigestUtils.md5Hex(password);
			String _md5_md5 = DigestUtils.md5Hex(_md5);

			if (_md5.equals(user.getPassword()) || _md5_md5.equals(user.getPassword())) {
				return user;
			} else {
				throw new ServiceException("帐号或密码错误");
			}
		}
	}

	@Override
	public Map<String, Object> login(UserExample example) {
		User user = userRepository.getUser(example.getAreaCode(), example.getTelephone(), null);
		if (null == user) {
			throw new ServiceException(1040101, "帐号不存在, 请重新注册!");
		} else {

			String password = example.getPassword();
			/*
			 * String _md5 = DigestUtils.md5Hex(password); String _md5_md5 =
			 * DigestUtils.md5Hex(_md5);
			 */
			/*
			 * if (password.equals(user.getPassword()) ||
			 * _md5.equals(user.getPassword()) ||
			 * _md5_md5.equals(user.getPassword()))
			 */

			if (password.equals(user.getPassword())) {
				// 登录成功后维护当前用户的会话人数和会话状态
				if (null == user.getUserId()) {
					throw new RuntimeException("获取用户信息失败！");
				} else {
					companyManager.modifyEmployeesByuserId(user.getUserId());
				}
				return loginSuccess(user, example);
			}
			throw new ServiceException(1040102, "帐号密码错误");
		}
	}

	@Override
	public Map<String, Object> loginv1(UserExample example) {
		User user = userRepository.getUserv1(example.getTelephone(), null);
		if (null == user) {
			throw new ServiceException(1040101, "帐号不存在, 请重新注册!");
		} else if (!StringUtil.isEmpty(example.getRandcode())) {
			// 使用验证码登陆
			if (smsService.isAvailable(user.getTelephone(), example.getRandcode()))
				return loginSuccess(user, example);
			else
				throw new ServiceException(0, "验证码不正确");

		} else {
			String password = example.getPassword();
			/*
			 * String _md5 = DigestUtils.md5Hex(password); String _md5_md5 =
			 * DigestUtils.md5Hex(_md5);
			 */

			/*
			 * || _md5.equals(user.getPassword()) ||
			 * _md5_md5.equals(user.getPassword())
			 */

			if (password.equals(user.getPassword())) {
				return loginSuccess(user, example);
			}
			throw new ServiceException(1040102, "帐号密码错误");
		}
	}

	// 登陆成功方法
	public Map<String, Object> loginSuccess(User user, UserExample example) {
		// 获取上次登录日志
		User.LoginLog login = userRepository.getLogin(user.getUserId());
		// 保存登录日志
		userRepository.updateLogin(user.getUserId(), example);
		// f1981e4bd8a0d6d8462016d2fc6276b3
		Map<String, Object> data = userRepository.getAT(user.getUserId(), example.getTelephone());
		data.put("userId", user.getUserId());
		// data.put("password", user.getPassword());
		data.put("nickname", user.getNickname());
		data.put("portrait", user.getPortrait());
		// data.put("companyId", user.getCompanyId());
		data.put("offlineNoPushMsg", user.getOfflineNoPushMsg());
		data.put("login", login);
		data.put("isCS", user.getIsCS());

		if (StringUtil.isEmpty(login.getSerial())) {
			data.put("isupdate", 1);// 用户登陆不同设备，通知客户端更新用户
		} else if (!login.getSerial().equals(example.getSerial())) {
			data.put("isupdate", 1);
		} else {
			data.put("isupdate", 0);
		}

		Query<Friends> q = dsForRW.createQuery(Friends.class).field("userId").equal(user.getUserId());

		// 好友关系数量
		data.put("friendCount", q.countAll());
		// 保存登录日志

		/// 检查该用户 是否注册到 Tigase
		examineTigaseUser(user.getUserId(), user.getPassword(), example.getXmppVersion());
		return data;
	}

	public Map<String, Object> loginAuto(String access_token, int userId, String serial, String appId, double latitude,
			double longitude) {

		User user = getUser(userId);
		if (null == user)
			throw new ServiceException(1040101, "帐号不存在, 请重新注册!");

		User.LoginLog loginLog = userRepository.getLogin(userId);
		boolean exists = jedisTemplate.execute(new JedisCallback<Boolean>() {

			@Override
			public Boolean execute(Jedis jedis) {
				String atKey = KKeyConstant.userIdKey(access_token);
				return jedis.exists(atKey);
			}

		});
		// 1=没有设备号、2=设备号一致、3=设备号不一致
		int serialStatus = null == loginLog ? 1 : (serial.equals(loginLog.getSerial()) ? 2 : 3);
		// 1=令牌存在、0=令牌不存在
		int tokenExists = exists ? 1 : 0;

		try {

			Map<String, Object> result = Maps.newHashMap();
			result.put("serialStatus", serialStatus);
			result.put("tokenExists", tokenExists);
			// 如果令牌存在,则将用户密钥加密后返回
			if (1 == tokenExists) {
				// 获取用户密钥
				String key = jedisTemplate.execute(new JedisCallback<String>() {
					// 密钥key
					String secretKey = String.format(KSessionUtil.GET_SECRET_KEY, userId);

					@Override
					public String execute(Jedis jedis) {
						return jedis.get(secretKey);
					}
				});
				// 获取加密密钥的秘钥
				String k = Md5Util.md5Hex(access_token + AESUtil.key).substring(0, 16);
				// 对密钥加密
				String mdSecret = AESUtil.encrypt(key, k);
				// 存入result
				result.put("AESSecret", mdSecret);
			}
			result.put("userId", userId);
			result.put("nickname", user.getNickname());
			result.put("portrait", user.getPortrait());
			result.put("isCS", user.getIsCS());
			result.put("name", user.getName());
			result.put("login", loginLog);

			// 更新appId
			updateAttributeByIdAndKey(userId, "appId", appId);

			DBObject values = new BasicDBObject();
			values.put("loginLog.loginTime", DateUtil.currentTimeSeconds());

			values.put("loc.lng", longitude);
			values.put("loc.lat", latitude);

			DBObject q = new BasicDBObject("_id", userId);
			DBObject o = new BasicDBObject("$set", values);

			dsForRW.getCollection(User.class).update(q, o);

			return result;
		} catch (NullPointerException e) {
			throw new ServiceException("帐号不存在");
		} catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public void logout(String access_token, String areaCode, String userKey) {
		if (StringUtil.isEmpty(userKey)) {
			Query<User> q = dsForRW.createQuery(User.class).filter("areaCode", areaCode).field("userKey")
					.equal(userKey);
			UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
			ops.set("active", DateUtil.currentTimeSeconds());
			ops.set("loginLog.offlineTime", DateUtil.currentTimeSeconds());
			dsForRW.findAndModify(q, ops);
		}

		jedisTemplate.execute(new JedisCallbackVoid() {
			@Override
			public void execute(Jedis jedis) {
				// 清空缓存信息
				String atKey = KKeyConstant.atKey(userKey);
				String userIdKey = KKeyConstant.userIdKey(access_token);
				// 获取用户id,
				String userId = jedis.get(userIdKey);
				// 密钥key
				String secretKey = String.format(KSessionUtil.GET_SECRET_KEY, userId);
				// 清空缓存数据
				jedis.del(atKey, userIdKey, secretKey);
			}
		});
	}

	@Override
	public List<DBObject> query(UserQueryExample param) {
		return userRepository.queryUser(param);
	}

	@Override
	public Map<String, Object> register(UserExample example) {
		if (isRegister(example.getTelephone()))
			throw new ServiceException("手机号已被注册");

		// 生成userId
		Integer userId = createUserId();
		// 新增用户
		Map<String, Object> data = userRepository.addUser(userId, example);

		if (null != data) {
			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), example.getPassword(),
						example.getXmppVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return data;
		}
		throw new ServiceException("用户注册失败");
	}

	@Override
	public Map<String, Object> registerIMUser(UserExample example) {
		if (isRegister(example.getTelephone()))
			throw new ServiceException("手机号已被注册");

		// 生成userId
		Integer userId = createUserId();
		// 新增用户
		Map<String, Object> data = userRepository.addUser(userId, example);

		// 新增用户后默认新用户关注哦了公众号
		// 根据publicId为20000001的公众号
		PublicNum publicNum = publicNumRepository.getPublicNum(20000001);

		UPublicNum uPublicNum = new UPublicNum();
		uPublicNum.setIsAtt(1);
		uPublicNum.setPortraitUrl(publicNum.getPortraitUrl());
		uPublicNum.setPublicId(publicNum.getPublicId());
		uPublicNum.setPublicName(publicNum.getNickname());
		uPublicNum.setTime(DateUtil.currentTimeSeconds());
		uPublicNum.setUserId(userId);
		uPublicNumRepository.addUpublicNum(uPublicNum);

		if (null != data) {
			try {
				KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), example.getPassword(),
						example.getXmppVersion());
			} catch (Exception e) {
				e.printStackTrace();
			}
			//自动添加10000好友
//			try {
//				friendsManager.followUser(userId, 10000);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			// 调用组织架构功能示例方法
			companyManager.autoJoinCompany(userId);
			if (example.getUserType() != null) {
				if (example.getUserType() == 3) {
					roomManager.join(userId, new ObjectId("5a2606854adfdc0cd071485e"), 3);
				}
			}

			return data;
		}
		throw new ServiceException("用户注册失败");
	}

	@Override
	public void resetPassword(String telephone, String password) {
		userRepository.updatePassword(telephone, password);
		KXMPPServiceImpl.getInstance().updateToTig(getUser(telephone).getUserId(), password);
	}

	public void resetPassword(int userId, String newPassword) {
		userRepository.updatePassowrd(userId, newPassword);
		KXMPPServiceImpl.getInstance().updateToTig(userId, newPassword);
	}

	@Override
	public void updatePassword(int userId, String oldPassword, String newPassword) {
		User user = getUser(userId);
		/*
		 * String _md5 = DigestUtils.md5Hex(oldPassword); String _md5_md5 =
		 * DigestUtils.md5Hex(_md5);
		 */
		/*
		 * || _md5.equals(user.getPassword()) ||
		 * _md5_md5.equals(user.getPassword())
		 */
		if (oldPassword.equals(user.getPassword())) {
			userRepository.updatePassowrd(userId, newPassword);
			KXMPPServiceImpl.getInstance().updateToTig(userId, newPassword);
		} else
			throw new ServiceException("旧密码错误");
	}

	@Override
	public User updateSettings(int userId, User.UserSettings userSettings) {
		return userRepository.updateSettings(userId, userSettings);
	}

	@Override
	public User updateUser(int userId, UserExample param) {
		return userRepository.updateUser(userId, param);
	}

	public List<User> findUserList(int pageIndex, int pageSize, Integer notId) {
		Query<User> query = createQuery();
		List<Integer> ids = new ArrayList<Integer>() {
			{
				add(10000);
				add(10005);
				add(10006);
				add(notId);
			}
		};
		query.field("_id").notIn(ids);
		return query.order("-_id").offset(pageIndex * pageSize).limit(pageSize).asList();
	}

	@Override
	public List<DBObject> findUser(int pageIndex, int pageSize) {
		return userRepository.findUser(pageIndex, pageSize);
	}

	@Override
	public List<Integer> getAllUserId() {
		return dsForRW.getCollection((User.class)).distinct("_id");
	}

	@Override
	public void outtime(String access_token, int userId) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("active", DateUtil.currentTimeSeconds());
		ops.set("loginLog.offlineTime", DateUtil.currentTimeSeconds());
		dsForRW.findAndModify(q, ops);
	}

	@Override
	public void addUser(int userId, String password) {
		userRepository.addUser(userId, password);
	}

	/*
	 * @Override public User getfUser(int userId) { User user =
	 * userRepository.getUser(userId); if (null == user) return null; if (0 !=
	 * user.getCompanyId())
	 * user.setCompany(companyManager.get(user.getCompanyId())); return user; }
	 */

	// 用户充值 type 1 充值 2 消费
	public Double rechargeUserMoeny(Integer userId, Double money, int type) {
		try {
			Query<User> q = dsForRW.createQuery(User.class);
			q.field("_id").equal(userId);

			UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
			User user = getUser(userId);
			if (null == user)
				return 0.0;
			if (KConstants.MOENY_ADD == type) {
				ops.inc("balance", money);
				ops.inc("totalRecharge", money);
				user.setBalance(user.getBalance() + money);
			} else {
				ops.inc("balance", -money);
				ops.inc("totalConsume", money);
				user.setBalance(user.getBalance() - money);
			}
			dsForRW.update(q, ops);
			KSessionUtil.saveUserByUserId(userId, user);
			return q.get().getBalance();
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}

	// 用户充值 type 1 充值 2 消费
	public Double getUserMoeny(Integer userId) {
		try {
			Query<User> q = dsForRW.createQuery(User.class);
			q.field("_id").equal(userId);

			return q.get().getBalance();
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}

	public int getOnlinestateByUserId(Integer userId) {
		DBObject q = new BasicDBObject("_id", userId);
		List<Object> states = distinct("onlinestate", q);
		if (states != null && states.size() > 0)
			return (int) states.get(0);
		return 0;
	}

	public void examineTigaseUser(Integer userId, String password, int xmppVersion) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DBObject query = new BasicDBObject("user_id", userId + "@" + xmppConfig.getHost());
					BasicDBObject result = (BasicDBObject) dsForTigase.getDB().getCollection("tig_users")
							.findOne(query);
					if (null == result) {
						KXMPPServiceImpl.getInstance().registerByThread(String.valueOf(userId), password, xmppVersion);
					} else {
						if (null == result.get("xmppVersion") || xmppVersion != result.getInt("xmppVersion")) {
							BasicDBObject value = new BasicDBObject();
							value.put("$set", new BasicDBObject("xmppVersion", xmppVersion));
							dsForTigase.getDB().getCollection("tig_users").update(query, value);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();

	}

	public void report(Integer userId, Integer toUserId, String reason) {
		Report report = new Report();
		report.setUserId(userId);
		report.setToUserId(toUserId);
		report.setReason(reason);
		report.setTime(DateUtil.currentTimeSeconds());
		saveEntity(report);

	}

	// 获取用户Id
	public synchronized Integer createUserId() {
		DBCollection collection = dsForRW.getDB().getCollection("idx_user");
		if (null == collection)
			return createIdxUserCollection(collection, 0);
		DBObject obj = collection.findOne();
		if (null != obj) {
			Integer id = new Integer(obj.get("id").toString());
			id += 1;
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject(MongoOperator.INC, new BasicDBObject("id", 1)));
			return id;
		} else {
			return createIdxUserCollection(collection, 0);
		}

	}

	// 获取Call
	public synchronized Integer createCall() {
		DBCollection collection = dsForRW.getDB().getCollection("idx_user");
		if (null == collection) {
			return createIdxUserCollection(collection, 0);
		}
		DBObject obj = collection.findOne();
		if (null != obj) {
			if (obj.get("call") == null) {
				obj.put("call", 300000);
			}
			Integer call = new Integer(obj.get("call").toString());
			call += 1;
			if (call > 349999) {
				call = 300000;
			}
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject(MongoOperator.SET, new BasicDBObject("call", call)));
			return call;
		} else {
			return createIdxUserCollection(collection, 0);
		}
	}

	// 获取videoMeetingNo
	public synchronized Integer createvideoMeetingNo() {
		DBCollection collection = dsForRW.getDB().getCollection("idx_user");
		if (null == collection) {
			return createIdxUserCollection(collection, 0);
		}
		DBObject obj = collection.findOne();
		if (null != obj) {
			if (obj.get("videoMeetingNo") == null) {
				obj.put("videoMeetingNo", 350000);
			}
			Integer videoMeetingNo = new Integer(obj.get("videoMeetingNo").toString());
			videoMeetingNo += 1;
			if (videoMeetingNo > 399999) {
				videoMeetingNo = 350000;
			}
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject(MongoOperator.SET, new BasicDBObject("videoMeetingNo", videoMeetingNo)));
			return videoMeetingNo;
		} else {
			return createIdxUserCollection(collection, 0);
		}
	}

	private Integer createIdxUserCollection(DBCollection collection, long userId) {
		if (null == collection)
			collection = dsForRW.getDB().createCollection("idx_user", new BasicDBObject());
		BasicDBObject init = new BasicDBObject();
		Integer id = getMaxUserId();
		if (0 == id || id < 1000000)
			id = new Integer("10000001");
		id += 1;
		init.append("id", id);
		init.append("stub", "id");
		init.append("call", 300000);
		init.append("videoMeetingNo", 350000);
		collection.insert(init);
		return id;
	}

	private Integer getMaxUserId() {
		BasicDBObject projection = new BasicDBObject("_id", 1);
		DBObject dbobj = dsForRW.getDB().getCollection("user").findOne(null, projection, new BasicDBObject("_id", -1));
		if (null == dbobj)
			return 0;
		Integer id = new Integer(dbobj.get("_id").toString());
		return id;
	}

	public Integer getServiceNo(String areaCode) {
		DBCollection collection = getDatastore().getDB().getCollection("sysServiceNo");
		BasicDBObject obj = (BasicDBObject) collection.findOne(new BasicDBObject("areaCode", areaCode));
		if (null != obj)
			return obj.getInt("userId");
		return createServiceNo(areaCode);
	}

	// 获取系统最大客服号
	private Integer getMaxServiceNo() {
		DBCollection collection = getDatastore().getDB().getCollection("sysServiceNo");
		BasicDBObject obj = (BasicDBObject) collection.findOne(null, new BasicDBObject("userId", 1),
				new BasicDBObject("userId", -1));
		if (null != obj) {
			return obj.getInt("userId");
		} else {
			BasicDBObject query = new BasicDBObject("_id", new BasicDBObject(MongoOperator.LT, 1000200));
			query.append("_id", new BasicDBObject(MongoOperator.GT, 10200));
			BasicDBObject projection = new BasicDBObject("_id", 1);
			DBObject dbobj = dsForRW.getDB().getCollection("user").findOne(query, projection,
					new BasicDBObject("_id", -1));
			if (null == dbobj)
				return 100200;
			Integer id = new Integer(dbobj.get("_id").toString());
			return id;
		}
	}

	// 创建系统服务号
	private Integer createServiceNo(String areaCode) {
		DBCollection collection = getDatastore().getDB().getCollection("sysServiceNo");
		Integer userId = getMaxServiceNo() + 1;
		BasicDBObject value = new BasicDBObject("areaCode", areaCode);
		value.append("userId", userId);
		collection.save(value);
		addUser(userId, Md5Util.md5Hex(userId + ""));

		return userId;
	}

	// 消息免打扰
	@Override
	public User updataOfflineNoPushMsg(int userId, int OfflineNoPushMsg) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(userId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("OfflineNoPushMsg", OfflineNoPushMsg);

		return dsForRW.findAndModify(q, ops);
	}

	// 添加收藏
	@Override
	public Object addEmoji(int userId, String url, String roomJid, String msgId, int type) {
		/*
		 * Query<Emoji>
		 * query=dsForRW.createQuery(Emoji.class).filter("userId",userId);
		 * List<Emoji> list=query.asList(); for(int i=0;i<list.size();i++){
		 * if(list.get(i).getUrl()!=null){ if(list.get(i).getUrl()==url){ return
		 * null; } }else if(list.get(i).getMsgId()!=null){
		 * if(list.get(i).getMsgId()==msgId){ return null; } } }
		 */
		log.debug(String.format("addEmoji的传入参数 userId=%d url=%s rommJid=%s msgId=%s type=%d", userId, url, roomJid,
				msgId, type));
		Query<Emoji> query = null;
		if (!StringUtil.isEmpty(url)) {
			query = dsForRW.createQuery(Emoji.class).filter("userId", userId).filter("url", url);
		}
		if (!StringUtil.isEmpty(msgId)) {
			query = dsForRW.createQuery(Emoji.class).field("userId").equal(userId).field("msgId").equal(msgId);
		}
		if (query.get() == null) {
			Emoji emoji = new Emoji();
			emoji.setUserId(userId);
			emoji.setType(type);
			if (!StringUtil.isEmpty(url)) {
				emoji.setUrl(url);
			}
			if (!StringUtil.isEmpty(roomJid)) {
				emoji.setRoomJid(roomJid);
			}
			if (!StringUtil.isEmpty(msgId)) {
				emoji.setMsgId(msgId);
				DBCollection dbCollection = null;
				if (StringUtil.isEmpty(roomJid)) {
					dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
				} else {
					dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + roomJid);
				}
				BasicDBObject q = new BasicDBObject();
				q.put("messageId", msgId);
				Object data = dbCollection.findOne(q);
				if (data == null) {
					log.debug("没有获取到消息，msgId=" + msgId);
					return null;
				}
				emoji.setMsg(data.toString());
			}

			emoji.setCreateTime(DateUtil.currentTimeSeconds());
			Key<Emoji> save = dsForRW.save(emoji);
			if (null != save) {
				log.warn("------错误  日志--------保存收藏不成功" + save.getId());
			}
			return emoji;
		} else {
			Emoji emoji = query.get();
			log.debug("=========已存在=========" + emoji);
			return emoji;
		}

	}

	// 取消收藏
	@Override
	public void deleteEmoji(ObjectId emojiId) {
		Query<Emoji> query = dsForRW.createQuery(Emoji.class).field("_id").equal(emojiId);
		dsForRW.delete(query);
	}

	// 收藏列表
	@Override
	public List<Emoji> emojiList(int userId, int type, int pageSize, int pageIndex) {

		Query<Emoji> query = dsForRW.createQuery(Emoji.class).field("userId").equal(userId).filter("type",
				new BasicDBObject(MongoOperator.LT, 6));
		if (type != 0) {
			query.filter("type", type);
		}

		List<Emoji> emojiList = query.order("createTime").asList();
		return emojiList;
	}

	// 收藏表情列表
	@Override
	public List<Emoji> emojiList(int userId) {
		Query<Emoji> query = dsForRW.createQuery(Emoji.class).filter("userId", userId).filter("type", 6);
		List<Emoji> emojiList = query.order("createTime").asList();
		return emojiList;
	}

	// 添加课程
	@Override
	public void addMessageCourse(int userId, List<String> messageIds, long createTime, String courseName,
			String roomJid) {
		Course course = new Course();
		course.setUserId(userId);
		course.setMessageIds(messageIds);
		course.setCreateTime(createTime);
		course.setCourseName(courseName);
		course.setRoomJid(roomJid);

		saveEntity(course);
		/*
		 * Query<Course>
		 * query=dsForRW.createQuery(Course.class).filter("courseId",courseId);
		 * Course course=query.get();
		 */
		int num = 0;
		for (int i = 0; i < messageIds.size(); i++) {
			num++;
			DBCollection dbCollection = null;
			if (course.getRoomJid().equals("0")) {
				dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
			} else {
				dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + course.getRoomJid());
			}
			BasicDBObject q = new BasicDBObject();
			q.put("messageId", messageIds.get(i).toString());
			Object data = dbCollection.findOne(q);
			if (data == null) {
				continue;
			}
			CourseMessage courseMessage = new CourseMessage();
			courseMessage.setCourseId(course.getCourseId().toString());

			courseMessage.setMessage(data.toString());
			courseMessage.setUserId(course.getUserId());
			courseMessage.setCreateTime(num);
			saveEntity(courseMessage);
		}
	}

	// 获取课程列表
	@Override
	public List<Course> getCourseList(int userId) {
		Query<Course> query = dsForRW.createQuery(Course.class).filter("userId", userId);
		List<Course> list = query.order("createTime").asList();
		return list;
	}

	// 修改课程
	@Override
	public void updateCourse(Course course, String courseMessageId) {
		Query<Course> q = dsForRW.createQuery(Course.class).filter("courseId", course.getCourseId());
		UpdateOperations<Course> ops = dsForRW.createUpdateOperations(Course.class);
		if (null != course.getMessageIds()) {
			ops.set("messageIds", course.getMessageIds());
		}
		if (0 != course.getUpdateTime()) {
			ops.set("updateTime", course.getUpdateTime());
		}
		if (null != course.getCourseName()) {
			ops.set("courseName", course.getCourseName());
		}
		if (!StringUtil.isEmpty(courseMessageId)) {
			Query<CourseMessage> query = dsForRW.createQuery(CourseMessage.class).filter("_id",
					new ObjectId(courseMessageId));
			CourseMessage courseMessage = query.get();
			dsForRW.delete(courseMessage);
		}
		dsForRW.update(q, ops);

	}

	// 删除课程
	@Override
	public void deleteCourse(ObjectId courseId) {
		Query<Course> query = dsForRW.createQuery(Course.class).filter("courseId", courseId);
		Course course = query.get();
		Query<CourseMessage> q = dsForRW.createQuery(CourseMessage.class).filter("courseId", courseId);
		for (int i = 0; i < q.asList().size(); i++) {
			dsForRW.delete(q.asList().get(i));
		}
		dsForRW.delete(query);

	}

	// 获取详情
	@Override
	public List<CourseMessage> getCourse(String courseId) {
		List<CourseMessage> listMessage = new ArrayList<CourseMessage>();
		Query<CourseMessage> que = dsForRW.createQuery(CourseMessage.class).filter("courseId", courseId);
		listMessage = que.order("createTime").asList();
		return listMessage;
	}

	@Override
	public WxUser addwxUser(JSONObject jsonObject) {
		WxUser wxUser = new WxUser();
		Integer userId = createUserId();
		wxUser.setWxuserId(userId);
		wxUser.setOpenId(jsonObject.getString("openid"));
		wxUser.setNickname(jsonObject.getString("nickname"));
		wxUser.setImgurl(jsonObject.getString("headimgurl"));
		wxUser.setSex(jsonObject.getIntValue("sex"));
		wxUser.setCity(jsonObject.getString("city"));
		wxUser.setCountry(jsonObject.getString("country"));
		wxUser.setProvince(jsonObject.getString("province"));
		wxUser.setCreatetime(DateUtil.currentTimeSeconds());
		dsForRW.save(wxUser);

		try {
			KXMPPServiceImpl.getInstance().registerByThread(userId.toString(), jsonObject.getString("openid"), 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wxUser;
	}

	@Override
	public ResultInfo<InviteListVo> getUserInfo(Integer userId, List<Integer> idList) {
		ResultInfo<InviteListVo> result = new ResultInfo<>();
		InviteListVo inviteListVo = new InviteListVo();
		List<UserVo> list = new ArrayList<>();
		User user = getUser(userId);
		inviteListVo.setInviteId(userId);
		inviteListVo.setInviteName(user.getNickname());
		for (Integer integer : idList) {
			User addUser = getUser(integer);
			UserVo vo = new UserVo();
			vo.setNickName(addUser.getNickname());
			vo.setUserId(integer);
			list.add(vo);
		}
		inviteListVo.setUserVo(list);
		result.setCode("1000");
		result.setData(inviteListVo);
		result.setMsg("获取成功");
		return result;
	}

	@Override
	public ResultInfo<String> changePhone(Integer userId, String phone, String areaCode) {
		ResultInfo<String> result = new ResultInfo<>();
		User user = userRepository.getByPhone(areaCode + phone);
		if (null == user) {
			userRepository.changePhone(userId, phone, areaCode);
			result.setCode("1000");
			result.setData("更换成功");
			result.setMsg("更换成功");
			return result;
		} else {
			result.setCode("1001");
			result.setData("更换失败");
			result.setMsg("该号码已被注册");
			return result;
		}
	}

	@Override
	public ResultInfo<String> rename(Integer userId, String lsId) {
		ResultInfo<String> result = new ResultInfo<>();
		User user = userRepository.getByLs(lsId);
		if (null == user) {
			userRepository.rename(userId, lsId);
			result.setCode("1000");
			result.setData(lsId);
			result.setMsg("更换成功");
			return result;
		} else {
			result.setCode("1001");
			result.setData("更换失败");
			result.setMsg("该id已存在");
			return result;
		}
	}

	public void updateIsCS(Integer csUserId, Integer isCS) {
		Query<User> q = dsForRW.createQuery(User.class);
		q.field("userId").equal(csUserId);
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("isCS", isCS);
		this.update(q, ops);
		User user = userRepository.get(csUserId);
		// 更新redis
		KSessionUtil.saveUserByUserId(csUserId, user);
	}

	public Object loginWeb(UserExample example) {

		User user = userRepository.getUser(example.getAreaCode(), example.getTelephone(), null);
		if (null == user) {
			throw new ServiceException(1040101, "帐号不存在, 请重新注册!");
		} else {
			String password = example.getPassword();
			if (password.equals(user.getPassword())) {
				// 登录成功后维护当前用户的会话人数和会话状态
				if (null == user.getUserId()) {
					throw new RuntimeException("获取用户信息失败！");
				} else {
					companyManager.modifyEmployeesByuserId(user.getUserId());
				}
				return loginSuccessWeb(user, example);
			}
			throw new ServiceException(1040102, "帐号密码错误");
		}
	}

	private Object loginSuccessWeb(User user, UserExample example) {

		// 获取上次登录日志
		User.LoginLog login = userRepository.getLogin(user.getUserId());
		// 保存登录日志
		userRepository.updateLogin(user.getUserId(), example);
		// f1981e4bd8a0d6d8462016d2fc6276b3
		Map<String, Object> data = userRepository.getAtWeb(user.getUserId(), example.getTelephone());
		data.put("userId", user.getUserId());
		// data.put("password", user.getPassword());
		data.put("nickname", user.getNickname());
		// data.put("companyId", user.getCompanyId());
		data.put("offlineNoPushMsg", user.getOfflineNoPushMsg());
		data.put("login", login);
		data.put("isCS", user.getIsCS());

		if (StringUtil.isEmpty(login.getSerial())) {
			data.put("isupdate", 1);// 用户登陆不同设备，通知客户端更新用户
		} else if (!login.getSerial().equals(example.getSerial())) {
			data.put("isupdate", 1);
		} else {
			data.put("isupdate", 0);
		}

		Query<Friends> q = dsForRW.createQuery(Friends.class).field("userId").equal(user.getUserId());

		// 好友关系数量
		data.put("friendCount", q.countAll());
		// 保存登录日志

		/// 检查该用户 是否注册到 Tigase
		examineTigaseUser(user.getUserId(), user.getPassword(), example.getXmppVersion());
		return data;

	}

	public String getPhoneById(Integer userId) {
		Query<User> query = dsForRW.createQuery(User.class).filter("userId", userId);
		User user = query.get();
		return user.getPhone();
	}

	public Integer getIdByPhone(String phone) {
		Query<User> query = dsForRW.createQuery(User.class).filter("phone", phone);
		User user = query.get();
		return user.getUserId();
	}

	@Override
	public String getPortrait(int userId) {
		User user = KSessionUtil.getUserByUserId(userId);
		if (user != null)
			return user.getPortrait();
		List names = distinct("portrait", new BasicDBObject("_id", userId));
		if (null == names || 0 == names.size())
			return "";
		return names.get(0).toString();
	}

	public CorrespondenceVo getCorrespondence(Integer userId, List<String> list) {
		CorrespondenceVo result = new CorrespondenceVo();
		List<Correspondence> corres = new ArrayList<>();
		for (String string : list) {
			Correspondence c = new Correspondence();
			Query<User> q = dsForRW.createQuery(User.class);
			//根据phone或telephone来查询电话是否注册
			q.or(q.criteria("phone").equal(string), q.criteria("telephone").equal(string));
			User user = q.get();
			// 判断user是否为空
			if (null == user) {
				c.setIsRegister(0);
				c.setNum(string);
			} else {
				//如果用户存在,则返回用户相关内容
				c.setIsRegister(1);
				c.setNum(string);
				if (!StringUtil.isEmpty(user.getNickname())) {
					c.setNickname(user.getNickname());
				}else{
					c.setNickname("");
				}
				if (!StringUtil.isEmpty(user.getPortrait())) {
					c.setPortrait(user.getPortrait());
				}else{
					c.setPortrait("");
				}
				c.setLsId(user.getLsId());
				c.setPhone(user.getPhone());
				c.setTelephone(user.getTelephone());
				c.setUserId(user.getUserId());
			}
			corres.add(c);
		}
		result.setList(corres);
		return result;
	}

}
