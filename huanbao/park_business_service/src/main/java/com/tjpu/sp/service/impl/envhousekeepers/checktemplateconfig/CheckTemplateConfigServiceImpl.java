package com.tjpu.sp.service.impl.envhousekeepers.checktemplateconfig;

import com.tjpu.sp.dao.envhousekeepers.checkentinfo.CheckEntInfoMapper;
import com.tjpu.sp.dao.envhousekeepers.checkproblemexpound.CheckProblemExpoundMapper;
import com.tjpu.sp.dao.envhousekeepers.checktemplateconfig.CheckTemplateConfigMapper;
import com.tjpu.sp.dao.envhousekeepers.dataconnection.DataConnectionMapper;
import com.tjpu.sp.model.envhousekeepers.checktemplateconfig.CheckTemplateConfigVO;
import com.tjpu.sp.model.envhousekeepers.dataconnection.DataConnectionVO;
import com.tjpu.sp.service.envhousekeepers.checktemplateconfig.CheckTemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CheckTemplateConfigServiceImpl implements CheckTemplateConfigService {
    @Autowired
    private CheckTemplateConfigMapper checkTemplateConfigMapper;
    @Autowired
    private DataConnectionMapper dataConnectionMapper;
    @Autowired
    private CheckProblemExpoundMapper checkProblemExpoundMapper;
    @Autowired
    private CheckEntInfoMapper checkEntInfoMapper;

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:35
     *@Description: 通过自定义参数获取检查模板配置信息
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getCheckTemplateConfigsByParamMap(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.getCheckTemplateConfigsByParamMap(paramMap);
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:35
     *@Description: 获取所有巡查类型
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [jsonObject]
     *@throws:
     **/
    @Override
    public List<Map<String, Object>> getAllInspectTypes(Map<String,Object> param) {
        List<Map<String, Object>> listdata = checkTemplateConfigMapper.getAllInspectTypes(param);
        List<String> types = listdata.stream().filter(m -> m.get("code") != null).map(m->m.get("code").toString()).distinct().collect(Collectors.toList());
        param.put("checktypecodes",types);
        Boolean issubmit  = false;
        if (param.get("issubmit")!=null){
            issubmit = (Boolean) param.get("issubmit");
        }
        List<Map<String, Object>> listmap = new ArrayList<>();
        if (param.get("pollutionid")!=null&&param.get("checktime")!=null) {
            listmap = checkEntInfoMapper.getCheckEntInfoStatusByParam(param);
            List<Map<String, Object>> resultlist = new ArrayList<>();
        if (listdata!=null&&listdata.size()>0){
            for (Map<String, Object> map:listdata){
                String code = map.get("code")!=null?map.get("code").toString():"";
                if (listmap!=null&&listmap.size()>0){
                    for (Map<String, Object> objmap:listmap) {
                        if (objmap.get("FK_CheckTypeCode")!=null&&code.equals(objmap.get("FK_CheckTypeCode").toString())){
                            if(objmap.get("Status")!=null&&!"".equals(objmap.get("Status").toString())) {
                                map.put("status", Integer.valueOf(objmap.get("Status").toString()));
                                if (issubmit){
                                    if (Integer.valueOf(objmap.get("Status").toString())>0&&Integer.valueOf(objmap.get("Status").toString())<3) {
                                        resultlist.add(map);
                                    }
                                }else{
                                    resultlist.add(map);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
            return resultlist;
        }else{
            return listdata;
        }
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 9:55
     *@Description: 通过主键id删除检查模板配置单条数据
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [id]
     *@throws:
     **/
    @Override
    public void deleteCheckTemplateConfigById(String id) {
        checkTemplateConfigMapper.deleteByPrimaryKey(id);
        dataConnectionMapper.deleteByTemplateConfigID(id);
        Map<String,Object> param = new HashMap<>();
        param.put("fkchecktemplateconfigid", id);
        //删除企业端配置
        checkTemplateConfigMapper.deleteEntCheckItemConfigByParam(param);
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
    public void insert(CheckTemplateConfigVO obj,List<DataConnectionVO> listobj) {
        checkTemplateConfigMapper.insert(obj);
        if (listobj!=null&&listobj.size()>0){
            dataConnectionMapper.batchInsert(listobj);
        }
    }

    /**
     *@author: xsm
     *@date: 2021/06/29 0029 09:56
     *@Description: 编辑检查模板配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [obj]
     *@throws:
     **/
    @Override
    public void updateByPrimaryKey(CheckTemplateConfigVO obj,List<DataConnectionVO> listobj) {
        checkTemplateConfigMapper.updateByPrimaryKey(obj);
        dataConnectionMapper.deleteByTemplateConfigID(obj.getPkId());
        if (listobj!=null&&listobj.size()>0){
            dataConnectionMapper.batchInsert(listobj);
        }
    }

    /**
     * @author: xsm
     * @date: 2021/07/01 0001 下午 4:09
     * @Description: 验证检查项目是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> IsValidForValueByParam(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.IsValidForValueByParam(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/07/05 0005 上午 10:03
     * @Description: 验证是否有录入该检查项目的历史记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> IsHasCheckTemplateConfigHistoryData(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.IsHasCheckTemplateConfigHistoryData(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllCheckCategoryDataByInspectTypeID(Map<String, Object> param) {
        return checkTemplateConfigMapper.getAllCheckCategoryDataByInspectTypeID(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/03 0003 下午 15:53
     * @Description: 根据检查类别ID获取该检查类别下的检查内容信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getCheckContentDataByCheckCategoryID(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.getCheckContentDataByCheckCategoryID(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 上午 9:22
     * @Description: 根据企业ID和检查类型获取企业的检查项、检查内容配置
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntCheckItemConfigDataByParam(Map<String, Object> paramMap) {
        List<Map<String, Object>> result = new ArrayList<>();
        result = checkTemplateConfigMapper.getEntCheckItemConfigDataByParam(paramMap);
        if (result==null||result.size()==0){
            result = checkTemplateConfigMapper.getCheckTemplateConfigsByParamMap(paramMap);
        }
        return result;
    }

    /**
     *@author: xsm
     *@date: 2021/08/30 0030 10:16
     *@Description: 添加企业检查项配置
     *@updateUser:
     *@updateDate:
     *@updateDescription:
     *@param: [addformdata]
     *@throws:
     **/
    @Override
    public void addEntCheckTemplateConfig(List<Map<String, Object>> addlist,String pollutionid,String checktypecode) {
        if (addlist!=null&&addlist.size()>0){
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("checktypecode", checktypecode);
            paramMap.put("pollutionid", pollutionid);
            checkTemplateConfigMapper.deleteEntCheckItemConfigByParam(paramMap);
            checkTemplateConfigMapper.batchInsertEntCheckItemConfig(addlist);
        }

    }

    /**
     * @author: xsm
     * @date: 2021/08/30 0030 上午 9:22
     * @Description: 根据检查项code 获取所以检查内容
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getCheckItemConfigDataByCheckItemCode(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.getCheckTemplateConfigsByParamMap(paramMap);
    }

    @Override
    public void deleteEntCheckItemConfigByParam(Map<String, Object> paramMap) {
        checkTemplateConfigMapper.deleteEntCheckItemConfigByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> IsHasEntCheckItemConfigHistoryData(Map<String, Object> paramMap) {
        return checkTemplateConfigMapper.IsHasEntCheckItemConfigHistoryData(paramMap);
    }

}
