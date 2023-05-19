package com.tjpu.pk.common.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author: lip
 * @date: 2018年7月27日 下午3:48:23
 * @Description:数据源配置类
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 */

@Component
public class DataSourceConfig implements ImportBeanDefinitionRegistrar, EnvironmentAware {

	// 指定默认数据源(springboot2.0默认数据源是hikari如何想使用其他数据源可以自己配置)
	//private static final String DATASOURCE_TYPE_DEFAULT = "com.zaxxer.hikari.HikariDataSource";

	// 默认数据源key
	private String defaultKey;

	// 默认数据源
	private DataSource defaultDataSource;
	// 用户自定义数据源
	private Map<String, DataSource> slaveDataSources = new HashMap<>();

	@Override
	public void setEnvironment(Environment environment) {
		initDefaultDataSource(environment);
		initslaveDataSources(environment);
	}

	private void initDefaultDataSource(Environment environment) {
		// 读取主数据源

		defaultKey = environment.getProperty("spring.datasource.primary.name");

		Map<String, Object> dsMap = new HashMap<>();
		dsMap.put("driver-class-name", environment.getProperty("spring.datasource.primary.driver-class-name"));
		dsMap.put("jdbc-url", environment.getProperty("spring.datasource.primary.jdbc-url"));
		dsMap.put("username", environment.getProperty("spring.datasource.primary.username"));
		dsMap.put("password", environment.getProperty("spring.datasource.primary.password"));
		defaultDataSource = buildDataSource(dsMap, environment);
	}

	private void initslaveDataSources(Environment env) {
		// 读取配置文件获取更多数据源
		String slaveRootPath = "spring.datasource.slave.";
		String dsPrefixs = env.getProperty(slaveRootPath + "names");
		if (dsPrefixs!=null&&!"".equals(dsPrefixs)){
			for (String dsPrefix : dsPrefixs.split(",")) {
				// 多个数据源
				Map<String, Object> dsMap = new HashMap<>();
				dsMap.put("driver-class-name", env.getProperty(slaveRootPath + dsPrefix + ".driver-class-name"));
				dsMap.put("jdbc-url", env.getProperty(slaveRootPath + dsPrefix + ".jdbc-url"));
				dsMap.put("username", env.getProperty(slaveRootPath + dsPrefix + ".username"));
				dsMap.put("password", env.getProperty(slaveRootPath + dsPrefix + ".password"));
				DataSource ds = buildDataSource(dsMap, env);
				slaveDataSources.put(dsPrefix, ds);
			}
		}
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
		Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
		// 添加默认数据源
		targetDataSources.put(defaultKey, this.defaultDataSource);
		DynamicDataSourceContextHolderUtil.dataSourceIds.add(defaultKey);
		// 添加其他数据源
		targetDataSources.putAll(slaveDataSources);
		for (String key : slaveDataSources.keySet()) {
			DynamicDataSourceContextHolderUtil.dataSourceIds.add(key);
		}

		// 创建DynamicDataSource
		GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
		beanDefinition.setBeanClass(DynamicDataSourceUtil.class);
		beanDefinition.setSynthetic(true);
		MutablePropertyValues mpv = beanDefinition.getPropertyValues();
		mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
		mpv.addPropertyValue("targetDataSources", targetDataSources);
		// 注册 - BeanDefinitionRegistry
		beanDefinitionRegistry.registerBeanDefinition("dataSource", beanDefinition);

	}

	public DataSource buildDataSource(Map<String, Object> dataSourceMap, Environment environment) {
		// 使用HikariCP连接池配置
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(dataSourceMap.get("driver-class-name").toString());
		hikariConfig.setJdbcUrl(dataSourceMap.get("jdbc-url").toString());
		hikariConfig.setUsername(dataSourceMap.get("username").toString());
		hikariConfig.setPassword(dataSourceMap.get("password").toString());

		String hikariPrefix = "spring.datasource.hikari.";
		// 配置文件里的配置项
		hikariConfig.setMinimumIdle(environment.getProperty(hikariPrefix + "minimum-idle", Integer.class));
		hikariConfig.setMaximumPoolSize(environment.getProperty(hikariPrefix + "maximum-pool-size", Integer.class));
		hikariConfig.setAutoCommit(environment.getProperty(hikariPrefix + "auto-commit", Boolean.class));
		hikariConfig.setIdleTimeout(environment.getProperty(hikariPrefix + "idle-timeout", Long.class));
		hikariConfig.setPoolName(environment.getProperty(hikariPrefix + "pool-name", String.class));
		hikariConfig.setMaxLifetime(environment.getProperty(hikariPrefix + "max-lifetime", Long.class));
		hikariConfig.setConnectionTimeout(environment.getProperty(hikariPrefix + "connection-timeout", Long.class));
		hikariConfig.setConnectionTestQuery(environment.getProperty(hikariPrefix + "connection-test-query", String.class));
		// 扩展配置项（只用于mysql？）
		/*
		 * hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		 * hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		 * hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		 * hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
		 */
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);

		return dataSource;
	}

}
