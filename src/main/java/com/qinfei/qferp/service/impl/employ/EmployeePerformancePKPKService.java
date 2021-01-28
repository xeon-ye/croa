package com.qinfei.qferp.service.impl.employ;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.exception.ResultEnum;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.core.utils.SpringUtils;
import com.qinfei.qferp.entity.employ.EmployeePerformancePKEmployeeRelate;
import com.qinfei.qferp.entity.employ.EmployeePerformancePk;
import com.qinfei.qferp.mapper.biz.ArticleMapperXML;
import com.qinfei.qferp.mapper.employ.EmployeePerformancePkEmployeeRelateMapper;
import com.qinfei.qferp.mapper.employ.EmployeePerformancePkMapper;
import com.qinfei.qferp.mapper.sys.UserMapper;
import com.qinfei.qferp.service.dto.PerformancePKProfitDto;
import com.qinfei.qferp.service.employ.IEmployeePerformancePKService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yanhonghao on 2019/4/23 17:27.
 */
@Service
public class EmployeePerformancePKPKService implements IEmployeePerformancePKService {

    private final EmployeePerformancePkMapper employeePerformancePkMapper;
    private final EmployeePerformancePkEmployeeRelateMapper relateMapper;
    private final ArticleMapperXML articleMapperXML;

    public EmployeePerformancePKPKService(EmployeePerformancePkEmployeeRelateMapper relateMapper,
                                          EmployeePerformancePkMapper employeePerformancePkMapper,
                                          ArticleMapperXML articleMapperXML) {
        this.relateMapper = relateMapper;
        this.employeePerformancePkMapper = employeePerformancePkMapper;
        this.articleMapperXML = articleMapperXML;
    }

    @Override
    @Transactional
    public void save(EmployeePerformancePk employeePerformancePk) {
        boolean isSave = Objects.isNull(employeePerformancePk.getId());

        employeePerformancePk.setCompanyCode(AppUtil.getUser().getCompanyCode());
        List<EmployeePerformancePk> ls = employeePerformancePkMapper.countDateConflict(employeePerformancePk);

        if (ls.size() > 0) {
            ResultEnum dateConflict = ResultEnum.DATE_CONFLICT;
            StringBuilder conflictMsg = new StringBuilder("存在pk时间冲突<br/>");
            for (EmployeePerformancePk conflict : ls) {
                conflictMsg.append("开始时间: ").append(DateUtils.format(conflict.getStartDate()));
                conflictMsg.append("结束时间: ").append(DateUtils.format(conflict.getEndDate())).append("<br/>");
            }
            dateConflict.setMsg(conflictMsg.toString());
            throw new QinFeiException(dateConflict);
        }

        if (isSave) {
            employeePerformancePk.setCreateInfo();
            employeePerformancePkMapper.insert(employeePerformancePk);
        } else {
            employeePerformancePk.setUpdateInfo();
            employeePerformancePkMapper.update(employeePerformancePk);
        }
        List<EmployeePerformancePKEmployeeRelate> relates = new ArrayList<>();
        List<String> leftLs = employeePerformancePk.getLeftPeopleIds();
        List<String> rightLs = employeePerformancePk.getRightPeopleIds();
        List<String> leftNames = employeePerformancePk.getLeftPeopleNames();
        List<String> rightNames = employeePerformancePk.getRightPeopleNames();
        for (int i = 0; i < leftLs.size(); i++) {
            Integer leftPeopleId = Integer.valueOf(leftLs.get(i));
            Integer rightPeopleId = Integer.valueOf(rightLs.get(i));

            EmployeePerformancePKEmployeeRelate relate = new EmployeePerformancePKEmployeeRelate();
            relate.setEEmployeePerformancePkId(employeePerformancePk.getId());
            relate.setLeftEmployeeId(leftPeopleId);
            relate.setRightEmployeeId(rightPeopleId);
            relate.setLeftEmployeeName(leftNames.get(i));
            relate.setRightEmployeeName(rightNames.get(i));
            relates.add(relate);
        }
        if (!isSave)
            relateMapper.deleteByPKId(employeePerformancePk.getId());
        relateMapper.insertBatch(relates);
    }

    @Override
    public EmployeePerformancePk findById(int id) {
        return employeePerformancePkMapper.findById(id);
    }

    @Override
    @Transactional
    public List<EmployeePerformancePk> all(Map<String, Object> map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        if (Objects.nonNull(map.get("currentDate"))) {
            String currentDate = map.get("currentDate").toString();
            if (!StringUtils.isEmpty(currentDate)) map.put("currentDate", DateUtils.parse(currentDate, "yyyy"));
            else map.put("currentDate", null);
        }
        return employeePerformancePkMapper.listAll(map);
    }

    @Override
    @Transactional
    public void deleteById(int performanceId) {
        employeePerformancePkMapper.deleteById(performanceId);
    }

