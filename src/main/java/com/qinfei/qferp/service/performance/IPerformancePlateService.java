package com.qinfei.qferp.service.performance;

import java.util.List;

import com.qinfei.qferp.entity.performance.PerformancePlate;
import com.qinfei.qferp.service.dto.PlateOutDto;

/**
 * 考核细则的业务接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/10 0028 14:22；
 */
public interface IPerformancePlateService {
	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工基础信息对象；
	 * @return ：处理完毕的员工基础信息对象；
	 */
	PerformancePlate saveOrUpdate(PerformancePlate record);

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param plateId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	int deletePlate(int plateId);

	/**
	 * 查询指定ID下权重分数统计；
	 *
	 * @param plateId：主键ID；
	 * @param plateParent：父级ID；
	 * @return ：分数统计；
	 */
	float selectTotalByParentId(Integer plateId, int plateParent);

	/**
	 * 根据上级ID查询下属的考核细则；
	 *
	 * @param plateParent：上级ID；
	 * @param plateLevel：考核细则层级；
	 * @param plateContent：考核细则内容；
	 * @return ：考核细则集合；
	 */
	List<PerformancePlate> listPlateByParentId(Integer plateId, Integer plateParent, Integer plateLevel, String plateContent);

	/**
	 * 查询指定节点的数据树；
	 * 
	 * @param plateParent：节点ID；
	 * @return ：树结构数据集合；
	 */
	List<PlateOutDto> listChildByParentId(int plateParent);

	/**
	 * 查询指定ID的细则数据；
	 * 
	 * @param plateId：主键ID；
	 * @return ：细则对象；
	 */
	PerformancePlate selectByPrimaryKey(int plateId);

	/**
	 * 查询有完整结构的节点数据；
	 * 
	 * @param plateLevel：节点层级；
	 * @return ：考核细则数据集合；
	 */
	List<PerformancePlate> listCompletePlates(int plateLevel);
}