package com.tjpu.sp.service.environmentalprotection.dangerwaste;


import com.tjpu.sp.model.environmentalprotection.dangerwaste.DangerWasteLicenceInfoVO;

import java.util.List;
import java.util.Map;

public interface DangerWasteLicenceInfoService {

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:根据自定义参数获取危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getDangerWasteLicenceInfosByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:新增危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(DangerWasteLicenceInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:修改危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(DangerWasteLicenceInfoVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:根据主键ID删除危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:根据主键ID获取危废许可证详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getDangerWasteLicenceInfoDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:32
     * @Description:根据id获取危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    DangerWasteLicenceInfoVO selectByPrimaryKey(String id);
}
