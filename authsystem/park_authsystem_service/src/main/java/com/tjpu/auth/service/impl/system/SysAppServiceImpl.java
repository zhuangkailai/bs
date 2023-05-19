package com.tjpu.auth.service.impl.system;

import com.tjpu.auth.dao.system.SysAppMapper;
import com.tjpu.auth.service.system.SysAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @version V1.0
 * @author: lip
 * @date: 2018年4月3日 上午11:53:41
 * @Description:系统接口处理实现类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Service
@Transactional
public class SysAppServiceImpl implements SysAppService {

	@Autowired
	private SysAppMapper sysAppMapper;

}
