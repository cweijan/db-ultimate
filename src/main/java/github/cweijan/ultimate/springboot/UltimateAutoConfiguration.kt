package github.cweijan.ultimate.springboot

import github.cweijan.ultimate.core.DbUltimate
import github.cweijan.ultimate.db.config.DbConfig
import github.cweijan.ultimate.util.Log
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(DbConfig::class)
open class UltimateAutoConfiguration {

    @Autowired
    private val dbConfig: DbConfig? = null
    @Autowired(required = false)
    private val dataSource: DataSource? = null

    @Bean
    open fun createUltimate(): DbUltimate? {

        if (null == dbConfig || !dbConfig.enable) {
            Log.logger.debug("Db-ultimate is disabled, skip..")
            return null
        }

        if (dataSource != null) {
            Log.logger.debug("use datasource init dbultimate..")
            return DbUltimate(DbConfig(dataSource))
        }

        if (null == dbConfig.username || null == dbConfig.password || null == dbConfig.driver || null == dbConfig.url) {
            Log.logger.error("db config not set! skip init db-ultimate!")
            return null
        }

        return DbUltimate(dbConfig)
    }

}
