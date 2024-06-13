package com.zht.dbrouter.config;

import com.zht.dbrouter.DBRouterConfig;
import com.zht.dbrouter.DBRouterJoinPoint;
import com.zht.dbrouter.dynamic.DynamicDataSource;
import com.zht.dbrouter.util.PropertyUtil;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceAutoConfig implements EnvironmentAware {


    private Map<String, Map<String, Object>> dataSourceMap = new HashMap<>();

    private int dbCount;  //分库数
    private int tbCount;  //分表数


    @Bean
    public DBRouterConfig dbRouterConfig(){
        return new DBRouterConfig(dbCount ,tbCount);
    }

    @Bean
    public DataSource dataSource(){
        Map<Object , Object> targetDataSources = new HashMap<>();
        for (String dbInfo: dataSourceMap.keySet()
             ) {
            Map<String, Object> objMap = dataSourceMap.get(dbInfo);
            targetDataSources.put(dbInfo, new DriverManagerDataSource(objMap.get("url").toString(), objMap.get("username").toString(), objMap.get("password").toString()));
        }

        // 动态源接受一个HashMap ， V是一个驱动对象（选择一种创建方式即可）
        DynamicDataSource dynamicDataSource = new DynamicDataSource();

        dynamicDataSource.setTargetDataSources(targetDataSources);
        return dynamicDataSource;

    }

    @Bean("db-router-point")
    public DBRouterJoinPoint dbRouterJoinPoint(){
        return new DBRouterJoinPoint();
    }

    @Override
    public void setEnvironment(Environment environment) {
        String prefix = "router.jdbc.datasource.";

        dbCount = Integer.valueOf(environment.getProperty(prefix + "dbCount"));
        tbCount = Integer.valueOf(environment.getProperty(prefix + "tbCount"));

        String dataSources = environment.getProperty(prefix + "list");
        for (String dbInfo : dataSources.split(",")) {
            Map<String, Object> dataSourceProps = PropertyUtil.handle(environment, prefix + dbInfo, Map.class);
            dataSourceMap.put(dbInfo, dataSourceProps);
        }
    }
}

