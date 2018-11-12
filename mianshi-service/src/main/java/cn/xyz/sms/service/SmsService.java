package cn.xyz.sms.service;

import java.util.Map;

import cn.xyz.commons.vo.ResultInfo;


public interface SmsService {

	public ResultInfo<String> sendMsg(Map<String,String> params);
}
