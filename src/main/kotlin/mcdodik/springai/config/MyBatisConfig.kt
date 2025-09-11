package mcdodik.springai.config

import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan(basePackages = ["mcdodik.springai.db.mybatis.mapper"])
class MyBatisConfig
