package com.tjpu.sp.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

/**
 * @author: lip
 * @date: 2019/7/18 0018 下午 5:00
 * @Description: rabbitmq配置类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @param:
 * @return:
 */
@Configuration
public class RabbitMqConfig implements RabbitListenerConfigurer {

    /**
     * 消息交换机的名字
     */

    //报警数据交换机
    public static final String ONLINE_DIRECT_EXCHANGE = "OnlineDirectExchange";

    //安全报警数据交换机
    public static final String AQ_ONLINE_DIRECT_EXCHANGE = "AQOnlineDirectExchange";
    //安全视频报警数据交换机
    public static final String AQ_ONLINE_VIDEO_EXCHANGE = "AQOnlineVideoExchange";

    //报警任务交换机
    public static final String TASK_DIRECT_EXCHANGE = "TaskDirectExchange";

    //雨水监控交换机
    public static final String RAIN_MONITOR_DIRECT_EXCHANGE = "RainMonitorDirectExchange";

    //排口停产交换机
    public static final String STOP_PRODUCTION_DIRECT_EXCHANGE = "StopProductionDirectExchange";

    //点位更新信息交换机
    public static final String POINT_UPDATE_DIRECT_EXCHANGE = "PointUpdateDirectExchange";


    //点位更新信息交换机
    public static final String AQPOINT_UPDATE_DIRECT_EXCHANGE = "AQPointUpdateDirectExchange";

    //点位更新信息队列
    public static final String AQPOINT_UPDATE_DIRECT_QUEUE = "AQPointUpdateDirectQueue";


    //视频叠加配置交换机
    public static final String VIDEO_OVERLAY_DIRECT_EXCHANGE = "VideoOverlayDirectExchange";

    //报警数据队列
    public static final String ONLINE_DIRECT_QUEUE = "OnlineDirectQueue";

    //安全报警数据队列
    public static final String AQ_ONLINE_DIRECT_QUEUE = "AQOnlineDirectQueue";

    //安全视频报警数据队列
    public static final String AQ_ONLINE_VIDEO_QUEUE = "AQOnlineVideoQueue";

    //报警任务队列
    public static final String TASK_DIRECT_QUEUE = "TaskDirectQueue";
    //雨水监控队列
    public static final String RAIN_MONITOR_DIRECT_QUEUE = "RainMonitorDirectQueue";

    //排口停产队列
    public static final String STOP_PRODUCTION_DIRECT_QUEUE = "StopProductionDirectQueue";

    //点位更新信息队列
    public static final String POINT_UPDATE_DIRECT_QUEUE = "PointUpdateDirectQueue";

    //点位MN号更新信息队列
    public static final String POINT_MN_UPDATE_DIRECT_QUEUE = "PointMnUpdateDirectQueue";







    //视频叠加队列
    public static final String VIDEO_OVERLAY_DIRECT_QUEUE = "VideoOverlayDirectQueue";


    //报警数据key
    public static final String ONLINE_DIRECT_KEY = "OnlineDirectKey";

    //报警数据key
    public static final String AQ_ONLINE_DIRECT_KEY = "AQOnlineDirectKey";

    //安全视频报警key
    public static final String AQ_ONLINE_VIDEO_KEY = "AQOnlineVideoKey";


    //报警任务key
    public static final String TASK_DIRECT_KEY = "TaskDirectKey";


    //雨水监控队列
    public static final String RAIN_MONITOR_DIRECT_KEY = "RainMonitorDirectKey";

    //排口停产队列
    public static final String STOP_PRODUCTION_DIRECT_KEY = "StopProductionDirectKey";

    //点位更新信息key
    public static final String POINT_UPDATE_DIRECT_KEY = "PointUpdateDirectKey";


    //点位MN号更新信息key
    public static final String POINT__MN_UPDATE_DIRECT_KEY = "PointMnUpdateDirectKey";



    //点位更新信息key
    public static final String AQPOINT_UPDATE_DIRECT_KEY = "AQPointUpdateDirectKey";
   /* private static final String TOPIC_KEY = "Topic.#";*/

    //视频叠加key
    public static final String VIDEO_OVERLAY_DIRECT_KEY = "VideoOverlayDirectKey";




    //寄存器更新信息交换机
    public static final String MODBUS_UPDATE_DIRECT_EXCHANGE = "ModBusUpdateDirectExchange";

