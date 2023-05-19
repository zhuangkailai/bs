package com.tjpu.auth.service.impl.system;


import com.tjpu.auth.common.utils.RedisTemplateUtil;
import com.tjpu.auth.dao.codeTable.CommonSelectFieldConfigMapper;
import com.tjpu.auth.dao.system.ButtonMapper;
import com.tjpu.auth.dao.system.OperateLogMapper;
import com.tjpu.auth.dao.system.SysMenuMapper;
import com.tjpu.auth.dao.system.UserInfoMapper;
import com.tjpu.auth.model.codeTable.CommonSelectFieldConfigVO;
import com.tjpu.auth.model.system.ButtonVO;
import com.tjpu.auth.model.system.OperateLogVO;
import com.tjpu.auth.model.system.SysMenuVO;
import com.tjpu.auth.model.system.UserInfoVO;
import com.tjpu.auth.service.common.CommonServiceSupport;
import com.tjpu.auth.service.system.OperateLogService;
import com.tjpu.pk.common.utils.DataFormatUtil;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Author: zzc
 * @Date: 2018/7/11 19:02
 * @Description:日志service层实现类
 */
@Service
@Transactional
public class OperateLogServiceImpl implements OperateLogService {
    @Autowired
    private OperateLogMapper operateLogMapper;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private CommonSelectFieldConfigMapper commonSelectFieldConfigMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private ButtonMapper buttonMapper;

    @Override
    public int insert(OperateLogVO operateLogVO) {
        return operateLogMapper.insert(operateLogVO);
    }

    /**
     * @param operateLogVO
     * @author: lip
     * @date: 2018年8月6日 下午2:02:11
     * @Description: 此处实现在切面配置的切点处自动扫描实现
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @Override
    public void insertByAop(OperateLogVO operateLogVO) {
    }

    /**
     * @param operateType                    :操作类型
     * @param paramMap                       :自定义参数（tablename:被操作的表的表名称;resetUserId:被重置密码的用户的用户ID）
     * @param compareDataMap                 :进行修改操作后的对比的数据
     * @param oldMapData:修改前的数据（回显在修改页面的数据）
     * @param newMapData:修改后的数据（点击保存时页面上的数据）
     * @author: xsm
     * @date: 2018/8/12 10:34
     * @Description: 通用日志存储，保存记录用户操作的日志信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @return:
     */
    public void saveUserOperationLog(String operateType,
                                     Map<String, Object> paramMap, Map<String, Object> compareDataMap,
                                     Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        // 日志描述
        String logDescription = "";
        // 日志类型
        String logType = "";
        //操作人姓名
        String userName = "";
        switch (operateType) {// 判断操作类型
            case "login":// 用户登陆_日志
                logDescription = "登录了系统。";
                if (paramMap.get("agent")!=null){
                    logDescription ="【"+paramMap.get("agent")+"】"+"登录了系统。";
                }
                logType = "loginlog";
                break;
            case "exit":// 用户退出_日志
                logDescription = "退出了系统。";
                logType = "loginlog";
                break;
            case "editpwd":// 用户修改密码_日志
                logDescription = "修改了密码。";
                logType = "handlelog";
                break;
            case "resetpwd":// 用户重置密码_日志
                if (paramMap.get("resetuserid") != null) {
                    // 被重置密码的用户的用户ID
                    String resetUserId = paramMap.get("resetuserid").toString();
                    // 根据用户ID查询出被重置密码的用户信息
                    UserInfoVO user = userInfoMapper.selectByPrimaryKey(resetUserId);
                    // 获取被重置、修改密码的用户帐号
                    String userAccount = user.getUserAccount();
                    // 日志描述
                    logDescription = "重置了帐号为【" + userAccount + "】的用户的帐号密码。";
                    logType = "handlelog";
                    break;
                }
            case "add":// 用户进行添加操作_日志
                if (paramMap.get("tablename") != null) {
                    // 被操作的表的表名
                    String tableName = paramMap.get("tablename").toString();
                    // 日志描述
                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    logType = "handlelog";
                    break;
                }
            case "delete":// 用户进行删除操作_日志
                if (paramMap.get("tablename") != null) {
                    // 被操作的表的表名
                    String tableName = paramMap.get("tablename").toString();
                    // 日志描述
                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    logType = "handlelog";
                    break;
                }
            case "edit":// 用户进行修改操作_日志
                if (paramMap.get("tablename") != null) {
                    // 被操作的表的表名
                    String tableName = paramMap.get("tablename").toString();
                    // 日志描述
                    logDescription = doOperateLogVO(operateType, tableName, oldMapData, newMapData);
                    if (!"Base_userInfo".equals(tableName) && !"".equals(logDescription)) {
                        Map<String, Object> oldMaps = new HashMap<String, Object>();
                        Map<String, Object> newMaps = new HashMap<String, Object>();
                        if (compareDataMap != null && compareDataMap.size() > 0) {
                            for (Map.Entry<String, Object> entry : compareDataMap.entrySet()) {
                                String key = entry.getKey();
                                oldMaps.put(key, oldMapData.get(key));
                                newMaps.put(key, newMapData.get(key));
                            }
                        }
                        // 拼接具体的修改操作描述操作描述
                        String lastDescription = fomateEditLog(tableName, oldMaps, newMaps);
                        if (!"".equals(lastDescription)) {
                            logDescription += lastDescription;
                        } else {
                            logDescription = "";
                        }
                        logType = "handlelog";
                        break;
                    }
                }

        }
        OperateLogVO operateLogVO = new OperateLogVO();
        // 生成日志主键ID
        String uuid = CommonServiceSupport.getUUID();
        // 获取操作人姓名 username
        /*if (!"exit".equals(operateType)) {
            userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        } else {*/
            if (paramMap.get("username") != null) {
                userName = paramMap.get("username").toString();
            }
       // }
        if (!"".equals(userName) && userName != null && !"".equals(logDescription)) {
            // 将获取到的值存储到日志实体对应字段中
            operateLogVO.setBaseOperateId(uuid);// 日志主键ID
            operateLogVO.setBaseOperateType(operateType); // 基础操作类型
            operateLogVO.setBaseOperateContent(logDescription); // 基础操作内容
            operateLogVO.setBaseOperatePerson(userName); // 基础操作人
            operateLogVO.setBaseOperateDatetime(DataFormatUtil.getDate()); // 基础操作时间
            operateLogVO.setBaseLogType(logType); // 基础日志类型
            if (paramMap!=null&&paramMap.get("baseOperateIp")!=null){
                operateLogVO.setBaseOperateIp(paramMap.get("baseOperateIp").toString()); // 客户端ip
            }
            if (!"".equals(logDescription)) {
                operateLogMapper.insert(operateLogVO);
            }
        }
    }

