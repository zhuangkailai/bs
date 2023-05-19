package com.tjpu.sp.service.envhousekeepers.focusconcernentset;

import com.tjpu.sp.model.envhousekeepers.focusconcernentset.FocusConcernEntSetVO;

import java.util.List;
import java.util.Map;

public interface FocusConcernEntSetService {

    /**
    *@author: xsm
    *@date: 2021/07/09 0009 上午 11:47
    *@Description: 通过自定义参数查询重点关注企业设置信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [jsonObject]
    *@throws:
    **/
    List<Map<String,Object>> getFocusConcernEntSetsByParamMap(Map<String, Object> param);

    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:25
     *@Description: 新增重点关注企业设置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    void insert(FocusConcernEntSetVO entity);


    /**
     *@author: xsm
     *@date: 2021/07/09 0009 下午 13:38
     *@Description: 根据主键ID删除重点关注企业设置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param:
     *@throws:
     **/
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2021/08/05 0005 下午 4:03
     * @Description: 验证重点关注企业是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    Map<String,Object> IsFocusConcernEntSetValidByPollutionid(String pollutionid);
}
