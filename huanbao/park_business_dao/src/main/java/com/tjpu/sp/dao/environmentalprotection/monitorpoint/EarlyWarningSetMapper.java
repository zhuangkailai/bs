package com.tjpu.sp.dao.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorpoint.EarlyWarningSetVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
@Repository
public interface EarlyWarningSetMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(EarlyWarningSetVO record);

    int insertSelective(EarlyWarningSetVO record);

    EarlyWarningSetVO selectByPrimaryKey(String pkId);

    int updateByPrimaryKeySelective(EarlyWarningSetVO record);

    int updateByPrimaryKey(EarlyWarningSetVO record);

    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 6:09
     * @Description: 批量新增报警关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    int insertSelectives(List<EarlyWarningSetVO> list);



    /**
     * @author: chengzq
     * @date: 2019/5/27 0027 下午 7:13
     * @Description: 通过排口id删除报警关联数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [outputid]
     * @throws:
     */
    int deleteByOutPutID(Map<String,Object> paramMap);

    List<Map<String, Object>> getMonitorPointDataTimeSetListByParam(Map<String, Object> paramMap);

    List<Map<String, Object>> getTimeDataSetByParam(Map<String, Object> paramMap);
}