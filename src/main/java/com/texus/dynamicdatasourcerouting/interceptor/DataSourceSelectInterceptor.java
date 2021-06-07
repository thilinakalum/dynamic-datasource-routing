package com.texus.dynamicdatasourcerouting.interceptor;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.texus.dynamicdatasourcerouting.config.DataBaseContextHolder;
import com.texus.dynamicdatasourcerouting.entity.DBConfig;
import com.texus.dynamicdatasourcerouting.repository.DBConfigRepository;

import lombok.extern.log4j.Log4j2;

/**
 * @author Thilina Kalum
 * @since 6/7/2021
 */
@Log4j2
@Component
public class DataSourceSelectInterceptor extends HandlerInterceptorAdapter {

  @Autowired
  private DBConfigRepository dbConfigRepository;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    DBConfig byName = dbConfigRepository.findByName(request.getParameter("source"));
    if (!Objects.nonNull(byName)) {
      return true;
    }
    DataBaseContextHolder.setCurrentDb(request.getParameter("source"));
    log.info("selected datasource name is: {}", DataBaseContextHolder.getCurrentDb());
    return true;
  }
}
