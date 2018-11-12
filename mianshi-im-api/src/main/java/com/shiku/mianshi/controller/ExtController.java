package com.shiku.mianshi.controller;

import java.util.List;

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
import com.mongodb.BasicDBObject;

import cn.xyz.commons.constants.MsgType;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DES;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.FriendsManager;
import cn.xyz.mianshi.service.impl.RoomManagerImplForIM;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.MsgNotice;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.service.ApnsPushService;
import cn.xyz.service.JPushService;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
/**
 * 
* <p>Title: ExtController</p>  
* <p>Description:此类曾用于离线消息推送,现已弃用 </p>  
* @author xiaobai  
* @date 2018年7月26日
 */
public class ExtController {

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
	
	@RequestMapping(value = "/notify")
	public JSONMessage notify(@RequestParam(defaultValue="1") int type,@RequestParam int from, @RequestParam(defaultValue="0") int to,
			@RequestParam(defaultValue="") String roomJid,
			@RequestParam String body,
			@RequestParam(defaultValue="0") long ts) {
		try {
			MsgNotice notice=parseMsgNotice(from, to,2==type?1:0,roomJid, body);
			
			if(null==notice){
				log.info("text 为null 不需要推送。。。。");
				return null;
			}
			notice.setTime(ts);
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					if(1==type){
						log.info("====日 志====pushOne"+to+"-----------");
						pushOne(to, notice,0);
						
					}
					else {
						log.info("====日 志====pushGroup"+to+"-----------");
						pushGroup(to, notice);
						
					}
				}
			});
			
			return JSONMessage.success();
		} catch (Exception e) {
			log.error("=====错 误 日 志=====消息解析失败,body:" +body);
			e.printStackTrace();
		}
		return JSONMessage.failure("推送失败");
	}
	//推送给一个用户
	private JSONMessage pushOne(int to,MsgNotice notice,int isGroup){
		try {
			//判断用户是否开启消息免打扰
			User user=dsForRW.createQuery(User.class).filter("_id", to).get();
			if(user.getOfflineNoPushMsg()==1){
				return null;
			}
			//判断用户是否对好友设置了消息免打扰
			Friends friends=dsForRW.createQuery(Friends.class).field("userId").equal(to).field("toUserId").equal(notice.getFrom()).get();
			if(friends!=null&&friends.getOfflineNoPushMsg()==1){
				return null;
			}
			//判断用户是否对房间设置了消息免打扰
			Member member=(Member) dsForRoom.createQuery(Member.class).field("roomId").equal(roomManager.getRoomId(notice.getRoomJid())).get();
			if(member!=null&&member.getOfflineNoPushMsg()==1){
				return null;
			}
			
			String key1 = String.format("user:%s:channelId", to);
			String key2 = String.format("user:%s:deviceId", to);
			
			String channelId = jedisTemplate.get(key1);
			String deviceId = jedisTemplate.get(key2);
			if (null == deviceId) 
				return JSONMessage.failure("deviceId is Null");
				// channeId没有设置对应的userId或者对应的userId等于消息接收方
				 if("2".equals(deviceId)){
					if (iOSVoIPEnabled && !StringUtil.isEmpty(KSessionUtil.getAPNSToken(to))) {
						//苹果APP上架时需禁用此功能VoIP(即CallKit)
						if(notice.getType()==100||notice.getType()==110||notice.getType()==115||notice.getType()==120){
							//apns 推送
							ApnsPushService.getInstance().pushMsgToUser(notice,notice.getFileName());
							return JSONMessage.success();
						}
					} else {
						log.info("iOS VoIP is disabled, it's nice to AppStore!");
					}
					try {
						if(StringUtil.isEmpty(channelId)){
							System.out.println("离线推送：未发现匹配的与用户匹配的推送通道Id");
							return JSONMessage.failure("channelId is Null");
						}
						jpushService.send(channelId, "收到一条新消息");
					} catch (NumberFormatException e) {
						jedisTemplate.del(key2);
					}
					return JSONMessage.success();
				}else {
					return JSONMessage.success();
				}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	//推送给群组
	private void pushGroup(int to,MsgNotice notice){
		
		if(notice.getType()==1&&!StringUtil.isEmpty(notice.getObjectId())&&!StringUtil.isEmpty(notice.getRoomJid())){
			if(notice.getObjectId().equals(notice.getRoomJid())){
				ObjectId roomId = roomManager.getRoomId(notice.getRoomJid());
				List groupUserList = roomManager.getMermerListIdByRedis(roomId);
				
				BasicDBObject query=new BasicDBObject("onlinestate", 0).append("_id", new BasicDBObject(MongoOperator.IN, groupUserList));
				List<Integer> userList = userManager.distinct("_id",query);
				if(!userList.isEmpty()){
					for (Integer userId : userList) {
						notice.setTo(userId);
						notice.setToName(userManager.getNickName(userId));
						pushOne(userId, notice,1);
					}
				}
			}else{
				String[] objectIdlist=notice.getObjectId().split(" ");
				for(int i=0;i<objectIdlist.length;i++){
					notice.setTo(Integer.parseInt(objectIdlist[i]));
					notice.setToName(userManager.getNickName(Integer.parseInt(objectIdlist[i])));
					pushOne(Integer.parseInt(objectIdlist[i]), notice,1);
				}
			}
		}else{
			ObjectId roomId = roomManager.getRoomId(notice.getRoomJid());
			List groupUserList = roomManager.getMermerListIdByRedis(roomId);
			//groupUserList.remove(notice.getFrom());
			BasicDBObject query=new BasicDBObject("onlinestate", 0).append("_id", new BasicDBObject(MongoOperator.IN, groupUserList));
			List<Integer> userList = userManager.distinct("_id",query);
			if(!userList.isEmpty()){
				for (Integer userId : userList) {
					if(userId==notice.getFrom())
						continue ;
					notice.setTo(userId);
					notice.setToName(userManager.getNickName(userId));
					pushOne(userId, notice,1);
				}
			}
		}
		
		
	}
	/**
	 * 
	 * <p>Title: parseMsgNotice</p>  
	 * <p>Description: 将传过来的参数进行封装,</p>  
	 * @param from
	 * @param to
	 * @param isGroup
	 * @param jid
	 * @param body
	 * @return
	 */
	private MsgNotice parseMsgNotice(int from,int to,int isGroup,String jid,String body){
		MsgNotice notice=new MsgNotice();
		int messageType = 0;
		String text =null;
		try {
			if(body.contains("http://api.map.baidu.com/staticimage")){
				messageType=MsgType.TYPE_LOCATION;
				notice.setName(userManager.getNickName(from));
				notice.setFrom(from);
				if(1==isGroup){
					notice.setTitle(roomManager.getRoomName(jid));
					notice.setRoomJid(jid);
					notice.setGroupName("("+notice.getTitle()+")");
					text=notice.getName()+notice.getGroupName()+":[位置]";
					notice.setIsGroup(1);
				}else{
					notice.setTo(to);
					notice.setTitle(notice.getName());
					text=notice.getName()+":[位置]";
				}
			}else{
				JSONObject jsonObj = JSON
						.parseObject(body);
				messageType = jsonObj.getIntValue("type");
				notice.setFrom(jsonObj.getIntValue("fromUserId"));
				notice.setTo(to);
				notice.setName(jsonObj.getString("fromUserName"));
				notice.setToName(jsonObj.getString("toUserName"));
				notice.setFileName(jsonObj.getString("fileName"));
				notice.setIsGroup(isGroup);
				notice.setTitle(notice.getName());
				if(!StringUtil.isEmpty(jsonObj.getString("objectId")))
					notice.setObjectId(jsonObj.getString("objectId"));
				
				if(!StringUtil.isEmpty(jid)){
					notice.setRoomJid(jid);
					notice.setTitle(roomManager.getRoomName(jid));
					notice.setGroupName("("+notice.getTitle()+")");
				}else if(!StringUtil.isEmpty(jsonObj.getString("objectId"))){
					if(roomManager.getRoomName(jsonObj.getString("objectId"))!=null)
						notice.setTitle(roomManager.getRoomName(jsonObj.getString("objectId")));
					notice.setGroupName("("+notice.getTitle()+")");
				}
					
				
				
				if(1==jsonObj.getIntValue("isEncrypt")){
					text=DES.decryptDES(jsonObj.getString("content"), "12345678");
				}else
					text=jsonObj.getString("content");
				text=parseText(messageType,isGroup, notice, text);
			}
			
			if(null==text)
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		notice.setType(messageType);
		notice.setText(text);
		if(StringUtil.isEmpty(notice.getToName()))
			notice.setToName(userManager.getNickName(to));
		
		return notice;
	}
	/**
	 * 
	
	 * <p>Title: parseText</p>  
	 * <p>Description: 用于messageType来组装推送的消息体</p>  
	 * @param messageType
	 * @param isGroup
	 * @param notice
	 * @param content
	 * @return
	 */
	private String parseText(int messageType,int isGroup, MsgNotice notice,String content) {
		String text=null;
		try {
			
			switch (messageType) {
				case MsgType.TYPE_TEXT:
				case MsgType.TYPE_FEEDBACK:
					
					text =content;
					if (StringUtils.hasText(text)) {
						if (text.length() > 20)
							text = text.substring(0, 20) + "...";
					}
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+text;
					if(!StringUtil.isEmpty(notice.getObjectId())&&!StringUtil.isEmpty(notice.getRoomJid())){
						text="[有人@我]"+notice.getName()+notice.getGroupName()+":"+content;
						if(notice.getObjectId().equals(notice.getRoomJid())){
							text="[有全体消息]"+notice.getName()+notice.getGroupName()+":"+content;
						}
								
					}
					break;
				case MsgType.TYPE_IMAGE:
				case MsgType.TYPE_GIF:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[图片]";
					break;
				case MsgType.TYPE_VOICE:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[语音]";
					break;
				case MsgType.TYPE_LOCATION:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[位置]";
					break;
				case MsgType.TYPE_VIDEO:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[视频]";
					break;
				case MsgType.TYPE_CARD:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[名片]";
					break;
				case MsgType.TYPE_FILE:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[文件]";
					break;
				case MsgType.TYPE_RED:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[红包]";
					break;
				case MsgType.TYPE_83:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+"领取了红包";
					break;
				case MsgType.TYPE_IMAGE_TEXT:
				case MsgType.TYPE_IMAGE_TEXT_MANY:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+":"+"[图文]";
					break;
				case MsgType.TYPE_BACK:
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+"撤回了一条消息";
					break;
				case MsgType.TYPE_MUCFILE_ADD:
					notice.setIsGroup(1);
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+"上传了群文件";
					break;
				case MsgType.TYPE_MUCFILE_DEL:
					notice.setIsGroup(1);
					text=notice.getName()+(1==isGroup?notice.getGroupName():"")+"删除了文件";
					break;
				case MsgType.TYPE_SAYHELLO:
					text=notice.getName()+":请求加为好友";
					break;
				case MsgType.TYPE_DELALL:
					text=notice.getName()+":删除好友你";
					break;
					
				case MsgType.TYPE_PASS:
					text=notice.getName()+":同意加好友";
					break;
				case MsgType.TYPE_CHANGE_NICK_NAME:
					notice.setIsGroup(1);
					text=notice.getName()+(notice.getGroupName())+"修改群昵称为"+content;
					break;
				case MsgType.TYPE_CHANGE_ROOM_NAME:
					notice.setIsGroup(1);
					text=notice.getName()+(notice.getGroupName())+"修改群名为"+content;
					break;
				case MsgType.TYPE_DELETE_MEMBER:
					notice.setIsGroup(1);
					if(notice.getFrom()==notice.getTo())
						text=notice.getToName()+(notice.getGroupName())+"退出群组";
					else {
						text=notice.getToName()+(notice.getGroupName())+"被踢出群组";
					}
					break;
				case MsgType.TYPE_NEW_NOTICE:
					notice.setIsGroup(1);
					text=notice.getName()+(notice.getGroupName())+"修改了群公告"+content;
					break;
				case MsgType.TYPE_GAG:
					notice.setIsGroup(1);
					long ts=Long.parseLong(content);
					//long time=DateUtil.currentTimeSeconds();
					if(0<ts)
						text=notice.getToName()+(notice.getGroupName())+"被禁言";
					else text=notice.getToName()+(notice.getGroupName())+"被取消禁言";
					break;
				case MsgType.NEW_MEMBER:
					notice.setIsGroup(1);
					text=notice.getToName()+(notice.getGroupName())+"加入群组";
					break;
				case MsgType.TYPE_SEND_MANAGER:
					notice.setIsGroup(1);
					if(1==Integer.parseInt(content))
						text=notice.getToName()+(notice.getGroupName())+"被设置管理员";
					else 
						text=notice.getToName()+(notice.getGroupName())+"被取消管理员";
					break;
					
					
				case MsgType.TYPE_INPUT:
				case MsgType.TYPE_COMMENT:
				case MsgType.TYPE_CHANGE_SHOW_READ:
					return null;
				default:
					if(StringUtil.isEmpty(content))
						return null;
					else 
						text=content;
					break;
			}
		} catch (Exception e) {
			log.error("=====错 误 日 志=====:" + "推送消息格式化失败,消息体:"+notice+"内容"+content);
			e.printStackTrace();
		}
		return text;
	}
}
