package com.shiku.mianshi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.service.PublicNumManager;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.PublicNumDvo;

/*
 * 公众号相关接口
 */
@RestController
@RequestMapping("/publicNum")
public class PublicNumController {

	/*@Autowired
	private UserManager userManager;*/
	@Autowired
	private PublicNumManager publicManager;

	/**
	 * 创建新公众号
	 * @param num
	 * @return
	 */
	@PostMapping("/addPublicNum")
	public ResultInfo<Integer> addPublicNum(PublicNum num) {
		PublicNum publicNum = new PublicNum();
		publicNum.setCsUserId(10000038);
		publicNum.setNickname("木瓜瓜");
		publicNum.setMessage("欢迎来到木瓜公众号");
		ResultInfo<Integer> result = publicManager.addPublicNum(publicNum);
		return result;
	}
	/**
	 * 
	 * <p>Title: removePublicNum</p>  
	 * <p>Description:根据公众号ID和客服id 移除绑定公众号的客服 </p>  
	 * @param publicId
	 * @param userId
	 * @return
	 */
	@PostMapping("/removePublicNum")
	public ResultInfo<String> removePublicNum(Integer publicId,Integer csUserId) {
		ResultInfo<String> result=publicManager.removePublicNum(publicId,csUserId);
		return result;
	}
	/**
	 * 
	 * <p>Title: deletePublicNum</p>  
	 * <p>Description: 根据公众号id注销公众号</p>  
	 * @param publicId
	 * @return
	 */
	@PostMapping("/deletePublicNum")
	public ResultInfo<String> deletePublicNum(Integer publicId) {
		ResultInfo<String> result=publicManager.deletePublicNum(publicId);
		return result;
	}

	/**
	 * 测试用
	 * <p>Title: 创建或添加公众号</p>  
	 * <p>Description: </p>  
	 * @return
	 */
	@GetMapping("/add")
	public String add() {
		PublicNum publicNum = new PublicNum();
		//publicNum.setPublicId(20000005);
		publicNum.setCsUserId(10000044);
		publicNum.setNickname("咔芒科技");
		publicNum.setMessage("欢迎来到咔芒的公众号");
		publicNum.setCreateTime(DateUtil.currentTimeSeconds());
		publicNum.setIndexUrl("https://www.ikamang.com");
		publicNum.setIndexUrlTital("咔芒科技");
		publicNum.setIntroduce("乐城商务1204");
		publicNum.setMessageUrl("https://www.ikamang.com");
		publicNum.setPortraitUrl("https://img2.woyaogexing.com/2018/05/09/dc16678d90ba4e79!400x400_big.jpg");
		publicNum.setIsDel(0);
		publicNum.setUpdateTime(DateUtil.currentTimeSeconds());
		publicManager.addPublicNum(publicNum);
		return "插入成功";
	}

	/**
	 * 获取公众号详情
	 * @param publicId
	 * @return
	 */
	@GetMapping("/getDetail")
	public ResultInfo<PublicNum> getDetail(@RequestParam(defaultValue = "") Integer publicId) {
		ResultInfo<PublicNum> result = publicManager.getDetail(publicId);
		return result;
	}

	/**
	 * 获取用户已经关注的公众号列表
	 * @param userId
	 * @return
	 */
	/*@GetMapping("/getAttentionList")
	public ResultInfo<UpublicDto> getAttentionList(@RequestParam(defaultValue = "") Integer userId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<UpublicDto> result = publicManager.getAttentionList(userId);
		return result;
	}
	*/
	/**
	 * 公众号名称精确查找
	 * @param nickname
	 * @return
	 */
	@GetMapping("/getByName")
	public ResultInfo<PublicNumDvo> getByName(@RequestParam(defaultValue = "") String nickname,Integer userId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<PublicNumDvo> result = publicManager.getByName(userId,nickname);
		return result;
	}
	
	/**
	 * 获取用户已经绑定客服的公众号
	 * 此方法为申请了公众号的用户使用
	 * @param csUserId
	 * @return
	 */
	/*@GetMapping("/getPublcNumListForCS")
	public ResultInfo<PublicNumVo> getPublcNumListForCS(@RequestParam(defaultValue = "") Integer csUserId) {
		ResultInfo<PublicNumVo> result = publicManager.getPublicNumListForCS(csUserId);
		return result;
	}*/
	
	/**
	 * 用户获取公众号的客服ID
	 * @param publicId
	 * @return
	 */
	@GetMapping("/getServiceId")
	public ResultInfo<Integer> getServiceId(@RequestParam(defaultValue = "") Integer publicId) {
		ResultInfo<Integer> result = publicManager.getServiceId(publicId);
		return result;
	}

	/**
	 * 关注公众号
	 * 关注指定的公众号
	 * @param userId
	 * @param publicId
	 * @return
	 */
	@PostMapping("/addAttention")
	public ResultInfo<String> addAttention(@RequestParam(defaultValue = "") Integer userId, Integer publicId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<String> result = publicManager.addAttention(userId, publicId);
		return result;
	}
	
	/**
	 * 
	 * <p>Title: 取消关注</p>  
	 * <p>Description:根据公众号id和用户id进行取消 </p>  
	 * @param userId
	 * @param publicId
	 * @return
	 */
	@PostMapping("/cancelAttention")
	public ResultInfo<String> cancelAttention(@RequestParam(defaultValue = "") Integer userId, Integer publicId) {
		userId = (null == userId ? ReqUtil.getUserId() : userId);
		ResultInfo<String> result = publicManager.cancelAttention(userId, publicId);
		return result;
	}
	
	
}
