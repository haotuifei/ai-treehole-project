package com.zxw.treehole;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智能伴学与情绪疏导 AI 树洞系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.zxw.treehole.mapper")
public class TreeholeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TreeholeApplication.class, args);
    }
}
