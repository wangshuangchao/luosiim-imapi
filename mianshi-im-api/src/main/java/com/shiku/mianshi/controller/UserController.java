package com.shiku.mianshi.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.util.AliPayUtil;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;
import com.wxpay.utils.WXPayUtil;
import com.wxpay.utils.WxPayDto;
import com.wxpay.utils.http.HttpClientConnectionManager;

import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.PayConstants;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.CorrespondenceVo;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.commons.vo.PublicNumVo;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.commons.vo.UpublicDto;
import cn.xyz.mianshi.example.UserExample;
import cn.xyz.mianshi.example.UserQueryExample;
import cn.xyz.mianshi.service.PublicNumManager;
import cn.xyz.mianshi.service.impl.ConsumeRecordManagerImpl;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.ConsumeRecord;
import cn.xyz.mianshi.vo.Course;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.UserStatusCount;
import cn.xyz.mianshi.vo.WxUser;
import cn.xyz.service.KXMPPServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Administrator
 *
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {

	@Resource(name = "jedisTemplate")
	protected JedisTemplate jedisTemplate;
	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	private PublicNumManager publicManager;
	@Autowired
	ConsumeRecordManagerImpl consumeRecordManager;
	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;

	@RequestMapping(value = "/register")
	public JSONMessage register(@Valid UserExample example) {
		example.setPhone(example.getTelephone());
		example.setTelephone(example.getAreaCode() + example.getTelephone());
		Object data = userManager.registerIMUser(example);
		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/login")
	public JSONMessage login(@ModelAttribute UserExample example,@RequestParam(defaultValue = "0") int type) {
		//type=0为移动端登录(默认),type=1为web端登录
		if(0==type){
			//客户端登录
			Object data = userManager.login(example);
			return JSONMessage.success(null, data);
		}else{
			//web端登录,只返回token
			Object data = userManager.loginWeb(example);
			return JSONMessage.success(null, data);
		}
		
		// try {
//		Object data = userManager.login(example);
//		return JSONMessage.success(null, data);
		/*
		 * } catch (ServiceException e) { return JSONMessage. }
		 */

	}

	//已作废
	@RequestMapping(value = "/login/v1")
	public JSONMessage loginv1(@ModelAttribute UserExample example) {
		// example.setTelephone(example.getAreaCode()+example.getTelephone());
		Object data = userManager.login(example);
		return JSONMessage.success(null, data);
	}

	//用户离线,客户端调用重新登录
	@RequestMapping(value = "/login/auto")
	public JSONMessage loginAuto(@RequestParam String access_token, @RequestParam int userId,
			@RequestParam String serial, @RequestParam(defaultValue = "") String appId,
			@RequestParam(defaultValue = "0.0") double latitude, @RequestParam(defaultValue = "0.0") double longitude) {
		Object data = userManager.loginAuto(access_token, userId, serial, appId, longitude, latitude);
		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/logout")
	public JSONMessage logout(@RequestParam String access_token, @RequestParam(defaultValue = "86") String areaCode,
			String telephone) {
		//清除缓存中的内容
		String key1 = String.format("user:%s:channelId", ReqUtil.getUserId());
		String key2 = String.format("user:%s:deviceId", ReqUtil.getUserId());
		jedisTemplate.del(key1, key2);
		userManager.logout(access_token, areaCode, telephone);
		return JSONMessage.success(null);
	}

	@RequestMapping(value = "/outtime")
	public JSONMessage outtime(@RequestParam String access_token, @RequestParam int userId) {
		userManager.outtime(access_token, userId);
		return JSONMessage.success(null);
	}

	@RequestMapping("/update")
	public JSONMessage updateUser(@ModelAttribute UserExample param) {
		User data = userManager.updateUser(ReqUtil.getUserId(), param);
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/changeMsgNum")
	public JSONMessage changeMsgNum(@RequestParam int num) {
		userManager.changeMsgNum(ReqUtil.getUserId(), num);
		return JSONMessage.success(null);
	}

	// 设置消息免打扰
	@RequestMapping("/update/OfflineNoPushMsg")
	public JSONMessage updatemessagefree(@RequestParam int offlineNoPushMsg) {
		Query<User> q = dsForRW.createQuery(User.class).field("_id").equal(ReqUtil.getUserId());
		UpdateOperations<User> ops = dsForRW.createUpdateOperations(User.class);
		ops.set("offlineNoPushMsg", offlineNoPushMsg);
		User data = dsForRW.findAndModify(q, ops);
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/channelId/set")
	public JSONMessage setChannelId(@RequestParam String deviceId, String channelId) {
		if (StringUtil.isEmpty(channelId))
			return JSONMessage.success();
		String key1 = String.format("user:%s:channelId", ReqUtil.getUserId());
		String key2 = String.format("user:%s:deviceId", ReqUtil.getUserId());
		String key3 = String.format("channelId:%s", channelId);
		jedisTemplate.del(key1, key2, key3);
		jedisTemplate.set(key1, channelId);
		jedisTemplate.set(key2, deviceId);
		jedisTemplate.set(key3, ReqUtil.getUserId() + "");

		return JSONMessage.success();
	}

	//设置小米推送账号
	@RequestMapping("/xmpush/setRegId")
	public JSONMessage setRegId(@RequestParam(defaultValue = "") String deviceId, String regId) {
		if (StringUtil.isEmpty(regId))
			return JSONMessage.success();
		KSessionUtil.saveXMPushRegId(regId, ReqUtil.getUserId());
		// String key1 = String.format(KSessionUtil.GET_XMPUSH_KEY,
		// ReqUtil.getUserId());
		String key2 = String.format("user:%s:deviceId", ReqUtil.getUserId());
		// jedisTemplate.set(key1, regId);
		jedisTemplate.set(key2, deviceId);

		return JSONMessage.success();
	}

	@RequestMapping("/apns/setToken")
	public JSONMessage setToken(@RequestParam(defaultValue = "") String deviceId, String token) {
		if (StringUtil.isEmpty(token))
			return JSONMessage.failure("null Token");
		KSessionUtil.saveAPNSToken(token, ReqUtil.getUserId());
		String key2 = String.format("user:%s:deviceId", ReqUtil.getUserId());
		jedisTemplate.set(key2, deviceId);
		return JSONMessage.success();
	}

	@RequestMapping("/hwpush/setToken")
	public JSONMessage setHWToken(@RequestParam(defaultValue = "") String deviceId, String token) {
		if (StringUtil.isEmpty(token))
			return JSONMessage.failure("null Token");
		KSessionUtil.saveHWPushToken(token, ReqUtil.getUserId());
		String key2 = String.format("user:%s:deviceId", ReqUtil.getUserId());

		jedisTemplate.set(key2, deviceId);
		return JSONMessage.success();
	}

	@RequestMapping(value = "/get")
	public JSONMessage getUser(@RequestParam(defaultValue = "0") int userId) {
		int loginedUserId = ReqUtil.getUserId();
		int toUserId = 0 == userId ? loginedUserId : userId;
		User user = userManager.getUser(loginedUserId, toUserId);
		//如果不是用户 本身获取自己信息,则不返回密码
		if(user.getUserId()!=loginedUserId){
			user.setPassword("");
		}
		user.setOnlinestate(userManager.getOnlinestateByUserId(toUserId));
		KSessionUtil.saveUserByUserId(userId, user);
		return JSONMessage.success(null, user);
	}

	@RequestMapping(value = "/query")
	public JSONMessage queryUser(@ModelAttribute UserQueryExample param) {
		Object data = userManager.query(param);
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/password/reset")
	public JSONMessage resetPassword(@RequestParam(defaultValue = "86") String areaCode,
			@RequestParam(defaultValue = "") String telephone, @RequestParam(defaultValue = "") String randcode,
			@RequestParam(defaultValue = "") String newPassword) {
		JSONMessage jMessage;
		telephone = areaCode + telephone;
		if (StringUtil.isEmpty(telephone) || (StringUtil.isEmpty(randcode)) || StringUtil.isEmpty(newPassword)) {
			jMessage = KConstants.Result.ParamsAuthFail;
		} else {
			userManager.resetPassword(telephone, newPassword);
			Integer userId = ReqUtil.getUserId();
			KSessionUtil.deleteUserByUserId(userId);
			jMessage = JSONMessage.success(null);
		}

		return jMessage;
	}

	@RequestMapping("/password/update")
	public JSONMessage updatePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword) {
		JSONMessage jMessage;

		if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
			jMessage = KConstants.Result.ParamsAuthFail;
		} else {
			Integer userId = ReqUtil.getUserId();
			userManager.updatePassword(userId, oldPassword, newPassword);
			KSessionUtil.deleteUserByUserId(userId);
			jMessage = JSONMessage.success(null);
		}
		return jMessage;
	}

	@RequestMapping(value = "/settings")
	public JSONMessage getSettings(@RequestParam int userId) {
		Object data = userManager.getSettings(0 == userId ? ReqUtil.getUserId() : userId);
		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/settings/update")
	public JSONMessage updateSettings(@ModelAttribute User.UserSettings userSettings) {
		Integer userId = ReqUtil.getUserId();
		Object data = userManager.updateSettings(userId, userSettings);
		KSessionUtil.deleteUserByUserId(userId);
		return JSONMessage.success(null, data);
	}

	@RequestMapping(value = "/recharge/getSign")
	public JSONMessage getSign(@RequestParam int payType, @RequestParam String price) {
		Map<String, String> map = Maps.newLinkedHashMap();
		String orderInfo = "";
		if (0 < payType) {
			String orderNo = AliPayUtil.getOutTradeNo();
			ConsumeRecord entity = new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.MOENY_ADD);
			entity.setDesc("余额充值");
			entity.setStatus(KConstants.OrderStatus.CREATE);
			entity.setTradeNo(orderNo);
			entity.setPayType(payType);
			entity.setMoney(new Double(price));
			if (KConstants.PayType.ALIPAY == payType) {
				orderInfo = AliPayUtil.getOrderInfo("酷聊余额充值", "余额充值", price, orderNo);
				String sign = AliPayUtil.sign(orderInfo);
				consumeRecordManager.saveConsumeRecord(entity);
				map.put("sign", sign);
				map.put("orderInfo", orderInfo);
				System.out.println("orderInfo>>>>>" + orderInfo);
				// System.out.println("sign>>>>>"+sign);
				return JSONMessage.success(null, map);
			} else {
				WxPayDto tpWxPay = new WxPayDto();
				// tpWxPay.setOpenId(openId);
				tpWxPay.setBody("酷聊余额充值");
				tpWxPay.setOrderId(orderNo);
				tpWxPay.setSpbillCreateIp(PayConstants.WXSPBILL_CREATE_IP);
				tpWxPay.setTotalFee(price);
				consumeRecordManager.saveConsumeRecord(entity);
				Object data = WXPayUtil.getPackage(tpWxPay);
				return JSONMessage.success(null, data);
			}
		}
		return JSONMessage.failure("没有选择支付类型");
	}

	// @RequestMapping(value = "/useralladd")
	public JSONMessage useralladd() throws Exception {
		Cursor attach = dsForRW.getDB().getCollection("user").find();
		while (attach.hasNext()) {
			DBObject fileobj = attach.next();
			DBObject ref = new BasicDBObject();
			ref.put("user_id", fileobj.get("_id") + "@www.shiku.co");
			DBObject obj = dsForTigase.getDB().getCollection("tig_users").findOne(ref);
			if (null != obj) {
				System.out.println("123");
			} else {
				KXMPPServiceImpl.getInstance().register(fileobj.get("_id").toString(),
						fileobj.get("password").toString());
			}
		}
		return JSONMessage.success();
	}

	@RequestMapping(value = "/Recharge")
	public JSONMessage Recharge(Double money, int type) throws Exception {
		String tradeNo = AliPayUtil.getOutTradeNo();
		Integer userId = ReqUtil.getUserId();
		Map<String, Object> data = Maps.newHashMap();
		// 创建充值记录
		ConsumeRecord record = new ConsumeRecord();
		record.setUserId(userId);
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setStatus(KConstants.OrderStatus.END);
		record.setType(KConstants.MOENY_ADD);
		record.setPayType(type);
		record.setDesc("余额充值");
		record.setTime(DateUtil.currentTimeSeconds());
		consumeRecordManager.save(record);
		try {
			Double balance = userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
			data.put("balance", balance);
			return JSONMessage.success(null, data);
		} catch (Exception e) {
			return JSONMessage.error(e);
		}

	}

	@RequestMapping(value = "/getUserMoeny")
	public JSONMessage getUserMoeny() throws Exception {
		Integer userId = ReqUtil.getUserId();
		Map<String, Object> data = Maps.newHashMap();
		Double balance = userManager.getUserMoeny(userId);
		if (null == balance)
			balance = 0.0;
		data.put("balance", balance);
		return JSONMessage.success(null, data);

	}

	@RequestMapping(value = "/getUserStatusCount")
	public JSONMessage getUserStatusCount(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "100") int pageSize, String sign, String startDate, String endDate,
			@RequestParam(defaultValue = "0") int type) throws Exception {
		Map data = Maps.newHashMap();
		Map<String, Long> timeM = new HashMap<String, Long>();
		Query<UserStatusCount> query = dsForRW.createQuery(UserStatusCount.class);
		long currentTime = DateUtil.currentTimeSeconds();
		long startTime = 0;
		long endTime = 0;
		if (!StringUtils.isEmpty(sign)) {
			timeM = getTimes(new Integer(sign));
			startTime = timeM.get("startTime");
			endTime = timeM.get("endTime");

		} else {
			try {
				startTime = DateUtil.getDate(startDate, "yyyy-MM-dd").getTime() / 1000;
				endTime = DateUtil.getDate(endDate, "yyyy-MM-dd").getTime() / 1000;
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException("时间转换异常");
			}
		}

		if (0 < type)
			query.field("type").equal(type);
		if (0 != endTime) {
			query.field("time").greaterThanOrEq(startTime);
			query.field("time").lessThanOrEq(endTime);
		}
		query.order("time");

		List<UserStatusCount> list = query.asList();

		return JSONMessage.success(null, list);

	}

	@RequestMapping(value = "/getOnLine")
	public JSONMessage getOnlinestateByUserId(Integer userId) {
		userId = null != userId ? userId : ReqUtil.getUserId();
		Object data = userManager.getOnlinestateByUserId(userId);
		return JSONMessage.success(null, data);
	}

	@RequestMapping("/report")
	public JSONMessage report(@RequestParam Integer toUserId, @RequestParam String text) {
		userManager.report(ReqUtil.getUserId(), toUserId, text);
		return JSONMessage.success();
	}

	// 添加收藏
	@RequestMapping("/emoji/add")
	public JSONMessage addEmoji(@RequestParam(defaultValue = "") String url,
			@RequestParam(defaultValue = "") String roomJid, @RequestParam(defaultValue = "") String msgId,
			@RequestParam int type) {
		Object data = userManager.addEmoji(ReqUtil.getUserId(), url, roomJid, msgId, type);
		log.debug("------添加收藏------"+data);
		return JSONMessage.success(null, data);
	}

	// 收藏表情列表
	@RequestMapping("/emoji/list")
	public JSONMessage EmojiList(@RequestParam Integer userId, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		Object data = userManager.emojiList(userId);
		return JSONMessage.success(null, data);
	}

	// 收藏列表
	@RequestMapping("/collection/list")
	public JSONMessage collectionList(@RequestParam Integer userId, @RequestParam(defaultValue = "0") int type,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
		Object data = userManager.emojiList(userId, type, pageSize, pageIndex);
		return JSONMessage.success(null, data);
	}

	// 取消收藏
	@RequestMapping("/emoji/delete")
	public JSONMessage deleteEmoji(@RequestParam String emojiId) {
		userManager.deleteEmoji(new ObjectId(emojiId));
		return JSONMessage.success();
	}

	// 添加消息录制
	@RequestMapping("/course/add")
	public JSONMessage addMessagecourse(@RequestParam Integer userId, @RequestParam String messageIds,
			@RequestParam long createTime, @RequestParam String courseName,
			@RequestParam(defaultValue = "0") String roomJid) {

		List<String> list = Arrays.asList(messageIds.split(","));
		userManager.addMessageCourse(userId, list, createTime, courseName, roomJid);
		return JSONMessage.success();
	}

	// 查询课程
	@RequestMapping("/course/list")
	public JSONMessage getCourseList(@RequestParam Integer userId) {
		Object data = userManager.getCourseList(userId);
		return JSONMessage.success(null, data);
	}

	// 修改课程
	@RequestMapping("/course/update")
	public JSONMessage updateCourse(@ModelAttribute Course course,
			@RequestParam(defaultValue = "") String courseMessageId) {
		userManager.updateCourse(course, courseMessageId);
		return JSONMessage.success();
	}

	// 删除课程
	@RequestMapping("/course/delete")
	public JSONMessage deleteCourse(@RequestParam ObjectId courseId) {
		userManager.deleteCourse(courseId);
		return JSONMessage.success();
	}

	// 获取详情
	@RequestMapping("/course/get")
	public JSONMessage getCourse(@RequestParam String courseId) {
		Object data = userManager.getCourse(courseId);
		return JSONMessage.success(null, data);
	}

	// 获取微信用户的openid
	@RequestMapping("/wxUserOpenId")
	public void getOpenId(HttpServletResponse res, HttpServletRequest request, String code)
			throws ClientProtocolException, IOException, ServletException {
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=AppId&secret=AppSecret&code=CODE&grant_type=authorization_code";
		url = url.replace("AppId", "wxd3f39f42d3e92536").replace("AppSecret", "3f15b6b7b7f79e310eaa68893387c2a2")
				.replace("CODE", code);
		HttpGet get = HttpClientConnectionManager.getGetMethod(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpResponse response = httpclient.execute(get);
		String jsonStr = EntityUtils.toString(response.getEntity(), "utf-8");
		System.out.println("jsonStr:====>" + jsonStr);
		JSONObject jsonTexts = (JSONObject) JSON.parse(jsonStr);
		String openid = "";
		String token = "";
		if (jsonTexts.get("openid") != null) {
			openid = jsonTexts.getString("openid").toString();
		}

		String tokenurl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=APPID&secret=APPSECRET";
		tokenurl = tokenurl.replace("APPID", "wxd3f39f42d3e92536").replace("APPSECRET",
				"3f15b6b7b7f79e310eaa68893387c2a2");
		HttpGet httpget = HttpClientConnectionManager.getGetMethod(tokenurl);
		CloseableHttpClient httpclient1 = HttpClients.createDefault();
		HttpResponse response1 = httpclient1.execute(httpget);
		String jsonStr1 = EntityUtils.toString(response1.getEntity(), "utf-8");
		System.out.println("jsonStr1:====>" + jsonStr1);
		JSONObject jsonTexts1 = (JSONObject) JSON.parse(jsonStr1);
		if (jsonTexts1.get("access_token") != null) {
			token = jsonTexts1.getString("access_token").toString();
		}

		System.out.println("openId:======>" + openid);
		System.out.println("access_token" + token);
		request.getSession().setAttribute("openid", openid);
		request.getSession().setAttribute("token", token);
		request.getRequestDispatcher("/user/getUserInfo").forward(request, res);
	}

	@RequestMapping("/getWxUser")
	@ResponseBody
	public WxUser getWxUser(String openid) {
		WxUser wxUser = dsForRW.createQuery(WxUser.class).field("openId").equal(openid).get();
		return wxUser;
	}

	@RequestMapping("/getWxUserbyId")
	@ResponseBody
	public WxUser getWxUser(Integer userId) {
		WxUser wxUser = dsForRW.createQuery(WxUser.class).field("wxuserId").equal(userId).get();
		return wxUser;
	}

	// 获取微信用户的详细信息
	@RequestMapping("/getUserInfo")
	public void getUserInfo(HttpServletRequest request, HttpServletResponse response)
			throws ClientProtocolException, IOException, ServletException {
		String openid = request.getSession().getAttribute("openid").toString();
		String token = request.getSession().getAttribute("token").toString();
		WxUser wxUser = dsForRW.createQuery(WxUser.class).field("openId").equal(openid).get();

		if (wxUser != null) {
			response.sendRedirect("http://web.shiku.co/wxchat?openid=" + openid);
		} else {
			String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID";
			url = url.replace("ACCESS_TOKEN", token).replace("OPENID", openid);
			HttpGet get = HttpClientConnectionManager.getGetMethod(url);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpResponse response1 = httpclient.execute(get);
			String jsonStr = EntityUtils.toString(response1.getEntity(), "utf-8");
			System.out.println("response1" + response1);
			System.out.println("jsonStr" + jsonStr);
			JSONObject jsonObject = (JSONObject) JSON.parse(jsonStr);
			if (jsonObject != null) {
				WxUser wxUser1 = userManager.addwxUser(jsonObject);
				response.sendRedirect("http://web.shiku.co/wxchat?openid=" + openid);
			}
		}

	}

	/**
	 * 获取开始时间和结束时间
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Long> getTimes(Integer sign) {
		Long startTime = null;
		Long endTime = DateUtil.currentTimeSeconds();
		Map<String, Long> map = Maps.newLinkedHashMap();

		if (sign == -3) {// 最近一个月
			startTime = endTime - (KConstants.Expire.DAY1 * 30);
		} else if (sign == -2) {// 最近7天
			startTime = endTime - (KConstants.Expire.DAY1 * 7);
		} else if (sign == -1) {// 最近48小时
			startTime = endTime - (KConstants.Expire.DAY1 * 2);
		}
		// 表示今天
		else if (sign == 0) {
			startTime = DateUtil.getTodayMorning().getTime() / 1000;
		}

		else if (sign == 3) {
			startTime = DateUtil.strYYMMDDToDate("2000-01-01").getTime() / 1000;
		}

		map.put("startTime", startTime);
		map.put("endTime", endTime);
		return map;
	}

	/**
	 * 获取用户已经关注的公众号列表
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping("/getAttentionList")
	public ResultInfo<UpublicDto> getAttentionList(@RequestParam(defaultValue = "") Integer userId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<UpublicDto> result = publicManager.getAttentionList(userId);
		return result;
	}

	/**
	 * 获取用户已经绑定客服的公众号 此方法为申请了公众号的用户使用
	 * 
	 * @param csUserId
	 * @return
	 */
	@GetMapping("/getPublcNumListForCS")
	public ResultInfo<PublicNumVo> getPublcNumListForCS(@RequestParam(defaultValue = "") Integer csUserId) {
		ResultInfo<PublicNumVo> result = publicManager.getPublicNumListForCS(csUserId);
		return result;
	}

	@PostMapping("/rename")
	public ResultInfo<String> rename(Integer userId, String lsId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<String> result = userManager.rename(userId, lsId);
		return result;
	}

	@PostMapping("/changePhone")
	public ResultInfo<String> changePhone(Integer userId,String areaCode, String phone, String randcode) {
		ResultInfo<String> result = new ResultInfo<>();
		if (null == userId || StringUtil.isEmpty(phone) || StringUtil.isEmpty(randcode)|| StringUtil.isEmpty(areaCode)) {
			result.setCode("1001");
			result.setData("参数有误");
			result.setMsg("缺少参数");
			return result;
		}
		String key = String.format(KConstants.Key.RANDCODE, areaCode+phone);
		String code = jedisTemplate.get(key);
		if (!randcode.equals(code)) {
			result.setCode("1001");
			result.setData("验证码错误或已过期");
			result.setMsg("更改失败");
			return result;
		}
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		result = userManager.changePhone(userId, phone,areaCode);
		return result;
	}
	
	@GetMapping("/getCorrespondence")
	public ResultInfo<CorrespondenceVo> getCorrespondence(Integer userId,String numList){
		ResultInfo<CorrespondenceVo> result=new ResultInfo<>();
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		List<String> list = StringUtil.isEmpty(numList) ? null : JSON.parseArray(numList, String.class);
		CorrespondenceVo vo=userManager.getCorrespondence(userId,list);
		result.setCode("1000");
		result.setData(vo);
		result.setMsg("获取成功");
		return result;
	}

}
