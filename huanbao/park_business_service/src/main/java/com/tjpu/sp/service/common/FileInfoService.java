package com.tjpu.sp.service.common;

import com.mongodb.client.gridfs.GridFSBucket;
import com.tjpu.sp.model.common.FileInfoVO;

import java.util.List;
import java.util.Map;

public interface FileInfoService {

    /**
     * @author: lip
     * @date: 2018/11/10 0010 下午 3:09
     * @Description: 插入记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void insert(FileInfoVO fileInfoVO);

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
    void deleteByFilePath(String fileId);

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
    List<FileInfoVO> getFilesInfoByParam(String fileflag, String businesstype, String businessfiletype);

    /**
     * 
     * @author: lip
     * @date: 2018/12/3 0003 下午 4:55
     * @Description: 自定义查询条件获取文件id数组
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
     * @date: 2019/5/13 0013 上午 9:09
     * @Description: 自定义查询条件文件集合信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    List<FileInfoVO> getFilesByParamMap(Map<String, Object> paramMap);

    /**
     *
     * @author: xsm
     * @date: 2019/6/16 0016 下午 2:23
     * @Description:  通过objectId，从mongodb批量删除排口图片和点位图片
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    void deleteFilesByParams(List<String> fileIds,GridFSBucket gridFSBucket);


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

    /**
     * @author: chengzq
     * @date: 2020/2/21 0021 上午 9:55
     * @Description: 获取问题数据文件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    List<Map<String,Object>> getProbleDataInfo(Map<String, Object> paramMap);

    void deleteById(String pkid);
}
