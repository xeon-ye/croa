package com.qinfei.qferp.controller.inventory;
import com.qinfei.core.ResponseData;
import com.qinfei.core.exception.QinFeiException;
import com.qinfei.qferp.entity.inventory.ReceiveScrap;
import com.qinfei.qferp.service.inventory.IReceiveScrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 物品报废控制器
 */
@RequestMapping("/scrap")
@Controller
public class ScrapController {
    @Autowired
    private IReceiveScrapService scrapService;

    /**
     * 获取物品报修code
     * @return
     */
    @RequestMapping("/getScrapCode")
    @ResponseBody
    public ResponseData getScrapCode(){
        ResponseData data = ResponseData.ok();
        String code = scrapService.getScrapCode();
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
        ReceiveScrap scrap = scrapService.queryById(id);
        data.putDataValue("entity",scrap);
        return data;
    }

    /**
     * 添加物品报废
     * @return
     */
    @RequestMapping("/saveScrap")
    @ResponseBody
    public ResponseData saveScrap(ReceiveScrap scrap){
        try {
            ResponseData data = ResponseData.ok();
            ReceiveScrap sc = scrapService.addScrap(scrap);
            data.putDataValue("entity",sc);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，添加物品报废信息出错啦，请联系技术人员");
        }
    }

    /**
     * 修改物品报废
     * @return
     */
    @RequestMapping("/editScrap")
    @ResponseBody
    public ResponseData editScrap(ReceiveScrap scrap){
        try {
            ResponseData data = ResponseData.ok();
            scrapService.editScrap(scrap);
            data.putDataValue("message","操作成功");
            return data;
        } catch (QinFeiException e) {
            return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，修改物品报废信息出错啦，请联系技术人员");
        }
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @RequestMapping("/deleteScrap")
    @ResponseBody
    public ResponseData deleteScrap(@RequestParam("id")Integer id){
        ResponseData data = ResponseData.ok();
        try {
            data.putDataValue("message","操作成功");
            scrapService.deleteScrap(id);
            return data;
        } catch (QinFeiException e) {
           return ResponseData.customerError(e.getCode(),e.getMessage());
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseData.customerError(1002,"抱歉，删除物品报废申请出错啦，请联系技术人员");
        }
    }
}
