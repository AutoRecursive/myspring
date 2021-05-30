package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FisherApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singleObjects = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public FisherApplicationContext(Class configClass) {
        this.configClass = configClass;
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singleObjects.put(beanName, bean);
            }
        }
    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        var clazz = beanDefinition.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // Dependency Injection

            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            //Bean Aware
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName((beanName));
            }

            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // Initialize
            if (instance instanceof InitializingBean) {
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void scan(Class configClass) {
        // Analysis config class
        // ComponentScan --> Scan Paths -->
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value(); // Scan Path
//        System.out.println(path);
        path = path.replace(".", "/");

        ClassLoader classLoader = FisherApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                String fileName = f.getAbsolutePath();

                if(fileName.endsWith(".class")) {
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            //..
//                            System.out.println(className);

                            // Figure out Singleton Bean or Prototype Bean
                            // BeanDefinition

                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);

                            }

                            Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }
//                            System.out.println(beanName);

                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singleObjects.get(beanName);
                return o;
            } else {
                // Create bean object
                return createBean(beanName, beanDefinition);
            }
        } else {
            // Non exist bean
            throw new NullPointerException();
        }
    }
}
