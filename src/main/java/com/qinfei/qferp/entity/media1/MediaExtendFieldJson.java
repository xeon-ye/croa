package com.qinfei.qferp.entity.media1;

import java.io.Serializable;

/**
 * @CalssName MediaExtendFieldJson
 * @Description 媒体扩展字段中JSON转换对象
 * @Author xuxiong
 * @Date 2019/7/4 0004 14:52
 * @Version 1.0
 */
public class MediaExtendFieldJson implements Serializable {
    private String text; //页面显示的字符
    private String value; //实际值
    private boolean isDefault; //是否默认选中

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        return "MediaExtendFieldJson{" +
                "text='" + text + '\'' +
                ", value='" + value + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
}
