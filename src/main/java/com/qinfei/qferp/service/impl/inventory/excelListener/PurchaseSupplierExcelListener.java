package com.qinfei.qferp.service.impl.inventory.excelListener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.GoodsType;
import com.qinfei.qferp.entity.inventory.PurchaseSupplier;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.mapper.inventory.PurchaseSupplierMapper;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import org.flowable.spring.boot.app.App;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 供应商导入监听器
 * @author tsf
 */
public class PurchaseSupplierExcelListener extends AnalysisEventListener {
    private List<PurchaseSupplier> datas = new ArrayList<>();
    private List<String> resultMsg = new ArrayList<>();
    private int updateNum = 0;
    private final PurchaseSupplierMapper purchaseSupplierMapper;
    private final GoodsTypeMapper goodsTypeMapper;

    public PurchaseSupplierExcelListener(PurchaseSupplierMapper purchaseSupplierMapper,GoodsTypeMapper goodsTypeMapper){
        this.purchaseSupplierMapper=purchaseSupplierMapper;
        this.goodsTypeMapper=goodsTypeMapper;
    }

    @Override
    public void invoke(Object object, AnalysisContext context) {
       PurchaseSupplier supplier = (PurchaseSupplier) object;
       if(supplier==null) return;
       //供应商资质
       String levelStr=supplier.getLevelStr();
       int level=0;
       if("普通".equalsIgnoreCase(levelStr)) level=0;
       else if("中等".equalsIgnoreCase(levelStr)) level=1;
       else if("优质".equalsIgnoreCase(levelStr)) level=2;
       supplier.setLevel(level);

       //产品分类（数据未确认）
       String typeStr = supplier.getTypeStr();
       int type=1;
       Map typeMap=new HashMap();
       typeMap.put("id",null);
       typeMap.put("name",typeStr);
       typeMap.put("companyCode", AppUtil.getUser().getDept().getCompanyCode());
       List<GoodsType> list = goodsTypeMapper.getSameNameList(typeMap);
       if(!CollectionUtils.isEmpty(list)){
           type = list.get(0).getId();
       }
       supplier.setType(type);

       //性别
        String sexStr = supplier.getContactSexStr();
        int sex=1;
        if("男".equalsIgnoreCase(sexStr)) sex=1;
        else if("女".equalsIgnoreCase(sexStr)) sex=2;
        supplier.setContactSex(sex);

        //支付方式
        String payMethodStr = supplier.getPayMethodStr();
        int payMethod = 0;
        if("微信".equalsIgnoreCase(payMethodStr)) payMethod=0;
        else if("支付宝".equalsIgnoreCase(payMethodStr)) payMethod=1;
        else if("银行卡".equalsIgnoreCase(payMethodStr)) payMethod=2;
        supplier.setPayMethod(payMethod);

        //供应商名称
        String name = supplier.getName();
        User user = AppUtil.getUser();
        try {
            Map map = new HashMap();
            map.put("name",name);
            map.put("companyCode",user.getDept().getCompanyCode());
            if(StringUtils.isEmpty(name)) throw new QinFeiException(50000, "【供应商姓名不为空】");
            //联系人
            String contactName = supplier.getContactName();
            if(StringUtils.isEmpty(contactName)) throw new QinFeiException(50000, "【联系人姓名不能为空】");
            String contactPhone = supplier.getContactPhone();
            if(StringUtils.isEmpty(contactPhone)) throw new QinFeiException(50000, "【联系方式不能为空】");
            List<PurchaseSupplier> purchaseSuppliers = purchaseSupplierMapper.getPurchaseSupplierByName(map);
            if (purchaseSuppliers.size()==0){
                //如果供应商名称不存在就添加
                String code = IConst.PURCHASE_SUPPLIER_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.PURCHASE_SUPPLIER_CODE),5);
                supplier.setCode(code);
                supplier.setCompanyCode(user.getDept().getCompanyCode());
                datas.add(supplier);
            }else if(purchaseSuppliers.size()==1){
                //获取供应商id
                Integer id = purchaseSuppliers.get(0).getId();
                supplier.setId(id);
                supplier.setUpdateUserId(user.getId());
                supplier.setUpdateTime(new Date());
                //替换供应商
                if(id!=null){
                    //防止Id为null时修改所有数据
                    purchaseSupplierMapper.update(supplier);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(datas.size()>0){
            try {
                purchaseSupplierMapper.insertPurchaseSupplierFormExcel(datas);
            } catch (Exception e) {
                e.printStackTrace();
            }
            datas.clear();
        }
    }

    public List<PurchaseSupplier> getDatas() {
        return datas;
    }

    public void setDatas(List<PurchaseSupplier> datas) {
        this.datas = datas;
    }

    public List<String> getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(List<String> resultMsg) {
        this.resultMsg = resultMsg;
    }

    public int getUpdateNum() {
        return updateNum;
    }

    public void setUpdateNum(int updateNum) {
        this.updateNum = updateNum;
    }
}
