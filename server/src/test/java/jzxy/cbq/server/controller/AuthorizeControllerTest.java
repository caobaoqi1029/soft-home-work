package jzxy.cbq.server.controller;

import jakarta.annotation.Resource;
import jzxy.cbq.server.entity.vo.request.ConfirmResetVO;
import jzxy.cbq.server.entity.vo.request.EmailRegisterVO;
import jzxy.cbq.server.entity.vo.request.EmailResetVO;
import jzxy.cbq.server.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuthorizeControllerTest {
    @Resource
    AccountService service;

    @Test
    void askVerifyCode() {
        Assertions.assertNull(service.registerEmailVerifyCode("register", "2024cbq@gmail.com", "127.0.0.1"));
    }

    @Test
    void register() {
        EmailRegisterVO vo = new EmailRegisterVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setUsername("cbq");
        vo.setCode("TODO");
        vo.setPassword("cbq@cb.123");
        Assertions.assertNull(service.registerEmailAccount(vo));
    }

    @Test
    void resetConfirm() {
        ConfirmResetVO vo = new ConfirmResetVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setCode("TODO");
        Assertions.assertNull(service.resetConfirm(vo));
    }

    @Test
    void resetPassword() {
        EmailResetVO vo = new EmailResetVO();
        vo.setEmail("2024cbq@gmail.com");
        vo.setCode("826660");
        vo.setPassword("cb@cbq.456");
        Assertions.assertNull(service.resetEmailAccountPassword(vo));
    }
}