package cn.xyz.sms.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import cn.xyz.commons.autoconfigure.KApplicationProperties.SmsConfig;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.HttpClientUtil;
import cn.xyz.commons.utils.WebNetEncode;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.SmsRecord;
import cn.xyz.sms.service.InternationalSmsService;
import lombok.extern.slf4j.Slf4j;
@Service("tianSmsServiceImpl")
@Slf4j
public class TianSmsServiceImpl implements InternationalSmsService, ApplicationContextAware{

	private static ApplicationContext context;

	@Resource(name="smsConfig")
	private SmsConfig smsConfig;
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	public static TianSmsServiceImpl getInstance() {
		return context.getBean(TianSmsServiceImpl.class);
	}
	@Override
	public ResultInfo<String> sendMsg(Map<String, String> params) {
		return null;
	}
	
	@Override
	public ResultInfo<String> sendInternationalMsg(Map<String, String> params) {
		ResultInfo<String> result=new ResultInfo<>();
		String telephone=params.get("telephone");
		String areaCode=params.get("areaCode");
		String code=params.get("code");
		String ip = smsConfig.getHost();
        int port = smsConfig.getPort();
        int openSMS = smsConfig.getOpenSMS();
        if(1!=openSMS){
        	log.warn("==============警告==========="+"天天短信未启用");
        	result.setCode("1001");
        	result.setData("");
        	result.setMsg("天天短信未启用");
        	return result;
        }
        // HTTP 请求工具
        //"/mt/MT3.ashx"
        HttpClientUtil util = new HttpClientUtil(ip,  port,smsConfig.getApi());
        String user = smsConfig.getUsername();//你的用户名 
        String pwd = smsConfig.getPassword();//你的密码：
        String ServiceID = "SEND"; //固定，不需要改变
        String dest = telephone; // 你的目的号码【收短信的电话号码】
        String sender = "";// 你的原号码,可空【大部分国家原号码带不过去，只有少数国家支持透传，所有一般为空】
        String msg ="[OLE IM], Your verification code is:"+code;//你的短信内容
        if("86".equals(areaCode)||"886".equals(areaCode)||"852".equals(areaCode))
        	msg = "[哦了],您的验证码为:"+code+"请妥善保管,切勿外泄";
        // codec=8 Unicode 编码,  3 ISO-8859-1, 0 ASCII
        // 短信内容 HEX 编码，8 为 UTF-16BE HEX 编码， dataCoding = 8 ,支持所有国家的语言，建议直接使用 8
        String hex = WebNetEncode.encodeHexStr(8, msg); 
        hex = hex.trim() + "&codec=8";
        //System.out.println("POST MT3");
        // HTTP 封包请求, util.sendPostMessage  返回结果，
        // 如果是以 “-” 开头的为发送失败，请查看错误代码，否则为MSGID
        String msgId=util.sendGetMessage(user, pwd, ServiceID, dest, sender, hex);
        if(msgId.startsWith("-")){
        	result.setCode("1001");
        	result.setData(msgId);
        	result.setMsg("发送失败");
        	return result;
        }else{
        	//存入数据库
        	System.out.println("msgid =  "+msgId);
	        //发送短信记录保存到数据库
	        saveSMSToDB(telephone, areaCode, code, msg, msgId);
        	result.setCode("1000");
        	result.setData(msgId);
        	result.setMsg("发送成功");
        	return result;
        }
	}
	

	private void saveSMSToDB(String telephone,String areaCode,String code,String content,String msgId){
		SmsRecord sms=ConstantUtil.getSmsPrice(areaCode);
		if(null==sms)
			sms=new SmsRecord();
		sms.setAreaCode(areaCode);
		sms.setTelephone(telephone);
		sms.setCode(code);
		sms.setContent(content);
		sms.setMsgId(msgId);
		sms.setTime(DateUtil.currentTimeSeconds());
		ConstantUtil.dsForRW.save(sms);
		
	}
}
