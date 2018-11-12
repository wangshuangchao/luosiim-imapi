package com.shiku.mianshi.filter;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;
import com.shiku.mianshi.ResponseUtil;
import com.shiku.mianshi.controller.AdminController;

import cn.xyz.commons.support.jedis.JedisTemplate;
import cn.xyz.commons.support.spring.SpringBeansUtils;
import cn.xyz.commons.utils.DateUtil;
import cn.xyz.commons.utils.ReqUtil;
import cn.xyz.commons.utils.StringUtil;
import cn.xyz.mianshi.utils.ConstantUtil;
import cn.xyz.mianshi.vo.Constant;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@WebFilter(filterName = "authorizationFilter", urlPatterns = { "/*" }, initParams = {
		@WebInitParam(name = "enable", value = "true") })
@Slf4j
public class AuthorizationFilter implements Filter {
	private static final String LOGIN="login%1$s";
	@Resource(name = "jedisTemplate")
	private static JedisTemplate jedisTemplate;
	
	private JedisPool jedisPool;
	private Map<String, String> requestUriMap;
	private AuthorizationFilterProperties properties;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {

		if (null == jedisPool || null == requestUriMap || null == properties) {
			requestUriMap = Maps.newHashMap();
			jedisPool = SpringBeansUtils.getBean("jedisPool");
			properties = SpringBeansUtils.getContext().getBean(AuthorizationFilterProperties.class);

			for (String requestUri : properties.getRequestUriList()) {
				requestUriMap.put(requestUri, requestUri);
			}
		}

		// if (!enable) {
		// arg2.doFilter(arg0, arg1);
		// return;
		// }

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		request.setCharacterEncoding("utf-8"); 
		response.setCharacterEncoding("utf-8"); 
		response.setContentType("text/html;charset=utf-8");
		String accessToken = request.getParameter("access_token");
		String requestUri = request.getRequestURI();
		if("/favicon.ico".equals(requestUri))
			return;

		// DEBUG**************************************************DEBUG
		StringBuffer sb = new StringBuffer();
		sb.append("请求：" + request.getRequestURI());
		Map<String, String[]> paramMap = request.getParameterMap();
		if (!paramMap.isEmpty())
			sb.append("?");
		for (String key : paramMap.keySet()) {
			sb.append(key).append("=").append(paramMap.get(key)[0]).append("&");
		}
		log.info(sb.toString());
		log.info("Content-Type：" + request.getContentType());
		log.info("User-Agent：" + request.getHeader("User-Agent"));
		log.info("***  "+DateUtil.getFullString());
//		System.out.println(sb.toString());
//		System.out.println("Content-Type：" + request.getContentType());
//		System.out.println("User-Agent：" + request.getHeader("User-Agent"));
//		System.out.println("********************************************   "+DateUtil.getFullString());
		// DEBUG**************************************************DEBUG

		// 如果访问的是控制台或资源目录
		if (requestUri.startsWith("/console") || requestUri.startsWith("/pages")
				|| requestUri.startsWith("/config/set")||requestUri.endsWith(".js")||requestUri.endsWith(".css")||requestUri.endsWith(".html")||requestUri.endsWith(".png")) {
			Object obj = request.getSession().getAttribute(AdminController.LOGIN_USER_KEY);
//			String key=String.format(LOGIN, userId);
//			obj=jedisTemplate.get(key);
			// 用户已登录或访问资源目录或访问登录页面
			if (null != obj || requestUri.startsWith("/pages") || requestUri.startsWith("/console/login")){
				arg2.doFilter(arg0, arg1);
				return;
			}
			else
				response.sendRedirect("/console/login");
		} else if (requestUri.startsWith("/mp")) {
			Object obj = request.getSession().getAttribute("MP_USER");
			if (null != obj || requestUri.startsWith("/pages") || requestUri.startsWith("/mp/login")){
				arg2.doFilter(arg0, arg1);
				return;
			}
			else
				response.sendRedirect("/mp/login");
		} else {
			// 需要登录
			if (isNeedLogin(request.getRequestURI())) {
				// 请求令牌是否包含
				if (StringUtil.isEmpty(accessToken)) {
					//System.out.println("不包含请求令牌");
					log.error("不包含请求令牌");
					int tipsKey =1030101;
					renderByErrorKey(response, tipsKey);
				} else {
					String userId = getUserId(accessToken);
					// 请求令牌是否有效
					if (null == userId) {
						//System.out.println("请求令牌无效或已过期...");
						log.error("请求令牌无效或已过期...");
						int tipsKey = 1030102;
						renderByErrorKey(response, tipsKey);
					} else {
						ReqUtil.setLoginedUserId(Integer.parseInt(userId));
						arg2.doFilter(arg0, arg1);
						return;
					}
				}
			} else {
				String userId = getUserId(accessToken);
				/*if(requestUri.startsWith("/tigase/notify")){
					String body= request.getParameter("body");
					System.out.println(body);
				}
				*/
				
				if (null != userId) {
					ReqUtil.setLoginedUserId(Integer.parseInt(userId));
				}
				arg2.doFilter(arg0, arg1);
			}
		}
	}

	private boolean isNeedLogin(String requestUri) {
		return !requestUriMap.containsKey(requestUri.trim());
	}

	private String getUserId(String _AccessToken) {
		Jedis resource = jedisPool.getResource();
		String userId = null;

		try {
			userId = resource.get(String.format("at_%1$s", _AccessToken));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			jedisPool.returnResource(resource);
		}

		return userId;
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// enable = Boolean.parseBoolean(arg0.getInitParameter("enable"));
		// requestUriMap = Maps.newHashMap();
		// jedisPool = SpringBeansUtils.getBean("jedisPool");
		// properties =
		// SpringBeansUtils.getContext().getBean(AuthorizationFilterProperties.class);
		//
		// for (String requestUri : properties.getRequestUriList()) {
		// requestUriMap.put(requestUri, requestUri);
		// }
	}

	private static final String template = "{\"resultCode\":%1$s,\"resultMsg\":\"%2$s\"}";

	private static void renderByErrorKey(ServletResponse response, int tipsKey) {
		Constant constant = ConstantUtil.getMsgByCode(tipsKey+"", "zh");
		String s=null;
		if(null==constant){
			s ="服务端缺少配置:错误代码为"+tipsKey+ "的KConstants未配置";
			log.error(s);
			s = String.format(template, tipsKey, s);
		}else{
			String tipsValue = constant.getValue();
			s = String.format(template, tipsKey, tipsValue);
			
		}

		ResponseUtil.output(response, s);
	}

}
