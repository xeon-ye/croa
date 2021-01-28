package com.qinfei.qferp.service.impl.performance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qinfei.qferp.entity.performance.PerformanceProgram;
import com.qinfei.qferp.mapper.performance.PerformanceProgramMapper;
import com.qinfei.qferp.service.performance.IPerformanceProgramService;

/**
 * 绩效考核计划关联的考核方案业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/16 0028 22:03；
 */
@Service
public class PerformanceProgramService implements IPerformanceProgramService {
	// 绩效考核计划的数据库执行接口；
	@Autowired
	private PerformanceProgramMapper programMapper;

	/**
	 * 查询考核计划关联的考核方案关系映射；
	 *
	 * @param proId：考核计划ID；
	 * @return ：查询结果集合；
	 */
	@Override
	public List<PerformanceProgram> selectByProId(int proId) {
		return programMapper.selectByProId(proId);
	}
}