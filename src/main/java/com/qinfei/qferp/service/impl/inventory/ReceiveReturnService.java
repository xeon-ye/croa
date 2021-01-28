package com.qinfei.qferp.service.impl.inventory;

import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.inventory.Goods;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.inventory.ReceiveReturn;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsMapper;
import com.qinfei.qferp.mapper.inventory.GoodsRecordMapper;
import com.qinfei.qferp.mapper.inventory.ReceiveReturnMapper;
import com.qinfei.qferp.service.flow.IProcessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.inventory.IGoodsService;
import com.qinfei.qferp.service.inventory.IReceiveReturnService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
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
 * 物品归还服务接口实现类
 */
@Service
public class ReceiveReturnService implements IReceiveReturnService {
    @Autowired
    private ReceiveReturnMapper returnMapper;
    @Autowired
    private IGoodsService goodsService;
    @Autowired
    private IProcessService processService;
    @Autowired
    private GoodsRecordMapper recordMapper;

    private static Integer type=3;

    /**
     * 通过ID查询物品归还信息
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public ReceiveReturn queryById(Integer id) {
        return returnMapper.queryById(id);
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
     * @param map
     * @param pageable
     * @return 对象列表
     */
    @Override
    public PageInfo<Map> listPg(Map map, Pageable pageable) {
        return null;
    }

    @Override
    public String getReturnCode() {
        return IConst.RETURN_CODE+ DateUtils.format(new Date(),"yyyyMMdd")+ CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.RETURN_CODE),5);
    }

    /**
     * 新增物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public ReceiveReturn addReturn(ReceiveReturn receiveReturn) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            //step1、修改库存id状态为报废（1、报修2、报废3、归还）
            goodsService.editGoodsState(3,receiveReturn.getInventoryId());
            receiveReturn.setId(null);
            receiveReturn.setCreateTime(new Date());
            receiveReturn.setUpdateUserId(user.getId());
            receiveReturn.setUpdateTime(new Date());
            receiveReturn.setCompanyCode(user.getCompanyCode());
            //step2、添加归还记录
            returnMapper.addReturn(receiveReturn);
            //step3、新增物品归还
            if(receiveReturn.getState()>0){
                processService.addReturnProcess(receiveReturn,3);
            }
            //获取发起流程的taskId
            ReceiveReturn returnObj = returnMapper.queryById(receiveReturn.getId());
            //step4、保存物品操作记录：保存前现将此库存之前的库存操作记录设置为无效状态5，防止我使用列表出现相同的数据
            List<GoodsRecord> list = recordMapper.queryByInventoryId(receiveReturn.getInventoryId());
            if(!CollectionUtils.isEmpty(list)){
                recordMapper.editGoodsRecordDelState(5,receiveReturn.getInventoryId());
            }
            GoodsRecord record = new GoodsRecord();
            record.setType(3);//1、报修2、报废3、归还
            record.setState(receiveReturn.getState());
            record.setForeignId(receiveReturn.getId());
            record.setInventoryId(receiveReturn.getInventoryId());
            record.setUserId(receiveReturn.getUserId());
            record.setUserName(receiveReturn.getUserName());
            if(!ObjectUtils.isEmpty(returnObj)){
                record.setTaskId(returnObj.getTaskId());
            }
            record.setCreateTime(receiveReturn.getCreateTime());
            record.setCompanyCode(user.getCompanyCode());
            recordMapper.saveGoodsRecord(record);
            return receiveReturn;
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，添加物品归还申请出错啦，请联系技术人员");
        }
    }

    /**
     * 修改物品归还信息
     * @param receiveReturn 实例对象
     * @return 实例对象
     */
    @Override
    @Transactional
    public void editReturn(ReceiveReturn receiveReturn) {
        try {
            User user = AppUtil.getUser();
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            receiveReturn.setUpdateUserId(user.getId());
            receiveReturn.setUpdateTime(new Date());
            receiveReturn.setCompanyCode(user.getCompanyCode());
            //step1、修改归还记录
            returnMapper.editReturn(receiveReturn);
            //step2、新增物品归还流程
            if(receiveReturn.getState()>0){
                processService.addReturnProcess(receiveReturn,3);
            }
            //为获取发起流程的taskId
            ReceiveReturn returnObj = returnMapper.queryById(receiveReturn.getId());
            GoodsRecord record = recordMapper.getGoodsRecordById(type,receiveReturn.getId());
            record.setState(receiveReturn.getState());
            record.setUpdateTime(new Date());
            record.setUpdateUserId(AppUtil.getUser().getId());
            if(!StringUtils.isEmpty(returnObj)){
                record.setTaskId(returnObj.getTaskId());
            }
            //step3、修改物品归还操作记录
            recordMapper.editGoodsRecord(record);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改物品归还申请出错啦，请联系技术人员");
        }
    }

    @Override
    @Transactional
    public void processReturn(ReceiveReturn receiveReturn) {
        returnMapper.editReturn(receiveReturn);
    }

    @Override
    public void editReturnState(Integer state, Integer id) {
        returnMapper.editReturnState(state,id);
    }

    /**
     * 删除物品归还信息
     * @param id 主键
     */
    @Override
    @Transactional
    public void deleteReturn(Integer id) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            GoodsRecord record = recordMapper.getGoodsRecordById(type,id);
            //step1、删除归还记录
            returnMapper.editReturnState(IConst.STATE_DELETE,id);
            //step2、删除库存操作记录
            Map map = new HashMap();
            map.put("id",record.getId());
            map.put("state",IConst.STATE_DELETE);
            map.put("updateTime",new Date());
            map.put("updateUserId",user.getId());
            recordMapper.editGoodsRecordState(map);
            //step3、还原库存状态
            goodsService.editGoodsState(-1,record.getInventoryId());
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，删除物品使用归还出错啦，请联系技术人员");
        }
    }
}
