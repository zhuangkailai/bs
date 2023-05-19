package com.tjpu.sp.config.mongodbConfig;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class PrimaryMongoConfigure implements EnvironmentAware {
    @Primary
    @Bean(name = "primaryMongoTemplate")
    public MongoTemplate mongoDbFactory(Environment environment) {
        //客户端配置（连接数，副本集群验证）
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.connectionsPerHost(Integer.valueOf(environment.getProperty("mongo.options.max-connections-per-host")));
        builder.minConnectionsPerHost(Integer.valueOf(environment.getProperty("mongo.options.min-connections-per-host")));
        builder.threadsAllowedToBlockForConnectionMultiplier(
                Integer.valueOf(environment.getProperty("mongo.options.threads-allowed-to-block-for-connection-multiplier")));
        builder.serverSelectionTimeout(Integer.valueOf(environment.getProperty("mongo.options.server-selection-timeout")));
        builder.maxWaitTime(Integer.valueOf(environment.getProperty("mongo.options.max-wait-time")));
        builder.connectTimeout(Integer.valueOf(environment.getProperty("mongo.options.connect-timeout")));
        builder.socketTimeout(Integer.valueOf(environment.getProperty("mongo.options.socket-timeout")));

        MongoClientOptions mongoClientOptions = builder.build();
        // MongoDB地址列表
        List<ServerAddress> serverAddresses = new ArrayList<>();
        String host = environment.getProperty("mongodb.primary.host");
        String port = environment.getProperty("mongodb.primary.port");
        if (StringUtils.isNotBlank(host) && StringUtils.isNotBlank(port)) {
            String[] hosts = host.split(",");
            String[] ports = port.split(",");
            for (int i = 0; i < hosts.length; i++) {
                String oneHost = hosts[i];
                Integer onePort = Integer.parseInt(ports[i]);
                ServerAddress serverAddress = new ServerAddress(oneHost, onePort);
                serverAddresses.add(serverAddress);
            }
        }
        String dataBase = environment.getProperty("mongodb.primary.database");

        MongoClient mongoClient;
        String dbUser = environment.getProperty("mongodb.primary.username");
        String dbPwd = environment.getProperty("mongodb.primary.password");
        if (StringUtils.isNotBlank(dbUser)&&StringUtils.isNotBlank(dbPwd)){
            MongoCredential credential = MongoCredential.createCredential(dbUser, dataBase, dbPwd.toCharArray());
            mongoClient  = new MongoClient(serverAddresses, credential, mongoClientOptions);
        }else {
            mongoClient  = new MongoClient(serverAddresses,mongoClientOptions);
        }
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient, dataBase);
        //去掉_class
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), new MongoMappingContext());
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return new MongoTemplate(mongoDbFactory, converter);
    }

    @Override
    public void setEnvironment(Environment environment) {
        mongoDbFactory(environment);
    }


}
