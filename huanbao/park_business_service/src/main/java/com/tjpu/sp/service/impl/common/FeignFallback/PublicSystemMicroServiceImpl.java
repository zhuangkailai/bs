package com.tjpu.sp.service.impl.common.FeignFallback;


import com.tjpu.sp.service.common.micro.PublicSystemMicroService;
import net.sf.json.JSONObject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

 





import com.tjpu.pk.common.utils.AuthUtil;

/**
 * 
 * @author: lip
 * @date: 2018年4月10日 上午10:54:02
 * @Description:微服务调用异常处理类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
@Component
public class PublicSystemMicroServiceImpl implements PublicSystemMicroService {
	 
	/**
	 * 
	 * @author: lip
	 * @date: 2018年4月10日 下午2:03:38
	 * @Description: 远程微服务调用，获取token
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param authParam
	 * @return
	 */
	@Override
	public Object getAuthDataByParam(JSONObject authParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object getListByParam(String authParam) {
		return AuthUtil.returnObject("fail",  "");
	}
	@Override
	public Object getListData(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}
	@Override
	public Object getDetail(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object doAddMethod(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object doEditMethod(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object deleteMethod(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object getQueryCriteriaData(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object getUserButtonAuthInMenu(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object getTableTitle(String microParam) {
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public ResponseEntity<byte[]> exportExcel(String microParam) {
		return null;
	}

	@Override
	public Object getAddPageInfo(String microParam) {
		// TODO Auto-generated method stub
		return AuthUtil.returnObject("fail",  "");
	}

	@Override
	public Object goUpdatePage(String microParam) {
		// TODO Auto-generated method stub
		return AuthUtil.returnObject("fail",  "");
	}

}
