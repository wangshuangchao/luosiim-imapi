package cn.xyz.mianshi.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import cn.xyz.commons.support.mongo.MongoOperator;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.PublicNumVo;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.commons.vo.UpublicDto;
import cn.xyz.mianshi.service.PublicNumManager;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.PublicNumDvo;
import cn.xyz.mianshi.vo.UPublicNum;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.PublicNumRepository;
import cn.xyz.repository.UPublicNumRepository;

@Service(PublicNumManagerImpl.BEAN_ID)
public class PublicNumManagerImpl extends MongoRepository<PublicNum, Integer> implements PublicNumManager {

	public static final String BEAN_ID = "PublicNumManagerImpl";

	@Autowired
	private PublicNumRepository publicNumRepository;
	@Autowired
	private UPublicNumRepository uPublicNumRepository;

	/**
	 * 
	
	 * <p>Title: createPublicId</p>  
	
	 * <p>Description: 用与生成新的公众号ID</p>  
	
	 * @return
	 */
	public synchronized Integer createPublicId() {
		DBCollection collection = dsForRW.getDB().getCollection("idx_public");
		if (null == collection)
			return createIdxPublicCollection(collection, 0);
		DBObject obj = collection.findOne();
		if (null != obj) {
			Integer id = new Integer(obj.get("id").toString());
			id += 1;
			// 将id加1
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject(MongoOperator.INC, new BasicDBObject("id", 1)));
			return id;
		} else {
			return createIdxPublicCollection(collection, 0);
		}

	}

	/**
	 * 
	 * <p>Title: createIdxPublicCollection</p>  
	 * <p>Description:将最大公众号id存入数据库 </p>  
	 * @param collection
	 * @param publicId
	 * @return
	 */
	private Integer createIdxPublicCollection(DBCollection collection, long publicId) {
		if (null == collection)
			collection = dsForRW.getDB().createCollection("idx_public", new BasicDBObject());
		BasicDBObject init = new BasicDBObject();
		Integer id = getMaxPublicId();
		if (0 == id || id < 2000000)
			id = new Integer("20000001");
		id += 1;
		init.append("id", id);
		init.append("stub", "id");
		init.append("call", 300000);
		init.append("videoMeetingNo", 350000);
		collection.insert(init);
		return id;
	}

	/**
	 * 
	 * <p>Title: getMaxPublicId</p>  
	 * <p>Description:获取最大公众号id </p>  
	 * @return
	 */
	private Integer getMaxPublicId() {
		BasicDBObject projection = new BasicDBObject("publicId", 1);
		DBObject dbobj = dsForRW.getDB().getCollection("publicNum").findOne(null, projection,
				new BasicDBObject("publicNum", -1));
		if (null == dbobj)
			return 0;
		Integer id = new Integer(dbobj.get("publicId").toString());
		return id;
	}

	/**
	 * 创建新的公众号
	 */
	@Override
	public PublicNum createPublicNum() {
		PublicNum num = new PublicNum();
		num.setPublicId(createPublicId());
		return num;
	}

	/**
	 * 添加公众号,如果公众号已经存在,则添加一条记录在数据库,如果不存在,则创建该公众号
	 */
	@Override
	public ResultInfo<Integer> addPublicNum(PublicNum publicNum) {
		ResultInfo<Integer> result = new ResultInfo<>();
		Integer integer = publicNum.getPublicId();
		if (null == integer) {
			publicNum.setPublicId(createPublicId());
		}//判断该数据是否已经存在,如果存在则修改
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("publicId")
				.equal(publicNum.getPublicId()).field("csUserId").equal(publicNum.getCsUserId());
		PublicNum findOne = this.findOne(query);
		//如果不为空则更新
		if(findOne!=null){
			this.updatePub(publicNum);
			result.setCode("1000");
			result.setData(publicNum.getPublicId());
			result.setMsg("更新成功");
			return result;
		}
		publicNum.setIsDel(0);
		publicNum.setCreateTime(DateUtil.currentTimeSeconds());
		publicNum.setUpdateTime(DateUtil.currentTimeSeconds());
		Integer publicId = publicNumRepository.addPublicNum(publicNum);
		result.setCode("1000");
		result.setData(publicId);
		result.setMsg("创建成功");
		return result;
	}

	/**
	 * 获取用户已经关注的公众号列表
	 */
	@Override // 获取公众号列表
	public ResultInfo<UpublicDto> getAttentionList(Integer userId) {
		ResultInfo<UpublicDto> result = new ResultInfo<>();
		UpublicDto dto = new UpublicDto();

		List<UPublicNum> list = uPublicNumRepository.getPublicNumByUserId(userId);
		if (!list.isEmpty()) {
			dto.setList(list);
			result.setCode("1000");
			result.setData(dto);
			result.setMsg("获取成功");
			return result;
		}
		result.setCode("1001");
		result.setData(dto);
		result.setMsg("未关注公众号");
		return result;
	}

	/**
	 * 用户添加关注公众号
	 */
	@Override // 添加关注
	public ResultInfo<String> addAttention(Integer userId, Integer publicId) {
		ResultInfo<String> result = new ResultInfo<>();
		PublicNum publicNum = publicNumRepository.getPublicNum(publicId);
		if (null == publicNum) {
			result.setCode("1001");
			result.setMsg("公众号不存在");
			result.setData("");
			return result;
		}
		// 判断原来是否关注过
		UPublicNum up = uPublicNumRepository.getUpublicNum(userId, publicId);
		Object objectId=null;
		// 如果数据库存在,则修改isAtt为1
		if (null != up) {
			up.setIsAtt(1);
			objectId = uPublicNumRepository.addAttention(up);
		} else {
			UPublicNum uPub = new UPublicNum();
			uPub.setIsAtt(1);
			uPub.setPublicId(publicId);
			uPub.setUserId(userId);
			uPub.setTime(DateUtil.currentTimeSeconds());
			uPub.setPortraitUrl(publicNum.getPortraitUrl());
			uPub.setPublicName(publicNum.getNickname());
			uPub.setType(publicNum.getType());
			objectId = uPublicNumRepository.addAttention(uPub);
		}
		if (null != objectId) {
			result.setCode("1000");
			result.setMsg("关注成功");
			result.setData(objectId.toString());
			return result;
			
		}else{
			result.setCode("1001");
			result.setMsg("关注失败");
			result.setData("");
			return result;
		}
	}

	/**
	 * 获取公众号详情
	 */
	@Override
	public ResultInfo<PublicNum> getDetail(Integer publicId) {
		ResultInfo<PublicNum> result = new ResultInfo<>();

		PublicNum publicNum = this.findOne("publicId", publicId);
		if (null == publicNum) {
			result.setCode("1001");
			result.setData(publicNum);
			result.setMsg("公众号不存在");
			return result;
		}
		result.setCode("1000");
		result.setData(publicNum);
		result.setMsg("获取成功");
		return result;
	}

	/**
	 * 根据公众号名称搜索公众号
	 */
	@Override
	public ResultInfo<PublicNumDvo> getByName(Integer userId,String nickname) {
		ResultInfo<PublicNumDvo> result = new ResultInfo<>();
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("nickname")
		.equal(nickname).field("isDel").equal(0);
		PublicNum publicNum = this.findOne(query);
		PublicNumDvo dvo=new PublicNumDvo();
		if (null == publicNum) {
			result.setCode("1001");
			result.setData(dvo);
			result.setMsg("公众号不存在");
			return result;
		}
		dvo.setNickname(nickname);
		dvo.setIndexUrl(publicNum.getIndexUrl());
		dvo.setIndexUrlTital(publicNum.getIndexUrlTital());
		dvo.setMessage(publicNum.getMessage());
		dvo.setMessageUrl(publicNum.getMessageUrl());
		dvo.setPublicId(publicNum.getPublicId());
		dvo.setIntroduce(publicNum.getIntroduce());
		dvo.setType(publicNum.getType());
		dvo.setPortraitUrl(publicNum.getPortraitUrl());
		//判断是否已经关注
		boolean att = uPublicNumRepository.isAtt(userId, nickname);
		if(att){
			dvo.setIsAtt(1);
		}else{
			dvo.setIsAtt(0);
		}
		result.setCode("1000");
		result.setData(dvo);
		result.setMsg("获取成功");
		return result;
	}

	/**
	 * 获取客服id
	 */
	@Override
	public ResultInfo<Integer> getServiceId(Integer publicId) {
		ResultInfo<Integer> result = new ResultInfo<>();
		List<Integer> list = publicNumRepository.getServiceIds(publicId);
		if (list.isEmpty()) {
			result.setCode("1001");
			result.setData(0);
			result.setMsg("获取失败");
			return result;
		} else {
			// 如果只有一个客服
			if (list.size() == 1) {
				result.setCode("1000");
				result.setData(list.get(0));
				result.setMsg("获取成功");
				return result;
			} // 多过一个则随机选择客服
			int i = (int) (Math.random() * (list.size() - 1));
			result.setCode("1000");
			result.setData(list.get(i));
			result.setMsg("获取成功");
			return result;

		}
	}

	/**
	 * 获取用户已经绑定的公众号列表
	 */
	@Override
	public ResultInfo<PublicNumVo> getPublicNumListForCS(Integer csUserId) {
		ResultInfo<PublicNumVo> result = new ResultInfo<>();
		PublicNumVo vo = new PublicNumVo();
		List<PublicNum> list = publicNumRepository.getPublcNumListForCS(csUserId);
		if (!list.isEmpty()) {
			vo.setList(list);
			result.setCode("1000");
			result.setData(vo);
			result.setMsg("获取成功");
			return result;
		} else {
			result.setCode("1001");
			result.setData(vo);
			result.setMsg("该用户不是客服");
			return result;
		}
	}

	/**
	 * 取消关注公众号
	 */
	@Override
	public ResultInfo<String> cancelAttention(Integer userId, Integer publicId) {
		ResultInfo<String> result = new ResultInfo<>();
		PublicNum publicNum = publicNumRepository.getPublicNum(publicId);
		if (null == publicNum) {
			result.setCode("1001");
			result.setMsg("公众号不存在");
			result.setData("");
			return result;
		}

		Object objectId = uPublicNumRepository.cancelAttention(userId, publicId);
		if (null == objectId) {
			result.setCode("1001");
			result.setMsg("取消关注失败");
			result.setData("");
			return result;
		}
		result.setCode("1000");
		result.setMsg("取消成功");
		result.setData(objectId.toString());
		return result;
	}

	@Override
	public ResultInfo<String> removePublicNum(Integer publicId, Integer csUserId) {
		ResultInfo<String>  result=new ResultInfo<>();
		PublicNum publicNum = publicNumRepository.removePublicNum(publicId,csUserId);
		if(null==publicNum){
			result.setCode("1001");
			result.setData("失败");
			result.setMsg("删除客服失败");
			return result;
		}else{
			result.setCode("1000");
			result.setData(publicNum.toString());
			result.setMsg("删除客服成功");
			return result;
			
		}
	}

	@Override
	public ResultInfo<String> deletePublicNum(Integer publicId) {
		ResultInfo<String>  result=new ResultInfo<>();
		//将用户已经关注过该公众号设为未关注
		uPublicNumRepository.removeAtt(publicId);
		UpdateResults update= publicNumRepository.deletePublicNum(publicId);
		if(null==update){
			result.setCode("1001");
			result.setData("失败");
			result.setMsg("删除公众号失败");
			return result;
		}else{
			result.setCode("1000");
			result.setData(update.toString());
			result.setMsg("删除公众号成功");
			return result;
			
		}
	}

	@Override
	public boolean isAtt(Integer userId, String nickName) {
		boolean boo =uPublicNumRepository.isAtt(userId,nickName);
		return false;
	}

	@Override
	public void deleteByObjectId(ObjectId id) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("_id")
				.equal(id);
		UpdateOperations<PublicNum> ops=this.createUpdateOperations();
		ops.set("isDel", 1);
		this.update(query, ops);
	}
	
	
	public void updatePub(PublicNum publicNum){
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("_id")
				.equal(publicNum.getId());
		UpdateOperations<PublicNum> ops=this.createUpdateOperations();
		if(null!=publicNum.getPublicId()){
			ops.set("publicId", publicNum.getPublicId());
		}
		if(!StringUtil.isEmpty(publicNum.getNickname())){
			ops.set("nickname", publicNum.getNickname());
		}
		if(null!=publicNum.getCsUserId()){
			ops.set("csUserId", publicNum.getCsUserId());
		}
		if(!StringUtil.isEmpty(publicNum.getMessage())){
			ops.set("message", publicNum.getMessage());
		}
		if(!StringUtil.isEmpty(publicNum.getMessageUrl())){
			ops.set("messageUrl", publicNum.getMessageUrl());
		}
		if(!StringUtil.isEmpty(publicNum.getIntroduce())){
			ops.set("introduce", publicNum.getIntroduce());
		}
		if(!StringUtil.isEmpty(publicNum.getIndexUrlTital())){
			ops.set("indexUrlTital", publicNum.getIndexUrlTital());
		}
		if(!StringUtil.isEmpty(publicNum.getIndexUrl())){
			ops.set("indexUrl", publicNum.getIndexUrl());
		}
		if(!StringUtil.isEmpty(publicNum.getPortraitUrl())){
			ops.set("portraitUrl", publicNum.getPortraitUrl());
		}
		if(0!=DateUtil.currentTimeSeconds()){
			ops.set("updateTime", DateUtil.currentTimeSeconds());
		}
		if(null!=publicNum.getIsDel()){
			ops.set("isDel", publicNum.getIsDel());
		}
			ops.set("type", publicNum.getType());
		if(null!=publicNum.getPhone()){
			ops.set("phone", publicNum.getPhone());
		}
		this.update(query, ops);
	}

	public PublicNum getbyObjectId(ObjectId objId) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("_id")
				.equal(objId);
		return this.findOne(query);
	}

	public int judgeCS(Integer csUserId) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class).field("csUserId")
				.equal(csUserId).field("isDel").equal(0);
		List<PublicNum> asList = query.asList();
		if(asList.isEmpty()){
			return 0;
		}else{
			return asList.size();
			
		}
	}
}
