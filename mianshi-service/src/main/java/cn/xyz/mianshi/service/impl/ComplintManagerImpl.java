package cn.xyz.mianshi.service.impl;


import javax.annotation.Resource;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.service.ComplintManager;
import cn.xyz.mianshi.vo.Complint;

@Service(ComplintManagerImpl.BEAN_ID)
public class ComplintManagerImpl extends MongoRepository<Complint, ObjectId> implements ComplintManager {
	public static final String BEAN_ID = "ComplintManagerImpl";

	@Resource(name = "dsForTigase")
	private Datastore dsForTigase;
	@Resource(name = "dsForRoom")
	protected Datastore dsForRoom;
	
	
	@Override
	public ResultInfo<String> addComplint(Complint complint) {
		DBCollection collection = dsForRW.getDB().getCollection("complint");
		BasicDBObject jo = new BasicDBObject();
		if(null!=complint.getUserId()){
			jo.put("userId", complint.getUserId());
		}
		if(!StringUtil.isEmpty(complint.getLsId())){
			jo.put("lsId", complint.getLsId());
		}
		if(!StringUtil.isEmpty(complint.getNickname())){
			jo.put("nickname", complint.getNickname());
		}
		if(!StringUtil.isEmpty(complint.getTitle())){
			jo.put("title", complint.getTitle());
		}
		if(!StringUtil.isEmpty(complint.getContent())){
			jo.put("content", complint.getContent());
		}
		jo.put("createTime", DateUtil.currentTimeSeconds());
		jo.put("isHandle", 0);
		WriteResult save = collection.save(jo);
		ResultInfo<String> result=new ResultInfo<>();
		if(null!=save){
			result.setCode("1000");
			result.setData(save.toString());
			result.setMsg("反馈成功");
			return result;
		}else{
			result.setCode("1001");
			result.setData("投诉失败");
			result.setMsg("反馈失败");
			return result;
		}
	}
	

	

}
