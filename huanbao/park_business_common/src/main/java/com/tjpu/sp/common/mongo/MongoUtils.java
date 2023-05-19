package com.tjpu.sp.common.mongo;

import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author: chengzq
 * @date: 2018/8/27 0027 上午 11:54
 * @Description:  MongoDb查询工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @throws:
 */
public abstract class MongoUtils {



    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 上午 11:56
     * @Description: 查询时返回query对象
     *
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [obj]
     * @throws:
     */
    public static Query getMongoQuery(Object obj,String pattern){
        Query query = new Query();
        if(null == obj){
            return query;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor propertyDescriptor : propertyDescriptors){
                if("class".equals(propertyDescriptor.getName())){
                    continue;
                }
                Method readMethod = propertyDescriptor.getReadMethod();
                Class<?> propertyType = propertyDescriptor.getPropertyType();
                Object value = readMethod.invoke(obj);
                if(null != value){
                    // generate query
                    String displayName = propertyDescriptor.getDisplayName();
                    Criteria criteria = Criteria.where(displayName);
                    //时间范围查询处理,时间格式必须为{"starttime":"","endtime":""}的json字符串
                    if((value.toString().toLowerCase().contains("starttime")) || (value.toString().toLowerCase().contains("endtime"))){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                        JSONObject jsonObject = JSONObject.fromObject(value.toString());
                        String starttime = jsonObject.getString("starttime");
                        String endtime = jsonObject.getString("endtime");
                        criteria.gte(simpleDateFormat.parse(starttime));
                        criteria.lte(simpleDateFormat.parse(endtime));
                        query.addCriteria(criteria);
                    }
                    //如果为逗号分隔则处理为多个的查询条件，相当于数据库的in查询
                    else if(value.toString().contains(",")){
                        String[] split = value.toString().split(",");
                        criteria.in(Arrays.asList(split));
                        query.addCriteria(criteria);
                    }
                    //查询条件为集合类型 内嵌查询（即查询list内为对象的查询条件）
                    else if(propertyType.getName().equalsIgnoreCase("java.util.list")){
                        List<Map<String,Object>> value1 = (List<Map<String,Object>>) value;
                        if(value1.size()>0){
                            Criteria criteria1=new Criteria();
                            for(Object key:value1.get(0).keySet()){
                                criteria1.and(key.toString()).is(value1.get(0).get(key));
                            }
                            criteria.elemMatch(criteria1);
                            query.addCriteria(criteria);
                        }
                    }else{
                        query.addCriteria(criteria.is(value));
                    }
                }
            }
            return query;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return query;
    }

    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 上午 11:56
     * @Description:  分页查询时返回query对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [obj, searchEntity]
     * @throws:
     */
    public static Query getMongoQueryWithPage(Object obj, MongoSearchEntity searchEntity,String pattern){
        Query mongoQuery = getMongoQuery(obj,pattern);

        int page = searchEntity.getPage();
        int size = searchEntity.getSize();
        mongoQuery.skip((page - 1) * size);
        mongoQuery.limit(size);
        List<String> sortNames = searchEntity.getSortname();
        if(CollectionUtils.isEmpty(sortNames)){
            return mongoQuery;
        }
        Sort sort;
        Integer direction = searchEntity.getSortorder();

        if(Direction.DESC.getKey().equals(direction)){
            sort = new Sort(Sort.Direction.DESC, sortNames);
        } else if (Direction.ASC.getKey().equals(direction)){
            sort = new Sort(Sort.Direction.ASC, sortNames);
        } else {
            String[] propertyArray = new String[sortNames.size()];
            for(int i = 0; i < sortNames.size(); i++){
                propertyArray[i] = sortNames.get(i);
            }
            sort = new Sort(propertyArray);
        }
        mongoQuery.with(sort);
        return mongoQuery;
    }


    /**
     * @author: chengzq
     * @date: 2018/8/27 0027 下午 3:05
     * @Description:  修改时返回Update对象
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [obj]
     * @throws:
     */
    public static Update getMongoUpdate(Object obj){
        Update update = new Update();
        if(null == obj){
            return update;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor propertyDescriptor : propertyDescriptors){
                if("class".equals(propertyDescriptor.getName())){
                    continue;
                }
                Method readMethod = propertyDescriptor.getReadMethod();
                Object value = readMethod.invoke(obj);
                if(null != value){
                    update.set(propertyDescriptor.getDisplayName(),value);
                }
            }
            return update;
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return update;
    }


}