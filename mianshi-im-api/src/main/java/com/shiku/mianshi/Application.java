package com.shiku.mianshi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableScheduling
@SpringBootApplication(scanBasePackages = { "cn.xyz", "com.shiku" }/*, exclude = { MongoAutoConfiguration.class, RedisAutoConfiguration.class,
		DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class }*/)
public class Application {
	
	public static void main(String... args) {
		SpringApplication.run(Application.class, args);
	}

}
