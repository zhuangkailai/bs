package com.tjpu.sp.service.common;


import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.model.common.mongodb.LatestDataVO;

import java.util.List;

/**
 * @author: chengzq
 * @date: 2018/8/27 0027 13:57
 * @Description: Mongodb增删改查公共接口
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
public interface MongoBaseService<T> {

     void save(T t);

     void delete(T t);

     void update(T t);
     /**
      * @author: chengzq
      * @date: 2018/9/12 0012 上午 8:27
      * @Description: 查询 查询时间格式为yyyy-MM-dd hh:mm:ss
      * @updateUser:
      * @updateDate:
      * @updateDescription:
      * @param: [t, database]
      * @throws:
      */
     List<T> getListByParam(T t, String database, String pattern);

     /**
      * @author: chengzq
      * @date: 2018/9/12 0012 上午 8:27
      * @Description: 分页查询 查询时间格式为yyyy-MM-dd hh:mm:ss
      * @updateUser:
      * @updateDate:
      * @updateDescription:
      * @param: [t, searchEntity, database]
      * @throws:
      */
     List<T> getListWithPageByParam(T t, MongoSearchEntity searchEntity, String database, String pattern);

     /**
      * @author: chengzq
      * @date: 2019/7/2 0002 下午 3:43
      * @Description: 获取总数
      * @updateUser:
      * @updateDate:
      * @updateDescription:
      * @param: [t, database, pattern]
      * @throws:
      */
     long getCount(T t,String database,String pattern);

    void deleteAndInsertByKey(T t, String key);
}
