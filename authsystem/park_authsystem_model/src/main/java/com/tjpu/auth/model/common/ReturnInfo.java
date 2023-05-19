package com.tjpu.auth.model.common;

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
    /**
     * 后台代码成功
     */
    public static final String success = "success";
    /**
     * 后台代码出错
     */
    public static final String fail = "fail";
    //后台判断标记
    private String flag;
    //错误信息
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


}
