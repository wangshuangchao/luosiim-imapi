package cn.xyz.repository.mongo;

import java.util.List;

import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.xyz.mianshi.vo.Friends;
import cn.xyz.mianshi.vo.UPublicNum;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.MongoRepository;
import cn.xyz.repository.UPublicNumRepository;
@Service
public class UPublicNumRepositoryImpl extends MongoRepository implements UPublicNumRepository{

	@Override
	public void addUpublicNum(UPublicNum upublicNum) {
		dsForRW.save(upublicNum);
	}

	@Override
	public List<UPublicNum> getPublicNumByUserId(Integer userId) {
		//filter("userId in", userId).filter("isAtt", 1);
		Query<UPublicNum> query = dsForRW.createQuery(UPublicNum.class);
		query.field("userId").equal(userId);
		query.field("isAtt").equal(1);
		return query.asList();
	}

	@Override
	public Object addAttention(UPublicNum uPub) {
		Key<UPublicNum> key = dsForRW.save(uPub);
		
		return key;
	}

	@Override
	public UPublicNum cancelAttention(Integer userId, Integer publicId) {
		Query<UPublicNum> q = dsForRW.createQuery(UPublicNum.class).field("userId").equal(userId).field("publicId").equal(publicId);
		UpdateOperations<UPublicNum> ops = dsForRW.createUpdateOperations(UPublicNum.class);
		ops.set("isAtt", 0);
		UPublicNum uPublicNum = dsForRW.findAndModify(q, ops);
		return uPublicNum;
	}

	@Override
	public UPublicNum getUpublicNum(Integer userId, Integer publicId) {
		Query<UPublicNum> query = dsForRW.createQuery(UPublicNum.class).field("userId")
				.equal(userId).field("publicId").equal(publicId).field("isAtt").equal(0);
		return query.get();
	}

	@Override
	public UpdateResults removeAtt(Integer publicId) {
		Query<UPublicNum> query = dsForRW.createQuery(UPublicNum.class).field("publicId").equal(publicId);
		UpdateOperations<UPublicNum> ops = dsForRW.createUpdateOperations(UPublicNum.class);
		ops.set("isAtt", 0);
		UpdateResults update = dsForRW.update(query, ops);
		return update;
	}

	@Override
	public boolean isAtt(Integer userId, String nickName) {
		Query<UPublicNum> query = dsForRW.createQuery(UPublicNum.class).field("userId")
				.equal(userId).field("publicName").equal(nickName).field("isAtt").equal(1);
		UPublicNum uPublicNum = query.get();
		if(null==uPublicNum){
			return false;
		}
		return true;
	}


}
