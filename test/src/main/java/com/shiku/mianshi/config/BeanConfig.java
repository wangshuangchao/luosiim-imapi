package com.shiku.mianshi.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Maps;
import com.shiku.mianshi.KAdminProperties;
import com.shiku.mianshi.filter.AuthorizationFilter;

@Configuration
public class BeanConfig {

	@Autowired
	private KAdminProperties props;

	@Bean
	public FilterRegistrationBean filterRegistrationBean() {
		AuthorizationFilter filter = new AuthorizationFilter();
		Map<String, String> initParameters = Maps.newHashMap();
		initParameters.put("enable", "true");
		List<String> urlPatterns = Arrays.asList("/*");

		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(filter);
		registrationBean.setInitParameters(initParameters);
		registrationBean.setUrlPatterns(urlPatterns);
		return registrationBean;
	}

	@Bean(name = "adminMap")
	public Map<String, String> adminMap() {
		Map<String, String> adminMap = Maps.newHashMap();
		String[] users = props.getUsers().split(",");
		for (String t : users) {
			String[] user = t.split(":");
			System.out.println(user);
			adminMap.put(user[0], user[1]);

			System.out.println("加载管理员帐号：" + user[0]);
		}
		return adminMap;
	}
	 
}
