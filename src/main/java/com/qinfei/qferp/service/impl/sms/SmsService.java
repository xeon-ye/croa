package com.qinfei.qferp.service.impl.sms;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;

import com.aliyuncs.exceptions.ClientException;
import com.qinfei.core.ResponseData;
import com.qinfei.qferp.entity.sys.User;
import com.qinfei.qferp.service.sms.ISmsService;
import com.qinfei.qferp.utils.NumberUtils;
import com.qinfei.qferp.utils.SmsUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService implements ISmsService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private SmsUtils smsUtils;

	@Value("${sms.sendMsgSwitch}")
	private String sendMsgSwitch;
	
	// redis前缀
	static final String KEY_PREFIX = "user";
	// redis计数器
	public static final String Key_Time = "verifyCode5";
	// redis时间限制
	public static final String Key_TimePoint = "verifyCode560";

	static final Logger logger = LoggerFactory.getLogger(SmsUtils.class);

	@Override
	public ResponseData checkSms(User user) {
		String count = redisTemplate.opsForValue().get(Key_Time + user.getPhone());
		String code = redisTemplate.opsForValue().get(KEY_PREFIX + count + user.getPhone());
		if (StringUtils.isNoneEmpty(code) && StringUtils.equals(code, user.getVerifyCode())) {
			try {
				//登陆成功，删除计数器，重新计数--其他两个设置了超时时间，删不删都行。
				redisTemplate.delete(Key_Time + user.getPhone());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ResponseData.ok();
		}
		return ResponseData.customerError(1002, "验证失败");
	}

	@Override
	public ResponseData sendVerifyCode(String phone) throws ClientException {
		try {
			// 计数用户请求次数
			
			
			RedisAtomicLong entityIdCounter = new RedisAtomicLong(Key_Time + phone, redisTemplate.getConnectionFactory());
			long count = entityIdCounter.get();
			
			if (count == 0) {
				sendSms(phone, count);
			} else {
				long date2 = System.currentTimeMillis();
				long date=0;
				String string = redisTemplate.opsForValue().get(Key_TimePoint+count + phone);
				if(StringUtils.isEmpty(string)) {
					sendSms(phone, count);
				}else {
					date = Long.parseLong(redisTemplate.opsForValue().get(Key_TimePoint+count + phone));
					if (date2 - date < 60 * 1 * 1000) {
						return ResponseData.customerError(201, "慢点哦，请60S后重试");
					} else {
						sendSms(phone, count);
					}
				}
				
			}
			ResponseData ok = ResponseData.ok();
			ok.putDataValue("count", "输入第"+(count+1)+"次验证码");
			ok.putDataValue("phone", phone);
			return ok;
		} catch (Exception e) {
			logger.error("发送短信失败。phone：{}， code：{}", phone);
			return ResponseData.customerError(200, "注册失败请重试");
		}
	}

	public void sendSms(String phone, long count) throws ClientException {
		String code = NumberUtils.generateCode(6);
		Long increment = redisTemplate.boundValueOps(Key_Time + phone).increment(1);
		if(StringUtils.equals("true", sendMsgSwitch)) {
			smsUtils.sendSms(phone, code, count);
		}
		logger.info("发送验证短信,手机号:" + phone + "序号为count" + "验证码为:" + count);
		redisTemplate.opsForValue().set(KEY_PREFIX + increment + phone, code, 5, TimeUnit.MINUTES);
		String date = String.valueOf(System.currentTimeMillis());
		redisTemplate.opsForValue().set(Key_TimePoint + increment + phone, date,5, TimeUnit.MINUTES);
	}
}
