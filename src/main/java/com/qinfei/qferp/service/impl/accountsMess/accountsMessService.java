package com.qinfei.qferp.service.impl.accountsMess;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.qinfei.qferp.entity.sys.Role;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.mapper.accountsMess.accountsMessMapper;
import com.qinfei.qferp.service.accountsMess.IAccountsMessService;
import com.qinfei.qferp.service.impl.fee.FeeCodeUtil;
import com.qinfei.qferp.service.impl.flow.ProcessService;
import com.qinfei.qferp.utils.AppUtil;
import com.qinfei.qferp.utils.CodeUtil;
import com.qinfei.qferp.utils.IConst;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
@Service
@Transactional
public class accountsMessService implements IAccountsMessService {
    @Autowired
    private accountsMessMapper accountsMessMapper;
    @Autowired
    ProcessService processService;


    @Override
    public Map<String,Object> accountsMessList(Map<String,Object> map){
        User user = AppUtil.getUser();
        Map<String,Object> map1 = new HashMap<>();
        map.put("userId",user.getId());
        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        List<Map<String,Object>> articleList =  accountsMessMapper.selectArticleList(map);
        PageInfo<Map<String,Object>> pageInfo = new PageInfo<Map<String, Object>>(articleList);
        map1.put("code",0);
        map1.put("msg","ok");
        map1.put("count",pageInfo.getTotal());
        map1.put("data",articleList);
        return map1;
    }

    @Override
    public Map<String,Object> dockingListTable (Map<String,Object> map){
        Map<String,Object> map1= new HashMap<>();
        map.put("userId",AppUtil.getUser().getId());
        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        List<Map<String,Object>> list = accountsMessMapper.getCustdockingPeople(map);
        PageInfo<Map<String,Object>> pageInfo = new PageInfo<Map<String, Object>>(list);
        map1.put("code",0);
        map1.put("msg","ok");
        map1.put("count",pageInfo.getTotal());
        map1.put("data",list);
        return map1;
    }

    @Override
    public Map<String,Object> accountMessListTable(Map<String,Object> map){
        Map<String,Object> map1= new HashMap<>();
        User user = AppUtil.getUser();
        List<Role> roles = user.getRoles();
        map.put("userId",AppUtil.getUser().getId());
        map.put("companyCode",user.getCompanyCode());
        String UType = roles.get(0).getType();
        if (!"YW".equals(UType) && !"CW".equals(UType) && !"ZJB".equals(UType)){
            map.put("ZJBFlag",1);
        }
        map.put("roleType", roles.get(0).getType());
        map.put("roleCode", roles.get(0).getCode());
        map.put("user", user);
        PageHelper.startPage(Integer.parseInt(map.get("page").toString()),Integer.parseInt(map.get("limit").toString()));
        List<AccountsMess> list = accountsMessMapper.selectMessListTable(map);
        PageInfo<AccountsMess> pageInfo = new PageInfo<AccountsMess>(list);
        map1.put("code",0);
        map1.put("msg","ok");
        map1.put("count",pageInfo.getTotal());
        map1.put("data",list);
        return map1;
    }

