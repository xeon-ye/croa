package com.qinfei.qferp.controller.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.ReceiveReturn;
import com.qinfei.qferp.service.inventory.IReceiveReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 物品归还控制器
 */
@RequestMapping("/return")
@Controller
public class ReturnController {
    @Autowired
    private IReceiveReturnService returnService;

    /**
     * 获取物品报修code
     * @return
     */
    @RequestMapping("/getReturnCode")
    @ResponseBody
    public ResponseData getReturnCode(){
        ResponseData data = ResponseData.ok();
        String code = returnService.getReturnCode();
        data.putDataValue("code",code);
        return data;
    }

    /**
     * 根据报修id查询
     * @param id
     * @return
     */
    @RequestMapping("/getById")
    @ResponseBody
    public ResponseData getById(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        ReceiveReturn entity= returnService.queryById(id);
        data.putDataValue("entity",entity);
        return data;
    }

    /**
     * 添加物品归还申请
     * @param receiveReturn
     * @return
     */
    @RequestMapping("/saveReturn")
    @ResponseBody
    public ResponseData saveReceiveReturn(ReceiveReturn receiveReturn){
        try {
            ResponseData data = ResponseData.ok();
            ReceiveReturn obj = returnService.addReturn(receiveReturn);
            data.putDataValue("entity",obj);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，添加物品归还信息出错啦，请联系技术人员");
        }
    }

    /**
     * 修改物品归还申请
     * @return
     */
    @RequestMapping("/editReturn")
    @ResponseBody
    public ResponseData editReceiveReturn(ReceiveReturn receiveReturn){
        try {
            ResponseData data = ResponseData.ok();
            returnService.editReturn(receiveReturn);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，修改物品归还信息出错啦，请联系技术人员");
        }
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @RequestMapping("/delReturn")
    @ResponseBody
    public ResponseData delReturn(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            data.putDataValue("message","操作成功");
            returnService.deleteReturn(id);
            return data;
        } catch (QinFeiException e) {
           return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，删除物品归还申请出错啦，请联系技术人员");
        }
    }
}
