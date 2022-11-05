package com.jie.file.storage;

import com.jie.file.entity.File;
import com.jie.file.properties.FileServerProperties;
import com.jie.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 阿里云文件存储策略
 * @author jie
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(value = "jie.file.type", havingValue = "ALI_OSS")
public class AliOssAutoConfigure {


//    @Bean
//    public FileStrategy getFileStrategy() {
//        return new LocalAutoConfigure.LocalServiceImpl();
//    }

    /**
     * 阿里云上传服务
     */
    public static class AliOssServiceImpl extends AbstractFileStrategy {


        @Override
        public String getFileUrl(String fileName) throws Exception {
            return null;
        }

        @Override
        public void uploadFile(File file, MultipartFile multipartFile) throws IOException {

        }

        @Override
        public boolean delete(String fileName) {
            return false;
        }

        @Override
        public boolean delete(String... fileNames) {
            return false;
        }

        @Override
        public boolean delete(File file) {
            return false;
        }

        @Override
        public boolean delete(List<File> listFile) {
            return false;
        }
    }
}
