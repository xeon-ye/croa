package com.qinfei.qferp.service.sys;

import com.alibaba.fastjson.JSONArray;
import com.qinfei.qferp.entity.sys.Dept;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface IDeptService {

    List<Dept> listAll();

    JSONArray listForTreeView();

    JSONArray onlyDeptMent();

    JSONArray listDeptAllMJ();
    /**
     * 获取指定部门下的部门数据；
     *
     * @param deptId：部门ID；
     * @return ：下属的部门数据；
     */
    JSONArray listForTreeView(int deptId);

    JSONArray list();

    JSONArray listByCompany();

    Dept edit(Dept dept);

    void delById(Integer id);

    Dept addChildDept(Map map);

    Dept getById(Integer id);

    List<Dept> queryDeptByName(String name);

    List<Dept> queryDeptByCompanyCodeAndCode(String companyCode, String code);

    List<Dept> listByTypeAndCompanyCode(String type, String companyCode);

    List<Dept> listByParentId(Integer id);

    /**
     * 根据部门第一层级查询所有分公司名称
     *
     * @param level
     * @return
     */
    List<Dept> listByLevelId(Integer level);

    List<Dept> allCompany(Integer level);

    String idsByParentId(Integer id);

    /**
     * 获取完整的部门树数据，用于业务量统计；
     *
     * @return ：包含部门树数据用户对象；
     */
    User getFullDeptTree(Map map);


    JSONArray listForTreeViewByCompanyCode();

    //根据登录人的公司code获取公司的所有部门
    List<Dept> getDeptByCompany();

    /**
     * 根据部门ID查询下方所有媒介部和业务部
     * @param deptId
     * @return
     */
    JSONArray listAllMJYWByDeptId(Integer deptId);

    /**
     * 根据部门ID查询下方指定部门
     * @param deptId 上级部门ID
     * @param deptCode 部门编码
     * @return
     */
    JSONArray listAllDeptByIdAndCode(Integer deptId, String deptCode);

    JSONArray listAllDeptByIdAndCodeAndLevel(Integer deptId, String deptCode, String level);

    /**
     * 根据公司编码获取公司信息
     * @param companyCode 公司编码
     * @return
     */
    Dept getCompanyByCode(String companyCode);

    /**
     * 获取集团总经办信息
     * @return
     */
    Dept getRootDept();

    List<Dept> queryDeptByCompanyCodeAndCodeAndDeptName(String companyCode, String yw, String deptName);

    /**
     * 获取集团下所有公司
     */
    List<Dept> listAllCompany();

    List<Dept> listJTAllCompany(String companyCode);

    /**
     * 修改添加公司时公司代码不能重复
     * @param companyCode
     * @return
     */
    List<Dept> checkDeptCompanyCode(String companyCode);

    /**
     * 根据条件查询部门人数，得出的列表如果是父级部门则会把直属子级部门人员计算进去一起返回，一层一层计算，最顶级的部门其实相当于计算所有子级人员
     * @param param
     * @return
     */
    Map<String, Map<String, Integer>> getDeptUserNum(Map<String, Object> param);

    List<Dept> listByCode(String code);
}
