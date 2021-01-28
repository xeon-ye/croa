package com.qinfei.core.controller;

import com.qinfei.core.ResponseData;
import com.qinfei.core.entity.Dict;
import com.qinfei.core.serivce.IDictService;
import com.qinfei.qferp.entity.fee.TaxUser;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.fee.ITaxUserService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 数据字典
 *
 * @author GZW
 */
@Controller
@RequestMapping("/dict")
class DictController {

    @Autowired
    IDictService dictService;
    @Autowired
    ITaxUserService taxUserService;

    @GetMapping
    @ResponseBody
    public Dict getByDict(Dict dict) {
        return dictService.getByTypeCodeAndCode(dict);
    }

    @GetMapping("list")
    @ResponseBody
    public List<Dict> list(Dict dict) {
        return dictService.list(dict);
    }

//    @GetMapping("dicts")
//    @ResponseBody
//    public List<Map<String, Object>> list(@RequestParam Map<String, Object> param) {
//        return dictService.list(param);
//    }

//    @GetMapping("listByTypeCode")
//    @ResponseBody
//    public List<Map<String, Object>> listByTypeCode(@RequestParam Map<String, Object> param) {
//        return dictService.listByTypeCode(param);
//    }

    @GetMapping("listByTypeCode2")
    @ResponseBody
    public List<Dict> listByTypeCode2(@RequestParam("typeCode") String typeCode) {
        return dictService.listByTypeCode(typeCode);
    }

    @RequestMapping("listDict")
    @ResponseBody
    public List<Dict> listDict(@RequestParam("typeCode") String typeCode) {

        return dictService.listDict(typeCode);
    }

