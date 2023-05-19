package com.tjpu.auth.cache;

import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月8日 上午9:30:02
 * @Description:redis集成mybatis实现二级缓存 <cache type="com.tjpu.dc.cache.MybatisRedisCache"/>
 * mapper.xml 文件中添加，使用二级缓存，在需要更新缓存的sql文件上添加flushCache="true"
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public class MybatisRedisCache implements Cache {

    private static JedisSentinelPool jedisSentinelPool;

    private final String id;

    /**
     * 读写锁定义
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    /**
     * @param
     * @author: hy
     * @date: 2018年6月22日 下午17:30:00
     * @Description: 获得redis连接
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    private static Jedis getJedis() {
        Jedis jedis;
        try {
            jedis = jedisSentinelPool.getResource();
            return jedis;
        } catch (JedisConnectionException e) {
            throw e;
        }
    }

    /**
     * @param id
     * @throws
     * @Title: RedisCache
     * @Description:构造方法定义
     */
    public MybatisRedisCache(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
    }

    /**
     * @author: lip
     * @date: 2018年4月8日 上午9:32:10
     * @Description: 清除缓存，通过映射xml文件配置flushCache="true"，自动清除缓存的方法
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void clear() {
        Jedis jedis = null;
        ScanParams scanParams = new ScanParams();
        String scanRet = "0";
        try {
            jedis = getJedis();
            List<String> retList = new ArrayList<>();
            do {
                ScanResult ret = jedis.scan(scanRet, scanParams.match("systemMark" + "*"));
                scanRet = ret.getStringCursor();
                retList.addAll(ret.getResult());
            } while (!scanRet.equals("0"));
            for (String strKey : retList) {
                byte[] bKey = strKey.getBytes();
                jedis.expire(bKey, 0);
            }
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                releaseRedis(jedis);
            }
        }
    }

    /**
     * @return
     * @author: lip
     * @date: 2018年4月8日 上午9:34:43
     * @Description:获取mybatis缓存操作对象的标识符。一个mapper对应一个mybatis的缓存操作对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * @param key
     * @return
     * @author: lip
     * @date: 2018年4月8日 上午10:13:00
     * @Description: 从缓存中获取被缓存的查询结果
     * @updateUser:hy
     * @updateDate:2018年6月22日 下午17:37:04
     * @updateDescription:修改获取方式
     */
    @Override
    public Object getObject(Object key) {
        Object result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();
            byte[] aa = serializer.serialize(key);
            String bb = new String(aa);
            byte[] cc = bb.getBytes();
            result = serializer.deserialize(jedis.get(cc));
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                releaseRedis(jedis);
            }
        }
        return result;
    }

    /**
     * @return
     * @author: lip
     * @date: 2018年4月8日 上午9:35:18
     * @Description:用于实现原子性的缓存操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public ReadWriteLock getReadWriteLock() {
        return this.readWriteLock;
    }

    /**
     * @return
     * @author: lip
     * @date: 2018年4月8日 上午9:35:47
     * @Description: 返回缓存的数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public int getSize() {
        int result = 0;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            result = Integer.valueOf(jedis.dbSize().toString());
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                releaseRedis(jedis);
            }
        }
        return result;
    }

    /**
     * @param key
     * @param value
     * @author: lip
     * @date: 2018年4月8日 上午9:36:05
     * @Description: 将查询结果塞入缓存
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void putObject(Object key, Object value) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();
            // 设置redis中数据过期时间为30分钟
            byte[] bt = serializer.serialize(key);
            String bb = new String(bt);
            byte[] cc = bb.getBytes();
            jedis.setex(cc, 1800, serializer.serialize(value));
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                releaseRedis(jedis);
            }
        }
    }

    /**
     * @param key
     * @return
     * @author: lip
     * @date: 2018年4月8日 上午9:36:30
     * @Description: 从缓存中删除对应的key、value。只有在回滚时触发
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public Object removeObject(Object key) {
        Object result = null;
        Jedis jedis = null;
        try {
            jedis = getJedis();
            RedisSerializer<Object> serializer = new JdkSerializationRedisSerializer();
            result = jedis.expire(serializer.serialize(key), 0);
        } catch (JedisConnectionException e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                releaseRedis(jedis);
            }
        }
        return result;
    }

    /**
     * @param sentinelPool
     * @author: lip
     * @date: 2018年4月8日 上午9:37:04
     * @Description: 设置连接工厂
     * @updateUser:hy
     * @updateDate:2018年6月22日 下午17:37:04
     * @updateDescription:设置哨兵连接池
     */
    public static void setJedisSentinelPool(JedisSentinelPool sentinelPool) {
        MybatisRedisCache.jedisSentinelPool = sentinelPool;
    }

    /**
     * @param jedis
     * @author: hy
     * @date: 2018年6月22日 下午17:37:04
     * @Description: 释放redis连接
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    public static void releaseRedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

}
