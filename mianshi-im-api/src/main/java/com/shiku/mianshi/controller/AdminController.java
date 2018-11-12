package com.shiku.mianshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 酷聊后台管理
 * 
 * @author luorc
 *
 */
@Controller
@RequestMapping("/console")
public class AdminController extends AbstractController {
	
	
	
	
	
	public static final String LOGIN_USER_KEY = "LOGIN_USER";

	@RequestMapping(value = "/err", method = RequestMethod.GET)
	public ModelAndView error() {
		ModelAndView mav = new ModelAndView("err");
		return mav;
	}
	
	
}
