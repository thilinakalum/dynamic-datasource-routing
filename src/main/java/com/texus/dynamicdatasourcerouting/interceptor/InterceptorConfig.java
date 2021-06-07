package com.texus.dynamicdatasourcerouting.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.MappedInterceptor;

/**
 * @author Thilina Kalum
 * @since 6/7/2021
 */
@Configuration
public class InterceptorConfig extends WebMvcConfigurerAdapter {

  @Autowired
  private DataSourceSelectInterceptor datasourceSelectInterceptor;

  @Bean
  public MappedInterceptor dbEditorTenantInterceptor() {
    return new MappedInterceptor(new String[]{"/**"}, datasourceSelectInterceptor);
  }
}