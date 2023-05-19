package com.tjpu.sp.controller.common.device;

import com.tjpu.pk.common.annotation.RequestJson;
import com.tjpu.pk.common.utils.AuthUtil;
import com.tjpu.sp.model.environmentalprotection.monitorpoint.DeviceStatusVO;
import com.tjpu.sp.service.environmentalprotection.monitorpoint.DeviceStatusService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Description: 设备状态表
 * @Author: zhangzhenchao
 * @Date: 2019/12/25 13:29
 */
@RestController
@RequestMapping("deviceStatus")
public class DeviceStatusController {

    private DeviceStatusService deviceStatusService;

    public DeviceStatusController(DeviceStatusService deviceStatusService) {
        this.deviceStatusService = deviceStatusService;
    }

    /**
     * @Description:通过MN号判断是否存在 no 不存在 yew 存在
     * @Param:
     * @Return:
     * @Author: zhangzhenchao
     * @Date: 2019/12/25 13:33
     */
    @RequestMapping(value = "isExistByMN", method = RequestMethod.POST)
    public Object isExistByMN(@RequestJson(value = "dgimn") String mn) {

        try {
            List<DeviceStatusVO> deviceStatusVOS = deviceStatusService.selectByDgimn(mn);
            return AuthUtil.parseJsonKeyToLower("success", deviceStatusVOS.size() > 0 ? "yes" : "no");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
