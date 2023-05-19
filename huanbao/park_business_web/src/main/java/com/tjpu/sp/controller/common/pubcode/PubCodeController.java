package com.tjpu.sp.controller.common.pubcode;


import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.sp.common.enumconfig.CommonTypeEnum;
import com.tjpu.sp.service.common.pubcode.PubCodeService;
import io.swagger.annotations.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月18日 上午10:50:29
 * @Description:公共代码接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@RestController
@RequestMapping("pubCode")
@Api(value = "公共代码统计处理类", tags = "公共代码统计处理类")
public class PubCodeController {


    @Autowired
    private PubCodeService pubCodeService;


    /**
     * @author: chengzq
     * @date: 2018/9/13 0013 下午 6:52
     * @Description: 通用接口，通过表名称、排序字段、where条件，获取公共代码表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:  tablename    表名称（必传）
     * @param:  orderfield   排序字段（非必传）
     * @param:  wherestring  where语句（非必传）
     * @return:
     */
    @RequestMapping(value = "getPubCodeDataByParam", method = RequestMethod.POST)
    public Object getPubCodeDataByParam(
            @RequestJson(value = "tablename", required = false) String tablename,
            @RequestJson(value = "orderfield", required = false) String orderfield,
            @RequestJson(value = "tableflag", required = false) String tableflag,
            @RequestJson(value = "wherestring", required = false) String wherestring) {
        try {

            if(StringUtils.isNotBlank(tableflag)){
                tablename=CommonTypeEnum.TableFlagEnum.getNameByMark(tableflag);
            }
            Map<String, Object> paramMap = new HashMap<>();
            if(tablename!=null && !tablename.contains("--") && !tablename.contains(";")){
                paramMap.put("tablename", tablename);
            }
            if(orderfield!=null && !orderfield.contains("--") && !orderfield.contains(";")){
                paramMap.put("orderfield", orderfield);
            }
            if(wherestring!=null && !wherestring.contains("--") && !wherestring.contains(";")){
                paramMap.put("wherestring", wherestring);
            }
            List<Map<String, Object>> dataList = pubCodeService.getPubCodeDataByParam(paramMap);
            return AuthUtil.parseJsonKeyToLower("success", dataList.stream().distinct().collect(Collectors.toList()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }


    /**
     * @author: chengzq
     * @date: 2019/10/14 0014 下午 2:26
     * @Description: 通过表名称、排序字段、where条件，获取公共代码表指定的数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [paramsarrayjson] 查询码表"参数对象"数组
     * 注：参数对象    tableflag：表名称标记字符串； 必填
     * wherestring：查询where条件字符串；
     * orderfield：排序字段字符串；
     * fields：要返回的字段数组  不写默认返回所有字段
     * @throws:
     */
    @RequestMapping(value = "getManyPubCodeDataByParam", method = RequestMethod.POST)
    public Object getManyPubCodeDataByParam(
            @RequestJson(value = "paramsarrayjson") List<Map<String, Object>> paramsarrayjson) {
        try {
            Map<String, Object> data = new HashMap<>();
            for (Map<String, Object> params : paramsarrayjson) {
                String tabelflag = params.get("tableflag") == null ? "" : params.get("tableflag").toString();
                String tableNameBySysmodel = CommonTypeEnum.TableFlagEnum.getNameByMark(tabelflag.trim());
                params.put("tablename", tableNameBySysmodel);
                List<Map<String, Object>> listdata;
                if(CommonTypeEnum.TableFlagEnum.isCached(tabelflag)){
                    listdata =pubCodeService.getPubCodesDataByParamWithCache(params);
                }else{
                    listdata = pubCodeService.getPubCodesDataByParam(params);
                }
                data.put(tabelflag, listdata.stream().distinct().collect(Collectors.toList()));
            }
            return AuthUtil.parseJsonKeyToLower("success", data);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * @author: chengzq
     * @date: 2018/11/14 0014 上午 11:51
     * @Description: 获取码表树数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: code   父节点编码
     * @throws:
     */
    @RequestMapping(value = "getPubTree", method = RequestMethod.POST)
    public Object getPubTree(@RequestJson(value = "tablename", required = false) String tablename, @RequestJson(value = "wherestring",required = false) String wherestring
                            , @RequestJson(value = "tableflag",required = false) String tableflag   ) throws Exception {
        try {
            if(StringUtils.isNotBlank(tableflag)){
                tablename=CommonTypeEnum.TableFlagEnum.getNameByMark(tableflag);
            }
            if(wherestring!=null && wherestring.contains("--") && wherestring.contains(";")){
                wherestring="";
            }
            return AuthUtil.parseJsonKeyToLower("success",  pubCodeService.getPubTree(tablename, wherestring));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * @author: chengzq
     * @date: 2018/11/14 0014 上午 11:51
     * @Description: 验证传入数据是否重复
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [code, tablename]
     * @throws:
     */
    @RequestMapping(value = "isTableDataHaveInfo", method = RequestMethod.POST)
    public Object isTableDataHaveInfo(@RequestJson(value = "tableflag",required = false) String tableflag,
                                      @RequestJson(value = "andstring", required = false) String andstring,
                                      @RequestJson(value = "value") String value,
                                      @RequestJson(value = "key") String key) throws Exception {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("tableName", CommonTypeEnum.TableFlagEnum.getNameByMark(tableflag));
            paramMap.put("key", key);
            paramMap.put("value", value);
            if(andstring!=null && !andstring.contains("--") && !andstring.contains(";")){
                paramMap.put("andstring", andstring);
            }
            int tableDataHaveInfo = pubCodeService.isTableDataHaveInfo(paramMap);
            if (tableDataHaveInfo == 0) {
                return AuthUtil.parseJsonKeyToLower("success", "no");
            } else {    //已经有了不添加
                return AuthUtil.parseJsonKeyToLower("success", "yes");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }





    /**
     * @Author: xsm
     * @Date: 2018/12/20 17:03
     * @Description: 公共代码-根据流域Code获取其下一级的流域信息 用于流域码表下拉树展示时的懒加载
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param:
     * @Return:
     */
    @RequestMapping(value = "getAllBasinInfoByCode", method = RequestMethod.POST)
    public Object getAllBasinInfoByCode(@RequestJson(value = "code", required = false) Object code) {
        List<Map<String, Object>> result = pubCodeService.getTreeDataByCodeValue("PUB_CODE_Basin", "Code", "Name",
                "ParentCode", "", "", code == null ? "" : code.toString());
        return AuthUtil.parseJsonKeyToLower("success", result);
    }


    @RequestMapping(value = "getAllMonitorPointTypeData", method = RequestMethod.POST)
    public Object getAllMonitorPointTypeData() {
        List<String> categorys = Arrays.asList(DataFormatUtil.parseProperties("system.category").split(","));
        Map<String,Object> parammap=new HashMap<>();
        parammap.put("fields",Arrays.asList("code,name"));
        if(categorys.contains("2")){
            //包含安全
            parammap.put("wherestring","Category in (1,2) and isused=1");
        }else{
            //只有环保
            parammap.put("wherestring","Category in (1) and isused=1");
        }
        parammap.put("tablename","PUB_CODE_MonitorPointType");
        parammap.put("orderfield","Category");
        return AuthUtil.parseJsonKeyToLower("success", pubCodeService.getPubCodesDataByParam(parammap));
    }
}
