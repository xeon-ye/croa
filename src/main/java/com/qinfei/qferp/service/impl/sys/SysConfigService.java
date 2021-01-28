package com.qinfei.qferp.service.impl.sys;

import com.alibaba.fastjson.JSON;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.SysConfig;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.mapper.sys.SysConfigMapper;
import com.qinfei.qferp.service.sys.ISysConfigService;
import com.qinfei.qferp.utils.AppUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @CalssName: SysConfigService
 * @Description: 系统配置参数接口
 * @Author: Xuxiong
 * @Date: 2020/3/13 0013 16:02
 * @Version: 1.0
 */
@Service
public class SysConfigService implements ISysConfigService {
    @Autowired
    private SysConfigMapper sysConfigMapper;
    @Autowired
    private DeptMapper deptMapper;

    @Transactional
    @Override
    public SysConfig save(SysConfig sysConfig) {
        SysConfig result = null;
        try{
            User user = AppUtil.getUser();
            validSave(sysConfig, user); //数据校验
            sysConfigMapper.save(sysConfig);
            result = sysConfig;
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "新增系统配置参数异常！");
        }
        return result;
    }

    private void validSave(SysConfig sysConfig, User user){
        if(user == null){
            throw new QinFeiException(1002, "请先登录");
        }
        if(StringUtils.isEmpty(sysConfig.getConfigTitle())){
            throw new QinFeiException(1002, "功能名称不能为空！");
        }
        if(StringUtils.isEmpty(sysConfig.getConfigKey())){
            throw new QinFeiException(1002, "配置类型不能为空！");
        }
        if(StringUtils.isEmpty(sysConfig.getDataType())){
            throw new QinFeiException(1002, "数据类型不能为空！");
        }
        if(StringUtils.isEmpty(sysConfig.getConfigKey())){
            throw new QinFeiException(1002, "配置项键不能为空！");
        }
        if(StringUtils.isEmpty(sysConfig.getConfigValue())){
            throw new QinFeiException(1002, "配置项值不能为空！");
        }
        if("json".equals(sysConfig.getDataType())){
            try{
                Map<String, Object> map = JSON.parseObject(sysConfig.getConfigValue(), Map.class);
                if(map == null){
                    throw new QinFeiException(1002, "配置项值Json转换无数据，请检查！");
                }
            }catch (Exception e){
                throw new QinFeiException(1002, "配置项值Json转换异常，请检查！");
            }
        }
        if("list".equals(sysConfig.getDataType()) && StringUtils.isEmpty(sysConfig.getConfigPattern())){
            sysConfig.setConfigPattern(","); //默认逗号分隔
        }
        if("date".equals(sysConfig.getDataType()) && StringUtils.isEmpty(sysConfig.getConfigPattern())){
            sysConfig.setConfigPattern("yyyy-MM-dd HH:mm:ss"); //默认日期格式
        }
        if("int".equals(sysConfig.getDataType())){
            try{
                Integer.parseInt(sysConfig.getConfigValue());
            }catch (Exception e){
                throw new QinFeiException(1002, "配置项值格式不满足数据类型要求！");
            }
        }
        if("float".equals(sysConfig.getDataType())){
            try{
                Float.parseFloat(sysConfig.getConfigValue());
            }catch (Exception e){
                throw new QinFeiException(1002, "配置项值格式不满足数据类型要求！");
            }
        }
        if("double".equals(sysConfig.getDataType())){
            try{
                Double.parseDouble(sysConfig.getConfigValue());
            }catch (Exception e){
                throw new QinFeiException(1002, "配置项值格式不满足数据类型要求！");
            }
        }
        if("date".equals(sysConfig.getDataType())){
            if(DateUtils.parse(sysConfig.getConfigValue(), sysConfig.getConfigPattern()) == null){
                throw new QinFeiException(1002, "配置项值格式不满足数据类型要求！");
            }
        }
        //校验是否有存在Key
        SysConfig temp = sysConfigMapper.getOneConfigByKey(sysConfig.getConfigKey(), sysConfig.getId());
        if(temp != null){
            throw new QinFeiException(1002, "存在该配置Key，请重新命名！");
        }
        sysConfig.setCreateId(user.getId());
        sysConfig.setUpdateId(user.getId());
    }

    @Transactional
    @Override
    public void update(SysConfig sysConfig) {
        try{
            User user = AppUtil.getUser();
            validUpdate(sysConfig, user);
            sysConfigMapper.update(sysConfig);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改系统配置参数异常！");
        }
    }

    private void validUpdate(SysConfig sysConfig, User user){
        if(sysConfig.getId() == null){
            throw new QinFeiException(1002, "主键ID不存在！");
        }
        validSave(sysConfig, user);
    }

    @Transactional
    @Override
    public void updateStateById(int id, byte state) {
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录！");
            }
            sysConfigMapper.updateStateById(state, user.getId(), id);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "修改系统配置参数状态异常！");
        }
    }

    @Override
    public PageInfo<SysConfig> list(Map<String, Object> map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        List<SysConfig> sysConfigList = sysConfigMapper.listConfigByParam(map);
        return new PageInfo<>(sysConfigList);
    }

    @Override
    public Map<String, Map<String, Object>> getAllConfig() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        List<SysConfig> sysConfigList = sysConfigMapper.listAllSysConfig();
        if(CollectionUtils.isNotEmpty(sysConfigList)){
            //由于许多系统参数配置在这里，所以如果某个参数解析异常，不能影响其他参数，所以不抛出异常
            for(SysConfig sysConfig : sysConfigList){
                try{
                    if("list".equals(sysConfig.getDataType())){
                        //split需要转义的特殊字符
                        List<String> splitCharts = Arrays.asList("(", ")", "[", "]", "{", "}", "\\", "?", "*", "+", ".", "^", "$", "|");
                        String pattern = splitCharts.contains(sysConfig.getConfigPattern()) ? String.format("[%s]",sysConfig.getConfigPattern()) : sysConfig.getConfigPattern();
                        handlerConfigValue(result, sysConfig.getConfigKey(), Arrays.asList(sysConfig.getConfigValue().split(pattern)), sysConfig.getDataType(), null);
                    }else if("json".equals(sysConfig.getDataType())){
                        handlerConfigValue(result, sysConfig.getConfigKey(), JSON.parseObject(sysConfig.getConfigValue(), Map.class), sysConfig.getDataType(), null);
                    }else if("int".equals(sysConfig.getDataType())){
                        handlerConfigValue(result, sysConfig.getConfigKey(), Integer.parseInt(sysConfig.getConfigValue()), sysConfig.getDataType(), null);
                    }else if("float".equals(sysConfig.getDataType())){
                        handlerConfigValue(result, sysConfig.getConfigKey(), Float.parseFloat(sysConfig.getConfigValue()), sysConfig.getDataType(), null);
                    }else if("double".equals(sysConfig.getDataType())){
                        handlerConfigValue(result, sysConfig.getConfigKey(), Double.parseDouble(sysConfig.getConfigValue()), sysConfig.getDataType(), null);
                    }else if("date".equals(sysConfig.getDataType())){
                        handlerConfigValue(result, sysConfig.getConfigKey(), DateUtils.parse(sysConfig.getConfigValue(), sysConfig.getConfigPattern()), sysConfig.getDataType(), sysConfig.getConfigPattern());
                    }else {
                        handlerConfigValue(result, sysConfig.getConfigKey(), sysConfig.getConfigValue(), sysConfig.getDataType(), null);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    //处理获取的配置值
    private void handlerConfigValue(Map<String, Map<String, Object>> result, String key, Object value, String dataType, String pattern){
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("dataType", dataType);
        if(StringUtils.isNotEmpty(pattern)){
            itemMap.put("pattern", pattern);
        }
        itemMap.put("value", value);
        result.put(key, itemMap);
    }

    @Override
    public List<Map<String, Object>> listTableData(String configType) {
        List<Map<String, Object>> result = new ArrayList<>();
        if("company_code".equals(configType)){
            List<Dept> deptList = deptMapper.listJTAllCompany(null);
            if(CollectionUtils.isNotEmpty(deptList)){
                for(Dept dept : deptList){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", dept.getName());
                    map.put("value", dept.getCode());
                    result.add(map);
                }
            }
        }else if("user_id".equals(configType)){
            listHandler(result, sysConfigMapper.listAllUser(), "name", "id");
        }else if("dept_id".equals(configType)){
            listHandler(result, sysConfigMapper.listAllDept(), "name", "id");
        }else if("dept_type".equals(configType)){
            listHandler(result, sysConfigMapper.listAllDept(), "name", "type");
        }else if("dept_code".equals(configType)){
            listHandler(result, sysConfigMapper.listAllDept(), "name", "code");
        }else if("role_type".equals(configType)){
            listHandler(result, sysConfigMapper.listAllRole(), "name", "type");
        }else if("role_code".equals(configType)){
            listHandler(result, sysConfigMapper.listAllRole(), "name", "code");
        }else if("media_plate".equals(configType)){
            listHandler(result, sysConfigMapper.listAllMediaPlate(), "name", "id");
        }
        return result;
    }

    @Override
    public SysConfig getOneConfigByKey(String configKey) {
        SysConfig sysConfig = null;
        try{
            User user = AppUtil.getUser();
            if(user == null){
                throw new QinFeiException(1002, "请先登录");
            }
            sysConfig = sysConfigMapper.getOneConfigByKey(configKey, null);
        }catch (QinFeiException e){
            throw e;
        }catch (Exception e){
            throw new QinFeiException(1002, "获取配置键对应的值失败！");
        }
        return sysConfig;
    }

    //处理读取数据的列表
    private void listHandler(List<Map<String, Object>> result, List<Map<String, Object>> source, String nameKey, String valueKey){
        if(CollectionUtils.isNotEmpty(source)){
            List<String> temp = new ArrayList<>(); //用户判断List是否存在相同value
            for(Map<String, Object> dataMap : source){
                if(dataMap.get(valueKey) != null && StringUtils.isNotEmpty(String.valueOf(dataMap.get(valueKey))) && !temp.contains(String.valueOf(dataMap.get(valueKey)))){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", dataMap.get(nameKey));
                    map.put("value", dataMap.get(valueKey));
                    temp.add(String.valueOf(dataMap.get(valueKey)));
                    result.add(map);
                }
            }
        }
    }
}
