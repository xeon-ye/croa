package com.qinfei.qferp.service.impl.inventory.excelListener;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsType;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.service.impl.inventory.excelModal.GoodsInfo;
import com.qinfei.qferp.utils.AppUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 产品导入监听器
 * @author tsf
 */
public class GoodsExcelListener extends AnalysisEventListener {
    private List<GoodsInfo> datas = new ArrayList<>();
    private List<String> resultMsg = new ArrayList<>();
    private int updateNum = 0;
    private final GoodsMapper goodsMapper;
    private final GoodsTypeMapper goodsTypeMapper;

    public GoodsExcelListener(GoodsMapper goodsMapper,GoodsTypeMapper goodsTypeMapper){
        this.goodsMapper=goodsMapper;
        this.goodsTypeMapper=goodsTypeMapper;
    }

    @Override
    public void invoke(Object object, AnalysisContext context) {
        GoodsInfo goods = (GoodsInfo) object;
        if (ObjectUtils.isEmpty(object)) return;
        updateNum++;
        User user = AppUtil.getUser();
        //产品分类
        String typeName = goods.getTypeName();
        Map map = new HashMap();
        map.put("name",typeName);
        map.put("typeFlag",1);
        map.put("companyCode",user.getDept().getCompanyCode());
        List<GoodsType> goodsTypeList = goodsTypeMapper.getSameNameList(map);
        if (CollectionUtils.isEmpty(goodsTypeList)) {
            resultMsg.add("第" + updateNum + "行产品分类不存在");
        }
        if(CollectionUtils.isNotEmpty(goodsTypeList)){
            Integer typeId = goodsTypeList.get(0).getId();
            goods.setTypeId(typeId);
        }
        //产品名称
        String name = goods.getName();
        if (StringUtils.isEmpty(name)) {
            resultMsg.add("第" + updateNum + "行产品名称不能为空");
        }
        Map typeMap = new HashMap();
        typeMap.put("name",name);
        typeMap.put("companyCode",user.getDept().getCompanyCode());
        List<GoodsType> list = goodsTypeMapper.getSameNameList(typeMap);
        if (CollectionUtils.isNotEmpty(list)) {
            resultMsg.add("第" + updateNum + "行已存在相同名称的产品名称");
        }
        //产品编码
        String code = goods.getCode();
        if (StringUtils.isEmpty(code)) {
            resultMsg.add("第" + updateNum + "行产品编码不能为空");
        }
        //单位
        String unit = goods.getUnit();
        if (StringUtils.isEmpty(unit)) {
            resultMsg.add("第" + updateNum + "行单位不能为空");
        }
        //单价
        String priceStr = goods.getPriceStr();
        if (StringUtils.isEmpty(priceStr)) {
            resultMsg.add("第" + updateNum + "行单价不能为空");
        }
        Double price = 0.0d;
        try {
            price = Double.valueOf(priceStr);
        } catch (NumberFormatException e) {
            resultMsg.add("第" + updateNum + "行请输入正确的单价");
        }
        goods.setPrice(price);
        goods.setState(0);
        goods.setNumber(0);
        goods.setCreateId(user.getId());
        goods.setCreateName(user.getName());
        goods.setCreateTime(new Date());
        goods.setUpdateUserId(user.getId());
        goods.setUpdateTime(new Date());
        goods.setCompanyCode(user.getDept().getCompanyCode());
        datas.add(goods);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        try {
            if(CollectionUtils.isNotEmpty(datas) && CollectionUtils.isEmpty(resultMsg)){
                goodsMapper.insertGoodsFormExcel(datas);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMsg.add("抱歉，产品导入出错啦，请联系技术人员");
            return;
        }
    }

    public List<GoodsInfo> getDatas() {
        return datas;
    }

    public void setDatas(List<GoodsInfo> datas) {
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
