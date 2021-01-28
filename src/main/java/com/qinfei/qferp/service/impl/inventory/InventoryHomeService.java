package com.qinfei.qferp.service.impl.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.InventoryHomeMapper;
import com.qinfei.qferp.service.inventory.IInventoryHomeService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 物品领用接口实现类
 * @author tsf
 */
@Service
public class InventoryHomeService implements IInventoryHomeService {
    @Autowired
    private InventoryHomeMapper inventoryHomeMapper;

    @Override
    public Map purchaseOrderStatistics(Map map) {
        try {
            User user =AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            Map purchaseMap=new HashMap();
            map.put("companyCode", user.getCompanyCode());
            List<Map> list = inventoryHomeMapper.purchaseOrderStatistics(map);
            Map result=inventoryHomeMapper.purchaseOrderResult(map);
            purchaseMap.put("list",list);
            purchaseMap.put("result",result);
            return purchaseMap;
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，物品采购订单统计出错啦，请联系技术人员");
        }
    }

    @Override
    public Map stockAnalysis(Map map) {
        try {
            User user =AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            map.put("companyCode", user.getCompanyCode());
//            map.put("userId", user.getId());
            Map stockMap=new HashMap();
            List<Map> listPie = inventoryHomeMapper.stockAnalysisPie(map);
            List<Map> list = inventoryHomeMapper.stockAnalysis(map);
            Map result = inventoryHomeMapper.stockAnalysisResult(map);
            stockMap.put("listPie",listPie);
            stockMap.put("list",list);
            stockMap.put("result",result);
            return stockMap;
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，商品库存分析数据出错啦，请联系技术人员");
        }
    }

    @Override
    public Map outBoundStatistics(Map map) {
        try {
            User user = AppUtil.getUser();
            if (user == null) {
                throw new QinFeiException(1002, "请先登录");
            }
            map.put("companyCode", user.getCompanyCode());
//            map.put("userId", user.getId());
            //入库统计数据
            List<Map> putList = inventoryHomeMapper.putStockStatistics(map);
            //出库统计数据
            List<Map> outList = inventoryHomeMapper.outStockStatistics(map);
            //封装入库统计数据成Map
            Map<String, Map> stockMap = new HashMap();
            if (!CollectionUtils.isEmpty(putList)) {
                for (Map putObj : putList) {
                    //入库时间
                    String putTime = putObj.get("time").toString();
                    stockMap.put(putTime, putObj);
                }
            }
            if (!CollectionUtils.isEmpty(putList)) {
                if (!CollectionUtils.isEmpty(outList)) {
                    for (Map outObj : outList) {
                        //出库时间
                        String outTime = outObj.get("time").toString();
                        //时间相同时数据合并
                        if (stockMap.containsKey(outTime)) {
                            Object outAmount = outObj.get("outAmount");
                            Object outMoney = outObj.get("outMoney");
                            if (!ObjectUtils.isEmpty(outAmount)) {
                                stockMap.get(outTime).put("outAmount", Integer.valueOf(outAmount.toString()));
                            }
                            if (!ObjectUtils.isEmpty(outMoney)) {
                                stockMap.get(outTime).put("outMoney", Double.valueOf(outMoney.toString()));
                            }
                        } else {
                            //入库中不包括的出库时间则添加
                            Map addMap = new HashMap();
                            Object outAmount = outObj.get("outAmount");
                            Object outMoney = outObj.get("outMoney");
                            Object time = outObj.get("time");
                            if (!ObjectUtils.isEmpty(outAmount)) {
                                addMap.put("outAmount", Integer.valueOf(outAmount.toString()));
                            }
                            if (!ObjectUtils.isEmpty(outMoney)) {
                                addMap.put("outMoney", Double.valueOf(outMoney.toString()));
                            }
                            if (!ObjectUtils.isEmpty(time)) {
                                addMap.put("time", time.toString());
                            }
                            stockMap.put(time.toString(), addMap);
                        }
                    }
                }
            } else {
                if (!CollectionUtils.isEmpty(outList)) {
                    for (Map outObj : outList) {
                        //入库时间
                        String outTime = outObj.get("time").toString();
                        stockMap.put(outTime, outObj);
                    }
                }
            }
            //出入库统计结果
            List<Map> outBoundResult = inventoryHomeMapper.outBoundResult(map);
            Map result = new HashMap();
            for (Map obj : outBoundResult) {
                Object putAmount = obj.get("putAmount");
                Object putMoney = obj.get("putMoney");
                Object outAmount = obj.get("outAmount");
                Object outMoney = obj.get("outMoney");
                if (!ObjectUtils.isEmpty(putAmount)) {
                    result.put("putAmount", Integer.valueOf(putAmount.toString()));
                }
                if (!ObjectUtils.isEmpty(putMoney)) {
                    result.put("putMoney", Double.valueOf(putMoney.toString()));
                }
                if (!ObjectUtils.isEmpty(outAmount)) {
                    result.put("outAmount", Integer.valueOf(outAmount.toString()));
                }
                if (!ObjectUtils.isEmpty(outMoney)) {
                    result.put("outMoney", Double.valueOf(outMoney.toString()));
                }
            }
            Map outBoundMap = new HashMap();
            outBoundMap.put("result", result);
            TreeMap<String,Map> treeMap=new TreeMap<String,Map>();
            treeMap.putAll(stockMap);
            List<Object> list = new ArrayList<Object>(treeMap.values());
            outBoundMap.put("list", list);
            return outBoundMap;
        } catch (QinFeiException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new QinFeiException(1002, "抱歉，出入库统计出错啦，请联系技术人员");
        }
    }
}
