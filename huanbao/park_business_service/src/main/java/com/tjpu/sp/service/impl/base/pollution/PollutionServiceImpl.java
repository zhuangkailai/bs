package com.tjpu.sp.service.impl.base.pollution;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.utils.FormatUtils;
import com.tjpu.sp.dao.base.pollution.PollutionLabelDataMapper;
import com.tjpu.sp.dao.base.pollution.PollutionMapper;
import com.tjpu.sp.dao.common.FunctionMapper;
import com.tjpu.sp.dao.common.pubcode.PollutantFactorMapper;
import com.tjpu.sp.dao.environmentalprotection.dischargepermit.LicenceMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.DeviceStatusMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.GasOutPutInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.monitorpoint.WaterOutputInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.particularpollutants.ParticularPollutantsMapper;
import com.tjpu.sp.dao.environmentalprotection.productionmaterials.ProductInfoMapper;
import com.tjpu.sp.dao.environmentalprotection.productionmaterials.RawMaterialMapper;
import com.tjpu.sp.dao.environmentalprotection.taskmanagement.AlarmTaskDisposeManagementMapper;
import com.tjpu.sp.dao.environmentalprotection.video.VideoCameraMapper;
import com.tjpu.sp.model.base.UserMonitorPointRelationDataVO;
import com.tjpu.sp.model.base.pollution.PollutionLabelDataVO;
import com.tjpu.sp.model.base.pollution.PollutionVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.GASOutPutInfoVO;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.WaterOutputInfoVO;
import com.tjpu.sp.service.base.pollution.PollutionService;
import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Transactional
@Service
public class PollutionServiceImpl implements PollutionService {
    private final PollutionMapper pollutionMapper;
    @Autowired
    private PollutionLabelDataMapper pollutionLabelDataMapper;

    @Autowired
    private AlarmTaskDisposeManagementMapper alarmTaskDisposeManagementMapper;
    @Autowired
    private ParticularPollutantsMapper particularPollutantsMapper;

    @Autowired
    private PollutantFactorMapper pollutantFactorMapper;

    @Autowired
    private FunctionMapper functionMapper;
    @Autowired
    private WaterOutputInfoMapper waterOutputInfoMapper;
    @Autowired
    private GasOutPutInfoMapper gasOutPutInfoMapper;
    @Autowired
    private PublicSystemMicroService publicSystemMicroService;
    @Autowired
    private DeviceStatusMapper deviceStatusMapper;
    @Autowired
    private LicenceMapper licenceMapper;
    @Autowired
    private ProductInfoMapper productInfoMapper;
    @Autowired
    private RawMaterialMapper rawMaterialMapper;
    @Autowired
    private VideoCameraMapper videoCameraMapper;

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate mongoTemplate;

    private String sysmodel = "pollutionInfo";
    private String pk_id = "pk_pollutionid";
    /**
     * 数据源
     */
    @Value("${spring.datasource.primary.name}")
    private String datasource;


