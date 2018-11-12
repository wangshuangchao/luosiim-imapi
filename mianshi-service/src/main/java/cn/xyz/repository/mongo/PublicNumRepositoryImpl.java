package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.UPublicNum;
import cn.xyz.repository.PublicNumRepository;

@Service
public class PublicNumRepositoryImpl extends BaseRepositoryImpl<PublicNum, ObjectId> implements PublicNumRepository {

	public static PublicNumRepositoryImpl getInstance() {
		return new PublicNumRepositoryImpl();
	}

	@Override
	public Integer addPublicNum(PublicNum publicNum) {
		BasicDBObject jo = new BasicDBObject();
		jo.put("publicId", publicNum.getPublicId());// 索引
		if (!StringUtil.isEmpty(publicNum.getNickname())) {
			jo.put("nickname", publicNum.getNickname());// 索引
		}
		jo.put("createTime", DateUtil.currentTimeSeconds());
		if(null!=publicNum.getCsUserId()){
			jo.put("csUserId", publicNum.getCsUserId());
		}
		if(!StringUtil.isEmpty(publicNum.getMessage())){
			jo.put("message", publicNum.getMessage());
		}
		if(!StringUtil.isEmpty(publicNum.getMessageUrl())){
			jo.put("messageUrl", publicNum.getMessageUrl());
		}
		if(!StringUtil.isEmpty(publicNum.getIntroduce())){
			jo.put("introduce", publicNum.getIntroduce());
		}
		if(!StringUtil.isEmpty(publicNum.getIndexUrlTital())){
			jo.put("indexUrlTital", publicNum.getIndexUrlTital());
		}
		if(!StringUtil.isEmpty(publicNum.getIndexUrl())){
			jo.put("indexUrl", publicNum.getIndexUrl());
		}
		if(!StringUtil.isEmpty(publicNum.getPortraitUrl())){
			jo.put("portraitUrl", publicNum.getPortraitUrl());
		}
		if(null!=publicNum.getPhone()){
			jo.put("phone",publicNum.getPhone());
		}
		jo.put("type", publicNum.getType());
		jo.put("isDel", publicNum.getIsDel());
		jo.put("updateTime", publicNum.getUpdateTime());
		// 1、生成新公众号
		dsForRW.getDB().getCollection("publicNum").save(jo);
		return publicNum.getPublicId();
	}

	@Override
	public PublicNum getPublicNum(Integer publicId) {
		PublicNum one = this.findOne("publicId", publicId);
		return one;
	}

	@Override
	public List<Integer> getServiceIds(Integer publicId) {
		List<Integer> result = new ArrayList<>();
		DBObject projection = new BasicDBObject("csUserId", 1);
		projection.put("_id", 0);
		DBCursor find = dsForRW.getDB().getCollection("publicNum").find(new BasicDBObject("publicId", publicId),
				projection);
		List<DBObject> array = find.toArray();
		for (DBObject dbObject : array) {
			Integer object = (Integer) dbObject.get("csUserId");
			result.add(object);
		}
		return result;
	}

	@Override
	public List<PublicNum> getPublcNumListForCS(Integer csUserId) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class);
		query.field("csUserId").equal(csUserId).field("isDel").equal(0);
		return query.asList();
	}

	@Override
	public PublicNum removePublicNum(Integer publicId, Integer csUserId) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class);
		query.field("publicId").equal(publicId);
		query.field("userId").equals(csUserId);
		PublicNum delete = dsForRW.findAndDelete(query);
		System.out.println("结束绑定公众号" + delete);
		UpdateOperations<PublicNum> ops = dsForRW.createUpdateOperations(PublicNum.class);
		UpdateResults update = dsForRW.update(query, ops);
		return delete;
	}

	@Override
	public UpdateResults deletePublicNum(Integer publicId) {
		Query<PublicNum> query = dsForRW.createQuery(PublicNum.class);
		query.field("publicId").equal(publicId);
		UpdateOperations<PublicNum> ops = dsForRW.createUpdateOperations(PublicNum.class);
		ops.set("isDel", 1);
		UpdateResults update = dsForRW.update(query, ops);
		System.out.println("软删除公众号" + update);

		return update;
	}

}
