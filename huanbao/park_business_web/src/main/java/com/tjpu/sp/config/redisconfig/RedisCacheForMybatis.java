package com.tjpu.sp.config.redisconfig;

import com.tjpu.sp.common.utils.SpringContextHolderUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.*;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: lip
 * @date: 2019/10/15 0015 下午 2:30
 * @Description: Mybatis二级缓存接口实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
public class RedisCacheForMybatis implements Cache {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final String id;
    private RedisTemplate redisTemplate;
    private final  String keyPrefix = "cache_pub_code_";

    //redis过期时间
    private static final long EXPIRE_TIME_IN_MINUTES = 30;

    public RedisCacheForMybatis(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instance required an ID");
        }
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Put query result to redis
     *
     * @Param key
     * @Param value
     */
    @Override
    public void putObject(Object key, Object value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        String putKey = keyPrefix + key.toString();
        opsForValue.set(putKey, value, EXPIRE_TIME_IN_MINUTES, TimeUnit.MINUTES);
    }

    /**
     *
     * @author: lip
     * @date: 2019/10/16 0016 下午 6:55
     * @Description: 执行查询，先从redis中获取结果，没有则查询数据库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public Object getObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        ValueOperations opsForValue = redisTemplate.opsForValue();
        return opsForValue.get(keyPrefix+key.toString());
    }

    /**
     * Remove cached query result to redis
     *
     * @Param key
     * @Return
     */
    @Override
    public Object removeObject(Object key) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.delete(key);
        System.out.println("从缓存中删除");
        return null;
    }

    /**
     *
     * @author: lip
     * @date: 2019/10/16 0016 下午 6:44
     * @Description: 清空当前相同namespace的所有key值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public void clear() {
        RedisTemplate redisTemplate = getRedisTemplate();
        Set<String> keys = redisTemplate.keys("*:" + this.id + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public int getSize() {
        Long size = (Long) redisTemplate.execute((RedisCallback) connection -> connection.dbSize());
        return size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    private RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = SpringContextHolderUtil.getBean("redisTemplate");
        }
        return redisTemplate;
    }


}
