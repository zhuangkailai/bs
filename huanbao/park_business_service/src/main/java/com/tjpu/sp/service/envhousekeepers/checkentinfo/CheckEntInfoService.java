package com.tjpu.sp.service.envhousekeepers.checkentinfo;

import com.tjpu.sp.model.envhousekeepers.checkcontentdescription.CheckContentDescriptionVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.CheckEntInfoVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.EntCheckFeedbackRecordVO;
import com.tjpu.sp.model.envhousekeepers.checkitemdata.CheckItemDataVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;

import java.util.List;
import java.util.Map;

public interface CheckEntInfoService {


    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 添加检查企业及检查项目数据
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     **/
    void insert(CheckEntInfoVO obj, List<CheckItemDataVO> listobj, List<CheckProblemExpoundVO> remarkobjs, List<CheckContentDescriptionVO> contentobjs);

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 15:59
     *@Description: 获取所有检查企业数据且按企业和检查日期分组
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     *
     * @param param*/
    List<Map<String,Object>> getAllCheckEntInfoGroupByEntAndData(Map<String, Object> param);

    /**
     *@author: xsm
     *@date: 2021/06/30 0030 11:59
     *@Description: 修改检查项目数据
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     **/
    void updateCheckItemData(CheckEntInfoVO obj, List<CheckItemDataVO> listobj,List<CheckProblemExpoundVO> remarkobjs, List<CheckContentDescriptionVO> contentobjs);

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查记录是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    List<Map<String,Object>> IsCheckEntInfoValidByParam(Map<String, Object> paramMap);


    /**
     * @Description: 获取待提交记录信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/8/31 14:42
     */
    List<Map<String, Object>> getSubmitListData(Map<String, Object> jsonObject);

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 下午 4:20
     * @Description: 添加企业反馈信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    int insertEntCheckFeedbackRecord(EntCheckFeedbackRecordVO obj);

    Map<String,Object> getEntCheckFeedbackRecordDetailByParam(Map<String, Object> param);

    List<Map<String,Object>> getEntCheckFeedbackRecordDataByParam(Map<String, Object> parammap);

    List<Map<String,Object>> getEntCheckFeedbackTreeDataByParam(Map<String, Object> param);

    int updateEntCheckFeedbackRecord(EntCheckFeedbackRecordVO obj);

    List<Map<String, Object>> getFeedbackDataListByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> getEntCheckSubmitDataByParam(Map<String, Object> parammap);

    Map<String,Object> getOneCheckEntDataByParam(Map<String, Object> param);

    /**
     * @author: mmt
     * @date: 2022/08/17
     * @Description: APP检查企业列表
     * @updateUser:mmt
     * @updateDate:2022/08/17
     * @return:
     */
    List<Map<String,Object>> getAllCheckEntInfoList(Map<String, Object> param);
}
