package com.tjpu.auth.config;
import com.tjpu.pk.common.utils.DataFormatUtil;
import com.tjpu.pk.common.utils.RequestUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务*/
public class StaticScheduleTask {
   /* private final String endTime = "2021-02-28 23:59:59";
    private boolean isFirstStart = true;
    @Value("${server.port}")
    private String post;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    private void shutDown() {
        if (!isFirstStart) {
            Date nowTime = new Date();
            if (nowTime.after(DataFormatUtil.getDateYMDHMS(endTime))) {
                RequestUtil.sendPost("http://localhost:" +
                        post + "/" +
                        contextPath + "/actuator/shutdown", "");
            }
        } else {
            isFirstStart = false;
        }

    }*/

}