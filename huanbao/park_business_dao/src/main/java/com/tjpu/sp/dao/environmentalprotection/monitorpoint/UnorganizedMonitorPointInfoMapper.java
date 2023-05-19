package com.tjpu.sp.dao.environmentalprotection.monitorpoint;


import com.tjpu.sp.model.environmentalprotection.monitorpoint.UnorganizedMonitorPointInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UnorganizedMonitorPointInfoMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(UnorganizedMonitorPointInfoVO record);

    int insertSelective(UnorganizedMonitorPointInfoVO record);

    UnorganizedMonitorPointInfoVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(UnorganizedMonitorPointInfoVO record);

    int updateByPrimaryKey(UnorganizedMonitorPointInfoVO record);


    /**
     * @author: lip
     * @date: 2018/12/19 0019 上午 11:51
     * @Description: 自定义查询条件统计表记录数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    long countTotalByParam(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/19 0019 下午 2:18
     * @Description: 自定义查询条件获取污染源下在线无组织监测点信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getOnlineUnorganizedMonitorPointInfoByParamMap(Map<String, Object> paramMap);


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
     * @date: 2019/6/25 0025 下午 3:07
     * @Description: 通过监测点名称，污染源id查询监测点
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    Map<String, Object> selectByPollutionidAndOutputName(Map<String, Object> params);


    List<Map<String, Object>> getEntBoundaryAllPollutantsByIDAndType(Map<String, Object> paramMap);

    List<Map<String, Object>> getOutPutUnorganizedInfoByIDAndType(Map<String, Object> paramMap);

    /**
     * @author: XSM
     * @date: 2019/7/13 0013 上午 11:32
     * @Description: 通过类型和自定义参数获取厂界污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [params]
     * @throws:
     */
    List<Map<String, Object>> getUnorganizedDgimnAndPollutantInfosByParam(Map<String, Object> paramMap);

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
     * @author: zhangzhenchao
     * @date: 2019/11/4 15:00
     * @Description: 根据mn号获取各个mn号监测的污染物
     * @param:
     * @return:
     * @throws:
     */
    List<Map<String,String>> getMonitorPollutantByParam(@Param("mns")List<String> mns);


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