package com.shiku.mianshi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.JSONUtil;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.service.impl.PublicNumManagerImpl;
import cn.xyz.mianshi.service.impl.UserManagerImpl;
import cn.xyz.mianshi.utils.SmsUtil;
import cn.xyz.mianshi.vo.Msg.Resource;
import cn.xyz.mianshi.vo.PublicNum;
import cn.xyz.mianshi.vo.PublicNumDvo;
import cn.xyz.mianshi.vo.UPublicNum;
import cn.xyz.mianshi.vo.User;
import cn.xyz.repository.mongo.MsgCommentRepositoryImpl;
import cn.xyz.repository.mongo.PublicNumRepositoryImpl;
import cn.xyz.repository.mongo.UPublicNumRepositoryImpl;
import cn.xyz.sms.service.DomesticSmsService;
import cn.xyz.sms.service.InternationalSmsService;
import cn.xyz.service.KXMPPServiceImpl;


@RunWith(SpringJUnit4ClassRunner.class)  
@SpringBootTest(classes=Application.class)// 指定spring-boot的启动类  
public class ApplicationTest {

	@Autowired
	private PublicNumManagerImpl publicNum;
	@Autowired
	private UserManagerImpl userManager;
	@Autowired
	UPublicNumRepositoryImpl uPubImpl;
	@Autowired
	PublicNumRepositoryImpl pubImpl;
	
	@Autowired
	KXMPPServiceImpl im;
	@Test
	public void test() {
		Integer id = publicNum.createPublicId();
		System.out.println("-------"+id);
	}
	@Test
	public void pubtest() {
		ResultInfo<String> addAttention = publicNum.addAttention(10000037, 20000005);
		System.out.println(addAttention);
	}
	
	@Test
	public void upubtest() {
		List<UPublicNum> list = uPubImpl.getPublicNumByUserId(10000036);
		System.out.println(list+"-----------");
	}
	@Test
	public void getByName() {
		ResultInfo<PublicNumDvo> pub = publicNum.getByName(1000035,"木瓜瓜");
		System.out.println(pub+"-----------");
	}
	@Test
	public void getService() {
		ResultInfo<Integer> resultInfo = publicNum.getServiceId(20000005);
		System.out.println(resultInfo);
	}
	
	@Test
	public void getPubForCs() {
		List<PublicNum> list = pubImpl.getPublcNumListForCS(10000037);
		for (PublicNum publicNum : list) {
			System.out.println(publicNum);
			
		}
	}
	@Test
	public void calsAtt() {
		UPublicNum upublicNum = uPubImpl.getUpublicNum(10000037, 20000006);
		System.out.println("=========="+upublicNum);
//		UPublicNum cancelAttention = uPubImpl.cancelAttention(10000036, 20000005);
//		System.out.println(cancelAttention);
	}
	@Test
	public void del(){
		UpdateResults deletePublicNum = pubImpl.deletePublicNum(20000005);
		
		System.out.println("-----------"+deletePublicNum.getUpdatedCount());
/*		pubImpl.removePublicNum(20000006, 10000037);
		System.out.println("-----------");
*/		
	}
	@Test
	public void jsonTest(){
		String body="{&quot;fromUserName&quot;:&quot;测试九&quot;,&quot;fromUserId&quot;:&quot;10000049&quot;,&quot;content&quot;:&quot;测试&quot;,&quot;messageId&quot;:&quot;26ff405077c84edd802079436fdf9957&quot;,&quot;type&quot;:1,&quot;timeSend&quot;:1526711641}";
		body = body.replaceAll("=&quot;", "\"").replaceAll("&quot;", "\"");
		System.out.println(body);
		JSONObject bodyObj = null;
	    bodyObj = JSON.parseObject(body);
	    System.out.println(bodyObj);
	}
	
	@Test
	public void upTest(){
		PublicNum num = pubImpl.getPublicNum(20000001);
		UPublicNum uPublicNum=new UPublicNum();
		uPublicNum.setIsAtt(1);
		uPublicNum.setPortraitUrl(num.getPortraitUrl());
		uPublicNum.setPublicId(num.getPublicId());
		uPublicNum.setPublicName(num.getNickname());
		uPublicNum.setTime(DateUtil.currentTimeSeconds());
		uPublicNum.setUserId(10000053);
		uPubImpl.addUpublicNum(uPublicNum);
	}
	
	@Autowired
	private SmsUtil smsUtil;
	@Autowired
	private Map<String,DomesticSmsService> domesticSmsServiceMap;//国内短信短信发送的父接口
	
	@Autowired
	private Map<String,InternationalSmsService> internationalSmsServiceMap;//国内短信短信发送的父接口
	
//	@Test
//	public void smsTest() throws ClientException{
//		SendSmsResponse response = sms.sendMsgByAL("14730311314", "515133");
//		String bizId = response.getBizId();
//		String requestId = response.getRequestId();
//		String message = response.getMessage();
//		String code = response.getCode();
//		System.out.println("bizId"+bizId);
//		System.out.println("requestId"+requestId);
//		System.out.println("message"+message);
//		System.out.println("code"+code);
//	}
	
	@Test
	public void send(){
		//发送短信的参数
//		Map<String,String> params=new HashMap<String, String>();
//		params.put("telephone", "15933314079");
//		params.put("code", "111111");
//		DomesticSmsService dSmsServer=domesticSmsServiceMap.get("aLiSmsServiceImpl");
//		ResultInfo<String> result= dSmsServer.sendDomesticMsg(params);
//		System.out.println(result);
	}
	
	@Test
	public void send1(){
		//发送短信的参数
//		Map<String,String> params=new HashMap<String, String>();
//		params.put("telephone", "14730311314");
//		params.put("code", "111111");
//		params.put("areaCode", "86");
//		InternationalSmsService itSmsServer=internationalSmsServiceMap.get("tianSmsServiceImpl");
//		ResultInfo<String> result= itSmsServer.sendInternationalMsg(params);
//		System.out.println(result);
	}
	@Test
	public void json(){
		List<Resource> list=new ArrayList<>();
		Resource re=new Resource();
		re.setOUrl("www.baidu.com");
		Resource res=new Resource();
		res.setOUrl("www.taobao.com");
		list.add(re);
		list.add(res);
		//[{"length":0,"oUrl":"www.baidu.com","size":0},{"length":0,"oUrl":"www.taobao.com","size":0}]

		String jsonString = JSONUtil.toJSONString(list);
		System.out.println(jsonString);
	}
	@Test
	public void jdk(){
		List<String> list=new ArrayList<>();
		list.add("小白");
		list.add("小红");
		list.add("小兰");
		list.add("小绿");
		list.add("小黄");
		list.add("小紫");
		list.forEach(i -> {
			System.out.println(i);
		});
	}
	
	@Autowired
	private MsgCommentRepositoryImpl imr;
	@Test
	public void msgTe(){
		List<Integer> praiseuserIdlist=new ArrayList<Integer>();
		DBObject d=new BasicDBObject("msgId","5b7a9102bd59b0429cf56183");
		praiseuserIdlist=imr.distinct("s_praise", "userId", d);
		List<Integer> userIdlist=new ArrayList<Integer>();
		userIdlist=imr.distinct("s_comment","userId", d);
		List<Integer> toUserIdlist=imr.distinct("s_comment","toUserId", d);
		
	}
}
