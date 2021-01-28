package com.qinfei.qferp.enumUtils;

/**
 * 附件类型的枚举类
 */
public enum FilesEnum {
    ARTICLEDIFFILES();

    private final Integer type;
    private final String name;

    FilesEnum()
    {
        this.type = 1;
        this.name = "稿件差价";
    }

    public Integer getType() {
        return type;
    }

    public String getName(){
        return name;
    }
}
