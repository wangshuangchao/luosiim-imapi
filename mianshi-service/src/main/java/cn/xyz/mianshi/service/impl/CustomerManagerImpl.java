package cn.xyz.mianshi.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.service.CustomerManager;
import cn.xyz.mianshi.vo.CommonText;
import cn.xyz.mianshi.vo.Customer;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.CompanyRepository;
import cn.xyz.repository.CustomerRepository;
import cn.xyz.repository.UserRepository;
import cn.xyz.service.KXMPPServiceImpl;

@Service
public class CustomerManagerImpl implements CustomerManager {

	
	@Resource
	private CompanyRepository companyRepository;
	
	@Resource
	private UserManagerImpl  userManager;
	
	@Resource
	private UserRepository userRepository;
	
	@Autowired
	private CustomerRepository customerRepository;
	
	@Override
	public Map<String, Object> registerUser(String companyId,String departmentId,String ip) {
		Map<String, Object> data = new HashMap<String, Object>();
		Integer customerId = 0;
		customerId = customerRepository.findUserByIp(ip);
		if (customerId!=null && customerId!=0){ //判断ip地址是否注册过
			try {
				//2、缓存用户认证数据到
				data = userRepository.saveAT(customerId, DigestUtils.md5Hex(ip));
				data.put("customerId", customerId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{ //没有注册
			
			// 生成userId
			customerId = userManager.createUserId();
			Customer customer = new Customer();
			customer.setCustomerId(customerId);
			customer.setIp(ip);
			customer.setCompanyId(companyId);
			// 新增客户
			data = customerRepository.addCustomer(customer);
		}	
		//分配客服号
		Integer serviceId =  allocation(companyId,departmentId);
		if (null != data) {
			data.put("serviceId", serviceId);
			try {
				//注册到tigsae
				KXMPPServiceImpl.getInstance().registerByThread(customerId.toString(), DigestUtils.md5Hex(customerId.toString()),0);
			} catch (Exception e){
				e.printStackTrace();
			}
			return data;
		}
		throw new ServiceException("用户注册失败");
	}
	
	
	
	//分配客服号
	public synchronized Integer  allocation(String companyId,String departmentId) {    
		ObjectId compId = new ObjectId(companyId);
		ObjectId departId = new ObjectId(departmentId);
		//到员工表中找到可分配状态的客服人员      map  key：userId  value:当前接待的客户数
		Map<Integer,Integer> map = customerRepository.findWaiter(compId, departId);
		
		int minValue = -1; //用于存放map中最小的value值
		int minKey = 0; //记录最小的value对应的key
		
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			if(userManager.getOnlinestateByUserId(entry.getKey())==0){ //在线状态，离线0  在线 1
				continue;
			}else{
				
				if(minValue == -1){ //首次将第一个value的值赋给maxValue
					minValue = entry.getValue();
					minKey = entry.getKey();
				}
				if(entry.getValue()==0){ //如果某个客服会话数为0，直接分配此客服
					minValue = entry.getValue();
					minKey = entry.getKey();
					break;
				}
				if(entry.getValue()<minValue){ //判断当前值是否小于上一个最小值
					minValue = entry.getValue();
					minKey = entry.getKey();
				}
				
			}	
			//System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
		}
		
		return minKey;
		
	}





	/**
	 * 添加常用语
	 */
	@Override
	public CommonText commonTextAdd(CommonText commonText) {
		if (!StringUtil.isEmpty(commonText.toString())) {
			customerRepository.commonTextAdd(commonText);
			return commonText;
		}else{
			throw new RuntimeException("添加常用语失败！");
		}
	}


	/**
	 * 删除常用语
	 */
	@Override
	public boolean deleteCommonTest(String commonTextId) {
		boolean a = customerRepository.deleteCommonText(commonTextId);
		if (true == a) {
			return true;
		}else {
			throw new RuntimeException("删除常用语失败！");
		}
	}

	/**
	 * 查询常用语
	 */
	@Override
	public List<CommonText> CommonTextGet(String companyId, int pageIndex, int pageSize) {
		List<CommonText> commonTextList = customerRepository.commonTextGet(companyId, pageIndex, pageSize);
		if (!StringUtil.isEmpty(commonTextList.toString())) {
			return commonTextList;
		}else {
			throw new RuntimeException("查询常用语失败！");
		}
	}

	/**
	 * 修改常用语
	 */
	@Override
	public CommonText commonTextModify(CommonText commonText) {
		if (null!=customerRepository.commonTextModify(commonText)) {
			return commonText;
		}else {
			throw new RuntimeException("修改常用语失败！");
		}
	}



	@Override
	public User getUser(String customerId) {
		
		return null;
	}
	

}