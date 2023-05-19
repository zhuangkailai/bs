package com.tjpu.sp.service.impl.environmentalprotection.dangerwaste;

import com.tjpu.sp.dao.environmentalprotection.dangerwaste.DangerWasteLicenceInfoMapper;
import com.tjpu.sp.model.environmentalprotection.dangerwaste.DangerWasteLicenceInfoVO;
import com.tjpu.sp.service.environmentalprotection.dangerwaste.DangerWasteLicenceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DangerWasteLicenceInfoServiceImpl implements DangerWasteLicenceInfoService {
    @Autowired
    private DangerWasteLicenceInfoMapper dangerWasteLicenceInfoMapper;

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:根据自定义参数获取危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getDangerWasteLicenceInfosByParamMap(Map<String, Object> paramMap) {
        return dangerWasteLicenceInfoMapper.getDangerWasteLicenceInfosByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:新增危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(DangerWasteLicenceInfoVO obj) {
        dangerWasteLicenceInfoMapper.insert(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:修改危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(DangerWasteLicenceInfoVO obj) {
        dangerWasteLicenceInfoMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:根据主键ID删除危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        dangerWasteLicenceInfoMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:根据主键ID获取危废许可证详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getDangerWasteLicenceInfoDetailByID(String pkid) {
        return dangerWasteLicenceInfoMapper.getDangerWasteLicenceInfoDetailByID(pkid);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 6:34
     * @Description:根据id获取危废许可证信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public DangerWasteLicenceInfoVO selectByPrimaryKey(String id) {
        return dangerWasteLicenceInfoMapper.selectByPrimaryKey(id);
    }
}
