package com.springmvc.context;

import com.springmvc.annotation.AutoWired;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.Service;
import com.springmvc.xml.XmlPaser;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * springmvc 容器
 */
public class WebApplicationContext {

    String contextConfigLocation;

    // 定义集合 用于存放 bean 的权限名|包名.类名，可以利用反射进行实例化
    List<String> classNameList = new ArrayList<String>();

    // 创建Map集合，存放IOC容器
    public Map<String,Object> iocMap = new ConcurrentHashMap<>();

    public WebApplicationContext() {

    }

    public WebApplicationContext(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * 初始化容器
     */
    public void onRefresh() {
        // 1 进行解析springmvc.xml配置文件操作 ===>
        String pack = XmlPaser.getbasePackage(contextConfigLocation.split(":")[1]);

        String[] packs = pack.split(",");
        // 2 进行包扫描
        for (String pa:packs){
            excuteScanPackage(pa);
        }

        // 3 实例化容器中的bean
        excuteInstance();

        //4、进行 自动注入操作
        executeAutoWired();
    }

    private void executeAutoWired() {
        try{
            // 判断bean中是否有属性上使用AutoWired，有的话进行自动注入
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                // 获取容器中的bean
                Object bean = entry.getValue();
                // 获取bean中的属性
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(AutoWired.class)){
                        // 获取注解中的value值，该值就是bean的name
                        AutoWired autoWiredAnno = field.getAnnotation(AutoWired.class);
                        String beanName = autoWiredAnno.value();
                        // 取消检查机制
                        field.setAccessible(true);
                        field.set(bean,iocMap.get(beanName));
                    }
                }
                
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void excuteInstance() {
        try {
            for (String className:classNameList){
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)){
                    // 控制层bean
                    String beanName = clazz.getSimpleName().substring(0,1).toLowerCase()+clazz.getSimpleName().substring(1);
                    iocMap.put(beanName,clazz.newInstance());
                }else if (clazz.isAnnotationPresent(Service.class)){
                    // Service层bean
                    Service serviceAnno = clazz.getAnnotation(Service.class);
                    String beanName = serviceAnno.value();
                    iocMap.put(beanName,clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 扫描包
     * 存好包下的所有类名
     */
    public void excuteScanPackage(String pack){
        // com.jayden.controller ==》 com/jayden/controller
        URL url = this.getClass().getClassLoader().getResource("/" + pack.replaceAll("\\.", "/"));
        String path = url.getFile();

        //
        File dir = new File(path);
        for (File file : dir.listFiles()) {
            if(file.isDirectory()) {
                // 当前是一个文件目录
                excuteScanPackage(pack + "." + file.getName());
            } else {
                // 文件目录下文件 获取全路径 UserController.class  ===> com.jayden.controller.UserController
                String className = pack + "." + file.getName().replaceAll(".class", "");
                classNameList.add(pack + "." + file.getName().split("\\.")[0]);
            }
        }

    }
}
