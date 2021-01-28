package com.qinfei.core.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qinfei.core.annotation.Relate;
import com.qinfei.qferp.entity.sys.User;

import com.qinfei.core.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;

/**
 * Created by Gzw
 *
 * @author GZW
 */
@Table(name = "t_log", schema = "qferp")
@Data
public class Log implements Serializable {
    @Id
    @Column(name = "id", columnDefinition = "bigint")
    private long id;
    @Column(name = "user_id")
    private Integer userId;
    @Relate(name = User.class, fkName = "user_id")
    @JSONField(serialzeFeatures = {SerializerFeature.DisableCircularReferenceDetect})
    @Transient
    private User user;
    @Column(name = "op_date")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date opDate;
    @Column(name = "ip")
    private String ip;
    @Column(name = "module")
    private String module;
    @Column(name = "op_type")
    private String opType;
    @Column(name = "note")
    private String note;
    @Column(name = "class_name")
    private String className;
    @Column(name = "method_name")
    private String methodName;
    @Column(name = "args", columnDefinition = "text")
    private String args;
    @Column(name = "url")
    private String url;
    @Column(name = "mac")
    private String mac;
    @Column(name = "ret_val")
    private String retVal;


}
