package com.tjpu.sp.dao.environmentalprotection.particularpollutants;

import com.tjpu.sp.model.environmentalprotection.particularpollutants.ParticularPollutantsVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ParticularPollutantsMapper {
    int deleteByPrimaryKey(String pkDataid);

    int insert(ParticularPollutantsVO record);

    int insertSelective(ParticularPollutantsVO record);

    ParticularPollutantsVO selectByPrimaryKey(String pkDataid);

    int updateByPrimaryKeySelective(ParticularPollutantsVO record);

    int updateByPrimaryKey(ParticularPollutantsVO record);

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 2:41
     * @Description: 通过污染源名称，排口名称，污染物名称，监测点类型，版本号查询污染物库信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getParticularPollutantsByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 4:01
     * @Description: 新增特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    int insertParticularPollutants(List<ParticularPollutantsVO> list);


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 6:07
     * @Description: 通过id查询特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    List<Map<String, Object>> selectParticularPollutantsById(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 6:52
     * @Description: 修改特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    int updateParticularPollutants(List<ParticularPollutantsVO> list);


    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:37
     * @Description: 通过id获取详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    Map<String, Object> getParticularPollutantsDetailByID(String id);

    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:50
     * @Description:获取最新版本号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    String getLastVersion();


    /**
     * @author: chengzq
     * @date: 2019/6/15 0015 下午 3:16
     * @Description: 通过污染源id，排口id，版本号删除污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    int deleteByParams(Map<String, Object> paramMap);

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 11:24
     * @Description: 自定义查询条件获取最新版本特征污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getLastVersionPollutantInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @param paramMap
     * @author: lip
     * @date: 2019/8/8 0008 下午 1:40
     * @Description: 根据特征污染物统计企业数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> countPollutionForPollutant(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/8/ 0009 下午 1:59
     * @Description: 根据监测点类型和特征污染物查询企业信息列表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPollutionListDataByParamMap(Map<String, Object> paramMap);
}