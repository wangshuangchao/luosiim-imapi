package cn.xyz.sms.service;

import java.util.Map;

import cn.xyz.commons.vo.ResultInfo;

public interface DomesticSmsService extends SmsService{

	public ResultInfo<String> sendDomesticMsg(Map<String,String> params);
}
