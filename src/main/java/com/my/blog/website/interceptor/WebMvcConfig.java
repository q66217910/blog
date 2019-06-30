package com.my.blog.website.interceptor;


import com.my.blog.website.constant.WebConst;
import com.my.blog.website.utils.TaleUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

/**
 * 向mvc中添加自定义组件
 * Created by BlueT on 2017/3/9.
 */
@Component
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    @Value("${" + WebConst.FILE_PATH_KEY + "}")
    String filePath;

    @Resource
    private BaseInterceptor baseInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(baseInterceptor);
    }

    /**
     * 添加静态资源文件，外部可以直接访问地址
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        TaleUtils.setFilePath(filePath);
        registry.addResourceHandler("/upload/**").addResourceLocations("file:"+ TaleUtils.getFilePath() + "upload/");
        super.addResourceHandlers(registry);
    }
}
