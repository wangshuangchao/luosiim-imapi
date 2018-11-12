package cn.xyz.sms.service;

import java.util.Map;

import cn.xyz.commons.vo.ResultInfo;

public interface InternationalSmsService extends SmsService {

	public ResultInfo<String> sendInternationalMsg(Map<String,String> params);

}
