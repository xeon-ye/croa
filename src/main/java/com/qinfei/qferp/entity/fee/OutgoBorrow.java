package com.qinfei.qferp.entity.fee;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import java.io.Serializable;

@Table(name = "fee_outgo_borrow")
public class OutgoBorrow implements Serializable {
    @Id
    private Integer id;
    private Integer outgoId ;
    private Integer borrowId ;
    private Double amount ;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOutgoId() {
        return outgoId;
    }

    public void setOutgoId(Integer outgoId) {
        this.outgoId = outgoId;
    }

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "OutgoBorrow{" +
                "id=" + id +
                ", outgoId=" + outgoId +
                ", borrowId=" + borrowId +
                ", amount=" + amount +
                '}';
    }
}
