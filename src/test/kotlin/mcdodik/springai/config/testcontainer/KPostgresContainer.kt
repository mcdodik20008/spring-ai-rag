package mcdodik.springai.config.testcontainer

import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

open class KPostgresContainer(
    image: DockerImageName =
        DockerImageName
            .parse("ankane/pgvector")
            .asCompatibleSubstituteFor("postgres"),
) : PostgreSQLContainer<KPostgresContainer>(image)

object PgTC : KPostgresContainer() {
    init {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        // быстрее локально (включи также ~/.testcontainers.properties -> testcontainers.reuse.enable=true)
        withReuse(true)
        // Подождём логи Postgres, чтобы Flyway не стрелял в «полумёртвую» БД
        waitingFor(
            Wait
                .forLogMessage(".*database system is ready to accept connections.*", 1)
                .withStartupTimeout(Duration.ofSeconds(60)),
        )
        // опционально:
        // withTmpFs(mapOf("/var/lib/postgresql/data" to "rw"))  // быстрая ephemeral FS
        // withInitScript("db/init.sql") // если нужен сид
        start()
    }
}
