package com.tjpu.sp.dao.environmentalprotection.parkprofile;

import com.tjpu.sp.model.environmentalprotection.parkprofile.ParkProfileVO;

import java.util.List;
import java.util.Map;

public interface ParkProfileMapper {
    int deleteByPrimaryKey(String pkId);

    int insert(ParkProfileVO record);

    Map<String,Object> selectByPrimaryKey(String pkId);

    int updateByPrimaryKey(ParkProfileVO record);


    /**
     * @author: chengzq
     * @date: 2020/11/13 0016 下午 2:37
     * @Description:  通过自定义参数获取园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getParkProfileByParamMap(Map<String, Object> paramMap);

}