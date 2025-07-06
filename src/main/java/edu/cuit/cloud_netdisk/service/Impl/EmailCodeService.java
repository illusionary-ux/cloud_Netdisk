package edu.cuit.cloud_netdisk.service.Impl;

import edu.cuit.cloud_netdisk.pojo.entity.VerificationCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class EmailCodeService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    //存储验证码
    private final Map<String, VerificationCode> codeStore = new HashMap<String, VerificationCode>();

    /**
     * 生成并发送验证码
     * @param email
     */
    public void sendVerificationCode(String email) {

        //生成6位随机验证码
        String code = String.format("%06d",new Random().nextInt(999999));

        //存储验证码，5分钟内有效
        codeStore.put(email,VerificationCode.builder().code(code).expireTime(System.currentTimeMillis()+300000).build());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的登录验证码");
        message.setText("您的登录验证码是："+code+"\n验证码5分钟内有效");

        mailSender.send(message);
        log.info("验证码已发送");

    }

    /**
     * 检验验证码
     */
    public boolean VerifyCode(String email, String code) {
        VerificationCode stored = codeStore.get(email);
        if (stored == null) {
            return false;
        }
        if (stored.getCode().equals(code) && stored.getExpireTime() > System.currentTimeMillis()) {
            codeStore.remove(email);
            return true;
        }
        return false;
    }
}
