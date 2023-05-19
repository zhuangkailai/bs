package com.tjpu.pk.common.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSourceUtil extends AbstractRoutingDataSource {
	@Override
	protected Object determineCurrentLookupKey() {

		/*
		 * 
		 * DynamicDataSourceContextHolder代码中使用setDataSourceType
		 * 
		 * 设置当前的数据源，在路由类中使用getDataSourceType进行获取，
		 * 
		 * 交给AbstractRoutingDataSource进行注入使用。
		 */

		return DynamicDataSourceContextHolderUtil.getDataSourceType();

	}
}
