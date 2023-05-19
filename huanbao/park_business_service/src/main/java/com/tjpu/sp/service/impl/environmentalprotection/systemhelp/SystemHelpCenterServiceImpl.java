package com.tjpu.sp.service.impl.environmentalprotection.systemhelp;

import com.tjpu.sp.dao.environmentalprotection.systemhelp.SystemHelpCenterMapper;
import com.tjpu.sp.model.environmentalprotection.systemhelp.SystemHelpCenterVO;
import com.tjpu.sp.service.environmentalprotection.systemhelp.SystemHelpCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemHelpCenterServiceImpl implements SystemHelpCenterService {
    @Autowired
    private SystemHelpCenterMapper systemHelpCenterMapper;

    /**
     * @Author: xsm
     * @Date: 2020/02/14 9:31
     * @Description: 自定义条件查询系统问题帮助列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getSystemHelpInfosByParamMap(Map<String, Object> paramMap) {
        return systemHelpCenterMapper.getSystemHelpInfosByParamMap(paramMap);
    }

    /**
     * @Author: xsm
     * @Date: 2020/02/14 9:31
     * @Description: 根据主键ID获取系统帮助详情信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public Map<String, Object> getSystemHelpInfoDetailByID(String id) {
        return systemHelpCenterMapper.getSystemHelpInfoDetailByID(id);
    }

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:19
     * @Description: 新增系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void insert(SystemHelpCenterVO systemHelpCenterVO) {
        systemHelpCenterMapper.insert(systemHelpCenterVO);
    }

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:20
     * @Description: 根据ID获取系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public SystemHelpCenterVO selectByPrimaryKey(String id) {
        return systemHelpCenterMapper.selectByPrimaryKey(id);
    }

    /**
     * @Author: xsm
     * @Date: 2020/02/14 11:21
     * @Description: 修改系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void updateByPrimaryKey(SystemHelpCenterVO systemHelpCenterVO) {
        systemHelpCenterMapper.updateByPrimaryKey(systemHelpCenterVO);
    }

    /**
     * @Author: xsm
     * @Date: 2020/02/14 12:53
     * @Description: 删除系统帮助信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        systemHelpCenterMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2020/02/14 0014 下午 14:09
     * @Description: 获取所有系统帮助信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllSystemHelpInfos() {
        return systemHelpCenterMapper.getAllSystemHelpInfos();
    }
}
