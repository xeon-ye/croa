package com.qinfei.qferp.mapper.standardized;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.standardized.StandardizedCompany;
import com.qinfei.qferp.utils.IConst;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 66
 * @Date: 2020/11/13 10:16
 * @Description: 标准化公司申请 dao 层
 */
public interface StandardizedCompanyMapper extends BaseMapper<StandardizedCompany, Integer> {

    @Select("SELECT count(1) FROM t_standardized_company_application WHERE company_name = #{value}")
    boolean checkCompanyNameIsExit(String companyName);

    @SelectProvider(type = StandardizedCompanySqlProvider.class, method = "findList")
    List<StandardizedCompany> findList(Map map);


    class StandardizedCompanySqlProvider {
        public String findList(Map map) {
            return new SQL() {{
                SELECT("id,no,company_name,apply_name,dept_name,state,create_time,update_time,apply_time,apply_id,task_id");
                FROM("t_standardized_company_application");
                if (map.get("companyQc") != null) {
                    WHERE("company_name like concat('%',#{companyQc},'%')");
                }
                if (map.get("applyNameQc") != null) {
                    WHERE("apply_name like concat('%',#{applyNameQc},'%')");
                }
                if (map.get("noQc") != null) {
                    WHERE("no like concat('%',#{noQc},'%')");
                }
                if (map.get("deptIds") != null) {
                    if (map.get("deptIds").toString().indexOf(",") > -1) {
                        WHERE("dept_id in (" + map.get("deptIds") + ")");
                    } else {
                        WHERE("dept_id = #{deptIds}");
                    }
                }
                if (map.get("code") != null) {
                    if (!Objects.equals(IConst.DEPT_CODE_XT, map.get("code")) && Objects.equals(0, map.get("isMgr"))) {
                        WHERE("apply_id = #{userId}");
                    }
                }
                if (map.get("stateQc") != null) {
                    if (Objects.equals("2", map.get("stateQc"))) {
                        WHERE("state in (37,41) ");
                    } else {
                        WHERE("state = #{stateQc}");
                    }
                }
                ORDER_BY("create_time DESC");
            }}.toString();
        }
    }


}
