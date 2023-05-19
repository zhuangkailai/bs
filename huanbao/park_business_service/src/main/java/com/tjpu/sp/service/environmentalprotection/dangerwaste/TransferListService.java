package com.tjpu.sp.service.environmentalprotection.dangerwaste;


import com.tjpu.sp.model.environmentalprotection.dangerwaste.TransferListVO;

import java.util.List;
import java.util.Map;

public interface TransferListService {

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:根据自定义参数获取转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    List<Map<String, Object>> getTransferListsByParamMap(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:新增转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void insert(TransferListVO obj);
    void insertBatch(List<TransferListVO> objs);
    int addAndUpdateBatch(List<TransferListVO> addlist,List<TransferListVO> updatelist);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:修改转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void updateByPrimaryKey(TransferListVO obj);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:根据主键ID删除转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    void deleteByPrimaryKey(String id);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:根据主键ID获取转移联单详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    Map<String, Object> getTransferListDetailByID(String id);

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:30
     * @Description:根据id获取转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    TransferListVO selectByPrimaryKey(String id);

    List<String> getTransferlistnumByParams(Map<String, Object> paramMap);

    List<Map<String,Object>> getTransferListInfoByParamMap(Map<String, Object> paramMap);

    /**
     * @Description：按年分组统计危废转移数量（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    List<Map<String,Object>> countTransferNumDataGroupByYear(Map<String, Object> paramMap);

    /**
     * @Description：统计按危废父级种类分组的危废占比情况（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    List<Map<String,Object>> countTransferNumDataGroupByParentCode(Map<String, Object> paramMap);
}
