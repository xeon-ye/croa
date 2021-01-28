package com.qinfei.qferp.service.impl.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.performance.PerformancePlate;
import com.qinfei.qferp.mapper.performance.PerformancePlateMapper;
import com.qinfei.qferp.service.dto.PlateOutDto;
import com.qinfei.qferp.service.performance.IPerformancePlateService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IEmployData;
import com.qinfei.qferp.utils.IPerformancePlate;

/**
 * 考核细则的业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/10 0028 14:25；
 */
@Service
public class PerformancePlateService implements IPerformancePlateService {
	// 数据库操作对象；
	@Autowired
	private PerformancePlateMapper performancePlateMapper;

	/**
	 * 保存或更新单条记录到数据库中；
	 *
	 * @param record：员工基础信息对象；
	 * @return ：处理完毕的员工基础信息对象；
	 */
	@Override
	@Transactional
	public PerformancePlate saveOrUpdate(PerformancePlate record) {
		// 如果没有类型传递，默认为板块；
		Integer plateLevel = record.getPlateLevel();
		if (plateLevel == null || plateLevel <= IPerformancePlate.PLATE) {
			plateLevel = IPerformancePlate.PLATE;
		} else if (plateLevel >= IPerformancePlate.DETAIL) {
			plateLevel = IPerformancePlate.DETAIL;
		}
		record.setPlateLevel(plateLevel);

		// 如果选择的类型不是板块且没有上级ID传递，设置类型为板块；
		Integer plateParent = record.getPlateParent();
		int intValue = plateLevel.intValue();
		if (intValue != IPerformancePlate.PLATE && plateParent == null) {
			plateLevel = IPerformancePlate.PLATE;
			record.setPlateLevel(plateLevel);
		}

		// 上级为空的话设置为0，方便清空；
		if (plateParent == null) {
			plateParent = 0;
			record.setPlateParent(plateParent);
		}
		Integer plateId = record.getPlateId();
		// 没有主键为新增操作；
		if (plateId == null) {
			// 设置创建人信息；
			record.setCreateInfo();
			record.setState(IEmployData.DATA_NORMAL);
			// 部门ID为登录人的部门信息；
			record.setDeptId(AppUtil.getUser().getDeptId());
			performancePlateMapper.insertSelective(record);
		} else {
			// 获取父级ID；
			int parentValue = plateParent.intValue();
			// 校验权重；
			Float plateProportion = record.getPlateProportion();
			// 校验父级的权重；
			// 板块；
			if (plateLevel == IPerformancePlate.PLATE && parentValue == 0) {
				// 获取该板块下的所有项目分值；
				PerformancePlate parentPlate = new PerformancePlate();
				parentPlate.setPlateParent(plateId);
				float totalScore = performancePlateMapper.selectTotalByParentId(parentPlate);

				if (plateProportion == null || totalScore > plateProportion.floatValue()) {
					record.setPlateProportion(totalScore);
				}
			}

			// 项目；
			if (plateLevel == IPerformancePlate.PROGRAM && parentValue != 0) {
				// 获取该板块下的所有项目分值；
				float totalScore = performancePlateMapper.selectTotalByParentId(record);
				totalScore += plateProportion == null ? 0.0f : plateProportion.floatValue();

				// 获取上级的权重信息；
				float parentProportion = performancePlateMapper.selectScoreById(plateParent);

				// 如果总计分值已超过板块的分值，更新父级的分值为累加后的分值；
				if (totalScore > parentProportion) {
					PerformancePlate parentPlate = new PerformancePlate();
					parentPlate.setPlateId(plateParent);
					parentPlate.setPlateProportion(totalScore);
					parentPlate.setUpdateInfo();
					performancePlateMapper.updateByPrimaryKeySelective(parentPlate);
				}
			}

			// 更新下级的级别；
			PerformancePlate plate = new PerformancePlate();
			plate.setPlateId(plateId);
			// 不能超过最低级；
			if (plateLevel != IPerformancePlate.DETAIL) {
				// plate.setPlateParent(plateParent);
				plateLevel += 1;
			}
			plate.setPlateLevel(plateLevel);
			plate.setUpdateInfo();
			performancePlateMapper.updateByParentId(plate);

			record.setUpdateInfo();
			// 部门ID不允许修改；
			record.setDeptId(null);
			performancePlateMapper.updateByPrimaryKeySelective(record);
		}
		return record;
	}

