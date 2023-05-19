package com.tjpu.sp.service.impl.envhousekeepers.problemconsult;


import com.tjpu.sp.dao.envhousekeepers.problemconsult.CommonProblemRecordMapper;
import com.tjpu.sp.model.envhousekeepers.problemconsult.CommonProblemRecordVO;
import com.tjpu.sp.service.envhousekeepers.problemconsult.CommonProblemRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
public class CommonProblemRecordServiceImpl implements CommonProblemRecordService {

    @Autowired
    private CommonProblemRecordMapper commonProblemRecordMapper;

    /**
     * @author: xsm
     * @date: 2021/08/18 0018 下午 1:11
     * @Description: 通过自定义参数查询常见问题记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsJson]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getCommonProblemRecordByParamMap(Map<String,Object> paramMap) {
        return commonProblemRecordMapper.getCommonProblemRecordByParamMap(paramMap);
    }

    @Override
    public void insert(CommonProblemRecordVO entity) {
        commonProblemRecordMapper.insert(entity);
    }

    @Override
    public CommonProblemRecordVO selectByPrimaryKey(String id) {
        return commonProblemRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public void updateByPrimaryKey(CommonProblemRecordVO entity) {
        commonProblemRecordMapper.updateByPrimaryKey(entity);
    }

    @Override
    public void deleteByPrimaryKey(String id) {
        commonProblemRecordMapper.deleteByPrimaryKey(id);
    }

    @Override
    public Map<String, Object> getCommonProblemRecordDetailByID(String id) {
        return commonProblemRecordMapper.getCommonProblemRecordDetailByID(id);
    }

    @Override
    public List<Map<String, Object>> getKeyCommonProblemRecordByParam(Map<String,Object> param) {
        List<Map<String, Object>> resultlist = new ArrayList<>();
        List<Map<String, Object>> problemtypelist = commonProblemRecordMapper.getCommonProblemTypesByParam(param);
        int num = 0;
        if ( param.get("typenum")!=null){
            num = Integer.valueOf(param.get("typenum").toString());
        }else{
            num = 3;
        }
        List<String> codes = new ArrayList<>();
        Map<String, Object> codeandname = new HashMap<>();
        if (problemtypelist!=null&&problemtypelist.size()>0){
            int i = 0;
            for (Map<String, Object> map:problemtypelist){
                if (i<num) {
                    if (map.get("Code") != null) {
                        codes.add(map.get("Code").toString());
                        codeandname.put(map.get("Code").toString(), map.get("Name"));
                    }
                }else{
                    break;
                }
                i++;
            }

        }
        param.put("problemtypes",codes);
        List<Map<String, Object>> result = commonProblemRecordMapper.getKeyCommonProblemRecordByParam(param);
        for (String code:codes){
            Map<String, Object> resultmap = new HashMap<>();
            resultmap.put("typecode",code);
            resultmap.put("typename",codeandname.get(code));
            List<Map<String, Object>> listdata = new ArrayList<>();
            if (result!=null&&result.size()>0){
                for (Map<String, Object> map:result){
                    if (map.get("FK_CommonProblemType")!=null&&code.equals(map.get("FK_CommonProblemType").toString())){
                        listdata.add(map);
                    }
                }
            }
            resultmap.put("datalist",listdata);
            resultlist.add(resultmap);
        }
        return resultlist;
    }

}
