package com.jie.file.strategy;

import com.jie.file.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

/**
 * 文件上传顶级接口
 *
 * @author jie
 */
public interface FileStrategy {

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @return 文件对象
     * @throws Exception 异常
     */
    File upload(MultipartFile file) throws Exception;


    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param dir  上传的目录
     * @return 文件对象
     * @throws Exception 异常
     */
    File upload(MultipartFile file, String dir) throws Exception;

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的单个文件
     * @return 是否删除成功
     * @throws Exception 异常
     */
    boolean delete(String fileName) throws Exception;

    /**
     * 删除对个文件
     *
     * @param fileNames 要删除的文件集合
     * @return 是否删除成功
     * @throws Exception 异常
     */
    boolean delete(String... fileNames) throws Exception;


    /**
     * 删除单个文件
     *
     * @param file 要删除的单个文件
     * @return 是否删除成功
     * @throws Exception 异常
     */
    boolean delete(File file) throws Exception;

    /**
     * 文件删除
     *
     * @param listFile 要删除的文件集合
     * @return 删除是否成功
     * @throws Exception 异常
     */
    boolean delete(List<File> listFile) throws Exception;

    /**
     * 获取文件访问路径
     *
     * @param fileName 文件名称
     * @return 文件访问路径
     * @throws Exception 异常
     */
    String getFileUrl(String fileName) throws Exception;


    /**
     * 获取文件流
     * @param fileName 文件名称
     * @return 文件流
     */
    InputStream getFileInputStream(String fileName);

    /**
     * 判断文件是否存在
     * @param fileName 文件名称
     * @return 文件是否存在
     */
    boolean fileExists(String fileName);
}
