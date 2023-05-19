package com.tjpu.sp.controller.extand;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.SM4Utils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("otherSystem")
public class OtherSystemController {


    /**
     * @Description: 获取乐平危废Url信息
     * @Param:
     * @return:
     * @Author: lip
     * @Date: 2021/11/5 15:09
     */
    @RequestMapping(value = "getLPWFUrl", method = RequestMethod.POST)
    public Object getLPWFUrl() {
        try {
            String userinfo  = DataFormatUtil.parseProperties("lp.wf.username");
            String key  = DataFormatUtil.parseProperties("lp.wf.key");

            String a = String.valueOf(System.currentTimeMillis());

            String str = "{\n" +
                    "        \"key\":\"" + key + "\",\n" +
                    "        \"userInfo\":\"" + userinfo + "\",\n" +
                    "        \"time\":" + a + "\n" +
                    "}";
            String token = SM4Utils.getEncStr(str,key);
            String url = DataFormatUtil.parseProperties("lp.wf.url")+token;
            return AuthUtil.parseJsonKeyToLower("success", url);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
