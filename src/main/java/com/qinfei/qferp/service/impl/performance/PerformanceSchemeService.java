package com.qinfei.qferp.service.impl.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.performance.PerformanceHistory;
import com.qinfei.qferp.entity.performance.PerformanceScheme;
import com.qinfei.qferp.mapper.performance.PerformanceHistoryMapper;
import com.qinfei.qferp.mapper.performance.PerformanceSchemeMapper;
import com.qinfei.qferp.service.performance.IPerformanceSchemeService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@Service
public class PerformanceSchemeService implements IPerformanceSchemeService {
    @Autowired
    private PerformanceSchemeMapper performanceSchemeMapper;
    @Autowired
    private PerformanceHistoryMapper performanceHistoryMapper;

    @Override
    @Transactional
    public PerformanceScheme save(PerformanceScheme scheme) {
        boolean saveFlag = Objects.isNull(scheme.getSchId());

        String schCode = "JXFA" + DateUtils.getStr();

        // 考核小组
        String groupIds = StringUtils.join(scheme.getGroupIdsLs(), ",");
        String groupNames = StringUtils.join(scheme.getGroupNamesLs(), ",");
        // 排除考核用户id
        String schUserIds = StringUtils.join(scheme.getSchUserIdLs(), ",");
        String schUserNames = StringUtils.join(scheme.getSchUserNameLs(), ",");

        List<String> content = scheme.getPlateContent();
        List<String> target = scheme.getPlateTarget();
        List<Integer> ids = scheme.getPlateId();
        List<Float> proportion = scheme.getPlateProportion();
        List<Integer> level = scheme.getPlateLevel();
        List<Integer> parent = scheme.getPlateParent();
        List<Integer> orders = scheme.getPlateOrder();
        List<String> demand = scheme.getPlateDemand();

        scheme.setSchCode(schCode);
        scheme.setGroupIds(groupIds);
        scheme.setGroupNames(groupNames);
        scheme.setSchUserId(schUserIds);
        scheme.setSchUserName(schUserNames);
        if (saveFlag) {
            scheme.setCreateInfo();
            // 部门ID为登录人的部门信息；
            scheme.setDeptId(AppUtil.getUser().getDeptId());
            scheme.setCompanyCode(AppUtil.getUser().getCompanyCode());
            performanceSchemeMapper.insertSelective(scheme);
        } else {
            scheme.setUpdateInfo();
            // 部门ID不允许修改；
            scheme.setDeptId(null);
            performanceSchemeMapper.updateByPrimaryKeySelective(scheme);
        }

        performanceHistoryMapper.deleteBySchId(scheme.getSchId());
        // 插入考核项目
        IntStream.range(0, scheme.getPlateContent().size()).forEach(i -> {
            PerformanceHistory history = new PerformanceHistory();
            history.setPlateContent(content.get(i));
            if(CollectionUtils.isNotEmpty(target)){
                history.setPlateTarget(target.get(i));
            }
            if(CollectionUtils.isNotEmpty(demand)){
                history.setPlateDemand(demand.get(i));
            }
            history.setPlateId(ids.get(i));
            history.setPlateLevel(level.get(i));
            history.setPlateProportion(proportion.get(i));
            history.setPlateParent(parent.get(i));
            history.setPlateOrder(orders.get(i));
            history.setSchId(scheme.getSchId());
            history.setCreateInfo();
            history.setDeptId(AppUtil.getUser().getDeptId());
            performanceHistoryMapper.insertSelective(history);
        });

        return scheme;
    }

    @Override
    public PageInfo<PerformanceScheme> listPg(Pageable pageable, Map map) {
        map.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        //方案启用状态
        map.put("schUsed", 0);
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<PerformanceScheme> ls = performanceSchemeMapper.listPg(map);
        return new PageInfo<>(ls);
    }

    @Override
    public PerformanceScheme selectById(Integer id) {
        List<PerformanceHistory> ls = performanceHistoryMapper.selectBySchId(id);
        PerformanceScheme scheme = performanceSchemeMapper.selectByPrimaryKey(id);
        scheme.setHistoryList(ls);
        return scheme;
    }

    @Override
    public List<Map> listUserByParam(Map map) {
        return performanceSchemeMapper.listUserByParam(map);
    }

    /**
     * 根据主键获取相关的考核方案信息；
     *
     * @param schemeId：主键ID；
     * @return ：考核方案数据；
     */
    @Override
    public PerformanceScheme getScheme(int schemeId) {
        return performanceSchemeMapper.selectByPrimaryKey(schemeId);
    }

    /**
     * 获取考核方案关联的考核细则；
     *
     * @param schemeId：考核方案ID；
     * @return ：考核细则集合；
     */
    @Override
    public List<PerformanceHistory> getSchemeHistory(int schemeId) {
        return performanceHistoryMapper.selectBySchId(schemeId);
    }

    @Override
    public List<PerformanceScheme> postInfo(Integer schemeType) {
        // 查询已录入系统的方案关联的部门信息
        Map<String, Object> map = new HashMap<>();
        map.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
        map.put("schemeType", schemeType);
        return performanceSchemeMapper.selectAll(map);
    }

    @Override
    @Transactional
    public void copy(Integer schId) {
        PerformanceScheme scheme = performanceSchemeMapper.selectByPrimaryKey(schId);
        String schCode = "JXFA" + DateUtils.getStr();
        scheme.setSchId(null);
        scheme.setCreateInfo();
        scheme.setSchCode(schCode);
        scheme.setCompanyCode(AppUtil.getUser().getCompanyCode());
        performanceSchemeMapper.insertSelective(scheme);

        List<PerformanceHistory> historyList = performanceHistoryMapper.selectBySchId(schId);
        historyList.forEach(history -> {
            history.setHistoryId(null);
            history.setSchId(scheme.getSchId());
            history.setCreateInfo();
            performanceHistoryMapper.insertSelective(history);
        });
    }
}
