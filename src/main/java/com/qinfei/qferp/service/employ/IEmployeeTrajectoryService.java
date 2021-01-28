package com.qinfei.qferp.service.employ;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;

import com.qinfei.qferp.entity.employ.EmployeeCommon;
import com.qinfei.qferp.entity.employ.EmployeeTrajectory;
import com.github.pagehelper.PageInfo;

/**
 * 员工轨迹信息业务接口；
 * 
 * @Author ：Yuan；
 * @Date ：2019/3/22 0022 10:26；
 */
public interface IEmployeeTrajectoryService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工轨迹对象；
	 * @return ：处理完毕的员工轨迹对象；
	 */
	EmployeeTrajectory saveOrUpdate(EmployeeTrajectory record);

	/**
	 * 批量更新员工轨迹关联的员工ID和姓名；
	 *
	 * @param params：查询参数和更新内容；
	 * @return ：操作影响的记录数；
	 */
	void updateByIds(Map<String, Object> params);

	/**
	 * 从员工公用记录对象中获取员工轨迹数据；
	 *
	 * @param record：员工公用记录对象；
	 * @return ：员工轨迹对象；
	 */
	EmployeeTrajectory getTrajectory(EmployeeCommon record);

	/**
	 * 录用流程发起时，员工没有关联的数据，因此保存的是入职申请ID，正式入库后则更新为员工ID，并将员工编号插入进来；
	 *
	 * @param entryId：入职申请ID；
	 * @return ：主键ID集合；
	 */
	List<Map<String, Integer>> selectIdsByParentId(int entryId);

	/**
	 * 分页查询员工轨迹信息；
	 *
	 * @param params：查询参数；
	 * @param pageable：分页参数；
	 * @return ：查询的员工轨迹信息集合；
	 */
	PageInfo<Map<String, Object>> selectPageTrajectory(Map<String, Object> params, Pageable pageable);
}