package com.shiku.mianshi.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.autoconfigure.KApplicationProperties.XMPPConfig;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.Md5Util;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.utils.ZHUtils;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.example.UserExample;
import cn.xyz.mianshi.service.RoomManager;
import cn.xyz.mianshi.service.impl.ComplintManagerImpl;
import cn.xyz.mianshi.service.impl.FriendsManagerImpl;
import cn.xyz.mianshi.service.impl.LiveRoomManagerImpl;
import cn.xyz.mianshi.service.impl.PublicNumManagerImpl;
import cn.xyz.mianshi.service.impl.RedPacketManagerImpl;
import cn.xyz.mianshi.service.impl.RoomManagerImplForIM;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Complint;
import cn.xyz.mianshi.vo.Emoji;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Gift;
import cn.xyz.mianshi.vo.LiveRoom;
import cn.xyz.mianshi.vo.LiveRoom.LiveRoomMember;
import cn.xyz.mianshi.vo.PageVO;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

/**
 * 酷聊后台管理
 * 
 * @author luorc
 *
 */
@RestController
@RequestMapping("/console")
public class AdminController extends AbstractController {
	public static final String LOGIN_USER_KEY = "LOGIN_USER";

	
	@Resource(name = "adminMap")
	private Map<String, String> adminMap;
	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRoom")
	private Datastore dsForRoom;
	@Resource
	private XMPPConfig xmppConfig;
	@Resource(name = RoomManager.BEAN_ID)
	private RoomManagerImplForIM roomManager;
	@Autowired
	private LiveRoomManagerImpl liveRoomManager;

	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	private PublicNumManagerImpl pubNumManager;
	@Autowired
	private FriendsManagerImpl friendsManager;
	@Autowired
	private ComplintManagerImpl complintManager;
	@Autowired
	private RedPacketManagerImpl redPacketManager;
	@Autowired
	private KXMPPServiceImpl xmppService;

