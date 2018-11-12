package com.shiku.mianshi.controller;

import java.util.List;

import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="/publicNum")
public class IndexController {

	@GetMapping("/gotoIndex")
	public ModelAndView complintList() {
		ModelAndView mav = new ModelAndView("complintList");
		mav.addObject("user", null);

		return mav;
	}
}
