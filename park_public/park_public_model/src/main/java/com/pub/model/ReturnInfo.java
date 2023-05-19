package com.pub.model;

public class ReturnInfo<T> {

	public static final String success = "success";

	public static final String fail = "fail";


	private String flag;

	private String state;

	private T data;


	private String errormessage;
	
	
	public String getErrormessage() {
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
