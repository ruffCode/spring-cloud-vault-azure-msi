package tech.alexib.tools.vaultCon.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
//@RefreshScope
@ConfigurationProperties("env")
data class DataConfig(
    var environment:String = "",
    var other:String = ""
)

@Component
//@RefreshScope
@ConfigurationProperties("pass")
data class AppProps(
    var word:String = ""
)
