package com.qinfei.qferp.entity.media;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "t_media_name")
public class MediaName implements Serializable {
    @Id
    @Column(name = "id")
    private Integer id;

    private String name;

    private Boolean state;

    private Date createDate;

    private Integer mediaTypeId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    @Override
    public String toString() {
        return "MediaName{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", createDate=" + createDate +
                ", mediaTypeId=" + mediaTypeId +
                '}';
    }
}