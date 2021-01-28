package com.qinfei.qferp.mapper.document;

import com.qinfei.core.mapper.BaseMapper;
import com.qinfei.qferp.entity.document.TDocumentLibrary;
import com.qinfei.qferp.entity.document.TDocumentPermission;
import com.qinfei.qferp.entity.document.TDocumentPermissionDetails;
import com.qinfei.qferp.entity.document.TDocumentType;
import com.qinfei.qferp.entity.sys.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TDocumentLibraryMapper extends BaseMapper<TDocumentLibrary,Integer> {

    List<TDocumentType> libraryType(Map<String,Object> map);

    List<TDocumentLibrary> selectLibraryList(Map<String,Object> map);

    Integer selectWorkAge(Integer userId);

    List<User> relaseUser(Map<String,Object> map);

    int addLibrary(TDocumentLibrary tDocumentLibrary);

    int addLibraryDetailsList(List<TDocumentPermissionDetails> tDocumentPermissionDetailsList);

    int addPermission(TDocumentPermission tDocumentPermission);

    TDocumentLibrary getById(@Param("id") Integer id);

    List<TDocumentPermissionDetails> selectRangeId(@Param("id") Integer id);

    int addType(Map<String,Object>map);

    int editType(Map<String,Object> map);

    int delType(Integer id);

    Map<String,Object> queryLibrary(Map map);

    int updateLibrary(@Param("id")Integer id,@Param("state")Integer state);

    int updateLibraryList(TDocumentLibrary tDocumentLibrary);

    List<TDocumentLibrary> selectCode(Map<String,Object> map);

    String selectRole(Integer id);

    int updataDetails(Integer id);

    int updateLibraryReady(Map<String,Object> map);

    Integer selectReady(@Param("libraryId") Integer id , @Param("userId") Integer userId);

    int delLibrary(Integer typeId);

    int updateFile(@Param("file") String file ,@Param("fileLink") String fileLink, @Param("id") Integer id);
    //获取该制度的权限类型
    List<TDocumentLibrary> selectpermissions(Integer id);
    // 获取满足混合权限的未读用户
    List<User> CheckList(Map<String,Object> map);
    //获取黑名单权限的未读人员
    List<User> selectBlackNotReady(@Param("libraryId") Integer libraryId,@Param("companyCode") String companyCode);
    // 获取制度已读人员
    List<User> selectReadyList(Integer libraryId);
    //获取白名单权限未读用户
    List<User> selectNotReady( @Param("libraryId") Integer libraryId,@Param("companyCode") String companyCode);
    //获取未设置权限未读人员
    List<User> selectNotList(@Param("libraryId") Integer libraryId, @Param("companyCode") String companyCode);

    int updateReading(Integer id);
}
