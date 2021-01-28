package com.qinfei.core.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.alibaba.druid.filter.config.ConfigTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;

/**
 * 配置阿里数据源
 */
@Configuration
@ConditionalOnClass(DruidDataSource.class)
// @ConfigurationProperties(prefix="spring.datasource.druid")
// @ConditionalOnProperty(name = "spring.datasource.type", havingValue =
// "DruidDataSource", matchIfMissing = true)
public class DruidDataSourceConfig {

	@Value("${spring.datasource.druid.initialSize}")
	private int initialSize;

	@Value("${spring.datasource.druid.minIdle}")
	private int minIdle;

	@Value("${spring.datasource.druid.maxActive}")
	private int maxActive;

	@Value("${spring.datasource.druid.maxWait}")
	private int maxWait;

	@Value("${spring.datasource.druid.timeBetweenEvictionRunsMillis}")
	private int timeBetweenEvictionRunsMillis;

	@Value("${spring.datasource.druid.minEvictableIdleTimeMillis}")
	private int minEvictableIdleTimeMillis;

	@Value("${spring.datasource.druid.validationQuery}")
	private String validationQuery;

	@Value("${spring.datasource.druid.testWhileIdle}")
	private boolean testWhileIdle;

	@Value("${spring.datasource.druid.testOnBorrow}")
	private boolean testOnBorrow;

	@Value("${spring.datasource.druid.testOnReturn}")
	private boolean testOnReturn;

	@Value("${spring.datasource.druid.filters}")
	private String filters;

	// @Value("${spring.datasource.druid.logSlowSql}")
	// private String logSlowSql;

	@Value("${spring.datasource.druid.connectionProperties}")
	private String connectionProperties;

	@SuppressWarnings("unchecked")
	private <T> T createDataSource(DataSourceProperties properties, Class<? extends DataSource> type) {
		return (T) properties.initializeDataSourceBuilder().type(type).build();
	}

	/**
	 * @param properties
	 *            读入的配置
	 * @return DruidDataSource
	 */
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Bean
	@ConfigurationProperties("spring.datasource.druid")
	public DruidDataSource dataSource(DataSourceProperties properties) {
		DruidDataSource dataSource = createDataSource(properties, DruidDataSource.class);
		DatabaseDriver databaseDriver = DatabaseDriver.fromJdbcUrl(properties.determineUrl());
		String validationQuery = databaseDriver.getValidationQuery();
		if (validationQuery != null) {
			dataSource.setTestOnBorrow(true);
			dataSource.setValidationQuery(validationQuery);
		}
		dataSource.setInitialSize(initialSize);
		dataSource.setMinIdle(minIdle);
		dataSource.setMaxActive(maxActive);
		dataSource.setMaxWait(maxWait);

		dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
		dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
		dataSource.setValidationQuery(validationQuery);
		dataSource.setTestWhileIdle(testWhileIdle);
		dataSource.setTestOnBorrow(testOnBorrow);
		dataSource.setTestOnReturn(testOnReturn);
		if (connectionProperties != null) {
			// String paramArray[] = connectionProperties.split(";");
			dataSource.setConnectionProperties(connectionProperties);
		}
		try {
			dataSource.setFilters(filters);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataSource;
	}

	/**
	 * 注册一个StatViewServlet
	 */
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Bean
	@ConfigurationProperties("spring.datasource.druid")
	public ServletRegistrationBean druidStatViewServlet(DataSourceProperties properties) {
		// org.springframework.boot.context.embedded.ServletRegistrationBean提供类的进行注册.
		// ServletRegistrationBean srb = new ServletRegistrationBean(new
		// StatViewServlet(), "/druid/*");
		ServletRegistrationBean srb = new ServletRegistrationBean();
		srb.setServlet(new StatViewServlet());
		srb.addUrlMappings("/druid/*");
		// 添加初始化参数：initParams
		// 白名单：
//		srb.addInitParameter("allow", "112.74.63.48");
//		// IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to
//		// view this page.
//		srb.addInitParameter("deny", "192.168.1.73");
		// 登录查看信息的账号密码.
		// srb.addInitParameter("loginUsername", "root");
//		srb.addInitParameter("loginUsername", properties.getUsername());
//		// srb.addInitParameter("loginPassword", "password");
//		srb.addInitParameter("loginPassword", properties.getPassword());
		// 是否能够重置数据.
		// srb.addInitParameter("resetEnable", "false");// 禁用HTML页面上的“Reset All”功能
		// srb.addInitParameter("logSlowSql", logSlowSql);

		return srb;
	}

	/**
	 * 注册一个：filterRegistrationBean
	 */
	@Bean
	public FilterRegistrationBean druidStatFilter() {
		FilterRegistrationBean frb = new FilterRegistrationBean(new WebStatFilter());
		frb.setName("druidWebStatFilter");
		// 添加过滤规则.
		frb.addUrlPatterns("/*");
		// 添加忽略的格式信息.
		frb.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		frb.addInitParameter("profileEnable", "true");
		return frb;
	}

}