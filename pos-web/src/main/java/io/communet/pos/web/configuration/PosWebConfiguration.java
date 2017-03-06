package io.communet.pos.web.configuration;

import io.communet.pos.web.interceptor.IpInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * <p>function:
 * <p>User: LeeJohn
 * <p>Date: 2016/7/18
 * <p>Version: 1.0
 */
@Configuration
public class PosWebConfiguration extends WebMvcConfigurerAdapter {
    @Value("${posWeb.ips:}")
    private String ips;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IpInterceptor(ips)).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
