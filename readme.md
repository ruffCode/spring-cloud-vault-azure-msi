##**POC - Fully automated credential/configuration boostrap**

Works on Azure VM instances using HashiCorp Vault and Azure Manged Identity

No non-development credentials are ever stored in your configuration. Credentials are "leased", renewed, and are
automatically revoked when your application stops.

For great tutorial on getting Vault set up with Azure, I would highly recommend watching this talk by
Ned Bellavance (https://github.com/ned1313)
_Azure Authentication With Vault_ https://www.youtube.com/watch?v=aM5gmMSTmJc

He also has some great courses on the subject on Pluralsight.

### **How it all works**

On start, credentials are obtained by calling several default endpoints only available inside the Azure VM instance.

First there's a call to the instance metadata URI, which returns most of the information needed to make a call to the 
Azure Identity OAuth2 endpoint, such as the subscription ID, VM name, and resource group.

For the OAuth2 call to work, you will need to have the Azure auth backend enabled in HS Vault and have role 
(azure-msi role) in linked that would allow this VM instance to obtain the Vault access token from this VM instance. The call will return a JWT
which can be used to log in to HS Vault.

All these calls are handled by Spring Vault Config, the only piece of information you need to provide is the role name.

For Consul configuration to work, there has to be role which allows Vault to issue Consul credentials and provide access
to the paths you require. Spring Config Consul takes care of obtaining the token and inserting it where needed 
(discovery/config acl_token).

Spring will take care of renewing leases on all tokens when required.

I am not 100% sure about the Spring Bootstrap configuration. It took some a lot of trial and error to get this to work.
The documentation on the new bootstrap system is a little unclear, and I couldn't get all the parts to work without 
using it.

### **Running in an Azure Function instead of a VM**

The Function App has to be using the B1 App Service Plan at a minimum, I couldn't get it to work with the Consumption 
plan.

There are also a few caveats regarding MSI roles. Vault's Azure backend requires a VM_NAME, which you will not have, 
to be part of the auth payload if the role is bound to a subscriptions or resource groups. The only way I got it to work
was bind the role to a Service Principal ID.

The Identity API has a different endpoint. The auth call is made to 
`System.getenv("IDENTITY_ENDPOINT")/?api-version=2019-08-01&resource=https://management.azure.com`
It is a GET request, and you have to include the header `{"X-IDENTITY-HEADER" : System.getenv("IDENTITY_HEADER")}`

I didn't try this part with Spring, just used Vault's Java library.

