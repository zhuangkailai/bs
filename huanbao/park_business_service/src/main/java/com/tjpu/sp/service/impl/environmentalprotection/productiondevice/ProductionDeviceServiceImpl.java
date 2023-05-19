package com.tjpu.sp.service.impl.environmentalprotection.productiondevice;

import com.tjpu.sp.dao.environmentalprotection.productiondevice.ProductionDeviceMapper;
import com.tjpu.sp.model.environmentalprotection.productiondevice.ProductionDeviceVO;
import com.tjpu.sp.service.environmentalprotection.productiondevice.ProductionDeviceService;
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
public class ProductionDeviceServiceImpl implements ProductionDeviceService {

    @Autowired
    private ProductionDeviceMapper productionDeviceMapper;


    @Override
    public int deleteByPrimaryKey(String pkId) {
        Map<String, Object> map = productionDeviceMapper.selectByPrimaryKey(pkId);
        List<String> collect =new ArrayList<>();
        if(map!=null){
            List<Map<String,Object>> list = (List) map.get("child");
            collect.addAll(list.stream().filter(m -> m.get("pkId") != null).map(m -> m.get("pkId").toString()).collect(Collectors.toList()));
        }
        collect.add(pkId);
        for (String s : collect) {
            productionDeviceMapper.deleteByPrimaryKey(s);
        }
        return 0;
    }

    @Override
    public int insert(List<ProductionDeviceVO> record) {
        for (ProductionDeviceVO productionDeviceVO : record) {
            productionDeviceMapper.insert(productionDeviceVO);
        }
        return 0;
    }

    @Override
    public Map<String, Object> selectByPrimaryKey(String pkId) {
        return productionDeviceMapper.selectByPrimaryKey(pkId);
    }

    @Override
    public int update(ProductionDeviceVO record ,List<ProductionDeviceVO> list) {
        productionDeviceMapper.deleteByParentid(record.getPkId());
        for (ProductionDeviceVO productionDeviceVO : list) {
            productionDeviceMapper.insert(productionDeviceVO);
        }
        productionDeviceMapper.updateByPrimaryKey(record);
        return 0;
    }

    @Override
    public int updateByPrimaryKey(ProductionDeviceVO record) {
        return productionDeviceMapper.updateByPrimaryKey(record);
    }


    /**
     * @author: chengzq
     * @date: 2019/11/01 0016 下午 2:38
     * @Description:  通过自定义参数获取生产装置设备信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getProductionDeviceByParamMap(Map<String, Object> paramMap) {
        return productionDeviceMapper.getProductionDeviceByParamMap(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/11/01 0016 下午 2:38
     * @Description: 通过id获取生产装置设备详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkid]
     * @throws:
     */
    @Override
    public Map<String, Object> getProductionDeviceDetailByID(String pkid) {
        return productionDeviceMapper.selectByPrimaryKey(pkid);
    }

    @Override
    public Map<String, Object> getProductionDeviceInfoByID(String id) {
        return productionDeviceMapper.getProductionDeviceInfoByID(id);
    }

    @Override
    public List<Map<String, Object>> getProductDeviceAndPollutionInfoByParamMap(Map<String, Object> paramMap) {
        return productionDeviceMapper.getProductDeviceAndPollutionInfoByParamMap(paramMap);
    }


}
