package com.qinfei.qferp.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ProcessMapper {


    @Select("<script>" +
            " SELECT\n" +
            " a.ID_ ,\n"+
            " a.START_TIME_ startTime ,\n" +
            " a.DURATION_ duration ,\n" +
            " GROUP_CONCAT(case WHEN b.NAME_ = 'processDate' OR b.NAME_='expectTime' THEN CONCAT(b.NAME_,':',b.LONG_) ELSE CONCAT(b.NAME_,':',b.TEXT_) END )collection   \n" +
            " FROM\n" +
            " \tact_hi_taskinst a,\n" +
            " \tact_hi_varinst b\n" +
            " WHERE\n" +
            " a.PROC_INST_ID_ = b.PROC_INST_ID_\n" +
            " AND a.ASSIGNEE_ = #{userId}\n" +
            " AND a.REV_ =3\n" +
            " AND (b.NAME_='processName' or b.NAME_='userName' OR b.NAME_='initiatorDeptName' OR b.NAME_='dataName' or b.NAME_='processDate' OR b.NAME_='expectTime' OR b.NAME_ = 'dataUrl'  or( b.NAME_ ='state' and LONG_ >-1) OR b.NAME_ = 'process'  OR b.NAME_='dataId' )\n" +
            " GROUP BY a.ID_\n" +
            " ORDER BY\n" +
            " \ta.LAST_UPDATED_TIME_ DESC" +
            " </script>")
    List<Map> theApproved(Map map);

    @Select({"<script>",
            "SELECT\n" +
                    " ard.ID_ AS id,\n" +
                    " ard.NAME_ AS processFileName,\n" +
                    " ard.DEPLOY_TIME_ AS deployTime,\n" +
                    " arp.NAME_ AS processName,\n" +
                    " arp.KEY_ AS processKey,\n" +
                    " arp.VERSION_ AS deployVersion,\n" +
                    " arp.DESCRIPTION_ AS processDesc\n" +
                    "FROM\n" +
                    " act_re_deployment ard\n" +
                    "LEFT JOIN act_re_procdef arp ON ard.ID_ = arp.DEPLOYMENT_ID_\n" +
                    "WHERE 1=1 \n" +
                    "<when test=\"processFileName != null and processFileName != ''\">" +
                    "   AND ard.NAME_ LIKE CONCAT('%',#{processFileName},'%')\n"+
                    "</when>",
            "<when test=\"processName != null and processName != ''\">" +
                    "   AND arp.NAME_ LIKE CONCAT('%',#{processName},'%')\n"+
                    "</when>",
            "<when test=\"processKey != null and processKey != ''\">" +
                    "   AND arp.KEY_ = #{processKey}\n"+
                    "</when>",
            "<when test=\"processDesc != null and processDesc != ''\">" +
                    "   AND arp.DESCRIPTION_ LIKE CONCAT('%',#{processDesc},'%')\n"+
                    "</when>",
            "<when test=\"startDate != null and startDate != ''\">" +
                    "   AND ard.DEPLOY_TIME_ <![CDATA[>=]]> STR_TO_DATE(CONCAT(#{startDate},' 00:00:00'),'%Y/%m/%d %T')\n"+
                    "</when>",
            "<when test=\"endDate != null and endDate != ''\">" +
                    "   AND ard.DEPLOY_TIME_ <![CDATA[<=]]> STR_TO_DATE(CONCAT(#{endDate},' 23:59:59'),'%Y/%m/%d %T')\n"+
                    "</when>",
            "ORDER BY\n" +
                    " deployTime DESC,\n" +
                    " deployVersion DESC",
            "</script>"})
    List<Map<String, Object>> listProcessDefinition(Map<String, Object> param);

    @Select("SELECT\n" +
            "\tahv.TEXT_ AS company,\n" +
            "\tahv1.LONG_ AS mediaType," +
            " ahv2.LONG_ AS configurationProcess \n" +
            "FROM\n" +
            "\tact_hi_varinst ahv \n" +
            " LEFT JOIN act_hi_varinst ahv1 on ahv.PROC_INST_ID_ = ahv1.PROC_INST_ID_\n" +
            "LEFT JOIN act_hi_varinst ahv2 ON ahv.PROC_INST_ID_ = ahv2.PROC_INST_ID_\n" +
            "WHERE\n" +
            "\tahv.PROC_INST_ID_ = #{processInstanceId} \n" +
            "AND \n" +
            "\tahv.NAME_ = 'company'\n" +
            "and ahv1.NAME_ = 'mediaType'" +
            "and ahv2.NAME_='configurationProcess'")
    List<Map<String, Object>>selectProcess(String processInstanceId);
}
