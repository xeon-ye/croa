package com.qinfei.qferp.service.impl.inventory;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.crm.Const;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.entity.workbench.Items;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsRecordMapper;
import com.qinfei.qferp.mapper.inventory.ReceiveRepairMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.service.inventory.IReceiveRepairService;
import com.qinfei.qferp.service.workbench.IItemsService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报修表(ReceiveRepair)表服务实现类
 */
@Service
public class ReceiveRepairService implements IReceiveRepairService {
    @Autowired
    private ReceiveRepairMapper repairMapper;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private GoodsRecordMapper recordMapper;
    @Autowired
    private IProcessService processService;
    @Autowired
    private IItemsService itemsService;

    private static Integer type=1;

    /**
     * 通过ID查询报修数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ReceiveRepair queryById(Integer id) {
        return repairMapper.queryById(id);
    }

    /**
     * 获取分页数量
     * @param map
     * @return
     */
    @Override
    public Integer getPageCount(Map map) {
        return repairMapper.getPageCount(map);
    }

    /**
     * 查询分页数据
     *
     * @param map
     * @param pageable 分页参数
     * @return 对象列表
     */
    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        PageHelper.startPage(pageable.getPageNumber(),pageable.getPageSize());
        List<Map> list = repairMapper.listPg(map,pageable);
        PageInfo<Map> pageInfo = new PageInfo<>(list);
        return pageInfo;
    }

    @Override
    public String getRepairCode() {
        return IConst.REPAIR_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.REPAIR_CODE),5);
    }

    /**
     * 新增报修数据
     *
     * @param repair 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public ReceiveRepair addRepair(ReceiveRepair repair) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            //step1、修改库存id状态（报修）
            goodsService.editGoodsState(1,repair.getInventoryId());
            repair.setId(null);
            repair.setUpdateUserId(user.getId());
            repair.setUpdateTime(new Date());
            repair.setCreateTime(new Date());
            repair.setCompanyCode(user.getCompanyCode());
            //step2、添加报修记录
            repairMapper.saveRepair(repair);
            //step3、新增物品报修
            if(repair.getState()>0){
                processService.addRepairProcess(repair,3);
            }
            ReceiveRepair repair2 = repairMapper.queryById(repair.getId());
            //step4、保存物品操作记录：保存前现将此库存之前的库存操作记录设置为无效状态5，防止我使用列表出现相同的数据
            List<GoodsRecord> list = recordMapper.queryByInventoryId(repair.getInventoryId());
            if(!CollectionUtils.isEmpty(list)){
                recordMapper.editGoodsRecordDelState(5,repair.getInventoryId());
            }
            GoodsRecord record = new GoodsRecord();
            record.setType(1);//1报修,2、报废,3、归还
            record.setState(repair.getState());
            record.setForeignId(repair.getId());
            record.setInventoryId(repair.getInventoryId());
            record.setUserId(repair.getUserId());
            record.setUserName(repair.getUserName());
            if(!ObjectUtils.isEmpty(repair2) && repair.getState()!=0){
                record.setTaskId(repair2.getTaskId());
            }
            record.setCreateTime(repair.getCreateTime());
            record.setCompanyCode(user.getCompanyCode());
            recordMapper.saveGoodsRecord(record);
            return repair;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，添加物品报修申请出错啦，请联系技术人员");
        }
    }

    /**
     * 修改物品报修数据
     *
     * @param repair 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public void editRepair(ReceiveRepair repair) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            //step2、添加物品报修
            repair.setUpdateUserId(user.getId());
            repair.setUpdateTime(new Date());
            repair.setCompanyCode(user.getCompanyCode());
            repairMapper.editRepair(repair);
            //step2、添加报修记录
            if(repair.getState()>0){
                processService.addRepairProcess(repair,3);
            }
            ReceiveRepair repair2 = repairMapper.queryById(repair.getId());
            GoodsRecord record = recordMapper.getGoodsRecordById(type,repair.getId());
            if(!ObjectUtils.isEmpty(repair2)){
                record.setTaskId(repair2.getTaskId());
            }
            record.setState(repair.getState());
            record.setUpdateTime(new Date());
            record.setUpdateUserId(AppUtil.getUser().getId());
            //修改物品操作记录
            recordMapper.editGoodsRecord(record);
        } catch (QinFeiException e) {
            throw  e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改物品报修申请出错啦，请联系技术人员");
        }
    }

    /**
     * 物品报修更新流程状态
     * @param receiveRepair
     */
    @Override
    @Transactional
    public void processRepair(ReceiveRepair receiveRepair) {
        repairMapper.editRepair(receiveRepair);
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    @Transactional
    public void deleteRepair(Integer id) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            GoodsRecord record = recordMapper.getGoodsRecordById(type,id);
            //step1、删除报修记录
            repairMapper.deleteRepair(id);
            //step2、删除库存操作记录
            Map map = new HashMap();
            map.put("id",record.getId());
            map.put("state",IConst.STATE_DELETE);
            map.put("updateTime",new Date());
            map.put("updateUserId",user.getId());
            recordMapper.editGoodsRecordState(map);
            //step3、还原状态:如果是产品库存操作则还原成库存状态，否则还原出库状态
            Goods goods=goodsService.getGoodsById(record.getInventoryId());
            if(!ObjectUtils.isEmpty(goods)){
                //如果用户id不为空则修改成出库状态
                Integer userId=goods.getUserId();
                if(userId!=null){
                    goodsService.editGoodsState(-1,record.getInventoryId());
                }else{
                    goodsService.editGoodsState(0,record.getInventoryId());
                }
            }
        } catch (QinFeiException e) {
            throw  e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，删除物品使用报修出错啦，请联系技术人员");
        }
    }

    /**
     * 处理待办
     * @param entity
     */
    private void finishItem(ReceiveRepair entity){
        if(entity.getItemId()!=null){
            Items items = new Items();
            items.setId(entity.getItemId());
            items.setTransactionState(Const.ITEM_Y);
            itemsService.finishItems(items);
        }
    }
}
