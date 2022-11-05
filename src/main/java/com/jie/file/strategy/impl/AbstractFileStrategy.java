package com.jie.file.strategy.impl;

import com.jie.file.entity.File;
import com.jie.file.enumeration.IconType;
import com.jie.file.properties.FileServerProperties;
import com.jie.file.strategy.FileStrategy;
import com.jie.file.utils.DateUtils;
import com.jie.file.utils.FileDataTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author jie
 */
@Slf4j
public abstract class AbstractFileStrategy implements FileStrategy {

    @Autowired
    protected FileServerProperties fileProperties;

    protected FileServerProperties.Properties properties;

    protected final static String FILE_SPLIT = ".";


    @Override
    public File upload(MultipartFile multipartFile) throws Exception {
        return upload(multipartFile, null);
    }

    @Override
    public File upload(MultipartFile multipartFile, String dir) throws Exception {

        try {
            //获得上传文件的原始文件名称
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename != null && !originalFilename.contains(FILE_SPLIT)) {
                //文件名称中没有.，这是非法的，直接抛出异常
                throw new Exception("上传文件名称缺少后缀");
            }

            // 封装一个File对象，在完成文件上传后需要将上传的文件信息保存到数据库
            File file = File.builder()
                    // 文件是否被删除
                    .isDelete(false)
                    // 文件大小
                    .size(multipartFile.getSize())
                    // 文件类型
                    .contextType(multipartFile.getContentType())
                    // 数据类型：
                    .dataType(FileDataTypeUtil.getDataType(multipartFile.getContentType()))
                    // 原始文件名称
                    .submittedFileName(multipartFile.getOriginalFilename())
                    // 后缀
                    .ext(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))
                    .build();

            // 设置文件的图标
            file.setIcon(IconType.getIcon(file.getExt()).getIcon());

            LocalDateTime now = LocalDateTime.now();

            // 设置文件创建时间
            file.setDir(dir);
            file.setCreateMonth(DateUtils.formatAsYearMonthEn(now));
            file.setCreateWeek(DateUtils.formatAsYearWeekEn(now));
            file.setCreateDay(DateUtils.formatAsDateEn(now));

            uploadFile(file, multipartFile);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("文件上传失败");
        }
    }

    /**
     * 文件上传,需要子类实现
     *
     * @param file          文件上传实体
     * @param multipartFile 上传的文件
     */
    public abstract void uploadFile(File file, MultipartFile multipartFile) throws IOException;

    /**
     * 获取下载地址前缀
     */
    protected String getUriPrefix() {
        if (properties.getUriPrefix() != null && !"".equals(properties.getUriPrefix())) {
            return properties.getUriPrefix();
        } else {
            return properties.getEndpoint();
        }
    }
}
