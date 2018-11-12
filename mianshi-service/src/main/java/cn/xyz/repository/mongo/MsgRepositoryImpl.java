package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.Callback;
import cn.xyz.commons.support.jedis.JedisCallback;
import cn.xyz.commons.support.jedis.JedisCallbackVoid;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ThreadUtil;
import cn.xyz.mianshi.example.AddMsgParam;
import cn.xyz.mianshi.example.MessageExample;
import cn.xyz.mianshi.service.FriendsManager;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Comment;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.Givegift;

import cn.xyz.mianshi.vo.Msg;
import cn.xyz.mianshi.vo.Msg.Body;
import cn.xyz.mianshi.vo.Msg.Resource;
import cn.xyz.mianshi.vo.Praise;
import cn.xyz.mianshi.vo.User;
import cn.xyz.mianshi.vo.UserPhotoAndVideo;
import cn.xyz.repository.FriendsRepository;
import cn.xyz.repository.MongoRepository;
import cn.xyz.repository.MsgCommentRepository;
import cn.xyz.repository.MsgGiftRepository;
import cn.xyz.repository.MsgListRepository;
import cn.xyz.repository.MsgPraiseRepository;
import cn.xyz.repository.MsgRepository;
import cn.xyz.service.KXMPPServiceImpl;
import cn.xyz.service.KXMPPServiceImpl.MessageBean;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
/**
 * 
* <p>Title: MsgRepositoryImpl</p>  
* <p>Description: </p>  
* @author xiaobai  
* @date 2018年8月1日
 */
@Service
public class MsgRepositoryImpl extends MongoRepository implements MsgRepository {

