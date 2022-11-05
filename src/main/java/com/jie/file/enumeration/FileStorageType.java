package com.jie.file.enumeration;

/**
 * 存储策略
 * @author jie
 */
public enum FileStorageType {
    /**
     * 本地存储
     */
    LOCAL,

    /**
     * 阿里云存储
     */
    ALI_OSS,

    /**
     * minio存储
     */
    MINIO,

    QI_NIU,

    TENCENT
}