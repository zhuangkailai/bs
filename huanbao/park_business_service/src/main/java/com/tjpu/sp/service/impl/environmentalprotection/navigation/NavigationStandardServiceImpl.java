package com.tjpu.sp.service.impl.environmentalprotection.navigation;

import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.environmentalprotection.navigation.NavigationStandardMapper;
import com.tjpu.sp.model.environmentalprotection.navigation.NavigationStandardVO;
import com.tjpu.sp.service.environmentalprotection.navigation.NavigationStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class NavigationStandardServiceImpl implements NavigationStandardService {
    @Autowired
    private NavigationStandardMapper navigationStandardMapper;

    @Override
    public List<Map<String, Object>> getNavigationStandardsByParamMap(Map<String, Object> paramMap) {
        return navigationStandardMapper.getNavigationStandardsByParamMap(paramMap);
    }

    @Override
    public void insert(NavigationStandardVO NavigationStandardVO) {
        navigationStandardMapper.insert(NavigationStandardVO);
    }

    @Override
    public NavigationStandardVO selectByPrimaryKey(String id) {
        return navigationStandardMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKey(NavigationStandardVO NavigationStandardVO) {
        navigationStandardMapper.updateByPrimaryKey(NavigationStandardVO);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        navigationStandardMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getNavigationStandardDetailByID(String id) {
        return navigationStandardMapper.getNavigationStandardDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getNavigationStandardDataGroupByCategory(Map<String, Object> paramMap) {
        List<Map<String, Object>> allpollutant = navigationStandardMapper.getAllNavigationPollutantData(paramMap);
        List<Map<String, Object>> result = new ArrayList<>();
        if (allpollutant.size()>0){
            //通过污染类别分组
            Map<String, List<Map<String, Object>>> onemap = allpollutant.stream().collect(Collectors.groupingBy(m -> m.get("PollutantCategory").toString()));
            for (Map.Entry<String, List<Map<String, Object>>> entry : onemap.entrySet()) {
                String PollutantCategory = entry.getKey();
                String name =  CommonTypeEnum.NavigationPollutantCategoryEnum.getNameByCode(Integer.valueOf(PollutantCategory));
                Map<String, Object> resultmap = new HashMap<>();
                resultmap.put("categorycode", PollutantCategory);
                resultmap.put("categoryname", name);
                resultmap.put("pollutantlist", entry.getValue());
                result.add(resultmap);
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllLevelNavigationStandardData() {
        return navigationStandardMapper.getAllLevelNavigationStandardData();
    }

    @Override
    public List<Map<String, Object>> getStandardColorDataByParamMap(Map<String, Object> paramMap) {
        return navigationStandardMapper.getStandardColorDataByParamMap(paramMap);
    }

    @Override
    public Integer CountStandardColorInfoByParamMap(Map<String, Object> paramMap) {
        return navigationStandardMapper.CountStandardColorInfoByParamMap(paramMap);
    }
}
