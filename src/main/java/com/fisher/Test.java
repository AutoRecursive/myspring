package com.fisher;

import com.fisher.service.UserService;
import com.spring.FisherApplicationContext;

public class Test {
    public static void main(String[] args) {
        FisherApplicationContext applicationContext = new FisherApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
//        System.out.println(applicationContext.getBean(("userService")));
//        System.out.println(applicationContext.getBean(("userService")));
//        System.out.println(applicationContext.getBean(("userService")));
        userService.test();

    }
}
