package com.tjpu.sp.service.impl.environmentalprotection.assess;

import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.dao.common.FileInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.assess.EntAssessmentDataMapper;
import com.tjpu.sp.dao.environmentalprotection.assess.EntAssessmentInfoMapper;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentDataVO;
import com.tjpu.sp.model.environmentalprotection.assess.EntAssessmentInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.environmentalprotection.assess.EntAssessScoreService;

import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class EntAssessScoreServiceImpl implements EntAssessScoreService {
    @Autowired
    private EntAssessmentInfoMapper entAssessmentInfoMapper;
    @Autowired
    private EntAssessmentDataMapper entAssessmentDataMapper;

    @Autowired
    private FileInfoMapper fileInfoMapper;


    /**
     * @Description: 列表查询
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 11:34
     */
    @Override
    public List<Map<String, Object>> getEntAssessInfoListByParam(JSONObject jsonObject) {
        return entAssessmentInfoMapper.getEntAssessInfoListByParam(jsonObject);
    }


    /**
     * @Description: 数据更新
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 11:34
     */
    @Override
    public void updateInfo(EntAssessmentInfoVO entAssessmentInfoVO) {
        String fkId = entAssessmentInfoVO.getPkDataid();
        //删除数据关联表
        entAssessmentDataMapper.deleteByFId(fkId);
        //设置总分
        List<EntAssessmentDataVO> entAssessmentDataVOS = entAssessmentInfoVO.getEntAssessmentDataVOS();
        if (entAssessmentDataVOS != null) {
            Double total = 0d;
            for (EntAssessmentDataVO entAssessmentDataVO : entAssessmentDataVOS) {
                total += entAssessmentDataVO.getReducescorevalue() != null ? entAssessmentDataVO.getReducescorevalue() : 0d;
                entAssessmentDataVO.setFkAssessinfoid(fkId);
            }
            entAssessmentInfoVO.setTotalreducescore(total);
            //添加数据关联表
            addRetionData(entAssessmentDataVOS);
        }
        //更新信息表
        entAssessmentInfoMapper.updateByPrimaryKey(entAssessmentInfoVO);
    }

    private void addRetionData(List<EntAssessmentDataVO> entAssessmentDataVOS) {
        for (EntAssessmentDataVO entAssessmentDataVO : entAssessmentDataVOS) {
            entAssessmentDataMapper.insert(entAssessmentDataVO);
        }
    }

    /**
     * @Description: 数据添加
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 11:34
     */
    @Override
    public void insertInfo(EntAssessmentInfoVO entAssessmentInfoVO) {
        //设置总分
        List<EntAssessmentDataVO> entAssessmentDataVOS = entAssessmentInfoVO.getEntAssessmentDataVOS();
        if (entAssessmentDataVOS != null) {
            Double total = 0d;
            String fkId = entAssessmentInfoVO.getPkDataid();
            for (EntAssessmentDataVO entAssessmentDataVO : entAssessmentDataVOS) {
                total += entAssessmentDataVO.getReducescorevalue() != null ? entAssessmentDataVO.getReducescorevalue() : 0d;
                entAssessmentDataVO.setFkAssessinfoid(fkId);
            }
            entAssessmentInfoVO.setTotalreducescore(total);
            //添加数据关联表
            addRetionData(entAssessmentDataVOS);
        }
        //更新信息表
        entAssessmentInfoMapper.insert(entAssessmentInfoVO);

    }

    /**
     * @Description: 删除信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2022/7/7 12:04
     */
    @Override
    public void deleteById(String id) {
        entAssessmentInfoMapper.deleteByPrimaryKey(id);
        entAssessmentDataMapper.deleteByFId(id);
    }

    @Override
    public List<Map<String, Object>> getAddItemDataList() {
        List<Map<String, Object>> dataList = entAssessmentInfoMapper.getAddItemDataList();
        if (dataList.size() > 0) {
            Map<String, List<Map<String, Object>>> resultMap = dataList.stream().collect(Collectors.groupingBy(m -> m.get("assesstypecode").toString() + "_" +
                    m.get("assesstypename").toString()));
            dataList.clear();
            for (String key : resultMap.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("assesstypecode", key.split("_")[0]);
                dataMap.put("assesstypename", key.split("_")[1]);
                dataMap.put("itemdatalist", resultMap.get(key));
                dataList.add(dataMap);
            }
            //排序
            dataList = dataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("assesstypecode").toString()))).collect(Collectors.toList());
        }
        return dataList;
    }

    @Override
    public Map<String, Object> getEditOrViewDataById(String id) {
        Map<String, Object> resultMap = new HashMap<>();
        //获取检查信息
        EntAssessmentInfoVO entAssessmentInfoVO = entAssessmentInfoMapper.selectByPrimaryKey(id);
        resultMap.put("checktime",DataFormatUtil.getDateYMD(entAssessmentInfoVO.getChecktime()));
        resultMap.put("checkpeople", entAssessmentInfoVO.getCheckpeople());
        List<Map<String,Object>> checkDataList = entAssessmentDataMapper.getCheckDataListByFId(id);
        if (checkDataList.size()>0){
            Map<String, List<Map<String, Object>>> itemMap = checkDataList.stream().collect(Collectors.groupingBy(m -> m.get("assesstypecode").toString() + "_" +
                    m.get("assesstypename").toString()));
            List<String> fileIds = checkDataList.stream().filter(m ->m.get("fkfileid")!=null&&!"".equals(m.get("fkfileid")))
                    .map(m ->m.get("fkfileid").toString())
                    .collect(Collectors.toList());


            Map<String, List<FileInfoVO>> idAndObj;
            if (fileIds.size()>0){
                Map<String,Object> paramMap = new HashMap<>();
                paramMap.put("fileflags",fileIds);
                List<FileInfoVO> fileList = fileInfoMapper.getFilesInfosByParam(paramMap);
                idAndObj = fileList.stream().collect(Collectors.groupingBy(FileInfoVO::getFileflag));
            }else {
                idAndObj = new HashMap<>();
            }
            checkDataList.clear();
            List<Map<String, Object>> itemdatalist;
            for (String key : itemMap.keySet()) {
                Map<String, Object> dataMap = new HashMap<>();
                dataMap.put("assesstypecode", key.split("_")[0]);
                dataMap.put("assesstypename", key.split("_")[1]);
                itemdatalist = itemMap.get(key);
                for (Map<String,Object> item:itemdatalist){
                    List<Map<String, Object>> filelist = new ArrayList<>();
                    if (idAndObj.containsKey(item.get("fkfileid"))){
                        List<FileInfoVO> fileInfoVOS = idAndObj.get(item.get("fkfileid"));
                        for (FileInfoVO fileInfoVO:fileInfoVOS){
                            Map<String,Object> map = new HashMap<>();
                            map.put("id",fileInfoVO.getPkFileid());
                            map.put("name",fileInfoVO.getOriginalfilename());
                            map.put("src",fileInfoVO.getFilepath());
                            filelist.add(map);
                        }
                    }
                    item.put("filelist",filelist);
                }
                dataMap.put("itemdatalist", itemdatalist);
                checkDataList.add(dataMap);
            }
            //排序
            checkDataList = checkDataList.stream().sorted(Comparator.comparing(m -> Integer.valueOf(((Map) m).get("assesstypecode").toString()))).collect(Collectors.toList());
        }
        resultMap.put("checkdatalist",checkDataList);
        return resultMap;
    }
}
