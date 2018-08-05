package io.github.spair.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.TimeUnit;

@Configuration
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    private static final int YEAR = 365;
    private static final CacheControl CACHE_CONTROL = CacheControl.maxAge(YEAR, TimeUnit.DAYS).cachePublic();

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .setCacheControl(CACHE_CONTROL);
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CACHE_CONTROL);
    }
}
