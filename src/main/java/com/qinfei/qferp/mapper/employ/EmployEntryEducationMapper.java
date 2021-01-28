package com.qinfei.qferp.mapper.employ;

import java.util.List;

import com.qinfei.qferp.entity.employ.EmployEntryEducation;

/**
 * 入职申请的教育经历数据操作接口；
 *
 * @Author ：Yuan；
 * @Date ：2019/2/25 0025 15:07；
 */
public interface EmployEntryEducationMapper {
    /**
     * 根据主键删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKey(EmployEntryEducation record);

    /**
     * 根据主键和父表ID删除单条记录；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int deleteByPrimaryKeyAndParentId(EmployEntryEducation record);

    /**
     * 插入单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insert(EmployEntryEducation record);

    /**
     * 插入单条记录，插入前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int insertSelective(EmployEntryEducation record);

    int insertSelectiveExcelBatch(List<EmployEntryEducation> employEntryEducations);

    /**
     * 更新单条记录，更新前会判断对应属性是否为空；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKeySelective(EmployEntryEducation record);

    int updateByHighestAndEntryId(EmployEntryEducation record);

    List<EmployEntryEducation> listByHighestAndEntryId(EmployEntryEducation record);

    /**
     * 移除指定入职申请的所有的最高学历设置；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int removeEducationHighest(EmployEntryEducation record);

    /**
     * 设置指定的学历为最高学历；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int setEducationHighest(EmployEntryEducation record);

    /**
     * 更新单条记录，全字段插入不做检查；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateByPrimaryKey(EmployEntryEducation record);

    /**
     * 根据父表ID更新创建人信息；
     *
     * @param record：数据对象；
     * @return ：操作影响的记录数；
     */
    int updateCreateInfoByParentId(EmployEntryEducation record);

    /**
     * 根据主键查询单条记录；
     *
     * @param eduId：主键ID；
     * @return ：查询结果的封装对象；
     */
    EmployEntryEducation selectByPrimaryKey(Integer eduId);

    /**
     * 根据入职申请的ID查询教育经历信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：教育信息集合；
     */
    List<EmployEntryEducation> selectByEntryId(Integer entryId);

    /**
     * 根据入职申请的ID查询教育经历信息数量；
     *
     * @param entryId：入职申请ID；
     * @return ：教育经历信息数量；
     */
    int getCountByParentId(Integer entryId);
}