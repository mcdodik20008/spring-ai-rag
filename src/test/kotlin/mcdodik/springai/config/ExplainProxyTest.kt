package mcdodik.springai.config

import mcdodik.springai.config.testcontainer.AbstractPgIT
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ExplainProxyDsPostProcessorConfig::class)
class ExplainProxyTest
    @Autowired
    constructor(
        private val jdbc: JdbcTemplate,
    ) : AbstractPgIT() {
        @Test
        fun `logs explain analyze on select`() {
            println("jdbc: $jdbc")
            jdbc.execute("CREATE TABLE IF NOT EXISTS t(id serial primary key, v text)")
            jdbc.update("INSERT INTO t(v) VALUES (?)", "a")
            jdbc.queryForList("SELECT * FROM t WHERE v = ?", "a")
        }
    }