    @Override
    public ResponseData saveMessArticle(Map<String,Object> map){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            AccountsMess accountsMess = new AccountsMess();
            accountsMess.setCode(IConst.ACCOUNTS_MESS +  CodeUtil.getMonthStr() + CodeUtil.getFourCode(FeeCodeUtil.getCode(IConst.ACCOUNTS_MESS), 4));
            accountsMess.setApplyId(user.getId());
            accountsMess.setApplyName(user.getName());
            accountsMess.setCompanyCode(user.getCompanyCode());
            accountsMess.setCreateUser(user.getId());
            accountsMess.setUpdateUser(user.getId());
            accountsMess.setArticleTime(map.get("releaseStateTime1").toString()+"~"+map.get("releaseEndTime1").toString());
            accountsMess.setState(2);
            accountsMess.setSessionSum(Double.parseDouble(map.get("session").toString()));
            accountsMess.setCostSum(Double.parseDouble(map.get("cost").toString()));
            accountsMess.setOfferSum(Double.parseDouble(map.get("offerSum").toString()));
            accountsMess.setMessSum(Double.parseDouble(map.get("messSum").toString()));
            accountsMess.setCustCompanyId(Integer.parseInt(map.get("companyId").toString()));
            accountsMess.setCustId(Integer.parseInt(map.get("dockingId").toString()));
            accountsMess.setCustName(map.get("custName").toString());
            accountsMess.setCustCompanyName(map.get("companyName").toString());
            accountsMessMapper.saveAccountsMess(accountsMess);
            List<Map<String,Object>> l  =  new ArrayList<>();
            String articleIdS = map.get("artId").toString();
            String[] articleId  = articleIdS.split(",|\\[|\\]" );
            for (String s : articleId) {
                Map<String,Object> m = new HashMap<>();
                if (!StringUtils.isEmpty(s)){
                    m.put("messId",accountsMess.getId());
                    m.put("articleId",s);
                    //更改稿件中烂账状态
                    accountsMessMapper.updateArticle(Integer.parseInt(s),2);
                    l.add(m);
                }
            }
            accountsMessMapper.addArticle(l);
            data.putDataValue("message","操作成功");
            data.putDataValue("accountsMess",accountsMess);
            return  data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001, e.getMessage());
        }
    }

    @Override
    public ResponseData selectMessDetails(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            Map<String,Object> map = new HashMap<>();
            AccountsMess accountsMess = accountsMessMapper.selectAccountMess(id);
            map.put("userId",AppUtil.getUser().getId());
            map.put("messId",id);
            List<Map<String,Object>> articleList = accountsMessMapper.selectHaveArticleList(map);
            data.putDataValue("articleList",articleList);
            data.putDataValue("accountsMess",accountsMess);
            data.putDataValue("message","操作成功");
            return  data;
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1001,e.getMessage());
        }
    }

    @Override
    public void updateMess(AccountsMess accountsMess){
        accountsMessMapper.updateMess(accountsMess);
        if (accountsMess.getState() ==1){
            accountsMessMapper.updateArt(accountsMess.getId(),1);

        }
    }

    @Override
    public ResponseData addMessList(AccountsMess accountsMess){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            if(accountsMess.getId()==null){
                throw new QinFeiException(1002,"为获取到烂账id");
            }
            accountsMess.setUpdateTime(new Date());
            accountsMess.setApplyTime(new Date());
            accountsMess.setUpdateUser(user.getId());
            accountsMessMapper.updateMess(accountsMess);
            processService.addAccountsMessProcess(accountsMess,3);
            data.putDataValue("message","操作成功");
            return data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception b){
            b.printStackTrace();
            return ResponseData.customerError(1002,"");

        }
    }

    //删除烂账需要将稿件的烂账状态还原为0
    @Override
    public ResponseData deletMess(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            User user = AppUtil.getUser();
            AccountsMess accountsMess = new AccountsMess();
            accountsMess.setUpdateUser(user.getId());
            accountsMess.setUpdateTime(new Date());
            accountsMess.setState(-9);
            accountsMess.setId(id);
            accountsMessMapper.updateMess(accountsMess);
            //更改稿件的烂账状态
            accountsMessMapper.updateArt(id,0);
            //更改烂账稿件中间表状态
            accountsMessMapper.updateMessArt(id);
            data.putDataValue("message","操作成功");
            return  data;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"烂账删除失败");
        }
    }
    @Override
    public  ResponseData selectMessId(Integer id){
        try{
            ResponseData data = ResponseData.ok();
            if (id== null){
                throw  new QinFeiException(1002,"未查询到稿件id");
            }
            Integer messId= accountsMessMapper.selectMessId(id);
            data.putDataValue("messId",messId);
            return data;
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"获取烂账id失败");
        }

    }

}
