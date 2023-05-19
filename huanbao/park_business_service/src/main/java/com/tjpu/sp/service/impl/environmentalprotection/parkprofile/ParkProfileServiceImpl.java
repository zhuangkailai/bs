package com.tjpu.sp.service.impl.environmentalprotection.parkprofile;

import com.tjpu.sp.dao.environmentalprotection.parkprofile.ParkProfileMapper;
import com.tjpu.sp.model.environmentalprotection.parkprofile.ParkProfileVO;
import com.tjpu.sp.service.environmentalprotection.parkprofile.ParkProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
@Transactional
public class ParkProfileServiceImpl implements ParkProfileService {

    @Autowired
    private ParkProfileMapper parkprofileMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        return parkprofileMapper.deleteByPrimaryKey(pkId);
    }

    @Override
    public int insert(ParkProfileVO record) {
        return parkprofileMapper.insert(record);
    }

    @Override
    public Map<String,Object> selectByPrimaryKey(String pkId) {
        return parkprofileMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int updateByPrimaryKey(ParkProfileVO record) {
        return parkprofileMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2020/11/13 0016 下午 2:38
     * @Description:  通过自定义参数获取园区概况信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getParkProfileByParamMap(Map<String, Object> paramMap) {
        return parkprofileMapper.getParkProfileByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/11/13 0016 下午 2:38
     * @Description: 通过id获取园区概况详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String,Object> getParkProfileDetailByID(String pkid) {
        Map<String,Object> paramMap=new HashMap<>();
        paramMap.put("pkid",pkid);
        Map<String,Object> detailInfo = parkprofileMapper.getParkProfileByParamMap(paramMap).stream().findFirst().orElse(new HashMap<>());
        return detailInfo;
    }

}
