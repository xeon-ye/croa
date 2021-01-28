package com.qinfei.qferp.controller.sms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qinfei.core.ResponseData;
import com.qinfei.qferp.service.sms.ISmsService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("sms")
@Api(description = "发送短信接口")
public class SmsController {

	@Autowired
	private ISmsService smsService;

	@PostMapping("/verify")
	@ApiOperation(value = "发送短信", notes = "发送短信")
	@ResponseBody
	public ResponseData signUp(String phone) {
		try {
			return smsService.sendVerifyCode(phone);
		} catch (Exception byeException) {
			return ResponseData.customerError();
		}
	}
}
