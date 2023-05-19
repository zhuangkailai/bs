package com.tjpu.sp.service.impl.environmentalprotection.particularpollutants;

import com.tjpu.sp.dao.environmentalprotection.particularpollutants.ParticularPollutantsMapper;
import com.tjpu.sp.model.environmentalprotection.particularpollutants.ParticularPollutantsVO;
import com.tjpu.sp.service.environmentalprotection.particularpollutants.ParticularPollutantsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticularPollutantsServiceImpl implements ParticularPollutantsService {

    @Autowired
    private ParticularPollutantsMapper particularPollutantsMapper;


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
    @Override
    public List<Map<String, Object>> getParticularPollutantsByParamMap(Map<String, Object> paramMap) {
        return particularPollutantsMapper.getParticularPollutantsByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 4:02
     * @Description: 新增特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    @Override
    public int insertParticularPollutants(List<ParticularPollutantsVO> list) {
        int subLenth = 100;

        for (int i = 0; i < list.size(); i += subLenth) {
            List<ParticularPollutantsVO> collect = list.stream().skip(i).limit(subLenth).collect(Collectors.toList());
            particularPollutantsMapper.insertParticularPollutants(collect);
        }
        return 0;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 6:08
     * @Description: 通过id查询特征污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> selectParticularPollutantsById(Map<String, Object> paramMap) {
        return particularPollutantsMapper.selectParticularPollutantsById(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/13 0013 下午 6:53
     * @Description: 修改污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [list]
     * @throws:
     */
    @Override
    public int updateParticularPollutants(List<ParticularPollutantsVO> list) {
        Map<String, Object> paramMap = new HashMap<>();
        List<String> pollutionids = list.stream().map(m -> m.getFkPollutionid()).collect(Collectors.toList());
        List<String> outputids = list.stream().map(m -> m.getFkOutputid()).collect(Collectors.toList());
        if (list.size() > 0) {
            //先删除再新增
            ParticularPollutantsVO particularPollutantsVO = list.get(0);
            String version = particularPollutantsVO.getVersion();
            String fkmonitorpointtypecode = particularPollutantsVO.getFkMonitorpointtypecode();
//            paramMap.put("pollutionids",pollutionids);
//            paramMap.put("outputids",outputids);
            paramMap.put("version", version);
            paramMap.put("fkmonitorpointtypecode", fkmonitorpointtypecode);

            particularPollutantsMapper.deleteByParams(paramMap);

            //新增
            int subLenth = 100;

            for (int i = 0; i < list.size(); i += subLenth) {
                List<ParticularPollutantsVO> collect = list.stream().skip(i).limit(subLenth).collect(Collectors.toList());
                particularPollutantsMapper.insertParticularPollutants(collect);
            }
        }
        return 0;
    }


    /**
     * @author: chengzq
     * @date: 2019/8/13 0013 下午 4:30
     * @Description: 修改单条记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public void updateByPrimaryKey(ParticularPollutantsVO record) {
        particularPollutantsMapper.updateByPrimaryKey(record);

    }




    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:29
     * @Description: 通过id删除污染物库
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public int deleteParticularPollutantsById(String id) {
        return particularPollutantsMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:38
     * @Description: 通过id查询排放口特征污染物库详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public Map<String, Object> getParticularPollutantsDetailByID(String id) {
        return particularPollutantsMapper.getParticularPollutantsDetailByID(id);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/14 0014 上午 9:51
     * @Description: 获取最新版本号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public String getLastVersion() {
        return particularPollutantsMapper.getLastVersion();
    }

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
    @Override
    public List<Map<String, Object>> countPollutionForPollutant(Map<String, Object> paramMap) {
        return particularPollutantsMapper.countPollutionForPollutant(paramMap);
    }

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
    @Override
    public List<Map<String, Object>> getPollutionListDataByParamMap(Map<String, Object> paramMap) {
        return particularPollutantsMapper.getPollutionListDataByParamMap(paramMap);
    }
}
