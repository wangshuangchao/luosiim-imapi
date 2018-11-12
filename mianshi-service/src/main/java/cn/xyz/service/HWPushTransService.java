package cn.xyz.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;


import org.json.simple.JSONArray;

import com.alibaba.fastjson.JSONObject;

import cn.xyz.mianshi.utils.KSessionUtil;
import cn.xyz.mianshi.vo.MsgNotice;

import org.apache.commons.io.IOUtils;
//华为透传消息推送
public class HWPushTransService {
	 private static String appSecret = "7dd3162b2ec4d39a30cc90af92bb97e8";
	    private static  String appId = "100141987";//用户在华为开发者联盟申请的appId和appSecret（会员中心->我的产品，点击产品对应的Push服务，点击“移动应用详情”获取）
	    private static  String tokenUrl = "https://login.cloud.huawei.com/oauth2/v2/token";//获取认证Token的URL
	    private static  String apiUrl = "https://api.push.hicloud.com/pushsend.do"; //应用级消息下发API
	    private static  String accessToken;//下发通知消息的认证Token
	    private static  long tokenExpiredTime;  //accessToken的过期时间
	    
	   /* public static void main(String[] args) throws IOException
	    {
	        sendPushMessage("0865217039424357300001122700CN01");
	    }*/
	    
	    //获取下发通知消息的认证Token
	    private static  void refreshToken() throws IOException
	    {
	        String msgBody = MessageFormat.format(
	        "grant_type=client_credentials&client_secret={0}&client_id={1}", 
	        URLEncoder.encode(appSecret, "UTF-8"), appId);
	        String response = httpPost(tokenUrl, msgBody, 5000, 5000);
	        JSONObject obj = JSONObject.parseObject(response);
	        accessToken = obj.getString("access_token");
	        tokenExpiredTime = System.currentTimeMillis() + obj.getLong("expires_in")  - 5*60*1000;
	    }
	    
	    //发送Push消息
	    public static  void sendPushMessage(MsgNotice notice,String callNum) throws IOException
	    {
	        if (tokenExpiredTime <= System.currentTimeMillis())
	        {
	            refreshToken();
	        }
	        String Token=KSessionUtil.getHWPushToken(notice.getTo());
	        /*PushManager.requestToken为客户端申请token的方法，可以调用多次以防止申请token失败*/
	        /*PushToken不支持手动编写，需使用客户端的onToken方法获取*/
	        JSONArray deviceTokens = new JSONArray();//目标设备Token       
	        deviceTokens.add(Token);
	        /*deviceTokens.add("22345678901234561234567890123456");
	        deviceTokens.add("32345678901234561234567890123456");*/
	             
	        JSONObject body = new JSONObject();
	        body.put("key1", notice.getText());//透传消息自定义body内容
	       /* body.put("key2", "value2");//透传消息自定义body内容
	        body.put("key3", "value3");//透传消息自定义body内容
*/	        
	        JSONObject msg = new JSONObject();
	        msg.put("type", 1);//1: 透传异步消息，通知栏消息请根据接口文档设置
	        msg.put("body", body.toString());//body内容不一定是JSON，可以是String，若为JSON需要转化为String发送
	        
	        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
	        hps.put("msg", msg);
	        
	        JSONObject payload = new JSONObject();
	        payload.put("hps", hps);
	        
	        String postBody = MessageFormat.format(
	        "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}",
	            URLEncoder.encode(accessToken,"UTF-8"),
	            URLEncoder.encode("openpush.message.api.send","UTF-8"),
	            URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
	            URLEncoder.encode(deviceTokens.toString(),"UTF-8"),
	            URLEncoder.encode(payload.toString(),"UTF-8"));
	        
	        String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + appId + "\"}", "UTF-8");
	        httpPost(postUrl, postBody, 5000, 5000);
	    }
	    
	    public static String httpPost(String httpUrl, String data, int connectTimeout, int readTimeout) throws IOException
	    {
	        OutputStream outPut = null;
	        HttpURLConnection urlConnection = null;
	        InputStream in = null;
	        
	        try
	        {
	            URL url = new URL(httpUrl);
	            urlConnection = (HttpURLConnection)url.openConnection();          
	            urlConnection.setRequestMethod("POST");
	            urlConnection.setDoOutput(true);
	            urlConnection.setDoInput(true);
	            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	            urlConnection.setConnectTimeout(connectTimeout);
	            urlConnection.setReadTimeout(readTimeout);
	            urlConnection.connect();
	            
	            // POST data
	            outPut = urlConnection.getOutputStream();
	            outPut.write(data.getBytes("UTF-8"));
	            outPut.flush();
	            
	            // read response
	            if (urlConnection.getResponseCode() < 400)
	            {
	                in = urlConnection.getInputStream();
	            }
	            else
	            {
	                in = urlConnection.getErrorStream();
	            }
	            
	            List<String> lines = IOUtils.readLines(in, urlConnection.getContentEncoding());
	            StringBuffer strBuf = new StringBuffer();
	            for (String line : lines)
	            {
	                strBuf.append(line);
	            }
	            System.out.println(strBuf.toString());
	            return strBuf.toString();
	        }
	        finally
	        {
	            IOUtils.closeQuietly(outPut);
	            IOUtils.closeQuietly(in);
	            if (urlConnection != null)
	            {
	                urlConnection.disconnect();
	            }
	        }
	    }
}
