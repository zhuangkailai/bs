package com.tjpu.sp.service.envhousekeepers.enterpriseportal;

import java.util.List;
import java.util.Map;

public interface EnterprisePortalService {

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID获取该企业的企业档案信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    List<Map<String,Object>> getEnterpriseArchivesDataByPollutionID(String pollutionid);

    /**
     * @author: xsm
     * @date: 2021/08/16 0016 上午 10:16
     * @Description: 根据企业ID获取该企业的最新动态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    List<Map<String,Object>> getEntNewDynamicDataByPollutionID( Map<String,Object> param);

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 2:13
     * @Description: 根据企业ID获取该企业的最新台账记录信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    List<Map<String,Object>> getEntNewStandingBookDataByPollutionID(Map<String, Object> param);

    Map<String,Object> getOneEntEarlyOverOrExceptionListDataByParams(Map<String, Object> paramMap);

    Integer countOneEntEarlyOverOrExceptionDataByParams(Map<String, Object> paramMap);

    Map<String,Object> getOneEntConcentrationChangeDataByParams(Map<String, Object> paramMap);
}
