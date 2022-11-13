package com.jie.file.properties;

import com.jie.file.enumeration.FileStorageType;
import com.jie.file.utils.StrPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件策略配置属性类
 *
 * @author jie
 */
@Data
@ConfigurationProperties(prefix = "jie.file")
public class FileServerProperties {

    /**
     * 存储策略
     */
    private FileStorageType type = FileStorageType.LOCAL;

    /**
     * 文件访问前缀
     */
    private String uriPrefix = "";
    /**
     * 内网通道前缀 主要用于解决某些服务器的无法访问外网ip的问题
     */
    private String innerUriPrefix = "";

    public String getInnerUriPrefix() {
        return innerUriPrefix;
    }

    public String getUriPrefix() {
        if (!uriPrefix.endsWith(StrPool.SLASH)) {
            uriPrefix += StrPool.SLASH;
        }
        return uriPrefix;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    /**
     * 指定分片上传时临时存放目录
     */
    private String storagePath;

    private Properties local;
    private Properties ali;
    private Properties minio;
    private Properties qiniu;
    private Properties tencent;

    @Data
    public static class Properties {
        /**
         * uri前缀（ip:端口）
         */
        private String uriPrefix;
        /**
         * 上传位置（ip:端口），本地策略是盘符目录地址（D:\）
         */
        private String endpoint;
        /**
         * 账号
         */
        private String accessKey;
        /**
         * 密码
         */
        private String secretKey;
        /**
         * 项目目录名称
         */
        private String bucketName;

        public String getUriPrefix() {
            if (!uriPrefix.endsWith(StrPool.BACK_SLASH)) {
                uriPrefix += StrPool.BACK_SLASH;
            }
            return uriPrefix;
        }

        public String getEndpoint() {
            if (!endpoint.endsWith(StrPool.SLASH)) {
                endpoint += StrPool.SLASH;
            }
            return endpoint;
        }
    }
}