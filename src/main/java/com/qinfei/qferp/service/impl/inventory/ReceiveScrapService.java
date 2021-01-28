package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.annotation.Transient;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.inventory.ReceiveRepair;
import com.qinfei.qferp.entity.inventory.ReceiveScrap;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsRecordMapper;
import com.qinfei.qferp.mapper.inventory.ReceiveScrapMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.service.inventory.IReceiveScrapService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.qinfei.qferp.utils.IProcess;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 物品报废服务实现类
 */
@Service
public class ReceiveScrapService implements IReceiveScrapService {
    @Autowired
    private ReceiveScrapMapper scrapMapper;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private GoodsRecordMapper recordMapper;

    private static Integer type=2;

    /**
     * 通过id查询物品报废信息
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ReceiveScrap queryById(Integer id) {
        return scrapMapper.queryById(id);
    }

    /**
     * 获取分页数量
     * @param map
     * @return
     */
    @Override
    public Integer getPageCount(Map map) {
        return null;
    }

    /**
     * 查询分页数据
     *
     * @param map
     * @param pageable
     * @return 对象列表
     */
    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        return null;
    }

    @Override
    public String getScrapCode() {
        return IConst.SCRAP_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.SCRAP_CODE),5);
    }

    /**
     * 新增物品报废数据
     *
     * @param scrap 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public ReceiveScrap addScrap(ReceiveScrap scrap) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            //step1、修改库存id状态为报废（1、报修2、报废3、归还）
            goodsService.editGoodsState(2,scrap.getInventoryId());
            scrap.setId(null);
            scrap.setUpdateUserId(user.getId());
            scrap.setUpdateTime(new Date());
            scrap.setCreateTime(new Date());
            scrap.setCompanyCode(user.getCompanyCode());
            //step2、添加报废记录
            scrapMapper.addScrap(scrap);
            //step3、新增物品报废
            if(scrap.getState()>0){
                processService.addScrapProcess(scrap,3);
            }
            //获取发起流程的taskId
            ReceiveScrap scrap2 = scrapMapper.queryById(scrap.getId());
            //step4、保存物品操作记录：保存前现将此库存之前的库存操作记录设置为无效状态5，防止我使用列表出现相同的数据
            List<GoodsRecord> list = recordMapper.queryByInventoryId(scrap.getInventoryId());
            if(!CollectionUtils.isEmpty(list)){
                recordMapper.editGoodsRecordDelState(5,scrap.getInventoryId());
            }
            GoodsRecord record = new GoodsRecord();
            record.setType(2);//1、报修2、报废3、归还
            record.setState(scrap.getState());
            record.setForeignId(scrap.getId());
            record.setInventoryId(scrap.getInventoryId());
            record.setUserId(scrap.getUserId());
            record.setUserName(scrap.getUserName());
            if(!ObjectUtils.isEmpty(scrap2)){
                record.setTaskId(scrap2.getTaskId());
            }
            record.setCreateTime(scrap.getCreateTime());
            record.setCompanyCode(user.getCompanyCode());
            recordMapper.saveGoodsRecord(record);
            return scrap;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，添加物品报废申请出错啦，请联系技术人员");
        }
    }

    /**
     * 修改物品报废数据
     *
     * @param scrap 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public void editScrap(ReceiveScrap scrap) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            //step1、添加物品报废
            scrap.setUpdateUserId(user.getId());
            scrap.setUpdateTime(new Date());
            scrap.setCompanyCode(user.getCompanyCode());
            scrapMapper.editScrap(scrap);
            //step2、启动报废流程
            if(scrap.getState()>0){
                processService.addScrapProcess(scrap,3);
            }
            ReceiveScrap scrap2 = scrapMapper.queryById(scrap.getId());
            GoodsRecord record = recordMapper.getGoodsRecordById(type,scrap2.getId());
            if(!ObjectUtils.isEmpty(scrap2)){
                record.setTaskId(scrap2.getTaskId());
            }
            record.setState(scrap.getState());
            record.setUpdateTime(new Date());
            record.setUpdateUserId(AppUtil.getUser().getId());
            //step3、添加报废操作记录
            recordMapper.editGoodsRecord(record);
        } catch (QinFeiException e) {
            throw  e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改物品报废申请出错啦，请联系技术人员");
        }
    }

    @Override
    public void processScrap(ReceiveScrap receiveScrap) {
        scrapMapper.editScrap(receiveScrap);
    }

    /**
     * 删除物品报废申请数据
     *
     * @param id 主键
     */
    @Override
    @Transactional
    public void deleteScrap(Integer id) {
        //删除物品报废申请
        scrapMapper.deleteScrap(id);
        GoodsRecord record = recordMapper.getGoodsRecordById(type,id);
        if(!ObjectUtils.isEmpty(record)){
            Map map = new HashMap();
            map.put("id",record.getId());
            map.put("state",IConst.STATE_DELETE);
            map.put("updateTime",new Date());
            map.put("updateUserId",AppUtil.getUser().getId());
            recordMapper.editGoodsRecordState(map);
            //还原状态:如果是产品库存操作则还原成库存状态，否则还原出库状态
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
        }else{
            throw new QinFeiException(1002,"抱歉，删除物品报废申请出错啦，请联系技术人员");
        }
    }
}