    @Override
    @Transactional
    public void copy(int performanceId) {
        EmployeePerformancePk pk = employeePerformancePkMapper.findById(performanceId);
        pk.setId(null);
        pk.setStartDate(null);
        pk.setEndDate(null);
        pk.setCreateInfo();
        employeePerformancePkMapper.insertEmployeePerformancePk(pk);

        List<EmployeePerformancePKEmployeeRelate> relates = pk.getRelates()
                .stream().peek(item -> {
                    item.setId(null);
                    item.setEEmployeePerformancePkId(pk.getId());
                }).collect(Collectors.toList());
        relateMapper.insertBatch(relates);
    }

    /**
     * 查找所有pk设置并带出每个pk业务员利润
     *
     * @param data
     * @param map  传入时间，按年份查询
     * @return List<PerformancePKProfitDto>
     */
    @Override
    public void allWithProfit(ResponseData data, Map<String, Object> map) {
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        //年份下拉框筛选条件   查询所有pk视图
        if (Objects.nonNull(map.get("currentDate"))) {
            String currentDate = map.get("currentDate").toString();
            if (!StringUtils.isEmpty(currentDate)) map.put("currentDate", DateUtils.parse(currentDate, "yyyy"));
            else map.put("currentDate", null);
        }

        //现在分可查询所有pk视图的角色和只能查询当前pk视图的角色
        if (Objects.nonNull(map.get("queryCurrentDateTime"))) {
            map.put("currentDateTime", new Date());
        }

        List<PerformancePKProfitDto> peopleList = employeePerformancePkMapper.listAllWithoutRelate(map);
        for (PerformancePKProfitDto item : peopleList) {
            int pkId = item.getId();
            int pkType = item.getPkType();
            Date startDate = item.getStartDate();
            Date endDate = DateUtils.calDay(item.getEndDate(), 1);

            List<PerformancePKProfitDto.TopPeopleDto> topProfit = new ArrayList<>();

            List<PerformancePKProfitDto.PKBizPeople> peopleLs = new ArrayList<>();

            for (EmployeePerformancePKEmployeeRelate relate : relateMapper.selectByPKId(pkId)) {
                int leftId = relate.getLeftEmployeeId();
                int rightId = relate.getRightEmployeeId();

                float leftProfit;
                float rightProfit;
                if (pkType == 0) {
                    leftProfit = articleMapperXML.findProfitByMediaId(leftId, startDate, endDate).getProfit();
                    rightProfit = articleMapperXML.findProfitByMediaId(rightId, startDate, endDate).getProfit();
                } else {
                    leftProfit = articleMapperXML.findProfitByDeptId(leftId, startDate, endDate).getProfit();
                    rightProfit = articleMapperXML.findProfitByDeptId(rightId, startDate, endDate).getProfit();
                }

                String leftName = relate.getLeftEmployeeName();
                String rightName = relate.getRightEmployeeName();

                topProfit.add(new PerformancePKProfitDto.TopPeopleDto(leftId, leftName, leftProfit));
                topProfit.add(new PerformancePKProfitDto.TopPeopleDto(rightId, rightName, rightProfit));

                float dif = leftProfit - rightProfit;
                //计算比例
                String rate;

                int leftWin = dif == 0 ? 0 : dif > 0 ? 1 : -1;

                if (leftWin == 0) rate = "50";
                else {
                    float r = Math.abs(leftProfit) / Math.abs(leftProfit + rightProfit);
                    if (leftWin == 1) {
                        //考虑为负数情况 强制让左侧更多
                        rate = String.format("%.2f", (r < 0.5 ? 1 - r : r) * 100);
                    } else {
                        rate = String.format("%.2f", (r > 0.5 ? 1 - r : r) * 100);
                    }
                }

                UserMapper userMapper = SpringUtils.getBean(UserMapper.class);

                PerformancePKProfitDto.PKBizPeople dto = PerformancePKProfitDto.PKBizPeople.builder()
                        .leftId(leftId)
                        .leftName(leftName)
                        .leftProfit(leftProfit)
                        .rightId(rightId)
                        .rightName(rightName)
                        .rightProfit(rightProfit)
                        .leftWin(leftWin)
                        .leftRate(rate)
                        .leftAvatar(userMapper.findAvatarById(leftId))
                        .rightAvatar(userMapper.findAvatarById(rightId))
                        .build();
                //部门不计算利润
                if (pkType == 1) {
                    dto.setLeftProfit(0f);
                    dto.setRightProfit(0f);
                }
                peopleLs.add(dto);
            }

            item.setPeopleList(peopleLs);
            item.setStartYear(startDate);

            topProfit.sort((a, b) -> (int) (b.getProfit() - a.getProfit()));
            //部门不计算利润
            if (pkType == 1) {
                topProfit.forEach(top -> top.setProfit(0f));
            }
            item.setTopPeople(topProfit);
        }

        data.putDataValue("peopleLs", peopleList);
    }

    @Override
    public void years(ResponseData result) {
        result.putDataValue("years", employeePerformancePkMapper.getYears(AppUtil.getUser().getCompanyCode()));
    }
}
