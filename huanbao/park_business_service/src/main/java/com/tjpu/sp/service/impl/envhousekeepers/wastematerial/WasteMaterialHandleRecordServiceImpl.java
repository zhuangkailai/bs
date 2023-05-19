package com.tjpu.sp.service.impl.envhousekeepers.wastematerial;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.dao.envhousekeepers.wastematerial.WasteMaterialHandleRecordMapper;
import com.tjpu.sp.model.envhousekeepers.wastematerial.WasteMaterialHandleRecordVO;
import com.tjpu.sp.service.envhousekeepers.wastematerial.WasteMaterialHandleRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class WasteMaterialHandleRecordServiceImpl implements WasteMaterialHandleRecordService {

    @Autowired
    private WasteMaterialHandleRecordMapper wasteMaterialHandleRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:11
     * @Description: 通过自定义参数查询危废处置记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWasteMaterialHandleRecordByParamMap(Map<String,Object> paramMap) {
        return wasteMaterialHandleRecordMapper.getWasteMaterialHandleRecordByParamMap(paramMap);
    }

    @Override
    public void insert(WasteMaterialHandleRecordVO entity) {
        wasteMaterialHandleRecordMapper.insert(entity);
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String id) {
        return wasteMaterialHandleRecordMapper.getWasteMaterialHandleRecordDetailByID(id);
    }

    @Override
    public void updateByPrimaryKey(WasteMaterialHandleRecordVO entity) {
        wasteMaterialHandleRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        wasteMaterialHandleRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getWasteMaterialHandleRecordDetailByID(String id) {
       return wasteMaterialHandleRecordMapper.getWasteMaterialHandleRecordDetailByID(id);
    }

    @Override
    public void deleteHandleRecordByEntIDAndCode(Map<String, Object> param) {
        wasteMaterialHandleRecordMapper.deleteHandleRecordByEntIDAndCode(param);
    }

    @Override
    public String WasteMaterialTitleNameByParam(Map<String, Object> paramMap) {
        String titlename = "";
        Map<String,Object> map = wasteMaterialHandleRecordMapper.WasteMaterialTitleNameByParam(paramMap);
        if (map!=null){
            String poname = map.get("ShorterName")!=null?map.get("ShorterName").toString():(map.get("PollutionName")!=null?map.get("PollutionName").toString():"");
            titlename = poname+"危险废物(危废名称:"+(map.get("wastematerialname")!=null?map.get("wastematerialname").toString():"-")+")产生、贮存、处置/利用情况台账";
        }
        return titlename;
    }

    @Override
    public List<Map<String, Object>> getWasteMaterialTreeByParam(Map<String, Object> param) {
        //获取该企业所有记录的危险废物
        List<Map<String, Object>> listdata = wasteMaterialHandleRecordMapper.getWasteMaterialTreeByParam(param);
        List<Map<String, Object>> treedata = new ArrayList<>();
        if (listdata!=null&&listdata.size() > 0) {
            Map<String, Object> parentmap = new HashMap<>();
            parentmap.put("id", "wxfw");
            parentmap.put("label", "危险废物");
            parentmap.put("type", "parent");
            parentmap.put("children", listdata);
            treedata.add(parentmap);
        }
        return treedata;
    }
}
