package com.qinfei.qferp.service.impl.employ;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.qferp.entity.employ.EmployEntryEducation;
import com.qinfei.qferp.entity.employ.EmployeeBasic;
import com.qinfei.qferp.mapper.employ.EmployEntryEducationMapper;
import com.qinfei.qferp.service.employ.IEmployEntryEducationService;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.service.employ.IEmployeeBasicService;
import com.qinfei.qferp.utils.IEmployEntry;
import com.qinfei.qferp.utils.IEntryEducation;

/**
 * 入职申请的教育培训经历业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 14:24；
 */
@Service
public class EmployEntryEducationService implements IEmployEntryEducationService {
    // 数据操作接口；
    @Autowired
    private EmployEntryEducationMapper educationMapper;
    // 入职申请业务接口；
    @Autowired
    private IEmployEntryService entryService;
    // 员工基本信息的业务接口；
    @Autowired
    private IEmployeeBasicService basicService;

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：入职申请教育培训经历信息对象；
     * @return ：处理完毕的入职申请教育培训经历信息对象；
     */
    @Override
    @Transactional
    public EmployEntryEducation saveOrUpdate(EmployEntryEducation record) {
        Integer entryId = record.getEntryId();
        // 确认数据的状态允许编辑；
        if (entryService.checkEnableUpdate(entryId)) {
            if (record.getEduId() == null) {
                // 第一条录入的数据默认为最高学历；
                boolean isHighestEducation = educationMapper.getCountByParentId(entryId) == 0;
                if (isHighestEducation) {
                    record.setEduHighest(IEntryEducation.EDUCATION_HIGHEST);
                } else {
                    record.setEduHighest(IEntryEducation.EDUCATION_NORMAL);
                }
                // 设置创建人信息；
                record.setCreateInfo();
                record.setState(IEmployEntry.ENTRY_PENDING);
                educationMapper.insertSelective(record);

                // 如果是最高学历，更新相关的信息到员工基本信息表；
                if (isHighestEducation) {
                    updateEmpEducation(entryId, record.getEduCollege(), record.getEduMajor());
                }
            } else {
                // 设置更新人信息；
                record.setUpdateInfo();
                Integer eduHighest = record.getEduHighest();
                // 此处不允许修改最高学历和父表主键；
                record.setEntryId(null);
                record.setEduHighest(null);
                educationMapper.updateByPrimaryKeySelective(record);

                // 属性重新封装回对象；
                record.setEntryId(entryId);
                record.setEduHighest(eduHighest);

                // 如果是最高学历，更新相关的信息到员工基本信息表；
                if (eduHighest != null && eduHighest.intValue() == IEntryEducation.EDUCATION_HIGHEST) {
                    updateEmpEducation(entryId, record.getEduCollege(), record.getEduMajor());
                }
            }
        }
        return record;
    }

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：入职申请教育培训经历信息对象；
     * @return ：处理完毕的入职申请教育培训经历信息对象；
     */
    @Override
    @Transactional
    public EmployEntryEducation saveOrUpdateAfterInjob(EmployEntryEducation record) {
        Integer entryId = record.getEntryId();
        if (record.getEduId() == null) {
            // 第一条录入的数据默认为最高学历；
            boolean isHighestEducation = educationMapper.getCountByParentId(entryId) == 0;
            if (isHighestEducation) {
                record.setEduHighest(IEntryEducation.EDUCATION_HIGHEST);
            } else {
                record.setEduHighest(IEntryEducation.EDUCATION_NORMAL);
            }
            // 设置创建人信息；
            record.setCreateInfo();
            record.setState(IEmployEntry.ENTRY_PENDING);
            educationMapper.insertSelective(record);

            // 如果是最高学历，更新相关的信息到员工基本信息表；
            if (isHighestEducation) {
                updateEmpEducation(entryId, record.getEduCollege(), record.getEduMajor());
            }
        } else {
            // 设置更新人信息；
            record.setUpdateInfo();
            Integer eduHighest = record.getEduHighest();
            // 此处不允许修改最高学历和父表主键；
            record.setEntryId(null);
            record.setEduHighest(null);
            educationMapper.updateByPrimaryKeySelective(record);

            // 属性重新封装回对象；
            record.setEntryId(entryId);
            record.setEduHighest(eduHighest);

            // 如果是最高学历，更新相关的信息到员工基本信息表；
            if (eduHighest != null && eduHighest.intValue() == IEntryEducation.EDUCATION_HIGHEST) {
                updateEmpEducation(entryId, record.getEduCollege(), record.getEduMajor());
            }
        }
        return record;
    }

