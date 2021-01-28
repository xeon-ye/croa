package com.qinfei.qferp.entity.sys;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;
import java.util.Date;

@Table(name = "auto_number")
public class AutoNumber implements Serializable {
    @Id
    private Integer id;
    private String code ;
    private Integer year ;
    private Integer month ;
    private Integer day ;
    private Integer value ;
    private Integer state ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "AutoNumber{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", value=" + value +
                ", state=" + state +
                '}';
    }
}
