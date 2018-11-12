package com.shiku.mianshi.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import cn.xyz.commons.autoconfigure.KApplicationProperties.ALiSmsConfig;
import cn.xyz.commons.constants.KConstants;
import cn.xyz.commons.ex.ServiceException;
import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.commons.utils.ValidateCode;
import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.service.AdminManager;
import cn.xyz.mianshi.service.UserManager;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Config;
import cn.xyz.service.KSMSServiceImpl;
import cn.xyz.service.MsgServiceImpl;
import cn.xyz.sms.service.SmsService;

@RestController
public class BasicController {

	@Autowired
	private AdminManager adminManager;
	@Autowired
	private KSMSServiceImpl pushManager;
	@Autowired
	private MsgServiceImpl msgServiceImpl;//发送短信
	
	
	@Autowired
	private Map<String,SmsService> smsServiceMap;
	@Autowired
	private UserManager userManager;

	@Autowired
	private JedisTemplate jedisTemplate;
	
	@RequestMapping(value = "/getCurrentTime")
	public JSONMessage getCurrentTime() {
		return JSONMessage.success(null, cn.xyz.commons.utils.DateUtil.currentTimeSeconds());
	}
	@RequestMapping(value = "/config")
	public JSONMessage getConfig() {
		//Map<String, Object> map=new HashMap<String, Object>();
		Config config=adminManager.getConfig();
		config.setDistance(ConstantUtil.getAppDefDistance());
		return JSONMessage.success(null, config);
	}

	@RequestMapping(value = "/user/debug")
	public JSONMessage getUser(@RequestParam int userId) {
		return JSONMessage.success(null, userManager.getUser(userId));
	}
	@RequestMapping(value = "/getImgCode")
	public void getImgCode(HttpServletRequest request, HttpServletResponse response,@RequestParam(defaultValue="") String telephone) throws Exception {
		
		 // 设置响应的类型格式为图片格式  
        response.setContentType("image/jpeg");  
        //禁止图像缓存。  
        response.setHeader("Pragma", "no-cache");  
        response.setHeader("Cache-Control", "no-cache");  
        response.setDateHeader("Expires", 0); 
        HttpSession session = request.getSession();  
          
      
        ValidateCode vCode = new ValidateCode(140,50,4,0);  
        String key = String.format(KConstants.Key.IMGCODE, telephone.trim());
		jedisTemplate.set(key, vCode.getCode());
		jedisTemplate.expire(key, 600);
		
        session.setAttribute("code", vCode.getCode()); 
       // session.setMaxInactiveInterval(10*60);
        System.out.println("getImgCode telephone ===>"+telephone+" code "+vCode.getCode());
        vCode.write(response.getOutputStream());  
	}
	
	/**
	 * 
	 * <p>Title: sendSms</p>  
	 * <p>Description:发送短信验证码 </p>  
	 * @param telephone
	 * @param areaCode
	 * @param version
	 * @param imgCode
	 * @param language
	 * @param isRegister
	 * @return
	 */
	//@RequestMapping("/basic/randcode/sendSms")
	@RequestMapping("/basic/randcode/sendMsg")
	public JSONMessage sendSms(@RequestParam String telephone,@RequestParam(defaultValue="86") String areaCode,
			@RequestParam(defaultValue="0") int version,
			@RequestParam(defaultValue="") String imgCode,@RequestParam(defaultValue="zh") String language,
			@RequestParam(defaultValue="1") int isRegister) {
		Map<String, Object> params = new HashMap<String, Object>();
		telephone=areaCode+telephone;
		if(1==isRegister){
			if (userManager.isRegister(telephone)){
				params.put("code", "-1");
				return JSONMessage.failureByErrCode(KConstants.ResultCode.PhoneRegistered,language,params);
			}
		}
		if(1==version){
			if(StringUtil.isEmpty(imgCode)){
				return JSONMessage.failureByErrCode(KConstants.ResultCode.NullImgCode,language,params);
			}else{
				if(!pushManager.checkImgCode(telephone, imgCode)){
					String key = String.format(KConstants.Key.IMGCODE, telephone);
					String cached = jedisTemplate.get(key);
					System.out.println("ImgCodeError  getImgCode "+cached+"  imgCode "+imgCode);
					return JSONMessage.failureByErrCode(KConstants.ResultCode.ImgCodeError,language,params);
				}
			}
		}
		String code=null;
		try {
			
			code=pushManager.sendSmsToInternational(telephone, areaCode,language,code);
			//线程延时返回结果
			Thread.sleep(2000);
			params.put("code", code);
			System.out.println("code >>>  "+code);
			//return JSONMessage.success(null,params);
		} catch (ServiceException e) {
			params.put("code", "-1");
			if(null==e.getResultCode())
				return JSONMessage.failure(e.getMessage());
			return JSONMessage.failureByErr(e, language,params);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return JSONMessage.success(null,params);
	}

	
	
	//@RequestMapping("/basic/randcode/sendMsg")
	@RequestMapping("/basic/randcode/sendSms")
	public JSONMessage sendMsg(@RequestParam String telephone,@RequestParam(defaultValue="86") String areaCode,
			@RequestParam(defaultValue="0") int version,
			@RequestParam(defaultValue="") String imgCode,@RequestParam(defaultValue="zh") String language,
			@RequestParam(defaultValue="1") int isRegister) {
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, String> param = new HashMap<String, String>();
		telephone=areaCode+telephone;
		param.put("telephone", telephone);
		param.put("areaCode", areaCode);
		param.put("language", language);
		if(1==isRegister){
			if (userManager.isRegister(telephone)){
				params.put("code", "-1");
				return JSONMessage.failureByErrCode(KConstants.ResultCode.PhoneRegistered,language,params);
			}
		}
		if(1==version){
			if(StringUtil.isEmpty(imgCode)){
				return JSONMessage.failureByErrCode(KConstants.ResultCode.NullImgCode,language,params);
			}else{
				if(!pushManager.checkImgCode(telephone, imgCode)){
					String key = String.format(KConstants.Key.IMGCODE, telephone);
					String cached = jedisTemplate.get(key);
					System.out.println("ImgCodeError  getImgCode "+cached+"  imgCode "+imgCode);
					return JSONMessage.failureByErrCode(KConstants.ResultCode.ImgCodeError,language,params);
				}
			}
		}
		return msgServiceImpl.sendMsg(telephone, areaCode, language);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@RequestMapping(value = "/config/set", method = RequestMethod.GET)
	public ModelAndView setConfig() {
		//adminManager.getConfig();

		ModelAndView mav = new ModelAndView("config_set");
		mav.addObject("config", adminManager.getConfig());
		return mav;
	}

	@RequestMapping(value = "/config/set", method = RequestMethod.POST)
	public void setConfig(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Config config) throws Exception {
		/*BasicDBObject dbObj = new BasicDBObject();
		for (String key : request.getParameterMap().keySet())
			dbObj.put(key, request.getParameter(key));
		dbObj.put("XMPPHost", dbObj.get("XMPPDomain"));*/
		//
		
		config.XMPPHost=config.XMPPDomain;
		config.setMeetingHost(config.getFreeswitch());
		adminManager.setConfig(config);
		System.out.println(config.toString());
		response.sendRedirect("/config/set");
	}

	@RequestMapping(value = "/verify/telephone")
	public JSONMessage virifyTelephone(@RequestParam(defaultValue="86") String areaCode,@RequestParam(value = "telephone", required = true) String telephone) {
		telephone=areaCode+telephone;
		return userManager.isRegister(telephone) ? JSONMessage.failure("手机号已注册") : JSONMessage.success("手机号未注册");
	}
}
