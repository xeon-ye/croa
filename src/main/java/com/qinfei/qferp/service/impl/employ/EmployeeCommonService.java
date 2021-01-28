package com.qinfei.qferp.service.impl.employ;

import com.qinfei.qferp.entity.employ.EmployeeCommon;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IEmployeeRecord;

/**
 * 部分公用属性的设置；
 * 
 * @Author ：Yuan；
 * @Date ：2019/4/19 0019 16:13；
 */
class EmployeeCommonService {
	/**
	 * 根据流程的状态设置对应的数据状态；
	 * 
	 * @param employeeCommon：数据对象；
	 */
    void updateState(EmployeeCommon employeeCommon) {
		Integer state = employeeCommon.getState();
		if (state != null) {
			employeeCommon.setUpdateInfo();
			if (state == IConst.STATE_FINISH) {
				employeeCommon.setState(IEmployeeRecord.STATE_CONNECT_READY);
			} else if (state == IConst.STATE_REJECT) {
				employeeCommon.setState(IEmployeeRecord.STATE_REJECT);
			} else {
				employeeCommon.setState(IEmployeeRecord.STATE_APPROVE);
			}
		}
	}

	/**
	 * 根据交接流程的状态设置对应的数据状态；
	 *
	 * @param employeeCommon：数据对象；
	 */
    void updateConnectState(EmployeeCommon employeeCommon) {
		Integer state = employeeCommon.getState();
		if (state != null) {
			employeeCommon.setUpdateInfo();
			if (state == IConst.STATE_FINISH) {
				employeeCommon.setState(IEmployeeRecord.STATE_FINISH);
			} else if (state == IConst.STATE_REJECT) {
				employeeCommon.setState(IEmployeeRecord.STATE_CONNECT_READY);
			} else {
				employeeCommon.setState(IEmployeeRecord.STATE_CONNECT);
			}
		}
	}
}