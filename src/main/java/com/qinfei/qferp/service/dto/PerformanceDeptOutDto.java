package com.qinfei.qferp.service.dto;

import java.util.List;

class PerformanceDeptOutDto {
    private String deptName;
    private List<PerformanceOutDto> performanceList;

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<PerformanceOutDto> getPerformanceList() {
        return performanceList;
    }

    public void setPerformanceList(List<PerformanceOutDto> performanceList) {
        this.performanceList = performanceList;
    }

    static class PerformanceOutDto {
        private int schId;
        private String schName;

        public int getSchId() {
            return schId;
        }

        public void setSchId(int schId) {
            this.schId = schId;
        }

        public String getSchName() {
            return schName;
        }

        public void setSchName(String schName) {
            this.schName = schName;
        }
    }
}
