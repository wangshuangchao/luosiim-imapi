package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.spring.SpringBeansUtils;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.RoomManager;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.vo.InviteListVo;
import cn.xyz.mianshi.vo.InviteVo;
import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.PageVO;
import cn.xyz.mianshi.vo.Room;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.Room.Notice;
import cn.xyz.mianshi.vo.Room.Share;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.UserVo;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

@Service(RoomManager.BEAN_ID)
public class RoomManagerImplForIM extends MongoRepository<Room, ObjectId> implements RoomManager {

	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRW")
	private Datastore dsForRW;
	@Resource(name = "dsForRoom")
	private Datastore dsForRoom;
	@Autowired
	private UserManagerImpl userManager;

	private JedisTemplate jedisTemplate = SpringBeansUtils.getBean("jedisTemplate");

	private String roomMemerList = "roomMemerList:";
	/*
	 * int num=300000; int voide=350000;
	 */

	@Override
	public Room add(User user, Room example, List<Integer> memberUserIdList) {
		/*
		 * this.num=num+1;
		 * 
		 * if(num>349999){ num=300000; } this.voide=voide+1; if(voide>399999){
		 * voide=350000; }
		 */
		user.setNum(user.getNum() + 1);
		Room entity = new Room();
		entity.setId(ObjectId.get());
		entity.setJid(example.getJid());
		entity.setName(example.getName());// 必须
		entity.setDesc(example.getDesc());// 必须
		entity.setShowRead(example.getShowRead());
		/* entity.setCall("0"+user.getUserId()+user.getNum()); */
		entity.setCall(String.valueOf(userManager.createCall()));
		entity.setVideoMeetingNo(String.valueOf(userManager.createvideoMeetingNo()));
		entity.setSubject("");
		entity.setCategory(0);
		entity.setIsNeedVerification(0);// 是否需要群主认证进群,默认为0
		entity.setIsAllowAddFriend(0);;//是否可以群内添加好友,默认为0,可以
		entity.setTags(Lists.newArrayList());
		entity.setNotice(new Room.Notice());
		entity.setNotices(Lists.newArrayList());
		entity.setUserSize(0);
		// entity.setMaxUserSize(1000);
		entity.setMembers(Lists.newArrayList());
		entity.setCountryId(example.getCountryId());// 必须
		entity.setProvinceId(example.getProvinceId());// 必须
		entity.setCityId(example.getCityId());// 必须
		entity.setAreaId(example.getAreaId());// 必须
		entity.setLongitude(example.getLongitude());// 必须
		entity.setLatitude(example.getLatitude());// 必须
		entity.setUserId(user.getUserId());
		entity.setNickname(user.getNickname());
		entity.setCreateTime(DateUtil.currentTimeSeconds());
		entity.setModifier(user.getUserId());
		entity.setModifyTime(entity.getCreateTime());
		entity.setS(1);
		entity.setIsLook(example.getIsLook());// 是否可见
		if (null == entity.getName())
			entity.setName("我的群组");
		if (null == entity.getDesc())
			entity.setDesc("");
		if (null == entity.getCountryId())
			entity.setCountryId(0);
		if (null == entity.getProvinceId())
			entity.setProvinceId(0);
		if (null == entity.getCityId())
			entity.setCityId(0);
		if (null == entity.getAreaId())
			entity.setAreaId(0);
		if (null == entity.getLongitude())
			entity.setLongitude(0d);
		if (null == entity.getLatitude())
			entity.setLatitude(0d);

		// 保存房间配置
		// dsForTigase.save(entity);
		dsForRoom.save(entity);
		// dsForTigase.save(user);

		// 创建者
		Member member = new Member();
		member.setActive(DateUtil.currentTimeSeconds());
		member.setCreateTime(member.getActive());
		member.setModifyTime(0L);
		member.setNickname(user.getNickname());
		member.setPortrait(user.getPortrait());//新增头像
		member.setRole(1);
		member.setRoomId(entity.getId());
		member.setSub(1);
		member.setTalkTime(0L);
		member.setCall(entity.getCall());
		member.setVideoMeetingNo(entity.getVideoMeetingNo());
		member.setUserId(user.getUserId());
		// 初试成员列表
		List<Member> memberList = Lists.newArrayList(member);
		// 初试成员列表不为空
		if (null != memberUserIdList && !memberUserIdList.isEmpty()) {
			Long currentTimeSeconds = DateUtil.currentTimeSeconds();
			ObjectId roomId = entity.getId();
			for (int userId : memberUserIdList) {
				User _user = userManager.getUser(userId);
				// 成员
				Member _member = new Member();
				_member.setActive(currentTimeSeconds);
				_member.setCreateTime(currentTimeSeconds);
				_member.setModifyTime(0L);
				_member.setNickname(_user.getNickname());
				_member.setPortrait(_user.getPortrait());//新增头像
				_member.setRole(3);
				_member.setRoomId(roomId);
				_member.setSub(1);
				_member.setCall(entity.getCall());
				_member.setVideoMeetingNo(entity.getVideoMeetingNo());
				_member.setTalkTime(0L);
				_member.setUserId(_user.getUserId());

				memberList.add(_member);
				// xmpp推送
				MessageBean messageBean = new MessageBean();
				messageBean.setType(KXMPPServiceImpl.NEW_MEMBER);
				messageBean.setFromUserId(user.getUserId().toString());
				messageBean.setFromUserName(user.getNickname());
				messageBean.setToUserId(_user.getUserId().toString());
				messageBean.setFileSize(entity.getShowRead());
				messageBean.setContent(entity.getName());
				messageBean.setToUserName(_user.getNickname());
				messageBean.setFileName(entity.getId().toString());
				messageBean.setObjectId(example.getJid());
				try {
					KXMPPServiceImpl.getInstance().send(_user.getUserId(), messageBean.toString());
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		// 保存成员列表
		// dsForTigase.save(memberList);
		dsForRoom.save(memberList);

		updateUserSize(entity.getId(), memberList.size());

		return entity;
	}

	private void updateMemerListToRedis(String roomId, List<Integer> memerList) {
		String jsonStr = JSON.toJSONString(memerList);
		jedisTemplate.set(roomMemerList + roomId, jsonStr);
	}

	public List<Integer> getMermerListIdByRedis(ObjectId roomId) {
		String jsonStr = null;
		jsonStr = jedisTemplate.get(roomMemerList + roomId.toString());
		if (StringUtil.isEmpty(jsonStr)) {
			List<Integer> memberIdList = getMemberIdList(roomId, 0);
			// jsonStr = JSON.toJSONString(memberIdList);
			updateMemerListToRedis(roomId.toString(), memberIdList);
			return memberIdList;
		} else {
			List<Integer> memberIdList = JSON.parseArray(jsonStr, Integer.class);
			return memberIdList;
		}
	}

	@Override
	public void delete(User user, ObjectId roomId) {
		// IMPORTANT 1-3、删房间推送-已改

		Query<Room> query = dsForRoom.createQuery(Room.class).field("_id").equal(roomId);
		Room room = query.get();
		MessageBean messageBean = new MessageBean();
		messageBean.setFromUserId(user.getUserId() + "");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setType(KXMPPServiceImpl.DELETE_ROOM);
		// messageBean.setObjectId(room.getId().toString());
		messageBean.setObjectId(room.getJid());
		messageBean.setContent(room.getName());

		try {
			KXMPPServiceImpl.getInstance().send(getMemberIdList(room, user.getUserId()), messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				// 删除群组 清除 群组成员
				Query<Member> merQuery = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId);
				// dsForTigase.delete(merQuery);
				// dsForTigase.delete(query);
				dsForRoom.delete(merQuery);
				dsForRoom.delete(query);
				List<Integer> memberIdList = getMemberIdList(roomId, 0);
				updateMemerListToRedis(roomId.toString(), memberIdList);
				// JSON.parseArray(text, clazz)
			}
		});

	}

	@Override
	public boolean update(User user, ObjectId roomId, String roomName, String notice, String desc, int showRead) {
		boolean result = true;
		BasicDBObject q = new BasicDBObject("_id", roomId);
		BasicDBObject o = new BasicDBObject();
		BasicDBObject values = new BasicDBObject();
		Room room = get(roomId);
		if (!StringUtil.isEmpty(roomName) && (!room.getName().equals(roomName) || exisname(roomName) == null)) {

			// o.put("$set", new BasicDBObject("name", roomName));
			values.put("name", roomName);

			// IMPORTANT 1-2、改房间名推送-已改
			MessageBean messageBean = new MessageBean();
			messageBean.setFromUserId(user.getUserId() + "");
			messageBean.setFromUserName(user.getNickname());
			messageBean.setType(KXMPPServiceImpl.CHANGE_ROOM_NAME);
			// messageBean.setObjectId(roomId.toString());
			messageBean.setObjectId(room.getJid());
			messageBean.setContent(roomName);
			try {
				KXMPPServiceImpl.getInstance().send(getMemberIdList(room, 0), messageBean.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			values.put("name", room.getName());
			result = StringUtil.isEmpty(roomName) == false ? false : true;
		}
		if (!StringUtil.isEmpty(desc)) {
			// o.put("$set", new BasicDBObject("desc", desc));
			values.put("desc", desc);
		}
		if (!StringUtil.isEmpty(notice)) {
			BasicDBObject dbObj = new BasicDBObject();
			dbObj.put("roomId", roomId);
			dbObj.put("text", notice);
			dbObj.put("userId", user.getUserId());
			dbObj.put("nickname", user.getNickname());
			dbObj.put("time", DateUtil.currentTimeSeconds());

			// 更新最新公告
			// o.put("$set", new BasicDBObject("notice", dbObj));
			values.put("notice", dbObj);

			// 新增历史公告记录
			dsForRoom.getCollection(Room.Notice.class).save(dbObj);

			// IMPORTANT 1-5、改公告推送-已改
			MessageBean messageBean = new MessageBean();
			messageBean.setFromUserId(user.getUserId() + "");
			messageBean.setFromUserName(user.getNickname());
			messageBean.setType(KXMPPServiceImpl.NEW_NOTICE);
			// messageBean.setObjectId(roomId.toString());
			messageBean.setObjectId(room.getJid());
			messageBean.setContent(notice);
			try {
				KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (-1 < showRead && room.getShowRead() != showRead) {
			values.append("showRead", showRead);
			MessageBean messageBean = new MessageBean();
			messageBean.setType(KXMPPServiceImpl.SHOWREAD);
			messageBean.setFromUserId(user.getUserId().toString());
			messageBean.setFromUserName(user.getNickname());
			messageBean.setContent(String.valueOf(showRead));
			messageBean.setObjectId(room.getJid());
			try {
				KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		o.put("$set", values);
		dsForRoom.getCollection(Room.class).update(q, o);
		return result;
	}

	@Override
	public Room get(ObjectId roomId) {
		Room room = dsForRoom.createQuery(Room.class).field("_id").equal(roomId).get();

		if (null != room) {
			// Member member;
			List<Member> members = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).order("role")
					.order("createTime").asList();
			List<Notice> notices = dsForRoom.createQuery(Room.Notice.class).field("roomId").equal(roomId).asList();

			// room.setMember(member);
			room.setMembers(members);
			room.setNotices(notices);
		}

		return room;
	}

	public ObjectId getRoomId(String jid) {
		Room room = dsForRoom.createQuery(Room.class).field("jid").equal(jid).get();
		if (null != room)
			return room.getId();
		else
			return null;
	}

	public String getRoomName(String jid) {
		Room room = dsForRoom.createQuery(Room.class).field("jid").equal(jid).get();
		if (null != room)
			return room.getName();
		else
			return null;
	}

	@Override
	public List<Room> selectList(int pageIndex, int pageSize, String roomName) {
		Query<Room> q = dsForRoom.createQuery(Room.class);
		if (!StringUtil.isEmpty(roomName)) {
			// q.field("name").contains(roomName);
			q.or(q.criteria("name").containsIgnoreCase(roomName), q.criteria("desc").containsIgnoreCase(roomName));
		}
		q.filter("isLook", 0);
		List<Room> roomList = q.offset(pageIndex * pageSize).limit(pageSize).order("-_id").asList();
		return roomList;
	}

	@Override
	public Object selectHistoryList(int userId, int type) {
		List<Object> historyIdList = Lists.newArrayList();

		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("userId").equal(userId);
		if (1 == type) {// 自己的房间
			q.filter("role =", 1);
		} else if (2 == type) {// 加入的房间
			q.filter("role !=", 1);
		}
		DBCursor cursor = dsForRoom.getCollection(Room.Member.class).find(q.getQueryObject(),
				new BasicDBObject("roomId", 1));
		while (cursor.hasNext()) {
			DBObject dbObj = cursor.next();
			historyIdList.add(dbObj.get("roomId"));
		}

		if (historyIdList.isEmpty())
			return null;

		List<Room> historyList = dsForRoom.createQuery(Room.class).field("_id").in(historyIdList).order("-_id")
				.asList();
		historyList.forEach(room -> {
			Member member = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(room.getId()).field("userId")
					.equal(userId).get();
			room.setMember(member);
		});

		return historyList;
	}

	@Override
	public Object selectHistoryList(int userId, int type, int pageIndex, int pageSize) {
		List<Object> historyIdList = Lists.newArrayList();

		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("userId").equal(userId);
		if (1 == type) {// 自己的房间
			q.filter("role =", 1);
		} else if (2 == type) {// 加入的房间
			q.filter("role !=", 1);
		}
		DBCursor cursor = dsForRoom.getCollection(Room.Member.class).find(q.getQueryObject(),
				new BasicDBObject("roomId", 1));
		while (cursor.hasNext()) {
			DBObject dbObj = cursor.next();
			historyIdList.add(dbObj.get("roomId"));
		}

		if (historyIdList.isEmpty())
			return null;

		List<Room> historyList = dsForRoom.createQuery(Room.class).field("_id").in(historyIdList).order("-_id")
				.offset(pageIndex * pageSize).limit(pageSize).asList();
		historyList.forEach(room -> {
			Member member = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(room.getId()).field("userId")
					.equal(userId).get();
			room.setMember(member);
		});

		return historyList;
	}

	@Override
	public void deleteMember(User user, ObjectId roomId, int userId) {
		Room room = get(roomId);
		User toUser = userManager.getUser(userId);

		// IMPORTANT 1-4、删除成员推送-已改
		MessageBean messageBean = new MessageBean();
		messageBean.setFromUserId(user.getUserId() + "");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setType(KXMPPServiceImpl.DELETE_MEMBER);
		// messageBean.setObjectId(roomId.toString());
		messageBean.setObjectId(room.getJid());
		messageBean.setToUserId(userId + "");
		messageBean.setToUserName(toUser.getNickname());
		messageBean.setContent(room.getName());
		try {
			KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId")
				.equal(userId);
		dsForRoom.delete(q);

		updateUserSize(roomId, -1);
		List<Integer> memberIdList = getMemberIdList(roomId, 0);
		updateMemerListToRedis(roomId.toString(), memberIdList);
	}

	@Override
	public void updateMember(User user, ObjectId roomId, List<Integer> userIdList) {
		for (int userId : userIdList) {
			Member _member = new Member();
			_member.setUserId(userId);
			_member.setRole(3);
			updateMember(user, roomId, _member);
		}
		List<Integer> memberIdList = getMemberIdList(roomId, 0);
		updateMemerListToRedis(roomId.toString(), memberIdList);
	}

	@Override
	public void updateMember(User user, ObjectId roomId, Member member) {
		DBCollection dbCollection = dsForRoom.getCollection(Room.Member.class);
		DBObject q = new BasicDBObject().append("roomId", roomId).append("userId", member.getUserId());
		Room room = get(roomId);
		User toUser = userManager.getUser(member.getUserId());
		Member oldMember = getMember(roomId, toUser.getUserId());
		if (1 == dbCollection.count(q)) {
			BasicDBObject values = new BasicDBObject();
			if (null != member.getRole())
				values.append("role", member.getRole());
			if (null != member.getPortrait())
				values.append("portrait", member.getPortrait());
			if (null != member.getSub())
				values.append("sub", member.getSub());
			if (null != member.getTalkTime())
				values.append("talkTime", member.getTalkTime());
			if (!StringUtil.isEmpty(member.getNickname()))
				values.append("nickname", member.getNickname());
			values.append("modifyTime", DateUtil.currentTimeSeconds());
			values.append("call", room.getCall());
			values.append("videoMeetingNo", room.getVideoMeetingNo());

			// 更新成员信息
			dbCollection.update(q, new BasicDBObject("$set", values));

			if (!StringUtil.isEmpty(member.getNickname()) && !oldMember.getNickname().equals(member.getNickname())) {
				// IMPORTANT 1-1、改昵称推送-已改
				MessageBean messageBean = new MessageBean();
				messageBean.setType(KXMPPServiceImpl.CHANGE_NICK_NAME);
				// messageBean.setObjectId(roomId.toString());
				messageBean.setObjectId(room.getJid());
				messageBean.setFromUserId(user.getUserId() + "");
				messageBean.setFromUserName(user.getNickname());
				messageBean.setToUserId(toUser.getUserId() + "");
				messageBean.setToUserName(toUser.getNickname());
				messageBean.setContent(member.getNickname());
				try {
					KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (null != member.getTalkTime()) {
				// IMPORTANT 1-6、禁言
				MessageBean messageBean = new MessageBean();
				messageBean.setType(KXMPPServiceImpl.GAG);
				// messageBean.setObjectId(roomId.toString());
				messageBean.setObjectId(room.getJid());
				messageBean.setFromUserId(user.getUserId() + "");
				messageBean.setFromUserName(user.getNickname());
				messageBean.setToUserId(toUser.getUserId() + "");
				messageBean.setToUserName(toUser.getNickname());
				messageBean.setContent(member.getTalkTime() + "");
				try {
					KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			User _user = userManager.getUser(member.getUserId());
			Member _member = new Member();
			_member.setActive(DateUtil.currentTimeSeconds());
			_member.setCreateTime(_member.getActive());
			_member.setModifyTime(0L);
			_member.setNickname(_user.getNickname());
			_member.setPortrait(_user.getPortrait());
			_member.setRole(member.getRole());
			_member.setRoomId(roomId);
			_member.setSub(1);
			_member.setTalkTime(0L);
			_member.setUserId(_user.getUserId());

			dsForRoom.save(_member);

			updateUserSize(roomId, 1);

			// IMPORTANT 1-7、新增成员
			MessageBean messageBean = new MessageBean();
			messageBean.setType(KXMPPServiceImpl.NEW_MEMBER);
			// messageBean.setObjectId(roomId.toString());
			messageBean.setObjectId(room.getJid());
			messageBean.setFromUserId(user.getUserId() + "");
			messageBean.setFromUserName(user.getNickname());
			messageBean.setToUserId(toUser.getUserId() + "");
			messageBean.setToUserName(toUser.getNickname());
			messageBean.setFileSize(room.getShowRead());
			messageBean.setContent(room.getName());
			messageBean.setFileName(room.getId().toString());
			List<Integer> memberIdList = getMermerListIdByRedis(roomId);
			memberIdList.add(_member.getUserId());
			try {
				KXMPPServiceImpl.getInstance().send(memberIdList, messageBean.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		List<Integer> memberIdList = getMemberIdList(roomId, 0);
		updateMemerListToRedis(roomId.toString(), memberIdList);
	}

	@Override
	public Member getMember(ObjectId roomId, int userId) {
		return dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId").equal(userId)
				.get();
	}

	@Override
	public void Memberset(Integer offlineNoPushMsg, ObjectId roomId, int userId) {
		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId")
				.equal(userId);
		UpdateOperations<Room.Member> ops = dsForRW.createUpdateOperations(Room.Member.class);
		ops.set("offlineNoPushMsg", offlineNoPushMsg);
		Room.Member data = dsForRoom.findAndModify(q, ops);
		// return JSONMessage.success(null, data);

	}

	@Override
	public List<Member> getMemberList(ObjectId roomId, String keyword) {
		List<Member> list = null;
		Query<Member> query = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId);
		if (!StringUtil.isEmpty(keyword))
			query.field("nickname").containsIgnoreCase(keyword);
		list = query.order("role").order("createTime").asList();
		/*
		 * for (Member member : list) {
		 * System.out.println(member.getNickname()); }
		 */
		return list;
	}

	public Object getMemberListByPage(ObjectId roomId, int pageIndex, int pageSize) {
		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId);
		List<Member> pageData = q.offset(pageIndex * pageSize).limit(pageSize).asList();
		for (Member member : pageData) {
			System.out.println(member.getNickname());
		}
		long total = q.countAll();
		return new PageVO(pageData, total, pageIndex, pageSize);
	}

	@Override
	public void join(int userId, ObjectId roomId, int type) {
		Member member = new Member();
		member.setUserId(userId);
		member.setRole(1 == type ? 1 : 3);
		updateMember(userManager.getUser(userId), roomId, member);
	}

	private void updateUserSize(ObjectId roomId, int userSize) {
		DBObject q = new BasicDBObject("_id", roomId);
		DBObject o = new BasicDBObject("$inc", new BasicDBObject("userSize", userSize));
		dsForRoom.getCollection(Room.class).update(q, o);
	}

	/**
	 * 获取房间成员列表
	 * 
	 * @param roomId
	 * @param userId
	 * @return
	 */
	public List<Integer> getMemberIdList(ObjectId roomId, int userId) {
		List<Integer> userIdList = Lists.newArrayList();

		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId);
		DBCursor cursor = dsForRoom.getCollection(Room.Member.class).find(q.getQueryObject(),
				new BasicDBObject("userId", 1));

		while (cursor.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) cursor.next();
			int _userId = dbObj.getInt("userId");
			// if (_userId != userId)
			userIdList.add(_userId);
		}

		return userIdList;
	}

	public List<Integer> getMemberIdList(Room room, int userId) {
		return getMemberIdList(room.getId(), userId);
	}

	@Override
	public void leave(int userId, ObjectId roomId) {

	}

	@Override
	public Room exisname(Object roomname) {
		Room room = dsForRoom.createQuery(Room.class).field("name").equal(roomname).get();
		if (null != room) {
			return room;
		}
		return null;
	}

	@Override
	public void delete(ObjectId roomId) {
		Room room = get(roomId);

		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.DELETE_ROOM);
		// messageBean.setObjectId(room.getId().toString());
		messageBean.setObjectId(room.getJid());
		messageBean.setContent(room.getName());
		List<Integer> list = new ArrayList<>();
		for (Room.Member member : getMemberList(roomId, null)) {
			list.add(member.getUserId());
		}
		try {
			KXMPPServiceImpl.getInstance().send(list, messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				// 删除群组 清除 群组成员
				Query<Member> merQuery = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId);
				dsForRoom.delete(merQuery);
				List<Integer> memberIdList = getMemberIdList(roomId, 0);
				updateMemerListToRedis(roomId.toString(), memberIdList);
			}
		});

	}

	// 设置/取消管理员
	@Override
	public void setAdmin(ObjectId roomId, int touserId, int type, int userId) {
		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId")
				.equal(touserId);
		UpdateOperations<Room.Member> ops = dsForRoom.createUpdateOperations(Room.Member.class);
		ops.set("role", type);
		dsForRoom.findAndModify(q, ops);
		Room room = get(roomId);
		User user = userManager.getUser(userId);
		// xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.SETADMIN);
		if (type == 2) {// 1为设置管理员
			messageBean.setContent(1);
		} else {
			messageBean.setContent(0);
		}
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserName(q.get().getNickname());
		messageBean.setToUserId(q.get().getUserId().toString());
		messageBean.setObjectId(room.getJid());
		try {
			List<Integer> list = new ArrayList<>();
			for (Room.Member member : getMemberList(roomId, null)) {
				list.add(member.getUserId());
			}
			KXMPPServiceImpl.getInstance().send(list, messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	// 添加文件（群共享）
	@Override
	public Share Addshare(ObjectId roomId, long size, int type, int userId, String url, String name) {
		User user = userManager.getUser(userId);
		Share share = new Share();
		share.setRoomId(roomId);
		share.setTime(DateUtil.currentTimeSeconds());
		share.setNickname(user.getNickname());
		share.setUserId(userId);
		share.setSize(size);
		share.setUrl(url);
		share.setType(type);
		share.setName(name);
		dsForRoom.save(share);
		Room room = get(roomId);
		// 上传文件xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.FILEUPLOAD);
		messageBean.setContent(share.getShareId().toString());
		messageBean.setFileName(share.getName());
		messageBean.setObjectId(room.getJid());
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		try {

			KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return share;
	}

	// 查询所有
	@Override
	public List<Share> findShare(ObjectId roomId, long time, int userId, int pageIndex, int pageSize) {
		/*
		 * List<Share>
		 * list=dsForTigase.createQuery(Room.Share.class).field("roomId").equal(
		 * roomId).asList();
		 */
		Query<Room.Share> q = dsForRoom.createQuery(Room.Share.class).field("roomId").equal(roomId);
		if (time != 0L) {
			q.filter("time", time);
		} else if (userId != 0) {
			q.filter("userId", userId);
		}

		List<Share> list = new ArrayList<Share>();
		list = q.offset(pageSize * pageIndex).limit(pageSize).asList();

		return list;
	}

	// 删除
	@Override
	public void deleteShare(ObjectId roomId, ObjectId shareId, int userId) {
		Query<Room.Share> q = dsForRoom.createQuery(Room.Share.class).field("roomId").equal(roomId).field("shareId")
				.equal(shareId);

		User user = userManager.getUser(userId);
		Room room = get(roomId);
		// 删除XMpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.DELETEFILE);
		messageBean.setContent(q.get().getShareId());
		messageBean.setFileName(q.get().getName());
		messageBean.setObjectId(room.getJid());
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		try {
			KXMPPServiceImpl.getInstance().send(getMermerListIdByRedis(roomId), messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		dsForRoom.delete(q);
	}

	// 获取单个文件
	@Override
	public Object getShare(ObjectId roomId, ObjectId shareId) {
		Share share = dsForRoom.createQuery(Room.Share.class).field("roomId").equal(roomId).field("shareId")
				.equal(shareId).get();
		return share;
	}

	@Override
	public String getCall(ObjectId roomId) {
		Room room = dsForRoom.createQuery(Room.class).field("_id").equal(roomId).get();
		return room.getCall();
	}

	@Override
	public String getVideoMeetingNo(ObjectId roomId) {
		Room room = dsForRoom.createQuery(Room.class).field("_id").equal(roomId).get();
		return room.getVideoMeetingNo();
	}

	/**
	 * 群主转让
	 */
	@Override
	public void transfer(ObjectId roomId, int userId, int toUserId) {
		// 修改Room的属性值
		Query<Room> query = dsForRoom.createQuery(Room.class).field("_id").equal(roomId);
		UpdateOperations<Room> op = dsForRoom.createUpdateOperations(Room.class);
		op.set("userId", toUserId);
		op.set("nickname", userManager.getUser(toUserId).getNickname());
		op.set("modifier", toUserId);
		dsForRoom.findAndModify(query, op);

		// 将群主role改为3
		Query<Room.Member> q = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId")
				.equal(userId);
		UpdateOperations<Room.Member> ops = dsForRoom.createUpdateOperations(Room.Member.class);
		ops.set("role", 3);
		// 将目标用户role改为1
		dsForRoom.findAndModify(q, ops);
		Query<Room.Member> q1 = dsForRoom.createQuery(Room.Member.class).field("roomId").equal(roomId).field("userId")
				.equal(toUserId);
		UpdateOperations<Room.Member> ops1 = dsForRoom.createUpdateOperations(Room.Member.class);
		ops1.set("role", 1);
		// 将目标用户role改为1
		dsForRoom.findAndModify(q1, ops1);

		Room room = get(roomId);
		User user = userManager.getUser(userId);
		User toUser = userManager.getUser(toUserId);
		// xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.TRANSFER);
		messageBean.setContent("群主转让给" + toUser.getNickname());
		messageBean.setFromUserId(user.getUserId().toString());
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserName(toUser.getNickname());
		messageBean.setToUserId(toUser.getUserId().toString());
		messageBean.setObjectId(room.getJid());
		try {
			List<Integer> list = new ArrayList<>();
			for (Room.Member member : getMemberList(roomId, null)) {
				list.add(member.getUserId());
			}
			KXMPPServiceImpl.getInstance().send(list, messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void askOwner(User user, ObjectId roomId, List<Integer> idList,String msg) {
		InviteListVo result=new InviteListVo(); 
		List<UserVo> vos=new ArrayList<>();
		result.setInviteId(user.getUserId());
		result.setInviteName(user.getNickname());
		for (Integer id : idList) {
			UserVo vo=new UserVo();
			User addUser = userManager.getUser(id);
			vo.setNickName(addUser.getNickname());
			vo.setUserId(id);
			vos.add(vo);
		}
		//InviteVo inviteVo=new InviteVo();
		//inviteVo.setAddUserIds(idList);
		//inviteVo.setPush(push)
		String push=user.getNickname()+"  想邀请"+idList.size()+"位朋友加入群聊";
		result.setPush(push);
		result.setUserVo(vos);
		result.setMsg(msg);
		///JSONObject jsonStu = JSONObject.fromObject(result.toString());
		Room room = get(roomId);
		// xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.APPLY);
		messageBean.setFromUserId(user.getUserId()+"");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(room.getUserId()+"");
		messageBean.setToUserId(room.getNickname());
		messageBean.setContent(result);
		messageBean.setObjectId(room.getJid());
		try {
			KXMPPServiceImpl.getInstance().send(room.getUserId(), messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void turnIsNeedVerification(ObjectId roomId, int isNeedVerification) {
		// 修改Room的属性值是否需要管理员认证
		Query<Room> query = dsForRoom.createQuery(Room.class).field("_id").equal(roomId);
		UpdateOperations<Room> op = dsForRoom.createUpdateOperations(Room.class);
		op.set("isNeedVerification", isNeedVerification);
		dsForRoom.findAndModify(query, op);
		Room room = get(roomId);
		//获取群成员
		List<Integer> memberIdList = getMermerListIdByRedis(roomId);
		// 推送消息
		// xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setFromUserId(room.getUserId()+"");
		messageBean.setFromUserName(room.getNickname());
		if(1==isNeedVerification){
			messageBean.setType(KXMPPServiceImpl.ISNEED);
			messageBean.setContent("群主已启用群聊邀请确认");
		}else{
			messageBean.setType(KXMPPServiceImpl.NONEED);
			messageBean.setContent("群主已恢复默认进群方式");
		}
		messageBean.setObjectId(room.getJid());
		try {
			KXMPPServiceImpl.getInstance().send(memberIdList, messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void addMember(User user, ObjectId roomId, Integer addUserId, Integer Inviter) {
		//获取房间信息
		Room room = get(roomId);
		User addUser = userManager.getUser(addUserId);
		//获取邀请者信息
		User userInviter = userManager.getUser(Inviter);
		//创建成员对象
		Member _member = new Member();
		_member.setActive(DateUtil.currentTimeSeconds());
		_member.setCreateTime(_member.getActive());
		_member.setModifyTime(0L);
		_member.setNickname(addUser.getNickname());
		//新增头像
		_member.setPortrait(addUser.getPortrait());
		_member.setRole(3);
		_member.setRoomId(roomId);
		_member.setSub(1);
		_member.setTalkTime(0L);
		_member.setUserId(addUserId);
		dsForRoom.save(_member);

		updateUserSize(roomId, 1);

		// IMPORTANT 1-7、新增成员
		MessageBean messageBean = new MessageBean();
		messageBean.setType(KXMPPServiceImpl.NEW_MEMBER);
		// messageBean.setObjectId(roomId.toString());
		messageBean.setObjectId(room.getJid());
		messageBean.setFromUserId(user.getUserId() + "");
		messageBean.setFromUserName(user.getNickname());
		messageBean.setToUserId(userInviter.getUserId() + "");
		messageBean.setToUserName(userInviter.getNickname());
		messageBean.setFileSize(room.getShowRead());
		messageBean.setContent(room.getName());
		messageBean.setFileName(room.getId().toString());
		List<Integer> memberIdList = getMermerListIdByRedis(roomId);
		memberIdList.add(_member.getUserId());
		try {
			KXMPPServiceImpl.getInstance().send(memberIdList, messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateMemerListToRedis(roomId.toString(), memberIdList);

	}

	@Override
	public void addMember(User user, ObjectId roomId, List<Integer> idList, Integer inviter) {
		for (int userId : idList) {
			Member _member = new Member();
			_member.setUserId(userId);
			_member.setRole(3);
			updateMember(user, roomId, _member);
		}
		List<Integer> memberIdList = getMemberIdList(roomId, 0);
		updateMemerListToRedis(roomId.toString(), memberIdList);
		
	}

	@Override
	public void turnIsAllowAddFriend(ObjectId roomId, Integer isAllowAddFriend) {

		// 修改Room的属性值是否需要管理员认证
		Query<Room> query = dsForRoom.createQuery(Room.class).field("_id").equal(roomId);
		UpdateOperations<Room> op = dsForRoom.createUpdateOperations(Room.class);
		op.set("isAllowAddFriend", isAllowAddFriend);
		dsForRoom.findAndModify(query, op);
		Room room = get(roomId);
		//获取群成员
		List<Integer> memberIdList = getMermerListIdByRedis(roomId);
		// 推送消息
		// xmpp推送
		MessageBean messageBean = new MessageBean();
		messageBean.setFromUserId(room.getUserId()+"");
		messageBean.setFromUserName(room.getNickname());
		if(1==isAllowAddFriend){
			messageBean.setType(KXMPPServiceImpl.ISNEED);
			messageBean.setContent("群主已设置可查看他人资料");
		}else{
			messageBean.setType(KXMPPServiceImpl.NONEED);
			messageBean.setContent("群主已设置不可查看他人资料");
		}
		messageBean.setObjectId(room.getJid());
		try {
			KXMPPServiceImpl.getInstance().send(memberIdList, messageBean.toString());
		} catch (Exception e) {
			// TODO: handle exception
		}
	
		
	}
}