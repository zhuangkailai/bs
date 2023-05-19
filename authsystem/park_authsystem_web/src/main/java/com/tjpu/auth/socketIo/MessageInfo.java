package com.tjpu.auth.socketIo;

public class MessageInfo {
	String MsgContent;

	/**提示信息：多个用户*/
	public static String tipManyUser = "当前浏览器有其他用户登录，请重新登录。";
	/**提示信息：多个会话*/
	public static String tipManySession = "当前用户在其他地方登录，被迫下线。";
	
	
	/**提示标记：多个用户*/
	public static String markManyUser = "manyuser";
	/**提示标记：多个会话*/
	public static String markManySession = "manysession";
	
	/**连接成功*/
	public static String successMark = "successsocket";

	/**连接失败*/
	public static String failMark = "failsocket";
	
	/**登录失效*/
	public static String noSession = "nosession";


	/**授权成功*/
	public static String rightSuccess = "rightsuccess";

	/**扫描成功*/
	public static String scanSuccess = "scansuccess";


	/**扫描成功*/
	public static String scanOver = "scanover";


	public String getMsgContent() {
		return MsgContent;
	}

	public void setMsgContent(String msgContent) {
		MsgContent = msgContent;
	}

}
