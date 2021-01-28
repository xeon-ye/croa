package com.qinfei.qferp.service.impl.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.Administrative;
import com.qinfei.qferp.entity.administrative.AdministrativeOnbusinessReport;
import com.qinfei.qferp.mapper.administrative.AdministrativeMapper;
import com.qinfei.qferp.mapper.administrative.AdministrativeOnbusinessReportMapper;
import com.qinfei.qferp.service.administrative.IAdministrativeOnbusinessReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdministrativeOnbusinessReportService implements IAdministrativeOnbusinessReportService {
    @Autowired
    private AdministrativeOnbusinessReportMapper reportMapper;
    @Autowired
    private AdministrativeMapper admMapper;

    /**
     * 添加总结报告
     * @param report
     * @return
     */
    @Override
    public ResponseData addReport(AdministrativeOnbusinessReport report) {
        ResponseData data = ResponseData.ok();
        report.setCreateInfo();
        reportMapper.insertSelective(report);
        //将行政表中的审批状态改成已提交出差总结
        Administrative administrative = new Administrative();
        administrative.setId(report.getAdministrativeId());
        administrative.setApproveState(3);
        admMapper.updateByPrimaryKeySelective(administrative);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity", report) ;
        return data;
    }

    /**
     * 更新总结报告
     * @param report
     * @return
     */
    @Override
    public ResponseData updateReport(AdministrativeOnbusinessReport report) {
        ResponseData data = ResponseData.ok();
        report.setUpdateInfo();
        reportMapper.updateByPrimaryKeySelective(report);
        data.putDataValue("message","操作成功");
        data.putDataValue("entity",report);
        return data;
    }

    /**
     * 根据流程id获取报告
     * @param admId
     * @return
     */
    @Override
    public ResponseData getReport(Integer admId) {
        ResponseData data = ResponseData.ok();
        AdministrativeOnbusinessReport report = reportMapper.getByAdmId(admId);
        data.putDataValue("entity",report);
        return data;
    }
}