    /**
     * 更新创建人信息；
     *
     * @param entryId：主键ID；
     * @param userId：创建人ID；
     * @param userName：创建人名称；
     * @return ：操作影响的记录数；
     */
    @Override
    public int updateCreateInfoByParentId(int entryId, int userId, String userName) {
        EmployEntryEducation education = new EmployEntryEducation();
        education.setEntryId(entryId);
        education.setUpdateInfo();

        // 前面会清空，此处重新设置；
        education.setCreateId(userId);
        education.setCreateName(userName);
        return educationMapper.updateCreateInfoByParentId(education);
    }

    /**
     * 设置指定的学历为最高学历；
     *
     * @param entryId：父表ID；
     * @param eduId：主键ID；
     * @param eduCollege：学院、培训机构名称；
     * @param eduMajor：专业名称；
     * @return ：操作影响的记录数；
     */
    @Override
    @Transactional
    public int setEducationHighest(int entryId, int eduId, String eduCollege, String eduMajor) {
        EmployEntryEducation education = new EmployEntryEducation();
        education.setEduId(eduId);
        education.setEntryId(entryId);
        education.setUpdateInfo();
        // 先清空所有的最高学历设置；
        educationMapper.removeEducationHighest(education);
        // 设置指定ID的数据为最高学历；
        int result = educationMapper.setEducationHighest(education);
        // 更新员工基础表相关的信息；
        updateEmpEducation(entryId, eduCollege, eduMajor);
        return result;
    }

    /**
     * 根据主键删除单条记录；
     *
     * @param eduId：主键ID；
     * @return ：操作影响的记录数；
     */
    @Override
    public int deleteByPrimaryKey(int eduId) {
        EmployEntryEducation education = new EmployEntryEducation();
        education.setEduId(eduId);
        education.setUpdateInfo();
        return educationMapper.deleteByPrimaryKey(education);
    }

    /**
     * 根据主键和父表ID删除单条记录；
     *
     * @param entryId：父表ID；
     * @param eduId：主键ID；
     * @return ：操作影响的记录数；
     */
    @Override
    public int deleteByPrimaryKeyAndParentId(int entryId, int eduId) {
        EmployEntryEducation education = new EmployEntryEducation();
        education.setEduId(eduId);
        education.setEntryId(entryId);
        education.setUpdateInfo();
        return educationMapper.deleteByPrimaryKeyAndParentId(education);
    }

    /**
     * 根据入职申请的ID查询教育经历信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：教育信息集合；
     */
    @Override
    public List<EmployEntryEducation> selectByEntryId(int entryId) {
        return educationMapper.selectByEntryId(entryId);
    }

    /**
     * 更新员工基础表的学历信息；
     *
     * @param entryId：父表ID；
     * @param eduCollege：学院、培训机构名称；
     * @param eduMajor：专业名称；
     */
    private void updateEmpEducation(Integer entryId, String eduCollege, String eduMajor) {
        EmployeeBasic employeeBasic = new EmployeeBasic();
        employeeBasic.setEntryId(entryId);
        employeeBasic.setEmpCollege(eduCollege);
        employeeBasic.setEmpMajor(eduMajor);
        basicService.updateEducationByParentId(employeeBasic);
    }
}