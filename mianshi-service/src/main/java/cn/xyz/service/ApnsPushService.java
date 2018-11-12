package cn.xyz.service;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;


import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

import com.notnoop.apns.PayloadBuilder;

import cn.xyz.commons.autoconfigure.KApplicationProperties.AppConfig;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.MsgNotice;

@Component
public class ApnsPushService implements ApplicationContextAware{
	
	@Resource(name = "appConfig")
	private  AppConfig appConfig;
	
	private static ApplicationContext context;
	
	
	private  ApnsService service;
	
	public static ApnsPushService getInstance() {
		return context.getBean(ApnsPushService.class);
	}
	
	private ApnsService getService(){
		if (null==service){
			service = APNS.newService()
				     .withCert(appConfig.getApns(), "123456")
				     .withProductionDestination()
				     .build();
		}
		return service;
	}
	
	public  void pushMsgToUser(MsgNotice notice,String  callNum){
		
		String payload=null;
		
		
		 payload = APNS.newPayload().alertBody(notice.getText()+"").build();
		 String token =KSessionUtil.getAPNSToken(notice.getTo());
		 
		 
		 /*String payload = APNS.newPayload()
		            .badge(3)
		            .customField("secret", "what do you think?")
		            .localizedKey("GAME_PLAY_REQUEST_FORMAT")
		            .localizedArguments("Jenna", "Frank")
		            .actionKey("Play").build();*/
		 
		  PayloadBuilder badge = APNS.newPayload().badge(3);
		  
		 // badge.customField("customField", "customField")
		  
		  badge.customField("from",notice.getFrom()+"");
		  badge.customField("fromUserName",notice.getName());
		  badge.customField("messageType", notice.getType()+"");
		  badge.customField("to", notice.getTo()+"");
		  if(120==notice.getType()||115==notice.getType()){
			  badge.customField("callNum", callNum);
		  }
	    
		  	/*
		            .localizedKey("localizedKey")
		            .alertBody("alertBody")
		            .localizedArguments("localizedArguments", "localizedArguments")*/
		  
		  payload =badge.actionKey("actionKey").build();
		           

		 //int now =  (int)(new Date().getTime()/1000);

		/* EnhancedApnsNotification notification = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID()  Next ID ,
		     now + 60 * 60  Expire in one hour ,
		     token  Device Token ,
		     payload);*/

		 getService().push(token, payload);
		
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context=applicationContext;
		
	}

}
