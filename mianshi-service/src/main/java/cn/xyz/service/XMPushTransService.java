package cn.xyz.service;



import org.springframework.util.StringUtils;

import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Message.Builder;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;

import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.MsgNotice;

//小米推送集成
public class XMPushTransService {
	
 public static Sender sender=new Sender("shQBqQFN/rim0OTxEDBDxg==");//申请到的AppSecret
  
  public final static String PACKAGE_NAME="com.luosi.letschat";//申请的包名
  
  public static void pushToRegId(MsgNotice notice,String callNum){
	  if(StringUtils.isEmpty(notice.getText()))
		  notice.setText("收到一条消息...");
	String messagePayload= notice.getText();
	  String title =notice.getTitle(); 
	 String description =notice.getText();
	 if(1==notice.getIsGroup()){
		 description=notice.getText().replace(notice.getGroupName(), "");
		 title=notice.getTitle();
	 }
	   
	    Message message=null;
	    Builder builder = new Message.Builder()
	                .title(title)
	                .description(description).payload(messagePayload)
	                .restrictedPackageName(PACKAGE_NAME)
	               .extra(Constants.EXTRA_PARAM_NOTIFY_EFFECT, Constants.NOTIFY_LAUNCHER_ACTIVITY);
	                //.extra(Constants.EXTRA_PARAM_INTENT_URI, "intent:#Intent;component=com.xiaomi.mipushdemo/.NewsActivity;end")
	    
	    	//自定义参数 
	    	builder.extra("from",notice.getFrom()+"");
	    	builder.extra("fromUserName",notice.getName()+"");
	    	builder.extra("messageType", notice.getType()+"");
	    	builder.extra("to", notice.getTo()+"");
	    	if(120==notice.getType()||115==notice.getType()){
	    		builder.extra("callNum", callNum+"");
	    		 builder.passThrough(1);
	    	}
	    	if(100==notice.getType()||110==notice.getType())	
	           builder.passThrough(1);
	         
	         message = builder.notifyType(1)     // 使用默认提示音提示
	                .build();
	    try {
	    	String regId=KSessionUtil.getXMPushRegId(notice.getTo());
	    	if(StringUtil.isEmpty(regId))
	    		return;
			Result result = sender.send(message, regId, 3);
			System.out.println(result.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	  
  }
  
  /*private String getRegId(Integer userId){
	  
  }*/

}
