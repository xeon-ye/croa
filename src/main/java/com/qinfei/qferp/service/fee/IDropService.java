package com.qinfei.qferp.service.fee;

import com.qinfei.qferp.entity.fee.Drop;
import com.qinfei.qferp.entity.fee.Outgo;
import com.qinfei.qferp.entity.sys.User;
import com.github.pagehelper.PageInfo;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface IDropService {


    PageInfo<Map> listPg(int pageNum, int pageSize, Map map);

    Drop getById(Integer id) ;

    Drop add(Drop entity);

    Drop edit(Drop entity);

    Drop update(Drop entity);

    void delById(Drop entity);

    Drop saveStepOne(Map map, User user,Outgo outgo);

    PageInfo<Map> listPgForSelectedArticle(int pageNum, int pageSize, Integer id);

    PageInfo<Map> listPgForSelectArticle(int pageNum, int pageSize, Map map);

    Map querySumAmount(Integer id);

    List<Map> exportDrop(Map map, OutputStream outputStream);

    Integer queryDropId(Integer articleId);

    List<Outgo> queryOutgoByArticleIds(List articleIds);
}