    public PollutionServiceImpl(PollutionMapper pollutionMapper) {
        this.pollutionMapper = pollutionMapper;
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/9 16:01
     * @Description: 按行业类型统计企业分布情况
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getEnterpriseForIndustry() {
        return pollutionMapper.getEnterpriseForIndustry();
    }

    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 上午 11:05
     * @Description: 通过企业id获取废水排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getWaterOutPutInfoByPollutionid(Map<String, Object> paramMap) {
        return pollutionMapper.getWaterOutPutInfoByPollutionid(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/20 0020 上午 11:05
     * @Description: 通过企业id获取废气排口信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getGasOutPutInfoByPollutionid(Map<String, Object> paramMap) {
        return pollutionMapper.getGasOutPutInfoByPollutionid(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 10:54
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [jsonObject]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionsInfoByParamMap(Map<String, Object> jsonObject) {
        return pollutionMapper.getPollutionsInfoByParamMap(jsonObject);
    }


    /**
     * @author: xsm
     * @date: 2020/03/24 0024 下午 3:06
     * @Description: 统计企业各子级菜单数据条数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> countPollutionChildMenuDataNum(Map<String, Object> paramMap) {
        PollutionVO obj = pollutionMapper.getPollutionDetailByUserId(paramMap);
        Map<String,Object> result =new HashMap<>();
        if (obj!=null){//该用户关联企业
            String pollutionid = obj.getPkpollutionid();
            result.put("pollutionid",pollutionid);
            Map<String,Object> datamap =new HashMap<>();
            paramMap.put("fk_pollutionid",pollutionid);
            //主要产品
            List<Map<String,Object>> zycplist= productInfoMapper.getProductInfosByParamMap(paramMap);
            datamap.put("zycp_num",(zycplist!=null&&zycplist.size()>0)?zycplist.size():0);
            //排污许可证
            paramMap.put("pollutionid",pollutionid);
            List<Map<String,Object>>  pwxkzlist = licenceMapper.getPermitListByParamMap(paramMap);
            datamap.put("pwxkz_num",(pwxkzlist!=null&&pwxkzlist.size()>0)?pwxkzlist.size():0);
            //原辅料
            List<Map<String,Object>> yfllist = rawMaterialMapper.getRawMaterialsByParamMap(paramMap);
            datamap.put("yfl_num",(yfllist!=null&&yfllist.size()>0)?yfllist.size():0);
            result.put("numdata",datamap);
            //企业应急

            datamap.put("qyyj_num",3);
            result.put("numdata",datamap);
        }
        return result;
    }



    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 上午 11:30
     * @Description: 通过实体对象新增污染源和标签
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record]
     * @throws:
     */
    @Override
    public int insertSelective(PollutionVO record, List<Map<String, Object>> labels) {
        String pollutioncode = "";
        Map<String, Object> paramMap = new HashMap<>();
        //如果有社会信用代码生成污染源code，没有不生成
        String entsocialcreditcode = record.getEntsocialcreditcode();
        if (StringUtils.isNotBlank(entsocialcreditcode)) {
            paramMap.put("EntSocialCreditCode", entsocialcreditcode);
            functionMapper.getPollutionCode(paramMap);
            if (paramMap.get("NewPollutionCode") != null) {
                pollutioncode = paramMap.get("NewPollutionCode").toString();
            }
            record.setPollutioncode(pollutioncode);
        }

        List<PollutionLabelDataVO> pollutionLabelDataVOS = new ArrayList<>();
        for (Map label : labels) {
            PollutionLabelDataVO pollutionLabelDataVO = new PollutionLabelDataVO();
            pollutionLabelDataVO.setPkId(UUID.randomUUID().toString());
            pollutionLabelDataVO.setFkPollutionid(record.getPkpollutionid());
            pollutionLabelDataVO.setFkPollutionlabelid(Integer.valueOf(label.get("labelcode").toString()));
            pollutionLabelDataVOS.add(pollutionLabelDataVO);
        }
        //新增标签
        if (pollutionLabelDataVOS.size() > 0) {
            pollutionLabelDataMapper.insertLabels(pollutionLabelDataVOS);
        }
        int i = pollutionMapper.insertSelective(record);
        return i;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 1:51
     * @Description: 通过实体对象新增污染源和标签
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [record, labels]
     * @throws:
     */
    @Override
    public int updateByPrimaryKeySelective(PollutionVO record, List<Map<String, Object>> labels) {
        PollutionVO pollutionVO = pollutionMapper.selectByPrimaryKey(record.getPkpollutionid());
        String entsocialcreditcode = record.getEntsocialcreditcode();
        String entsocialcreditcode1 = pollutionVO.getEntsocialcreditcode();
        String pollutioncode = "";
        Map<String, Object> paramMap = new HashMap<>();
        if (entsocialcreditcode != null && !entsocialcreditcode.equals(entsocialcreditcode1)) {
            //如果有社会信用代码生成污染源code，没有不生成
            if (StringUtils.isNotBlank(entsocialcreditcode)) {
                paramMap.put("EntSocialCreditCode", entsocialcreditcode);
                functionMapper.getPollutionCode(paramMap);
                if (paramMap.get("NewPollutionCode") != null) {
                    pollutioncode = paramMap.get("NewPollutionCode").toString();
                }
                record.setPollutioncode(pollutioncode);
            }
        }


        List<PollutionLabelDataVO> pollutionLabelDataVOS = new ArrayList<>();
        //通过污染源id删除标签
        pollutionLabelDataMapper.deleteByPolltionid(record.getPkpollutionid());
        for (Map label : labels) {
            PollutionLabelDataVO pollutionLabelDataVO = new PollutionLabelDataVO();
            pollutionLabelDataVO.setPkId(UUID.randomUUID().toString());
            pollutionLabelDataVO.setFkPollutionid(record.getPkpollutionid());
            pollutionLabelDataVO.setFkPollutionlabelid(Integer.valueOf(label.get("labelcode").toString()));
            pollutionLabelDataVOS.add(pollutionLabelDataVO);
        }
        //新增标签
        if (pollutionLabelDataVOS.size() > 0) {
            pollutionLabelDataMapper.insertLabels(pollutionLabelDataVOS);
        }
        int i = pollutionMapper.updateByPrimaryKey(record);
        return i;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 2:59
     * @Description: 通过id删除污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkPollutionid]
     * @throws:
     */
    @Override
    public int deleteByPrimaryKey(String pkPollutionid) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(pk_id, pkPollutionid);
        pollutionMapper.deleteByPrimaryKey(pkPollutionid);
        pollutionMapper.deleteVideoCameraByPollutionId(pkPollutionid);
        pollutionMapper.deleteMonitorEquipmentByPollutionId(pkPollutionid);
        //deleteSecurityRelevantData(paramMap);//删除安全相关数据
        deleteEnvRelevantData(paramMap);//删除环保相关数据
//        publicSystemMicroService.deleteMethod(param);
        return 0;
    }

    /**
     * @author: chengzq
     * @date: 2019/5/23 0023 下午 3:32
     * @Description: 通过污染源id查询污染源及标签
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public PollutionVO getPollutionAndLabelsByPollutionid(String id) {
        return pollutionMapper.getPollutionAndLabelsByPollutionid(id);
    }

    /**
     * @author: zhangzc
     * @date: 2019/5/27 13:21
     * @Description: 获取所有污染源
     * @param:
     * @return:
     */
    @Override
    public List<String> getPollutionNames() {
        return pollutionMapper.getPollutionNames();
    }


    /**
     * @author: chengzq
     * @date: 2019/5/29 0029 下午 7:50
     * @Description: 通过id获取污染源详情
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public PollutionVO getDetailById(Map<String, Object> paramMap) {
        return pollutionMapper.getDetailById(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/10 0010 上午 10:59
     * @Description: 修改污染源时将该污染源下所有废水废气排口code修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     */
    @Override
    public void updateOutPutCode(String pollutionid) {
        Map<String, Object> paramMap = new HashMap<>();
        //修改污染源将所有排口编码修改
        if (StringUtils.isNotBlank(pollutionid)) {
            List<WaterOutputInfoVO> waterOutputInfoVOS = waterOutputInfoMapper.selectOutputByPollutionid(pollutionid);
            for (WaterOutputInfoVO waterOutputInfoVO : waterOutputInfoVOS) {
                paramMap.clear();
                paramMap.put("pollutioId", pollutionid);
                functionMapper.getWaterOutputCode(paramMap);
                //生成新code
                Object waterOutputCode = paramMap.get("WaterOutputCode");
                //获取原来code
                String outputcode = waterOutputInfoVO.getOutputcode();
                //截取原来code后几位DA001
                String substring = "";
                if (outputcode!=null && outputcode.length() > 22) {
                    substring = outputcode.substring(22, outputcode.length());
                }
                //截取新code前22位拼接substring
                String code = "";
                if (waterOutputCode.toString().length() > 22) {
                    code = waterOutputCode.toString().substring(0, 22) + substring;
                }
                waterOutputInfoVO.setOutputcode(code);
                waterOutputInfoMapper.updateByPrimaryKey(waterOutputInfoVO);
            }
            List<GASOutPutInfoVO> gasOutPutInfoVOS = gasOutPutInfoMapper.selectOutputByPollutionid(pollutionid);
            for (GASOutPutInfoVO gasOutPutInfoVO : gasOutPutInfoVOS) {
                paramMap.clear();
                paramMap.put("pollutioId", pollutionid);
                functionMapper.getGasOutputCode(paramMap);
                //生成新code
                Object gasOutputCode = paramMap.get("GasOutputCode");
                //获取原来code
                String outputcode = gasOutPutInfoVO.getOutputcode();
                //截取原来code后几位DA001
                String substring = "";
                if (outputcode.length() > 22) {
                    substring = outputcode.substring(22, outputcode.length());
                }
                //截取新code前22位拼接substring
                String code = "";
                if (gasOutputCode.toString().length() > 22) {
                    code = gasOutputCode.toString().substring(0, 22) + substring;
                }
                gasOutPutInfoVO.setOutputcode(code);
                gasOutPutInfoMapper.updateByPrimaryKey(gasOutPutInfoVO);
            }
        }
    }

    /**
     * @author: lip
     * @date: 2019/6/12 0012 下午 2:11
     * @Description: 自定义查询条件按污染标签类型分组统计污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> countPollutionForPollutionLabelByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.countPollutionForPollutionLabelByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/6/13 0013 下午 1:38
     * @Description: 获取按标签类型分组的污染源标签信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionLabelsGroupByLabelType() {
        List<Map<String, Object>> list = pollutionMapper.getPollutionLabelsGroupByLabelType();
        List<Map<String, Object>> resultlist = new ArrayList<Map<String, Object>>();
        Set set = new HashSet();
        if (list != null && list.size() > 0) {
            for (Map<String, Object> objmap : list) {
                if (set.contains(objmap.get("labeltype"))) {//判断是否类型重复
                    continue;//重复
                } else {//不重复
                    Map<String, Object> resultmap = new HashMap<String, Object>();
                    List<Map<String, Object>> labellist = new ArrayList<Map<String, Object>>();
                    for (Map<String, Object> map : list) {
                        Map<String, Object> labelmap = new HashMap<String, Object>();
                        if (objmap.get("labeltype").equals(map.get("labeltype"))) {//当标签类型相同
                            labelmap.put("pollutionlabelcode", map.get("code"));
                            labelmap.put("pollutionlabelname", map.get("name"));
                            labellist.add(labelmap);
                        }
                    }
                    set.add(objmap.get("labeltype"));
                    resultmap.put("labeltypename", objmap.get("labeltype"));
                    resultmap.put("labellist", labellist);
                    resultlist.add(resultmap);
                }
            }
        }
        return resultlist;
    }


    /**
     * @author: chengzq
     * @date: 2019/5/21 0021 下午 3:55
     * @Description: 自定义条件，查询污染源总数量
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public long countTotalByParam(Map<String, Object> paramMap) {
        return pollutionMapper.countTotalByParam(paramMap);
    }


    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 5:57
     * @Description: 通过主键查询污染源
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public PollutionVO selectByPrimaryKey(String id) {
        return pollutionMapper.selectByPrimaryKey(id);
    }

    /**
     * @author: chengzq
     * @date: 2019/6/17 0017 下午 6:09
     * @Description: 通过污染源id查询废水，废气，无组织监测点文件id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [id]
     * @throws:
     */
    @Override
    public List<String> getImgIdByPollutionid(String id) {
        return pollutionMapper.getImgIdByPollutionid(id);
    }

    /**
     * @author: lip
     * @date: 2019/6/21 0021 上午 10:59
     * @Description: 自定义查询条件获取所有污染源下排口（废水直接排口、废水间接排口、雨水排口、废气有组织、废气无组织）信息以及污染物信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllPollutionAndOutPutAndPollutantInfoByParamMap(Map<String, Object> paramMap) {
        if (paramMap.get("monitorpollutant") != null && !"".equals(paramMap.get("monitorpollutant"))) {
            List<Map<String, Object>> particularPollutant = particularPollutantsMapper.getLastVersionPollutantInfoByParamMap(paramMap);
            List<String> outputids = new ArrayList<>();
            Set<String> monitorpointtypes = new HashSet<>();
            for (Map<String, Object> map : particularPollutant) {
                outputids.add(map.get("fk_outputid").toString());
                monitorpointtypes.add(map.get("fk_monitorpointtypecode").toString());
            }
            if (outputids.size() == 0) {
                outputids.add("");
            }
            paramMap.put("outputids", outputids);
            if (monitorpointtypes.size() > 0) {
                String temp = "";
                for (String type : monitorpointtypes) {
                    temp += type + ",";
                }
                temp = temp.substring(0, temp.length() - 1);
                paramMap.put("monitorpointtypes", temp);
            }
        }
        if (paramMap.get("pagesize") != null && paramMap.get("pagenum") != null) {
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()),
                    Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        if (paramMap.get("monitorpointtypes") != null && !"".equals(paramMap.get("monitorpointtypes"))) {
            paramMap.put("monitorpointtypes", Arrays.asList(paramMap.get("monitorpointtypes").toString().split(",")));
        } else {
            paramMap.put("monitorpointtypes", null);
        }
        //获取分页排口信息
        List<Map<String, Object>> listData = pollutionMapper.getAllPollutionAndOutPut(paramMap);
        String particularpollutants = "";
        String monitorpollutants = "";
        for (Map<String, Object> map : listData) {
            particularpollutants = "";
            monitorpollutants = "";
            paramMap.put("pollutionid", map.get("pollutionid"));
            paramMap.put("outputid", map.get("outputid"));
            paramMap.put("monitorpointtype", map.get("monitorpointtype").toString());
            List<Map<String, Object>> particularPollutant = particularPollutantsMapper.getLastVersionPollutantInfoByParamMap(paramMap);
            if (particularPollutant.size() > 0) {
                for (Map<String, Object> pollutant : particularPollutant) {
                    particularpollutants += pollutant.get("pollutantname") + "、";
                }
                if (StringUtils.isNotBlank(particularpollutants)) {
                    particularpollutants = particularpollutants.substring(0, particularpollutants.length() - 1);
                }
            }
            map.put("particularpollutants", particularpollutants);
            List<Map<String, Object>> monitorPollutant = pollutantFactorMapper.getPollutantSetInfoByParamMap(paramMap);
            if (monitorPollutant.size() > 0) {
                for (Map<String, Object> pollutant : monitorPollutant) {
                    monitorpollutants += pollutant.get("pollutantname") + "、";
                }
                if (StringUtils.isNotBlank(monitorpollutants)) {
                    monitorpollutants = monitorpollutants.substring(0, monitorpollutants.length() - 1);
                }
            }
            map.put("monitorpollutants", monitorpollutants);
        }
        //遍历获取特征污染物信息和监测污染物信息
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(listData);
        paramMap.put("total", pageInfo.getTotal());
        return listData;
    }

    /**
     * @author: chengzq
     * @date: 2019/6/26 0026 下午 3:43
     * @Description: 获取所有污染源名称和id
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionNameAndPkid(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionNameAndPkid(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 4:20
     * @Description: 自定义查询条件获取用户企业报警关联信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getUserEntAlarmRelationListByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getUserEntAlarmRelationListByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/7/12 0012 下午 4:20
     * @Description: 设置用户报警关联数据信息（选中数据）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public void setUserEntAlarmRelationData(String userid, Object formdata, String updateUserId) {
        //清理该用户的报警关联信息
        pollutionMapper.deleteUserEntAlarmRelationDataByUserId(userid);
        //添加用户的报警关联信息
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) formdata;
        if (dataList.size() > 0) {
            List<UserMonitorPointRelationDataVO> userMonitorPointRelationDataVOS = new ArrayList<>();
            String monitorpointtype;
            String pollutionid = null;
            String dgimn = null;
            String monitorpointid;
            List<String> selected;
            Date nowDate = new Date();
            for (Map<String, Object> map : dataList) {
                monitorpointtype = map.get("monitorpointtype").toString();
                selected = (List<String>) map.get("selected");
                for (String id : selected) {
                    if (id.contains("_output_")) {
                        pollutionid = id.split("_output_")[1].split(",")[0];
                        dgimn = id.split("_output_")[1].split(",")[1];
                        monitorpointid = id.split("_output_")[0];
                        UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                        userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                        userMonitorPointRelationDataVO.setDgimn(dgimn);
                        userMonitorPointRelationDataVO.setFkUserid(userid);
                        userMonitorPointRelationDataVO.setFkPollutionid(pollutionid);
                        userMonitorPointRelationDataVO.setFkMonitorpointid(monitorpointid);
                        userMonitorPointRelationDataVO.setFkMonitorpointtype(monitorpointtype);
                        userMonitorPointRelationDataVO.setUpdatetime(nowDate);
                        userMonitorPointRelationDataVO.setUpdateuser(updateUserId);
                        userMonitorPointRelationDataVOS.add(userMonitorPointRelationDataVO);
                    } else if (id.contains("_monitorpointname")) {
                        monitorpointid = id.replace("_monitorpointname", "").split(",")[0];
                        dgimn = id.replace("_monitorpointname", "").split(",")[1];
                        UserMonitorPointRelationDataVO userMonitorPointRelationDataVO = new UserMonitorPointRelationDataVO();
                        userMonitorPointRelationDataVO.setPkId(UUID.randomUUID().toString());
                        userMonitorPointRelationDataVO.setDgimn(dgimn);
                        userMonitorPointRelationDataVO.setFkUserid(userid);
                        userMonitorPointRelationDataVO.setFkMonitorpointid(monitorpointid);
                        userMonitorPointRelationDataVO.setFkMonitorpointtype(monitorpointtype);
                        userMonitorPointRelationDataVO.setUpdatetime(nowDate);
                        userMonitorPointRelationDataVO.setUpdateuser(updateUserId);
                        userMonitorPointRelationDataVOS.add(userMonitorPointRelationDataVO);
                    }
                }
            }
            if (userMonitorPointRelationDataVOS.size() > 0) {//分批次保存数据权限
                int insertLength = userMonitorPointRelationDataVOS.size();
                int i = 0;
                while (insertLength > 150) {
                    pollutionMapper.batchInsertUserEntRelation(userMonitorPointRelationDataVOS.subList(i, i + 150));
                    i = i + 150;
                    insertLength = insertLength - 150;
                }
                if (insertLength > 0) {
                    pollutionMapper.batchInsertUserEntRelation(userMonitorPointRelationDataVOS.subList(i, i + insertLength));
                }
              // pollutionMapper.batchInsertUserEntRelation(userMonitorPointRelationDataVOS);
            }
        }


    }

    /**
     * @author: lip
     * @date: 2019/7/27 0027 上午 10:42
     * @Description: 根据监测类型和污染源id获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPollutionDataByIdAndType(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionDataByIdAndType(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/7/31 0031 上午 10:06
     * @Description: 自定义查询条件获取持有排污许可证企业的信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getPWXKPollutionDataByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getPWXKPollutionDataByParamMap(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/8/1 0001 下午 1:25
     * @Description: 自定义查询条件获取排口停产信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getStopProductionOutPutByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getStopProductionOutPutByParamMap(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2019/8/10 0010 上午 10:37
     * @Description: gis-获取所有污染源企业信息和污染源在线状态
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getAllPollutionInfoAndStatus() {
        //获取所有污染源企业
        List<Map<String, Object>> allPollutions = pollutionMapper.getAllPollutionInfo();
        List<Map<String, Object>> allOutputs = pollutionMapper.getOutputStatusAndOutputInfo();
        Map<String, Object> result = new HashMap<>();
        int onlinenum = 0;
        int offlinenum = 0;
        int waterkeynum = 0;
        int gaskeynum = 0;
        if (allPollutions != null && allPollutions.size() > 0) {
            for (Map<String, Object> obj : allPollutions) {
                int status = 0;
                for (Map<String, Object> map : allOutputs) {
                    if ((obj.get("PK_PollutionID").toString()).equals(map.get("Pollutionid").toString())) {//当污染源相等时
                        if (map.get("Status") != null && !"".equals(map.get("Status").toString())) {//当状态不为空
                            if ("1".equals(map.get("Status").toString())) {//有在线排口，有一个在线排口，则该企业为在线企业
                                status = 1;
                                break;
                            } else {
                                if (status < Integer.parseInt(map.get("Status").toString())) {
                                    status = Integer.parseInt(map.get("Status").toString());
                                }
                            }
                        }
                    }
                }
                if (obj.get("FK_PollutionClass") != null) {
                    if ("1".equals(obj.get("FK_PollutionClass").toString()) || "3".equals(obj.get("FK_PollutionClass").toString())) {
                        waterkeynum += 1;
                    }
                    if ("2".equals(obj.get("FK_PollutionClass").toString()) || "3".equals(obj.get("FK_PollutionClass").toString())) {
                        gaskeynum += 1;
                    }
                }

                if (status == 0) {//离线
                    offlinenum += 1;
                } else if (status == 1) {//在线
                    onlinenum += 1;
                }
                obj.put("onlinestatus", status);
            }
        }
        result.put("total", (allPollutions != null && allPollutions.size() > 0) ? allPollutions.size() : 0);
        result.put("waterpollution", waterkeynum);
        result.put("gaspollution", gaskeynum);
        result.put("onlinepollution", onlinenum);
        result.put("offlinepollution", offlinenum);
        result.put("listdata", allPollutions);
        return result;
    }

    /**
     * @author: xsm
     * @date: 2019/8/12 0012 下午 4:42
     * @Description: 获取所有污染源企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public List<Map<String, Object>> getAllPollutionInfoByPollutionids(Map<String, Object> parammap) {
        return pollutionMapper.getAllPollutionInfoByPollutionids(parammap);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/21 0021 上午 10:04
     * @Description: 通过污染源id获取安全管理机构信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pkpollutionid]
     * @throws:
     */
    @Override
    public Map<String, Object> getSafeManageInfoByPollutionid(String pkpollutionid) {
        return pollutionMapper.getSafeManageInfoByPollutionid(pkpollutionid);
    }


    /**
     * @author: chengzq
     * @date: 2019/10/21 0021 上午 10:18
     * @Description: 修改安全管理机构信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int updateSafeManageInfo(Map<String, Object> paramMap) {
        return pollutionMapper.updateSafeManageInfo(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2019/10/23 0023 上午 9:41
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionByParamsMap(Map<String, Object> parammap) {
        return pollutionMapper.getPollutionByParamsMap(parammap);
    }

    /**
     * @author: xsm
     * @date: 2019/11/06 0006 上午 9:01
     * @Description: 根据污染源ID获取污染源下所有排口的MN号
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOutputInfosByPollutionID(String pollutionid) {
        return pollutionMapper.getOutputInfosByPollutionID(pollutionid);
    }

    /**
     * @author: xsm
     * @date: 2019/11/07 0007 上午 10:50
     * @Description: 根据监测类型获取重点排放量污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getKeyFlowPollutantsByParam(Map<String, Object> parammap) {
        return pollutantFactorMapper.getKeyFlowPollutantsByParam(parammap);
    }

    /**
     * @author: liyc
     * @date: 2019/11/11 0011 18:45
     * @Description: 档案首页  通过污染源的id获取污染源的基本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid]
     * @throws:
     **/
    @Override
    public Map<String, Object> getPollutionBasicInfoByPollutionId(String pollutionid) {
        return pollutionMapper.getPollutionBasicInfoByPollutionId(pollutionid);
    }


    /**
     * @author: chengzq
     * @date: 2019/11/25 0025 上午 9:35
     * @Description: 删除安全与企业相关数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int deleteSecurityRelevantData(Map<String, Object> paramMap) {

       /* Map<String, Object> tablenames = new HashMap<>();
        tablenames.put("T_BAS_EntSecurityPersonInfo","FK_PollutionID");//安全人员
        tablenames.put("T_AQ_PersionLicenseInfo","FK_PollutionID");
        tablenames.put("T_AQ_EntHazardousTechnology","Fk_PollutionID");
        tablenames.put("T_AQ_EmergencyDisposalCard","Fk_PollutionID");
        tablenames.put("T_AQ_EntMonitorChemicals","Fk_PollutionID");
        tablenames.put("T_AQ_EntSecurityPersonInfo","FK_PollutionID");
        tablenames.put("T_AQ_LicenseInfo","FK_PollutionID");
        tablenames.put("T_AQ_ProduceAnnualReport","FK_PollutionID");
        tablenames.put("T_AQ_SafeManageRules","Fk_PollutionID");
        tablenames.put("T_AQ_HiddenDanger","FK_PollutionID");
        tablenames.put("T_AQ_HiddenDangerTaskRecord","FK_Pollutionid");
        tablenames.put("T_AQ_RiskInfo","Fk_PollutionID");
        tablenames.put("T_AQ_ProductionAccident","FK_PollutionID");
        tablenames.put("T_AQ_ProductionDevice","FK_PollutionID");
        tablenames.put("T_AQ_SpecialEquipment","FK_PollutionID");//特种设备
        tablenames.put("T_AQ_MainBuilding","FK_PollutionID");//建构筑物*/

        //storageTankAreaInfoMapper.deleteStorageTankAreaInfoByPollutionID(paramMap);//根据污染源ID删除贮罐区及贮罐区下相关信息
        //paramMap.put("tablenames", tablenames);
        //pollutionMapper.deleteSecurityRelevantData(paramMap);
        return 1;
    }


    /**
     * @author: xsm
     * @date: 2019/11/26 0026 下午 3:39
     * @Description: 删除环保与企业相关数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public int deleteEnvRelevantData(Map<String, Object> paramMap) {
        List<Map<String, Object>> outPutInfosByParamMap = pollutionMapper.getOutPutInfosByParamMap(paramMap);
        List<String> collect = outPutInfosByParamMap.stream().filter(m -> m.get("DGIMN") != null).map(m -> m.get("DGIMN").toString()).collect(Collectors.toList());
        //删除状态表信息
        deviceStatusMapper.deleteDeviceStatusByMNs(collect);


        Map<String, Object> tablenames = new HashMap<>();
        tablenames.put("T_BAS_WaterOutputInfo","FK_Pollutionid");//废水排口
        tablenames.put("T_BAS_WaterOutPutPollutantSet","FK_PollutionID");//废水set
        tablenames.put("T_BAS_GASOutPutInfo","FK_PollutionID");//废气排口
        tablenames.put("T_BAS_GasOutPutPollutantSet","FK_PollutionID");//废气set
        tablenames.put("T_BAS_UnorganizedMonitorPointInfo","FK_Pollutionid");//无组织监测点
        tablenames.put("T_BAS_EarlyWarningSet","FK_PollutionID");//预警set
        tablenames.put("T_BAS_PollutionLabelData","FK_PollutionID");//标签
        tablenames.put("T_BAS_ParticularPollutants","FK_PollutionID");//特征污染物
        tablenames.put("T_SCWL_ProductInfo","FK_PollutionID");//主要产品
        tablenames.put("T_BAS_ParticularPollutants","FK_PollutionID");//特征污染物库
        tablenames.put("T_SCWL_RawMaterial","FK_PollutionID"); //原料及辅料
        tablenames.put("T_SCWL_FuelInfo","FK_PollutionID");//主要燃料
        tablenames.put("T_Project_Approval","FK_PollutionId");//建设项目环评
        tablenames.put("T_Project_Check","FK_PollutionID");//建设项目验收
        tablenames.put("T_PWXKZ_LicenceInfo","FK_PollutionID");//排污许可证
        tablenames.put("T_WXFW_LicenceInfo","FK_PollutionID");//危废许可证
        tablenames.put("T_HYFS_LicenceInfo","FK_PollutionID");//辐射安全许可证
        tablenames.put("T_HJWF_CaseInfo","FK_PollutionID");//行政处罚
        tablenames.put("T_JCZF_TaskInfo","FK_PollutionID");//监察执法
        tablenames.put("T_XFTS_PetitionInfo","FK_PollutionID");//信访投诉
        tablenames.put("T_XYPJ_EnvCreditEvaluation","FK_PollutionID");//信用评价
        tablenames.put("T_QJSC_CleanerProductionInfo","FK_PollutionID");//清洁生产
        tablenames.put("T_BAS_UserMonitorPointRelationData","FK_PollutionID");//权限数据
        tablenames.put("Base_EntUser","ent_id");//企业用户数据
        paramMap.put("tablenames", tablenames);
        return pollutionMapper.deleteSecurityRelevantData(paramMap);
    }

    /**
     * @author: lip
     * @date: 2019/12/2 0002 下午 1:51
     * @Description: 获取企业（监测点）传输率、有效率、传输有效率列表信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public PageInfo<Map<String, Object>> getMonitorPointTransmissionEffectiveRateList(Map<String, Object> paramMap) {
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Double ratevalue;
        String ratevaluestring;
        String format = "######0";
        List<Map<String, Object>> dataList = pollutionMapper.getMonitorPointTransmissionEffectiveRateList(paramMap);
        for (Map<String, Object> mapIndex : dataList) {
            ratevalue = Double.parseDouble(mapIndex.get("ratevalue").toString());
            ratevaluestring = DataFormatUtil.formatDouble(format, 100d*ratevalue)+"%";
            mapIndex.put("ratevalue",ratevaluestring);
        }
        return new PageInfo<>(dataList);
    }

    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 4:32
     * @Description: 通过自定义参数获取所有企业下的废水，废气，雨水，厂界恶臭，厂界小型站因子
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [param]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getAllPollutionPollutantInfoByParamMap(Map<String, Object> param) {
        return pollutantFactorMapper.getAllPollutionPollutantInfoByParamMap(param);
    }


    /**
     * @author: chengzq
     * @date: 2019/12/11 0011 下午 5:04
     * @Description: 通过污染因子获取企业信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getPollutionInfoByPollutantcodes(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionInfoByPollutantcodes(paramMap);
    }

    /**
     * @author: chengzq
     * @date: 2020/1/7 0007 上午 10:26
     * @Description: 通过自定义参数获取企业最高风险等级
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getMaxRiskLevelInfoByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getMaxRiskLevelInfoByParamMap(paramMap);
    }

    /**
     *
     * @author: lip
     * @date: 2020/3/3 0003 上午 9:44
     * @Description: 根据企业ID获取企业点位信息（点位状态，传输有效率，监测污染物）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorPointDataByPollutionId(String pollutionId) {
        Map<String,Object> resultMap = new HashMap<>();
        //1,点位状态+传输有效率
        List<Map<String,Object>> pointDataList = pollutionMapper.getMonitorPointInfoByPollutionId(pollutionId);
        if (pointDataList.size()>0){
            Map<String,List<String>> keyAndPollutantNames = new HashMap<>();
            String monitorpointid;
            String monitorpointtypecode;
            String pollutantname;
            String key;
            //2,监测污染物
            List<Map<String,Object>> pollutantDataList = pollutionMapper.getPollutantDataByPollutionId(pollutionId);
            if (pollutantDataList.size()>0){
                for (Map<String,Object> pollutantData:pollutantDataList){
                    monitorpointid = pollutantData.get("monitorpointid")!=null?pollutantData.get("monitorpointid").toString():"";
                    monitorpointtypecode = pollutantData.get("monitorpointtypecode")!=null?pollutantData.get("monitorpointtypecode").toString():"";
                    pollutantname = pollutantData.get("pollutantname")!=null?pollutantData.get("pollutantname").toString():"";
                    key = monitorpointtypecode+"#"+monitorpointid;
                    if (keyAndPollutantNames.containsKey(key)){
                        keyAndPollutantNames.get(key).add(pollutantname);
                    }else {
                        List<String> pollutants = new ArrayList<>();
                        pollutants.add(pollutantname);
                        keyAndPollutantNames.put(key,pollutants);
                    }
                }
            }
            List<Map<String,Object>> monitorpointdata = new ArrayList<>();
            Double transmissionrate = 0d;
            Double effectiverate = 0d;
            Double transmissioneffectiverate=0d;
            int total = 0;
            for (Map<String,Object> pointData:pointDataList){
                total++;
                Map<String,Object> dataMap = new HashMap<>();
                dataMap.put("monitorpointtypecode",pointData.get("monitorpointtypecode"));
                dataMap.put("monitorpointtypename",pointData.get("monitorpointtypename"));
                dataMap.put("outputname",pointData.get("outputname"));
                dataMap.put("onlinestatus",pointData.get("onlinestatus"));
                dataMap.put("outputstatus",pointData.get("outputstatus"));
                dataMap.put("updatedate",pointData.get("countdate"));
                transmissionrate += pointData.get("transmissionrate")!=null?Double.parseDouble(pointData.get("transmissionrate").toString()):0d;
                effectiverate += pointData.get("effectiverate")!=null?Double.parseDouble(pointData.get("effectiverate").toString()):0d;
                transmissioneffectiverate += pointData.get("transmissioneffectiverate")!=null?Double.parseDouble(pointData.get("transmissioneffectiverate").toString()):0d;
                dataMap.put("transmissionrate",formatRate(pointData.get("transmissionrate")));
                dataMap.put("effectiverate",formatRate(pointData.get("effectiverate")));
                dataMap.put("transmissioneffectiverate",formatRate(pointData.get("transmissioneffectiverate")));
                monitorpointid = pointData.get("monitorpointid")!=null?pointData.get("monitorpointid").toString():"";
                monitorpointtypecode = pointData.get("monitorpointtypecode")!=null?pointData.get("monitorpointtypecode").toString():"";
                key = monitorpointtypecode+"#"+monitorpointid;
                dataMap.put("monitorpollutants",DataFormatUtil.FormatListToString(keyAndPollutantNames.get(key),"、"));
                monitorpointdata.add(dataMap);
            }
            Map<String,Object> pollutiondata = new HashMap<>();
            pollutiondata.put("transmissionrate",formatRate(transmissionrate/total));
            pollutiondata.put("effectiverate",formatRate(effectiverate/total));
            pollutiondata.put("transmissioneffectiverate",formatRate(transmissioneffectiverate/total));
            resultMap.put("pollutiondata",pollutiondata);
            resultMap.put("monitorpointdata",monitorpointdata);
        }
        return resultMap;
    }

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 上午 10:02
     * @Description: 通过用户ID获取企业基本信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public PollutionVO getPollutionDetailByUserId(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionDetailByUserId(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/03/24 0024 下午 1:25
     * @Description: 获取企业信息提醒
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramMap]
     * @throws:
     */
    @Override
    public Map<String, Object> getPollutionInfoRemindData(Map<String, Object> paramMap) {
        PollutionVO obj = pollutionMapper.getPollutionDetailByUserId(paramMap);
        Map<String,Object> result =new HashMap<>();
        if (obj!=null){//该用户关联企业
            String pollutionid = obj.getPkpollutionid();
            result.put("pollutionid",pollutionid);
            Map<String,Object> datamap =new HashMap<>();
            datamap.put("licence",0);
            paramMap.put("pollutionid",pollutionid);
            //排污许可证
            Map<String,Object>  licencemap = licenceMapper.getNewPWXKZLicenseByPollutionId(paramMap);
            if (licencemap!=null){
                    datamap.put("licence",licencemap.get("isoverdue"));
            }

            result.put("reminddata",datamap);
        }
        return result;
    }


    /**
     *
     * @author: lip
     * @date: 2020/3/3 0003 上午 11:24
     * @Description: 格式化数据率（保留整数）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    private Object formatRate(Object rateData) {
        String rateString = "";

        if (rateData!=null){
            Double data = Double.parseDouble(rateData.toString());
            rateString =   DataFormatUtil.SaveOneAndSubZero(data*100)+"%";
        }
        return rateString;

    }


    /**
     * @author: chengzq
     * @date: 2019/11/6 0006 上午 9:10
     * @Description: 通过自定义参数获取污染源信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [parammap]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getOutPutInfosByParamMap(Map<String, Object> parammap) {
        return pollutionMapper.getOutPutInfosByParamMap(parammap);
    }


    @Override
    public List<Map<String, Object>> getPollutionOutputMn(Map<String, Object> paramMap) {
        return alarmTaskDisposeManagementMapper.getAllOutputMn(paramMap);
    }

    /**
     * @author: xsm
     * @date: 2020/06/18 0018 上午 10:22
     * @Description: 统计某个时间范围内监测点传输率、有效率、传输有效率（100%,75%-100%,<75%）占比个数
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public Map<String, Object> countMonitorPointTransmissionEffectiveRateNum(Map<String, Object> paramMap) {
        Double ratevalue;
        Map<String, Object> resultmap = new HashMap<>();
        //String ratevaluestring;
        String format = "######0";
        int num1 = 0;
        int num2 = 0;
        int num3 = 0;
        List<Map<String, Object>> dataList = pollutionMapper.getManyMonitorPointTransmissionEffectiveRateList(paramMap);
        for (Map<String, Object> mapIndex : dataList) {
            ratevalue = Double.parseDouble(mapIndex.get("ratevalue").toString());
            String value = DataFormatUtil.formatDouble(format, 100d*ratevalue);
            if ("100".equals(value)){
                num1+=1;
            }else if(Integer.valueOf(value)>=75 && Integer.valueOf(value)<100){
                num2+=1;
            }else if(Integer.valueOf(value)<75){
                num3+=1;
            }
            /*ratevaluestring = DataFormatUtil.formatDouble(format, 100d*ratevalue)+"%";
            mapIndex.put("ratevalue",ratevaluestring);*/
        }
        resultmap.put("level1",num1);
        resultmap.put("level2",num2);
        resultmap.put("level3",num3);
        return resultmap;
    }

    /**
     * @author: xsm
     * @date: 2020/06/19 0019 下午 4:44
     * @Description: 修改污染源信息(只更新部分数据)
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public void updatePollutantPartInfo(PollutionVO record, List<Map<String, Object>> labels) {
        PollutionVO pollutionVO = pollutionMapper.selectByPrimaryKey(record.getPkpollutionid());
        String entsocialcreditcode = record.getEntsocialcreditcode();
        String entsocialcreditcode1 = pollutionVO.getEntsocialcreditcode();
        String pollutioncode = "";
        Map<String, Object> paramMap = new HashMap<>();
        if (entsocialcreditcode != null && !entsocialcreditcode.equals(entsocialcreditcode1)) {
            //如果有社会信用代码生成污染源code，没有不生成
            if (StringUtils.isNotBlank(entsocialcreditcode)) {
                paramMap.put("EntSocialCreditCode", entsocialcreditcode);
                functionMapper.getPollutionCode(paramMap);
                if (paramMap.get("NewPollutionCode") != null) {
                    pollutioncode = paramMap.get("NewPollutionCode").toString();
                }
                record.setPollutioncode(pollutioncode);
            }
        }
       /* List<PollutionLabelDataVO> pollutionLabelDataVOS = new ArrayList<>();
        //通过污染源id删除标签
        pollutionLabelDataMapper.deleteByPolltionid(record.getPkPollutionid());
        for (Map label : labels) {
            PollutionLabelDataVO pollutionLabelDataVO = new PollutionLabelDataVO();
            pollutionLabelDataVO.setPkId(UUID.randomUUID().toString());
            pollutionLabelDataVO.setFkPollutionid(record.getPkPollutionid());
            pollutionLabelDataVO.setFkPollutionlabelid(Integer.valueOf(label.get("labelcode").toString()));
            pollutionLabelDataVOS.add(pollutionLabelDataVO);
        }
        //新增标签
        if (pollutionLabelDataVOS.size() > 0) {
            pollutionLabelDataMapper.insertLabels(pollutionLabelDataVOS);
        }*/
        pollutionMapper.updatePollutionInfoByPrimaryKey(record);

    }

    @Override
    public PageInfo<Map<String, Object>> getManyMonitorPointTransmissionEffectiveRateList(Map<String, Object> paramMap) {
        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        Double ratevalue;
        String ratevaluestring;
        String format = "######0";
        List<Map<String, Object>> dataList = pollutionMapper.getManyMonitorPointTransmissionEffectiveRateList(paramMap);
        for (Map<String, Object> mapIndex : dataList) {
            ratevalue = Double.parseDouble(mapIndex.get("ratevalue").toString());
            ratevaluestring = DataFormatUtil.formatDouble(format, 100d*ratevalue)+"%";
            mapIndex.put("ratevalue",ratevaluestring);
        }
        return new PageInfo<>(dataList);
    }

    @Override
    public List<Map<String, Object>> getIsUseMonitorPointTypeData() {
        return pollutionMapper.getIsUseMonitorPointTypeData();
    }

    /**
     *
     * @author: lip
     * @date: 2020/8/18 0018 上午 11:49
     * @Description: 获取企业安全相关信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
    */
    @Override
    public PageInfo<Map<String, Object>> getSafePollutionListByParam(Map<String, Object> paramMap) {

        if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {//分页数据
            PageHelper.startPage(Integer.parseInt(paramMap.get("pagenum").toString()), Integer.parseInt(paramMap.get("pagesize").toString()));
        }
        List<Map<String, Object>> dataList = pollutionMapper.getSafePollutionListByParam(paramMap);
        return  new PageInfo<>(dataList);
    }

    @Override
    public List<Map<String, Object>> getHBPointDataList() {
        return pollutionMapper.getHBPointDataList();
    }

    /**
     * @author: chengzq
     * @date: 2020/12/9 0009 下午 6:33
     * @Description: 通过企业id获取企业用户信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @Override
    public List<String> getUserInfoByPollution(String fkpollutionid) {
        return pollutionMapper.getUserInfoByPollution(fkpollutionid);
    }

    /**
     * @author: xsm
     * @date: 2021/02/01 09:46
     * @Description: 根据自定义参数统计某类型所有点位某污染的小时浓度对比
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: monitortime:yyyy-MM-dd HH
     * @return:
     */
    @Override
    public Map<String, Object> getMonitorPointHourConcentrationDataByParam(List<String> mns, Map<String, Object> mn_name, String monitortime, String pollutantcode,Map<String, Double> mnandstand) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> datalist = new ArrayList<>();
        List<Map<String, Object>> nonull_datalist = new ArrayList<>();//接收小时数据值为空的数据
        //获取表头
        List<Map<String, Object>> tables = new ArrayList<>();
        tables = getTableTitleList(monitortime);
        if (mns.size() > 0) {
            String ymd = monitortime.substring(0, 10);
            int hour = Integer.parseInt(monitortime.substring(11, 13));
            if (hour == 0) {//当为0点时  只查出0点和前一天23点的数据
                Date endDate = DataFormatUtil.parseDate(monitortime + ":00:00");
                Date startDate = DataFormatUtil.addHourDate(endDate,-1);
                String previoustime = DataFormatUtil.getDateYMDH(startDate);
                Criteria criteria = new Criteria();
                Criteria criteria2 = new Criteria();
                criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
                criteria2.and("HourDataList.PollutantCode").is(pollutantcode);
                List<Document> documents = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("HourDataList.PollutantCode").as("PollutantCode").and("HourDataList.AvgStrength").as("AvgStrength")
                ), "HourData", Document.class).getMappedResults();
                Map<String, List<Document>> documentMap = new HashMap<>();
                if (documents != null && documents.size() > 0) {
                    documentMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                }
                for (String mn : mns) {
                        Object previousvalue = null;
                        Object lastvalue = null;
                        Object valueste = null;
                        Double standvalue = 0d;
                        if (mnandstand.get(mn)!=null){
                            standvalue = mnandstand.get(mn);
                        }
                        List<Document> mndocument = documentMap.get(mn);
                        Map<String, Object> map = new HashMap<>();
                        map.put("dgimn", mn);
                        map.put("monitorpointname", mn_name.get(mn));
                        map.put("monitortime_"+"0h", "");
                        if (mndocument != null) {
                                for (Document document : mndocument) {
                                    String onetime = document.get("MonitorTime")!=null?DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")):"";
                                    if (monitortime.equals(onetime)){//时间相等
                                        map.put("monitortime_"+"0h", document.get("AvgStrength"));
                                        if (standvalue!=0d&&document.get("AvgStrength")!=null&&!"".equals(document.get("AvgStrength").toString())){
                                          if (Double.valueOf(document.get("AvgStrength").toString())>=standvalue){
                                              map.put("isoverflag_"+"0h", 1);
                                          }else{
                                              map.put("isoverflag_"+"0h", 0);
                                          }
                                        }else{
                                            map.put("isoverflag_"+"0h", 0);
                                        }
                                        lastvalue = document.get("AvgStrength");
                                    }
                                    if (onetime.equals(previoustime)){
                                        previousvalue = document.get("AvgStrength");
                                    }
                                }
                        }
                        if (previousvalue!=null&&lastvalue!=null){
                            valueste =  DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(((Double.valueOf(lastvalue.toString())-Double.valueOf(previousvalue.toString()))*100)/Double.valueOf(previousvalue.toString())));
                        }
                        map.put("hbfd_value", valueste);
                        if (valueste!=null) {//不为空
                            datalist.add(map);
                        }else{
                            nonull_datalist.add(map);
                        }
                    }
            } else {
                //获取时间
                List<String> times = DataFormatUtil.separateTimeForHour(ymd + " 00:00:00", monitortime + ":00:00", 1);
                times.add(monitortime);
                Date startDate = DataFormatUtil.parseDate(ymd + " 00:00:00");
                Date endDate = DataFormatUtil.parseDate(monitortime + ":00:00");
                Criteria criteria = new Criteria();
                Criteria criteria2 = new Criteria();
                criteria.and("DataGatherCode").in(mns).and("MonitorTime").gte(startDate).lte(endDate);
                criteria2.and("HourDataList.PollutantCode").is(pollutantcode);
                List<Document> documents = mongoTemplate.aggregate(newAggregation(
                        match(criteria), unwind("HourDataList"), match(criteria2), project("DataGatherCode", "MonitorTime").and("HourDataList.PollutantCode").as("PollutantCode").and("HourDataList.AvgStrength").as("AvgStrength")
                ), "HourData", Document.class).getMappedResults();
                Map<String, List<Document>> documentMap = new HashMap<>();
                if (documents != null && documents.size() > 0) {
                    documentMap = documents.stream().collect(Collectors.groupingBy(m -> m.get("DataGatherCode").toString()));
                }
                   for (String mn : mns) {
                        Object previousvalue = null;
                        Object lastvalue = null;
                        Object valueste = null;
                       Double standvalue = 0d;
                       if (mnandstand.get(mn)!=null){
                           standvalue = mnandstand.get(mn);
                       }
                        List<Document> mndocument = documentMap.get(mn);
                        Map<String, Object> map = new HashMap<>();
                        map.put("dgimn", mn);
                        map.put("monitorpointname", mn_name.get(mn));
                            for(int i=0;i<times.size();i++) {
                                String ymdh = times.get(i);
                                map.put("monitortime_"+i+"h", "");
                                if (mndocument != null) {
                                for (Document document : mndocument) {
                                    String onetime = document.get("MonitorTime")!=null?DataFormatUtil.getDateYMDH(document.getDate("MonitorTime")):"";
                                    if (ymdh.equals(onetime)){//时间相等
                                        map.put("monitortime_"+i+"h", document.get("AvgStrength"));
                                        if (standvalue!=0d&&document.get("AvgStrength")!=null&&!"".equals(document.get("AvgStrength").toString())){
                                            if (Double.valueOf(document.get("AvgStrength").toString())>=standvalue){
                                                map.put("isoverflag_"+i+"h", 1);
                                            }else{
                                                map.put("isoverflag_"+i+"h", 0);
                                            }
                                        }else{
                                            map.put("isoverflag_"+i+"h", 0);
                                        }
                                        if (i == times.size()-2){
                                            previousvalue = document.get("AvgStrength");
                                        }
                                        if (i == times.size()-1){
                                            lastvalue = document.get("AvgStrength");
                                        }
                                    }
                                }
                            }
                        }
                        if (previousvalue!=null&&lastvalue!=null){
                                if (Double.valueOf(lastvalue.toString())==0 && Double.valueOf(previousvalue.toString())==0){
                                    valueste = "0";
                                }else{
                                   if(Double.valueOf(previousvalue.toString())==0){
                                       valueste = "100";
                                   }else{
                                       double numvalue= ((Double.valueOf(lastvalue.toString())-Double.valueOf(previousvalue.toString()))*100)/Double.valueOf(previousvalue.toString());
                                       valueste =  DataFormatUtil.subZeroAndDot(DataFormatUtil.formatDoubleSaveTwo(numvalue));
                                   }
                                }
                        }
                        map.put("hbfd_value", valueste);
                        if (valueste!=null) {//不为空
                            datalist.add(map);
                        }else{
                            nonull_datalist.add(map);
                        }
                    }
            }
        }
        if (datalist!=null&&datalist.size()>0) {
            //倒序排
            datalist= datalist.stream().sorted(Comparator.comparing(m -> Double.valueOf(((Map) m).get("hbfd_value").toString())).reversed()).collect(Collectors.toList());
        }
        if (nonull_datalist!=null&&nonull_datalist.size()>0) {
            nonull_datalist = nonull_datalist.stream()
                    .sorted(Comparator.comparing(m -> m.get("monitorpointname").toString(), (x,y) ->{
                        // ToFirstChar 将汉字首字母转为拼音
                        x = FormatUtils.ToFirstChar(x).toUpperCase();
                        y = FormatUtils.ToFirstChar(y).toUpperCase();
                        Collator clt = Collator.getInstance();
                        return clt.compare(x, y);
                    })).collect(Collectors.toList());
            //nonull_datalist = nonull_datalist.stream().sorted(Comparator.comparing(m -> ((Map) m).get("monitorpointname").toString())).collect(Collectors.toList());
            //List<Map<String, Object>> collect = nonull_datalist.stream().sorted(Comparator.comparing(m -> m.get("monitorpointname").toString())).collect(Collectors.toList());
            datalist.addAll(nonull_datalist);
        }
        result.put("datalist", datalist);
        result.put("tabletitledata", tables);

        return result;
    }

    @Override
    public List<Map<String, Object>> countComplateData(Map<String, Object> paramMap) {
        return pollutionMapper.countComplateData(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutionInfoByParamMaps(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionInfoByParamMaps(paramMap);
    }

    @Override
    public List<Map<String, Object>> getUserEntInfoByParamMap(Map<String, Object> paramMap) {
        return pollutionMapper.getUserEntInfoByParamMap(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPointListByParam(Map<String, Object> paramMap) {





        return pollutionMapper.getPointListByParam(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutionSecurityPointMn(Map<String, Object> paramMap) {
        return pollutionMapper.getPollutionSecurityPointMn(paramMap);
    }

    @Override
    public List<Map<String, Object>> getPollutionVideoAlarmDateDataByMonthTime(Map<String, Object> paramMap) {
        return videoCameraMapper.getPollutionVideoAlarmDateDataByMonthTime(paramMap);
    }

    @Override
    public List<Map<String, Object>> getAllIsUsedMonitorPointTypes() {
        return pollutionMapper.getAllIsUsedMonitorPointTypes();
    }

    @Override
    public List<Map<String, Object>> countEntRateDataGroupByIndustryType() {
        List<Map<String, Object>> datalist = new ArrayList<>();
        datalist = pollutionMapper.countEntRateDataGroupByIndustryType();
        if (datalist!=null&&datalist.size()>0){
            int total = 0;
            for (Map<String, Object> map:datalist){
                if (map.get("entnum")!=null&&!"".equals(map.get("entnum").toString())){
                    total += Integer.valueOf(map.get("entnum").toString());
                }
            }
            if (total>0){
                for (Map<String, Object> map:datalist){
                    if (map.get("entnum")!=null&&!"".equals(map.get("entnum").toString())){
                        map.put("proportion",((Integer.valueOf(map.get("entnum").toString())) * 100/total)+"%");
                    }else{
                        map.put("proportion","-");
                    }
                }
            }
        }
        return datalist;
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID获取该企业所监测的监测类型
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [fkpollutionid]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntMonitorPointTypeByEntID(Map<String,Object> param) {
        return pollutionMapper.getEntMonitorPointTypeByEntID(param);
    }

    /**
     * @author: xsm
     * @date: 2021/08/12 0012 下午 13:34
     * @Description: 根据企业ID和监测类型获取该类型企业点位监测污染物
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [pollutionid、monitorpointtype]
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntMonitorPointPollutantDataByParam(Map<String, Object> param) {
        return pollutionMapper.getEntMonitorPointPollutantDataByParam(param);
    }

    @Override
    public List<Map<String, Object>> getEntPointMNDataByParam(Map<String, Object> param) {
        return pollutionMapper.getEntPointMNDataByParam(param);
    }

    /**
     * @author: xsm
     * @date: 2021/09/13 0013 上午 10:31
     * @Description: 根据企业ID和监测类型获取该类型企业点位信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @throws:
     */
    @Override
    public List<Map<String, Object>> getEntPointInfoByEntIDAndTypes(Map<String, Object> param) {
        return pollutionMapper.getEntPointInfoByEntIDAndTypes(param);
    }

    @Override
    public long countTotalByLabel(String labelcode) {
        return pollutionMapper.countTotalByLabel(labelcode);
    }

    @Override
    public long countTotalByClass(Map<String, Object> paramMap) {
        return pollutionMapper.countTotalByClass(paramMap);
    }

    @Override
    public List<Map<String, Object>> countTotalByIndustry() {
        return pollutionMapper.countTotalByIndustry();
    }

    @Override
    public List<Map<String, Object>> countPWPollutionData() {
        return pollutionMapper.countPWPollutionData();
    }

    @Override
    public long countStopTotal(String nowDay) {
        return pollutionMapper.countStopTotal(nowDay);
    }

    @Override
    public List<String> getTotalIdsByParam() {
        return pollutionMapper.getTotalIdsByParam();
    }

    @Override
    public List<String> getStopIdsTotal(String nowDay) {
        return pollutionMapper.getStopIdsTotal(nowDay);
    }

    @Override
    public List<String> getTotalIdsByClass(Map<String, Object> paramMap) {
        return pollutionMapper.getTotalIdsByClass(paramMap);
    }

    @Override
    public List<String> getTotalIdsByLabel(String labelcode) {
        return pollutionMapper.getTotalIdsByLabel(labelcode);
    }

    @Override
    public List<Map<String, Object>> getEntMonitorPointTypeByParam(Map<String, Object> param) {
        return pollutionMapper.getEntMonitorPointTypeByParam(param);
    }

    @Override
    public List<Map<String, Object>> countEntStandingBookDataByPollutionID(Map<String, Object> param) {
        return pollutionMapper.countEntStandingBookDataByPollutionID(param);
    }

    @Override
    public long countPWTotalByParam(HashMap<Object, Object> objectObjectHashMap) {
        return pollutionMapper.countPWTotalByParam(objectObjectHashMap);
    }

    @Override
    public List<String> getAllPWIds() {
        return pollutionMapper.getAllPWIds();
    }

    @Override
    public List<Map<String, Object>> countEntControlData() {
        return pollutionMapper.countEntControlData();
    }

    @Override
    public List<Map<String, Object>> countEntRegionData() {
        return pollutionMapper.countEntRegionData();
    }

    @Override
    public List<Map<String, Object>> getEntQRListDataByParamMap(Map<String, Object> jsonObject) {
        return pollutionMapper.getEntQRListDataByParamMap(jsonObject);
    }

    @Override
    public int updateQRDataByParam(Map<String, Object> param) {
        return  pollutionMapper.updateQRDataByParam(param);
    }

    @Override
    public void addQRDataByParam(Map<String, Object> param) {
        pollutionMapper.addQRDataByParam(param);
    }

    @Override
    public List<Map<String, Object>> getEntLabelDataListById(String pollutionid) {
        return pollutionMapper.getEntLabelDataListById(pollutionid);
    }

    @Override
    public long getQJSCNumByPid(String pollutionid) {
        return pollutionMapper.getQJSCNumByPid(pollutionid);
    }

    private List<Map<String,Object>> getTableTitleList(String monitortime) {
        String hour = monitortime.substring(11,13);
        int totalnum = Integer.parseInt(hour);
        List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> stationname = new HashMap<>();
            stationname.put("label", "监测点名称");
            stationname.put("prop", "monitorpointname");
            stationname.put("minwidth", "150px");
            stationname.put("headeralign", "center");
            stationname.put("fixed", "left");
            stationname.put("align", "center");
            stationname.put("showhide", true);
            dataList.add(stationname);

        for (int i = 0; i <= totalnum; i++) {
            Map<String, Object> counttimenum = new HashMap<>();
            counttimenum.put("label", i+"时");
            counttimenum.put("prop", "monitortime_"+i+"h");
            counttimenum.put("width", "100px");
            counttimenum.put("headeralign", "center");
            counttimenum.put("align", "center");
            counttimenum.put("showhide", true);
            dataList.add(counttimenum);
        }
            Map<String, Object> hb_map = new HashMap<>();
            hb_map.put("label", "环比变化幅度");
            hb_map.put("type", "percentage");
            hb_map.put("prop", "hbfd_value");
            hb_map.put("width", "120px");
            hb_map.put("headeralign", "center");
            hb_map.put("fixed", "right");
            hb_map.put("align", "center");
            hb_map.put("showhide", true);
            dataList.add(hb_map);
        return dataList;
    }
}
