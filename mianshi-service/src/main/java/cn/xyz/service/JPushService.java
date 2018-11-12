package cn.xyz.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import lombok.extern.slf4j.Slf4j;

/**
 * 
* <p>Title: JPushService</p>  
* <p>Description: 极光推送</p>  
* @author xiaobai  
* @date 2018年5月30日
 */
@Slf4j
@Component
public class JPushService {

	@Value("${im.JPush.appKey}")
	public String appKey = "a75a341cfc7dd95d855faa6b";
	@Value("${im.JPush.masterSecret}")
	public String masterSecret = "0f54499be19ecea233c611d1";
	
	public JPushClient jpushClient;
	
	@PostConstruct
	public void init(){
		jpushClient = new JPushClient(masterSecret, appKey);
	}

	public String send(String regId,String msg){
		 //PushPayload payload = buildPushObject_ios_tagAnd_alertWithExtrasAndMessage();
		 //PushPayload payload = PushPayload.alertAll(ALERT);
		 PushPayload payload = buildPushObject_android_ios_reg_alert(regId,msg);
		  try {
		        PushResult result = jpushClient.sendPush(payload);
		        log.info("==================="+result);
		        log.info("Got result - " + result);
		        return result.toString();

		    } catch (APIConnectionException e) {
		        // Connection error, should retry later
		    	log.error("--错-误-日-志----"+"Connection error, should retry later", e);
		    	return "出错了";

		    } catch (APIRequestException e) {
		        // Should review the error, and fix the request
		    	log.error("--错-误-日-志----"+"Should review the error, and fix the request", e);
		    	log.info("HTTP Status: " + e.getStatus());
		    	log.info("Error Code: " + e.getErrorCode());
		    	log.info("Error Message: " + e.getErrorMessage());
		    	return "出错了";
		    }
	}
	
	/**
	* 通过regID推送给个人
	* 生成极光推送对象PushPayload（采用java SDK）
	* @param regId
	* @param msg
	* @return PushPayload
	*/
	public PushPayload buildPushObject_android_ios_reg_alert(String regId, String msg) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.registrationId(regId))
				.setNotification(Notification.newBuilder()
				.addPlatformNotification(
								AndroidNotification.newBuilder()
//								.addExtra("type", "infomation")
								.setAlert(msg).build()) 
								.addPlatformNotification(
								IosNotification.newBuilder()
//								.addExtra("type", "infomation")
								.setAlert(msg)
								.autoBadge()
								.build())
								.build() )
				.setOptions(Options.newBuilder().setApnsProduction(true)// true-推送生产环境 false-推送开发环境（测试使用参数）
//								.setTimeToLive(90)// 消息在JPush服务器的失效时间（测试使用参数）
								.build())
								.build();
	}
	
	public static void main(String[] args) {
		JPushService jpush = new JPushService();
		jpush.appKey = "a75a341cfc7dd95d855faa6b";
		jpush.masterSecret = "0f54499be19ecea233c611d1";
		jpush.init();
		jpush.send("140fe1da9efbc040a28", "线下测试");
	}
}
