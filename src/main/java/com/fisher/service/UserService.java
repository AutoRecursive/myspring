package com.fisher.service;

import com.spring.*;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware,InitializingBean {

    @Autowired
    private OrderService orderService;

    private String beanName;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void test() {
        System.out.println(orderService);
        System.out.println(name);

    }

    @Override
    public void setBeanName(String name) {
        beanName = name;
        System.out.println(beanName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Initializing Bean...");
    }
}
