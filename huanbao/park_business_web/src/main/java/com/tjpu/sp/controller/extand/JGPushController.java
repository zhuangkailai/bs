package com.tjpu.sp.controller.extand;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.common.utils.RedisTemplateUtil;
import com.tjpu.sp.service.extand.JGUserRegisterInfoService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jgpush")
public class JGPushController {
    @Autowired
    private JGUserRegisterInfoService jgUserRegisterInfoService;
    /**
     *
     * @author: lip
     * @date: 2019/8/2 0002 下午 2:20
     * @Description: 更新极光注册id和用户关联信息表
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */

    @RequestMapping(value = "updateUserRegisterInfo", method = RequestMethod.POST)
    public Object updateUserRegisterInfo(@RequestJson(value="formdata")Object formData, HttpSession session) throws Exception {
        try {
            if (formData!=null){
                Map<String,Object> map = (Map<String, Object>) formData;
                String username = RedisTemplateUtil.getRedisCacheDataByToken("username",String.class);
                map.put("username",username);
                jgUserRegisterInfoService.updateUserRegisterByParams(map);
            }
            return AuthUtil.parseJsonKeyToLower("success", "");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
     
     /**
      * @Description: 获取发送短信记录信息列表
      * @Param:  
      * @return:  
      * @Author: lip
      * @Date: 2021/7/28 9:05
      */ 
    @RequestMapping(value = "getTextMessageListDataByParam", method = RequestMethod.POST)
    public Object getTextMessageListDataByParam(@RequestJson(value = "paramsjson", required = false) Object map) {
        try {
            JSONObject paramMap = JSONObject.fromObject(map);
            if (paramMap.get("pagenum") != null && paramMap.get("pagesize") != null) {
                int pagenum = Integer.parseInt(paramMap.get("pagenum").toString());
                int pagesize = Integer.parseInt(paramMap.get("pagesize").toString());
                PageHelper.startPage(pagenum, pagesize);
            }
            List<Map<String, Object>> listData = jgUserRegisterInfoService.getTextMessageListData(paramMap);
            PageInfo<Map<String, Object>> page = new PageInfo<>(listData);
            Map<String, Object> resultMap = new HashMap<>();

            //总条数
            resultMap.put("total", page.getTotal());
            resultMap.put("tablelistdata", listData);
            return AuthUtil.parseJsonKeyToLower("success", resultMap);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
