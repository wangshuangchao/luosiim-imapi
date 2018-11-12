package cn.xyz.repository.mongo;





import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ValueUtil;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.CompanyVO;
import cn.xyz.mianshi.vo.Customer;
import cn.xyz.mianshi.vo.Employee;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.CustomerRepository;
import cn.xyz.repository.UserRepository;

/**
 * 客服模块数据操纵接口的实现
 * @author hsg
 *
 */

@Service
public class CustomerRepositoryImpl extends BaseRepositoryImpl<CompanyVO, ObjectId> implements CustomerRepository {
	
	public static CustomerRepositoryImpl getInstance(){
		return new CustomerRepositoryImpl();
	}
	

	@Resource
	private UserManagerImpl  userManager;
	
	@Resource
	private UserRepository userRepository;
	
	@Override
	public Map<String, Object> addCustomer(Customer customer) {
		BasicDBObject jo = new BasicDBObject();
		jo.put("customerId", customer.getCustomerId());// 索引
		jo.put("userKey", DigestUtils.md5Hex(customer.getIp()));
		jo.put("ip",customer.getIp());// 索引
		jo.put("macAddress","");
		jo.put("createTime", DateUtil.currentTimeSeconds());
		jo.put("companyId", customer.getCompanyId());
		
		// 1、新增客户记录
		dsForRW.getDB().getCollection("customer").save(jo);
		
		try {
			//2、缓存用户认证数据到
			Map<String, Object> data = userRepository.saveAT(customer.getCustomerId(), jo.getString("userKey"));
			data.put("customerId", customer.getCustomerId());
			//data.put("nickname",jo.getString("nickname"));
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	
	@Override
	public Integer findUserByIp(String ip) {
		
		Query<Customer> query = dsForRW.createQuery(Customer.class);
		if (!StringUtil.isEmpty(ip)){
			query.field("userKey").equal(DigestUtils.md5Hex(ip));
		}
		
		if(query.get()!=null){
			return query.get().getCustomerId();
		}else{
			return null;
		}
				
	}
	
	/**
	 * 获取处于可分配态的客服人员
	 */
	@Override
	public Map<Integer,Integer> findWaiter(ObjectId companyId,ObjectId departmentId){
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		Query<Employee> query = dsForRW.createQuery(Employee.class).field("companyId").equal(companyId)
				.field("departmentId").equal(departmentId).field("isPause").equal(1);
		if(query == null)
			return null;
		List<Employee> emps= query.asList();
		
		for(Iterator<Employee> iter = emps.iterator(); iter.hasNext(); ){
			Employee emp = iter.next();	
			map.put(emp.getUserId(), emp.getChatNum());
		}
		return map;
	}
		
	/**
	 * 添加常用语
	 */
	@Override
	public CommonText commonTextAdd(CommonText commonText) {
		commonText.setCreateTime(DateUtil.currentTimeSeconds());//创建时间
		commonText.setCreateUserId(ReqUtil.getUserId());//创建人
		commonText.setModifyUserId(ReqUtil.getUserId());//修改人
		dsForRW.save(commonText);
		return commonText;
	}
	
	/**
	 * 删除常用语
	 */
	@Override
	public boolean deleteCommonText(String commonTextId) {
		ObjectId commonTextIds = new ObjectId(commonTextId);	
		Query<CommonText> query = dsForRW.createQuery(CommonText.class).filter("_id", commonTextIds);
		//CommonText commonText = query.get();获取查询出的对象
		dsForRW.delete(query);
		return true;
	}
	
	/**
	 * 查询常用语
	 */
	@Override
	public List<CommonText> commonTextGet(String companyId, int pageIndex, int pageSize) {
		ObjectId companyIds = new ObjectId(companyId);	
		Query<CommonText> query = dsForRW.createQuery(CommonText.class);
		query.filter("companyId", companyIds);
		//根据创建时间倒叙
		List<CommonText> commonTextList = query.offset(pageIndex * pageSize).limit(pageSize).order("-createTime").asList();
		return commonTextList;
	}
	
	/**
	 * 修改常用语
	 */
	@Override
	public CommonText commonTextModify(CommonText commonText) {
		if (!StringUtil.isEmpty(commonText.getId().toString())) {
			//根据常用语id来查询出数据
			Query<CommonText> query = dsForRW.createQuery(CommonText.class).field("_id").equal(commonText.getId());
			//修改
			UpdateOperations<CommonText> uo = dsForRW.createUpdateOperations(CommonText.class);
			//赋值
			if (null!=commonText.getContent()) {
				uo.set("content", commonText.getContent());
			}
			uo.set("modifyUserId", ReqUtil.getUserId());
			uo.set("createTime", DateUtil.currentTimeSeconds());
			commonText = dsForRW.findAndModify(query, uo);
		}
		return commonText;
		
	}

}