package jzxy.cbq.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ServerApplicationTests {

    private final PasswordEncoder encoder;
    private final ApplicationContext context;

    ServerApplicationTests(PasswordEncoder encoder, ApplicationContext context) {
        this.encoder = encoder;
        this.context = context;
    }

    @Test
    void contextLoads() {
        System.out.println(context);
    }

    @Test
    void beanDefinition() {
        String[] names = context.getBeanDefinitionNames();

        for (String name : names) {
            System.out.println(name);
        }
    }

    @Test
    void testPwd() {
        String encode = encoder.encode("cbq.monitor");
        System.out.println(encode);
        boolean matches = encoder.matches("cbq.monitor", "$2a$10$LLl5LHClLwN0ofSU4sftKunWalHM/GvAwdeKhE3UilthbVzC0vJn6");
        Assertions.assertTrue(matches);
    }
}
