package com.tjpu.sp.dao.common;

import com.tjpu.sp.model.common.FileInfoVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FileInfoMapper {
    int deleteByPrimaryKey(String pkFileid);

    int insert(FileInfoVO record);

    int insertSelective(FileInfoVO record);

    FileInfoVO selectByPrimaryKey(String pkFileid);

    int updateByPrimaryKeySelective(FileInfoVO record);

    int updateByPrimaryKey(FileInfoVO record);

    /**
     * @author: lip
     * @date: 2018/11/10 0010 下午 3:14
     * @Description: 根据文件路径（mongodb中的文件主键）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteByFilePath(String filePath);

    /**
     * @Author: zhangzc
     * @Date: 2018/11/10 16:15
     * @Description: 根据文件标识和文件业务相关类型以及业务类型下的文件类型获取文件信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: fileflag         文件标识（对应业务表中附件字段所存的ID）（必传）
     * @Param: businesstype     文件业务相关类型（必传）
     * @Param: businessfiletype 业务类型下的文件类型（非必传）
     * @Return:
     */
    List<FileInfoVO> getFilesInfoByParam(@Param("fileflag") String fileflag, @Param("businesstype") String businesstype, @Param("businessfiletype") String businessfiletype);
    /**
     *
     * @author: lip
     * @date: 2018/12/3 0003 下午 4:55
     * @Description: 自定义查询条件获取文件标识数组
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<String> getFileIdsListByParam(Map<String, Object> paramMap);



    /**
     *
     * @author: lip
     * @date: 2019/5/13 0013 上午 9:17
     * @Description: 自定义动态参数查询文件集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    List<FileInfoVO> getFilesByParamMap(Map<String, Object> paramMap);


    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:24
     * @Description:  根据文件标识集合和文件业务相关类型以及业务类型下的文件类型获取文件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<FileInfoVO> getFilesInfosByParam(Map<String,Object> paramMap);

    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:24
     * @Description:  根据文件标识集合和文件业务相关类型以及业务类型下的文件类型删除文件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    int deleteByParam(Map<String,Object> paramMap);

    List<Map<String,Object>> getProbleDataInfo(Map<String,Object> paramMap);

    List<Map<String,Object>> getFileDataByFileflags(Map<String, Object> param);
}