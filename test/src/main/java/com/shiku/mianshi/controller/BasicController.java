package com.shiku.mianshi.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import cn.xyz.commons.vo.JSONMessage;
import cn.xyz.mianshi.service.AdminManager;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Config;

@RestController
public class BasicController {
	@Autowired
	private AdminManager adminManager;
	
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
	@RequestMapping(value = "/config/set", method = RequestMethod.GET)
	public ModelAndView setConfig() {
		//adminManager.getConfig();

		ModelAndView mav = new ModelAndView("config_set");
		mav.addObject("config", adminManager.getConfig());
		return mav;
	}
	@RequestMapping(value = "/console/error", method = RequestMethod.GET)
	public ModelAndView error() {
		ModelAndView mav = new ModelAndView("error");
		return mav;
	}

	@RequestMapping(value = "/config/set", method = RequestMethod.POST)
	public void setConfig(HttpServletRequest request, HttpServletResponse response,@ModelAttribute Config config) throws Exception {
		config.XMPPHost=config.XMPPDomain;
		config.setMeetingHost(config.getFreeswitch());
		adminManager.setConfig(config);
		System.out.println(config.toString());
		response.sendRedirect("/config/set");
	}
}
