package com.tjpu.sp.service.impl.environmentalprotection.hazardouswasteinfo;

import com.tjpu.sp.dao.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoMapper;
import com.tjpu.sp.model.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoVO;
import com.tjpu.sp.service.environmentalprotection.hazardouswasteinfo.HazardousWasteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class HazardousWasteInfoServiceImpl implements HazardousWasteInfoService {

    @Autowired
    private HazardousWasteInfoMapper hazardousWasteInfoMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return hazardousWasteInfoMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(HazardousWasteInfoVO record) {
        return hazardousWasteInfoMapper.insert(record);
    }

    @Override
    public int insertBatch(List<HazardousWasteInfoVO> record) {
        for (HazardousWasteInfoVO hazardousWasteInfoVO : record) {
             hazardousWasteInfoMapper.insert(hazardousWasteInfoVO);
        }
        return 0;
    }

    @Override
    public int addAndUpdateBatch(List<HazardousWasteInfoVO> repeat, List<HazardousWasteInfoVO> unrepeat) {
        //修改重复
        for (HazardousWasteInfoVO hazardousWasteInfoVO : repeat) {
            hazardousWasteInfoMapper.updateByParams(hazardousWasteInfoVO);
        }

        for (HazardousWasteInfoVO hazardousWasteInfoVO : unrepeat) {
            hazardousWasteInfoMapper.insert(hazardousWasteInfoVO);
        }
        return 0;
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return hazardousWasteInfoMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(HazardousWasteInfoVO record) {
        return hazardousWasteInfoMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/09/22 0016 下午 2:38
     * @Description:  通过自定义参数获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getHazardousWasteInfoByParamMap(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.getHazardousWasteInfoByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/09/22 0016 下午 2:38
     * @Description: 通过id获取危废信息详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getHazardousWasteInfoDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = hazardousWasteInfoMapper.getHazardousWasteInfoByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }


    /**
     * @author: chengzq
     * @date: 2020/9/27 0027 上午 10:33
     * @Description: 通过自定义参数获取危废信息信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countHazardousWasteDataByParamMap(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.countHazardousWasteDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/05/18 0018 上午 10:00
     * @Description: 通过自定义参数统计危废年生产、同比情况（生产、贮存、利用）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countHazardousWasteDataGroupYearByParamMap(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.countHazardousWasteDataGroupYearByParamMap(paramMap);
    }


    @Override
    public List<Map<String, Object>> countMainHazardousWasteTypeDataByParamMap(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.countMainHazardousWasteTypeDataByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/05/18 0018 下午 16:34
     * @Description: 通过自定义参数统计危险特性占比情况
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countHazardousWasteCharacteristicRatioData(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.countHazardousWasteCharacteristicRatioData(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2022/05/19 0019 上午 8:49
     * @Description: 通过自定义参数统计企业贮存危废量排名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> countEntKeepStorageHazardousWasteRankData(Map<String, Object> paramMap) {
        return hazardousWasteInfoMapper.countEntKeepStorageHazardousWasteRankData(paramMap);
    }

}
