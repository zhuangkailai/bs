package com.tjpu.sp.service.impl.environmentalprotection.pointofflinerecord;

import com.tjpu.sp.dao.environmentalprotection.pointofflinerecord.PointOffLineRecordMapper;
import com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintVO;
import com.tjpu.sp.model.environmentalprotection.pointofflinerecord.PointOffLineRecordVO;
import com.tjpu.sp.service.environmentalprotection.pointofflinerecord.PointOffLineRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PointOffLineRecordServiceImpl implements PointOffLineRecordService {
    @Autowired
    private PointOffLineRecordMapper pointOffLineRecordMapper;


    @Override
    public List<Map<String, Object>> getPointOffLineRecordsByParamMap(Map<String, Object> paramMap) {
        return pointOffLineRecordMapper.getPointOffLineRecordsByParamMap(paramMap);
    }

    @Override
    public PointOffLineRecordVO getPointOffLineRecordInfoById(String id) {
        return pointOffLineRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public int insert(PointOffLineRecordVO obj) {
        return pointOffLineRecordMapper.insert(obj);
    }

    @Override
    public void update(PointOffLineRecordVO obj) {
        pointOffLineRecordMapper.updateByPrimaryKey(obj);
    }

    @Override
    public Map<String, Object> getPointOffLineRecordDetailById(Map<String, Object> paramMap) {
        return pointOffLineRecordMapper.getPointOffLineRecordDetailById(paramMap);
    }

    @Override
    public List<Map<String, Object>> getNowPointOffLineRecordsByParamMap(Map<String, Object> parammap) {
        return pointOffLineRecordMapper.getNowPointOffLineRecordsByParamMap(parammap);
    }

    @Override
    public List<Map<String, Object>> getEntPointOffLineRecordsByParamMap(Map<String, Object> parammap) {
        return pointOffLineRecordMapper.getEntPointOffLineRecordsByParamMap(parammap);
    }
}
