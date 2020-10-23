package com.yitu.sturnus.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 简单调用redis 排查
 */
@Slf4j
@Controller
public class RedisSampleController {

  @Autowired
  private RedisTemplate redisTemplate;

  private static final String REDIS_KEY = "TEST_REDIS";
  private static final String PREFIEX_LIST = "LIST:";
  private static final String PREFIEX_VALUE = "VALUE:";

  @GetMapping("/exec-redis-cmd")
  @ResponseBody
  public String redisExecSample() {
    try {
      redisTemplate.boundListOps(PREFIEX_LIST + REDIS_KEY).rightPush("2333");
      redisTemplate.boundValueOps(PREFIEX_VALUE + REDIS_KEY).set("2333");
      return "redis exec cmd success";
    } catch (Exception e) {
      log.error("exec redis cmd error, timestamp: " + System.currentTimeMillis(), e);
      return "redis exec cmd error";
    }
  }
}
