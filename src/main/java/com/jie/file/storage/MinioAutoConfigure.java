package com.jie.file.storage;

import com.jie.file.entity.File;
import com.jie.file.properties.FileServerProperties;
import com.jie.file.strategy.FileStrategy;
import com.jie.file.strategy.impl.AbstractFileStrategy;
import com.jie.file.utils.DateUtils;
import com.jie.file.utils.StrPool;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * minio存储策略
 *
 * @author jie
 */
@Configuration
@Slf4j
@EnableConfigurationProperties({FileServerProperties.class})
@ConditionalOnProperty(value = "jie.file.type", havingValue = "MINIO")
@ConditionalOnClass({MultipartFile.class, MinioClient.class})
public class MinioAutoConfigure {

    @Bean
    public FileStrategy getFileStrategy() {
        return new MinioAutoConfigure.MinioServiceImpl();
    }

    /**
     * minio上传服务
     */
    public static class MinioServiceImpl extends AbstractFileStrategy implements InitializingBean {

        public MinioServiceImpl() {
            log.info("=========================Minio储策略！");
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            properties = fileProperties.getMinio();
            log.info("=========================Minio储策略创建完成！");
        }

        @Autowired
        private MinioClient minioClient;

        @Override
        public void uploadFile(File file, MultipartFile multipartFile) {
            String bucketName = properties.getBucketName();

            existBucket(properties.getBucketName());

            //使用UUID为文件生成新文件名
            String fileName = UUID.randomUUID().toString() + StrPool.DOT + file.getExt();

            //日期目录
            String relativePath = Paths.get(LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_DATE_FORMAT))).toString();
            // 最终路径
            relativePath = Paths.get(relativePath, fileName).toString().replaceAll("\\\\", "/");
            if (file.getDir() != null && !"".equals(file.getDir())) {
                relativePath = Paths.get(file.getDir(), bucketName, relativePath).toString().replaceAll("\\\\", "/");
            }

            file.setFileName(fileName);
            file.setRelativePath(relativePath);

            InputStream in = null;
            try {
                in = multipartFile.getInputStream();
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(relativePath)
                        .stream(in, in.available(), -1)
                        .contentType(multipartFile.getContentType())
                        .build()
                );
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        @Override
        public boolean delete(String fileName) {
            if (fileName == null && "".equals(fileName)) {
                log.error("删除文件为空！");
                return false;
            }
            return deleteFile(fileName);
        }


        @Override
        public boolean delete(String... fileNames) {
            if (fileNames == null || fileNames.length < 1) {
                log.error("删除文件集合为空！");
                return false;
            }
            int errNum = fileNames.length;
            List<DeleteObject> deleteObjectList = new CopyOnWriteArrayList<>();
            for (String fileName : fileNames) {
                deleteObjectList.add(new DeleteObject(fileName));
            }
            errNum = 0;
            try {
                final Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(properties.getBucketName()).objects(deleteObjectList).build());
                for (Result<DeleteError> result : results) {
                    final DeleteError deleteError = result.get();
                    log.error("删除文件{}时发生错误！", deleteError.objectName());
                    log.error(deleteError.message());
                    ++errNum;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return errNum <= 0;
        }


        @Override
        public boolean delete(File file) {
            if (file == null) {
                log.error("删除文件为空！");
                return false;
            }
            return deleteFile(file.getRelativePath());
        }

        @Override
        public boolean delete(List<File> listFile) {
            if (CollectionUtils.isEmpty(listFile)) {
                log.error("删除文件集合为空！");
                return false;
            }
            int errNum = listFile.size();
            List<DeleteObject> deleteObjectList = new CopyOnWriteArrayList<>();
            for (File file : listFile) {
                deleteObjectList.add(new DeleteObject(file.getRelativePath()));
            }
            errNum = 0;
            try {
                final Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(properties.getBucketName()).objects(deleteObjectList).build());
                for (Result<DeleteError> result : results) {
                    final DeleteError deleteError = result.get();
                    log.error("删除文件{}时发生错误！", deleteError.objectName());
                    log.error(deleteError.message());
                    ++errNum;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return errNum <= 0;
        }


        private boolean deleteFile(String fileName) {
            int errNum = 1;
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(properties.getBucketName()).object(fileName).build());
                --errNum;
            } catch (Exception e) {
                log.error("删除文件失败");
                e.printStackTrace();
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
            GetPresignedObjectUrlArgs build = GetPresignedObjectUrlArgs.builder().bucket(properties.getBucketName()).object(fileName).method(Method.GET).build();
            try {
                return minioClient.getPresignedObjectUrl(build);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

        /**
         * 获取文件流
         * @param fileName 文件名称
         * @return 文件流
         */
        @Override
        public InputStream getFileInputStream(String fileName) {
            try {
                return minioClient.getObject(GetObjectArgs.builder().bucket(properties.getBucketName()).object(fileName).build());
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
                this.minioClient.statObject(StatObjectArgs.builder().bucket(properties.getBucketName()).object(fileName).build());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }


        public void existBucket(String name) {
            try {
                boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(name).build());
                if (!exists) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(name).build());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public Boolean makeBucket(String bucketName) {
            try {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public Boolean removeBucket(String bucketName) {
            try {
                minioClient.removeBucket(RemoveBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
    }
}
