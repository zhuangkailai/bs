package com.tjpu.sp.service.impl.environmentalprotection.cjpz;

import com.tjpu.sp.dao.environmentalprotection.cjpz.ProgramExecutionLogMapper;
import com.tjpu.sp.service.environmentalprotection.cjpz.ProgramExecutionLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;


@Service
@Transactional
public class ProgramExecutionLogServiceImpl implements ProgramExecutionLogService {
    @Autowired
    private ProgramExecutionLogMapper programExecutionLogMapper;

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 11:07
     * @Description: 清空程序执行日志信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public void clearProgramExecutionLogs() {
        programExecutionLogMapper.clearProgramExecutionLogs();
    }

    /**
     * @author: xsm
     * @date: 2021/01/13 0013 上午 11:07
     * @Description: 通过id获取程序执行日志详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public Map<String, Object> getProgramExecutionLogDetailByID(String id) {
        return programExecutionLogMapper.getProgramExecutionLogDetailByID(id);
    }

    /**
     * @Author: xsm
     * @Date: 2021/01/13 0013 上午 11:07
     * @Description: 自定义查询条件查询程序执行日志列表数据
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @Override
    public List<Map<String, Object>> getProgramExecutionLogsByParamMap(Map<String, Object> parammap) {
        return programExecutionLogMapper.getProgramExecutionLogsByParamMap(parammap);
    }

    @Override
    public Long countProgramExecutionLogNumByTimes(Map<String, Object> paramMap) {
        List<Map<String, Object>> datalist = programExecutionLogMapper.getProgramExecutionLogsByParamMap(paramMap);
        long i = 0;
        if (datalist!=null&&datalist.size()>0){
            i = datalist.size();
        }
        return i;
    }
}