    @RequestMapping("view")
    @ResponseBody
    public ResponseData view(@RequestParam("typeCode") String typeCode, @RequestParam("name") String name) {
        try {
            ResponseData data = ResponseData.ok();
            String companyCode = AppUtil.getUser().getCompanyCode();
            Dict entity = dictService.getByTypeCodeAndName(typeCode, name, companyCode);
            data.putDataValue("entity", entity);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @RequestMapping("getDictName")
    @ResponseBody
    public ResponseData getDictName(@RequestParam("typeCode") String typeCode, @RequestParam("name") String name) {
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            Integer userDeptId = user.getDeptId();
            Dict entity = dictService.getDictName(typeCode, name, userDeptId);
            data.putDataValue("entity", entity);
            data.putDataValue("message", "操作成功");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 查询抬头
     *
     * @param pageable
     * @return
     */
    @RequestMapping("/listPg")
    @ResponseBody
    public PageInfo<Dict> listPg(@RequestParam Map map, @PageableDefault() Pageable pageable) {
        return dictService.selectDict(map, pageable);
    }

    /**
     * 新增抬头
     */
    @RequestMapping("/add")
    @ResponseBody
    public ResponseData add(@RequestBody Dict dict) {
        try {

            Integer[] deptIds = dict.getDeptIds();
            dictService.add(dict, deptIds);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", dict);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 删除选择部门
     *
     * @param deptId
     * @return
     */
    @RequestMapping("/delDept")
    @ResponseBody
    public ResponseData delDept(@RequestParam("dictId") Integer dictId, @RequestParam("deptId") Integer deptId) {
        try {

            List<Dept> list = dictService.delDept(dictId, deptId);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("list", list);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 删除税种
     */
    @RequestMapping(value = "/del")
    @ResponseBody
    public ResponseData del(@RequestParam("id") Integer id) {
        try {
            Dict dict = dictService.getById(id);
            ResponseData data = ResponseData.ok();
            dictService.delById(dict);
            data.putDataValue("message", "操作成功");
            return data;

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    //编辑、查看内容
    @RequestMapping(value = "/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id") Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            Dict dict = dictService.getById(id);
            List<Dept> list = dictService.queryDeptId(dict.getId());
            List<TaxUser> listUser = taxUserService.getTaxUser(dict.getId());
            data.putDataValue("list", list);
            data.putDataValue("entity", dict);
            data.putDataValue("listUser", listUser);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    //编辑抬头管理保存
    @RequestMapping(value = "/edit")
    @ResponseBody
    public ResponseData edit(@RequestBody Dict dict) {
        try {
            User user = AppUtil.getUser();
            Integer[] deptIds = dict.getDeptIds();
            dictService.edit(dict);
            if (deptIds != null) {
                Map map;
                List<Map> file = new ArrayList<>();
                Integer dictId = dict.getId();
                for (int deptId : deptIds) {
                    map = new HashMap();
                    map.put("deptId", deptId);
                    map.put("id", dict.getId());
                    file.add(map);
                }
                dictService.editDeptId(dictId);
                dictService.insertDept(file);
            }
            if (dict.getInputUserId() != null) {

                List<TaxUser> taxUserList = new ArrayList<>();
                Integer dictId1 = dict.getId();
                for (Integer userId : dict.getInputUserId()) {
                    TaxUser taxUser = new TaxUser();
                    taxUser.setDictId(dictId1);
                    taxUser.setAssistantUserId(userId);
                    taxUser.setCreateUserId(user.getId());
                    taxUser.setCreateTime(new Date());
                    taxUser.setUpdateUserId(user.getId());
                    taxUser.setUpdateTime(new Date());
                    taxUserList.add(taxUser);
                }
                taxUserService.editDictUser(dictId1);
                taxUserService.insertAssistant(taxUserList);
            }
            ResponseData data = ResponseData.ok();
            data.putDataValue("message", "操作成功");
            data.putDataValue("entity", dict);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    /**
     * 建议类型管理
     *
     * @param nameQc
     * @param id
     * @return
     */
    @RequestMapping("/queryProposeDict")
    @ResponseBody
    public PageInfo<Dict> queryProposeDict(@RequestParam(value = "nameQc", required = false) String nameQc, @RequestParam(value = "id", required = false) Integer id, Pageable pageable) {
        return dictService.queryProposeDict(nameQc, id, pageable);
    }

    /**
     * 添加建议类型
     *
     * @param dict
     * @param ids
     * @return
     */
    @RequestMapping("/saveAdvice")
    @ResponseBody
    public ResponseData saveAdvice(Dict dict, String ids) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        dictService.saveSuggest(dict, ids);
        return data;
    }

    /**
     * 修改建议类型
     *
     * @param dict
     * @param ids
     * @return
     */
    @RequestMapping("/editAdvice")
    @ResponseBody
    public ResponseData updateAdvice(Dict dict, String ids) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        dictService.updateSuggest(dict, ids);
        return data;
    }

    /**
     * 修改建议类型
     *
     * @param id
     * @return
     */
    @RequestMapping("/delAdvice")
    @ResponseBody
    public ResponseData delAdvice(@RequestParam Integer id) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        dictService.delSuggest(id);
        return data;
    }

    /**
     * 客户保护参数设置
     * @param level  A/B/C  三类
     * @param num  保护数量
     * @param audit 是否审核，1是0否
     * @param eval 是否考核，1是0否
     * @return
     */
    @RequestMapping("/updateCompanyParam")
    @ResponseBody
    public ResponseData updateCompanyParam(@RequestParam("level") String level,
                                          @RequestParam("num") Integer num,
                                          @RequestParam("audit") Integer audit,
                                          @RequestParam("eval") Integer eval,
                                          @RequestParam("TRACK_EVAL_DAY") Integer TRACK_EVAL_DAY,
                                          @RequestParam("DEAL_EVAL_DAY") Integer DEAL_EVAL_DAY,
                                          @RequestParam("EVAL_REMIND_DAY") Integer EVAL_REMIND_DAY,
                                          @RequestParam("TO_BLACK_TIMES") Integer TO_BLACK_TIMES,
                                          @RequestParam("CLAIM_TIMES_DAY") Integer CLAIM_TIMES_DAY,
                                          @RequestParam("CLAIM_START_TIME") Integer CLAIM_START_TIME) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("message", "操作成功");
        String typeCodeProtect = new StringBuffer(IConst.CUST_PROTECT).append("_").append(level).toString();
        dictService.updateCompanyParam(typeCodeProtect, level, num, audit, eval, IConst.CUST_TRANSFER, TRACK_EVAL_DAY, DEAL_EVAL_DAY, EVAL_REMIND_DAY,
                TO_BLACK_TIMES, CLAIM_TIMES_DAY, CLAIM_START_TIME);
        return data;
    }
}
