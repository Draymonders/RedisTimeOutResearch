package com.yitu.sturnus.web;

import com.yitu.sturnus.service.RedisService;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class GarbageCollector {

  @Autowired
  private RedisTemplate<String, Object> redisTemplate;

  @Scheduled(fixedRate = 5 * 60 * 1000)
  public void collectGarbage() {
    long currentTime = System.currentTimeMillis();
    Set<Object> objects = redisTemplate.boundZSetOps(RedisService.MILLS_KEY)
        .rangeByScore(0, currentTime - RedisService.DURATION);

    if (objects != null && objects.size () > 0) {
      objects.forEach(object -> {
        RedisController.getDataSetOps(redisTemplate, Long.parseLong(object.toString())).expire(1000, TimeUnit.MILLISECONDS);
      });
      redisTemplate.boundZSetOps(RedisService.MILLS_KEY).removeRangeByScore(0, currentTime - RedisService.DURATION);
      log.info("gc sucess, reduce {} useless object", objects.size());
    } else {
      log.warn("gc end, but not found useful object");
    }
  }
}
