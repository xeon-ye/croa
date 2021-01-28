package com.qinfei.qferp.service.impl.performance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.performance.PerformanceDetail;
import com.qinfei.qferp.mapper.performance.PerformanceDetailMapper;
import com.qinfei.qferp.service.performance.IPerformanceDetailService;
import com.qinfei.qferp.utils.IPerformancePlate;

/**
 * 绩效考核的评分明细接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/04/15 0028 17:23；
 */
@Service
public class PerformanceDetailService implements IPerformanceDetailService {
	// 数据执行业务接口；
	@Autowired
	private PerformanceDetailMapper detailMapper;

	/**
	 * 批量新增或更新相关的评分明细；
	 *
	 * @param details：评分明细；
	 */
	@Override
	@Transactional
	public void saveOrUpdate(List<PerformanceDetail> details) {
		// 获取第一条数据的主键来判断是新增还是更新；
		PerformanceDetail detail = details.get(0);
		Integer detailId = detail.getDetailId();
		if (detailId == null) {
			detailMapper.insertBatch(details);
		} else {
			// 更新则仅更新有分值的数据；
			for (PerformanceDetail data : details) {
				// 分值存储在二级评分细则上；
//				if (data.getPlateLevel().intValue() == IPerformancePlate.PROGRAM) {
					data.setUpdateInfo();
					detailMapper.updateScoreData(data);
//				}
			}
		}
	}

	/**
	 * 根据主表ID获取关联的评分明细；
	 *
	 * @param scoreId：评分ID；
	 * @return ：评分明细集合；
	 */
	@Override
	public List<PerformanceDetail> getDetailInfo(int scoreId) {
		return detailMapper.selectByParentId(scoreId);
	}
}