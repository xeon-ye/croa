package com.qinfei.qferp.service.propose;

import com.qinfei.qferp.entity.propose.Propose;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
/**
 * 建议模块接口
 * @Author ：tsf;
 */
public interface IProposeService {
    /**
     * 根据id查询建议信息
     * @param id
     * @return
     */
    Propose queryProposeById(int id);

    /**
     * 查询所有的建议信息
     * @param map
     * @return
     */
    PageInfo<Map> queryPropose(Map map, Pageable pageable);

    /**
     * 查询自己的建议信息
     * @param map
     * @param pageable
     * @return
     */
    PageInfo<Propose> queryProposeByself(Map map, Pageable pageable);

    /**
     * 保存建议信息
     * @param propose
     */
    Propose save(Propose propose);

    /**
     * 建议管理修改建议信息
     * @param t
     */
    void update(Propose t);

    /**
     * 判断建议类别下是否有建议
     * @param id
     * @return
     */
    List<Propose> queryAdviceById(Integer id);

    /**
     * 确认驳回
     * @param propose
     */
    void confirmReject(Propose propose);

    /**
     * 建议查询修改建议信息
     * @param propose
     */
    void modifyPropose(Propose propose);

    /**
     * 删除建议信息
     * @param id
     */
    void deletePropose(int id);

    /**
     * 获取建议的负责人
     * @return
     */
    List<Map> queryChargeUsers();
    /**
     * 导出建议
     * @param map
     * @param outputStream
     * @return
     */
    List<Map> exportPropose(Map map, OutputStream outputStream);

    /**
     * 导出未录入建议人员
     * @param map
     * @param outputStream
     * @return
     */
    List<Map> exportUserNoPropose(Map map, OutputStream outputStream);
}
