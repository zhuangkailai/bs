package com.tjpu.sp.service.base.pollution;


import com.tjpu.sp.model.base.pollution.PollutionLabelDataVO;

import java.util.List;
public interface PollutionLabelDataService {


    int insertLabels(List<PollutionLabelDataVO> records);
}
