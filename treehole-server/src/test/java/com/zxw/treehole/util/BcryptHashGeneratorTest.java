package com.zxw.treehole.util;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 需要重新生成 BCrypt 样例时去掉 @Disabled，执行：mvn test -Dtest=BcryptHashGeneratorTest
 */
@Disabled("仅本地按需生成密文，勿在 CI 中启用")
class BcryptHashGeneratorTest {

    @Test
    void printHashes() {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("admin123: " + enc.encode("admin123"));
        System.out.println("counselor123: " + enc.encode("counselor123"));
        System.out.println("student123: " + enc.encode("student123"));
        System.out.println("123456: " + enc.encode("123456"));
    }
}