	@Autowired
	private MsgCommentRepository commentRepository;
	@Autowired
	private FriendsRepository friendsRepository;
	@Autowired
	private FriendsManager friendsService;
	@Autowired
	private MsgGiftRepository giftRepository;
	@Autowired
	private MsgListRepository msgListRepository;
	@Autowired
	private MsgPraiseRepository pariseRepository;
	@Autowired
	private UserManager userService;
	@Autowired
	private UserPhotoAndVideoRepositoryImpl userPhotoRepository;
	/**
	 * 
	 */
	private static int TEXT=1;
	/**
	 * 
	 */
	private static int IMAGE=2;
	/**
	 * 
	 */
	private static int AUDIO=3;
	/**
	 * 
	 */
	private static int OTHER=4;
	
	
	@Override
	public ObjectId add(int userId, AddMsgParam param) {
		User user = userService.getUser(userId);
		Msg entity = Msg.build(user, param);
		// 保存商务圈消息
		dsForRW.save(entity);
		//保存到个人相册列表
		
		if(2==param.getType()){
			List<Resource> images = entity.getBody().getImages();
			for (Resource resource : images) {
				UserPhotoAndVideo u=new UserPhotoAndVideo();
				u.setMsgId(entity.getMsgId());
				u.setUesrId(user.getUserId());
				u.setUrl(resource.getOUrl());
				u.setTime(DateUtil.currentTimeSeconds());
				dsForRW.save(u);
			}
			
		}else if(4==param.getType()){
			 UserPhotoAndVideo u=new UserPhotoAndVideo();
			 Resource resource = entity.getBody().getVideos().get(0);
			 u.setMsgId(entity.getMsgId());
			 u.setUesrId(user.getUserId());
			 u.setUrl(resource.getOUrl());
			 u.setTime(DateUtil.currentTimeSeconds());
			 dsForRW.save(u);
		}
		//提醒朋友列表-.-
		if(null!=param.getUserRemindLook()){
			ThreadUtil.executeInThread(new Callback() {
				
				@Override
				public void execute(Object obj) {
					for(int i=0;i<param.getUserRemindLook().size();i++){
						push(userId,param.getUserRemindLook().get(i),entity.getMsgId());
					}
				}
			});
		}
		
		
		try {
			// 添加到最新商务圈榜单
			msgListRepository.addToLatestList(param.getCityId(), entity.getUserId(), entity.getMsgId().toString());
			// 推送消息给粉丝
			// PushManager.newMsg(userId, entity.getMsgId().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return entity.getMsgId();
	}

	private void push(int userId,int toUserId,ObjectId msgId){
		// xmpp推送
		User user = userService.getUser(userId);
		Query<Msg> q=dsForRW.createQuery(Msg.class);
		Msg msg=q.filter("msgId", msgId).get();
		int type=msg.getBody().getType();
		String url=null;
		if(type==TEXT){
			url=msg.getBody().getText();
		}else if(type==IMAGE){
			url=msg.getBody().getImages().get(0).getOUrl();
		}else if(type==AUDIO){
			url=msg.getBody().getAudios().get(0).getOUrl();
		}else if(type==OTHER){
			url=msg.getBody().getVideos().get(0).getOUrl();
		}
		String t=String.valueOf(type);
		String u=String.valueOf(msgId);
		String mm=u+","+t+","+url;
		MessageBean messageBean=new MessageBean();
		messageBean.setType(KXMPPServiceImpl.REMIND);
		messageBean.setFromUserId(String.valueOf(userId));
		messageBean.setFromUserName(user.getNickname());
		messageBean.setContent("");
		messageBean.setObjectId(mm);
		messageBean.setPortrait(user.getPortrait());
		
		try {
		/*	List<Integer> praiseuserIdlist=new ArrayList<Integer>();
			DBObject d=new BasicDBObject("msgId",msgId);
			praiseuserIdlist=distinct("s_praise", "userId", d);
			
			List<Integer> userIdlist=new ArrayList<Integer>();
			userIdlist=distinct("s_comment","userId", d);
			
			userIdlist.addAll(praiseuserIdlist);
			
			userIdlist.add(msg.getUserId());
			
			HashSet<Integer> hs=new HashSet<Integer>(userIdlist);
			List<Integer> list=new ArrayList<Integer>(hs);
			
			//移出集合中当前操作人
			for (int i = 0; i < list.size(); i++) {   
			       if (list.get(i).equals(userId)) {   
			    	   list.remove(i);   
			       }   
			    } */
			KXMPPServiceImpl.getInstance().send("10006",DigestUtils.md5Hex("10006"),toUserId, messageBean.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将说说进行软删除
	 */
	@Override
	public boolean delete(Integer userId, ObjectId msgId) {
		
		Query<Msg> query=dsForRW.createQuery(Msg.class);
		query.field(Mapper.ID_KEY).equal(msgId);
		
		UpdateOperations<Msg> ops=dsForRW.createUpdateOperations(Msg.class);
		ops.set("isDel", 1);
		UpdateResults results = dsForRW.update(query, ops);
		if(1==results.getUpdatedCount()){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * <p>Title: del</p>  
	 * <p>Description:此方法为彻底删除说说,现使用delete方法,进行软删除 </p>  
	 * @param userId
	 * @param msgId
	 * @return
	 */
	public boolean del(Integer userId, ObjectId msgId) {
		Query<Msg> query=dsForRW.createQuery(Msg.class);
		query.field(Mapper.ID_KEY).equal(msgId);
		Msg msg=query.get();
		Body body=null;
		if(null!=msg){
			body=msg.getBody();
		}
		try {
			// 删除消息主体
			dsForRW.delete(query);
			if(null!=body){
				if(null!=body.getImages()){
					deleteResource(body.getImages());
				}
				if(null!=body.getAudios()){
					deleteResource(body.getAudios());
				}
				if(null!=body.getVideos()){
					deleteResource(body.getVideos());
				}
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		// 删除评论
		dsForRW.delete(dsForRW.createQuery(Comment.class).field("msgId").equal(msgId));
		// 删除赞
		dsForRW.delete(dsForRW.createQuery(Praise.class).field("msgId").equal(msgId));
		// 删除礼物
		dsForRW.delete(dsForRW.createQuery(Givegift.class).field("msgId").equal(msgId));

		return true;
	}
	
	
	
	public List<Resource> deleteResource(List<Resource> resources){
		for (Resource resource : resources) {
			try {
				ConstantUtil.deleteFile(resource.getOUrl());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return resources;
	}
	private List<Msg> fetchAndAttach(int userId, Query<Msg> query) {
		List<Msg> msgList = query.asList();
		msgList.forEach(msg -> {
			msg.setComments(commentRepository.find(userId,msg.getMsgId(), null, 0, 30));
			msg.setPraises(pariseRepository.find(userId,msg.getMsgId(), null, 0, 10));
			msg.setGifts(giftRepository.find(msg.getMsgId(), null, 0, 10));
			msg.setIsPraise(pariseRepository.exists(ReqUtil.getUserId(), msg.getMsgId()) ? 1 : 0);
		});
		
		return msgList;
	}

	@Override
	public List<Msg> findByExample(int userId, MessageExample example) {
		List<Integer> userIdList = friendsRepository.queryFollowId(userId);
		userIdList.add(userId);

		Query<Msg> query = dsForRW.createQuery(Msg.class);

		if (!StringUtil.isEmpty(example.getBodyTitle())){
			query.field("body.title").contains(example.getBodyTitle());
		}
		if (0 != example.getCityId()){
			query.field("cityId").equal(example.getCityId());
		}
		if (0 != example.getFlag()){
			query.field("flag").equal(example.getFlag());
		}
		if (ObjectId.isValid(example.getMsgId())){
			query.field(Mapper.ID_KEY).lessThan(new ObjectId(example.getMsgId()));
		}
		query.filter("userId in", userIdList);
		query.field("visible").greaterThan(0);

		query.order("-_id").limit(example.getPageSize());

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> gets(int userId, String ids) {
		List<ObjectId> idList = Lists.newArrayList();
		JSON.parseArray(ids, String.class).forEach(id -> {
			idList.add(new ObjectId(id));
		});

		Query<Msg> query = dsForRW.createQuery(Msg.class).filter("_id in", idList);
		query.order("-_id").asList();

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> getUserMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize) {
		List<Msg> list = Lists.newArrayList();

		// 获取登录用户最新消息
		if (null == toUserId || userId.intValue() == toUserId.intValue()) {
			list = findByUser(userId, msgId, pageSize);
		}
		// 获取某用户最新消息
		else {
			// 获取BA关系
			Friends friends = friendsService.getFriends(new Friends(toUserId, userId));

			// 陌生人
			if (null == friends || (Friends.Blacklist.No == friends.getBlacklist() && Friends.Status.Stranger == friends.getStatus())) {
				list = findByUser(toUserId, msgId, 10);
			}
			// 关注或好友
			else if (Friends.Blacklist.No == friends.getBlacklist()) {
				list = findByUser(toUserId, msgId, pageSize);
			}
			// 黑名单
			else {
				// 不返回
			}
		}

		return list;
	}

	@Override
	public List<Msg> findByUser(Integer userId, ObjectId msgId, Integer pageSize) {
		Query<Msg> query = dsForRW.createQuery(Msg.class).field("userId").equal(userId);
		if (null != msgId){
			
			query.field(Mapper.ID_KEY).lessThan(msgId);
		}
		query.order("-_id").limit(pageSize);

		return fetchAndAttach(userId, query);
	}

	@Override
	public List<Msg> getUserMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize) {
		Query<Msg> query = dsForRW.createQuery(Msg.class).retrievedFields(true, "userId", "nickname").field("userId").equal(userId);
		if (null != msgId){
			query.field(Mapper.ID_KEY).lessThan(msgId);
			
		}
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public boolean forwarding(Integer userId, AddMsgParam param) {
		return true;
	}

	@Override
	public Msg get(int userId, ObjectId msgId) {
		String key = String.format("msg:%1$s", msgId.toString());
		boolean exists = jedisTemplate.keyExists(key);

		if (!exists) {
			Msg msg = dsForRW.createQuery(Msg.class).field(Mapper.ID_KEY).equal(msgId).get();

			jedisTemplate.execute(new JedisCallbackVoid() {

				@Override
				public void execute(Jedis jedis) {
					String value = msg.toString();

					Pipeline pipe = jedis.pipelined();
					pipe.set(key, value);
					pipe.expire(key, 43200);// 重置过期时间
					pipe.sync();
				}
			});
		}

		String text = jedisTemplate.execute(new JedisCallback<String>() {
			@Override
			public String execute(Jedis jedis) {
				return jedis.get(key);
			}
		});

		Msg msg;
		// 缓存未命中、超出缓存范围
		if (null == text || "".equals(text)) {
			msg = dsForRW.createQuery(Msg.class).field(Mapper.ID_KEY).equal(msgId).get();
		} else {
			// msg = JSON.parseObject(text, Msg.class);
			try {
				msg = new ObjectMapper().readValue(text, Msg.class);
			} catch (Exception e) {
				throw new ServiceException("消息缓存解析失败");
			}
		}

		msg.setComments(commentRepository.find(userId,msg.getMsgId(), null, 0, 10));
		msg.setPraises(pariseRepository.find(userId,msg.getMsgId(), null, 0, 10));
		msg.setGifts(giftRepository.find(msg.getMsgId(), null, 0, 10));
		msg.setIsPraise(pariseRepository.exists(userId, msg.getMsgId()) ? 1 : 0);
		return msg;
	}

	@Override
	public List<Msg> getMsgIdList(int userId, int toUserId, ObjectId msgId, int pageSize) {
		List<Integer> userIdList = friendsRepository.queryFollowId(userId);
		userIdList.add(userId);

		Query<Msg> query = dsForRW.createQuery(Msg.class).retrievedFields(true, "userId", "nickname").filter("userId in", userIdList);

		if (null != msgId){
			query.field(Mapper.ID_KEY).lessThan(msgId);
			
		}
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public List<Msg> getMsgList(Integer userId, Integer toUserId, ObjectId msgId, Integer pageSize,Integer pageIndex) {

		List<Integer> userIdList = friendsRepository.queryFollowId(userId);
		userIdList.add(userId);
		
		List<Integer> users=new ArrayList<Integer>();
		users.add(userId);

		Query<Msg> query1 = dsForRW.createQuery(Msg.class).filter("userId in", userIdList);
		if (null != msgId){
			query1.field(Mapper.ID_KEY).lessThan(msgId);
			
		}
		Query<Msg> query2=dsForRW.createQuery(Msg.class);
		Query<Msg> query3=dsForRW.createQuery(Msg.class);
		Query<Msg> query4=dsForRW.createQuery(Msg.class);
		
		CriteriaContainer criteria1 = query1.criteria("isDel").equal(0).criteria("visible").equal(1).criteria("userNotLook").hasNoneOf(users);
		CriteriaContainer criteria2=query2.criteria("isDel").equal(0).criteria("userId").equal(userId);
		CriteriaContainer criteria3=query3.criteria("isDel").equal(0).criteria("visible").equal(3).criteria("userLook").hasAnyOf(users);
		CriteriaContainer criteria4=query4.criteria("isDel").equal(0).criteria("visible").equal(4).criteria("userNotLook").hasNoneOf(users);
		query1.or(criteria1,criteria2,criteria3,criteria4);
		
		query1.order("-_id").offset(pageIndex*pageSize).limit(pageSize);

		return fetchAndAttach(userId, query1);
	}

	@Override
	public List<Msg> getSquareMsgList(int userId, ObjectId msgId, Integer pageSize) {
		Query<Msg> query = dsForRW.createQuery(Msg.class);
		if (null != msgId){
			query.field(Mapper.ID_KEY).lessThan(msgId);
			
		}
		query.order("-_id").limit(pageSize);

		return query.asList();
	}

	@Override
	public void update(ObjectId msgId, Msg.Op op, int activeValue) {
		Query<Msg> q = dsForRW.createQuery(Msg.class).field("_id").equal(msgId);
		UpdateOperations<Msg> ops = dsForRW.createUpdateOperations(Msg.class).inc(op.getKey(), activeValue).inc("count.total", activeValue);
		// 更新消息
		Msg entity = dsForRW.findAndModify(q, ops);  
		// 更新消息缓存

		// 上榜
		msgListRepository.addToHotList(entity.getCityId(), entity.getUserId(), msgId.toString(), entity.getCount().getTotal());
	}


}
