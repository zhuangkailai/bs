package com.tjpu.sp.service.environmentalprotection.petitionlettercomplaint;


import com.tjpu.sp.model.environmentalprotection.petitionlettercomplaint.PetitionLetterComplaintVO;

import java.util.List;
import java.util.Map;

public interface PetitionLetterComplaintService {

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:根据自定义参数获取投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getPetitionLetterComplaintsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午5:25
     * @Description:新增投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(PetitionLetterComplaintVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:修改投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(PetitionLetterComplaintVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:根据主键ID删除投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:根据主键ID获取投诉案件详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getPetitionLetterComplaintDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:获取投诉案件表头数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getTableTitleForPetitionLetterComplaint();

    /**
     * @author: xsm
     * @date: 2019/10/16 0016 下午 5:25
     * @Description:根据id获取投诉案件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    PetitionLetterComplaintVO selectByPrimaryKey(String id);
    /**
    *@author: liyc
    *@date: 2019/11/5 0005 17:19
    *@Description: 通过企业id统计信访投诉信息
    *@updateUser:
    *@updateDate:
    *@updateDescription:
    *@param: [pollutionid]
    *@throws:
    **/
    List<Map<String,Object>> countLetterComplaintByPollutionId(String pollutionid);
}
