package com.tjpu.sp.dao.common.pubcode;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PubCodeMapper {

    /**
     * @author: lip
     * @date: 2018/9/13 0013 下午 5:44
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<Map<String, Object>> getPubCodeDataByParam(Map<String, Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/10/14 0014 下午 2:19
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String, Object>> getPubCodesDataByParam(Map<String, Object> paramMap);

	/**
	 * @author: chengzq
	 * @date: 2019/10/14 0014 下午 2:19
	 * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据(有二级缓存)
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param: [paramMap]
	 * @throws:
	 */
	List<Map<String, Object>> getPubCodesDataByParamWithCache(Map<String, Object> paramMap);


    /**
     * @Author: zhangzc
     * @Date: 2018/12/20 15:03
     * @Description:
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: tableName 表名称 （必填）
     * @Param: codeFieldName  表中code字段名称 （必填）
     * @Param: viewFieldName  表中name字段名称  （必填）
     * @Param: orderFieldName 排序字段名称 （非必填有则排序无则不排序）
     * @Param: parentFieldName 父级字段名称（非必填有是树形数据没有则是下拉数据）
     * @Param: whereSql 查询条件Sql（非必填有则根据查询条件筛选）
     * @Return:
     */
    List<Map<String, Object>> getTreeDataByCodeValue(@Param("tableName") String tableName,
                                                     @Param("codeFieldName") String codeFieldName,
                                                     @Param("viewFieldName") String viewFieldName,
                                                     @Param("parentFieldName") String parentFieldName,
                                                     @Param("orderFieldName") String orderFieldName,
                                                     @Param("whereSql") String whereSql
    );

    /**
     * @return
     * @author: xsm
     * @date: 2018年12月27日 下午1:31:33
     * @Description: 获取公共分类配置树
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    List<Map<String, Object>> getPubClassConfigTreeData();


    /**
     * @author: chengzq
     * @date: 2019/5/31 0031 下午 4:40
     * @Description: 验证传入数据是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    int isTableDataHaveInfo(Map<String, Object> paramMap);

    /**
     * @author: xsm
     * @date: 2019/6/17 0017 下午 5:36
     * @Description: 根据sysmodel获取表名
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    String getPubCodeTableNameBySysmodel(Map<String, Object> paramMap);



    /**
     *
     * @author: lip
     * @date: 2019/10/15 0015 下午 5:14
     * @Description: 自定义条件删除公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deletePubCodeDataByParam(Map<String, Object> paramMap);
    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，添加公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void addPubCodeDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:07
     * @Description: 拼接sql，修改公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void editPubCodeDataByParam(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:38
     * @Description: 根据sysmodel获取表配置信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    Map<String,Object> getPubCodeTableConfigBySysmodel(Map<String, Object> paramMap);

    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:37
     * @Description: 判断表是否自增
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    int getTableHasIdentity(@Param("tableName") String tableName);

    /**
     *
     * @author: lip
     * @date: 2019/10/17 0017 上午 9:44
     * @Description: 据表名称获取主键为数字类型的表主键最大值
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    int getMaxNumByTableName(Map<String, Object> paramMapTemp);

    List<Map<String, Object>> getRegionDataList();
}