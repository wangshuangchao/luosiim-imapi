package cn.xyz.sms.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

import cn.xyz.commons.autoconfigure.KApplicationProperties.ALiSmsConfig;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.SmsRecord;
import cn.xyz.sms.service.DomesticSmsService;

@Service("aLiSmsServiceImpl")
public class ALiSmsServiceImpl implements DomesticSmsService, ApplicationContextAware {
	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	public static ALiSmsServiceImpl getInstance() {
		return context.getBean(ALiSmsServiceImpl.class);
	}

	// 产品名称:云通信短信API产品,开发者无需替换
	static final String product = "Dysmsapi";
	// 产品域名,开发者无需替换
	static final String domain = "dysmsapi.aliyuncs.com";

	@Resource(name = "aLiSmsConfig")
	private ALiSmsConfig smsConfig;
	
	@Override
	public ResultInfo<String> sendMsg(Map<String, String> params){
		ResultInfo<String> result=new ResultInfo<>();
		try {
			SendSmsResponse response = sendMsgByAL(params.get("mobile"),params.get("code"));
			result.setCode("1000");
			result.setData(params.get("code"));
			result.setMsg(response.getMessage());
		} catch (ClientException e) {
			e.printStackTrace();
			result.setCode("1000");
			result.setData(params.get("code"));
			result.setMsg(e.getMessage());
			return result;
		}
		
		return result;
	}

	public SendSmsResponse sendMsgByAL(String mobile, String param) throws ClientException {
		System.out.println(smsConfig.getCodeTemplate()+"---"+smsConfig.getSignName());
		SendSmsResponse sendSms = sendSms(mobile, param, smsConfig.getCodeTemplate(), smsConfig.getSignName());
		return sendSms;
	}

	public SendSmsResponse sendSms(String mobile, String param, String templateCode, String signName)
			throws ClientException {

		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		// 初始化acsClient,暂不支持region化
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsConfig.getAccessId(),
				smsConfig.getAccessKey());
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
		IAcsClient acsClient = new DefaultAcsClient(profile);

		// 组装请求对象-具体描述见控制台-文档部分内容
		SendSmsRequest request = new SendSmsRequest();
		// 必填:待发送手机号
		request.setPhoneNumbers(mobile);
		// 必填:短信签名-可在短信控制台中找到
		request.setSignName(signName);
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode(templateCode);
		// 可选:模板中的变量替换JSON串,如模板内容为"您的验证码为${code}"时,此处的值为
		request.setTemplateParam("{\"code\" : \"" + param + "\"}");
		// request.setTemplateParam(param);

		// 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
		// request.setSmsUpExtendCode("90997");

		// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		request.setOutId("yourOutId");

		// hint 此处可能会抛出异常，注意catch
		SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

		return sendSmsResponse;
	}

	public QuerySendDetailsResponse querySendDetails(String mobile, String bizId) throws ClientException {

		/*
		 * String accessKeyId = env.getProperty("accessKeyId"); String
		 * accessKeySecret = env.getProperty("accessKeySecret");
		 */

		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		// 初始化acsClient,暂不支持region化
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsConfig.getAccessId(),
				smsConfig.getAccessKey());
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
		IAcsClient acsClient = new DefaultAcsClient(profile);

		// 组装请求对象
		QuerySendDetailsRequest request = new QuerySendDetailsRequest();
		// 必填-号码
		request.setPhoneNumber(mobile);
		// 可选-流水号
		request.setBizId(bizId);
		// 必填-发送日期 支持30天内记录查询，格式yyyyMMdd
		SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
		request.setSendDate(ft.format(new Date()));
		// 必填-页大小
		request.setPageSize(10L);
		// 必填-当前页码从1开始计数
		request.setCurrentPage(1L);

		// hint 此处可能会抛出异常，注意catch
		QuerySendDetailsResponse querySendDetailsResponse = acsClient.getAcsResponse(request);

		return querySendDetailsResponse;
	}

	@Override
	public ResultInfo<String> sendDomesticMsg(Map<String, String> params) {
		ResultInfo<String> result=new ResultInfo<>();
		String telephone=params.get("telephone");
		String areaCode=params.get("areaCode");
		String code=params.get("code");
		String phone=telephone.substring(areaCode.length());
		try {
			SendSmsResponse response = sendMsgByAL(phone,code);
			//存入数据库
        	System.out.println("msgid =  "+response.getBizId());
	        //发送短信记录保存到数据库
	        saveSMSToDB(telephone, areaCode, code, response.getCode(), response.getBizId());
			result.setCode("1000");
			result.setData(params.get("code"));
			result.setMsg(response.getMessage());
		} catch (ClientException e) {
			e.printStackTrace();
			result.setCode("1001");
			result.setData(params.get("code"));
			result.setMsg(e.getMessage());
			return result;
		}
		
		return result;
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
