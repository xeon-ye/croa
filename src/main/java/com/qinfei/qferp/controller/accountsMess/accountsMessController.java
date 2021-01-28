package com.qinfei.qferp.controller.accountsMess;

import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.accountsMess.AccountsMess;
import com.qinfei.qferp.service.accountsMess.IAccountsMessService;
import com.qinfei.qferp.service.fee.IAccountService;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/accountMess")
public class accountsMessController {
    @Autowired
    private IAccountsMessService accountsMessService;

    /*
    * 查询自己稿件中的业务员为当前人的稿件*/
    @RequestMapping("/accountsMessList")
    @ResponseBody
    public Map<String,Object> accountsMessList(@RequestParam Map<String,Object>map){
        return accountsMessService.accountsMessList(map);
    }

    /**
     * 加载当前用户客户列表
     * @param map
     * @param
     * @return
     */
    @RequestMapping("/dockingListTable")
    @ResponseBody
    public Map<String,Object> dockingListTable (@RequestParam Map<String,Object> map){
        return accountsMessService.dockingListTable(map);
    }

    /**
     * 保存选中烂账稿件信息
     */
    @RequestMapping("/saveMessArticle")
    @ResponseBody
    public ResponseData saveMessArticle(@RequestBody  Map<String,Object> map){
        return accountsMessService.saveMessArticle(map);

    }

    /**
     * 查询烂账列表
     */
    @RequestMapping("/accountMessListTable")
    @ResponseBody
    public Map<String,Object> accountMessListTable(@RequestParam Map<String,Object> map){
        return accountsMessService.accountMessListTable(map);
    }

    @RequestMapping("/selectMessDetails")
    @ResponseBody
    public ResponseData selectMessDetails(@RequestBody Map<String,Object> map){
        try{
            if (StringUtils.isEmpty(map.get("id").toString())){
                throw new QinFeiException(1002,"为获取到烂账id");
            }
            Integer id = Integer.parseInt(map.get("id").toString());
            return accountsMessService.selectMessDetails(id);
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"更据制度id查询制度错误，请联系系统管理员");

        }
    }
    //烂账提交申请

    @RequestMapping("/addMessList")
    @ResponseBody
    public ResponseData addMessList(AccountsMess accountsMess){
        try{
            return accountsMessService.addMessList(accountsMess);
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            return ResponseData.customerError(1002,"烂账添加失败");
        }
    }
    /**
     * 烂账删除
     */
    @RequestMapping("/deleteMess")
    @ResponseBody
    public ResponseData deleteMess(Integer id){
        try {
            if (id == null){
                throw new QinFeiException(1002,"未获取到烂账id");
            }
            return accountsMessService.deletMess(id);
        }catch (QinFeiException b){
            return ResponseData.customerError(b.getCode(),b.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            return ResponseData.customerError(1002,"烂账删除失败");

        }
    }

    @RequestMapping("/selectMessId")
    @ResponseBody
    public ResponseData selectMessId(Integer id){
        return accountsMessService.selectMessId(id);
    }

}