	@RequestMapping(value = "/chat_logs", method = { RequestMethod.GET })
	public ModelAndView chat_logs(@RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize, HttpServletRequest request) {
		// User user = getUser();

		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		BasicDBObject q = new BasicDBObject();
		// q.put("sender", user.getUserId());
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));

		long total = dbCollection.count(q);
		java.util.List<DBObject> pageData = Lists.newArrayList();

		DBCursor cursor = dbCollection.find(q).skip(pageIndex * pageSize).limit(pageSize);
		while (cursor.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) cursor.next();
			if (1 == dbObj.getInt("direction")) {
				int sender = dbObj.getInt("receiver");
				dbObj.put("receiver_nickname", userManager.getUser(sender).getNickname());
			}
			pageData.add(dbObj);
		}
		request.setAttribute("page", new PageVO(pageData, total, pageIndex, pageSize));
		return new ModelAndView("chat_logs");
	}

	@RequestMapping(value = "/chat_logs_all", method = { RequestMethod.GET })
	public ModelAndView chat_logs_all(@RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int sender,
			@RequestParam(defaultValue = "0") int receiver, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "25") int pageSize, HttpServletRequest request) {
		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		BasicDBObject q = new BasicDBObject();
		if (0 == receiver) {
			q.put("receiver", new BasicDBObject("$ne", 10005));
		} else {
			q.put("direction", 1);
			q.put("receiver", BasicDBObjectBuilder.start("$eq", receiver).add("$ne", 10005).get());
		}
		if (0 == sender) {
			q.put("sender", new BasicDBObject("$ne", 10005));
		} else {
			q.put("direction", 0);
			q.put("sender", BasicDBObjectBuilder.start("$eq", sender).add("$ne", 10005).get());
		}

		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));

		long total = dbCollection.count(q);
		java.util.List<DBObject> pageData = Lists.newArrayList();

		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("_id", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) cursor.next();
			try {
				dbObj.put("sender_nickname", userManager.getNickName(dbObj.getInt("sender")));
			} catch (Exception e) {
				dbObj.put("sender_nickname", "未知");
			}
			try {
				dbObj.put("receiver_nickname", userManager.getNickName(dbObj.getInt("receiver")));
			} catch (Exception e) {
				dbObj.put("receiver_nickname", "未知");
			}
			try {
				dbObj.put("content",
						JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class).get("content"));
			} catch (Exception e) {
				dbObj.put("content", "--");
			}
			pageData.add(dbObj);
		}
		request.setAttribute("page", new PageVO(pageData, total, pageIndex, pageSize));
		request.setAttribute("sender", 0 == sender ? "" : sender);
		request.setAttribute("receiver", 0 == receiver ? "" : receiver);
		return new ModelAndView("chat_logs_all");
	}

	@RequestMapping(value = "/chat_logs_all/del", method = { RequestMethod.GET })
	public void chat_logs_all_del(@RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int sender,
			@RequestParam(defaultValue = "0") int receiver, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "25") int pageSize, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		BasicDBObject q = new BasicDBObject();

		if (0 == sender) {
			q.put("sender", new BasicDBObject("$ne", 10005));
		} else {
			q.put("sender", BasicDBObjectBuilder.start("$eq", sender).add("$ne", 10005).get());
		}
		if (0 == receiver) {
			q.put("receiver", new BasicDBObject("$ne", 10005));
		} else {
			q.put("receiver", BasicDBObjectBuilder.start("$eq", receiver).add("$ne", 10005).get());
		}
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));
		dbCollection.remove(q);

		response.sendRedirect(String.format("/console/chat_logs_all?pageIndex=%s&pageSize=%s&sender=%s&receiver=%s",
				pageIndex, pageSize, sender, receiver));
	}

	@RequestMapping(value = "/deleteRoom", method = { RequestMethod.GET })
	public void deleteRoom(@RequestParam String roomId, @RequestParam int pageIndex, HttpServletResponse response) {
		try {
			DBCollection dbCollection = dsForTigase.getCollection(Room.class);
			roomManager.delete(new ObjectId(roomId));
			dbCollection.remove(new BasicDBObject("_id", new ObjectId(roomId)));
			response.sendRedirect("/console/roomList?pageIndex=" + pageIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 删除房间成员
	@RequestMapping(value = "/deleteMember", method = { RequestMethod.GET })
	public void deleteMember(@RequestParam String roomId, @RequestParam int userId, @RequestParam int pageIndex,
			HttpServletResponse response) {
		try {
			User user = userManager.getUser(userId);
			roomManager.deleteMember(user, new ObjectId(roomId), userId);
			response.sendRedirect("/console/roomUserManager?pageIndex=" + pageIndex + "&id=" + roomId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/deleteUser", method = { RequestMethod.GET })
	public void deleteUser(@RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "") String nickname, @RequestParam int pageIndex,
			HttpServletResponse response) {
		try {
			if (0 != userId) {
				DBCollection dbCollection = dsForRW.getCollection(User.class);
				dbCollection.remove(new BasicDBObject("_id", userId));

				DBCollection tdbCollection = dsForTigase.getDB().getCollection("tig_users");
				String xmpphost = xmppConfig.getHost();
				tdbCollection.remove(new BasicDBObject("user_id", userId + "@" + xmpphost));
				// 删除用户关系
				friendsManager.deleteFansAndFriends(userId);
			}
			/*
			 * if (!StringUtil.isEmpty(nickname)) { DBCollection dbCollection =
			 * dsForRW.getCollection(User.class); dbCollection.remove(new
			 * BasicDBObject("nickname", Pattern.compile(nickname))); }
			 */

			response.sendRedirect(String.format("/console/userList?pageIndex=%s&nickname=%s", pageIndex, nickname));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public User getUser() {
		Object obj = RequestContextHolder.getRequestAttributes().getAttribute(LOGIN_USER_KEY,
				RequestAttributes.SCOPE_SESSION);
		return null == obj ? null : (User) obj;
	}

	@RequestMapping(value = "/groupchat_logs", method = { RequestMethod.GET })
	public ModelAndView groupchat_logs(@RequestParam(defaultValue = "") String room_jid_id,
			@RequestParam(defaultValue = "0") long startTime, @RequestParam(defaultValue = "0") long endTime,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize,
			HttpServletRequest request) {
		ModelAndView mav = new ModelAndView("groupchat_logs");
		Object historyList = roomManager.selectHistoryList(getUser().getUserId(), 0, pageIndex, pageSize);
		if (!StringUtil.isEmpty(room_jid_id)) {
			DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_muc_msgs");

			BasicDBObject q = new BasicDBObject();
			// q.put("room_jid_id", room_jid_id);
			if (0 != startTime)
				q.put("ts", new BasicDBObject("$gte", startTime));
			if (0 != endTime)
				q.put("ts", new BasicDBObject("$lte", endTime));
			long total = dbCollection.count(q);
			java.util.List<DBObject> pageData = Lists.newArrayList();

			DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
					.limit(pageSize);
			while (cursor.hasNext()) {
				pageData.add(cursor.next());
			}
			mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		}
		mav.addObject("historyList", historyList);
		return mav;
	}

	@RequestMapping(value = "/groupchat_logs_all", method = { RequestMethod.GET })
	public ModelAndView groupchat_logs_all(@RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "") String room_jid_id,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize,
			HttpServletRequest request) {
		DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);

		BasicDBObject q = new BasicDBObject();
		if (!StringUtil.isEmpty(room_jid_id))
			q.put("room_jid_id", room_jid_id);
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));
		long total = dbCollection.count(q);
		java.util.List<DBObject> pageData = Lists.newArrayList();
		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) cursor.next();
			try {
				Map<?, ?> params = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
				dbObj.put("content", params.get("content"));
				dbObj.put("fromUserName", params.get("fromUserName"));
			} catch (Exception e) {
				dbObj.put("content", "--");
			}
			pageData.add(dbObj);
		}

		ModelAndView mav = new ModelAndView("groupchat_logs_all");
		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		mav.addObject("room_jid_id", room_jid_id);
		return mav;
	}

	@RequestMapping(value = "/groupchat_logs_all/del", method = { RequestMethod.GET })
	public void groupchat_logs_all_del(@RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "") String room_jid_id,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_muc_msgs");

		BasicDBObject q = new BasicDBObject();
		if (!StringUtil.isEmpty(room_jid_id))
			q.put("room_jid_id", room_jid_id);
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));

		dbCollection.remove(q);

		response.sendRedirect(String.format("/console/groupchat_logs_all?pageIndex=%s&pageSize=%s&room_jid_id=%s",
				pageIndex, pageSize, room_jid_id));
	}

	@RequestMapping(value = { "", "/" })
	public ModelAndView index(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView("index");
	}

	private boolean isAdmin(String username) {
		return adminMap.containsKey(username);
	}

	@RequestMapping(value = "/login", method = { RequestMethod.GET })
	public ModelAndView login() {
		return new ModelAndView("login");
	}

	@RequestMapping(value = "/login", method = { RequestMethod.POST })
	public ModelAndView login(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView view = new ModelAndView("login");
		String telephone = request.getParameter("telephone");
		String password = request.getParameter("password");

		if (isAdmin(telephone)) {
			if (login(telephone, password)) {
				request.getSession().setAttribute(LOGIN_USER_KEY, telephone);
				request.getSession().setAttribute("IS_ADMIN", 1);
				response.sendRedirect("/console/userStatus");
				return null;
			}
			request.setAttribute("tips", "帐号或密码错误！");
			return view;
		} else {
			try {
				User user = userManager.login(telephone, password);
				request.getSession().setAttribute(LOGIN_USER_KEY, user);
				request.getSession().setAttribute("IS_ADMIN", 0);
				response.sendRedirect("/console");
				return null;
			} catch (Exception e) {

			}
			request.setAttribute("tips", "帐号或密码错误！");
			return view;
		}
	}

	private boolean login(String username, String password) {
		return adminMap.get(username).equals(password);
	}

	@RequestMapping(value = "/logout", method = { RequestMethod.GET })
	public void logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.getSession().removeAttribute(LOGIN_USER_KEY);
		response.sendRedirect("/console/login");
	}

	@RequestMapping(value = "/pushToAll")
	public void pushToAll(HttpServletResponse response, @RequestParam int fromUserId, @RequestParam String body) {
		/*
		 * try { body = new String(body.getBytes("ISO-8859-1"), "utf-8"); }
		 * catch (UnsupportedEncodingException e) { e.printStackTrace(); }
		 */
		// MessageBean mb = new MessageBean();
		// mb.setContent(body);
		// mb.setFromUserId("10000");
		// mb.setFromUserName("系统消息");
		// mb.setType(1);
		// mb.setTimeSend(DateUtil.currentTimeSeconds());

		MessageBean mb = JSON.parseObject(body, MessageBean.class);
		mb.setFromUserId(fromUserId + "");
		mb.setTimeSend(DateUtil.currentTimeSeconds());

		ThreadUtil.executeInThread(new Callback() {

			@Override
			public void execute(Object obj) {
				DBCursor cursor = dsForRW.getDB().getCollection("user").find(null, new BasicDBObject("_id", 1))
						.sort(new BasicDBObject("_id", -1));
				while (cursor.hasNext()) {
					BasicDBObject dbObj = (BasicDBObject) cursor.next();
					int userId = dbObj.getInt("_id");
					try {
						if (10005 == fromUserId)
							KXMPPServiceImpl.getInstance().send(userId, JSON.toJSONString(mb));
						else if (10000 == fromUserId)
							KXMPPServiceImpl.getInstance().send("10000", DigestUtils.md5Hex("10000"), userId,
									JSON.toJSONString(mb));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		try {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter writer = response.getWriter();
			writer.write(
					"<script type='text/javascript'>alert('\u6279\u91CF\u53D1\u9001\u6D88\u606F\u5DF2\u5B8C\u6210\uFF01');window.location.href='/pages/qf.jsp';</script>");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/roomList", method = { RequestMethod.GET })
	public ModelAndView roomList(@RequestParam(defaultValue = "") String roomName,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "50") int pageSize) {
		ModelAndView mav = new ModelAndView("roomList");
		DBObject q = new BasicDBObject();
		if (!StringUtil.isEmpty(roomName))
			q.put("name", Pattern.compile(roomName));

		DBCollection dbCollection = dsForRoom.getCollection(Room.class);
		long total = dbCollection.count(q);
		List<DBObject> pageData = Lists.newArrayList();
		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("_id", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			pageData.add(obj);
		}
		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		mav.addObject("roomName", roomName);

		return mav;
	}

	@RequestMapping(value = "/roomMsgDetail", method = { RequestMethod.GET })
	public ModelAndView roomDetail(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "50") int pageSize, @RequestParam(defaultValue = "") String room_jid_id) {
		ModelAndView mav = new ModelAndView("roomDetail");

		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_muc_msgs");

		BasicDBObject q = new BasicDBObject();
		if (!StringUtil.isEmpty(room_jid_id))
			q.put("room_jid_id", room_jid_id);
		long total = dbCollection.count(q);
		java.util.List<DBObject> pageData = Lists.newArrayList();
		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("_id", 1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) cursor.next();
			try {
				Map<?, ?> params = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
				dbObj.put("content", params.get("content"));
				dbObj.put("fromUserName", params.get("fromUserName"));
			} catch (Exception e) {
				dbObj.put("content", "--");
			}
			pageData.add(dbObj);
		}

		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		mav.addObject("room_jid_id", room_jid_id);
		return mav;
	}

	@RequestMapping(value = "/userList", method = { RequestMethod.GET })
	public ModelAndView userList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "50") int pageSize, @RequestParam(defaultValue = "0") int onlinestate,
			@RequestParam(defaultValue = "") String nickname) {
		ModelAndView mav = new ModelAndView("userList");
		Query<User> query = userManager.createQuery();
		if (!StringUtil.isEmpty(nickname)) {
			query.or(query.criteria("nickname").contains(nickname), query.criteria("telephone").contains(nickname));
		}
		if (0 != onlinestate) {
			query.filter("onlinestate", onlinestate);
		}
		long total = query.countAll();
		List<User> pageData = query.order("-createTime").offset(pageIndex * pageSize).limit(pageSize).asList();
		PageVO page = new PageVO(pageData, total, pageIndex, pageSize);
		mav.addObject("page", page);
		mav.addObject("nickname", nickname);
		return mav;
	}

	@RequestMapping(value = "/updateUser", method = { RequestMethod.GET })
	public ModelAndView updateUser(@RequestParam(defaultValue = "0") Integer userId) {
		User user = null;
		if (0 == userId)
			user = new User();
		else
			user = userManager.getUser(userId);
		ModelAndView mav = new ModelAndView("updateUser");
		mav.addObject("u", user);
		return mav;
	}

	@RequestMapping(value = "/restPwd")
	public ModelAndView restPwd(@RequestParam(defaultValue = "0") Integer userId) {
		ModelAndView mav = new ModelAndView("userList");
		if (0 < userId)
			userManager.resetPassword(userId, Md5Util.md5Hex("123456"));
		return mav;
	}

	@RequestMapping(value = "/updateUser", method = { RequestMethod.POST })
	public void saveUserMsg(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") Integer userId, UserExample example) throws Exception {
		if (!StringUtil.isEmpty(example.getTelephone())) {
			example.setPhone(example.getTelephone());
			example.setTelephone(example.getAreaCode() + example.getTelephone());
		}
		// 保存到数据库
		if (0 == userId) {// 后台注册用户(后台注册传的密码没有加密，这里进行加密)
			example.setPassword(DigestUtils.md5Hex(example.getPassword()));
			userManager.registerIMUser(example);
		} else {
			userManager.updateUser(userId, example);
		}

		// 更新数据
		response.sendRedirect("/console/userList");
	}

	// 用户在线状态 走势图
	@RequestMapping(value = "/userStatus", method = { RequestMethod.GET })
	public ModelAndView userStatus(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelAndView mav = new ModelAndView("userStatus");

		return mav;
	}

	@RequestMapping("/redPacketList")
	public ModelAndView getRedPacketList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "25") int pageSize) {
		ModelAndView mav = new ModelAndView("/redPacketList");
		Object data = redPacketManager.getRedPacketList(ReqUtil.getUserId(), pageIndex, pageSize);
		mav.addObject("page", data);
		return mav;
	}

	@RequestMapping(value = "/addRoom", method = { RequestMethod.GET })
	public ModelAndView addRomm(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "") String id) throws Exception {
		ModelAndView mav = new ModelAndView("editRoom");
		if (StringUtil.isEmpty(id))
			mav.addObject("o", new Room());
		else
			mav.addObject("o", roomManager.get(new ObjectId(id)));
		return mav;
	}

	@RequestMapping(value = "/addRoom", method = { RequestMethod.POST })
	public void addRomm(HttpServletRequest request, HttpServletResponse response, Room room,
			@RequestParam(defaultValue = "") String ids) throws Exception {
		ModelAndView mav = new ModelAndView("editRoom");
		List<Integer> idList = StringUtil.isEmpty(ids) ? null : JSON.parseArray(ids, Integer.class);
		if (null == room.getId()) {
			User user = userManager.getUser(room.getUserId());
			// String jid=UUID.randomUUID().toString().replaceAll("-", "");
			String jid = xmppService.createMucRoom(user.getNickname(), room.getName(), room.getSubject(),
					room.getDesc());
			// String jid=xmppService.createChatRoom(user.getNickname(),
			// room.getName(), room.getSubject(), "20");
			if (null == jid)
				return;
			room.setJid(jid);
			roomManager.add(user, room, idList);
		}
		/*
		 * else{ roomManager.update(null, roomId, roomName, notice, desc); }
		 */
		// 更新数据
		response.sendRedirect("/console/roomList");
	}

	@RequestMapping(value = "/roomUserManager", method = { RequestMethod.GET })
	public ModelAndView roomUserManager(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "") String id)
			throws Exception {
		ModelAndView mav = new ModelAndView("roomUserManager");
		if (!StringUtil.isEmpty(id))
			mav.addObject("page", roomManager.getMemberListByPage(new ObjectId(id), pageIndex, pageSize));
		return mav;
	}

	@RequestMapping(value = "/sendMessage", method = { RequestMethod.POST })
	public ModelAndView sendMssage(@RequestParam String body, Integer from, Integer to, Integer count) {
		ModelAndView mav = new ModelAndView("qf");
		try {
			System.out.println("body=======>  " + body);
			// String msg = new String(body.getBytes("iso8859-1"),"utf-8");
			if (null == from) {
				List<Friends> uList = friendsManager.queryFriendsList(to, 0, 0, count);
				new Thread(new Runnable() {

					@Override
					public void run() {
						User user = null;
						MessageBean messageBean = null;
						;
						for (Friends friends : uList) {
							try {
								user = userManager.getUser(friends.getToUserId());
								messageBean = new MessageBean();
								messageBean.setType(1);
								messageBean.setContent(body);
								messageBean.setFromUserId(user.getUserId() + "");
								messageBean.setFromUserName(user.getNickname());

								KXMPPServiceImpl.getInstance().send(user.getUserId() + "", user.getPassword(), to,
										messageBean.toString());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							;
						}
					}
				}).start();
			} else {
				new Thread(new Runnable() {

					@Override
					public void run() {
						KXMPPServiceImpl.getInstance().send(userManager.get(from), to, body);
					}
				}).start();
			}
			return mav;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mav;
	}

	@RequestMapping(value = "/addAllUser")
	public JSONMessage updateTigaseDomain() throws Exception {
		Cursor attach = dsForRW.getDB().getCollection("user").find();
		String userId = "";
		String password = "";
		while (attach.hasNext()) {
			DBObject fileobj = attach.next();
			DBObject ref = new BasicDBObject();
			ref.put("user_id", fileobj.get("_id") + "@" + xmppConfig.getHost());
			DBObject obj = dsForTigase.getDB().getCollection("tig_users").findOne(ref);
			userId = fileobj.get("_id").toString().replace(".", "0");
			password = fileobj.get("password").toString();
			if (null != obj) {
				System.out.println(fileobj.get("_id").toString() + "  已注册");
			} else {
				String user_id = userId + "@" + xmppConfig.getHost();
				BasicDBObject jo = new BasicDBObject();
				jo.put("_id", generateId(user_id));
				jo.put("user_id", user_id);
				jo.put("domain", xmppConfig.getHost());
				jo.put("password", password);
				jo.put("type", "shiku");
				dsForTigase.getDB().getCollection("tig_users").save(jo);
				System.out.println(user_id + "  注册到Tigase：" + xmppConfig.getHost());
			}
		}
		return JSONMessage.success();
	}

	private byte[] generateId(String username) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		return md.digest(username.getBytes());
	}

	@RequestMapping(value = "/initSysUser")
	public JSONMessage initSysUser(@RequestParam(defaultValue = "0") int userId) throws Exception {
		if (0 < userId) {
			userManager.addUser(userId, String.valueOf(userId));
			KXMPPServiceImpl.getInstance().register(String.valueOf(userId), String.valueOf(userId));
		} else {
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
			KXMPPServiceImpl.getInstance().register("10000", Md5Util.md5Hex("10000"));
			KXMPPServiceImpl.getInstance().register("10005", Md5Util.md5Hex("10005"));
			KXMPPServiceImpl.getInstance().register("10006", Md5Util.md5Hex("10006"));
			KXMPPServiceImpl.getInstance().register("10007", Md5Util.md5Hex("10007"));
			KXMPPServiceImpl.getInstance().register("10008", Md5Util.md5Hex("10008"));
			KXMPPServiceImpl.getInstance().register("10009", Md5Util.md5Hex("10009"));
			KXMPPServiceImpl.getInstance().register("10010", Md5Util.md5Hex("10010"));
			KXMPPServiceImpl.getInstance().register("10011", Md5Util.md5Hex("10011"));
			KXMPPServiceImpl.getInstance().register("10012", Md5Util.md5Hex("10012"));
			KXMPPServiceImpl.getInstance().register("10013", Md5Util.md5Hex("10013"));
			KXMPPServiceImpl.getInstance().register("10014", Md5Util.md5Hex("10014"));
			KXMPPServiceImpl.getInstance().register("10015", Md5Util.md5Hex("10015"));
		}
		return JSONMessage.success();
	}

	// 直播间列表
	@RequestMapping(value = "/liveRoomList", method = { RequestMethod.GET })
	public ModelAndView liveRoomList(@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") String nickName, @RequestParam(defaultValue = "0") Integer userId,
			@RequestParam(defaultValue = "0") Integer pageIndex, @RequestParam(defaultValue = "10") Integer pageSize,
			@RequestParam(defaultValue = "-1") Integer status) throws Exception {
		ModelAndView mav = new ModelAndView("liveRoomList");
		DBObject q = new BasicDBObject();
		List<LiveRoom> pageData = Lists.newArrayList();
		if (!StringUtil.isEmpty(name)) {
			q.put("nickName", Pattern.compile(name));
		}
		DBCollection dbCollection = dsForTigase.getCollection(LiveRoom.class);
		long total = dbCollection.count(q);
		try {

			pageData = liveRoomManager.findLiveRoomList(name, nickName, userId, pageIndex, pageSize, status);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		mav.addObject("name", name);
		return mav;
	}

	@RequestMapping(value = "/addLiveRoom", method = { RequestMethod.GET })
	public ModelAndView addLiveRoom() {
		ModelAndView mav = new ModelAndView("addliveRoom");
		mav.addObject("o", new LiveRoom());
		return mav;
	}

	// 保存新增直播间
	@RequestMapping(value = "/saveNewLiveRoom", method = { RequestMethod.POST })
	public void saveNewLiveRoom(HttpServletRequest request, HttpServletResponse response, LiveRoom liveRoom)
			throws IOException {

		liveRoomManager.createLiveRoom(liveRoom);
		response.sendRedirect("/console/liveRoomList");
	}

	// 删除直播间
	@RequestMapping(value = "/deleteLiveRoom", method = { RequestMethod.GET })
	public void deleteLiveRoom(@RequestParam String liveRoomId, @RequestParam int pageIndex,
			HttpServletResponse response) {
		try {
			DBCollection dbCollection = dsForTigase.getCollection(LiveRoom.class);
			liveRoomManager.deleteLiveRoom(new ObjectId(liveRoomId));
			/* liveRoomManager.deleteLiveRoom(new ObjectId(liveRoomId)); */
			dbCollection.remove(new BasicDBObject("_id", new ObjectId(liveRoomId)));
			response.sendRedirect("/console/liveRoomList?pageIndex=" + pageIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 查询直播间人员
	@RequestMapping(value = "/liveRoomUserManager", method = { RequestMethod.GET })
	public ModelAndView liveRoomManager(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "") String name, @RequestParam(defaultValue = "") String nickName,
			@RequestParam(defaultValue = "0") Integer userId, @RequestParam(defaultValue = "10") int pageSize,
			@RequestParam(defaultValue = "") String roomId) throws Exception {
		ModelAndView mav = new ModelAndView("liveRoomUserManager");
		List<LiveRoomMember> pageData = Lists.newArrayList();
		DBObject q = new BasicDBObject();
		ObjectId id = null;
		DBCollection dbCollection = dsForTigase.getCollection(LiveRoom.class);
		long total = dbCollection.count(q);
		/* if(StringUtil.isEmpty(roomId)){ */
		id = new ObjectId(roomId);
		pageData = liveRoomManager.findLiveRoomMemberList(id);
		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));

		return mav;

	}

	// 删除直播间成员
	@RequestMapping(value = "/deleteRoomUser")
	public void deleteliveRoomUserManager(@RequestParam Integer userId,
			@RequestParam(defaultValue = "") String liveRoomId, HttpServletResponse response,
			@RequestParam(defaultValue = "0") int pageIndex) {
		try {
			liveRoomManager.exitLiveRoom(userId, new ObjectId(liveRoomId));
			response.sendRedirect("/console/liveRoomUserManager?pageIndex=" + pageIndex + "&roomId=" + liveRoomId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 禁言
	@RequestMapping(value = "/shutup")
	public void shutup(@RequestParam Integer userId, @RequestParam int type, @RequestParam ObjectId roomId) {
		try {
			liveRoomManager.shutup(type, userId, roomId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 禁播
	@RequestMapping(value = "/banplay")
	public void ban() {
		try {

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 礼物列表
	@RequestMapping(value = "/giftList", method = { RequestMethod.GET })
	public ModelAndView giftList(@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
		ModelAndView mav = new ModelAndView("giftList");
		List<Gift> pageData = Lists.newArrayList();
		DBObject q = new BasicDBObject();
		DBCollection dbCollection = dsForRW.getCollection(Gift.class);
		long total = dbCollection.count(q);
		try {
			pageData = liveRoomManager.findAllgift(name, pageIndex, pageSize);
		} catch (Exception e) {
			// TODO: handle exception
		}
		mav.addObject("page", new PageVO(pageData, total, pageIndex, pageSize));
		return mav;
	}

	// 添加礼物
	@RequestMapping(value = "/add/gift", method = { RequestMethod.GET })
	public void addGift(HttpServletRequest request, HttpServletResponse response, @RequestParam String name,
			@RequestParam String photo, @RequestParam double price, @RequestParam int type) throws IOException {
		ModelAndView mav = new ModelAndView("editRoom");
		try {
			liveRoomManager.addGift(name, photo, price, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.sendRedirect("/console/giftList");
	}

	// 删除礼物
	@RequestMapping(value = "/delete/gift")
	public void deleteGift(@RequestParam String giftId) {
		try {
			liveRoomManager.deleteGift(new ObjectId(giftId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 禁止送礼
	@RequestMapping(value = "/banGift")
	public void banGift() {
		try {

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@RequestMapping(value = "/messageList")
	public ModelAndView messageList(String keyword, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		ModelAndView mav = new ModelAndView("messageList");
		DBCollection db = dsForRW.getDB().getCollection("message");
		BasicDBObject query = null;

		if (!StringUtil.isEmpty(keyword)) {
			query = new BasicDBObject();
			BasicDBList values = new BasicDBList();
			values.add(new BasicDBObject("type", new BasicDBObject(MongoOperator.REGEX, keyword)));
			values.add(new BasicDBObject("code", new BasicDBObject(MongoOperator.REGEX, keyword)));
			query.put("$or", values);
		}
		List<DBObject> list = null;
		long total = 0;
		if (null == query) {
			list = db.find().skip(pageIndex * pageSize).limit(pageSize).toArray();
			total = db.count();
		} else {
			list = db.find(query).skip(pageIndex * pageSize).limit(pageSize).toArray();
			total = db.count(query);
		}

		mav.addObject("page", new PageVO(list, total, pageIndex, pageSize));
		mav.addObject("languages", appConfig.getLanguages());
		return mav;
	}

	@RequestMapping(value = "/messageEdit", method = { RequestMethod.GET })
	public ModelAndView messageEdit(@RequestParam(defaultValue = "") String id) {
		ModelAndView mav = new ModelAndView("messageEdit");
		BasicDBObject query = new BasicDBObject();
		DBObject dbObj = null;
		if (!StringUtil.isEmpty(id)) {
			query.append("_id", parse(id));
			dbObj = userManager.getDatastore().getDB().getCollection("message").findOne(query);
			mav.addObject("action", "update");
		} else
			dbObj = new BasicDBObject();

		mav.addObject("o", dbObj);
		mav.addObject("url", "messageEdit");
		mav.addObject("languages", appConfig.getLanguages());
		addReferer();
		return mav;
	}

	@RequestMapping(value = "/messageUpdate", method = { RequestMethod.POST })
	public void messageUpdate(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String[]> parameterMap = request.getParameterMap();
		BasicDBObject dbObj = new BasicDBObject();
		for (Entry<String, String[]> entry : parameterMap.entrySet()) {
			if ("code".equals(entry.getKey()) && StringUtil.isEmpty(entry.getValue()[0]))
				referer(response, "/console/messageList", 1);
			else if ("type".equals(entry.getKey()) && StringUtil.isEmpty(entry.getValue()[0]))
				referer(response, "/console/messageList", 1);
			else if ("zh".equals(entry.getKey()) && StringUtil.isEmpty(entry.getValue()[0]))
				referer(response, "/console/messageList", 1);
			dbObj.put(entry.getKey(), entry.getValue()[0]);
		}
		if (StringUtil.isEmpty(dbObj.getString("big5")))
			dbObj.put("big5", ZHUtils.convert(dbObj.getString("zh"), ZHUtils.BIG5));
		BasicDBObject query = new BasicDBObject("_id", parse(dbObj.getString("_id")));
		dbObj.remove("_id");
		userManager.getDatastore().getDB().getCollection("message").update(query,
				new BasicDBObject(MongoOperator.SET, dbObj), true, false);

		referer(response, "/messageList", 1);
	}

	@RequestMapping(value = "/messageDelete")
	public void messageDelete(HttpServletResponse response, @RequestParam(defaultValue = "") String id) {
		if (!StringUtil.isEmpty(id)) {
			BasicDBObject query = new BasicDBObject("_id", parse(id));
			userManager.getDatastore().getDB().getCollection("message").remove(query);
		}
		referer(response, "/console/messageList", 0);
	}

	// 关键词列表
	@RequestMapping(value = "/keywordfilter")
	public ModelAndView keywordfilter(@RequestParam(defaultValue = "") String word,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "10") int pageSize) {
		ModelAndView mav = new ModelAndView("keyword");
		DBCollection db = dsForRW.getDB().getCollection("notKeyword");
		BasicDBObject query = null;
		if (!StringUtil.isEmpty(word)) {
			query = new BasicDBObject();
			BasicDBList values = new BasicDBList();
			values.add(new BasicDBObject("word", new BasicDBObject(MongoOperator.REGEX, word)));
			query.put("$or", values);
		}
		List<DBObject> list = null;
		long total = 0;
		if (null == query) {
			list = db.find().skip(pageIndex * pageSize).limit(pageSize).toArray();
			total = db.count();
		} else {
			list = db.find(query).skip(pageIndex * pageSize).limit(pageSize).toArray();
			total = db.count(query);
		}
		mav.addObject("page", new PageVO(list, total, pageIndex, pageSize));
		return mav;
	}

	@RequestMapping(value = "/keywordEdit")
	public ModelAndView keywordEdit() {
		ModelAndView mav = new ModelAndView("addkeyword");
		return mav;
	}

	// 添加敏感词
	@RequestMapping(value = "/addkeyword", method = { RequestMethod.POST })
	public void addkeyword(HttpServletResponse response, @RequestParam(defaultValue = "") String id,
			@RequestParam String word) throws IOException {

		BasicDBObject dbObj = new BasicDBObject();
		dbObj.put("word", word);
		if (StringUtil.isEmpty(id))
			userManager.getDatastore().getDB().getCollection("notKeyword").insert(dbObj);
		else {
			BasicDBObject query = new BasicDBObject("_id", parse(id));
			userManager.getDatastore().getDB().getCollection("notKeyword").update(query,
					new BasicDBObject(MongoOperator.SET, dbObj), true, false);
		}
		response.sendRedirect("/console/keywordfilter");
	}

	// 删除敏感词
	@RequestMapping(value = "/deletekeyword", method = { RequestMethod.GET })
	public void deletekeyword(HttpServletResponse response, @RequestParam String id) throws IOException {

		BasicDBObject query = new BasicDBObject("_id", parse(id));
		userManager.getDatastore().getDB().getCollection("notKeyword").remove(query);
		response.sendRedirect("/console/keywordfilter");
	}

	// 删除聊天记录
	@RequestMapping(value = "/deleteMsgGroup", method = { RequestMethod.POST })
	public void deleteMsgGroup(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") long startTime, @RequestParam(defaultValue = "0") long endTime,
			@RequestParam(defaultValue = "") String room_jid_id) throws Exception {
		if (room_jid_id != null) {
			DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + room_jid_id);
			BasicDBObject query = new BasicDBObject();
			if (0 != startTime) {
				query.put("ts", new BasicDBObject("$gte", startTime));
			}
			if (0 != endTime) {
				query.put("ts", new BasicDBObject("$gte", endTime));
			}
			DBCursor cursor = dbCollection.find(query);
			if (cursor.size() > 0) {

				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				// 解析消息体

				Map<String, Object> body = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
				int contentType = (int) body.get("type");

				if (contentType == 2 || contentType == 3 || contentType == 5 || contentType == 6 || contentType == 7
						|| contentType == 9) {
					String paths = (String) body.get("content");
					Query<Emoji> que = dsForRW.createQuery(Emoji.class);
					List<Emoji> list = que.asList();
					for (int i = 0; i < list.size(); i++) {
						if (list.get(i).getUrl() == paths) {
							return;
						} else {
							try {
								// 调用删除方法将文件从服务器删除
								ConstantUtil.deleteFile(paths);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				dbCollection.remove(query); // 将消息记录中的数据删除
			}
		} else {
			List<String> jidList = dsForRoom.getCollection(Room.class).distinct("jid", new BasicDBObject());

			for (int j = 0; j < jidList.size(); j++) {
				DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_" + jidList.get(j));
				BasicDBObject query = new BasicDBObject();
				if (0 != startTime) {
					query.put("ts", new BasicDBObject("$gte", startTime));
				}
				if (0 != endTime) {
					query.put("ts", new BasicDBObject("$gte", endTime));
				}
				DBCursor cursor = dbCollection.find(query);
				if (cursor.size() > 0) {
					BasicDBObject dbObj = (BasicDBObject) cursor.next();
					// 解析消息体
					Map<String, Object> body = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""),
							Map.class);
					int contentType = (int) body.get("type");

					if (contentType == 2 || contentType == 3 || contentType == 5 || contentType == 6 || contentType == 7
							|| contentType == 9) {
						String paths = (String) body.get("content");
						Query<Emoji> que = dsForRW.createQuery(Emoji.class);
						List<Emoji> list = que.asList();
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i).getUrl() == paths) {
								return;
							} else {
								try {
									// 调用删除方法将文件从服务器删除
									ConstantUtil.deleteFile(paths);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
					dbCollection.remove(query); // 将消息记录中的数据删除
				}
			}

		}

		referer(response, "/console/groupchat_logs_all?room_jid_id=" + room_jid_id, 0);
	}

	/**
	 * 
	 * <p>Title: publicNumList</p>  
	 * <p>Description:获取公众号列表</p>  
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/publicNumList", method = { RequestMethod.GET })
	public ModelAndView publicNumList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "50") int pageSize) {
		ModelAndView mav = new ModelAndView("publicNumList");
		Query<PublicNum> query = pubNumManager.createQuery();
		long total = query.countAll();
		List<PublicNum> pageData = query.order("publicId").offset(pageIndex * pageSize).limit(pageSize).asList();
		PageVO page = new PageVO(pageData, total, pageIndex, pageSize);
		mav.addObject("page", page);

		return mav;
	}

	/**
	 * 
	 * <p>Title: updatePublicNum</p>  
	 * <p>Description:添加或获取公众号信息 </p>  
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/updatePublicNum", method = { RequestMethod.GET })
	public ModelAndView updatePublicNum(@RequestParam(defaultValue = "0") String id) {
		PublicNum pub = null;
		if ("0".equals(id)) {
			pub = new PublicNum();
			pub.setType(0);
		} else {
			ObjectId objId = new ObjectId(id);
			pub = pubNumManager.findOne("_id", objId);
			//如果联系电话或者id为空则查询
			if(null==pub.getPhone()){
				//根据id查询电话
				String phone=userManager.getPhoneById(pub.getCsUserId());
				pub.setPhone(phone);
			}
			if(null==pub.getCsUserId()){
				//根据电话查询userId
				Integer csUserId=userManager.getIdByPhone(pub.getPhone());
				pub.setCsUserId(csUserId);
			}
		}
		ModelAndView mav = new ModelAndView("updatePublicNum");
		mav.addObject("p", pub);
		return mav;
	}

	/**
	 * 
	 * <p>Title: savePubNumMsg</p>  
	 * <p>Description: 添加或修改公众号</p>  
	 * @param request
	 * @param response
	 * @param id
	 * @param publicNum
	 * @throws Exception
	 */
	@RequestMapping(value = "/updatePublicNum", method = { RequestMethod.POST })
	public void savePubNumMsg(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") String pid, PublicNum publicNum) throws Exception {
		if ("0".equals(pid)||null==pid||"null".equals(pid)) {
			publicNum.setPublicId(null);
			pubNumManager.addPublicNum(publicNum);
			//修改用户isCS状态为1
			userManager.updateIsCS(publicNum.getCsUserId(),1);
		} else {
			ObjectId objId = new ObjectId(pid);
			publicNum.setId(objId);
			pubNumManager.updatePub(publicNum);
			//如果是注销了公众号
			if(1==publicNum.getIsDel()){
				Integer csUserId=publicNum.getPublicId();
				int count=pubNumManager.judgeCS(csUserId);
				if(count<1){
					userManager.updateIsCS(publicNum.getCsUserId(),0);
				}
			}else{
				userManager.updateIsCS(publicNum.getCsUserId(),1);
			}
		}

		// 更新数据
		response.sendRedirect("/console/publicNumList");
	}

	/**
	 * 
	 * <p>Title: deletePublicNum</p>  
	 * <p>Description: 软删除公众号</p>  
	 * @param request
	 * @param response
	 * @param pid
	 * @throws Exception
	 */
	@RequestMapping(value = "/deletePublicNum", method = { RequestMethod.GET })
	public void deletePublicNum(HttpServletRequest request, HttpServletResponse response, String pid) throws Exception {
		ObjectId objId = new ObjectId(pid);
		//将该公众号注销
		pubNumManager.deleteByObjectId(objId);
		//更改公众号客服的isCS状态
		PublicNum publicNum=pubNumManager.getbyObjectId(objId);
		//判断客服是否还绑定其他公众号
		//如果绑定为空则修改isCS为0
		Integer csUserId=publicNum.getPublicId();
		int count=pubNumManager.judgeCS(csUserId);
		if(count<1){
			userManager.updateIsCS(publicNum.getCsUserId(),0);
		}
		// 更新数据
		response.sendRedirect("/console/publicNumList");
	}

	/**
	 * 
	
	 * <p>Title: complintList</p>  
	 * <p>Description: 投诉与反馈列表</p>  
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping(value = "/complintList", method = { RequestMethod.GET })
	public ModelAndView complintList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "50") int pageSize) {
		ModelAndView mav = new ModelAndView("complintList");
		Query<Complint> query = complintManager.createQuery();
		long total = query.countAll();
		List<Complint> pageData = query.order("createTime").offset(pageIndex * pageSize).limit(pageSize).asList();
		PageVO page = new PageVO(pageData, total, pageIndex, pageSize);
		mav.addObject("page", page);

		return mav;
	}
	
	/**
	 * 
	 * <p>Title: updateComplint</p>  
	 * <p>Description: 修改投诉反馈处理状态</p>  
	 * @param request
	 * @param response
	 * @param id
	 * @throws Exception
	 */
	@RequestMapping(value = "/updateComplint", method = { RequestMethod.GET })
	public void updateComplint(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "0") String id) throws Exception {
			ObjectId objId = new ObjectId(id);
			Query<Complint> query = complintManager.createQuery().filter("_id", objId);
			UpdateOperations ops=complintManager.createUpdateOperations();
			ops.set("isHandle", 1);
			complintManager.update(query, ops);
		// 更新数据
		response.sendRedirect("/console/complintList");
	}
}
