package com.qinfei.qferp.entity.media;

import com.qinfei.core.annotation.Column;
import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;

@Table(name = "t_media_industry")
public class Industry implements Serializable {
    @Id
    @Column(name = "id")
    private Integer id;

    private String name;

    private Integer mediaTypeId;

    private Boolean state;

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

    public Integer getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Integer mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Industry{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mediaTypeId=" + mediaTypeId +
                ", state=" + state +
                '}';
    }
}