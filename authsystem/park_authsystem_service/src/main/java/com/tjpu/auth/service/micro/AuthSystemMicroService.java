package com.tjpu.auth.service.micro;


import com.tjpu.auth.service.impl.FeignFallback.AuthSystemMicroServiceImpl;
import net.sf.json.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年3月19日 下午5:57:29
 * @Description:微服务调用接口类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@FeignClient(value = "publicSystem", path = "/publicSystem", fallback = AuthSystemMicroServiceImpl.class)
// 指的是调用authSystemServer:单点登录系统微服务
public interface AuthSystemMicroService {

    /**
     * @param jsonObject
     * @return
     * @author: lip
     * @date: 2018年4月10日 下午2:03:38
     * @Description: 远程微服务调用，获取token
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @RequestMapping(value = "auth/getTokenJson", method = RequestMethod.POST)
    Object getAuthDataByParam(@RequestBody JSONObject jsonObject);


    /**
     * @author: lip
     * @date: 2018/11/6 0006 下午 5:15
     * @Description: 通过socketIO给所有客户端推送消息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @RequestMapping(value = "socket/sendAllClientMessage", method = RequestMethod.POST)
    Object sendAllClientMessage(@RequestBody JSONObject jsonObject);


    /**
     * @param microParam
     * @return
     * @author: lip
     * @date: 2018年7月11日 下午3:39:43
     * @Description: 远程微服务调用，获取初始化列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @RequestMapping(value = "generalMethod/getListByParam", method = RequestMethod.POST)
    Object getListByParam(@RequestParam("microparam") String microParam);


    /**
     * @param microParam
     * @return
     * @author: lip
     * @date: 2018年7月11日 下午3:40:10
     * @Description: 远程微服务调用，获取列表数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @RequestMapping(value = "generalMethod/getListData", method = RequestMethod.POST)
    Object getListData(@RequestParam("microparam") String microParam);

    /**
     * @param microParam
     * @return
     * @author: lip
     * @date: 2018年7月11日 下午3:43:55
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     */
    @RequestMapping(value = "generalMethod/getDetail")
    Object getDetail(@RequestParam("microparam") String microParam);

    /**
     * @author: chengzq
     * @date: 2018/8/31 0031 上午 11:52
     * @Description: 新增
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/doAddMethod", method = RequestMethod.POST)
    Object doAddMethod(@RequestParam("microparam") String microParam);

    /**
     * @author: xsm
     * @date: 2018/9/06  下午 1:26
     * @Description: 获取添加页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/getAddPageInfo", method = RequestMethod.POST)
    Object getAddPageInfo(@RequestParam("microparam") String microParam);

    /**
     * @author: xsm
     * @date: 2018/9/7   8:42
     * @Description: 获取修改页面
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/goUpdatePage", method = RequestMethod.POST)
    Object goUpdatePage(@RequestParam("microparam") String microParam);

    /**
     * @author: chengzq
     * @date: 2018/8/31 0031 上午 11:52
     * @Description: 修改
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/doEditMethod", method = RequestMethod.POST)
    Object doEditMethod(@RequestParam("microparam") String microParam);


    /**
     * @author: chengzq
     * @date: 2018/8/31 0031 上午 11:52
     * @Description: 删除
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/deleteMethod", method = RequestMethod.POST)
    Object deleteMethod(@RequestParam("microparam") String microParam);

    @RequestMapping(value = "generalMethod/isTableDataHaveInfo", method = RequestMethod.POST)
    Object isTableDataHaveInfo(@RequestParam("microparam") String microParam);

    /**
     * @author: chengzq
     * @date: 2018/10/17 0031 上午 11:52
     * @Description: 获取查询条件数据
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/getQueryCriteriaData", method = RequestMethod.POST)
    Object getQueryCriteriaData(@RequestParam("microparam") String microParam);

    /**
     * @author: chengzq
     * @date: 2018/10/17 0031 上午 11:52
     * @Description: 获取用户在菜单上拥有的按钮权限信息
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: [microParam]
     * @throws:
     */
    @RequestMapping(value = "generalMethod/getUserButtonAuthInMenu", method = RequestMethod.POST)
    Object getUserButtonAuthInMenu(@RequestParam("microparam") String microParam);

    /**
     * @Author: zhangzc
     * @Date: 2018/10/27 10:02
     * @Description: 获取表头信息
     * @UpdateUser:
     * @UpdateDate:
     * @UpdateDescription:
     * @Param: Json字符串 sysmodel 和 listfieldtype list配置字段类型
     * @Return:
     */
    @RequestMapping(value = "generalMethod/getTableTitle", method = RequestMethod.POST)
    Object getTableTitle(@RequestParam("microparam") String microParam);


    /**
     * @author: zzc
     * @date: 2018/11/14 0014 下午 1:21
     * @Description: poi excel 文件导出操作
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "generalMethod/getHSSFWorkbook", method = RequestMethod.POST)
    byte[] getHSSFWorkbook(@RequestParam("microparam") String microParam);
}
