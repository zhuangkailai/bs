package com.tjpu.sp.service.envhousekeepers.checkitemdata;

import java.util.List;
import java.util.Map;

public interface CheckItemDataService {

    /**
     * @author: xsm
     * @date: 2021/06/29 0029 下午 15:37
     * @Description: 根据污染源ID、检查日期、检查类型获取检查项目数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllCheckItemDataByParam(Map<String, Object> param);

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 17:22
     * @Description: 根据污染源ID、检查日期、检查类型获取检查企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    Map<String,Object> getOneCheckEntInfoByParam(Map<String, Object> param);

    /**
     * @author: mmt
     * @date: 2022/08/18
     * @Description: 自定义参数获取多个问题记录数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getManyCheckProblemExpoundDataByParamMap(Map<String, Object> param);
    /**
     * @author: xsm
     * @date: 2021/07/07 0007 上午 10:11
     * @Description: 根据污染源ID、检查日期更新该企业该日期所有检查报告的问题状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    int updateAllCheckProblemExpoundStatusByParam(Map<String, Object> param);

    List<Map<String,Object>> SetCheckTemplateConfigUrlPath(List<Map<String, Object>> listdata,Map<String, Object> param);

    void updateEntCheckFeedbackData(Map<String, Object> param);
}
