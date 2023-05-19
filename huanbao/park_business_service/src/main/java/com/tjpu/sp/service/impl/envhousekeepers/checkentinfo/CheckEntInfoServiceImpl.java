package com.tjpu.sp.service.impl.envhousekeepers.checkentinfo;

import com.tjpu.sp.dao.envhousekeepers.checkcontentdescription.CheckContentDescriptionMapper;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.CheckEntInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkentinfo.EntCheckFeedbackRecordMapper;
import com.tjpu.sp.dao.envhousekeepers.checkitemdata.CheckItemDataMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.model.envhousekeepers.checkcontentdescription.CheckContentDescriptionVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.CheckEntInfoVO;
import com.tjpu.sp.model.envhousekeepers.checkentinfo.EntCheckFeedbackRecordVO;
import com.tjpu.sp.model.envhousekeepers.checkitemdata.CheckItemDataVO;
import com.tjpu.sp.model.envhousekeepers.checkproblemexpound.CheckProblemExpoundVO;
import com.tjpu.sp.service.envhousekeepers.checkentinfo.CheckEntInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CheckEntInfoServiceImpl implements CheckEntInfoService {
    @Autowired
    private CheckEntInfoMapper checkEntInfoMapper;
    @Autowired
    private CheckItemDataMapper checkItemDataMapper;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    @Autowired
    private CheckContentDescriptionMapper checkContentDescriptionMapper;
    @Autowired
    private EntCheckFeedbackRecordMapper entCheckFeedbackRecordMapper;

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 添加检查模板配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     **/
    @Override
    public void insert(CheckEntInfoVO obj, List<CheckItemDataVO> listobj,List<CheckProblemExpoundVO> remarkobjs, List<CheckContentDescriptionVO> contentobjs) {
        checkEntInfoMapper.insert(obj);
        if (listobj.size()>0){
            checkItemDataMapper.batchInsert(listobj);
            if (remarkobjs.size()>0){
                checkProblemExpoundMapper.batchInsert(remarkobjs);
            }
            if (contentobjs.size()>0){
                checkContentDescriptionMapper.batchInsert(contentobjs);
            }
        }
    }

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
    @Override
    public List<Map<String, Object>> getAllCheckEntInfoGroupByEntAndData(Map<String, Object> param) {
        return checkEntInfoMapper.getAllCheckEntInfoGroupByEntAndData(param);
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 添加检查模板配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     **/
    @Override
    public void updateCheckItemData(CheckEntInfoVO obj, List<CheckItemDataVO> listobj,List<CheckProblemExpoundVO> remarkobjs, List<CheckContentDescriptionVO> contentobjs) {
        //删除问题记录
        checkProblemExpoundMapper.deleteCheckProblemExpoundByCheckEntID(obj.getPkId());
        //删除检查内容说明记录
        checkContentDescriptionMapper.deleteCheckContentDescriptionByCheckEntID(obj.getPkId());
        //删除检查记录
        checkItemDataMapper.deleteByCheckEntInfoID(obj.getPkId());
        //删除检查企业
        checkEntInfoMapper.deleteByPrimaryKey(obj.getPkId());
        checkEntInfoMapper.insert(obj);
        if (listobj.size()>0){
            checkItemDataMapper.batchInsert(listobj);
            if (remarkobjs.size()>0){
                checkProblemExpoundMapper.batchInsert(remarkobjs);
            }
            if (contentobjs.size()>0){
                checkContentDescriptionMapper.batchInsert(contentobjs);
            }
        }
    }

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
    @Override
    public List<Map<String, Object>> IsCheckEntInfoValidByParam(Map<String, Object> paramMap) {
        return checkEntInfoMapper.IsCheckEntInfoValidByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getSubmitListData(Map<String, Object> jsonObject) {
        return checkEntInfoMapper.getSubmitListData(jsonObject);
    }

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
    @Override
    public int insertEntCheckFeedbackRecord(EntCheckFeedbackRecordVO obj) {
        try {
            int i = entCheckFeedbackRecordMapper.insert(obj);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public Map<String, Object> getEntCheckFeedbackRecordDetailByParam(Map<String, Object> param) {
        return entCheckFeedbackRecordMapper.getEntCheckFeedbackRecordDetailByParam(param);
    }

    @Override
    public List<Map<String, Object>> getEntCheckFeedbackRecordDataByParam(Map<String, Object> parammap) {
        return entCheckFeedbackRecordMapper.getEntCheckFeedbackRecordDataByParam(parammap);
    }

    @Override
    public List<Map<String, Object>> getEntCheckFeedbackTreeDataByParam(Map<String, Object> param) {
        return checkEntInfoMapper.getEntCheckFeedbackTreeDataByParam(param);
    }

    @Override
    public int updateEntCheckFeedbackRecord(EntCheckFeedbackRecordVO obj) {
        try {
            int i = entCheckFeedbackRecordMapper.updateByPrimaryKey(obj);
            return i;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public List<Map<String, Object>> getFeedbackDataListByParam(Map<String, Object> paramMap) {
        return checkEntInfoMapper.getFeedbackDataListByParam(paramMap);
    }


    @Override
    public List<Map<String, Object>> getEntCheckSubmitDataByParam(Map<String, Object> parammap) {
        return checkEntInfoMapper.getEntCheckSubmitDataByParam(parammap);
    }

    @Override
    public Map<String, Object> getOneCheckEntDataByParam(Map<String, Object> param) {
        return checkEntInfoMapper.getOneCheckEntDataByParam(param);
    }

    /**
     * @author: mmt
     * @date: 2022/08/17
     * @Description: 检查企业列表
     * @updateUser:mmt
     * @updateDate:2022/08/17
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllCheckEntInfoList(Map<String, Object> param) {
        return checkEntInfoMapper.getAllCheckEntInfoList(param);
    }


}
