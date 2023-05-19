package com.tjpu.sp.common.utils;

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
    public final static int CAHCEONEMINUTE = 60;// 默认缓存时间 60S
    public final static int CAHCEONEHOUR = 60 * 60;// 默认缓存时间 1hr
    public final static int CAHCETWOHOUR = 60 * 60 * 2;// 默认缓存时间 2hr
    public final static int CAHCE12HOUR = 60 * 60 * 12;// 默认缓存时间 12hr
    public final static int CAHCEDAY = 60 * 60 * 24;// 默认缓存时间 1Day
    public final static int CAHCETWODAY = 60 * 60 * 24 * 2;// 默认缓存时间 2Day
    public final static int CAHCEWEEK = 60 * 60 * 24 * 7;// 默认缓存时间 1week
    public final static int CAHCEMONTH = 60 * 60 * 24 * 7 * 30;// 默认缓存时间 1month


    /**
     * @author: lip
     * @date: 2019/6/3 0003 下午 2:23
     * @Description: 设置过期时间存储到redis
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
     * @author: chengzq
     * @date: 2019/09/03 0003 下午 2:23
     * @Description: 设置过期时间存储到redis
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: true:存放成功，false：存放失败
     */
    public static boolean putCacheWithExpireAtTime(String key, Object value, long unixTimeInMillis) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                connection.set(keySerializer.serialize(key), valueSerializer.serialize(value));
                connection.pExpireAt(keySerializer.serialize(key), unixTimeInMillis);
                return true;
            }
        });
    }

    /**
     * @author: chengzq
     * @date: 2019/09/03 0003 下午 2:23
     * @Description: 如果库中存在key则不做任何操作返回false，没有则存入key返回true
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: true:存放成功，false：存放失败
     */
    public static boolean putCacheNXWithExpireAtTime(String key, Object value, long unixTimeInMillis) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
                Boolean aBoolean = connection.setNX(keySerializer.serialize(key), valueSerializer.serialize(value));
                connection.pExpireAt(keySerializer.serialize(key), unixTimeInMillis);
                return aBoolean;
            }
        });
    }


    /**
     * @author: chengzq
     * @date: 2020/3/10 0010 上午 10:27
     * @Description: 设置过期时间到某时间点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [key, unixTimeInMillis]
     * @throws:
     */
    public static boolean setExpireAtTime(String key, long unixTimeInMillis) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer keySerializer = redisTemplate.getKeySerializer();
                Boolean aBoolean = connection.pExpireAt(keySerializer.serialize(key), unixTimeInMillis);
                return aBoolean;
            }
        });
    }


    /**
     * @author: lip
     * @date: 2019/6/3 0003 下午 2:23
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
     * @author: lip
     * @date: 2019/6/3 0003 下午 3:49
     * @Description:
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

            if (request.getHeader("token") != null) {
                String redisKey = request.getHeader("token");
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
    public synchronized static <T> T getRedisCacheDataByKey(String paramKey, String key, Class<T>target) {
        try {
            JSONObject userInfoJson = RedisTemplateUtil.getCache(key, JSONObject.class);
            if (userInfoJson != null) {
                return (T) userInfoJson.get(paramKey);
            }

            return null;
        } catch (JedisConnectionException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: xsm
     * @date: 2020/06/09 0009 下午 7:35
     * @Description: 判断是否存在key
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: true:存在，false：不存在
     */
    public static boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean RLock(String lockKey, int delaySeconds) {
        boolean isSuccess;
        if (hasKey(lockKey)){
            isSuccess = false;
        }else {
            RedisTemplateUtil.putCacheWithExpireTime(lockKey,lockKey,delaySeconds);
            isSuccess = true;
        }
        return isSuccess;
    }
}
