package com.jie.file.storage;

import com.jie.file.entity.File;
import com.jie.file.properties.FileServerProperties;
import com.jie.file.strategy.FileStrategy;
import com.jie.file.strategy.impl.AbstractFileStrategy;
import com.jie.file.utils.DateUtils;
import com.jie.file.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 本地上传策略配置类
 *
 * @author jie
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(value = "jie.file.type", havingValue = "LOCAL")
@ConditionalOnClass({MultipartFile.class})
public class LocalAutoConfigure {


    @Bean
    public FileStrategy getFileStrategy() {
        return new LocalAutoConfigure.LocalServiceImpl();
    }

    /**
     * 本地上传服务
     */
    public static class LocalServiceImpl extends AbstractFileStrategy implements InitializingBean {

        public LocalServiceImpl() {
            log.info("=========================本地存储策略！");
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            properties = fileProperties.getLocal();
            log.info("=========================本地存储策略创建完成！");
        }

        @Override
        public void uploadFile(File file, MultipartFile multipartFile) throws IOException {
            String endpoint = properties.getEndpoint();
            String bucketName = properties.getBucketName();
            String uriPrefix = properties.getUriPrefix();

            //使用UUID为文件生成新文件名
            String fileName = UUID.randomUUID().toString() + StrPool.DOT + file.getExt();

            // D:\\uploadFiles\\oss-file-service\\2020\\05\\xxx.doc
            //日期目录
            String relativePath = Paths.get(LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT))).toString();

            //上传文件存储的绝对目录 例如：D:\\uploadFiles\\oss-file-service\\2020\\05
            relativePath = Paths.get(endpoint, bucketName, relativePath).toString();
            if (file.getDir() != null && !"".equals(file.getDir())) {
                relativePath = Paths.get(endpoint, bucketName, file.getDir(), relativePath).toString();
            }

            //目标输出文件D:\\uploadFiles\\oss-file-service\\2020\\05\\xxx.doc
            java.io.File outFile = new java.io.File(Paths.get(relativePath, fileName).toString());

            //向目标文件写入数据
            FileUtils.writeByteArrayToFile(outFile, multipartFile.getBytes());

            //文件上传完成后需要设置File对象的属性(url，filename，relativePath），用于保存到数据库
            String url = getUriPrefix() +
                    StrPool.SLASH +
                    properties.getBucketName() +
                    StrPool.SLASH +
                    relativePath +
                    StrPool.SLASH +
                    fileName;
            //替换掉windows环境的\路径
            url = url.replace("\\\\", StrPool.SLASH);
            url = url.replace("\\", StrPool.SLASH);
            file.setFileName(fileName);
            file.setRelativePath(relativePath);
        }

        /**
         * 文件删除
         */
        @Override
        public boolean delete(String fileName) {
            if (fileName == null) {
                log.error("删除文件为空！");
                return false;
            }
            return deleteFile(fileName);
        }

        /**
         * 文件删除
         */
        @Override
        public boolean delete(String... fileNames) {
            if (fileNames == null || fileNames.length < 1) {
                log.error("删除文件集合为空！");
                return false;
            }
            int errNum = fileNames.length;
            for (String fileName : fileNames) {
                try {
                    String filePath = Paths.get(properties.getEndpoint(), properties.getBucketName(), fileName).toString();
                    java.io.File rmFile = new java.io.File(filePath);
                    FileUtils.deleteQuietly(rmFile);
                    --errNum;
                } catch (Exception e) {
                    log.error("删除文件{}时发生错误！", fileName);
                    break;
                }
            }
            return errNum <= 0;
        }

        /**
         * 文件删除
         */
        @Override
        public boolean delete(File file) {
            if (file == null) {
                log.error("删除文件为空！");
                return false;
            }
            // 拼接要删除的文件的绝对磁盘路径
            int errNum = 1;
            try {
                String filePath = Paths.get(properties.getEndpoint(), properties.getBucketName(), file.getRelativePath()).toString();
                java.io.File rmFile = new java.io.File(filePath);
                FileUtils.deleteQuietly(rmFile);
                --errNum;
            } catch (Exception e) {
                log.error("删除文件{}时发生错误！", file.getFileName());
            }
            return errNum <= 0;
        }

        /**
         * 文件删除
         */
        @Override
        public boolean delete(List<File> listFile) {
            if (CollectionUtils.isEmpty(listFile)) {
                log.error("删除文件集合为空！");
                return false;
            }
            int errNum = listFile.size();
            for (File file : listFile) {
                try {
                    String filePath = Paths.get(properties.getEndpoint(), properties.getBucketName(), file.getRelativePath()).toString();
                    java.io.File rmFile = new java.io.File(filePath);
                    FileUtils.deleteQuietly(rmFile);
                    --errNum;
                } catch (Exception e) {
                    log.error("删除文件{}时发生错误！", file.getFileName());
                    break;
                }
            }
            return errNum <= 0;
        }

        private boolean deleteFile(String fileName) {
            // 拼接要删除的文件的绝对磁盘路径
            int errNum = 1;
            try {
                String filePath = Paths.get(properties.getEndpoint(), properties.getBucketName(), fileName).toString();
                java.io.File rmFile = new java.io.File(filePath);
                FileUtils.deleteQuietly(rmFile);
                --errNum;
            } catch (Exception e) {
                log.error("删除文件{}时发生错误！", fileName);
            }
            return errNum <= 0;
        }


        /**
         * 获取文件访问地址
         *
         * @param fileName 文件名称
         * @return 文件访问地址
         */
        @Override
        public String getFileUrl(String fileName) throws Exception {
            if (fileName != null && !fileName.contains(FILE_SPLIT)) {
                throw new Exception("上传文件名称缺少后缀");
            }
            return properties.getUriPrefix() +  fileName;
        }

        /**
         * 获取文件流
         * @param fileName 文件名称
         * @return 文件流
         */
        @Override
        public InputStream getFileInputStream(String fileName) {
            try {
                return new FileInputStream(Paths.get(properties.getEndpoint(), fileName).toString());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("读取文件流失败！");
            }
            return null;
        }

        /**
         * 判断文件是否存在
         * @param fileName 文件名称
         * @return 是否存在
         */
        @Override
        public boolean fileExists(String fileName) {
            try {
                return Paths.get(properties.getEndpoint(), fileName).toFile().exists();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("获取文件失败！");
                return false;
            }
        }
    }
}
