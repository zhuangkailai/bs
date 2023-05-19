package com.tjpu.sp.service.impl.environmentalprotection.petitionlettercomplaint;

import com.tjpu.sp.dao.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintMapper;
import com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintVO;
import com.tjpu.sp.service.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PetitionLetterComplaintServiceImpl implements PetitionLetterComplaintService {
    @Autowired
    private PetitionLetterComplaintMapper petitionLetterComplaintMapper;

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 1:38
     * @Description:根据自定义参数获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getPetitionLetterComplaintsByParamMap(Map<String, Object> paramMap) {
        return petitionLetterComplaintMapper.getPetitionLetterComplaintsByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:19
     * @Description:新增执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(PetitionLetterComplaintVO obj) {
        petitionLetterComplaintMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:25
     * @Description:修改执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(PetitionLetterComplaintVO obj) {
        petitionLetterComplaintMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:27
     * @Description:根据主键ID删除执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        petitionLetterComplaintMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 2:30
     * @Description:根据主键ID获取执法任务详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getPetitionLetterComplaintDetailByID(String pkid) {
        return petitionLetterComplaintMapper.getPetitionLetterComplaintDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:获取执法任务表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getTableTitleForPetitionLetterComplaint() {
        //基本信息表头
        List<Map<String, Object>> tableTitleData = new ArrayList<>();
        String[] titlename = new String[]{"事件标题", "举报对象", "举报时间", "行政区划", "缓急程度", "举报方式", "投诉类型"};
        String[] titlefiled = new String[]{"eventtitle", "petitionobject", "petitiontime", "regionname", "enerlvlname", "petitionkindname", "petitiontypename"};
        for (int i = 0; i < titlefiled.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("minwidth", "180px");
            map.put("headeralign", "center");
            map.put("fixed", "left");
            map.put("showhide", true);
            map.put("prop", titlefiled[i]);
            map.put("label", titlename[i]);
            map.put("align", "center");
            tableTitleData.add(map);
        }
        return tableTitleData;
    }

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 4:00
     * @Description:根据id获取执法任务信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public PetitionLetterComplaintVO selectByPrimaryKey(String id) {
        return petitionLetterComplaintMapper.selectByPrimaryKey(id);
    }
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 17:20
    *@Description: 通过企业id统计信访投诉信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    @Override
    public List<Map<String, Object>> countLetterComplaintByPollutionId(String pollutionid) {
        return petitionLetterComplaintMapper.countLetterComplaintByPollutionId(pollutionid);
    }
}
