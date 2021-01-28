package com.qinfei.qferp.mapper.propose;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.propose.ProposeRelation;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 建议类型-负责人数据库接口
 * @author tsf
 */
public interface ProposeRelationMapper extends BaseMapper<ProposeRelation,Integer> {
    /**
     * 添加建议表关系
     * @param list
     */
    @Insert({"<script>" +
            "insert into t_propose_relation(id,user_id,creator,create_time) values" +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.id},#{item.userId},#{item.creator},#{item.createTime})" +
            "</foreach>" +
            "</script>"})
    void addProposeRelation(List<ProposeRelation> list);

    /**
     * 删除建议类型表关系
     * @param id
     */
    @Delete("delete from t_propose_relation where id = #{id}")
    void delProposeRelation(Integer id);

    /**
     * 编辑建议类型回显数据
     * @param id
     * @return
     */
    @Select("select b.* from t_propose_relation a " +
            "LEFT JOIN sys_user b on a.user_id = b.id " +
            "left join sys_dict c on a.id=c.id "+
            "where c.type_code = 'PROPOSE_TYPE' and b.handover_state=0 and c.id=#{id} ")
    List<User> queryProposeUsers(Integer id);

    /**
     * 获取所有建议类别负责人
     * @param companyCode
     * @return
     */
    @Select("select DISTINCT(b.id) id,b.name from t_propose_relation a LEFT JOIN sys_user b on a.user_id = b.id where b.company_code=#{companyCode} and b.state>-2 and b.handover_state=0")
    List<Map> queryChargeUsers(String companyCode);

    /**
     * 根据用户id获取其拥有建议类别
     * @param id
     * @return
     */
    @Select("select DISTINCT(a.id) id from t_propose_relation a where a.user_id=#{id}")
    List<Integer> queryAdviceId(Integer id);
}
