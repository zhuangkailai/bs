package com.tjpu.sp.service.impl.environmentalprotection.parkinfo.parkintroduce;

import com.tjpu.sp.dao.environmentalprotection.parkinfo.parkintroduce.ParkIntroduceMapper;
import com.tjpu.sp.model.base.parkintroduce.ParkIntroduceVO;
import com.tjpu.sp.service.environmentalprotection.parkinfo.parkintroduce.ParkIntroduceService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ParkIntroduceServiceImpl implements ParkIntroduceService {
    private final ParkIntroduceMapper parkIntroduceMapper;

    public ParkIntroduceServiceImpl(ParkIntroduceMapper parkIntroduceMapper) {
        this.parkIntroduceMapper = parkIntroduceMapper;
    }


    @Override
    public int insert(ParkIntroduceVO record) {
        return parkIntroduceMapper.insert(record);
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 14:17
     * @Description: 获取最新一条园区介绍信息
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getLastParkIntroduceInfo() {
        return parkIntroduceMapper.getLastParkIntroduceInfo();
    }
}
