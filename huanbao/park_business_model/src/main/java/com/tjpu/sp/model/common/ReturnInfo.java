package com.tjpu.sp.model.common;

/***
 * 
 * @author: lip
 * @date: 2018年4月13日 下午1:38:40
 * @Description:返回前端对象
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 * @param <T>
 */
public class ReturnInfo<T> {
	/**后台代码成功*/
	public static final String success = "success";

	/**后台其他提示信息*/
	public static final String other_many = "other#数据量太大，请重新选择查询条件！";
	/**后台代码出错*/
	public static final String fail = "fail";

	//后台判断标记
	private String flag;
	//前端判断标记
	private String state;
	//响应数据
	private T data;

	//错误信息
	private String errormessage;
	
	
	public String getErrorMessage() {
		return errormessage;
	}

	public void setErrormessage(String errorMessage) {
		this.errormessage = errorMessage;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
