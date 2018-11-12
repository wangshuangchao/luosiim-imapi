package cn.xyz.mianshi.service;

import cn.xyz.commons.vo.ResultInfo;
import cn.xyz.mianshi.vo.Complint;

public interface ComplintManager {

	ResultInfo<String> addComplint(Complint complint);
	
}
