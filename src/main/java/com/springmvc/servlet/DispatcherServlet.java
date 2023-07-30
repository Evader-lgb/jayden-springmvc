package com.springmvc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springmvc.annotation.Controller;
import com.springmvc.annotation.RequestMapping;
import com.springmvc.annotation.ResponseBody;
import com.springmvc.context.WebApplicationContext;
import com.springmvc.handler.MyHandler;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    // 指定SpringMVC容器
    private WebApplicationContext webApplicationContext;

    List<MyHandler> handlerList = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        // 1 加载初始化参数 classpath:springmvc.xml
        String contextConfigLocation = this.getServletConfig().getInitParameter("contextConfigLocation");

        // 2 创建Springmvc容器
        webApplicationContext = new WebApplicationContext(contextConfigLocation);

        // 3 进行初始化操作
        webApplicationContext.onRefresh();

        // 4 初始化请求映射关系
        initHandlerMapping();
    }

    private void initHandlerMapping() {
        for (Map.Entry<String, Object> entry : webApplicationContext.iocMap.entrySet()) {
            // 获取bean的class类型
            Class<?> clazz = entry.getValue().getClass();

            if (clazz.isAnnotationPresent(Controller.class)){
                // 获取bean中的所有方法，为所有的方法建立映射关系
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        // 获取注解中的值
                        String url = requestMapping.value();

                        // 建立映射地址 与控制器，方法
                        MyHandler myHandler = new MyHandler(url, entry.getValue(), method);
                        handlerList.add(myHandler);
                    }
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 进行请求分发处理
        doDisPatcher(request,response);
    }

    private void doDisPatcher(HttpServletRequest request, HttpServletResponse response) {
        // 查找handler
        MyHandler myHandler = getHandler(request);

        try{
            if (myHandler == null){
                response.getWriter().write("404 not found");
                return;
            }else {
                Object result = myHandler.getMethod().invoke(myHandler.getController());
                if (result instanceof String){
                    if(result instanceof String){
                        String viewName = (String) result;
                        // forward:/success.jsp
                        if (viewName.contains(":")){
                            String viewType = viewName.split(":")[0];
                            String viewPage = viewName.split(":")[1];
                            if(viewType.equals("forward")) {
                                request.getRequestDispatcher(viewPage).forward(request, response);
                            }else {
                                response.sendRedirect(viewPage);
                            }
                        }else {
                            // 默认转发
                            // 跳转页面
                            request.getRequestDispatcher(viewName).forward(request,response);
                        }
                    }
                }else{
                    // 返回json数据
                    Method method = myHandler.getMethod();
                    if (method.isAnnotationPresent(ResponseBody.class)) {
                       // 将返回值转换成json数据
                        ObjectMapper objectMapper = new ObjectMapper();
                        String json = objectMapper.writeValueAsString(result);
                        response.setContentType("text/html;charset=utf-8");
                        PrintWriter writer = response.getWriter();
                        writer.print(json);
                        writer.flush();
                        writer.close();
                    }
                }

            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MyHandler getHandler(HttpServletRequest request) {
        // 获取用户请求的地址
        String requestURI = request.getRequestURI();
        for (MyHandler myHandler : handlerList) {
            if (requestURI.equals(myHandler.getUrl())){
                return myHandler;
            }
        }
        return null;
    }
}