    /**
     * @author: xsm
     * @date: 2018/7/18 13：45
     * @Description: 当系统报错时日志数据处理（异常日志）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:operateLogVO 日志实体类
     * @return:
     */
    public void saveExceptionLog(OperateLogVO operateLogVO) {
        // 日志类型 exceptionlog：异常日志
        String logType = "exceptionlog";
        // 生成日志主键ID
        String uuId = CommonServiceSupport.getUUID();
        // 获取操作人姓名
        String userName = RedisTemplateUtil.getRedisCacheDataByToken("username", String.class);
        // 操作类型
        String operateType = "exit";
        // 日志描述
        String logDescription = "";
        // 将获取到的值存储到日志实体对应字段中
        operateLogVO.setBaseOperateId(uuId);// 日志主键ID
        operateLogVO.setBaseOperateType(operateType); // 基础操作类型
        operateLogVO.setBaseOperateContent(logDescription); // 基础操作内容
        operateLogVO.setBaseOperatePerson(userName); // 基础操作人
        operateLogVO.setBaseOperateDatetime(DataFormatUtil.getDate()); // 基础操作时间
        operateLogVO.setBaseLogType(logType); // 基础日志类型
    }

    /**
     * @author: xsm
     * @date: 2018/7/11 19:14
     * @Description: 构建添加、修改、删除操作日志描述的基础语句，例：修改用户名为【xx】的记录。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:methodType：操作类型 ；tableName：表名称； oldMapData：修改前的数据；
     * newMapData：修改后的数据/添加的数据
     * @return:
     */
    public String doOperateLogVO(String methodType, String tableName,
                                 Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        // 日志拼接的头部
        String firstDescription = "";
        // 日志拼接的中部
        String middleDescription = "";
        // 最终的日志拼接信息
        String lastDescription = "";
        // 操作类型 根据操作类型判断是什么操作，用于拼接日志信息
        if ("add".equals(methodType)) {
            firstDescription = "添加";
        } else if ("edit".equals(methodType)) {
            firstDescription = "修改";
        } else if ("delete".equals(methodType)) {
            firstDescription = "删除";
        }
        // 将操作类型configtype、表名称tablename、日志要显示的字段logfieldflag（1
        // 表示配置表中日志标记要显示的字段）存入map中
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("configtype", "add");
        map.put("tablename", tableName);
        map.put("logfieldflag", 1);
        // 获取日志操作要显示的字段
        List<CommonSelectFieldConfigVO> list = new ArrayList<CommonSelectFieldConfigVO>();
        list = commonSelectFieldConfigMapper.getOperateLogFieldFlag(map);
        // 当修改前数据和修改后数据都不为空时，可判断出记录的是修改操作的日志
        if (oldMapData != null && newMapData != null) {
            // 遍历存储着带有日志标记的字段list，然后拼接拼接出格式为“xx为【xx】的记录”的字符串。
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (oldMapData.get(FieldName.toLowerCase()) != null) {
                            String value = oldMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }
            // 先拼接初始字符串 当firstString为空 则证明未添加日志标记字段
            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录\n";
            }
        } else if (oldMapData == null && newMapData != null) {// 拼接添加操作的日志描述
            // 当修改前数据为空，修改后数据不为空时，可判断出为添加操作的记录日志
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (newMapData.get(FieldName.toLowerCase()) != null) {
                            String value = newMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }
            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录。";
            }
        } else if (oldMapData != null && newMapData == null) { // 当修改前数据为空，修改后数据不为空时，可判断出是删除操作的记录日志
            if (list.size() > 0) {
                for (int j = 0; j < list.size(); j++) {
                    CommonSelectFieldConfigVO obj = list.get(j);
                    String FieldComments = obj.getFieldComments();
                    String FieldName = obj.getFieldName();
                    if (FieldComments != null && FieldName != null) {
                        if (oldMapData.get(FieldName.toLowerCase()) != null) {
                            String value = oldMapData.get(FieldName.toLowerCase()).toString();
                            if (j == 0) {
                                middleDescription = FieldComments + "为【" + value;
                            } else {
                                middleDescription = middleDescription + "(" + value + ")";
                            }
                        }
                    }
                }
            }
            if (!"".equals(middleDescription)) {
                middleDescription = middleDescription + "】的记录。";
            }
        }

        // lastString为空 则说明用户没有修改信息，不需要保存日志
        if (!"".equals(middleDescription)) {
            // 操作描述拼接
            lastDescription = firstDescription + middleDescription;
            // 将获取的值存入对应的字段中
        } else {
            lastDescription = "";
        }
        return lastDescription;
    }

    /**
     * @author: xsm
     * @date: 2018/8/7 16:00
     * @Description:操作用户数据权限时的日志记录
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: oldMapData：存储着修改前的数据权限数据； newMapData：存储着修改后的数据权限数据
     * @return:
     */
    public String getUserDataPermissionsEditLog(Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        String addDescription = "";
        String deleteDescription = "";
        String resultDescription = "";
        List<String> oldRegionList = null;
        List<String> newRegionList = null;
        List<String> oldPollutionList = null;
        List<String> newPollutionList = null;
        // 存储修改前数据中对应key值的value值
        if (oldMapData.get("fk_region") != null && !"".equals(oldMapData.get("fk_region"))) {
            oldRegionList = (List<String>) oldMapData.get("fk_region");
            if (oldRegionList.size() > 0) {

            } else {
                oldRegionList = null;
            }
        }
        // 存储修改后数据中对应key值的value值
        if (newMapData.get("fk_region") != null && !"".equals(newMapData.get("fk_region"))) {
            newRegionList = (List<String>) newMapData.get("fk_region");
            if (newRegionList.size() > 0) {

            } else {
                newRegionList = null;
            }
        }
        // 存储修改前数据中对应key值的value值
        if (oldMapData.get("fk_pollutionid") != null && !"".equals(newMapData.get("fk_pollutionid"))) {
            oldPollutionList = (List<String>) oldMapData.get("fk_pollutionid");
            if (oldPollutionList.size() > 0) {

            } else {
                oldPollutionList = null;
            }
        }
        // 存储修改后数据中对应key值的value值
        if (newMapData.get("fk_pollutionid") != null && !"".equals(newMapData.get("fk_pollutionid"))) {
            newPollutionList = (List<String>) newMapData.get("fk_pollutionid");
            if (newPollutionList.size() > 0) {

            } else {
                newPollutionList = null;
            }
        }
        //当行政区划编码、污染源企业ID都不为空时，拼接数据权限操作信息
        if ((oldRegionList != null && newRegionList != null) && (oldPollutionList != null && newPollutionList != null)) {
            List<Map<String, String>> regionOldList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> regionNewList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionOldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionNewlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOldRegion = new HashMap<String, Object>();
            Map<String, Object> paramsNewRegion = new HashMap<String, Object>();
            Map<String, Object> paramsOldPollution = new HashMap<String, Object>();
            Map<String, Object> paramsNewPollution = new HashMap<String, Object>();
            // 遍历两个List，remove删除掉相同元素
            if (oldPollutionList != null && newPollutionList != null) {
                for (int m = oldPollutionList.size() - 1; m >= 0; m--) {
                    String valueKeyOne = oldPollutionList.get(m).toString();
                    for (int n = 0; n < newPollutionList.size(); n++) {
                        if (newPollutionList.get(n) != null) {
                            String valueKeyTwo = newPollutionList.get(n).toString();
                            if (valueKeyOne.equals(valueKeyTwo)) {
                                oldPollutionList.remove(m);
                                newPollutionList.remove(n);
                                break;
                            }
                        }
                    }
                }
            }
            //判断
            if (oldRegionList.size() > 0) {
                paramsOldPollution.put("pollutionidlist", oldPollutionList);
                paramsOldRegion.put("regioncodelist", oldRegionList);
            }
            if (newRegionList.size() > 0) {
                paramsNewPollution.put("pollutionidlist", newPollutionList);
                paramsNewRegion.put("regioncodelist", newRegionList);
            }
            // 判断oldStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (oldRegionList != null && oldRegionList.size() > 0) {
                regionOldList = commonSelectFieldConfigMapper.getRegionListByparams(paramsOldRegion);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                pollutionOldlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsOldPollution);
            }
            // 判断newStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (newRegionList != null && newRegionList.size() > 0) {
                regionNewList = commonSelectFieldConfigMapper.getRegionListByparams(paramsNewRegion);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                pollutionNewlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsNewPollution);
            }
            String oldRegionName = "";
            String NewRegionName = "";
            //获取修改前和修改后的行政区划名称
            if (regionOldList.size() > 0) {
                Map<String, String> regionOldMap = regionOldList.get(0);
                oldRegionName = regionOldMap.get("name");
            }
            if (regionNewList.size() > 0) {
                Map<String, String> regionNewMap = regionNewList.get(0);
                NewRegionName = regionNewMap.get("name");
            }
            // 遍历两个list 拼接字符串
            if (pollutionOldlist.size() > 0) {
                //若修改前和修改后行政区划名称相同，则证明只修改了污染源企业
                if (oldRegionName.equals(NewRegionName)) {
                    deleteDescription = " 删除数据权限:行政区划【" + oldRegionName
                            + "】下企业";
                } else {
                    deleteDescription = " 删除数据权限:行政区划【" + oldRegionName
                            + "】及企业";
                }
                for (int m = 0; m < pollutionOldlist.size(); m++) {
                    Map<String, String> maps = pollutionOldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("pollutionname") + "】、";
                }
                if (!"".equals(deleteDescription)) {
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                }
            }
            if (pollutionNewlist.size() > 0) {
                //若修改前和修改后行政区划名称相同，则证明只修改了污染源企业
                if (oldRegionName.equals(NewRegionName)) {
                    addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】下企业";
                } else {
                    addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】及企业";
                }
                for (int m = 0; m < pollutionNewlist.size(); m++) {
                    Map<String, String> maps = pollutionNewlist.get(m);
                    addDescription = addDescription + "【" + maps.get("pollutionname") + "】、";
                }
                if (!"".equals(addDescription)) {
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                }
            }
            //当行政区划编码不为空，污染源企业ID为空时
        } else if ((oldRegionList != null && newRegionList != null) && oldPollutionList == null && newPollutionList == null) {
            List<Map<String, String>> oldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> newlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOld = new HashMap<String, Object>();
            Map<String, Object> paramsNew = new HashMap<String, Object>();
            // 遍历两个List，remove删除掉相同元素
            if (oldRegionList != null && newRegionList != null) {
                for (int m = oldRegionList.size() - 1; m >= 0; m--) {
                    if (oldRegionList.get(m) != null) {
                        String valueKeyOne = oldRegionList.get(m).toString();
                        for (int n = 0; n < newRegionList.size(); n++) {
                            if (newRegionList.get(n) != null) {
                                String valueKeyTwo = newRegionList.get(n).toString();
                                if (valueKeyOne.equals(valueKeyTwo)) {
                                    oldRegionList.remove(m);
                                    newRegionList.remove(n);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            paramsOld.put("regioncodelist", oldRegionList);
            paramsNew.put("regioncodelist", newRegionList);
            // 判断oldStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (oldRegionList != null && oldRegionList.size() > 0) {
                oldlist = commonSelectFieldConfigMapper.getRegionListByparams(paramsOld);
            }
            // 判断newStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (newRegionList != null && newRegionList.size() > 0) {
                newlist = commonSelectFieldConfigMapper.getRegionListByparams(paramsNew);
            }
            // 遍历两个list 拼接字符串
            if (oldlist.size() > 0) {
                deleteDescription = " 删除数据权限:行政区划";
                for (int m = 0; m < oldlist.size(); m++) {
                    Map<String, String> maps = oldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("name") + "】、";
                }
                if (!"".equals(deleteDescription)) {
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                }
            }
            if (newlist.size() > 0) {
                addDescription = " 新增数据权限:行政区划";
                for (int m = 0; m < newlist.size(); m++) {
                    Map<String, String> maps = newlist.get(m);
                    addDescription = addDescription + "【" + maps.get("name") + "】、";
                }
                if (!"".equals(addDescription)) {
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                }
            }
        } else {
            List<Map<String, String>> regionOldList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> regionNewList = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionOldlist = new ArrayList<Map<String, String>>();
            List<Map<String, String>> pollutionNewlist = new ArrayList<Map<String, String>>();
            Map<String, Object> paramsOldRegion = new HashMap<String, Object>();
            Map<String, Object> paramsNewRegion = new HashMap<String, Object>();
            Map<String, Object> paramsOldPollution = new HashMap<String, Object>();
            Map<String, Object> paramsNewPollution = new HashMap<String, Object>();

            if (oldRegionList != null && oldRegionList.size() > 0) {
                paramsOldRegion.put("regioncodelist", oldRegionList);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                paramsOldPollution.put("pollutionidlist", oldPollutionList);
            }
            if (newRegionList != null && newRegionList.size() > 0) {

                paramsNewRegion.put("regioncodelist", newRegionList);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                paramsNewPollution.put("pollutionidlist", newPollutionList);
            }
            String oldRegionName = "";
            String NewRegionName = "";
            // 判断oldStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (oldRegionList != null && oldRegionList.size() > 0) {
                regionOldList = commonSelectFieldConfigMapper.getRegionListByparams(paramsOldRegion);
            }
            if (oldPollutionList != null && oldPollutionList.size() > 0) {
                pollutionOldlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsOldPollution);
                if (regionOldList.size() > 0) {
                    Map<String, String> regionOldMap = regionOldList.get(0);
                    oldRegionName = regionOldMap.get("name");
                }
            }
            // 判断newStr是否为空，不为空则获取到该字段关联的表的相关信息
            if (newRegionList != null && newRegionList.size() > 0) {
                regionNewList = commonSelectFieldConfigMapper.getRegionListByparams(paramsNewRegion);
            }
            if (newPollutionList != null && newPollutionList.size() > 0) {
                pollutionNewlist = commonSelectFieldConfigMapper.getPollutionListByparams(paramsNewPollution);
                if (regionNewList.size() > 0) {
                    Map<String, String> regionNewMap = regionNewList.get(0);
                    NewRegionName = regionNewMap.get("name");
                }
            }

            // 遍历两个list 拼接字符串
            if (pollutionOldlist.size() > 0) {
                deleteDescription = " 删除数据权限:行政区划【" + oldRegionName + "】及企业";
                for (int m = 0; m < pollutionOldlist.size(); m++) {
                    Map<String, String> maps = pollutionOldlist.get(m);
                    deleteDescription = deleteDescription + "【" + maps.get("pollutionname") + "】、";
                }
                deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
            } else {
                if (regionOldList.size() > 0) {
                    deleteDescription = " 删除数据权限:行政区划";
                    for (int i = 0; i < regionOldList.size(); i++) {
                        Map<String, String> regionOldMap = regionOldList.get(i);
                        oldRegionName = regionOldMap.get("name").toString();
                        deleteDescription += "【" + oldRegionName + "】、";
                    }
                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                } else {

                }

            }

            if (pollutionNewlist.size() > 0) {
                addDescription = " 新增数据权限:行政区划【" + NewRegionName + "】及企业";
                for (int m = 0; m < pollutionNewlist.size(); m++) {
                    Map<String, String> maps = pollutionNewlist.get(m);
                    addDescription = addDescription + "【" + maps.get("pollutionname") + "】、";
                }
                addDescription = addDescription.substring(0, addDescription.length() - 1);
            } else {
                if (regionNewList.size() > 0) {
                    addDescription = " 新增数据权限:行政区划";
                    for (int i = 0; i < regionNewList.size(); i++) {
                        Map<String, String> regionNewMap = regionNewList.get(i);
                        NewRegionName = regionNewMap.get("name");
                        addDescription += "【" + NewRegionName + "】、";
                    }
                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                } else {

                }
            }

        }
        resultDescription = addDescription + deleteDescription;

        return resultDescription;
    }

    /**
     * @author: xsm
     * @date: 2018/7/10 14:24
     * @Description:对比两个对象，返回操作描述信息。
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: tableName:操作的表名;oldMapData:修改前Data; newMapData:修改后Data
     * @return:
     */
    @SuppressWarnings("unused")
	public String fomateEditLog(String tableName, Map<String, Object> oldMapData, Map<String, Object> newMapData) {
        // 没有外键关联的基础数据
        String logDescription = "";
        // 存储修改前后对比中新增的拼接的内容
        String addDescription = "";
        // 存储修改前后对比中删除的拼接的内容
        String deleteDescription = "";
        // 中段部分描述
        String middleDescription = "";
        // 最终拼接的日志描述，返回结果
        String lastDescription = "";
        // 拼接权限有新增的字符串(用户权限、角色权限)
        String addAuthority = "";
        // 拼接权限有删除的字符串(用户权限、角色权限)
        String deleteAuthority = "";
        // 单选框描述拼接字符串
        String radioDescription = "";
        // 当修改前对象和修改后对象都不为空时
        // 修改的内容：oldMapData存储的修改前的数据（回显在修改页面的数据），newMapData存储的修改后的数据
        // 遍历两个map中相同的key值，比较同一个key下的value值
        for (String key : oldMapData.keySet()) {
            if ((oldMapData.get(key) != null && newMapData.get(key) != null) && oldMapData.get(key).toString()
                    .equals(newMapData.get(key).toString())) {

            } else { // 若两个value值有不同 则证明有修改操作
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("tablename", tableName);
                map.put("configtype", "edit");
                map.put("fieldname", key);
                List<CommonSelectFieldConfigVO> datas = new ArrayList<CommonSelectFieldConfigVO>();
                // 去配置表中查询该字段的配置信息，然后根据RelationalTable（关联外键表）等字段的value值是否为空
                // 从而判断是否有关联关系
                datas = commonSelectFieldConfigMapper.getCommonSelectFieldConfigByMap(map);
                // key值中文说明
                String keyComments = datas.get(0).getFieldComments();
                // datas不为空 getCustomOptions()为空，判断该字段不是单选框
                if (datas.size() > 0 && (datas.get(0).getCustomOptions() == null || "".equals(datas.get(0).getCustomOptions()))) {
                    CommonSelectFieldConfigVO com = datas.get(0);
                    // 关联表
                    String RelationalTable = com.getRelationalTable();
                    // 关联字段
                    String FKKeyField = com.getFkKeyField();
                    // 关联表要显示的字段
                    String FKNameField = com.getFkNameField();
                    // 中间表名称
                    String MiddleTable = com.getMiddleTable();
                    // 左表主键ID
                    String LeftId = com.getLeftId();
                    // 右表主键ID
                    String RightId = com.getRightId();
                    // 菜单ID（用于用户权限的修改）
                    String MenuId = "";
                    // 按钮ID（用于用户权限的修改）
                    String ButtonId = "";
                    // 判断中间表右表主键ID是否为空,不为空则判断其值是否存的"Menu_Id"和"Button_Id"，用来处理用户权限、角色权限
                    if (RightId != null) {
                        String[] Right = RightId.split(",");
                        if (Right.length > 1) {
                            if ("menu_id".equals(Right[0].toLowerCase())) {
                                MenuId = Right[0];
                            }
                            if ("button_id".equals(Right[1].toLowerCase())) {
                                ButtonId = Right[1];
                            }
                        }
                    }
                    if (RelationalTable == null && MiddleTable == null) {// 拼接没有外键关联的基础数据

                        Object oldvalue = oldMapData.get(key);
                        Object newvalue = newMapData.get(key);
                        logDescription = logDescription + "\n" + keyComments + "【" + oldvalue + "】修改为【" + newvalue + "】,";
                    } else {// 有外键关系的数据拼接
                        // 存储修改前数据中对应key值的value值
                        List<String> oldDataList = new ArrayList<>();
                        // 存储修改后数据中对应key值的value值
                        List<String> newDataList = new ArrayList<>();
                        // 分割map
                        if (oldMapData.get(key) != null) {
                            if (oldMapData.get(key) instanceof List) {
                                oldDataList = (List<String>) oldMapData.get(key);
                            } else {
                                String name = oldMapData.get(key).toString();
                                oldDataList.add(name);
                            }
                        }
                        if (newMapData.get(key) != null) {
                            if (newMapData.get(key) instanceof List) {
                                newDataList = (List<String>) newMapData.get(key);
                            } else {
                                String name = newMapData.get(key).toString();
                                newDataList.add(name);
                            }
                        }
                        // 遍历修改前修改后两个List，remove删除掉value中相同的值
                        if (oldDataList != null && newDataList != null) {
                            for (int m = oldDataList.size() - 1; m >= 0; m--) {
                                if (oldDataList.get(m) != null) {
                                    String valueKeyOne = oldDataList.get(m).toString();
                                    for (int n = 0; n < newDataList.size(); n++) {
                                        if (newDataList.get(n) != null) {
                                            String valueKeyTwo = newDataList.get(n).toString();
                                            if (valueKeyOne.equals(valueKeyTwo)) {
                                                oldDataList.remove(m);
                                                newDataList.remove(n);
                                                break;
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        
                       /* if (RelationalTable != null && MiddleTable == null ) {
                        	// 当配置表中RelationalTable关联外键表字段不为空，MiddleTable中间表字段为空
                        	
                        	
                        // 且两个数组中的值不为空时，可知该字段为多选下拉框
                        }else*/ if ((RelationalTable != null && MiddleTable == null)||(RelationalTable != null && MiddleTable != null)) { // 多选下拉框
                            // 存储修改前查询条件的map
                            Map<String, Object> params1 = new HashMap<String, Object>();
                            // 存储修改后查询条件的map
                            Map<String, Object> params2 = new HashMap<String, Object>();
                            List<Map<String, String>> oldlist = new ArrayList<Map<String, String>>();
                            List<Map<String, String>> newlist = new ArrayList<Map<String, String>>();
                            // 将查询条件存储到map中
                            params1.put("tablename", RelationalTable);
                            params2.put("tablename", RelationalTable);

                            params1.put("fkkeyfield", FKKeyField);
                            params2.put("fkkeyfield", FKKeyField);

                            params1.put("fknamefield", FKNameField);
                            params2.put("fknamefield", FKNameField);

                            params1.put("fieldlist", oldDataList);
                            params2.put("fieldlist", newDataList);
                            // 判断oldDataList是否为空，不为空则获取到该字段关联的表的相关信息
                            if (oldDataList != null && oldDataList.size() > 0) {
                                oldlist = commonSelectFieldConfigMapper.getRelationTableFieldNameList(params1);
                            }
                            // 判断newDataList是否为空，不为空则获取到该字段关联的表的相关信息
                            if (newDataList != null && newDataList.size() > 0) {
                                newlist = commonSelectFieldConfigMapper.getRelationTableFieldNameList(params2);
                            }
                            if(RelationalTable != null && MiddleTable == null){
                            	String strstr ="";
                            	if (oldlist.size() > 0) {
                            		 Map<String, String> maps = oldlist.get(0);
                                     String name = maps.get(FKNameField);
                                     strstr= "修改"+keyComments+"【"+name+"】";
                            	}else{
                            		strstr= "修改"+keyComments+"【】";
                            	}
                            	 if (newlist.size() > 0) {
                            		 Map<String, String> maps = newlist.get(0);
                                     String name = maps.get(FKNameField);
                                     strstr+= "为【"+name+"】";
                            	 }else{
                            		 strstr+= "为【】";
                            	 }
                            	 middleDescription += strstr;
                            }else{
                            // 遍历两个list 拼接字符串
                            if (oldlist.size() > 0) {
                                deleteDescription = "删除" + keyComments + ":";
                                for (int m = 0; m < oldlist.size(); m++) {
                                    Map<String, String> maps = oldlist.get(m);
                                    String name = maps.get(FKNameField);
                                    deleteDescription += "【" + name + "】、";
                                }
                                if (!"".equals(deleteDescription)) {
                                    deleteDescription = deleteDescription.substring(0, deleteDescription.length() - 1);
                                }
                            }
                            if (newlist.size() > 0) {
                                addDescription = "新增" + keyComments + ":";
                                for (int n = 0; n < newlist.size(); n++) {
                                    Map<String, String> maps = newlist.get(n);
                                    String name = maps.get(FKNameField);
                                    addDescription += "【" + name + "】、";
                                }
                                if (!"".equals(addDescription)) {
                                    addDescription = addDescription.substring(0, addDescription.length() - 1);
                                }
                            }
                            middleDescription += addDescription + deleteDescription;
                            }
                            // 当关联表为空，中间表不为空时（用户权限、角色权限）
                        } else if (RelationalTable == null && MiddleTable != null && LeftId != null && RightId != null && !"".equals(MenuId) && !"".equals(ButtonId)) {
                            String[] oldData = null;
                            String[] newData = null;
                            // 修改前菜单ID集合和修改后菜单ID集合的并集
                            List<String> menuIdList = new ArrayList<String>();
                            // 修改前按钮ID集合和修改后按钮ID集合的并集
                            List<String> buttonList = new ArrayList<String>();
                            // 存储用户权限中的菜单名称的集合
                            List<SysMenuVO> menuNamelist = new ArrayList<SysMenuVO>();
                            // 存储用户权限中的按钮名称的集合
                            List<ButtonVO> buttonNameList = new ArrayList<ButtonVO>();
                            // 存储查询菜单名称时的条件的map
                            Map<String, Object> munuParams = new HashMap<String, Object>();
                            // 存储查询按钮名称查询条件的map
                            Map<String, Object> buttonParams = new HashMap<String, Object>();
                            /*
                             * 1.遍历已经比较过并且去掉了相同元素的数组 oldDataList和newDataList
                             * 2.以"_"
                             * 分割其中的元素，若分割后接收数据的数组长度大于1，则证明该元素是以"菜单ID_按钮ID"
                             * 格式传递过来的ID，不大于1则是菜单ID
                             * 3.用menuIdList接收两个数组遍历出的菜单ID和分割出的菜单ID
                             * ，buttonlist接收两数组分割出的按钮ID
                             * ，oldbuttonlist接收oldDataList未分割的元素。 *
                             */
                            // 修改前菜单ID集合和修改后菜单ID集合的并集
                            List<String> oldMenuKeyList = new ArrayList<String>();
                            // 修改前按钮ID集合和修改后按钮ID集合的并集
                            List<String> newMenuKeyList = new ArrayList<String>();
                            Map<String, Object> oldMenuMap = new HashMap<String, Object>();
                            Map<String, Object> newMenuMap = new HashMap<String, Object>();
                            if (oldDataList != null && oldDataList.size() > 0) {
                                for (int m = 0; m < oldDataList.size(); m++) {
                                    String valueKeys = oldDataList.get(m).toString();
                                    oldData = valueKeys.split("_");
                                    String oldMenuKey = oldData[0];
                                    if (oldData.length > 1) {
                                        menuIdList.add(oldData[0]);
                                        buttonList.add(oldData[1]);
                                    } else {
                                        menuIdList.add(valueKeys);
                                    }
                                    if (oldMenuKeyList.size() > 0) {
                                        boolean flag = true;
                                        for (int n = 0; n < oldMenuKeyList.size(); n++) {
                                            String menuKey = oldMenuKeyList.get(n);
                                            if (oldMenuKey.equals(menuKey)) {
                                                flag = false;
                                            }
                                        }
                                        if (flag == true) {
                                            oldMenuKeyList.add(oldMenuKey);
                                            String[] oldDataLists = null;
                                            String buttonValue = "";
                                            for (int i = 0; i < oldDataList.size(); i++) {
                                                if (oldDataList.get(i) != null) {
                                                    String valuekey = oldDataList.get(i).toString();
                                                    oldDataLists = valuekey.split("_");
                                                    String menuKey = oldDataLists[0];
                                                    if (oldMenuKey.equals(menuKey)) {
                                                        buttonValue += valuekey + "、";
                                                    }
                                                }
                                            }
                                            if (!"".equals(buttonValue)) {
                                                buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                                oldMenuMap.put(oldMenuKey, buttonValue);
                                            }
                                        }

                                    } else {
                                        oldMenuKeyList.add(oldMenuKey);
                                        String[] oldDataLists = null;
                                        String buttonValue = "";
                                        for (int i = 0; i < oldDataList.size(); i++) {
                                            if (oldDataList.get(i) != null) {
                                                String valuekey = oldDataList.get(i).toString();
                                                oldDataLists = valuekey.split("_");
                                                String menuKey = oldDataLists[0];
                                                if (oldMenuKey.equals(menuKey)) {
                                                    buttonValue += valuekey + "、";
                                                }
                                            }
                                        }
                                        if (!"".equals(buttonValue)) {
                                            buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                            oldMenuMap.put(oldMenuKey, buttonValue);
                                        }
                                    }
                                }
                            }
                            // 修改后数据
                            if (newDataList != null && newDataList.size() > 0) {
                                for (int m = 0; m < newDataList.size(); m++) {
                                    String valueKeys = "";
                                    if (newDataList.get(m) != null) {
                                        valueKeys = newDataList.get(m).toString();
                                    }
                                    newData = valueKeys.split("_");
                                    String newMenuKey = newData[0];
                                    if (newData.length > 1) {
                                        menuIdList.add(newData[0]);
                                        buttonList.add(newData[1]);
                                    } else {
                                        menuIdList.add(valueKeys);
                                    }
                                    if (newMenuKeyList.size() > 0) {
                                        boolean flag = true;
                                        for (int n = 0; n < newMenuKeyList.size(); n++) {
                                            String menuKey = newMenuKeyList.get(n);
                                            if (newMenuKey.equals(menuKey)) {
                                                flag = false;
                                            }
                                        }
                                        if (flag == true) {
                                            newMenuKeyList.add(newMenuKey);
                                            String[] newDataLists = null;
                                            String buttonValue = "";
                                            for (int i = 0; i < newDataList.size(); i++) {
                                                String valueKey = newDataList.get(i).toString();
                                                newDataLists = valueKey.split("_");
                                                String menuKey = newDataLists[0];
                                                if (newMenuKey.equals(menuKey)) {
                                                    buttonValue += valueKey + "、";
                                                }
                                            }
                                            if (!"".equals(buttonValue)) {
                                                buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                                newMenuMap.put(newMenuKey, buttonValue);
                                            }
                                        }

                                    } else {
                                        newMenuKeyList.add(newMenuKey);
                                        String[] newDataLists = null;
                                        String buttonValue = "";
                                        for (int i = 0; i < newDataList.size(); i++) {
                                            String valueKey = "";
                                            if (newDataList.get(i) != null) {
                                                valueKey = newDataList.get(i).toString();
                                            }
                                            newDataLists = valueKey.split("_");
                                            String menuKey = newDataLists[0];
                                            if (newMenuKey.equals(menuKey)) {
                                                buttonValue += valueKey + "、";
                                            }
                                        }
                                        if (!"".equals(buttonValue)) {
                                            buttonValue = buttonValue.substring(0, buttonValue.length() - 1);
                                            newMenuMap.put(newMenuKey, buttonValue);
                                        }
                                    }
                                }
                            }
                            // 通过HashSet踢除重复元素
                            HashSet<String> H1 = new HashSet<String>(menuIdList);
                            menuIdList.clear();
                            menuIdList.addAll(H1);
                            // 通过HashSet踢除重复元素
                            HashSet<String> H2 = new HashSet<String>(buttonList);
                            buttonList.clear();
                            buttonList.addAll(H2);
                            // 查询menuIdList中所有菜单ID对应的菜单名称
                            if (menuIdList.size() > 0 && menuIdList != null) {
                                munuParams.put("menuidlist", menuIdList);
                                menuNamelist = sysMenuMapper.getMenuNameByMenuid(munuParams);
                            }
                            // 查询buttonList中所有按钮ID对应的按钮名称
                            if (buttonList.size() > 0 && buttonList != null) {
                                buttonParams.put("buttonidlist", buttonList);
                                buttonNameList = buttonMapper.getButtonNameByButtonid(buttonParams);
                            }
                            // 遍历
                            if (oldMenuMap.size() > 0) {
                                for (Object value : oldMenuMap.values()) {
                                    String[] keyValue = ((String) value).split("、");
                                    String menuString = "";
                                    String buttonString = "";
                                    for (int n = 0; n < keyValue.length; n++) {
                                        String buttonId = keyValue[n];
                                        String[] buttonIdStr = buttonId.split("_");
                                        if (n == 0) {
                                            if (buttonIdStr.length > 1) {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单下";
                                                        break;
                                                    }
                                                }
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (buttonIdStr.length > 1) {
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = buttonString + "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                        break;
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!"".equals(buttonString)) {
                                        buttonString = buttonString.substring(0, buttonString.length() - 1);
                                        deleteAuthority += menuString + buttonString + "按钮;";
                                    } else {
                                        if (!"".equals(menuString)) {
                                            menuString = menuString.substring(0, menuString.length() - 1);
                                            deleteAuthority += menuString + ";";
                                        }

                                    }
                                }
                            }
                            if (!"".equals(deleteAuthority)) {
                                deleteAuthority = "删除功能权限" + ":" + deleteAuthority;
                            }
                            // 遍历修改后数据
                            if (newMenuMap.size() > 0) {
                                for (Object value : newMenuMap.values()) {
                                    String[] keyValue = ((String) value).split("、");
                                    String menuString = "";
                                    String buttonString = "";
                                    for (int n = 0; n < keyValue.length; n++) {
                                        String buttonId = keyValue[n];
                                        String[] buttonIdStr = buttonId.split("_");
                                        if (n == 0) {
                                            if (buttonIdStr.length > 1) {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单下";
                                                    }
                                                }
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                    }
                                                }
                                            }
                                        } else {
                                            if (buttonIdStr.length > 1) {
                                                for (int j = 0; j < buttonNameList.size(); j++) {
                                                    if (buttonNameList.get(j).getButtonId().equals(buttonIdStr[1])) {
                                                        buttonString = buttonString + "【" + buttonNameList.get(j).getButtonName() + "】、";
                                                    }
                                                }
                                            } else {
                                                for (int i = 0; i < menuNamelist.size(); i++) {
                                                    if (menuNamelist.get(i).getMenuId().equals(buttonIdStr[0])) {
                                                        menuString = "【" + menuNamelist.get(i).getMenuName() + "】菜单及";
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!"".equals(buttonString)) {
                                        buttonString = buttonString.substring(0, buttonString.length() - 1);
                                        addAuthority += menuString + buttonString + "按钮;";
                                    } else {
                                        if (!"".equals(menuString)) {
                                            menuString = menuString.substring(0, menuString.length() - 1);
                                            addAuthority += menuString + ";";
                                        }
                                    }
                                }
                            }
                            if (!"".equals(addAuthority)) {
                                addAuthority = "新增功能权限" + ":" + addAuthority;
                            }

                        }

                    }
                } else if (datas.size() > 0 && datas.get(0).getCustomOptions() != null && !"".equals(datas.get(0).getCustomOptions())) { // 判断是单选框
                    String customOptions = datas.get(0).getCustomOptions();
                    if (customOptions.length() > 0) {
                        customOptions = customOptions.substring(1);
                    }
                    @SuppressWarnings("rawtypes")
                    Map jsonObject = JSONObject.fromObject(customOptions);

                    String oldValue = "";
                    String newValue = "";
                    if (oldMapData.get(key) == null) {
                        oldValue = "";
                    } else {
                        oldValue = oldMapData.get(key).toString();
                    }
                    if (newMapData.get(key) == null) {
                        newValue = "";
                    } else {
                        newValue = newMapData.get(key).toString();
                    }
                    String valueOne = "";
                    String valueTwo = "";
                    if (jsonObject.size() > 0) {
                        if (!"".equals(oldValue) && jsonObject.get(oldValue) != null) {
                            valueOne = jsonObject.get(oldValue).toString();
                        } else if (!"".equals(oldValue) && jsonObject.get("value") != null) {
                            @SuppressWarnings("rawtypes")
                            Map valueMap = JSONObject.fromObject(jsonObject.get("value").toString());
                            if (valueMap.get(oldValue) != null) {
                                valueOne = valueMap.get(oldValue).toString();
                            }
                        }
                        if (!"".equals(newValue) && jsonObject.get(newValue) != null) {
                            valueTwo = jsonObject.get(newValue).toString();
                        } else if (!"".equals(newValue) && jsonObject.get("value") != null) {
                            @SuppressWarnings("rawtypes")
                            Map valueMap = JSONObject.fromObject(jsonObject.get("value").toString());
                            if (valueMap.get(newValue) != null) {
                                valueTwo = valueMap.get(newValue).toString();
                            }

                        }
                    }
                    if (!valueOne.equals(valueTwo)) {

                        radioDescription = radioDescription + "\n" + keyComments + "【" + valueOne + "】修改为【" + valueTwo + "】,";
                        radioDescription = radioDescription.substring(0, radioDescription.length() - 1);
                    }

                }
            }
        }
        if (!"".equals(logDescription)) {
            logDescription = logDescription.substring(0, logDescription.length() - 1);
        }
        // 拼接修改操作的日志记录
        lastDescription = logDescription + "\t" + radioDescription + "\t" + middleDescription + "\t" + addAuthority + "\t" + "" + deleteAuthority;
        return lastDescription;
    }

}
