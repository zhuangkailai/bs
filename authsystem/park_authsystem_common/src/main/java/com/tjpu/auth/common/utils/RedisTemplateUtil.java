//这段代码是一个用于操作 Redis 的工具类。它通过 Spring 框架提供的 RedisTemplate 进行 Redis 数据的读写操作。
//RedisTemplateUtil 类中的方法都是通过 RedisTemplate 的 execute() 方法来执行 Redis 操作的。


package com.tjpu.auth.common.utils;

import net.sf.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import redis.clients.jedis.exceptions.JedisConnectionException;


import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RedisTemplateUtil {

    private static RedisTemplate<String, Serializable> redisTemplate = (RedisTemplate<String, Serializable>) SpringContextHolderUtil.getBean("redisTemplate");
    public final static int CAHCEONEMINUTE = 60;// 默认缓存时间 60S
    public final static int CAHCEONEHOUR = 60 * 60;// 默认缓存时间 1hr
    public final static int CAHCETWOHOUR = 60 * 60 * 2;// 默认缓存时间 2hr
    public final static int CAHCE12HOUR = 60 * 60 * 12;// 默认缓存时间 12hr
    public final static int CAHCEDAY = 60 * 60 * 24;// 默认缓存时间 1Day
    public final static int CAHCETWODAY = 60 * 60 * 24*2;// 默认缓存时间 2Day
    public final static int CAHCEWEEK = 60 * 60 * 24 * 7;// 默认缓存时间 1week
    public final static int CAHCEMONTH = 60 * 60 * 24 * 7 * 30;// 默认缓存时间 1month


    /**
     * @author: zkl
     * @date: 2023/1/19 0003 下午 2:23
     * @Description: 设置过期时间存储到 Redis，将数据存储到 Redis 中，并设置过期时间
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: true:存放成功，false：存放失败
     */
    public static boolean putCacheWithExpireTime(String key, Object value, long expireTime) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                connection.set(keySerializer.serialize(key), valueSerializer.serialize(value));
                connection.expire(keySerializer.serialize(key), expireTime);
                return true;
            }
        });
    }


    /**
     * @author: zkl
     * @date: 2023/1/19 0003 下午 2:23
     * @Description: 从redis获取数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    public static <T> T getCache(final String key, Class<T> targetClass) {
        return redisTemplate.execute(new RedisCallback<T>() {
            @Override
            public T doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                GenericJackson2JsonRedisSerializer valueSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getValueSerializer();
                byte[] value = connection.get(keySerializer.serialize(key));
                return valueSerializer.deserialize(value, targetClass);
            }
        });
    }

    /**
     * @author: zkl
     * @date: 2023/1/19 0003 下午 2:23
     * @Description: 删除 Redis 中的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: true:删除成功，false：删除失败
     */
    public static boolean deleteCache(String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return redisTemplate.delete(key);
            }
        });
    }
    
    /**
     * @Description: 获取key值过期时间（秒），获取 Redis 中指定 key 值的过期时间
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/9/21 8:58
     */ 
    public static long getExpireTime(String key) {
        return redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return redisTemplate.opsForValue().getOperations().getExpire(key, TimeUnit.SECONDS);
            }
        });
    }


    /**
     * @author: zhangzc
     * @date: 2018/5/23 18:50
     * @Description: 通过存入的key值在redis中获取用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:Object
     */
    @SuppressWarnings("unchecked")
    public synchronized static <T> T getRedisCacheDataByToken(String paramKey, Class<T> target) {
        final HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        try {
            String redisKey;
            if (request.getHeader("token")!=null){
                redisKey = request.getHeader("token");
                JSONObject userInfoJson = RedisTemplateUtil.getCache(redisKey, JSONObject.class);
                if (userInfoJson != null) {
                    return (T) userInfoJson.get(paramKey);
                }
            }
            return null;
        } catch (JedisConnectionException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