	/**
	 * 根据主键删除单条记录；
	 *
	 * @param plateId：主键ID；
	 * @return ：操作影响的记录数；
	 */
	@Override
	public int deletePlate(int plateId) {
		PerformancePlate plate = new PerformancePlate();
		plate.setPlateId(plateId);
		plate.setUpdateInfo();
		return performancePlateMapper.deleteByPrimaryKey(plate);
	}

	/**
	 * 查询指定ID下权重分数统计；
	 *
	 * @param plateId：主键ID；
	 * @param plateParent：父级ID；
	 * @return ：分数统计；
	 */
	@Override
	public float selectTotalByParentId(Integer plateId, int plateParent) {
		PerformancePlate record = new PerformancePlate();
		record.setPlateId(plateId);
		record.setPlateParent(plateParent);
		return performancePlateMapper.selectScoreById(plateParent) - performancePlateMapper.selectTotalByParentId(record);
	}

	/**
	 * 根据上级ID查询下属的考核细则；
	 *
	 * @param plateId：主键ID；
	 * @param plateParent：上级ID；
	 * @param plateLevel：考核细则层级；
	 * @param plateContent：考核细则内容；
	 * @return ：考核细则集合；
	 */
	@Override
	public List<PerformancePlate> listPlateByParentId(Integer plateId, Integer plateParent, Integer plateLevel, String plateContent) {
		PerformancePlate record = new PerformancePlate();
		record.setPlateId(plateId);
		record.setPlateParent(plateParent);
		record.setPlateLevel(plateLevel);
		record.setPlateContent(plateContent);
		// 如果没有上级ID则需要添加公司代码过滤；
		if (plateParent == null) {
			// 此处用做存储公司代码；
			record.setCreateName(AppUtil.getUser().getDept().getCompanyCode());
		}
		return performancePlateMapper.listPlateByParentId(record);
	}

	/**
	 * 查询指定节点的数据树；
	 *
	 * @param plateParent：节点ID；
	 * @return ：树结构数据集合；
	 */
	@Override
	public List<PlateOutDto> listChildByParentId(int plateParent) {
		PerformancePlate record = new PerformancePlate();
		if (Objects.isNull(plateParent))
			return null;
		record.setPlateParent(plateParent);

		return performancePlateMapper.listPlateByParentId(record).stream().map(e -> {
			PlateOutDto plateOutDto = new PlateOutDto();
			plateOutDto.setPlate(e);

			PerformancePlate r = new PerformancePlate();
			r.setPlateParent(e.getPlateId());
			List<PerformancePlate> ls = performancePlateMapper.listPlateByParentId(r);
			plateOutDto.setChilds(ls);
			return plateOutDto;
		}).collect(Collectors.toList());
	}

	/**
	 * 查询指定ID的细则数据；
	 *
	 * @param plateId：主键ID；
	 * @return ：细则对象；
	 */
	@Override
	public PerformancePlate selectByPrimaryKey(int plateId) {
		return performancePlateMapper.selectByPrimaryKey(plateId);
	}

	/**
	 * 查询有完整结构的节点数据；
	 *
	 * @param plateLevel：节点层级；
	 * @return ：考核细则数据集合；
	 */
	@Override
	public List<PerformancePlate> listCompletePlates(int plateLevel) {
		PerformancePlate record = new PerformancePlate();
		record.setPlateLevel(plateLevel);
		List<PerformancePlate> performancePlates = performancePlateMapper.listPlateByParentId(record);
		List<PerformancePlate> result = new ArrayList<>();
		for (PerformancePlate plate : performancePlates) {
			boolean hasLv3 = true;

			// 是否有第二层节点
			Integer plateParent = plate.getPlateId();
			if (performancePlateMapper.countByParentId(plateParent) == 0)
				continue;

			// 判断是否有第三层节点
			PerformancePlate recordLv2 = new PerformancePlate();
			recordLv2.setPlateParent(plateParent);
			for (PerformancePlate plateLv3 : performancePlateMapper.listPlateByParentId(recordLv2)) {
				if (performancePlateMapper.countByParentId(plateLv3.getPlateId()) == 0) {
					hasLv3 = false;
					break;
				}
			}
			if (hasLv3)
				result.add(plate);
		}
		return result;
	}
}