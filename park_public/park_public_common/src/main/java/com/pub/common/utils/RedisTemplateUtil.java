package com.pub.common.utils;

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

public class RedisTemplateUtil {

    private static RedisTemplate<String, Serializable> redisTemplate = (RedisTemplate<String, Serializable>) SpringContextHolderUtil.getBean("redisTemplate");
    public final static int CAHCEONEMINUTE = 60;
    public final static int CAHCEONEHOUR = 60 * 60;
    public final static int CAHCETWOHOUR = 60 * 60 * 2;
    public final static int CAHCE12HOUR = 60 * 60 * 12;
    public final static int CAHCEDAY = 60 * 60 * 24;
    public final static int CAHCETWODAY = 60 * 60 * 24 * 2;
    public final static int CAHCEWEEK = 60 * 60 * 24 * 7;
    public final static int CAHCEMONTH = 60 * 60 * 24 * 7 * 30;


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

    public static boolean deleteCache(String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return redisTemplate.delete(key);
            }
        });
    }


    @SuppressWarnings("unchecked")
    public synchronized static <T> T getRedisCacheDataByToken(String paramKey, Class<T> target) {
        final HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        try {
            String redisKey;
            if (request.getHeader("token") != null) {
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