    //寄存器更新信息队列
    public static final String MODBUS_UPDATE_DIRECT_QUEUE = "ModBusUpdateDirectQueue";

    //寄存器更新信息key
    public static final String MODBUS_UPDATE_DIRECT_KEY = "ModBusUpdateDirectKey";


    //离线点位信息交换机
    public static final String OFFLINE_POINT_DIRECT_EXCHANGE = "OfflinePointDirectExchange";

    //离线点位信息队列
    public static final String OFFLINE_POINT_DIRECT_QUEUE = "OfflinePointDirectQueue";

    //离线点位信息key
    public static final String OFFLINE_POINT_DIRECT_KEY = "OfflinePointDirectKey";



    //在线数据补充交换机
    public static final String ONLINE_Supply_DIRECT_EXCHANGE = "OnlineSupplyDirectExchange";

    //在线数据补充队列
    public static final String ONLINE_Supply_DIRECT_QUEUE = "OnlineSupplyDirectQueue";

    //在线数据补充key
    public static final String ONLINE_Supply_DIRECT_KEY = "OnlineSupplyDirectKey";

    //超标超限报警任务交换机
    public static final String ALARM_TASK_DIRECT_EXCHANGE = "AlarmTaskDirectExchange";

    //超标超限报警任务队列
    public static final String ALARM_TASK_DIRECT_QUEUE = "AlarmTaskDirectQueue";

    //超标超限报警任务key
    public static final String ALARM_TASK_DIRECT_KEY = "AlarmTaskDirectKey";

    //反控命令交换机
    public static final String ANTI_CONTROL_DIRECT_EXCHANGE = "AntiControlDirectExchange";

    //反控命令队列
    public static final String ANTI_CONTROL_DIRECT_QUEUE = "AntiControlDirectQueue";

    //反控命令key
    public static final String ANTI_CONTROL_DIRECT_KEY = "AntiControlDirectKey";


