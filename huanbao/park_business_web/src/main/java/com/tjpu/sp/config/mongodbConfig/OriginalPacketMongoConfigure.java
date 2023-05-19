package com.tjpu.sp.config.mongodbConfig;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OriginalPacketMongoConfigure implements EnvironmentAware {
    @Bean(name = "originalPacketMongoTemplate")
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
        String host = environment.getProperty("mongodb.originalpacket.host");
        String port = environment.getProperty("mongodb.originalpacket.port");

        if(StringUtils.isNotBlank(host)&&StringUtils.isNotBlank(port)){
            String [] hosts = host.split(",");
            String [] ports = port.split(",");
            for (int i = 0; i <hosts.length ; i++) {
                String oneHost = hosts[i];
                Integer onePort = Integer.parseInt(ports[i]);
                ServerAddress serverAddress = new ServerAddress(oneHost, onePort);
                serverAddresses.add(serverAddress);
            }
        }
        String dataBase = environment.getProperty("mongodb.originalpacket.database");
        String dbUser = environment.getProperty("mongodb.originalpacket.username");
        String dbPwd = environment.getProperty("mongodb.originalpacket.password");
        MongoClient mongoClient;
        if (StringUtils.isNotBlank(dbUser)&&StringUtils.isNotBlank(dbPwd)){
            MongoCredential credential = MongoCredential.createCredential(dbUser, dataBase, dbPwd.toCharArray());
            mongoClient  = new MongoClient(serverAddresses, credential, mongoClientOptions);
        }else {
            mongoClient  = new MongoClient(serverAddresses,mongoClientOptions);
        }
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient,dataBase);

        return new MongoTemplate(mongoDbFactory);
    }
    @Override
    public void setEnvironment(Environment environment) {
        mongoDbFactory(environment);
    }



}
