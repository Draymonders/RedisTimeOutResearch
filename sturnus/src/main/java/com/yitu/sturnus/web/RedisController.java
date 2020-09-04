package com.yitu.sturnus.web;

import com.yitu.sturnus.service.RedisService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class RedisController {

  @Autowired
  private RedisTemplate redisTemplate;

  BoundZSetOperations<String, Object> millsZSetOps;

  BoundListOperations<String, Object> millsListOps;

  @PostConstruct
  void init() {
    millsZSetOps = redisTemplate.boundZSetOps(RedisService.MILLS_KEY);
  }


  @GetMapping("/set/{id}")
  @ResponseBody
  public String batchPutAndGetMillsData(@PathVariable("id") String id) {
    // 存取5分钟内的数据
    // 5 * 60 = 300s
    long currentTime = 0;
    try {
      currentTime = System.currentTimeMillis();
      millsZSetOps.add(currentTime, currentTime);

      Set<Object> avaliableTimeSet = getAvaliableTime(currentTime);
      long endTime = System.currentTimeMillis();
      log.info("get avaliable time used: {} ms", endTime - currentTime);
      if (avaliableTimeSet != null && avaliableTimeSet.size() > 0) {
        List<Object> arr = new ArrayList<>();

        Iterator<Object> iterator = avaliableTimeSet.iterator();
        int i = 0;
        while (iterator.hasNext()) {
          if (i >= 3) {
            break;
          }
          Object avaliableTime = iterator.next();
          Object o = getDataSetOps(redisTemplate, Long.parseLong(avaliableTime.toString()))
              .randomMember();
          arr.add(o);
          i ++;
        }
        redisTemplate.boundValueOps("listSize").increment(arr.size());
      }
      setObjectByMills(currentTime, id);
      return "ok";
    } catch (Exception e) {
      log.error("set id: " + id + " and ts: " + currentTime + " error, errMsg: ", e);
    }
    return "error";
  }

  @GetMapping("/add")
  @ResponseBody
  public String addZSet() {
    long currentTime = System.currentTimeMillis();
    try {
      millsZSetOps.add(currentTime, currentTime);
      Set<Object> objects = millsZSetOps.rangeByScore(currentTime - 2000, currentTime);
      redisTemplate.boundValueOps("zsetSize").increment(objects.size());
      return "ok";
    } catch (Exception e) {
      log.error("add ts: " + currentTime + " error, errMsg: ", e);
    }
    return "error";
  }

  private static String getMillsKey(long ts) {
    return ts + ":mills";
  }

  public static BoundSetOperations<String, Object> getDataSetOps(
      RedisTemplate<String, Object> redisTemplate, long ts) {
    return redisTemplate.boundSetOps(getMillsKey(ts));
  }

  private void setObjectByMills(long ts, String id) {
    BoundSetOperations<String, Object> dataSetOps = getDataSetOps(redisTemplate, ts);
    dataSetOps.add(id);
  }

  private Set<Object> getAvaliableTime(long currentTime) {
    return millsZSetOps.rangeByScore(currentTime - RedisService.DURATION, currentTime);
  }
}
