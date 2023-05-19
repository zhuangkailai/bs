package com.tjpu.pk.common.datasource;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author: lip
 * @date: 2018年7月12日 下午1:34:55
 * @Description:多数据源切换工具类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
public class DynamicDataSourceContextHolderUtil {
	/*
	 * 
	 * 当使用ThreadLocal维护变量时，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
	 * 
	 * 所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
	 */

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	/*
	 * 
	 * 管理所有的数据源id;
	 * 
	 * 主要是为了判断数据源是否存在;
	 */

	public static List<String> dataSourceIds = new ArrayList<String>();

	/**
	 * 
	 * @author: lip
	 * @date: 2018年7月12日 下午1:35:56
	 * @Description: 使用setDataSourceType设置当前的
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param dataSourceType
	 */

	public static void setDataSourceType(String dataSourceType) {

		contextHolder.set(dataSourceType);

	}

	/**
	 * 
	 * @author: lip
	 * @date: 2018年7月12日 下午1:36:06
	 * @Description: 获取当前DataSourceType
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @return
	 */
	public static String getDataSourceType() {

		return contextHolder.get();

	}

	/**
	 * 
	 * @author: lip
	 * @date: 2018年7月12日 下午1:36:31
	 * @Description: 清除当前DataSourceType
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 */
	public static void clearDataSourceType() {

		contextHolder.remove();

	}

	/**
	 * 
	 * @author: lip
	 * @date: 2018年7月12日 下午1:38:41
	 * @Description: 判断数据源是否存在
	 * @updateUser:
	 * @updateDate:
	 * @updateDescription:
	 * @param dataSourceId
	 * @return
	 */
	public static boolean containsDataSource(String dataSourceId) {
		return dataSourceIds.contains(dataSourceId);

	}

}
