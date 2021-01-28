package com.qinfei.qferp.controller.sys;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.service.sys.IUserHomeModalService;
import com.qinfei.qferp.utils.AppUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by yanhonghao on 2019/10/8 14:53.
 */
@RestController
@RequestMapping("/user/home/modal")
public class UserHomeModalController {
    private final IUserHomeModalService homeModalService;

    public UserHomeModalController(IUserHomeModalService homeModalService) {
        this.homeModalService = homeModalService;
    }

    @PostMapping
    public ResponseData save(String homeModal) {
        Integer id = AppUtil.getUser().getId();
        homeModalService.save(id, homeModal);
        return ResponseData.ok();
    }

    @PutMapping
    public ResponseData update(String homeModal) {
        Integer id = AppUtil.getUser().getId();
        homeModalService.updateByUserId(id, homeModal);
        return ResponseData.ok();
    }

    @GetMapping
    public ResponseData findByUserId() {
        Integer id = AppUtil.getUser().getId();
        return ResponseData.ok().putDataValue("result", homeModalService.findHomeModalByUserId(id));
    }
}
