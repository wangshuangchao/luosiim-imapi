package com.shiku.mianshi.mq;

import java.util.List;

import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;

import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.impl.RoomManagerImplForIM;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.Room.Member;
import cn.xyz.service.JPushService;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
//@RabbitListener(queues = "ole-push")
@SuppressWarnings("all")
public class MessageReceiver {
	/*
	 * @RabbitHandler public void process(String msg) {
	 * 
	 * try { System.out.println("MessageReceiver  : " + msg); } catch (Exception
	 * e) { e.printStackTrace(); } }
	 */
	// @RabbitHandler
	// public void process(MqMessage msg) {
	// System.out.println("Receiver2 : " + msg);
	// }

	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRW")
	protected Datastore dsForRW;
	@Resource(name = "dsForRoom")
	protected Datastore dsForRoom;
	@Resource(name = "jedisTemplate")
	protected JedisTemplate jedisTemplate;
	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	private RoomManagerImplForIM roomManager;
	@Autowired
	private JPushService jpush;

//	@Value("${spring.rabbitmq.queue-name}")
//	private String queueName;
	@RabbitListener(queues = "ole-push-dev", containerFactory = "rabbitListenerContainerFactory")
	public void process(@Payload byte[] msg) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			MqMessage message = mapper.readValue(msg, MqMessage.class);
			log.info("----------RabbitMQ 监听处理消息----------mqmessage+"+message);
			if (1 == message.getType()) {
				pushOne(message.getTo(), message);
			} else if(2 == message.getType()) {
				pushGroup(message.getRoomJid(), message);
			}
		
		} catch (Exception e) {
			log.error("------MQ处理消息异常------"+e.getMessage());
		}
	}

	// 推送给一个用户
	private JSONMessage pushOne(int to, MqMessage notice) {
		try {
			// 判断用户是否开启消息免打扰
			User user = dsForRW.createQuery(User.class).filter("_id", to).get();
			if (user.getOfflineNoPushMsg() == 1) {
				return null;
			}
			// 判断用户是否对好友设置了消息免打扰
			Friends friends = dsForRW.createQuery(Friends.class).field("userId").equal(to).field("toUserId")
					.equal(notice.getFrom()).get();
			if (friends != null && friends.getOfflineNoPushMsg() == 1) {
				return null;
			}
			// 判断用户是否对房间设置了消息免打扰
			Member member = (Member) dsForRoom.createQuery(Member.class).field("roomId")
					.equal(roomManager.getRoomId(notice.getRoomJid())).get();
			if (member != null && member.getOfflineNoPushMsg() == 1) {
				return null;
			}

			String key1 = String.format("user:%s:channelId", to);
			String key2 = String.format("user:%s:deviceId", to);

			String channelId = jedisTemplate.get(key1);
			String deviceId = jedisTemplate.get(key2);
			if (null == deviceId)
				return JSONMessage.failure("deviceId is Null");
			if ("2".equals(deviceId)) {
				try {
					if (StringUtil.isEmpty(channelId)) {
						System.out.println("离线推送：未发现匹配的与用户匹配的推送通道Id");
						return JSONMessage.failure("channelId is Null");
					}
					log.info("推送给:  "+to);
					jpush.send(channelId, "收到一条新消息");
				} catch (Exception e) {
					log.error("-错-误-日-志----极光推送失败-----"+e.getMessage());
					jedisTemplate.del(key2);
				}
				return JSONMessage.success();
			} else {
				return JSONMessage.success();
			}
		} catch (Exception e) {
			log.error("-错-误-日-志----极光推送失败-----"+e.getMessage());
		}finally{
			
		}
		return null;
	}

	// 推送给群组
	private void pushGroup(String to, MqMessage notice) {
		ObjectId roomId = roomManager.getRoomId(notice.getRoomJid());
		List<Integer> groupUserList = roomManager.getMermerListIdByRedis(roomId);
		BasicDBObject query = new BasicDBObject("onlinestate", 0).append("_id",
				new BasicDBObject(MongoOperator.IN, groupUserList));
		
		List<Integer> userList = userManager.distinct("_id", query);
		if (!userList.isEmpty()) {
			for (Integer userId : userList) {
				if (userId == notice.getFrom())
					continue;
				notice.setTo(userId);
				pushOne(userId, notice);
			}
		}
	}
}
