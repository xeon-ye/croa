package com.qinfei.qferp.service.impl.inventory;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.GoodsRecord;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.inventory.GoodsRecordMapper;
import com.qinfei.qferp.mapper.inventory.GoodsTypeMapper;
import com.qinfei.qferp.service.inventory.IGoodsRecordService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存操作表实现类
 * @author tsf
 */
@Service
public class GoodsRecordService implements IGoodsRecordService {
    @Autowired
    private GoodsRecordMapper recordMapper;

    @Transactional
    @Override
    public void saveGoodsRecord(GoodsRecord record) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            recordMapper.saveGoodsRecord(record);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，添加库存记录报错啦，请联系技术人员");
        }
    }

    @Transactional
    @Override
    public void editGoodsRecord(GoodsRecord record) {
        User user = AppUtil.getUser();
        try {
            if(user==null){
                throw new QinFeiException(1002,"请先登录");
            }
            recordMapper.editGoodsRecord(record);
        } catch (QinFeiException e) {
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002,"抱歉，修改库存记录报错啦，请联系技术人员");
        }
    }

    @Override
    public GoodsRecord getGoodsRecordById(Integer type,Integer id) {
        return recordMapper.getGoodsRecordById(type,id);
    }

    @Override
    public List<GoodsRecord> queryByInventoryId(Integer id) {
        try {
            if(id==null){
                throw new QinFeiException(1002,"库存id不存在");
            }
            return recordMapper.queryByInventoryId(id);
        } catch (QinFeiException e) {
            throw e;
        }
    }
}
