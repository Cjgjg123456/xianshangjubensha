package org.example.jubensha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 线上剧本杀系统启动类
 */
@SpringBootApplication
@MapperScan("org.example.jubensha.mapper") // 扫描Mapper接口
public class JubenshaApplication {
    public static void main(String[] args) {
        SpringApplication.run(JubenshaApplication.class, args);
    }
}