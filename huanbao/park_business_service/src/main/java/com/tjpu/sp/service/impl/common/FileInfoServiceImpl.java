package com.tjpu.sp.service.impl.common;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.tjpu.sp.dao.common.FileInfoMapper;
import com.tjpu.sp.model.common.FileInfoVO;
import com.tjpu.sp.service.common.FileInfoService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class FileInfoServiceImpl implements FileInfoService {

    @Autowired
    private FileInfoMapper fileInfoMapper;

    @Override
    public void insert(FileInfoVO fileInfoVO) {
        fileInfoMapper.insert(fileInfoVO);
    }

    @Autowired
    @Qualifier("secondMongoTemplate")
    private MongoTemplate mongoTemplate;
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
    @Override
    public void deleteByFilePath(String filePath) {




        fileInfoMapper.deleteByFilePath(filePath);
    }

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
    @Override
    public List<FileInfoVO> getFilesInfoByParam(String fileflag, String businesstype, String businessfiletype) {
        return fileInfoMapper.getFilesInfoByParam(fileflag, businesstype, businessfiletype);
    }
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
    @Override
    public List<String> getFileIdsListByParam(Map<String, Object> paramMap) {
        return fileInfoMapper.getFileIdsListByParam(paramMap);
    }

    /**
     *
     * @author: lip
     * @date: 2019/5/13 0013 上午 9:16
     * @Description: 自定义动态参数查询文件集合
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public List<FileInfoVO> getFilesByParamMap(Map<String, Object> paramMap) {
        return fileInfoMapper.getFilesByParamMap(paramMap);
    }

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
    @Override
    public void deleteFilesByParams(List<String> fileIds,GridFSBucket gridFSBucket) {
        for (String fileId : fileIds) {

            GridFSFindIterable gridFSFindIterable = gridFSBucket.find(Filters.eq("_id", new ObjectId(fileId)));
            GridFSFile gridFSFile = gridFSFindIterable.first();
            if (gridFSFile != null) {
                gridFSBucket.delete(new ObjectId(fileId));
            }
            //2，删除文件关联
            fileInfoMapper.deleteByFilePath(fileId);
        }
    }

    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:25
     * @Description: 根据文件标识集合和文件业务相关类型以及业务类型下的文件类型获取文件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<FileInfoVO> getFilesInfosByParam(Map<String, Object> paramMap) {
        return fileInfoMapper.getFilesInfosByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:25
     * @Description: 根据文件标识集合和文件业务相关类型以及业务类型下的文件类型删除文件信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int deleteByParam(Map<String, Object> paramMap) {
        return fileInfoMapper.deleteByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2020/2/21 0021 上午 9:56
     * @Description: 获取问题数据信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getProbleDataInfo(Map<String, Object> paramMap) {
        return fileInfoMapper.getProbleDataInfo(paramMap);
    }

    @Override
    public void deleteById(String pkid) {
        fileInfoMapper.deleteByPrimaryKey(pkid);
    }


}
