package com.qinfei.qferp.controller.inventory;

import com.qinfei.core.ResponseData;
import com.qinfei.core.config.Config;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.UUIDUtil;
import com.qinfei.qferp.entity.fee.Borrow;
import com.qinfei.qferp.entity.inventory.ReceiveApply;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.inventory.IApplyService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 物品领用控制器
 * @author tsf
 */
@Slf4j
@Controller
@RequestMapping("/apply")
public class ApplyController {
    @Autowired
    private IApplyService applyService;
    @Autowired
    private Config config;

    /**
     * 获取领用分页数据数量
     * @param map
     * @return
     */
    @RequestMapping("/getPageCount")
    @ResponseBody
    public ResponseData getPageCount(@RequestParam Map map){
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                ResponseData.customerError(1002,"未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                Integer number = applyService.getApplyCount(map);
                data.putDataValue("total",number);
            }
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉,物品领用获取分页数量出错啦,请联系技术人员!");
        }
    }

    /**
     * 获取领用分页信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/getPageInfo")
    @ResponseBody
    public PageInfo<Map> getPageInfo(@RequestParam Map map,Pageable pageable){
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        if (roles == null || roles.size() == 0) {
            ResponseData.customerError(1002,"未查询到角色信息");
        } else {
            map.put("roleType", roles.get(0).getType());
            map.put("roleCode", roles.get(0).getCode());
            map.put("user", user);
        }
        return applyService.listPg(map,pageable);
    }

    /**
     * 获取用户使用分页数据的数量
     * @param map
     * @return
     */
    @RequestMapping("/getUserApplyCount")
    @ResponseBody
    public ResponseData getUserApplyCount(@RequestParam Map map){
        try {
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            List<Role> roles = user.getRoles();
            if (roles == null || roles.size() == 0) {
                ResponseData.customerError(1002,"未查询到角色信息");
            } else {
                map.put("roleType", roles.get(0).getType());
                map.put("roleCode", roles.get(0).getCode());
                map.put("user", user);
                Integer number = applyService.getUserApplyCount(map);
                data.putDataValue("total",number);
            }
            return data;
        } catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉,物品领用获取分页数量出错啦,请联系技术人员!");
        }
    }

    /**
     * 获取用户使用分页信息
     * @param map
     * @param pageable
     * @return
     */
    @RequestMapping("/getUserApplyData")
    @ResponseBody
    public PageInfo<Map> getUserApplyData(@RequestParam Map map,Pageable pageable){
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        if (roles == null || roles.size() == 0) {
            ResponseData.customerError(1002,"未查询到角色信息");
        } else {
            map.put("roleType", roles.get(0).getType());
            map.put("roleCode", roles.get(0).getCode());
            map.put("user", user);
        }
        return applyService.getUserApplyData(map,pageable);
    }


    //根据id查询物品领用信息
    @RequestMapping("/editAjax")
    @ResponseBody
    public ResponseData editAjax(@RequestParam("id") Integer id){
        try {
            ResponseData data = ResponseData.ok();
            ReceiveApply apply = applyService.getById(id);
            data.putDataValue("entity",apply);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉，根据id查询物品领用信息出错啦,请联系技术人员!");
        }
    }

    //根据id查询物品领用信息
    @RequestMapping("/getByWareIdAndApplyId")
    @ResponseBody
    public ResponseData getByWareIdAndApplyId(@RequestParam("id") Integer id){
        try {
            ResponseData data = ResponseData.ok();
            ReceiveApply apply = applyService.getByWareIdAndApplyId(id);
            data.putDataValue("entity",apply);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉，查询物品领用信息出错啦,请联系技术人员!");
        }
    }

    //生成物品采购编号
    @RequestMapping("/getApplyCode")
    @ResponseBody
    public ResponseData getApplyCode(){
        try {
            String code = applyService.getApplyCode();
            ResponseData data = ResponseData.ok();
            data.putDataValue("code",code);
            return data;
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，自动生成物品领用编号出错啦，请联系技术人员！");
        }
    }

    private static String getStringData() {
        Date newData = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        String fileDate = simpleDateFormat.format(newData);
        return fileDate;
    }
    /**
     * 新增物品领用信息
     * @param apply 物品领用信息
     * @param type 仓库id
     * @param goodsId 产品id
     * @param unit //产品单位
     * @param amount //产品数量
     * @param price //单价
     * @param totalMoney //总金额
     * @param handleId //使用人
     * @param returnDate //归还日期
     * @param multipartFiles //附件
     * @return
     */
    @RequestMapping(value="/saveApply",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData saveApply(ReceiveApply apply,@RequestParam(value = "type", required = false) List<Integer> type,
                                  @RequestParam(value = "goodsId", required = false) List<Integer> goodsId,
                                  @RequestParam(value = "unit", required = false) List<String> unit,
                                  @RequestParam(value = "amount", required = false) List<Integer> amount,
                                  @RequestParam(value = "price", required = false) List<Double> price,
                                  @RequestParam(value = "totalMoney", required = false) List<Double> totalMoney,
                                  @RequestParam(value = "handleId", required = false) List<Integer> handleId,
                                  @RequestParam(value = "returnDate", required = false) List<Date> returnDate,
                                  @RequestParam(value = "file", required = false) MultipartFile [] multipartFiles){
        try {
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                if (multipartFile.getSize() > 0) {
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                }
            }
            apply.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
            apply.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            ReceiveApply entity = applyService.saveApply(apply,type,goodsId,unit,amount,price,totalMoney,handleId,returnDate);
            ResponseData data = ResponseData.ok();
            data.putDataValue("entity",entity);
            data.putDataValue("message","操作成功");
            return data;
        } catch (IOException e) {
            return ResponseData.customerError(1002,"很抱歉，新增物品领用时文件流出错啦，请联系技术人员！");
        }catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，新增物品领用信息出错啦，请联系技术人员！");
        }
    }

    /**
     * 编辑物品领用信息
     * @param apply 物品领用信息
     * @param type 产品分类id
     * @param goodsId 产品id
     * @param unit //产品单位
     * @param amount //产品数量
     * @param price //单价
     * @param totalMoney //总金额
     * @param handleId //使用人
     * @param returnDate //归还日期
     * @param multipartFiles //附件
     * @return
     */
    @RequestMapping(value="/editApply",produces="text/html;charset=utf-8")
    @ResponseBody
    public ResponseData editApply(ReceiveApply apply,@RequestParam(value = "type", required = false) List<Integer> type,
                                  @RequestParam(value = "goodsId", required = false) List<Integer> goodsId,
                                  @RequestParam(value = "unit", required = false) List<String> unit,
                                  @RequestParam(value = "amount", required = false) List<Integer> amount,
                                  @RequestParam(value = "price", required = false) List<Double> price,
                                  @RequestParam(value = "totalMoney", required = false) List<Double> totalMoney,
                                  @RequestParam(value = "handleId", required = false) List<Integer> handleId,
                                  @RequestParam(value = "returnDate", required = false) List<Date> returnDate,
                                  @RequestParam(value = "file", required = false) MultipartFile [] multipartFiles){
        try {
            //通过选中的记录的id，查询数据库，并判定该记录的状态是否可被删除
            ResponseData data = ResponseData.ok();
            ReceiveApply old = applyService.getById(apply.getId());
            if(old.getState()==IConst.STATE_SAVE || old.getState()==IConst.STATE_REJECT){
                //判断前台是否传过来文件，若为有，给予保存信息
                dealAffix(apply, old, multipartFiles);
                ReceiveApply entity = applyService.editApply(apply,type,goodsId,unit,amount,price,totalMoney,handleId,returnDate);
                data.putDataValue("entity",entity);
                data.putDataValue("message","操作成功");
                return data;
            }else{
                return ResponseData.customerError(1002,"当前状态不支持修改");
            }
        } catch (Exception e){
            return ResponseData.customerError(1002,"很抱歉，修改物品领用信息出错啦，请联系技术人员！");
        }
    }

    private ReceiveApply dealAffix(ReceiveApply entity, ReceiveApply old, MultipartFile[] multipartFiles) {
        try {
            List<String> picNames = new ArrayList<>();
            List<String> picPaths = new ArrayList<>();
            //附件处理逻辑：1、如果取得的multipartFiles.length>1,那么一定是上传了多个新附件，直接使用二进制存储
            //2、如果multipartFiles.length=1，那么可能没有上传附件，也可能上传了一个附件
            //3、如果上传了一个附件，multipartFile.getSize()=1,二进制存储
            //4、如果没有上传新附件，直接把old中的附件拿过来
            if (multipartFiles.length > 1) {//表示上传了新附件
                for (MultipartFile multipartFile : multipartFiles) {
                    if (multipartFile.getSize() > 0) {
                        String temp = multipartFile.getOriginalFilename();
                        String ext = null;
                        if (temp.indexOf(".") > -1) {
                            ext = temp.substring(temp.lastIndexOf("."));
                        }
                        String fileName = UUIDUtil.get32UUID() + ext;
                        String childPath =getStringData()+ "/fee/borrow/";
                        File destFile = new File(config.getUploadDir() + childPath + fileName);
                        if (!destFile.getParentFile().exists()) {
                            destFile.getParentFile().mkdirs();
                        }
                        multipartFile.transferTo(destFile);
                        picNames.add(multipartFile.getOriginalFilename());
                        picPaths.add(config.getWebDir() + childPath + fileName);
                    }
                }
                entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
            } else {
                MultipartFile multipartFile = multipartFiles[0];
                if (multipartFile.getSize() > 0) {//表示上传了新附件
                    String temp = multipartFile.getOriginalFilename();
                    String ext = null;
                    if (temp.indexOf(".") > -1) {
                        ext = temp.substring(temp.lastIndexOf("."));
                    }
                    String fileName = UUIDUtil.get32UUID() + ext;
                    String childPath = getStringData()+"/fee/borrow/";
                    File destFile = new File(config.getUploadDir() + childPath + fileName);
                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    multipartFile.transferTo(destFile);
                    picNames.add(multipartFile.getOriginalFilename());
                    picPaths.add(config.getWebDir() + childPath + fileName);
                    entity.setAffixName(picNames.toString().replaceAll("\\[|\\]", ""));
                    entity.setAffixLink(picPaths.toString().replaceAll("\\[|\\]", ""));
                } else {//表示附件没有变化
                    entity.setAffixName(old.getAffixName());
                    entity.setAffixLink(old.getAffixLink());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    /**
     * 领用出库加载可选择的领用订单
     * @param map
     * @return
     */
    @RequestMapping("/orderList")
    @ResponseBody
    public PageInfo<Map> orderList(@RequestParam Map map){
        try {
            return applyService.orderList(map);
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，领用出库加载可选择的领用订单时出错啦，请联系技术人员！");
        }
    }

    /**
     * 编辑领用出库加载可选择的领用订单
     * @param map
     * @return
     */
    @RequestMapping("/orderList2")
    @ResponseBody
    public PageInfo<Map> orderList2(@RequestParam Map map){
        try {
            return applyService.orderList2(map);
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"很抱歉，编辑领用出库加载可选择的领用订单出错啦，请联系技术人员！");
        }
    }


    //删除物品采购信息
    @RequestMapping("/delApply")
    @ResponseBody
    public ResponseData delApply(@RequestParam("id") Integer id){
        try {
            applyService.delApply(id);
            ResponseData data = ResponseData.ok();
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"很抱歉，删除物品领用出错啦，请联系技术人员！");
        }
    }
}
