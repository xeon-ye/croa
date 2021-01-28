package com.qinfei.qferp.mapper.biz;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.biz.Project;
import com.qinfei.qferp.entity.biz.ProjectNode;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ProjectMapper extends BaseMapper<Project, Integer> {
    @Select({ "<script>",
            " SELECT max(a.id) id,max(a.code) code,max(a.name) name,max(a.state) state,"
                    + " max(a.disabled) disabled,max(a.apply_id) applyId, max(a.apply_name) applyName,max(b.user_name) ywy,max(c.user_name) khzj,"
                    + " max(a.apply_dept_id) applyDeptId, max(a.apply_dept_name) applyDeptName,"
                    + " DATE_FORMAT(max(a.apply_time),\"%Y-%m-%d %H:%i:%s\") applyTime, max(a.update_user_id) updateUserId,"
                    + " DATE_FORMAT(max(a.update_time),\"%Y-%m-%d %H:%i:%s\") updateTime,max(a.company_code) companyCode," +
                    " max(a.task_id) taskId,sum(e.sale_amount) saleSum "
                    + " FROM t_biz_project a  " +
                    " left join t_biz_project_node b on a.id=b.project_id and b.`index`=1 " +
                    " left join t_biz_project_node c on a.id=c.project_id and c.`index`=2 " +
                    " left join t_biz_article_extend d on a.id=d.project_id\n" +
                    " left join t_biz_article e on d.article_id=e.id" +
                     " where a.state > -2 "
                    + " <when test='code!=null and code!=\"\"'>",
            " AND a.code like concat('%',#{code},'%') ",
            " </when>",
            " <when test='userId!=null and userId!=\"\"'>",
            " AND (b.user_id = #{userId} or a.apply_id = #{userId} or c.user_id = #{userId})",
            " </when>",
            /*currentUserId传业务员id，业务员只能查询到销售是自己的项目*/
            " <when test='currentUserId!=null and currentUserId!=\"\"'>",
            " AND (b.user_id = #{currentUserId})",
            " </when>",
            " <when test='name!=null and name!=\"\"'>",
            " AND a.name like concat('%',#{name},'%')",
            " </when>",
            " <when test='applyName!=null and applyName!=\"\"'>",
            " AND a.apply_name = #{applyName} ",
            " </when>",
            " <when test='companyCode!=null and companyCode!=\"\"'>",
            " AND a.company_code = #{companyCode} ",
            " </when>",
            " <when test='ywy!=null and ywy!=\"\"'>",
            " AND b.user_name like concat('%',#{ywy},'%') ",
            " </when>",
            " <when test='khzj!=null and khzj!=\"\"'>",
            " AND c.user_name like concat('%',#{khzj},'%') ",
            " </when>",
            " <when test='state!=null and state!=\"\"'>",
            " AND a.state = #{state} ",
            " </when>",
            " <when test='disabled!=null and disabled!=\"\"'>",
            " AND a.disabled = #{disabled} ",
            " </when>",
            " <when test='applyTimeStart!=null and applyTimeStart!=\"\"'>",
            " AND a.apply_time &gt;= #{applyTimeStart}",
            " </when>",
            " <when test='applyTimeEnd!=null and applyTimeEnd!=\"\"'>",
            " AND a.apply_time &lt;= STR_TO_DATE(concat(#{applyTimeEnd},' 23:59:59'),'%Y-%m-%d %T')",
            " </when>",
            " <when test='updateTimeStart!=null and updateTimeStart!=\"\"'>",
            " AND a.update_time &gt;= #{updateTimeStart}",
            " </when>",
            " <when test='updateTimeEnd!=null and updateTimeEnd!=\"\"'>",
            " AND a.update_time &lt;= STR_TO_DATE(concat(#{updateTimeEnd},' 23:59:59'),'%Y-%m-%d %T')",
            " </when>",
            " group by a.id ",
            " <when test='saleSum!=null and saleSum!=\"\"'>",
            " having sum(e.sale_amount) = #{saleSum}",
            " </when>",
            "order by a.id desc ",
            "</script>" })
    List<Map> listPg(Map map);

    @Select("select id as id,name as name,code as code,`index` as `index` from t_biz_project_node_config where state=1")
    List<Map> initNodeConfig(Map map);

    @Insert({"<script>",
            " insert into t_biz_project_node (" +
                    "project_id," +
                    "code," +
                    "name," +
                    "type," +
                    "user_id," +
                    "user_name," +
                    "dept_id," +
                    "dept_name," +
                    "create_id," +
                    "create_time," +
                    "ratio," +
                    "`index`," +
                    "state," +
                    "company_code" +
                    ") values " +
                    " <foreach collection='list' item='item' separator=',' >" +
                    "(#{item.projectId}," +
                    "#{item.code}," +
                    "#{item.name}," +
                    "#{item.type}," +
                    "#{item.userId}," +
                    "#{item.userName}," +
                    "#{item.deptId}," +
                    "#{item.deptName}," +
                    "#{item.createId}," +
                    "#{item.createTime}," +
                    "#{item.ratio}," +
                    "#{item.index}," +
                    "#{item.state}," +
                    "#{item.companyCode})" +
                    "</foreach>",
            "</script>"})
    void saveNodeBatch(List<ProjectNode> list);

    @Delete(" delete from t_biz_project_node " +
            " where  project_id=#{projectId}")
    void delNodeByProjectId(Integer projectId);

    @Select("select id as id,name as name,code as code,type as type,user_id as userId,user_name as userName,dept_id as deptId,dept_name as deptName, ratio as ratio,`index` as `index` " +
            " from t_biz_project_node " +
            " where state=1 " +
            " and project_id=#{projectId}")
    List<Map> queryNodeList(Integer projectId);

    @Select("<script>" +
            " select a.id as artId,a.title as title,a.link as link," +
            " DATE_FORMAT(a.issued_date,\"%Y-%m-%d\") issuedDate," +
            " a.media_type_name as mediaTypeName, b.user_name as userName," +
            " b.company_name as custCompanyName,b.cust_name as custName," +
            " a.media_user_name as mediaUserName,a.media_name as mediaName," +
            " DATE_FORMAT(a.promise_date,\"%Y-%m-%d\") promiseDate," +
            " a.num as num,a.brand as brand,a.sale_amount saleAmount," +
            " a.income_amount incomeAmount,a.outgo_amount outgoAmount," +
            " a.taxes as taxes,a.refund_amount as refundAmount,a.other_pay as otherPay," +
            " a.profit as profit,a.commission as comm" +
            " from t_biz_article a " +
            " left join t_biz_order b on a.order_id=b.id " +
            " left join t_biz_article_extend c on a.id=c.article_id" +
            " where a.state>-2 " +
            " and c.project_id=#{id}" +
            " </script>")
    List<Map> queryArticlesByProjectId(Map map);

    @Select("<script>" +
            " select " +
            " count(a.id) count,sum(a.sale_amount) as saleSum,sum(a.income_amount) as incomeSum,sum(a.commission) as commSum" +
            " from t_biz_article a " +
            " left join t_biz_order b on a.order_id=b.id " +
            " left join t_biz_article_extend c on a.id=c.article_id" +
            " where a.state>-2 " +
            " and c.project_id=#{id}" +
            " </script>")
    Map querySumByProjectId(Map map);

}