package tech.alexib.tools.vaultCon

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.vault.authentication.AuthenticationSteps
import org.springframework.vault.authentication.AuthenticationStepsFactory
import org.springframework.vault.authentication.AuthenticationStepsOperator
import org.springframework.vault.authentication.AzureMsiAuthentication
import org.springframework.vault.authentication.AzureMsiAuthenticationOptions
import org.springframework.vault.authentication.ClientAuthentication
import org.springframework.vault.client.ReactiveVaultClients
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.client.WebClientFactory
import org.springframework.vault.config.AbstractReactiveVaultConfiguration
import org.springframework.vault.support.VaultToken
import java.net.URI

private val endPoint = "http://10.2.128.8:8200"
private val options = AzureMsiAuthenticationOptions.builder()
	.instanceMetadataUri(URI.create("http://169.254.169.254/metadata/instance?api-version=2017-08-01"))
	.identityTokenServiceUri(URI.create("http://169.254.169.254/metadata/identity/oauth2/token?api-version=2018-02-01&resource=https://management.azure.com/"))
	.role("groomo-role")
	.build()



class VaultConfig(
private val	connector: ClientHttpConnector
):ClientAuthentication,AuthenticationStepsFactory{


	private val webClientFactory = WebClientFactory {
		ReactiveVaultClients.createWebClient(VaultEndpoint.from(URI(endPoint)),connector)
	}

	private val webClient = webClientFactory.create()


	override fun getAuthenticationSteps(): AuthenticationSteps {
		return AzureMsiAuthentication.createAuthenticationSteps(options)
	}

	override fun login(): VaultToken {
	return runBlocking {
		AuthenticationStepsOperator(authenticationSteps,webClient).vaultToken.awaitFirstOrNull()
		} ?: throw IllegalStateException("NO TOKEN")
	}
}

@Profile("!dev")
@Configuration
class SConfig:AbstractReactiveVaultConfiguration(){

	@Bean
	override fun vaultEndpoint(): VaultEndpoint {
		return VaultEndpoint.from(URI(endPoint))
	}

	@Bean
	fun connector(): ClientHttpConnector {
		return clientHttpConnector()
	}
	@Bean
	override fun clientAuthentication(): ClientAuthentication {
		return VaultConfig(connector())
	}

}



@SpringBootApplication
@EnableConfigurationProperties
class VaultConApplication

fun main(args: Array<String>) {
	runApplication<VaultConApplication>(*args)
}


