package com.shiku.mianshi.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DES;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.FriendsManager;
import cn.xyz.mianshi.service.impl.RoomManagerImplForIM;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.Fans;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.MsgNotice;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.mianshi.vo.User;
import cn.xyz.service.ApnsPushService;
import cn.xyz.service.BaiduPushService;
import cn.xyz.service.HWPushService;
import cn.xyz.service.HWPushTransService;
import cn.xyz.service.JPushService;
import cn.xyz.service.XMPushService;
import cn.xyz.service.XMPushTransService;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;
import lombok.extern.slf4j.Slf4j;

/**
 * Tigase支持接口
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/tigase")
@Slf4j
public class TigaseController extends AbstractController {

	@Value("${iOS.AppStore.VoIP.enabled:false}")
	private Boolean iOSVoIPEnabled;
	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRW")
	protected Datastore dsForRW;
	@Resource(name="dsForRoom")
	protected Datastore dsForRoom;
	@Resource(name = "jedisTemplate")
	protected JedisTemplate jedisTemplate;
	@Autowired
	private FriendsManager friendsManager;
	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	private RoomManagerImplForIM roomManager;
	@Autowired
	private JPushService jpushService;
	/*@Autowired
	private ApnsPushService apnsPushService;*/
	
	//单聊聊天记录
	@RequestMapping("/shiku_msgs")
	public JSONMessage getMsgs(@RequestParam int receiver, @RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "20") int pageSize,@RequestParam(defaultValue = "100") int maxType) {
		int sender = ReqUtil.getUserId();
		DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		BasicDBObject q = new BasicDBObject();
		q.put("sender", sender);
		q.put("receiver", receiver);
		if(maxType>0)
			q.put("contentType",new BasicDBObject(MongoOperator.LT, maxType));
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));///待改

		java.util.List<DBObject> list = Lists.newArrayList();

		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("timeSend", -1)).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		//-----------------------------------------------------
	/*	q.put("sender",receiver);
		q.put("receiver",sender);
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));

		java.util.List<DBObject> list1 = Lists.newArrayList();

		DBCursor cursor1 = dbCollection.find(q).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor1.hasNext()) {
			list.add(cursor1.next());
		}*/
		//----------------------------------------------------------
		//String s=receiver+","+startTime+","+endTime;
		/*Collections.reverse(list);//倒序*/
		return JSONMessage.success("", list);
		
	}
	//漫游
	/*@RequestMapping("/shiku_history")
	public JSONMessage getHistory(){
		int from=ReqUtil.getUserId();
		DBCollection dbCollection=dsForTigase.getDB().getCollection("shiku_history");
		BasicDBObject q=new BasicDBObject();
		q.put("form",from);
		
		return null;
	}*/
	//群组聊天记录
	@RequestMapping("/shiku_muc_msgs")
	public JSONMessage getMucMsgs(@RequestParam String roomId, @RequestParam(defaultValue = "0") long startTime,
			@RequestParam(defaultValue = "0") long endTime, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "20") int pageSize,@RequestParam(defaultValue = "100") int maxType) {

		DBCollection dbCollection = dsForRoom.getDB().getCollection("mucmsg_"+roomId);
		BasicDBObject q = new BasicDBObject();
		q.put("room_jid_id", roomId);
		if(maxType>0)
			q.put("contentType",new BasicDBObject(MongoOperator.LT, maxType));
		if (0 != startTime)
			q.put("ts", new BasicDBObject("$gte", startTime));
		if (0 != endTime)
			q.put("ts", new BasicDBObject("$lte", endTime));

		java.util.List<DBObject> list = Lists.newArrayList();

		DBCursor cursor = dbCollection.find(q).sort(new BasicDBObject("timeSend", -1)).sort(new BasicDBObject("ts", -1)).skip(pageIndex * pageSize)
				.limit(pageSize);
		while (cursor.hasNext()) {
			list.add(cursor.next());
		}
		/*Collections.reverse(list);//倒序*/	
		return JSONMessage.success("", list);
	}

	
	
	//该方法未启用
	@RequestMapping(value = "/baiduPush")
	public JSONMessage baiduPush(@RequestParam Integer from, @RequestParam Integer to,@RequestParam(defaultValue="1") int type, @RequestParam String content) {
		try {
			//String c = new String(body.getBytes("iso8859-1"),"utf-8");
			String key1 = String.format("user:%s:channelId", to);
			String key2 = String.format("user:%s:deviceId", to);
			String channelId = jedisTemplate.get(key1);
			String deviceId = jedisTemplate.get(key2);
			if (null != deviceId && null != channelId) {
				String key3 = String.format("channelId:%s", channelId);
				String toUserId = jedisTemplate.get(key3);
				// channeId没有设置对应的userId或者对应的userId等于消息接收方
				if (!StringUtils.hasText(toUserId) || Integer.parseInt(toUserId) == to) {
					String fromUserName = "";
					fromUserName=userManager.getNickName(from);
					
					
					BaiduPushService.PushMessage msg = new BaiduPushService.PushMessage();
					msg.setTime(System.currentTimeMillis());
					msg.setUserId(from);
					msg.setToUserId(to);
					msg.setTitle(fromUserName);
					msg.setType(type);
					msg.setDescription(fromUserName+":" +content);
					
					String appId=userManager.get(new Integer(to.toString())).getAppId();
					BaiduPushService.pushSingle(Integer.parseInt(deviceId), channelId, msg,appId);
					return JSONMessage.success();
				} else {
					return JSONMessage.success();
				}
			} else {
				System.out.println("离线推送：未发现匹配的与用户匹配的推送通道Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.failure("推送失败");
	}
	
	@RequestMapping(value = "/push")
	public JSONMessage push(@RequestParam String text, @RequestParam String body) {
		System.out.println("push");
		List<Integer> userIdList = JSON.parseArray(text, Integer.class);
		try {
			//String c = new String(body.getBytes("iso8859-1"),"utf-8");
			KXMPPServiceImpl.getInstance().send(userIdList, body);
			return JSONMessage.success();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.failure("推送失败");
		// {userId:%1$s,toUserIdList:%2$s,body:'%3$s'}
	}
	
	@RequestMapping(value = "/OnlineState")
	public JSONMessage OnlineState(@RequestParam long userId, @RequestParam int OnlineState) {		
		List<Fans> data = friendsManager.getFansList((int)userId);
		List<Integer>userlist=new ArrayList<>();
		MessageBean messageBean = new MessageBean();
		messageBean.setFromUserId(Long.toString(userId));
		for (Fans fans : data) {
			userlist.add(fans.getToUserId());
		}
		if (OnlineState==0) {
			messageBean.setType(KXMPPServiceImpl.OFFLINE);
		}else{
			messageBean.setType(KXMPPServiceImpl.ONLINE);
		}
		try {
			KXMPPServiceImpl.getInstance().send(userlist, messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSONMessage.failure("推送失败");
	}
	//加密
	@RequestMapping(value = "/encrypt")
	public JSONMessage encrypt(@RequestParam String text, @RequestParam String key) {		
		
		Map<String,String> map=Maps.newConcurrentMap();
		try {
			text=DES.encryptDES(text, key);
			map.put("text", text);
			return JSONMessage.success(null, map);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("text", text);
			return JSONMessage.success(null, map);
		}
	}
	
	//解密
	@RequestMapping(value = "/decrypt")
	public JSONMessage decrypt(@RequestParam String text, @RequestParam String key) {		
		
		Map<String,String> map=Maps.newConcurrentMap();
		String content=null;
		try {
			content=DES.decryptDES(text, key);
			map.put("text", content);
			return JSONMessage.success(null, map);
		}catch (StringIndexOutOfBoundsException e) {
			//没有加密的 消息
			map.put("text", text);
			return JSONMessage.success(null, map);
		}catch (Exception e) {
			// TODO Auto-generated catch block
			map.put("text", text);
			return JSONMessage.success(null, map);
		}
	}

	//	获取消息接口(阅后即焚)
	//type 1 单聊  2 群聊
	@RequestMapping("/getMessage")
	public JSONMessage getMessage(@RequestParam(defaultValue="1") int type,@RequestParam String messageId,@RequestParam(defaultValue="0") ObjectId roomJid) throws Exception{
		DBCollection dbCollection=null;
		if(type==1)
			 dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		else 
			 dbCollection = dsForRoom.getDB().getCollection("mucmsg_"+roomJid);
			
			BasicDBObject query = new BasicDBObject();
			query.put("messageId",messageId); 
			Object data=dbCollection.findOne(query);
		
		return JSONMessage.success(null, data);
		
	}
	
	//	删除消息接口
	@RequestMapping("/deleteMsg")
	//type 1 单聊  2 群聊
	//delete 1  删除属于自己的消息记录 2：撤回 删除 整条消息记录
	public JSONMessage deleteMsg(@RequestParam(defaultValue="1") int type,@RequestParam(defaultValue="1") int delete,@RequestParam String messageId,@RequestParam(defaultValue="") String roomJid) throws Exception{
		int sender = ReqUtil.getUserId();
		DBCursor cursor = null;
		DBCollection dbCollection=null;
		try{
			if(type==1)
				 dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
			else 
				 dbCollection = dsForRoom.getDB().getCollection("mucmsg_"+roomJid);
			BasicDBObject query = new BasicDBObject();
			
			
			query.put("messageId",new BasicDBObject(MongoOperator.IN, messageId.split(","))); 
			if(1==delete)
				query.put("sender", sender);
			cursor = dbCollection.find(query);
			if(cursor.size()>0){
				
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				//解析消息体
				
				Map<String,Object> body = JSON.parseObject(dbObj.getString("body").replace("&quot;", "\""), Map.class);
				int contentType = (int) body.get("type");
				dbCollection.remove(query); //将消息记录中的数据删除	
				
					/**
					Type = 1,//文本
				    Type = 2,//图片
				    Type = 3,//语音
				    Type=4, //位置
				    Type=5,//动画
				    Type=6,//视频
				    Type=7,//音频
				    Type=8,//名片
				    Type=9, //文件
				    Type=10, //提醒
					 */
					if(contentType==2 || contentType==3 || contentType==5 || contentType==6 || contentType==7 || contentType==9){
						String paths = (String) body.get("content");
						//调用删除方法将文件从服务器删除
						ConstantUtil.deleteFile(paths);
					}
				}
				
			} catch (Exception e){
				e.printStackTrace();
			}finally {
				if(cursor != null) cursor.close(); 
			}
		
		return JSONMessage.success();
		
	}

	
	//修改消息的已读状态
	@RequestMapping("/changeRead")
	public JSONMessage changeRead(@RequestParam String messageId) throws Exception{
		
		try{
			DBCollection dbCollection = dsForTigase.getDB().getCollection("shiku_msgs");
		
			BasicDBObject query = new BasicDBObject();
			query.put("messageId", messageId);
			
			BasicDBObject dbObj = (BasicDBObject) dbCollection.findOne(query);
			String body=null;
			if(null==dbObj)
				return JSONMessage.success();
			else {
				body=dbObj.getString("body");
				if(null==body)
					return JSONMessage.success();
			}
			//解析消息体
			Map<String,Object> msgBody = JSON.parseObject(body.replace("&quot;", "\""), Map.class);
			msgBody.put("isRead",true); 
			 body = JSON.toJSON(msgBody).toString();
			dbCollection.update(query, new BasicDBObject(MongoOperator.SET,new BasicDBObject("body", body)));
				
			} catch (Exception e){
				e.printStackTrace();
			}
		
		return JSONMessage.success();
		
	}
	
}





















