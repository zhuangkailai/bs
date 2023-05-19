package com.tjpu.sp.service.impl.common;

import com.tjpu.sp.common.mongo.MongoSearchEntity;
import com.tjpu.sp.common.mongo.MongoUtils;
import com.tjpu.sp.service.common.MongoBaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author: chengzq
 * @date: 2018/8/27 0027 14:02
 * @Description: Mongodb增删改查公共类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
public class MongoBaseServiceImpl<T> implements MongoBaseService<T> {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:39
     * @Description: 保存
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t]
     * @throws:
     */
    @Override
    public void save(T t) {
        mongoTemplate.save(t);
    }

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:40
     * @Description: 删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t]
     * @throws:
     */
    @Override
    public void delete(T t) {
        mongoTemplate.remove(t);
    }

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:40
     * @Description: 根据id修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t]
     * @throws:
     */
    @Override
    public void update(T t) {
        Method[] declaredMethods = t.getClass().getDeclaredMethods();
        for(Method method: declaredMethods){
            try {
                if(method !=null && "getId".equals(method.getName())){
                    String id = method.invoke(t).toString();
                    Query query = new Query(Criteria.where("id").is(id));

                    Update update = MongoUtils.getMongoUpdate(t);
                    mongoTemplate.updateFirst(query, update, t.getClass());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:40
     * @Description:查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t, database]
     * @throws:
     */
    @Override
    public List getListByParam(T t,String database,String pattern) {
        Query mongoQuery = MongoUtils.getMongoQuery(t,pattern);
        return mongoTemplate.find(mongoQuery, t.getClass(),database);
    }

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:40
     * @Description: 分页查询
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t, searchEntity, database]
     * @throws:
     */
    @Override
    public List getListWithPageByParam(T t, MongoSearchEntity searchEntity, String database, String pattern) {
        Query mongoQueryWithPage = MongoUtils.getMongoQueryWithPage(t, searchEntity,pattern);
        return mongoTemplate.find(mongoQueryWithPage, t.getClass(), database);
    }


    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 4:40
     * @Description:查询总条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [t, database]
     * @throws:
     */
    @Override
    public long getCount(T t,String database,String pattern) {
        Query mongoQuery = MongoUtils.getMongoQuery(t,pattern);
        return mongoTemplate.count(mongoQuery,t.getClass(),database);
    }

    @Override
    public void deleteAndInsertByKey(T t, String key) {
        Method[] declaredMethods = t.getClass().getDeclaredMethods();
        for(Method method: declaredMethods){
            try {
                if(method !=null && ("get"+key).equals(method.getName())){
                    String value = method.invoke(t).toString();
                    Query query = new Query(Criteria.where(key).is(value));
                    mongoTemplate.remove(query,t.getClass());
                    mongoTemplate.save(t);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
