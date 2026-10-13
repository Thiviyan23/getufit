package com.getufit.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    private final TrainerAuthInterceptor trainerAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/account", "/account/**");
        registry.addInterceptor(trainerAuthInterceptor)
                .addPathPatterns("/trainer", "/trainer/**")
                .excludePathPatterns("/trainer/login");
    }
}