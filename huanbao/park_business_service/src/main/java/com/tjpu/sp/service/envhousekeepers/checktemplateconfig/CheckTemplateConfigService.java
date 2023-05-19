package com.tjpu.sp.service.envhousekeepers.checktemplateconfig;

import com.tjpu.sp.model.envhousekeepers.checktemplateconfig.CheckTemplateConfigVO;
import com.tjpu.sp.model.envhousekeepers.dataconnection.DataConnectionVO;

import java.util.List;
import java.util.Map;

public interface CheckTemplateConfigService {
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
    List<Map<String,Object>> getCheckTemplateConfigsByParamMap(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getAllInspectTypes(Map<String,Object> param);

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
    void deleteCheckTemplateConfigById(String id);

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
    void insert(CheckTemplateConfigVO obj,List<DataConnectionVO> listobj);

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
    void updateByPrimaryKey(CheckTemplateConfigVO obj,List<DataConnectionVO> listobj);

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
    List<Map<String,Object>> IsValidForValueByParam(Map<String, Object> paramMap);

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
    List<Map<String,Object>> IsHasCheckTemplateConfigHistoryData(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2021/07/30 0030 下午 4:57
     * @Description: 根据检查类型ID 获取该类型下的所有检查类别
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String,Object>> getAllCheckCategoryDataByInspectTypeID(Map<String, Object> param);

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
    List<Map<String,Object>> getCheckContentDataByCheckCategoryID(Map<String, Object> paramMap);

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
    List<Map<String,Object>> getEntCheckItemConfigDataByParam(Map<String, Object> paramMap);

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
    void addEntCheckTemplateConfig(List<Map<String, Object>> addlist,String pollutionid,String checktypecode);

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
    List<Map<String,Object>> getCheckItemConfigDataByCheckItemCode(Map<String, Object> paramMap);

    void deleteEntCheckItemConfigByParam(Map<String, Object> paramMap);

    List<Map<String,Object>> IsHasEntCheckItemConfigHistoryData(Map<String, Object> paramMap);
}
