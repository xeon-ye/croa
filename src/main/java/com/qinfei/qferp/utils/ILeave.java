package com.qinfei.qferp.utils;

public interface ILeave {
    // 待审核；Leave
    int LEAVE_PENDING = 0;
    // 审核中；
    int LEAVE_APPROVE = 1;
    // 同意；
    int LEAVE_AGREE = 2;
    // 已删除；
    int LEAVE_DELETE = 3;
    // 拒绝；
    int LEAVE_REFUSE = -1;
    //已完成
    int LEAVE_FINISH = 4;
}
