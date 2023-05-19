package com.pub.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UserInfoService {

	List<Map<String,Object>> getSystemAccessTokenByParam(HashMap<String, Object> hashMap);
}