    /**
     * 1.队列名字
     * 2.durable="true" 是否持久化 rabbitmq重启的时候不需要创建新的队列
     * 3.auto-delete    表示消息队列没有在使用时将被自动删除 默认是false
     * 4.exclusive      表示该消息队列是否只在当前connection生效,默认是false
     */

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 在线报警直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue onlineDirectQueue() {
        return new Queue(ONLINE_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 点位信息更新队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue pointUpdateDirectQueue() {
        return new Queue(POINT_UPDATE_DIRECT_QUEUE, true, false, false);
    }

    /**
     * @author: mmt
     * @date: 2022/8/8 0019 上午 10:35
     * @Description: 点位MN信息更新队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue pointMnUpdateDirectQueue() {
        return new Queue(POINT_MN_UPDATE_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 点位信息更新队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue AQPointUpdateDirectQueue() {
        return new Queue(AQPOINT_UPDATE_DIRECT_QUEUE, true, false, false);
    }



    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 安全在线报警直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue AQOnlineDirectQueue() {
        return new Queue(AQ_ONLINE_DIRECT_QUEUE, true, false, false);
    }

    /**
     * @author: chengzq
     * @date: 2020/9/19 0019 上午 9:59
     * @Description: 安全在线视频报警直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Bean
    public Queue AQOnlineVideoQueue() {
        return new Queue(AQ_ONLINE_VIDEO_QUEUE, true, false, false);
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 报警任务直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue taskDirectQueue() {
        return new Queue(TASK_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 雨水监控直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue rainMonitorDirectQueue() {
        return new Queue(RAIN_MONITOR_DIRECT_QUEUE, true, false, false);
    }

    /**
     * @author: xsm
     * @date: 2021/03/22 0022 上午 10:35
     * @Description: 排口停产直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue stopProductionDirectQueue() {
        return new Queue(STOP_PRODUCTION_DIRECT_QUEUE, true, false, false);
    }

    /**
     * @author: xsm
     * @date: 2020/2/18 0018 下午 19:39
     * @Description: 视频叠加直连队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue videoOverlayDirectQueue() {
        return new Queue(VIDEO_OVERLAY_DIRECT_QUEUE, true, false, false);
    }



    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 报警数据交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange onlineDirectExchange() {
        return new DirectExchange(ONLINE_DIRECT_EXCHANGE, true, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 点位信息更新交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange pointUpdateDirectExchange() {
        return new DirectExchange(POINT_UPDATE_DIRECT_EXCHANGE, true, false);
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 点位信息更新交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange AQPointUpdateDirectExchange() {
        return new DirectExchange(AQPOINT_UPDATE_DIRECT_EXCHANGE, true, false);
    }
    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 安全报警数据交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange AQOnlineDirectExchange() {
        return new DirectExchange(AQ_ONLINE_DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange AQOnlineVideoExchange() {
        return new DirectExchange(AQ_ONLINE_VIDEO_EXCHANGE, true, false);
    }
    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 报警任务交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange taskDirectExchange() {
        return new DirectExchange(TASK_DIRECT_EXCHANGE, true, false);
    }



    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 雨水监控交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange rainMonitorDirectExchange() {
        return new DirectExchange(RAIN_MONITOR_DIRECT_EXCHANGE, true, false);
    }

    /**
     * @author: xsm
     * @date: 2021/03/22 0022 上午 10:38
     * @Description: 排口停产交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange stopProductionDirectExchange() {
        return new DirectExchange(STOP_PRODUCTION_DIRECT_EXCHANGE, true, false);
    }

    /**
     * @author: xsm
     * @date: 2020/2/18 0018 下午 19:33
     * @Description: 视频叠加监控交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange videoOverlayDirectExchange() {
        return new DirectExchange(VIDEO_OVERLAY_DIRECT_EXCHANGE, true, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定报警数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingOnlineDirect() {
        return BindingBuilder.bind(onlineDirectQueue()).to(onlineDirectExchange()).with(ONLINE_DIRECT_KEY);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定点位更新数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingPointUpdateDirect() {
        return BindingBuilder.bind(pointUpdateDirectQueue()).to(pointUpdateDirectExchange()).with(POINT_UPDATE_DIRECT_KEY);
    }

    /**
     * @author: mmt
     * @date: 2022/8/8 0019 上午 10:39
     * @Description: 绑定点位MN更新数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingPointMnUpdateDirect() {
        return BindingBuilder.bind(pointMnUpdateDirectQueue()).to(pointUpdateDirectExchange()).with(POINT__MN_UPDATE_DIRECT_KEY);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定点位更新数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingAQPointUpdateDirect() {
        return BindingBuilder.bind(AQPointUpdateDirectQueue()).to(AQPointUpdateDirectExchange()).with(AQPOINT_UPDATE_DIRECT_KEY);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定安全报警数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingAQOnlineDirect() {
        return BindingBuilder.bind(AQOnlineDirectQueue()).to(AQOnlineDirectExchange()).with(AQ_ONLINE_DIRECT_KEY);
    }


    /**
     * @author: chengzq
     * @date: 2020/9/19 0019 上午 10:03
     * @Description: 绑定安全视频报警数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Bean
    public Binding bindingAQOnlineVideoDirect() {
        return BindingBuilder.bind(AQOnlineVideoQueue()).to(AQOnlineVideoExchange()).with(AQ_ONLINE_VIDEO_KEY);
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定报警任务队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingTaskDirect() {
        return BindingBuilder.bind(taskDirectQueue()).to(taskDirectExchange()).with(TASK_DIRECT_KEY);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 雨水监控队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingRainMonitorDirect() {
        return BindingBuilder.bind(rainMonitorDirectQueue()).to(rainMonitorDirectExchange()).with(RAIN_MONITOR_DIRECT_KEY);
    }

    /**
     * @author: xsm
     * @date: 2021/03/22 0022 上午 10:39
     * @Description: 排口停产队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingStopProductionDirect() {
        return BindingBuilder.bind(stopProductionDirectQueue()).to(stopProductionDirectExchange()).with(STOP_PRODUCTION_DIRECT_KEY);
    }



    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description: 寄存器信息更新队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue ModBusUpdateDirectQueue() {
        return new Queue(MODBUS_UPDATE_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description: 寄存器信息更新交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange ModBusUpdateDirectExchange() {
        return new DirectExchange(MODBUS_UPDATE_DIRECT_EXCHANGE, true, false);
    }

    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description: 绑定寄存器更新数据队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingModBusUpdateDirect() {
        return BindingBuilder.bind(ModBusUpdateDirectQueue()).to(ModBusUpdateDirectExchange()).with(MODBUS_UPDATE_DIRECT_KEY);
    }




    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:35
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue OfflinePointDirectQueue() {
        return new Queue(OFFLINE_POINT_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:38
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange OfflinePointDirectExchange() {
        return new DirectExchange(OFFLINE_POINT_DIRECT_EXCHANGE, true, false);
    }



    /**
     * @author: lip
     * @date: 2019/7/19 0019 上午 10:39
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingOfflinePointDirect() {
        return BindingBuilder.bind(OfflinePointDirectQueue()).to(OfflinePointDirectExchange()).with(OFFLINE_POINT_DIRECT_KEY);
    }




    /**
     * @author: chengzq
     * @date: 2021/4/26 0026 下午 2:54
     * @Description: 在线数据补充交换机
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Bean
    public DirectExchange onlineSupplyDirectEXCHANGE() {
        return new DirectExchange(ONLINE_Supply_DIRECT_EXCHANGE, true, false);
    }

    /**
     * @author: chengzq
     * @date: 2021/4/26 0026 下午 2:55
     * @Description: 在线数据补充队列
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Bean
    public Queue onlineSupplyDirectQueue() {
        return new Queue(ONLINE_Supply_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: chengzq
     * @date: 2021/4/26 0026 下午 2:57
     * @Description:
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param: []
     * @throws:
     */
    @Bean
    public Binding bindingOnlineSupplyDIRECT() {
        return BindingBuilder.bind(onlineSupplyDirectQueue()).to(onlineSupplyDirectEXCHANGE()).with(ONLINE_Supply_DIRECT_KEY);
    }


    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 1:55
     * @Description:补充队列（超标超限任务）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue AlarmTaskDirectQueue() {
        return new Queue(ALARM_TASK_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 1:55
     * @Description:补充交换机（超标超限任务）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange AlarmTaskDirectExchange() {
        return new DirectExchange(ALARM_TASK_DIRECT_EXCHANGE, true, false);
    }



    /**
     * @author: xsm
     * @date: 2021/12/08 0008 下午 1:55
     * @Description:绑定寄存器更新数据队列和交互器（超标超限任务）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingAlarmTaskDirect() {
        return BindingBuilder.bind(AlarmTaskDirectQueue()).to(AlarmTaskDirectExchange()).with(ALARM_TASK_DIRECT_KEY);
    }

    /**
     * @author: xsm
     * @date: 2022/01/04 0004 下午 6:37
     * @Description:补充队列（反控命令）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Queue AntiControlDirectQueue() {
        return new Queue(ANTI_CONTROL_DIRECT_QUEUE, true, false, false);
    }


    /**
     * @author: xsm
     * @date: 2022/01/04 0004 下午 6:37
     * @Description:补充交换机（反控命令）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public DirectExchange AntiControlDirectExchange() {
        return new DirectExchange(ANTI_CONTROL_DIRECT_EXCHANGE, true, false);
    }



    /**
     * @author: xsm
     * @date: 2022/01/04 0004 下午 6:37
     * @Description:绑定寄存器更新数据队列和交互器（反控命令）
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingAntiControlDirect() {
        return BindingBuilder.bind(AntiControlDirectQueue()).to(AntiControlDirectExchange()).with(ANTI_CONTROL_DIRECT_KEY);
    }

    /**
     * @author: xsm
     * @date: 2020/2/18 0018 下午 19:36
     * @Description: 绑定视频叠加队列和交互器
     * @updateUser:
     * @updateDate:
     * @updateDescription:
     * @param:
     * @return:
     */
    @Bean
    public Binding bindingVideoOverlayDirect() {
        return BindingBuilder.bind(videoOverlayDirectQueue()).to(videoOverlayDirectExchange()).with(VIDEO_OVERLAY_DIRECT_KEY);
    }


    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(producerJackson2MessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory messageHandlerMethodFactory = new DefaultMessageHandlerMethodFactory();
        messageHandlerMethodFactory.setMessageConverter(consumerJackson2MessageConverter());
        return messageHandlerMethodFactory;
    }

    @Bean
    public MappingJackson2MessageConverter consumerJackson2MessageConverter() {
        return new MappingJackson2MessageConverter();
    }
}
