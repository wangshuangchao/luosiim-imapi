package com.shiku.mianshi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.service.ComplintManager;
import cn.xyz.mianshi.vo.Complint;

@RestController
@RequestMapping(value="/complint")
public class ComplintController {

	@Autowired
	private ComplintManager complintManager;
	
	@PostMapping("/add")
	public ResultInfo<String> addComplint(Complint complint){
		ResultInfo<String> result=complintManager.addComplint(complint);
		return result;
	}
	@GetMapping("/test")
	public ResultInfo<String> test(){
		Complint complint=new Complint();
		complint.setUserId(10086);
		complint.setNickname("小白");
		complint.setLsId("123466789");
		complint.setTitle("投诉你丫的");
		complint.setContent("有人发黄片儿");
		ResultInfo<String> result=complintManager.addComplint(complint);
		return result;
	}
	
	
	
}
