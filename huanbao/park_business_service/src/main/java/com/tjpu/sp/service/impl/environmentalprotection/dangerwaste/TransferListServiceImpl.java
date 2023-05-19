package com.tjpu.sp.service.impl.environmentalprotection.dangerwaste;

import com.tjpu.sp.dao.environmentalprotection.dangerwaste.TransferListMapper;
import com.tjpu.sp.model.environmentalprotection.dangerwaste.TransferListVO;
import com.tjpu.sp.service.environmentalprotection.dangerwaste.TransferListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TransferListServiceImpl implements TransferListService {
    @Autowired
    private TransferListMapper transferListMapper;

    public static final String[] MaterialPropertyName =
            new String[]{"腐蚀性", "急性毒性", "浸出毒性", "易燃性", "反应性", "含毒性物质", "传染性物质", "其它"};
    public static final String[] MaterialPropertyCode =
            new String[]{"1", "2", "3", "4", "5", "6", "7", "8"};

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:根据自定义参数获取转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public List<Map<String, Object>> getTransferListsByParamMap(Map<String, Object> paramMap) {
        return transferListMapper.getTransferListsByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:新增转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void insert(TransferListVO obj) {
        transferListMapper.insert(obj);
    }

    @Override
    public void insertBatch(List<TransferListVO> objs) {
        for (TransferListVO obj : objs) {
            transferListMapper.insert(obj);
        }
    }

    @Override
    public int addAndUpdateBatch(List<TransferListVO> addlist, List<TransferListVO> updatelist) {
        for (TransferListVO obj : addlist) {
            transferListMapper.insert(obj);
        }
        for (TransferListVO obj : updatelist) {
            transferListMapper.updateByPrimaryKey(obj);
        }
        return 0;
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:修改转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void updateByPrimaryKey(TransferListVO obj) {
        transferListMapper.updateByPrimaryKey(obj);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:根据主键ID删除转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public void deleteByPrimaryKey(String id) {
        transferListMapper.deleteByPrimaryKey(id);
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:根据主键ID获取转移联单详情信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public Map<String, Object> getTransferListDetailByID(String pkid) {
        Map<String, Object>  objmap = transferListMapper.getTransferListDetailByID(pkid);
        if (objmap!=null&&objmap.get("MaterialProperty")!=null){
            String [] strs=(objmap.get("MaterialProperty").toString()).split(",");
            String name ="";
            for (String str:strs){
                for (int i=0;i<MaterialPropertyCode.length;i++ ){
                    if (str.equals(MaterialPropertyCode[i])){
                        name=name+MaterialPropertyName[i]+",";
                    }
                }
            }
            if (!"".equals(name)){
                name=name.substring(0, name.length()-1);
            }
            objmap.put("MaterialProperty",name);
        }
        return objmap;
    }

    /**
     * @author: xsm
     * @date: 2019/10/21 0021 下午 7:36
     * @Description:根据id获取转移联单信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     */
    @Override
    public TransferListVO selectByPrimaryKey(String id) {
        return transferListMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<String> getTransferlistnumByParams(Map<String, Object> paramMap) {
        return transferListMapper.getTransferlistnumByParams(paramMap);
    }

    @Override
    public List<Map<String, Object>> getTransferListInfoByParamMap(Map<String, Object> paramMap) {
        return transferListMapper.getTransferListInfoByParamMap(paramMap);
    }

    /**
     * @Description：按年分组统计危废转移数量（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @Override
    public List<Map<String, Object>> countTransferNumDataGroupByYear(Map<String, Object> paramMap) {
        return transferListMapper.countTransferNumDataGroupByYear(paramMap);
    }

    /**
     * @Description：统计按危废父级种类分组的危废占比情况（省内转移、省外转移）
     * @Param:
     * @return:
     * @Author: xsm
     * @Date: 2022/05/19 10:44
     */
    @Override
    public List<Map<String, Object>> countTransferNumDataGroupByParentCode(Map<String, Object> paramMap) {
        return transferListMapper.countTransferNumDataGroupByParentCode(paramMap);
    }
}
