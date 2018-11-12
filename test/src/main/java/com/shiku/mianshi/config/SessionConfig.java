package com.shiku.mianshi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
/**
 * 
* <p>Title: SessionConfig</p>  
* <p>Description:用于集群session共享 </p>  
* @author xiaobai  
* @date 2018年8月1日
 */
@Configuration 
@EnableRedisHttpSession(maxInactiveIntervalInSeconds= 1800)
public class SessionConfig {

//    @Bean
//    public HttpSessionStrategy httpSessionStrategy() {
//        return new HeaderHttpSessionStrategy();
//    }

}
