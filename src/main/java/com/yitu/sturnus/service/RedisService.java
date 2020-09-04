package com.yitu.sturnus.service;

import org.springframework.stereotype.Service;

public class RedisService {
  /**
   * ZSET中的key
   */
  public static final String MILLS_KEY = "mills_key";
  /**
   * 取ZSET时间范围，即[ts-300s, ts]
   */
  public static final long DURATION = 5 * 60 * 1000;
  /**
   * 每次往set里面插入的数据量
   */
  public static final int SET_SIZE = 3;

  /**
   * 从set中取数数量
   */
  public static final int SET_RANGE_SIZE = 1;
}
