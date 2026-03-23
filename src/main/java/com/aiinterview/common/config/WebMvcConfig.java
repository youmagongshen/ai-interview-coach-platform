package com.aiinterview.common.config;

import com.aiinterview.common.auth.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/inv/**")
                .excludePathPatterns(
                    "/inv/auth/**",
                    "/inv/roles",  // 岗位列表公开访问
                    "/inv/ai/**",   // AI接口公开访问
                    "/inv/question-bank/**"  // 题库接口公开访问
                );
    }
}