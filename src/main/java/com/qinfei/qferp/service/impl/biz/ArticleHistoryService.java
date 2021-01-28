package com.qinfei.qferp.service.impl.biz;

import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.biz.ArticleHistoryMapper;
import com.qinfei.qferp.service.biz.IArticleHistoryService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.ExcelUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ArticleHistoryService implements IArticleHistoryService {
    @Autowired
    private ArticleHistoryMapper articleHistoryMapper;

    @Override
    public PageInfo<Map<String, Object>> queryArticleChange(int pageNum, int pageSize, Map map) {
        doWithPermission(map, AppUtil.getUser());
        Integer type = MapUtils.getInteger(map, "type");
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = new ArrayList();
        if (type == 1 || type == 2) {
            list = articleHistoryMapper.queryArticleChange(map);
        }
        return new PageInfo<>(list);
    }

    @Override
    public Map queryArticleChangeSum(Map map) {
        doWithPermission(map, AppUtil.getUser());
        Map result = articleHistoryMapper.queryArticleChangeSum(map);
        Map saleResult = articleHistoryMapper.queryArticleSaleAmountSum(map);
        if (!ObjectUtils.isEmpty(result) && !ObjectUtils.isEmpty(saleResult)
                && saleResult.containsKey("saleSum") && !ObjectUtils.isEmpty(saleResult.get("saleSum"))) {
            result.put("saleSumOriginal", saleResult.get("saleSum"));
        }
        return result;
    }

    @Override
    public Map queryArticleSaleAmountSum(Map map) {
        doWithPermission(map, AppUtil.getUser());
        return articleHistoryMapper.queryArticleSaleAmountSum(map);
    }

    @Override
    public PageInfo<Map<String, Object>> queryArticleChangeDetail(int pageNum, int pageSize, Map map) {
        doWithPermission(map, AppUtil.getUser());
        if (map.containsKey("createEndTime")) {
            String createEndTime = MapUtils.getString(map, "createEndTime");
            createEndTime = createEndTime.concat(" 23:59:59");
            map.put("createEndTime", createEndTime);
        }
        if (map.containsKey("issuedEndTime")) {
            String issuedEndTime = MapUtils.getString(map, "issuedEndTime");
            issuedEndTime = issuedEndTime.concat(" 23:59:59");
            map.put("issuedEndTime", issuedEndTime);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = articleHistoryMapper.queryArticleChangeDetail(map);
        return new PageInfo<>(list);
    }

    @Override
    public Map queryArticleChangeDetailSum(Map map) {
        doWithPermission(map, AppUtil.getUser());
        if (map.containsKey("createEndTime")) {
            String createEndTime = MapUtils.getString(map, "createEndTime");
            createEndTime = createEndTime.concat(" 23:59:59");
            map.put("createEndTime", createEndTime);
        }
        if (map.containsKey("issuedEndTime")) {
            String issuedEndTime = MapUtils.getString(map, "issuedEndTime");
            issuedEndTime = issuedEndTime.concat(" 23:59:59");
            map.put("issuedEndTime", issuedEndTime);
        }
        return articleHistoryMapper.queryArticleChangeDetailSum(map);
    }

    @Override
    public List<Map<String, Object>> exportArticleChange(Map map, OutputStream outputStream) {
        doWithPermission(map, AppUtil.getUser());
        if (map.containsKey("createEndTime")) {
            String createEndTime = MapUtils.getString(map, "createEndTime");
            createEndTime = createEndTime.concat(" 23:59:59");
            map.put("createEndTime", createEndTime);
        }
        if (map.containsKey("issuedEndTime")) {
            String issuedEndTime = MapUtils.getString(map, "issuedEndTime");
            issuedEndTime = issuedEndTime.concat(" 23:59:59");
            map.put("issuedEndTime", issuedEndTime);
        }
        List<Map<String, Object>> list = articleHistoryMapper.queryArticleChange(map);
        for (Map<String, Object> temp : list) {
            Integer createYear = MapUtils.getInteger(temp, "createYear");
            Integer createMonth = MapUtils.getInteger(temp, "createMonth");
            StringBuffer createStr = new StringBuffer();
            createStr.append(createYear).append("年").append(createMonth).append("月");
            temp.put("createYearAndMonth", createStr.toString());

            Integer issuedYear = MapUtils.getInteger(temp, "issuedYear");
            Integer issuedMonth = MapUtils.getInteger(temp, "issuedMonth");
            StringBuffer issuedStr = new StringBuffer();
            issuedStr.append(issuedYear).append("年").append(issuedMonth).append("月");
            temp.put("issuedYearAndMonth", issuedStr.toString());
        }
        String[] heads = {"修改年月", "发布年月", "业绩（含税）", "回款", "税金", "退款", "其它支出",
                "成本", "利润", "提成"};
        String[] fields = {"createYearAndMonth", "issuedYearAndMonth", "alterSale", "alterIncome", "alterTax", "alterRefund", "alterOtherPay",
                "alterOutgo", "alterProfit", "alterComm"};
        ExcelUtil.exportExcel("业绩统计", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("alterSale".equals(field) || "alterIncome".equals(field) || "alterTax".equals(field) || "alterRefund".equals(field)
                        || "alterOtherPay".equals(field) || "alterOutgo".equals(field) || "alterProfit".equals(field) || "alterComm".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public List<Map<String, Object>> exportArticleChangeDetail(Map map, OutputStream outputStream) {
        doWithPermission(map, AppUtil.getUser());
        List<Map<String, Object>> list = articleHistoryMapper.queryArticleChangeDetail(map);
        String[] heads = {"媒体板块", "媒体", "媒介", "业务部门", "业务员",
                "标题", "链接", "发布日期", "业绩（含税）", "回款", "税金", "退款", "其它支出",
                "成本", "利润", "提成", "修改日期", "修改方式"};
        String[] fields = {"mediaTypeName", "mediaName", "mediaUserName", "deptName", "userName",
                "title", "link", "issuedDate", "alterSale", "alterIncome", "alterTax", "alterRefund", "alterOtherPay",
                "alterOutgo", "alterProfit", "alterComm", "createTime", "editDesc"};
        ExcelUtil.exportExcel("业绩统计", heads, fields, list, outputStream, "yyyy-MM-dd", (sheet, rowIndex, cellIndex, row, cell, field, value) -> {
            if (value != null) {
                if ("alterSale".equals(field) || "alterIncome".equals(field) || "alterTax".equals(field) || "alterRefund".equals(field)
                        || "alterOtherPay".equals(field) || "alterOutgo".equals(field) || "alterProfit".equals(field) || "alterComm".equals(field)) {
                    cell.setCellValue(Double.parseDouble(value.toString()));
                } else {
                    cell.setCellValue(value.toString());
                }
            }
        });
        return list;
    }

    @Override
    public PageInfo<Map<String, Object>> queryArticleChangeSingle(int pageNum, int pageSize, Map map) {
        doWithPermission(map, AppUtil.getUser());
        if (map.containsKey("createEndTime")) {
            String createEndTime = MapUtils.getString(map, "createEndTime");
            createEndTime = createEndTime.concat(" 23:59:59");
            map.put("createEndTime", createEndTime);
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = articleHistoryMapper.queryArticleChangeSingle(map);
        return new PageInfo<>(list);
    }

    @Override
    public Map queryArticleChangeSingleSum(Map map) {
        doWithPermission(map, AppUtil.getUser());
        if (map.containsKey("createEndTime")) {
            String createEndTime = MapUtils.getString(map, "createEndTime");
            createEndTime = createEndTime.concat(" 23:59:59");
            map.put("createEndTime", createEndTime);
        }
        return articleHistoryMapper.queryArticleChangeSingleSum(map);
    }

    private Map doWithPermission(Map map, User user) {
        map.put("companyCode", user.getCompanyCode());
        List<Role> roles = user.getRoles();
        Integer typeQx = 0;//权限类型，放在where子句,type=1
        for (Role role : roles) {
            if (IConst.ROLE_TYPE_ZJB.equals(role.getType())
                    || IConst.ROLE_TYPE_CW.equals(role.getType())) {
                typeQx = 1;
            }
            if (IConst.ROLE_TYPE_JT.equals(role.getType())) {
                typeQx = 2;
            }
        }
        map.put("typeQx", typeQx);
        return map;
    }
}
