package tech.alexib.tools.vaultCon.config

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.vault.core.ReactiveVaultTemplate
import org.springframework.vault.core.listAsFlow
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json


@Configuration
class Router(
    private val handler: RouteHandler,
    private val otherHandler: OtherHandler
) {

    @Bean
    fun routes() = coRouter {
        "/test".nest {
            GET("", handler::handle)
        }
        "/test2".nest {
            GET("", otherHandler::handle)
        }
    }


}

private val logger = KotlinLogging.logger { }

@Component
class RouteHandler(
    private val dataConfig: DataConfig,
    private val appProps: AppProps
) {


    suspend fun handle(request: ServerRequest): ServerResponse {

        logger.info {
            """data props
                $dataConfig
            """.trimIndent()
        }
        logger.info {
            """
            app props
            $appProps
        """.trimIndent()
        }
        return ServerResponse.ok().json().bodyValue(dataConfig.other).awaitFirst()
    }


}


@Component
class OtherHandler(

) {

    @Autowired
    private lateinit var dataConfig: DataConfig

    @Autowired
    private lateinit var appProps: AppProps

    @Autowired
    private lateinit var reactiveVaultTemplate: ReactiveVaultTemplate

    suspend fun handle(request: ServerRequest): ServerResponse {

        logger.info {
            """
                data props
                $dataConfig
            """.trimIndent()
        }
        logger.info {
            """
            app propes
            $appProps
        """.trimIndent()
        }


        val contest = reactiveVaultTemplate.listAsFlow("secret/metadata/contest")

        val info = contest.toList()
        logger.info {
            """
                contest

                $info

            """.trimIndent()
        }

        return ServerResponse.ok().json().bodyValue(info).awaitFirst()
    }

}
