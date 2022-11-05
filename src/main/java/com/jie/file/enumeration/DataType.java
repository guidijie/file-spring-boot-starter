package com.jie.file.enumeration;

public enum DataType  {

    /**
     * DIR="目录"
     */
    DIR("目录"),
    /**
     * IMAGE="图片"
     */
    IMAGE("图片"),
    /**
     * VIDEO="视频"
     */
    VIDEO("视频"),
    /**
     * AUDIO="音频"
     */
    AUDIO("音频"),
    /**
     * DOC="文档"
     */
    DOC("文档"),
    /**
     * OTHER="其他"
     */
    OTHER("其他");

    private String desc;

    DataType(String type) {
        this.desc = type;
    }

}

