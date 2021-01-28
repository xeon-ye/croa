package com.qinfei.qferp.controller.mediauser;

import com.qinfei.core.ResponseData;
import com.qinfei.core.annotation.Verify;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.core.log.OperateType;
import com.qinfei.core.log.annotation.Log;
import com.qinfei.core.utils.DateUtils;
import com.qinfei.qferp.entity.biz.Article;
import com.qinfei.qferp.service.mediauser.IMediaUserService1;
import com.qinfei.qferp.utils.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/mediauser1")
public class MediaUserController1 {
    @Autowired
    IMediaUserService1 mediaUserService;

    @Verify(code = "/mediauser/turnDown", module = "媒介管理/稿件驳回")
    @RequestMapping("/turnDown")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "媒介管理", note = "稿件驳回")
    public ResponseData turnDown(Article article){
        try{
            mediaUserService.turnDown(article);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "稿件驳回异常！");
        }
    }

    @Verify(code = "/mediauser/arrange", module = "媒介管理/稿件安排")
    @RequestMapping("/arrange")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "媒介管理", note = "稿件安排")
    public ResponseData arrange(Article article){
        try{
            mediaUserService.arrange(article);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(), e.getMessage());
        }catch (Exception e){
            e.printStackTrace();
            throw new QinFeiException(1002, "稿件安排异常！");
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true)); // true:允许输入空值，false:不能为空值
    }

    @Verify(code = "/mediauser/publish", module = "媒介管理/发布稿件")
    @RequestMapping("/publish")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "媒介管理", note = "发布稿件")
    public ResponseData publish(@RequestParam Map map, @RequestParam(value = "updatePrice",required = false) Integer updatePrice){
        try{
            mediaUserService.publish(map,updatePrice);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            log.error("稿件发布异常",e);
            return ResponseData.customerError(1002,"稿件发布异常！");
        }
    }

    @Verify(code = "/mediauser/yj", module = "媒介管理/移交稿件")
    @RequestMapping("/yj")
    @ResponseBody
//    @Log(opType = OperateType.UPDATE, module = "媒介管理", note = "移交稿件")
    public ResponseData yj(@RequestParam("artId") String artId,@RequestParam("mediaUserId") Integer mediaUserId,@RequestParam("mediaUserName") String mediaUserName){
        try{
            mediaUserService.yj(artId,mediaUserId, mediaUserName);
            return ResponseData.ok();
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            return ResponseData.customerError(1002,"稿件移交异常！");
        }
    }

    @RequestMapping("/priceFloat")
    @ResponseBody
    public ResponseData priceFloat(Article article){
        try{
            boolean b = mediaUserService.priceFloat(article);
            ResponseData responseData = ResponseData.ok();
            responseData.putDataValue("b",b);
            return responseData;
        }catch (QinFeiException e){
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e){
            log.error("获取价格浮动异常",e);
            return ResponseData.customerError(1002,"获取价格浮动异常");
        }
    }

    @RequestMapping("exportTemplate")
    @ResponseBody
//    @Log(opType = OperateType.ADD, note = "批量下单", module = "媒体管理/媒介")
    public void exportTemplate(HttpServletResponse response, @RequestParam Map<String,Object> map) {
        try {
            response.setContentType("application/binary;charset=UTF-8");
            String name = URLEncoder.encode("稿件批量导入模板"+ AppUtil.getUser().getName() + DateUtils.getNowTime(DateUtils.DATE_SMALL) + ".xlsx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;fileName=" + name);
            OutputStream outputStream = response.getOutputStream();
            mediaUserService.exportTemplate(map, outputStream);
        } catch (Exception e) {
            log.error("导出模板失败", e);
        }
    }
}
