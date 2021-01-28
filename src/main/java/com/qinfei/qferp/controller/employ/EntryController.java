package com.qinfei.qferp.controller.employ;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.qinfei.qferp.service.employ.IEmployEntryEducationService;
import com.qinfei.qferp.service.employ.IEmployEntryFamilyService;
import com.qinfei.qferp.service.employ.IEmployResourceService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.qferp.entity.employ.EmployEntry;
import com.qinfei.qferp.entity.employ.EmployEntryEducation;
import com.qinfei.qferp.entity.employ.EmployEntryExperience;
import com.qinfei.qferp.entity.employ.EmployEntryFamily;
import com.qinfei.qferp.service.employ.IEmployEntryService;

import io.swagger.annotations.ApiOperation;

/**
 * 入职申请的信息录入和查询；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/27 0027 10:25；
 */
@Controller
@RequestMapping(value = "/entry")
class EntryController {
    // 入职申请的业务接口；
    @Autowired
    private IEmployEntryService entryService;
    @Autowired
    private IEmployEntryEducationService employEntryEducationService;
    @Autowired
    private IEmployResourceService employResourceService;
    @Autowired
    private IEmployEntryFamilyService employEntryFamilyService;

    /**
     * 文件上传；
     *
     * @param multipartFile：上传的文件对象；
     * @return ：处理结果；
     */
    @RequestMapping("upload")
    @ApiOperation(value = "文件上传", notes = "上传文件到服务器")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "上传文件到服务器")
    @ResponseBody
    public ResponseData upload(@RequestParam("file") MultipartFile multipartFile) {
        ResponseData data = ResponseData.ok();
        entryService.uploadFile(data, multipartFile);
        return data;
    }

    /**
     * 保存入职申请的基本信息；
     *
     * @param entry：提交的申请字符串；
     * @return ：处理结果；
     */
    @RequestMapping("saveBasic")
    @ApiOperation(value = "提交入职申请", notes = "保存入职申请的基本信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "保存入职申请的基本信息")
    @ResponseBody
    public ResponseData saveBasic(@RequestParam String entry) {
        ResponseData data = ResponseData.ok();
        entryService.saveBasic(data, entry);
        return data;
    }

    /**
     * 保存入职申请的家庭婚姻信息；
     *
     * @param entryFamily：提交的家庭信息对象；
     * @return ：处理结果；
     */
    @RequestMapping("saveFamily")
    @ApiOperation(value = "提交入职申请", notes = "保存入职申请的家庭婚姻信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "保存入职申请的家庭婚姻信息")
    @ResponseBody
    public ResponseData saveFamily(EmployEntryFamily entryFamily) {
        ResponseData data = ResponseData.ok();
        entryService.saveFamily(data, entryFamily);
        return data;
    }


    /**
     * 保存入职申请的家庭成员信息；
     */
    @RequestMapping("saveFamilyInJob")
    @ApiOperation(value = "员工查询补全", notes = "保存入职申请的家庭成员信息")
//    @Log(opType = OperateType.ADD, module = "员工查询补全", note = "保存入职申请的家庭成员信息")
    @ResponseBody
    public ResponseData saveFamilyInJob(EmployEntryFamily entryFamily) {
        ResponseData data = ResponseData.ok();
        employEntryFamilyService.saveOrUpdateAfterInjob(entryFamily);
        return data;
    }

    /**
     * 保存入职申请的教育培训经历信息；
     *
     * @param entryEducation：提交的教育培训经历信息对象；
     * @return ：处理结果；
     */
    @RequestMapping("saveEducation")
    @ApiOperation(value = "提交入职申请", notes = "保存入职申请的教育培训经历信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "保存入职申请的教育培训经历信息")
    @ResponseBody
    public ResponseData saveEducation(EmployEntryEducation entryEducation) {
        ResponseData data = ResponseData.ok();
        entryService.saveEducation(data, entryEducation);
        return data;
    }

    /**
     * 保存入职申请的教育培训经历信息；
     *
     * @param entryEducation：提交的教育培训经历信息对象；
     * @return ：处理结果；
     */
    @RequestMapping("saveEducationInJob")
    @ApiOperation(value = "员工查询补全", notes = "保存入职申请的教育培训经历信息")
//    @Log(opType = OperateType.ADD, module = "员工查询补全", note = "保存入职申请的教育培训经历信息")
    @ResponseBody
    public ResponseData saveEducationInJob(EmployEntryEducation entryEducation) {
        ResponseData data = ResponseData.ok();
        employEntryEducationService.saveOrUpdateAfterInjob(entryEducation);
        return data;
    }

    /**
     * 保存入职申请的工作经历信息；
     *
     * @param entryExperience：提交的工作经历信息对象；
     * @return ：处理结果；
     */
    @RequestMapping("saveExperienceInJob")
    @ApiOperation(value = "员工查询补全", notes = "保存工作经历信息")
//    @Log(opType = OperateType.ADD, module = "员工查询补全", note = "保存工作经历信息")
    @ResponseBody
    public ResponseData saveExperienceInJob(EmployEntryExperience entryExperience) {
        ResponseData data = ResponseData.ok();
        entryService.saveExperienceInJob(data, entryExperience);
        return data;
    }

    /**
     * 保存入职申请的工作经历信息；
     *
     * @param entryExperience：提交的工作经历信息对象；
     * @return ：处理结果；
     */
    @RequestMapping("saveExperience")
    @ApiOperation(value = "提交入职申请", notes = "保存入职申请的工作经历信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "保存入职申请的工作经历信息")
    @ResponseBody
    public ResponseData saveExperience(EmployEntryExperience entryExperience) {
        ResponseData data = ResponseData.ok();
        entryService.saveExperience(data, entryExperience);
        return data;
    }

    /**
     * 保存入职申请的其他入职信息；
     *
     * @param employEntry：提交的其他入职信息对象；
     * @param empRelativeName：推荐人姓名；
     * @param empRelativePhone：推荐人联系电话；
     * @param empRelativeRelation：与推荐人的关系；
     * @return ：处理结果；
     */
    @RequestMapping("saveOther")
    @ApiOperation(value = "提交入职申请", notes = "保存入职申请的其他入职信息")
//    @Log(opType = OperateType.ADD, module = "入职申请管理", note = "保存入职申请的其他入职信息")
    @ResponseBody
    public ResponseData saveOther(EmployEntry employEntry, Integer empRelative, String empRelativeName, String empRelativePhone, String empRelativeRelation) {
        ResponseData data = ResponseData.ok();
        entryService.saveOther(data, employEntry, empRelative, empRelativeName, empRelativePhone, empRelativeRelation);
        return data;
    }

    /**
     * 删除入职申请的家庭婚姻信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param famId：提交的家庭信息对象ID；
     * @return ：处理结果；
     */
    @RequestMapping("deleteFamily")
    @ApiOperation(value = "更新入职申请", notes = "删除入职申请的家庭婚姻信息")
//    @Log(opType = OperateType.DELETE, module = "入职申请管理", note = "删除入职申请的家庭婚姻信息")
    @ResponseBody
    public ResponseData deleteFamily(@RequestParam Integer entryId, @RequestParam Integer famId) {
        ResponseData data = ResponseData.ok();
        entryService.deleteFamily(entryId, famId);
        data.putDataValue("message", "处理完毕。");
        return data;
    }

    /**
     * 删除入职申请的教育培训经历信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param eduId：提交的教育培训经历信息ID；
     * @return ：处理结果；
     */
    @RequestMapping("deleteEducation")
    @ApiOperation(value = "更新入职申请", notes = "删除入职申请的教育培训经历信息")
//    @Log(opType = OperateType.DELETE, module = "入职申请管理", note = "删除入职申请的教育培训经历信息")
    @ResponseBody
    public ResponseData deleteEducation(@RequestParam Integer entryId, @RequestParam Integer eduId) {
        ResponseData data = ResponseData.ok();
        entryService.deleteEducation(entryId, eduId);
        data.putDataValue("message", "处理完毕。");
        return data;
    }

    /**
     * 删除入职申请的工作经历信息；
     *
     * @param entryId：提交的入职申请ID；
     * @param expId：提交的工作经历信息ID；
     * @return ：处理结果；
     */
    @RequestMapping("deleteExperience")
    @ApiOperation(value = "更新入职申请", notes = "删除入职申请的工作经历信息")
//    @Log(opType = OperateType.DELETE, module = "入职申请管理", note = "删除入职申请的工作经历信息")
    @ResponseBody
    public ResponseData deleteExperience(@RequestParam Integer entryId, @RequestParam Integer expId) {
        ResponseData data = ResponseData.ok();
        entryService.deleteExperience(entryId, expId);
        data.putDataValue("message", "处理完毕。");
        return data;
    }

    /**
     * 设置入职申请的教育培训经历信息的最高学历；
     *
     * @param entryId：提交的入职申请ID；
     * @param eduId：提交的教育培训经历信息ID；
     * @param eduCollege：提交的教育培训经历学校名称；
     * @param eduMajor：提交的教育培训经历专业名称；
     * @return ：处理结果；
     */
    @RequestMapping("setEducationHighest")
    @ApiOperation(value = "更新入职申请", notes = "设置入职申请的教育培训经历信息的最高学历")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "设置入职申请的教育培训经历信息的最高学历")
    @ResponseBody
    public ResponseData setEducationHighest(@RequestParam Integer entryId, @RequestParam Integer eduId, @RequestParam String eduCollege, @RequestParam String eduMajor) {
        ResponseData data = ResponseData.ok();
        entryService.setEducationHighest(data, entryId, eduId, eduCollege, eduMajor);
        data.putDataValue("message", "处理完毕。");
        return data;
    }

    /**
     * 上传入职申请的各项材料，完成入职申请；
     *
     * @param entry：提交的申请字符串；
     * @return ：处理结果；
     */
    @RequestMapping("completeEntry")
    @ApiOperation(value = "更新入职申请", notes = "上传入职申请的各项材料，完成入职申请")
//    @Log(opType = OperateType.UPDATE, module = "入职申请管理", note = "上传入职申请的各项材料，完成入职申请")
    @ResponseBody
    public ResponseData completeEntry(@RequestParam String entry) {
        ResponseData data = ResponseData.ok();
        // 解析对象；
        JSONObject json = JSON.parseObject(entry);
        // 处理数据；
        data.putDataValue("message", entryService.completeEntry(json));
        return data;
    }

    /**
     * 根据入职申请的身份证号码查询相关的入职信息是否已存在；
     *
     * @param entryId：主键ID；
     * @param empCode：身份证号码；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    @RequestMapping("checkInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请的身份证号码查询相关的入职信息是否已存在")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请的身份证号码查询相关的入职信息是否已存在")
    @ResponseBody
    public boolean checkInfo(Integer entryId, @RequestParam String empCode, @RequestParam String entryCompanyCode) {
        return entryService.checkRepeatByCode(entryId, empCode, entryCompanyCode);
    }

    /**
     * 根据入职申请的推荐人姓名和联系电话查询相关的推荐人是否存在；
     *
     * @param empRelativeName：推荐人姓名；
     * @param empRelativePhone：推荐人联系电话；
     * @return ：查询结果，true为验证通过，false为已存在；
     */
    @RequestMapping("checkRelative")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请的推荐人姓名和联系电话查询相关的推荐人是否存在")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请的推荐人姓名和联系电话查询相关的推荐人是否存在")
    @ResponseBody
    public boolean checkRelative(@RequestParam String empRelativeName, @RequestParam String empRelativePhone) {
        return entryService.selectRelative(empRelativeName, empRelativePhone) == null;
    }

    /**
     * 根据入职申请的提供的查询码查询入职申请信息；
     *
     * @param code：查询码；
     * @return ：入职申请信息；
     */
    @RequestMapping("getEntryInfo")
    @ApiOperation(value = "查询入职申请", notes = "根根据入职申请的提供的查询码查询入职申请信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请的提供的查询码查询入职申请信息")
    @ResponseBody
    public ResponseData getEntryInfo(@RequestParam String code) {
        ResponseData data = ResponseData.ok();
        Map<String, Object> params = new HashMap<>();
        params.put("entryValidate", code);
        data.putDataValue("entry", entryService.getEntryInfo(params));
        return data;
    }

    /**
     * 根据入职申请ID查询家庭成员信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请信息；
     */
    @RequestMapping("getFamilyInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请ID查询家庭成员信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请ID查询家庭成员信息")
    @ResponseBody
    public ResponseData getFamilyInfo(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("family", entryService.selectFamily(entryId));
        return data;
    }

    /**
     * 根据入职申请ID查询教育培训信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请信息；
     */
    @RequestMapping("getEducationInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请ID查询教育培训信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请ID查询教育培训信息")
    @ResponseBody
    public ResponseData getEducationInfo(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("education", entryService.selectEducation(entryId));
        return data;
    }

    /**
     * 根据入职申请ID查询工作经历信息；
     *
     * @param entryId：入职申请ID；
     * @return ：入职申请信息；
     */
    @RequestMapping("getExperienceInfo")
    @ApiOperation(value = "查询入职申请", notes = "根据入职申请ID查询工作经历信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据入职申请ID查询工作经历信息")
    @ResponseBody
    public ResponseData getExperienceInfo(@RequestParam Integer entryId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("experience", entryService.selectExperience(entryId));
        return data;
    }

    /**
     * 根据提供的姓名、身份证号码、联系电话找回查询码；
     *
     * @param request：请求对象；
     * @param name：姓名；
     * @param phone：联系电话；
     * @param identity：身份证号码；
     * @param code：验证码；
     * @return ：入职申请的查询码；
     */
    @RequestMapping("getEntryValidate")
    @ApiOperation(value = "查询入职申请", notes = "根据提供的姓名、身份证号码、联系电话找回查询码")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "根据提供的姓名、身份证号码、联系电话找回查询码")
    @ResponseBody
    public ResponseData getEntryValidate(HttpServletRequest request, @RequestParam String name, @RequestParam String phone, @RequestParam String identity, @RequestParam String code) {
        ResponseData data = ResponseData.ok();
        Object verifyCode = request.getSession().getAttribute("verifyCode");
        if (verifyCode == null) {
            data.putDataValue("message", "页面信息已过期，请刷新页面重新提交。");
        } else {
            if (verifyCode.toString().equalsIgnoreCase(code)) {
                String entryValidate = entryService.selectEntryValidate(name, phone, identity);
                if (StringUtils.isEmpty(entryValidate)) {
                    data.putDataValue("message", "查询无结果，请确认信息已正确输入。");
                } else {
                    data.putDataValue("entryValidate", entryValidate);
                }
            } else {
                data.putDataValue("message", "验证码错误。");
            }
        }
        return data;
    }

    /**
     * 获取民族信息；
     *
     * @return ：民族信息；
     */
    @RequestMapping("getNation")
    @ApiOperation(value = "查询入职申请", notes = "获取民族信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "获取民族信息")
    @ResponseBody
    public ResponseData getNation() {
        ResponseData data = ResponseData.ok();
        data.putDataValue("nation", entryService.listNation());
        return data;
    }

    /**
     * 获取区域信息；
     *
     * @param areaId：上级区域ID；
     * @return ：区域信息；
     */
    @RequestMapping("getDistrict")
    @ApiOperation(value = "查询入职申请", notes = "获取区域信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "获取区域信息")
    @ResponseBody
    public ResponseData getDistrict(@RequestParam(required = false) Integer areaId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("district", entryService.listDistrict(areaId));
        return data;
    }

    /**
     * 获取部门信息；
     *
     * @return ：民族信息；
     */
    @RequestMapping("getDept")
    @ApiOperation(value = "查询入职申请", notes = "获取部门信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "获取部门信息")
    @ResponseBody
    public ResponseData getDept() {
        ResponseData data = ResponseData.ok();
        data.putDataValue("dept", entryService.listDept());
        return data;
    }

    /**
     * 获取部门下的职位信息；
     *
     * @param deptId：上级区域ID；
     * @return ：区域信息；
     */
    @RequestMapping("getPost")
    @ApiOperation(value = "查询入职申请", notes = "获取部门下的职位信息")
//    @Log(opType = OperateType.QUERY, module = "入职申请管理", note = "获取部门下的职位信息")
    @ResponseBody
    public ResponseData getPost(@RequestParam Integer deptId) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("post", entryService.listPost(deptId));
        return data;
    }

    /**
     * 获取公司下的职位信息；
     * @return ：区域信息；
     */
    @RequestMapping("getPostByCompanyCode")
    @ResponseBody
    public ResponseData getPostByCompanyCode() {
        ResponseData data = ResponseData.ok();
        data.putDataValue("post", entryService.listPostByCompanyCode());
        return data;
    }

    @RequestMapping("listPostByCompanyAndDept")
    @ApiOperation(value = "查询入职申请", notes = "获取部门下的职位信息")
    @ResponseBody
    public ResponseData getPost(@RequestParam String companyCode, @RequestParam Integer firstDept) {
        ResponseData data = ResponseData.ok();
        data.putDataValue("post", employResourceService.listPost(companyCode, firstDept));
        return data;
    }

    @GetMapping("family/{entryId}")
    @ResponseBody
    public PageInfo<EmployEntryFamily> familyInfo(@PathVariable int entryId, @PageableDefault Pageable pageable) {
        return entryService.familyInfo(entryId, pageable);
    }

    @GetMapping("education/{empId}")
    @ResponseBody
    public PageInfo<Map<String, Object>> educationInfo(@PathVariable long empId, @PageableDefault Pageable pageable) {
        return entryService.educationInfo(empId, pageable);
    }

    @GetMapping("experience/{empId}")
    @ResponseBody
    public PageInfo<Map<String, Object>> experienceInfo(@PathVariable long empId, @PageableDefault Pageable pageable) {
        return entryService.experienceInfo(empId, pageable);
    }
}