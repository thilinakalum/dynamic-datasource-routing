package com.texus.dynamicdatasourcerouting.config;

/**
 * @author Thilina Kalum
 * @since 6/5/2021
 */
public class DataBaseContextHolder {

  private static final ThreadLocal<String> dbContextHolder = new ThreadLocal<>();

  public static String getCurrentDb() {
    return dbContextHolder.get();
  }

  public static void setCurrentDb(String dbType) {
    dbContextHolder.set(dbType);
  }

  public static void clear() {
    dbContextHolder.remove();
  }
}
