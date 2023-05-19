package com.tjpu.sp.service.impl.base.pollution;

import com.tjpu.sp.dao.base.pollution.PollutionLabelDataMapper;
import com.tjpu.sp.model.base.pollution.PollutionLabelDataVO;
import com.tjpu.sp.service.base.pollution.PollutionLabelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PollutionLabelDataServiceImpl implements PollutionLabelDataService {

    @Autowired
    private PollutionLabelDataMapper pollutionLabelDataMapper;


    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 11:57
     * @Description: 通过标签实体批量新增污染源标签
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insertLabels(List<PollutionLabelDataVO> records) {
        return pollutionLabelDataMapper.insertLabels(records);
    }



}
