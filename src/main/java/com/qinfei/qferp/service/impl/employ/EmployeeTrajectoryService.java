package com.qinfei.qferp.service.impl.employ;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.employ.EmployeeCommon;
import com.qinfei.qferp.entity.employ.EmployeeTrajectory;
import com.qinfei.qferp.mapper.employ.EmployeeTrajectoryMapper;
import com.qinfei.qferp.service.employ.IEmployeeTrajectoryService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.EntityUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * 员工轨迹信息业务接口实现类；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:38；
 */
@Service
public class EmployeeTrajectoryService implements IEmployeeTrajectoryService {
	// 数据库操作接口；
	@Autowired
	private EmployeeTrajectoryMapper trajectoryMapper;

	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工轨迹对象；
	 * @return ：处理完毕的员工轨迹对象；
	 */
	@Override
	public EmployeeTrajectory saveOrUpdate(EmployeeTrajectory record) {
		Integer trajId = record.getTrajId();
		if (trajId == null) {
			record.setCreateInfo();
			trajectoryMapper.insertSelective(record);
		} else {
			record.setUpdateInfo();
			trajectoryMapper.updateByPrimaryKeySelective(record);
		}
		return record;
	}

	/**
	 * 批量更新员工轨迹关联的员工ID和姓名；
	 *
	 * @param params：查询参数和更新内容；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public void updateByIds(Map<String, Object> params) {
		EntityUtil.setUpdateInfo(params);
		trajectoryMapper.updateByIds(params);
	}

	/**
	 * 从员工公用记录对象中获取员工轨迹数据；
	 *
	 * @param record：员工公用记录对象；
	 * @return ：员工轨迹对象；
	 */
	@Override
	public EmployeeTrajectory getTrajectory(EmployeeCommon record) {
		EmployeeTrajectory trajectory = new EmployeeTrajectory();
		// 防止出现空指针（数据被删除）；
		if (record != null) {
			trajectory.setEmpId(record.getEmpId());
			trajectory.setEmpNum(record.getEmpNum());
			trajectory.setEmpName(record.getEmpName());
			trajectory.setEmpProfession(record.getEmpProfession());
			trajectory.setEmpProfessionName(record.getEmpProfessionName());
			trajectory.setEmpDept(record.getEmpDept());
			trajectory.setEmpDeptName(record.getEmpDeptName());
		}
		return trajectory;
	}

	/**
	 * 录用流程发起时，员工没有关联的数据，因此保存的是入职申请ID，正式入库后则更新为员工ID，并将员工编号插入进来；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：主键ID集合；
	 */
	@Override
	public List<Map<String, Integer>> selectIdsByParentId(int entryId) {
		return trajectoryMapper.selectIdsByParentId(entryId);
	}

	/**
	 * 分页查询员工轨迹信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工轨迹信息集合；
	 */
	@Override
	public PageInfo<Map<String, Object>> selectPageTrajectory(Map<String, Object> params, Pageable pageable) {
		// 公司代码过滤；
		params.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
		PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        return new PageInfo<>(trajectoryMapper.selectPageTrajectory(params));
	}
}