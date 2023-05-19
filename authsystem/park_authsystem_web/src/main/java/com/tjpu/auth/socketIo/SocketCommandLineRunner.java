package com.tjpu.auth.socketIo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
 
import com.corundumstudio.socketio.SocketIOServer;


/**
 * 
* @author: lip 
* @date: 2018年7月2日 下午5:06:29
* @Description:启动加载类
* @updateUser: 
* @updateDate: 
* @updateDescription:
* @version V1.0  
*
 */

@Component
@Order(value = 1)
public class SocketCommandLineRunner implements CommandLineRunner {
	private final SocketIOServer server;

	@Autowired
	public SocketCommandLineRunner(SocketIOServer server) {
		this.server = server;
	}

	@Override
	public void run(String... args) throws Exception {
		server.start();
		System.out.println("socket.io启动成功！");
	}
}
