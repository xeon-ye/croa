package com.qinfei.core.serivce.impl;


import com.qinfei.core.annotation.Transient;
import com.qinfei.core.data.DictiEnum;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.exception.ResultEnum;
import com.qinfei.core.mapper.DictMapper;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.core.utils.StrUtil;
import com.qinfei.qferp.entity.announcementinform.MediaPass;
import com.qinfei.qferp.entity.fee.TaxUser;
import com.qinfei.qferp.entity.propose.ProposeRelation;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.crm.CrmCompanyUserMapper;
import com.qinfei.qferp.mapper.fee.TaxUserMapper;
import com.qinfei.qferp.mapper.propose.ProposeRelationMapper;
import com.qinfei.qferp.mapper.sys.DeptMapper;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import org.flowable.spring.boot.app.App;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.util.MapUtils;

import java.util.*;


@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class DictService implements IDictService {
    @Autowired
    private DictMapper dictMapper;
    @Autowired
    private DeptMapper deptMapper;
    @Autowired
    private ProposeRelationMapper proposeRelationMapper;
    @Autowired
    private TaxUserMapper taxUserMapper;
    @Autowired
    private CrmCompanyUserMapper crmCompanyUserMapper;

    @Override
    public void initdictEnum() throws Exception {
        List<Dict> dicts = dictMapper.all(Dict.class);
        this.toTree(dicts);
        //用枚举存放数据字典
        for (DictiEnum type : DictiEnum.values()) {
            List<Dict> list = new ArrayList<Dict>();
            for (Dict dict : dicts) {
                if (type.getTypeCode().equals(dict.getTypeCode())) {
                    list.add(dict);
                }
            }
            //加载字典
            type.load(list);
        }
    }

    @Override
    public Dict getByTypeCodeAndCode(Dict dict) {
        return dictMapper.getByTypeCodeAndCode(dict.getTypeCode(), dict.getCode());
    }

    @Override
    public List<Dict> list(Dict dict) {
        return dictMapper.list(dict);
    }

    @Override
    @Cacheable(value = CACHE_KEY_LIST, key = "'typeCode='+#typeCode")
    public List<Dict> listByTypeCode(String typeCode) {
        return dictMapper.listByTypeCode(typeCode);
    }

    @Override
    @Cacheable(value = CACHE_KEY_LIST, key = "'typeCode='+#typeCode+'&companyCode='+#companyCode")
    public List<Dict> listByTypeCodeAndCompanyCode(String typeCode, String companyCode) {
        return dictMapper.listByTypeCodeAndCompanyCode(typeCode,companyCode);
    }

    @Override
    public List<Dict> listDict (String typtCode){
        User user= AppUtil.getUser();
        Map map=new HashMap();
        map.put("typeCode",typtCode);
        map.put("userDeptId",user.getDeptId());
        return dictMapper.listDict(map);
    }

//    @Override
//    public List<Map<String, Object>> list(Map<String, Object> param) {
//        String sql = "SELECT a.*,b.`type_id`,b.`type`,b.`term_sql`,b.`term_name`,b.`state`,b.name,b.`json`,b.`html`,b.`data_type` FROM (\n" +
//                "SELECT * FROM sys_dict WHERE type_code=" + param.get("type_code") + " AND disabled=" + param.get("disabled") + " ) a LEFT JOIN t_media_term b ON a.`code`=b.`name`  ORDER BY  sort_no";
//        return dictMapper.dictSQL(sql);
//    }

//    @Override
//    public List<Map<String, Object>> listByTypeCode(Map<String, Object> param) {
//        String sql = "SELECT a.*,b.`type_id`,b.`type`,b.`term_sql`,b.`term_name`,b.`state`,b.name,b.`json`,b.`html`,b.`data_type` FROM (\n" +
//                "SELECT * FROM sys_dict WHERE type_code=" + param.get("type_code") + " AND disabled=0 ) a LEFT JOIN t_media_term b ON a.`code`=b.`name` AND a.type_code=b.TYPE_id ORDER BY  sort_no";
//        return dictMapper.dictSQL(sql);
//    }

    /**
     * Description: 将节点放进父节点
     *
     * @param dicts
     */
    private List<Dict> toTree(List<Dict> dicts) {
        for (Dict dict : dicts) {
            int parentId = dict.getParentId();
            if (-1 == parentId || 0 == parentId) continue;
            for (Dict parent : dicts) {
                if (parentId == parent.getId()) {
                    if (parent.getChilds() == null) {
                        parent.setChilds(new HashSet<Dict>());
                    }
                    parent.getChilds().add(dict);
                    break;
                }
            }
        }
        return dicts;
    }

    @Override
    public Dict getByTypeCodeAndName(String typeCode, String name,String companyCode) {
        return dictMapper.getByTypeCodeAndName(typeCode, name, companyCode);
    }

    @Override
    public Dict getDictName(String typeCode, String name,Integer userDeptId){
        return dictMapper.getDictName(typeCode,name,userDeptId);
    }


    /**
     * 查询抬头表
     *
     * @param map
     * @param pageable
     * @return
     */
    @Override
    public PageInfo<Dict> selectDict(@RequestParam Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        User user = AppUtil.getUser();
        List<Dict> list;
        map.put("userId",user.getId());
        map.put("deptId",user.getDeptId());
        map.put("companyCode", user.getCompanyCode());
        list = dictMapper.selectDict(map);
        return (PageInfo<Dict>) new PageInfo(list);
    }

    /**
     * 增加抬头
     */

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public Dict add(Dict dict, Integer[] deptIds) {
        List<Map> maps = new ArrayList<>();
        User user = AppUtil.getUser();
        //更新人、创建者
        user = user == null ? new User() : user;
        dict.setCreateId(user.getId());
        dict.setUpdateId(user.getId());
        dict.setUpdateUser(user.getName());
        dict.setCreateUser(user.getName());
        dict.setCreateTime(new Date());
        dict.setUpdateTime(new Date());
        dict.setCompanyCode(user.getCompanyCode());
        dict.setTypeCode("tax");
        dict.setTypeName("税种");
        List<TaxUser> taxUserList = new ArrayList<>();


        if (dictMapper.getDictCount(dict)>0){
            throw new QinFeiException(ResultEnum.DICT_NAME_EXIST);
        }
        dictMapper.insertReturnId(dict);
        if (deptIds !=null && deptIds.length>0) {
            for (Integer deptId : deptIds) {
                Map<String, Object> map = new HashMap<>();
                map.put("deptId", deptId);
                map.put("dictId", dict.getId());
                maps.add(map);
            }

            dictMapper.insertDeptId(maps);
        }
        if (!CollectionUtils.isEmpty(dict.getInputUserId())){
            for (Integer userId: dict.getInputUserId()){
                TaxUser taxUser = new TaxUser();
                taxUser.setAssistantUserId(userId);
                taxUser.setCreateUserId(user.getId());
                taxUser.setCreateTime(new Date());
                taxUser.setDictId(dict.getId());
                taxUser.setUpdateUserId(user.getId());
                taxUser.setUpdateTime(new Date());
                taxUserList.add(taxUser);

            }
            taxUserMapper.addTaxUser(taxUserList);
        }
        return dict;
    }

    @Override
    public Dict getById(Integer id) {
        return dictMapper.getById(id);
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public void delById(Dict dict) {
        User user = AppUtil.getUser();
        dict.setState(IConst.STATE_DELETE);
        dict.setUpdateId(user.getId());
        dictMapper.update(dict);
    }

    @Override
    public List<Dept> queryDeptId(Integer id) {
        return dictMapper.queryDeptId(id);
    }

    @Override
    @Transactional
    public List<Dept> delDept(Integer dictId,Integer deptId) {
        List<Dept> list =deptMapper.listByParentId(deptId);
        Map<String, Object> map = new HashMap<>();
        Dept dept = new Dept();
        dept.setId(deptId);
        list.add(dept);
        map.put("dictId",dictId);
        map.put("list",list);
        if (list != null && list.size() >0){
        dictMapper.delDeptAccountDept(map);
        }
        return list;
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public Dict edit(Dict dict){
        User user = AppUtil.getUser();
        dict.setUpdateId(user.getId());
        dict.setUpdateUser(user.getName());
        dict.setUpdateTime(new Date());
        dict.setTypeCode("tax");
        dict.setCompanyCode(user.getCompanyCode());
        if (dictMapper.getDictCount(dict)>0){
            throw new QinFeiException(ResultEnum.DICT_NAME_EXIST);
        }
        dictMapper.update(dict);
        return  dict;
    }

    @Override
    public  void editDeptId(Integer dictId ){
        dictMapper.editDeptId(dictId);
    }

    @Override
    public void insertDept(List<Map> file) {
        dictMapper.insertDept(file);

    }

    /**
     * 建议类型管理
     * @param nameQc
     * @param id
     * @return
     */
    @Override
    public PageInfo<Dict> queryProposeDict(String nameQc,Integer id,Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        String companyCode = AppUtil.getUser().getCompanyCode();
        List<Dict> list = dictMapper.queryProposeDict(nameQc,id,companyCode);
        PageInfo page = new PageInfo(list);
        return page;
    }

    /**
     * 添加建议
     * @param dict
     * @return
     */
    @Override
  @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public Dict saveSuggest(Dict dict,String ids) {
        dict.setTypeCode("PROPOSE_TYPE");
        dict.setTypeName("建议类型");
        dict.setCode("24");
        dict.setName(dict.getName().trim());
        dict.setCompanyCode(AppUtil.getUser().getCompanyCode());
        dictMapper.insertReturnId(dict);
        if(ids!=null && !"".equals(ids)){
            List<ProposeRelation> list = new ArrayList<>();
            String [] str = ids.split(",");
            Integer userId = AppUtil.getUser().getId();
            for(String id : str){
                ProposeRelation obj = new ProposeRelation();
                obj.setId(dict.getId());
                obj.setUserId(Integer.parseInt(id));
                obj.setCreator(userId);
                obj.setCreateTime(new Date());
                list.add(obj);
            }
            proposeRelationMapper.addProposeRelation(list);
        }
        return dict;
    }

    /**
     * 修改建议类型
     * @param dict
     * @param ids
     */
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public void updateSuggest(Dict dict,String ids) {
        dictMapper.update(dict);
        if(ids!=null && !"".equals(ids)){
            List<ProposeRelation> list = new ArrayList<>();
            String [] str = ids.split(",");
            Integer userId = AppUtil.getUser().getId();
            //删除建议类型关系
            proposeRelationMapper.delProposeRelation(dict.getId());
            if(str.length>0) {
                for (String id : str) {
                    ProposeRelation obj = new ProposeRelation();
                    obj.setId(dict.getId());
                    obj.setUserId(Integer.parseInt(id));
                    obj.setCreator(userId);
                    obj.setCreateTime(new Date());
                    list.add(obj);
                }
                proposeRelationMapper.addProposeRelation(list);
            }
        }
    }

    /**
     * 删除建议类型
     * @param id
     */
    @Override
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, allEntries = true)})
    public void delSuggest(Integer id) {
        dictMapper.delSuggest(id);
        proposeRelationMapper.delProposeRelation(id);
    }

    /**
     *
     *根据公司code 查询公司名称
     */
    @Override
    public String code(String code){
        return  dictMapper.selectCodeName(code);
    }

    @Override
    @Transient
    @Caching(evict = {@CacheEvict(value = CACHE_KEY_LIST, key = "'typeCode='+#typeCodeProtect"),
            @CacheEvict(value = CACHE_KEY_LIST, key = "'typeCode='+#typeCodeTrans")})
    public void updateCompanyParam(String typeCodeProtect, String level, Integer num, Integer audit, Integer eval,
                                    String typeCodeTrans, Integer TRACK_EVAL_DAY, Integer DEAL_EVAL_DAY, Integer EVAL_REMIND_DAY,
                                   Integer TO_BLACK_TIMES, Integer CLAIM_TIMES_DAY, Integer CLAIM_START_TIME){
        String numCode = new StringBuffer(IConst.CUST_PROTECT_NUM).append("_").append(level).toString();
        String evalCode = new StringBuffer(IConst.CUST_PROTECT_EVAL).append("_").append(level).toString();
        String auditCode = new StringBuffer(IConst.CUST_PROTECT_AUDIT).append("_").append(level).toString();
        List<Dict> protectList = this.listByTypeCode(typeCodeProtect);
        if(protectList == null || protectList.size() == 0){
            throw new QinFeiException(1002,"字典表中typeCode="+typeCodeProtect+"的数据未找到！") ;
        }
        for(int i=0;i<protectList.size();i++){
            Dict dict = protectList.get(i);
            if(numCode.equals(dict.getCode())){
                dict.setType(num.toString());
            }
            if(evalCode.equals(dict.getCode())){
                dict.setType(eval.toString());
            }
            if(auditCode.equals(dict.getCode())){
                dict.setType(audit.toString());
            }
            dict.setUpdateTime(new Date());
            dict.setUpdateId(AppUtil.getUser().getId());
            dictMapper.update(dict);
        }

        List<Dict> transList = this.listByTypeCode(typeCodeTrans);
        if(transList == null || transList.size() == 0){
            throw new QinFeiException(1002,"字典表中typeCode="+typeCodeTrans+"的数据未找到！") ;
        }
        for(int i=0;i<transList.size();i++){
            Dict dict = transList.get(i);
            if(IConst.TRACK_EVAL_DAY.equals(dict.getCode())){
                if(!dict.getType().equals(TRACK_EVAL_DAY.toString())){//跟进天数有变化，重置跟进时间
                    int count = crmCompanyUserMapper.resetEvalTime();
                    System.out.println("重置了跟进考核客户数量："+count);
                }
                dict.setType(TRACK_EVAL_DAY.toString());
            }
            if(IConst.DEAL_EVAL_DAY.equals(dict.getCode())){//考核天数有变化，重置考核时间
                if(!dict.getType().equals(DEAL_EVAL_DAY.toString())){//跟进天数有变化，重置跟进时间
                    int count = crmCompanyUserMapper.resetDealTime();
                    System.out.println("重置了成交考核客户数量："+count);
                }
                dict.setType(DEAL_EVAL_DAY.toString());
            }
            if(IConst.EVAL_REMIND_DAY.equals(dict.getCode())){
                dict.setType(EVAL_REMIND_DAY.toString());
            }
            if(IConst.TO_BLACK_TIMES.equals(dict.getCode())){
                dict.setType(TO_BLACK_TIMES.toString());
            }
            if(IConst.CLAIM_TIMES_DAY.equals(dict.getCode())){
                dict.setType(CLAIM_TIMES_DAY.toString());
            }
            if(IConst.CLAIM_START_TIME.equals(dict.getCode())){
                dict.setType(CLAIM_START_TIME.toString());
            }
            dict.setUpdateTime(new Date());
            dict.setUpdateId(AppUtil.getUser().getId());
            dictMapper.update(dict);
        }
    }
}