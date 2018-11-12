package cn.xyz.service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import cn.xyz.commons.autoconfigure.KApplicationProperties.ALiSmsConfig;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.SmsRecord;
import cn.xyz.sms.service.DomesticSmsService;
import cn.xyz.sms.service.InternationalSmsService;
import cn.xyz.sms.service.SmsService;
import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class MsgServiceImpl implements ApplicationContextAware {

	private static ApplicationContext context;
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}
	public static MsgServiceImpl getInstance() {
		return context.getBean(MsgServiceImpl.class);
	}
	
	@Resource(name = "aLiSmsConfig")
	private ALiSmsConfig smsConfig;//判断阿里短信是否启用
	@Autowired
	private Map<String,SmsService> smsServiceMap;//获取短信发送的父接口
	
	@Autowired
	private Map<String,DomesticSmsService> domesticSmsServiceMap;//国内短信短信发送的父接口
	
	@Autowired
	private Map<String,InternationalSmsService> internationalSmsServiceMap;//国内短信短信发送的父接口
	
	@Autowired
	private JedisTemplate jedisTemplate;
	
	public boolean isAvailable(String telephone, String randcode) {
		String key = String.format(KConstants.Key.RANDCODE, telephone);
		String _randcode = jedisTemplate.get(key);
		return randcode.equals(_randcode);
	}
	public boolean checkImgCode(String telephone, String imgCode) {
		String key = String.format(KConstants.Key.IMGCODE, telephone);
		String cached = jedisTemplate.get(key);
		return imgCode.toUpperCase().equals(cached);
	}
	
	
	
	
	public JSONMessage sendMsg(String telephone, String areaCode,String language) {
		
		//发送短信的参数
		Map<String,String> params=new HashMap<String, String>();
		params.put("telephone", telephone);
		params.put("areaCode", areaCode);
		params.put("language", language);
		
		//回传结果参数,里面包含验证码
		Map<String, Object> p = new HashMap<String, Object>();
		//国内短信父接口
		DomesticSmsService dSmsServer;
		//国际短信父接口
		InternationalSmsService itSmsService;
		//调用短信发送后返回信息
		ResultInfo<String> result ;
		
		String code=null;//验证码
		//判断是否十分钟内是否发送过
		String key = String.format(KConstants.Key.RANDCODE, telephone);
		Long ttl = jedisTemplate.ttl(key);
		code=jedisTemplate.get(key);
		//如果code不为空则说明短时间内重复发送
		if(ttl>545){
			//可能发送恶意短信
			String msg=ConstantUtil.getMsgByCode(KConstants.ResultCode.ManySedMsg+"", language).getValue();
			 msg=MessageFormat.format(msg,ttl-535);
			System.out.println("======  "+msg);
			return JSONMessage.failureByErr(new ServiceException(msg), language,params);
		}
		if(null!=code){
			params.put("code", code);
			//调用天天短信发送
			itSmsService=internationalSmsServiceMap.get("tianSmsServiceImpl");
			result=itSmsService.sendInternationalMsg(params);
			//如果发送失败则返回报错信息
			if("1001".equals(result.getCode())){
				log.error("=============错误日志======="+"天天短信发送失败:参数"+params);
				return JSONMessage.failureByErr(new ServiceException(KConstants.ResultCode.SedMsgFail,language), language,params);
			}else{
				//将信息存入缓存
				jedisTemplate.set(key, code);
				jedisTemplate.expire(key, 600);
				p.put("code", code);
				return JSONMessage.success(null,params);
			}
		}
		if(null==code)
			code=StringUtil.randomCode();
			params.put("code", code);//参数中封装短信
		

		//校验是否国内短信
		if("86".equals(areaCode)){
			//判断是否启用阿里,如果未启用,则调用天天短信接口
			if(1!=smsConfig.getOpenALiSMS()){
				log.warn("=============警告======="+"阿里短信未启用,调用天天短信接口");
				itSmsService=internationalSmsServiceMap.get("tianSmsServiceImpl");
				result=itSmsService.sendInternationalMsg(params);
				//如果发送失败则返回报错信息
				if("1001".equals(result.getCode())){
					log.error("=============错误日志======="+"天天短信发送失败:参数"+params);
					return JSONMessage.failureByErr(new ServiceException(KConstants.ResultCode.SedMsgFail,language), language,params);
				}else{
					jedisTemplate.set(key, code);
					jedisTemplate.expire(key, 600);
					p.put("code", code);
					return JSONMessage.success(null,params);
				}
			}
			//如果启用国内则调用阿里短信接口
			dSmsServer=domesticSmsServiceMap.get("aLiSmsServiceImpl");
			result = dSmsServer.sendDomesticMsg(params);
			//是否发送成功,如果失败则调用另一个短信接口,现在调用天天国际短信
			if("1001".equals(result.getCode())){
				log.warn("=============警告======="+"阿里短信发送失败,调用天天短信接口");
				itSmsService=internationalSmsServiceMap.get("tianSmsServiceImpl");
				result=itSmsService.sendInternationalMsg(params);
				//如果发送失败则返回报错信息
				if("1001".equals(result.getCode())){
					log.error("=============错误日志======="+"天天短信发送失败:参数"+params);
					return JSONMessage.failureByErr(new ServiceException(KConstants.ResultCode.SedMsgFail,language), language,params);
				}
			}else{
				jedisTemplate.set(key, code);
				jedisTemplate.expire(key, 600);
				p.put("code", code);
				return JSONMessage.success(null,params);
			}
			//如果是国际短信,则调用天天短信接口
		}else{
			itSmsService=internationalSmsServiceMap.get("tianSmsServiceImpl");
			result=itSmsService.sendInternationalMsg(params);
			//如果发送失败则返回报错信息
			if("1001".equals(result.getCode())){
				return JSONMessage.failureByErr(new ServiceException(KConstants.ResultCode.SedMsgFail,language), language,params);
			}else{
				jedisTemplate.set(key, code);
				jedisTemplate.expire(key, 600);
				p.put("code", code);
				return JSONMessage.success(null,params);
			}
		}
		
		return null;
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
