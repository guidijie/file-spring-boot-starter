package com.jie.file.config;

import com.jie.file.properties.FileServerProperties;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "jie.file.type", havingValue = "MINIO")
@Slf4j
public class MinioConfig {

    @Autowired
    protected FileServerProperties fileProperties;

    @Bean
    public MinioClient minioClient(){
        if (fileProperties == null) {
            return null;
        }
        return MinioClient.builder()
                .endpoint(fileProperties.getMinio().getEndpoint())
                .credentials(fileProperties.getMinio().getAccessKey(), fileProperties.getMinio().getSecretKey())
                .build();
    }

}
