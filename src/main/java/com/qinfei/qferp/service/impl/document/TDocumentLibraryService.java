package com.qinfei.qferp.service.impl.document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.document.TDocumentLibrary;
import com.qinfei.qferp.entity.document.TDocumentPermission;
import com.qinfei.qferp.entity.document.TDocumentPermissionDetails;
import com.qinfei.qferp.entity.document.TDocumentType;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.document.TDocumentLibraryMapper;
import com.qinfei.qferp.mapper.study.TrainCourseMapper;

import com.qinfei.qferp.service.document.ITDocumentLibraryService;

import com.qinfei.qferp.utils.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class TDocumentLibraryService implements ITDocumentLibraryService {
    @Autowired
    private TDocumentLibraryMapper documentLibraryMapper;
    @Autowired
    private TrainCourseMapper trainCourseMapper;

    private List<TDocumentType> typeList;
    @Autowired
    private Config config;
    @Override
    public JSONArray libraryType(){
        JSONArray typeJson;
        Map<String,Object> map = new HashMap<>();
        map.put("companyCode", AppUtil.getUser().getCompanyCode());
        List<TDocumentType> libraryTypeJson=  documentLibraryMapper.libraryType(map);
        typeJson = jsonListX(libraryTypeJson);
        return typeJson;
    }

    public JSONArray jsonListX(List<TDocumentType> paramList) {
        JSONArray array = new JSONArray();
        this.typeList = paramList;
        for (TDocumentType x : paramList) {
            JSONObject obj = new JSONObject();
            obj.put("id", x.getId());
            obj.put("text", x.getTypeName());
            obj.put("level", x.getLevel());
            obj.put("parentId", x.getParentId());
            obj.put("nodes", treeChild(x.getId()));
            array.add(obj);
        }
        return array;
    }

    private JSONArray treeChild(int id) {
        JSONArray arrays = new JSONArray();
        for (TDocumentType a : typeList) {
            JSONObject childObj = new JSONObject();
            if (a.getParentId() == id) {
                childObj.put("id", a.getId());
                childObj.put("text", a.getTypeName());
                childObj.put("level", a.getLevel());
                childObj.put("parentId", a.getParentId());
                childObj.put("nodes", treeChild(a.getId()));
                arrays.add(childObj);
            }
        }
        return arrays;
    }

    @Override
    public int getLibraryTotal(Map<String,Object> map){
        int result = 0;
        User user = AppUtil.getUser();
        String[] userRole = documentLibraryMapper.selectRole(user.getId()).split(",");
        map.put("companyCode",user.getCompanyCode());

        //获取制度管理员
//        List<String> su=SysConfigUtils.getConfigValue("systemUser",List.class);
        boolean su =false ;
        if (userRole.length>0){
            for (String r : userRole){
                if ("ZDZY".equals(r)){
                    su = true;
                }
            }
        }
        if (su){
            map.put("admin",1);
            //根据map里的条件，查出所有
            List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
            result= libraryList.size();
        }else {
            if (user.getHandoverState() == 1 || user.getState()==-9){
                return 0;
            }
            //根据map里的条件，查出所有
            List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
            List<TDocumentLibrary> libraryList1 = new ArrayList<>();
            //进行权限过滤
            if(CollectionUtils.isNotEmpty(libraryList)){
                Integer WorkAge = documentLibraryMapper.selectWorkAge(user.getId());
                for (TDocumentLibrary tDocumentLibrary : libraryList){
                    Integer flag= tDocumentLibrary.getWorkAgeFlag();
                    Integer min = tDocumentLibrary.getWorkAgeMin();
                    Integer max = tDocumentLibrary.getWorkAgeMax();
                    String randId = tDocumentLibrary.getRandId();
                    String f = tDocumentLibrary.getEnrollFlag();
                    if (!StringUtils.isEmpty(randId)){
                        for (String role : userRole){
                            if (tDocumentLibrary.getLibraryEnrollFlag()==1) {
                                if ((randId.contains(user.getId().toString()) && "4".equals(f) )||
                                        randId.contains(user.getDeptId().toString()) ||
                                        randId.contains(role) ||
                                        (!randId.contains(user.getId().toString())&& "3".equals(f)) ||
                                        ((min!=null && max!=null && WorkAge !=null) && min < WorkAge && WorkAge<max )  ||
                                        ((min!=null && max==null && WorkAge !=null) && min < WorkAge) ||
                                        ((min==null && max!=null && WorkAge !=null) && max > WorkAge) ){
                                    libraryList1.add(tDocumentLibrary);
                                }
                            }
                        }
                    }
                    else if(tDocumentLibrary.getLibraryEnrollFlag()==0) {
                            libraryList1.add(tDocumentLibrary);
                        }else if ( StringUtils.isEmpty(randId) && flag !=null ){
                            if (((min!=null && max!=null && WorkAge !=null) && min < WorkAge && WorkAge<max )  ||
                                    ((min!=null && max==null && WorkAge !=null) && min < WorkAge) ||
                                    ((min==null && max!=null && WorkAge !=null) && max > WorkAge)){
                                libraryList1.add(tDocumentLibrary);
                            }
                        }

                }
            }
            result= libraryList1.size();
        }

        return  result;

    }

    @Override
    public Map<String,Object> list(Map<String, Object> map) {
        User user = AppUtil.getUser();
        Map map1 = new HashMap();
        map.put("companyCode",user.getCompanyCode());
        String[] userRole = documentLibraryMapper.selectRole(user.getId()).split(",");
        //根据map里的条件，查出所有
        //获取制度管理员
//        List<String> su=SysConfigUtils.getConfigValue("systemUser",List.class);
        boolean su =false ;
        if (userRole.length>0){
            for (String r : userRole){
                if ("ZDZY".equals(r)){
                    su = true;
                }
            }
        }
        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        if (su){
            map.put("admin",1);
            List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
            PageInfo<TDocumentLibrary> pageInfo = new PageInfo<TDocumentLibrary>(libraryList);
            map1.put("code",0);
            map1.put("msg","ok");
            map1.put("count",pageInfo.getTotal());
            map1.put("data",libraryList);
            return map1;
        }else {
            if (user.getHandoverState() == 1 || user.getState()==-9){
                map1.put("code",0);
                map1.put("msg","ok");
                map1.put("count","");
                map1.put("data","");
                return map1;
            }else {
                List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
                List<TDocumentLibrary> libraryList1 = new ArrayList<>();
                Integer WorkAge = documentLibraryMapper.selectWorkAge(user.getId());
                //进行权限过滤
                if(CollectionUtils.isNotEmpty(libraryList)){
                    for (TDocumentLibrary tDocumentLibrary : libraryList){
                        String randId = tDocumentLibrary.getRandId();
                        String f = tDocumentLibrary.getEnrollFlag();

                        Integer flag= tDocumentLibrary.getWorkAgeFlag();
                        Integer min = tDocumentLibrary.getWorkAgeMin();
                        Integer max = tDocumentLibrary.getWorkAgeMax();

                        if (!StringUtils.isEmpty(randId)){
                            for (String role : userRole){
                                if (tDocumentLibrary.getLibraryEnrollFlag()==1) {
                                    if ((randId.contains(user.getId().toString()) && "4".equals(f) )||
                                            randId.contains(user.getDeptId().toString()) ||
                                            randId.contains(role) ||
                                            (!randId.contains(user.getId().toString())&& "3".equals(f)) ||
                                            ((min!=null && max!=null && WorkAge !=null) && min < WorkAge && WorkAge<max )  ||
                                            ((min!=null && max==null && WorkAge !=null) && min < WorkAge) ||
                                            ((min==null && max!=null && WorkAge !=null) && max > WorkAge) ){
                                        libraryList1.add(tDocumentLibrary);
                                    }
                                }
                            }
                        }else if(tDocumentLibrary.getLibraryEnrollFlag()==0) {
                            libraryList1.add(tDocumentLibrary);
                        }else if ( StringUtils.isEmpty(randId) && flag !=null ){
                            if (((min!=null && max!=null && WorkAge !=null) && min < WorkAge && WorkAge<max )  ||
                                    ((min!=null && max==null && WorkAge !=null) && min < WorkAge) ||
                                    ((min==null && max!=null && WorkAge !=null) && max > WorkAge)){
                                libraryList1.add(tDocumentLibrary);
                            }
                        }

                    }
                }
                map1.put("code",0);
                map1.put("msg","ok");
                map1.put("count",libraryList1.size());
                map1.put("data",libraryList1);
                return map1;
            }

        }

    }

    public PageInfo<TDocumentLibrary> selectLibraryList(Map<String, Object> map, Pageable pageable){
        User user = AppUtil.getUser();
        map.put("companyCode",user.getCompanyCode());
        String[] userRole = documentLibraryMapper.selectRole(user.getId()).split(",");

        //获取制度管理员
//        List<String> su=SysConfigUtils.getConfigValue("systemUser",List.class);
        boolean su =false ;
        if (userRole.length>0){
            for (String r : userRole){
                if ("ZDZY".equals(r)){
                    su = true;
                }
            }
        }
        Integer WorkAge = documentLibraryMapper.selectWorkAge(user.getId());
        //根据map里的条件，查出所有
        PageHelper.startPage(pageable.getPageNumber(), pageable.getPageSize());
        if (su){
            map.put("admin",1);
            List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
            return new PageInfo<>(libraryList);
        }else {
            if (user.getHandoverState() == 1 || user.getState()==-9){
                return new PageInfo<>();
            }

            List<TDocumentLibrary> libraryList = documentLibraryMapper.selectLibraryList(map);
            List<TDocumentLibrary> libraryList1 = new ArrayList<>();

            //进行权限过滤
            if(CollectionUtils.isNotEmpty(libraryList)){
                for (TDocumentLibrary tDocumentLibrary : libraryList){
                    Integer flag= tDocumentLibrary.getWorkAgeFlag();
                    Integer min = tDocumentLibrary.getWorkAgeMin();
                    Integer max = tDocumentLibrary.getWorkAgeMax();
                    String randId = tDocumentLibrary.getRandId();
                    String f = tDocumentLibrary.getEnrollFlag();
                    if (!StringUtils.isEmpty(randId)){
                        for (String role : userRole) {
                            if (tDocumentLibrary.getLibraryEnrollFlag() == 1) {
                                if ((randId.contains(user.getId().toString()) && "4".equals(f)) ||
                                        randId.contains(user.getDeptId().toString()) ||
                                        randId.contains(role) ||
                                        (!randId.contains(user.getId().toString()) && "3".equals(f)) ||
                                        ((min != null && max != null && WorkAge != null) && min < WorkAge && WorkAge < max) ||
                                        ((min != null && max == null && WorkAge != null) && min < WorkAge) ||
                                        ((min == null && max != null && WorkAge != null) && max > WorkAge)) {
                                    libraryList1.add(tDocumentLibrary);
                                }
                            }
                        }
                    }
                   else if(tDocumentLibrary.getLibraryEnrollFlag()==0) {
                            libraryList1.add(tDocumentLibrary);
                        }else if ( StringUtils.isEmpty(randId) && flag !=null ){
                            if (((min!=null && max!=null && WorkAge !=null) && min < WorkAge && WorkAge<max )  ||
                                    ((min!=null && max==null && WorkAge !=null) && min < WorkAge) ||
                                    ((min==null && max!=null && WorkAge !=null) && max > WorkAge)){
                                libraryList1.add(tDocumentLibrary);
                            }
                        }
                    }

            }
            return new PageInfo<>(libraryList1);
        }

    }

    @Override
    public List<TDocumentLibrary> getDocumentLibraryList(Map map) {
        User user = AppUtil.getUser();
        map.put("companyCode",user.getCompanyCode());
        return documentLibraryMapper.selectLibraryList(map);
    }

    @Override
    public ResponseData releaseUser(){
        try{
            ResponseData data = ResponseData.ok();
            Map<String,Object> map = new HashMap<>();
            map.put("companyCode",AppUtil.getUser().getCompanyCode());
            List<User> userList = documentLibraryMapper.relaseUser(map);
            data.putDataValue("userList",userList);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    /**
     * 新增制度
     * @param tDocumentLibrary
     * @return
     */
    @Override
    public ResponseData addLibrary(TDocumentLibrary tDocumentLibrary,MultipartFile[] files){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            TDocumentPermission tDocumentPermission = new TDocumentPermission();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            if (StringUtils.isEmpty(tDocumentLibrary.getEffectiveStartTime())){
               tDocumentLibrary.setEffectiveStartTime(new Date());
            }
            if (tDocumentLibrary.getBtnState() == 0){
                //提交按钮  删除原来已阅用户
                documentLibraryMapper.updateReading(tDocumentLibrary.getId());
            }
            String code ="";
            String fl=tDocumentLibrary.getFlag();
            if (!StringUtils.isEmpty(fl)){
                String[] fls = fl.split(",");
                for (String d :fls){
                    if ("1".equals(d)){
                        code +="/PD";
                    }else if ("2".equals(d)){
                        code +="/PR";
                    }else if ("5".equals(d) && tDocumentLibrary.getWorkAgeFlag()==0){
                        code +="/PTY";
                    }else if ("5".equals(d) && tDocumentLibrary.getWorkAgeFlag()==1){
                        code +="/PTM";
                    }else if ("3".equals(d)){
                        code +="/BL";
                    }else if ("4".equals(d)){
                        code +="/WL";
                    }
                }
            }else {
                code+="PP";
            }
            //将生效时间进行转换成yyyyMMDD 用于制度权限编号组成
            String timeState = dateFormat.format(tDocumentLibrary.getEffectiveStartTime()).replaceAll("\\/","");
            String timeEnd="";
            if (!StringUtils.isEmpty(tDocumentLibrary.getEffectiveEndTime())){
                timeEnd = dateFormat.format(tDocumentLibrary.getEffectiveEndTime()).replaceAll("\\/","");
            }
            //制度权限编号code
            tDocumentPermission.setCode(user.getCompanyCode()+"-"+timeState+timeEnd+"-"+code+"-"+tDocumentLibrary.getLevel()+"-"+tDocumentLibrary.getVersion());
            tDocumentPermission.setCompanyCode(user.getCompanyCode());
            tDocumentPermission.setCreateId(user.getId());
            tDocumentPermission.setCreateTime(new Date());
            //新增制度权限表
            documentLibraryMapper.addPermission(tDocumentPermission);
            tDocumentLibrary.setPermissionId(tDocumentPermission.getId());
            tDocumentLibrary.setReleaseTime(new Date());
            tDocumentLibrary.setCreateId(user.getId());
            tDocumentLibrary.setCreateTime(new Date());
            tDocumentLibrary.setUpdateTime(new Date());
            tDocumentLibrary.setCompanyCode(user.getCompanyCode());

                List<String> fileNames = new ArrayList<>();
                List<String> filePaths = new ArrayList<>();
                if (files.length>1){
                    for (MultipartFile multipartFile:  files){
                        if (multipartFile.getSize()>0){
                            String temp = multipartFile.getOriginalFilename();
                            String ext = null;
                            if (temp.indexOf(".") > -1) {
                                ext = temp.substring(temp.lastIndexOf("."));
                                String fileName = UUIDUtil.get32UUID() + ext;
                                String childPath =getStringData()+ "/document/documentLibrary/";
                                File destFile = new File(config.getUploadDir() + childPath + fileName);
                                if (!destFile.getParentFile().exists()) {
                                    destFile.getParentFile().mkdirs();
                                }
                                multipartFile.transferTo(destFile);
                                fileNames.add(multipartFile.getOriginalFilename());
                                filePaths.add(config.getWebDir() + childPath + fileName);

                            }
                        }
                        tDocumentLibrary.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                        tDocumentLibrary.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                    }
                }else {
                    MultipartFile multipartFile = files[0];
                    if (multipartFile.getSize()>0){
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/document/documentLibrary/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        fileNames.add(multipartFile.getOriginalFilename());
                        filePaths.add(config.getWebDir() + childPath + fileName);
                        tDocumentLibrary.setAttachment(fileNames.toString().replaceAll("\\[|\\]", ""));
                        tDocumentLibrary.setAttachmentLink(filePaths.toString().replaceAll("\\[|\\]", ""));
                    }else {
                        if (tDocumentLibrary.getId() != null){
                            TDocumentLibrary old = documentLibraryMapper.getById(tDocumentLibrary.getId());
                            tDocumentLibrary.setAttachment(old.getAttachment());
                            tDocumentLibrary.setAttachmentLink(old.getAttachmentLink());
                        }

                    }
                }
            List<TDocumentPermissionDetails> tDocumentPermissionDetailsList = new ArrayList<>();
            //将制度中的司龄（年）进行转换成月。
            if (tDocumentLibrary.getWorkAgeMax()!=null && tDocumentLibrary.getWorkAgeMin()!=null && tDocumentLibrary.getWorkAgeFlag()==0 && tDocumentLibrary.getFlag().contains("5")){
                tDocumentLibrary.setWorkAgeMax(tDocumentLibrary.getWorkAgeMax() * 12);
                tDocumentLibrary.setWorkAgeMin(tDocumentLibrary.getWorkAgeMin() * 12);
            }else if ((tDocumentLibrary.getWorkAgeMax()==null && tDocumentLibrary.getWorkAgeMin()==null) || !tDocumentLibrary.getFlag().contains("5")){
                tDocumentLibrary.setWorkAgeFlag(null);
                tDocumentLibrary.setWorkAgeMax(null);
                tDocumentLibrary.setWorkAgeMin(null);
            }
            if (!StringUtils.isEmpty(tDocumentLibrary.getFlag())){
                //当制度权限类型不为空时存制度表类型为1
                tDocumentLibrary.setLibraryEnrollFlag(1);
                if (tDocumentLibrary.getId()!=null){
                        //编辑提交 要进行校验
                        if (tDocumentLibrary.getBtnState() == 0) {
                            if (judgeLibraryCode(tDocumentLibrary.getLibraryCode(),tDocumentLibrary.getVersion())) {
                                //存在制度编号相同  则替换。
                                selectLCode(tDocumentLibrary.getLibraryCode());
                                if (tDocumentLibrary.getBtnState() == 0) {
                                    tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                                }

                                //编辑要更新权限
                                documentLibraryMapper.updataDetails(tDocumentLibrary.getId());

                                documentLibraryMapper.updateLibraryList(tDocumentLibrary);
                            }
                        } else {
                            if (tDocumentLibrary.getBtnState() == 0) {
                                tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                            }
                            //编辑要更新权限
                            documentLibraryMapper.updataDetails(tDocumentLibrary.getId());
                            //暂存
                            documentLibraryMapper.updateLibraryList(tDocumentLibrary);
                        }

                }else {
                        if (tDocumentLibrary.getBtnState()==0){
                            if (judgeLibraryCode(tDocumentLibrary.getLibraryCode(),tDocumentLibrary.getVersion())) {
                                //存在制度编号相同  则替换。
                                selectLCode(tDocumentLibrary.getLibraryCode());
                            }
                        }
                        tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                        documentLibraryMapper.addLibrary(tDocumentLibrary);

                }
                //获取制度权限值（角色 用户（黑名单、白名单） 部门）
               Map<String,Object> doucumentList = JSON.parseObject(tDocumentLibrary.getDocumentPermissionDetailsList(), Map.class);;
                    for(String key : doucumentList.keySet()){
                        //当权限为角色  并且 包含 2  为制度权限类型
                        if (key.equals("role") && tDocumentLibrary.getFlag().contains("2")){
                            String value = doucumentList.get(key).toString().replaceAll("\"","");
                            String[] rangeId = value.split(",|\\[|\\]" );
                            for (String range : rangeId){
                                if (!StringUtils.isEmpty(range)){
                                    TDocumentPermissionDetails tDocumentPermissionDetails = new TDocumentPermissionDetails();
                                    tDocumentPermissionDetails.setPermissionId(tDocumentPermission.getId());
                                    tDocumentPermissionDetails.setLibraryEnrollFlag(2);
                                    tDocumentPermissionDetails.setRangeId(range);
                                    tDocumentPermissionDetails.setLibraryId(tDocumentLibrary.getId());
                                    tDocumentPermissionDetails.setCreateTime(new Date());
                                    tDocumentPermissionDetails.setUpdateTime(new Date());
                                    tDocumentPermissionDetails.setCreateId(user.getId());
                                    tDocumentPermissionDetails.setUpdateId(user.getId());
                                    tDocumentPermissionDetailsList.add(tDocumentPermissionDetails);
                                }
                            }
                        }
                        //当权限为部门  并且 包含 1  为制度权限类型
                        if (key.equals("dept") && tDocumentLibrary.getFlag().contains("1")){
                            String value = doucumentList.get(key).toString().replaceAll("\"","");
                            String[] rangeId = value.split(",|\\[|\\]" );
                            for (String range : rangeId){
                                if (!StringUtils.isEmpty(range)) {
                                    TDocumentPermissionDetails tDocumentPermissionDetails = new TDocumentPermissionDetails();
                                    tDocumentPermissionDetails.setPermissionId(tDocumentPermission.getId());
                                    tDocumentPermissionDetails.setLibraryEnrollFlag(1);
                                    tDocumentPermissionDetails.setRangeId(range);
                                    tDocumentPermissionDetails.setLibraryId(tDocumentLibrary.getId());
                                    tDocumentPermissionDetails.setCreateTime(new Date());
                                    tDocumentPermissionDetails.setUpdateTime(new Date());
                                    tDocumentPermissionDetails.setCreateId(user.getId());
                                    tDocumentPermissionDetails.setUpdateId(user.getId());
                                    tDocumentPermissionDetailsList.add(tDocumentPermissionDetails);
                                }
                            }
                        }
                        //当权限为白名单  并且 包含 2  为制度权限类型

                        if (key.equals("user") &&tDocumentLibrary.getFlag().contains("3")){
                            String value = doucumentList.get(key).toString().replaceAll("\"","");
                            String[] rangeId = value.split(",|\\[|\\]" );
                            for (String range : rangeId){
                                if (!StringUtils.isEmpty(range)) {
                                    TDocumentPermissionDetails tDocumentPermissionDetails = new TDocumentPermissionDetails();
                                    tDocumentPermissionDetails.setLibraryEnrollFlag(3);
                                    tDocumentPermissionDetails.setPermissionId(tDocumentPermission.getId());
                                    tDocumentPermissionDetails.setRangeId(range);
                                    tDocumentPermissionDetails.setLibraryId(tDocumentLibrary.getId());
                                    tDocumentPermissionDetails.setCreateTime(new Date());
                                    tDocumentPermissionDetails.setUpdateTime(new Date());
                                    tDocumentPermissionDetails.setCreateId(user.getId());
                                    tDocumentPermissionDetails.setUpdateId(user.getId());
                                    tDocumentPermissionDetailsList.add(tDocumentPermissionDetails);
                                }
                            }
                        }
                        if (key.equals("user") &&tDocumentLibrary.getFlag().contains("4")){
                            String value = doucumentList.get(key).toString().replaceAll("\"","");
                            String[] rangeId = value.split(",|\\[|\\]" );
                            for (String range : rangeId){
                                if (!StringUtils.isEmpty(range)) {
                                    TDocumentPermissionDetails tDocumentPermissionDetails = new TDocumentPermissionDetails();
                                    tDocumentPermissionDetails.setLibraryEnrollFlag(4);
                                    tDocumentPermissionDetails.setPermissionId(tDocumentPermission.getId());
                                    tDocumentPermissionDetails.setRangeId(range);
                                    tDocumentPermissionDetails.setLibraryId(tDocumentLibrary.getId());
                                    tDocumentPermissionDetails.setCreateTime(new Date());
                                    tDocumentPermissionDetails.setUpdateTime(new Date());
                                    tDocumentPermissionDetails.setCreateId(user.getId());
                                    tDocumentPermissionDetails.setUpdateId(user.getId());
                                    tDocumentPermissionDetailsList.add(tDocumentPermissionDetails);
                                }
                            }
                        }
                    }
            }else {
                tDocumentLibrary.setLibraryEnrollFlag(0);
                if(tDocumentLibrary.getId()!=null){
                        //编辑提交 要进行校验
                        if (tDocumentLibrary.getBtnState() == 0) {
                            if (judgeLibraryCode(tDocumentLibrary.getLibraryCode(),tDocumentLibrary.getVersion())) {
                                //存在制度编号相同  则替换。
                                selectLCode(tDocumentLibrary.getLibraryCode());
                                if (tDocumentLibrary.getBtnState() == 0) {
                                    tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                                }
                                documentLibraryMapper.updateLibraryList(tDocumentLibrary);
                            }
                        } else {
                            if (tDocumentLibrary.getBtnState() == 0) {
                                tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                            }
                            //暂存
                            documentLibraryMapper.updateLibraryList(tDocumentLibrary);
                        }


                //失效制度  编辑提交
                }else {
                      //制度的编号和版次 判断是否存在    和 判断制度编号是否存在。   制度判重
                        if (tDocumentLibrary.getBtnState() == 0) {
                            if (judgeLibraryCode(tDocumentLibrary.getLibraryCode(),tDocumentLibrary.getVersion())) {
                                //存在制度编号相同  则替换。
                                selectLCode(tDocumentLibrary.getLibraryCode());
                            }
                        }
                        tDocumentLibrary.setState(tDocumentLibrary.getBtnState());
                        documentLibraryMapper.addLibrary(tDocumentLibrary);
                    }
            }
            if (CollectionUtils.isNotEmpty(tDocumentPermissionDetailsList)){
                documentLibraryMapper.addLibraryDetailsList(tDocumentPermissionDetailsList);
            }
            data.putDataValue("message","操作成功");
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }


    @Override
    public List<Map<String,Object>> listpermissions(String signStr, String name){
        List<Map<String, Object>> result = new ArrayList<>();
        try{
            User user = AppUtil.getUser();
            if(user != null){
                if("role".equals(signStr)){
                    result = trainCourseMapper.listRoleByParam(name);
                }else if("dept".equals(signStr)){
                    result = trainCourseMapper.listDeptByParam(name, user.getCompanyCode());
                }else {
                    result = trainCourseMapper.listUserByParam(name, user.getCompanyCode());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public ResponseData selectLibrary(Integer id){
        try{
            ResponseData data = ResponseData.ok();
             if (id ==null ){
                throw new QinFeiException(1002,"没有获取到制度id");
            }
            TDocumentLibrary tDocumentLibrary= documentLibraryMapper.getById(id);
            Map<String,Object> map =new HashMap<>();
            map.put("1",new ArrayList<>());
            map.put("2",new ArrayList<>());
            map.put("3",new ArrayList<>());
            map.put("4",new ArrayList<>());
             //根据制度id 获取制度权限list
            List<TDocumentPermissionDetails> TdList= documentLibraryMapper.selectRangeId(id);
            if (CollectionUtils.isNotEmpty(TdList)){
                for (TDocumentPermissionDetails tDocumentPermissionDetails : TdList){
                    if (tDocumentPermissionDetails.getLibraryEnrollFlag() == 1){
                        ((List)map.get("1")).add(tDocumentPermissionDetails.getRangeId());
                    }else if (tDocumentPermissionDetails.getLibraryEnrollFlag() == 2){
                        ((List)map.get("2")).add(tDocumentPermissionDetails.getRangeId());
                    }else if (tDocumentPermissionDetails.getLibraryEnrollFlag() == 3){
                        ((List)map.get("3")).add(tDocumentPermissionDetails.getRangeId());
                    }else if (tDocumentPermissionDetails.getLibraryEnrollFlag() == 4){
                        ((List)map.get("4")).add(tDocumentPermissionDetails.getRangeId());
                    }
                }
            }
            data.putDataValue("tDocumentLibrary",tDocumentLibrary);
            data.putDataValue("selectRangId",map);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }

    }

    @Override
    public ResponseData addType(Map<String,Object> map){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            map.put("companyCode",user.getCompanyCode());
            map.put("createId",user.getId());
            map.put("updateId",user.getId());
            map.put("createTime",new Date());
            map.put("updateTime",new Date());
            documentLibraryMapper.addType(map);
            data.putDataValue("message","操作成功");
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());

        }
    }
    @Override
    public ResponseData editTypeName(Map map){
        ResponseData data= ResponseData.ok();
        User user = AppUtil.getUser();
        map.put("updateId",user.getId());
        map.put("updateTime",new Date());
        documentLibraryMapper.editType(map);
        data.putDataValue("message","操作成功");
        return data;
    }

    @Override
    public ResponseData delType(String typeId){
        ResponseData data= ResponseData.ok();
        String[] typeArr = typeId.split(",|\\[|\\]" );
        for (String t : typeArr){
            if (!StringUtils.isEmpty(t)){
                documentLibraryMapper.delType(Integer.parseInt(t));
                documentLibraryMapper.delLibrary(Integer.parseInt(t));
            }
        }
        data.putDataValue("message","操作成功");
        return data;
    }


    @Override
    public ResponseData selectTypeFlag(Map<String,Object> map){
        ResponseData data = ResponseData.ok();
        Map map1 = documentLibraryMapper.queryLibrary(map);
        data.putDataValue("map1",map1);
        return data;
    }

    @Override
    public ResponseData delLibrary(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            if (id ==null){
                throw new QinFeiException(1002,"未获取到制度id");
            }
            Integer state = IConst.STATE_DELETE;
            documentLibraryMapper.updateLibrary(id,state);
            documentLibraryMapper.updataDetails(id);
            data.putDataValue("message","删除成功");
            return data;
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"删除制度失败了哦");
        }
    }

    public void updateLibraryReady(Integer id){
        User user =AppUtil.getUser();
        //需要判断该用户是否已阅：
        Integer readyFlag = documentLibraryMapper.selectReady(id,user.getId());
        if (readyFlag==0){
            Map map= new HashMap();
            map.put("libraryId",id);
            map.put("userId",user.getId());
            map.put("createUser",user.getId());
            map.put("createTime",new Date());
            documentLibraryMapper.updateLibraryReady(map);
        }
    }

    @Override
    public TDocumentLibrary selectLibraryview(Integer id){
        return documentLibraryMapper.getById(id);
    }

    /**
     * 判断制度编号是否存在，
     * 制度编号和版次判断唯一，如果制度编号和版次已经存在，将提示出，该编号的版次存在，如果制度编号相同，则展示最新的制度。
     */

    private boolean judgeLibraryCode(String libraryCode,String version){
        Map<String,Object> map = new HashMap<>();
        map.put("libraryCode",libraryCode);
        map.put("version",version);
        List<TDocumentLibrary> tDocumentLibrary = documentLibraryMapper.selectCode(map);
        if (CollectionUtils.isNotEmpty(tDocumentLibrary)){
            throw new QinFeiException(1002,"该制度编号的版次已存在，请重新填写版次");
        }else {
            return true;
        }

    }

    private void selectLCode(String libraryCode){
        Map<String,Object> map = new HashMap<>();
        map.put("libraryCode",libraryCode);
        List<TDocumentLibrary> tDocumentLibrary = documentLibraryMapper.selectCode(map);
        if (CollectionUtils.isNotEmpty(tDocumentLibrary)){
            for (TDocumentLibrary t: tDocumentLibrary){
                documentLibraryMapper.updateLibrary(t.getId(),2);
                documentLibraryMapper.updataDetails(t.getId());
            }
        }
    }


    public ResponseData updatefailure(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            if (id ==null){
                throw new QinFeiException(1002,"未获取到制度id");
            }
            Integer state = 2;
            documentLibraryMapper.updateLibrary(id,state);
            documentLibraryMapper.updataDetails(id);
            data.putDataValue("message","操作成功");
            return data;

        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"删除制度失败了哦");
        }
    }

    @Override
    public ResponseData deleteFile(String file ,String fileLink , Integer id) {
        try {
            ResponseData data = ResponseData.ok();
            documentLibraryMapper.updateFile(file,fileLink,id);
            data.putDataValue("message","操作成功");
            return data;
        } catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"删除附件失败！");
        }
    }

    @Override
    public ResponseData CheckList(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            if (id==null){
                throw new QinFeiException(1002,"未获取到制度id");
            }
            Map<String,Object> map = new HashMap<>();
            map.put("libraryId",id);
            // 获取制度已读人员
            List<User> libraryReadyList = documentLibraryMapper.selectReadyList(id);
            List<User> libraryNotReadyList = new ArrayList<>();
            //获取该制度的权限类型
            List<TDocumentLibrary> permissionsList = documentLibraryMapper.selectpermissions(id);
            if (CollectionUtils.isNotEmpty(permissionsList)){
              for (TDocumentLibrary documentLibrary : permissionsList){
                  data.putDataValue("state",documentLibrary.getState());
                  if(documentLibrary.getState() == 0){
                      //该制度有效
                      if (documentLibrary.getLibraryEnrollFlag()==0){
                          //获取未设置权限未读人员
                          libraryNotReadyList = documentLibraryMapper.selectNotList(id,documentLibrary.getCompanyCode());
                          //未设置权限
                          map.put("permissionsFlag",0);
                      }else {
                          //设置权限
                          map.put("permissionsFlag",1);
                          if(documentLibrary.getEnrollFlag().contains("3")){
                              //黑名单
                              map.put("blackFlag",1);
                              //获取黑名单权限的未读人员
                              libraryNotReadyList = documentLibraryMapper.selectBlackNotReady(id,documentLibrary.getCompanyCode());
                          } else if (documentLibrary.getEnrollFlag().contains("4")){
                              //白名单
                              //获取白名单权限未读用户
                              libraryNotReadyList = documentLibraryMapper.selectNotReady(id,documentLibrary.getCompanyCode());
                          } else {
                              map.put("companyCode",documentLibrary.getCompanyCode());
                              Integer flag= documentLibrary.getWorkAgeFlag();
                              Integer min = documentLibrary.getWorkAgeMin();
                              Integer max = documentLibrary.getWorkAgeMax();
                              if (documentLibrary.getEnrollFlag().contains("1")){
                                  map.put("deptFlag",1);
                              }
                              if (documentLibrary.getEnrollFlag().contains("2")){
                                  map.put("roleFlag",1);
                              }
                              if (flag != null){
                                  map.put("ageFlag",1);
                                  map.put("maxAge",max);
                                  map.put("minAge",min);
                              }
                              // 获取满足混合权限的未读用户
                              libraryNotReadyList= documentLibraryMapper.CheckList(map);
                          }

                      }
                  }

              }
            }
            //需要将所有权限用户筛选出来， 然后通过查 制度阅读表， 进行遍历，  不相同的则为未读者
            //黑名单 则需将 所有非黑名单用户查询出来 然后通过查 制度阅读表， 进行遍历，  不相同的则为未读者 ，
            //白名单

            data.putDataValue("libraryNotReadyList",libraryNotReadyList);
            data.putDataValue("libraryReadyList",libraryReadyList);
            return data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"查询出错！");
        }
    }
}
