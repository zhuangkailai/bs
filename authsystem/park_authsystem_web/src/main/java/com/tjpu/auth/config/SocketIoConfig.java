package com.tjpu.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;

/**
 * 
 * @author: lip
 * @date: 2018年3月14日 下午4:43:01
 * @Description:socketIO配置类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@Configuration
public class SocketIoConfig {

	@Value("${socketIp}")
	private String socketIp;

	@Value("${socketPost}")
	private String socketPost;

	@Bean
	public SocketIOServer socketIOServer() {
		com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();

		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) { // 在本地window环境测试时用localhost
			config.setHostname(socketIp);
		} else {
			config.setHostname(socketIp); // 部署到你的远程服务器正式发布环境时用服务器公网ip
		}
		config.setPort(Integer.parseInt(socketPost));
		//config.setContext("socket");

		/*
		 * config.setAuthorizationListener(new AuthorizationListener() {//类似过滤器
		 * 
		 * @Override public boolean isAuthorized(HandshakeData data) {
		 * //http://localhost:8081?username=test&password=test
		 * //例如果使用上面的链接进行connect，可以使用如下代码获取用户密码信息，本文不做身份验证 // String username =
		 * data.getSingleUrlParam("username"); // String password =
		 * data.getSingleUrlParam("password"); return true; } });
		 */

		final SocketIOServer server = new SocketIOServer(config);
		

		return server;
	}

	@Bean
	public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketServer) {
		return new SpringAnnotationScanner(socketServer);
	}

}
