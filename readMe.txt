# This is a sample client for oauth-seam-app. Two kind of client implementation example is there. 
	
	1. SimpleRestOAuthClient0Legged: 0-legged implementation does not involve any Token Exchange
	
	2. SimpleRestOAuthClient: This is 3-legged implementation, though the customer verification flow is not implemented here (check the comment in the class - Authorize the RequestToken, receive a Verifier). 
							  For typical 3-legged based authorization, following steps should be followed: 
							  	1. After customer gets the request token the user should be redirected to the service provider's screen.
							  	2. Standing in the provider's login screen the user will give the credential and will login. 
							  	3. After successful login, a prompt comes asking whether the user is allowing the customer to access her content. 
							  	4. If the user agrees, the /confirm url will be called. This will in turn generate the oauth_verifier and the call_back url will be called to redirect the user into the Customer application.