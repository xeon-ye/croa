package com.qinfei.qferp.entity.fee;

import com.qinfei.core.annotation.Id;
import com.qinfei.core.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Table(name = "fee_reimbursement_borrow")
@Setter
@Getter
@ToString
public class ReimbursementBorrow implements Serializable {
    @Id
    private Integer id;
    private Integer remId ;
    private Integer borrowId ;
    private Double amount ;

}
