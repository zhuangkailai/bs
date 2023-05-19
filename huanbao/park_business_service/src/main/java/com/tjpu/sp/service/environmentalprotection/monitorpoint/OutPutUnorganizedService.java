package com.tjpu.sp.service.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;

import java.util.List;
import java.util.Map;

public interface OutPutUnorganizedService {

    long countTotalByParam(Map<String, Object> paramMap);

    int deleteByPrimaryKey(String pkId);

    UnorganizedMonitorPointInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(UnorganizedMonitorPointInfoVO record);

    List<Map<String, Object>> getOnlineUnorganizedMonitorPointInfoByParamMap(Map<String, Object> paramMap);

//    PageInfo<Map<String, Object>> getOnlineUnorganizedMonitorPointInfoByParamMapForPage(Integer pageSize, Integer pageNum, Map<String, Object> paramMap);

    List<Map<String, Object>> getPollutionUnorganizedMonitorPointInfoByParamMap(Map<String, Object> paramMap);

    List<Map<String, Object>> setUNGasOutPutAndPollutantDetail(List<Map<String, Object>> detailDataTemp, String id);

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:36
     * @Description: 获取所有已监测厂界小型站和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorUnMINIAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/21 0021 下午 3:36
     * @Description: 获取所有已监测厂界恶臭和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorUnstenchAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/12/12 0012 上午 10:54
     * @Description: 获取所有已监测厂界扬尘和状态信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    List<Map<String, Object>> getAllMonitorUnDustAndStatusInfo();

    /**
     * @author: chengzq
     * @date: 2019/6/25 0025 下午 3:07
     * @Description: 通过监测点名称，污染源id查询监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params);

    /**
     * @author: xsm
     * @date: 2019/6/27 0027 下午 4:55
     * @Description: 根据监测点ID和监测点类型获取该监测点 下所有污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getEntBoundaryAllPollutantsByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/27 0027 下午 4:57
     * @Description: 根据监测点ID和监测点类型获取该监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getOutPutUnorganizedInfoByIDAndType(Map<String, Object> paramMap);


    /**
     * @author: zhangzc
     * @date: 2019/7/30 15:23
     * @Description: 获取无组织排口相关的企业、排口、污染物信息（厂界小型站，厂界恶臭）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getUnorganizedPollutionOutPutPollutants(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 下午 5:02
     * @Description: gis-根据监测点类型获取所有厂界恶臭或厂界小型站的点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    Map<String, Object> getAllUnorganizedInfoByType(Map<String, Object> paramMap, Map<String, Object> pollutionMap);


    /**
     * @author: chengzq
     * @date: 2019/10/28 0028 下午 3:42
     * @Description: 通过味道code和mn号集合查询厂界恶臭信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> selectFactStenchInfoBySmellCodeAndMns(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/11/4 0004 下午 1:35
     * @Description: 删除状态表中垃圾数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int deleteGarbageData();
}
