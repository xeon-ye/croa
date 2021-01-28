package com.qinfei.qferp.service.impl.employ;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.employ.EmployEntryExperience;
import com.qinfei.qferp.entity.employ.EmployeeBasic;
import com.qinfei.qferp.mapper.employ.EmployEntryExperienceMapper;
import com.qinfei.qferp.service.employ.IEmployEntryExperienceService;
import com.qinfei.qferp.service.employ.IEmployEntryService;
import com.qinfei.qferp.service.employ.IEmployeeBasicService;
import com.qinfei.qferp.utils.IEmployEntry;

/**
 * 入职申请的教育培训经历业务接口实现类；
 *
 * @Author ：Yuan；
 * @Date ：2019/3/1 0001 14:24；
 */
@Service
public class EmployEntryExperienceService implements IEmployEntryExperienceService {
    // 数据操作接口；
    @Autowired
    private EmployEntryExperienceMapper experienceMapper;
    // 入职申请业务接口；
    @Autowired
    private IEmployEntryService entryService;
    // 员工基础信息业务接口；
    @Autowired
    private IEmployeeBasicService basicService;

    /**
     * 保存或更新单条记录到数据库中；
     *
     * @param record：入职申请工作经历信息对象；
     * @return ：处理完毕的入职申请工作经历信息对象；
     */
    @Override
    @Transactional
    public EmployEntryExperience saveOrUpdate(EmployEntryExperience record) {
        Integer entryId = record.getEntryId();
        // 确认数据的状态允许编辑；
        if (entryService.checkEnableUpdate(entryId)) {
            if (record.getExpId() == null) {
                // 设置创建人信息；
                record.setCreateInfo();
                record.setState(IEmployEntry.ENTRY_PENDING);
                experienceMapper.insertSelective(record);

                // 更新履历信息；
                updateEmpExperience(entryId);
            } else {
                // 设置更新人信息；
                record.setUpdateInfo();
                // 父表主键不允许修改；
                record.setEntryId(null);
                experienceMapper.updateByPrimaryKeySelective(record);

                // 重新封装回对象；
                record.setEntryId(entryId);

                // 更新履历信息；
                updateEmpExperience(entryId);
            }
        }
        return record;
    }

    /**
     * 入职后保存员工信息
     */
    @Override
    @Transactional
    public EmployEntryExperience saveOrUpdateInJob(EmployEntryExperience record) {
        Integer entryId = record.getEntryId();
        // 确认数据的状态允许编辑；
        if (record.getExpId() == null) {
            // 设置创建人信息；
            record.setCreateInfo();
            record.setState(IEmployEntry.ENTRY_PENDING);
            experienceMapper.insertSelective(record);

            // 更新履历信息；
            updateEmpExperience(entryId);
        } else {
            // 设置更新人信息；
            record.setUpdateInfo();
            // 父表主键不允许修改；
            record.setEntryId(null);
            experienceMapper.updateByPrimaryKeySelective(record);

            // 重新封装回对象；
            record.setEntryId(entryId);

            // 更新履历信息；
            updateEmpExperience(entryId);
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
        EmployEntryExperience experience = new EmployEntryExperience();
        experience.setUpdateInfo();

        // 前面会清空，此处重新设置；
        experience.setCreateId(userId);
        experience.setEntryId(entryId);
        experience.setCreateName(userName);
        return experienceMapper.updateCreateInfoByParentId(experience);
    }

    /**
     * 根据主键删除单条记录；
     *
     * @param expId：主键ID；
     * @return ：操作影响的记录数；
     */
    @Override
    public int deleteByPrimaryKey(int expId) {
        EmployEntryExperience experience = new EmployEntryExperience();
        experience.setExpId(expId);
        experience.setUpdateInfo();
        return experienceMapper.deleteByPrimaryKey(experience);
    }

    /**
     * 根据主键和父表ID删除单条记录；
     *
     * @param entryId：父表ID；
     * @param expId：主键ID；
     * @return ：操作影响的记录数；
     */
    @Override
    public int deleteByPrimaryKeyAndParentId(int entryId, int expId) {
        EmployEntryExperience experience = new EmployEntryExperience();
        experience.setExpId(expId);
        experience.setEntryId(entryId);
        experience.setUpdateInfo();
        int result = experienceMapper.deleteByPrimaryKeyAndParentId(experience);
        updateEmpExperience(entryId);
        return result;
    }

    /**
     * 根据入职申请的ID查询工作经历信息集合；
     *
     * @param entryId：入职申请ID；
     * @return ：工作经历信息集合；
     */
    @Override
    public List<EmployEntryExperience> selectByEntryId(int entryId) {
        return experienceMapper.selectByEntryId(entryId);
    }

    /**
     * 更新员工基础信息的工作履历信息；
     */
    private void updateEmpExperience(int entryId) {
        // 查询履历信息集合，进行处理；
        List<EmployEntryExperience> experiences = experienceMapper.selectByEntryId(entryId);
        StringBuilder experienceContent = new StringBuilder();

        // 拼接文字信息；
        EmployEntryExperience experience;
        int size = experiences.size();
        for (int i = 0; i < size; i++) {
            experience = experiences.get(i);
            experienceContent.append("[").append(DateUtils.format(experience.getExpStart(), DateUtils.DATE_SMALL)).append("至").append(DateUtils.format(experience.getExpEnd(), DateUtils.DATE_SMALL));
            experienceContent.append("在").append(experience.getExpLocation()).append("的").append(experience.getExpCompany()).append("担任").append(experience.getExpProfession()).append("，");
            experienceContent.append("薪资待遇为").append(experience.getExpSalary()).append("，同公司的").append(experience.getExpContactor()).append("与我关系交好，后因").append(experience.getExpResignReason());
            experienceContent.append("原因从该公司离职。]");
            // 每条记录之间使用换行符隔开；
            if (i < size - 1) {
                experienceContent.append("\\n");
            }
        }

        // 更新工作履历信息；
        EmployeeBasic employeeBasic = new EmployeeBasic();
        employeeBasic.setEntryId(entryId);
        employeeBasic.setEmpExperience(experienceContent.toString());
        basicService.updateExperienceByParentId(employeeBasic);
    }
}