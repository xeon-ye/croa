package com.qinfei.qferp.service.administrative;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.administrative.AdministrativeOnbusinessReport;

public interface IAdministrativeOnbusinessReportService {
    ResponseData addReport(AdministrativeOnbusinessReport report);
    ResponseData updateReport(AdministrativeOnbusinessReport report);
    ResponseData getReport(Integer admId);
}
