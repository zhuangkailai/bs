package com.tjpu.sp.service.impl.environmentalprotection.licence;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.envhousekeepers.GasTreatmentFacilityMapper;
import com.tjpu.sp.dao.envhousekeepers.WaterTreatmentFacilityMapper;
import com.tjpu.sp.dao.environmentalprotection.licence.LicenceInfoMapper;
import com.tjpu.sp.service.environmentalprotection.licence.LicenceService;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class LicenceServiceImpl implements LicenceService {

    private final LicenceInfoMapper licenceInfoMapper;
    private final GasTreatmentFacilityMapper gasTreatmentFacilityMapper;
    private final WaterTreatmentFacilityMapper waterTreatmentFacilityMapper;

    @Autowired
    public LicenceServiceImpl(LicenceInfoMapper licenceInfoMapper, GasTreatmentFacilityMapper gasTreatmentFacilityMapper, WaterTreatmentFacilityMapper waterTreatmentFacilityMapper) {
        this.licenceInfoMapper = licenceInfoMapper;
        this.gasTreatmentFacilityMapper = gasTreatmentFacilityMapper;
        this.waterTreatmentFacilityMapper = waterTreatmentFacilityMapper;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/30 9:10
     * @Description: 获取过期排污许可证个数
     * @param:
     * @return:
     */
    @Override
    public Integer countOverdueLicenceNum() {
        return licenceInfoMapper.countOverdueLicenceNum();
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 6:09
     * @Description: 获取排污许可证表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return: 污染源名称、许可证编号、有效开始日期、有效截止日期、发证日期、发证单位
     */
    @Override
    public List<Map<String, Object>> getOverdueLicenceTableTitleData() {
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        tableTitleData.add(getSingleTitle("shortername", "企业名称", "150px"));
        tableTitleData.add(getSingleTitle("licencenum", "许可证编号", ""));
        tableTitleData.add(getSingleTitle("licencestartdate", "有效开始日期", ""));
        tableTitleData.add(getSingleTitle("licenceenddate", "有效截止日期", ""));
        tableTitleData.add(getSingleTitle("licenceissuedate", "发证日期", ""));
        tableTitleData.add(getSingleTitle("issueunit", "发证单位", ""));
        return tableTitleData;
    }

    /**
     * @author: lip
     * @date: 2019/6/17 0017 下午 6:14
     * @Description: 获取过期许可证表格内容数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public PageInfo<Map<String, Object>> getOverdueLicenceTableListDataByParamMap(Map<String, Object> paramMap) {
        Integer pageSize = paramMap.get("pagesize") != null ? Integer.parseInt(paramMap.get("pagesize").toString()) : 20;
        Integer pageNum = paramMap.get("pagenum") != null ? Integer.parseInt(paramMap.get("pagenum").toString()) : 1;
        if (pageSize != null && pageNum != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        paramMap.put("licenceenddate", DataFormatUtil.getDateYMDHMS(new Date()));
        List<Map<String, Object>> listData = licenceInfoMapper.getPWLicenceListDataByParamMap(paramMap);
        return new PageInfo<>(listData);
    }

    @Override
    public List<Map<String, Object>> getWaterOutPutDataListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getWaterOutPutDataListByParam(paramMap);
    }
    @Override
    public List<Map<String, Object>> getGasFacilityDataListByParam(Map<String, Object> paramMap) {
        return gasTreatmentFacilityMapper.getGasFacilityDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getWaterFacilityDataListByParam(Map<String, Object> paramMap) {
        return waterTreatmentFacilityMapper.getWaterFacilityDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasUnOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasUnOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> PWOutOverListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.PWOutOverListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getFacilityExceptionDataListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getFacilityExceptionDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasQOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasQOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasUnQOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasUnQOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getWaterOutPutQDataListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getWaterOutPutQDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasYOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasYOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> gasUnYOutListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.gasUnYOutListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getWaterOutPutYDataListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getWaterOutPutYDataListByParam(paramMap);
    }

    @Override
    public PageInfo<Map<String, Object>> getEntStandingInfoByParam(JSONObject jsonObject) {

        Integer pageSize = jsonObject.get("pagesize") != null ? Integer.parseInt(jsonObject.get("pagesize").toString()) : 20;
        Integer pageNum = jsonObject.get("pagenum") != null ? Integer.parseInt(jsonObject.get("pagenum").toString()) : 1;
        if (pageSize != null && pageNum != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> listData = licenceInfoMapper.getEntStandingInfoByParam(jsonObject);
        return new PageInfo<>(listData);
    }

    @Override
    public List<Map<String, Object>> getInOrOutMenuDataListByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getInOrOutMenuDataListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getInOrOutAllMenuData(Map<String, Object> paramMap) {
        return licenceInfoMapper.getInOrOutAllMenuData(paramMap);
    }

    @Override
    public void setInOrOutMenuData(List<Map<String, Object>> dataList) {
        licenceInfoMapper.deleteInOrOutMenuData();
        licenceInfoMapper.batchInsert(dataList);
    }

    @Override
    public List<Map<String, Object>> getPWLicenceListDataByParamMap(Map<String, Object> paramMap) {
        return licenceInfoMapper.getPWLicenceListDataByParamMap(paramMap);
    }
    
    /**
     * @Description: 信息公开
     * @Param:  
     * @return:  
     * @Author: lip
     * @Date: 2022/8/12 9:06
     */ 
    @Override
    public List<Map<String, Object>> getInfoOpenByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getInfoOpenByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getStandingBookRequireByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getStandingBookRequireByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getNoiseOutputInfoByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getNoiseOutputInfoByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getCorrectProvideByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getCorrectProvideByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getOtherTextRequireByParam(Map<String, Object> paramMap) {
        return licenceInfoMapper.getOtherTextRequireByParam(paramMap);
    }

    @Override
    public PageInfo<Map<String, Object>> getLastDataListByParam(Map<String, Object> paramMap) {


        if (paramMap.get("pagesize")!=null&&paramMap.get("pagenum")!=null) {
            Integer pageSize =Integer.parseInt( paramMap.get("pagesize").toString());
            Integer pageNum =Integer.parseInt( paramMap.get("pagenum").toString());
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Map<String, Object>> listData = licenceInfoMapper.getLastDataListByParam(paramMap);
        return new PageInfo<>(listData);
    }


    private Map<String, Object> getSingleTitle(String propValue, String labelValue, String minWidth) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isNotBlank(minWidth)) {
            map.put("minwidth", minWidth);
        }
        map.put("headeralign", "center");
        map.put("showhide", true);
        map.put("prop", propValue);
        map.put("label", labelValue);
        map.put("align", "center");
        return map;
    }

}
