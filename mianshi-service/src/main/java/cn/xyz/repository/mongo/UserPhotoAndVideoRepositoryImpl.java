package cn.xyz.repository.mongo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.mongodb.morphia.query.CriteriaContainer;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.xyz.mianshi.service.impl.MongoRepository;
import cn.xyz.mianshi.vo.UserPhotoAndVideo;
import cn.xyz.repository.MsgRepository;
import cn.xyz.repository.UserPhotoAndVideoRepository;
@Service
public class UserPhotoAndVideoRepositoryImpl extends MongoRepository implements UserPhotoAndVideoRepository {

	@Autowired
	private MsgRepository msgRepository;

	public List<String> getUserPohots(Integer userId) {
		List<String> result=new ArrayList<>();
		Query<UserPhotoAndVideo> query = dsForRW.createQuery(UserPhotoAndVideo.class).field("uesrId").equal(userId);
		query.order("-_id").limit(4);
		List<UserPhotoAndVideo> list = query.asList();
		list.forEach(funs -> {
			result.add(funs.getUrl());
		});
		return  result;
	}

	@Override
	public void addUserPhotos(UserPhotoAndVideo upav) {
		dsForRW.save(upav);
	}
}
