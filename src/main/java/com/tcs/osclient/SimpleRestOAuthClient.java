package com.tcs.osclient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.ParameterStyle;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.URIUtil;

/**
 * 3-legged OAuth client.
 * User verification is not implemented, this can be called 2-legged implementation too 
 * 
 * @author 256396
 *
 */
public class SimpleRestOAuthClient {
	
	public static void main(String args[]) {
		String baseUrl = "http://localhost:8080";
		
		final String requestTokenURL = baseUrl + "/oauth-seam/oauth/request_token";
		final String authorizeURL = baseUrl + "/oauth-seam/oauth/authorization";
		final String accessTokenURL = baseUrl + "/oauth-seam/oauth/access_token";
		
		final String serviceURLGET = baseUrl + "/oauth-seam/seam/resource/oauth-seam-rest/news-feed/sports";
		final String serviceURLPOST = baseUrl + "/oauth-seam/seam/resource/oauth-seam-rest/news-feed/post-news";
		
		final String CLIENT_IDENTIFIER = "OAUTH-WEB-SERVICE";
		final String CLIENT_SECRET = "2de8a308-1fe5-4216-89f9-963c0bd17cd5";
		
		try {
			OAuthServiceProvider provider = new OAuthServiceProvider(
					requestTokenURL, authorizeURL, accessTokenURL);
			
			OAuthConsumer consumer = new OAuthConsumer(null, CLIENT_IDENTIFIER,
					CLIENT_SECRET, provider);
			consumer.setProperty(OAuthClient.PARAMETER_STYLE,
					ParameterStyle.BODY);
			
			OAuthAccessor accessor = new OAuthAccessor(consumer);
			
			OAuthClient client = new OAuthClient(new HttpClient4());
			
			// Get a RequestToken
			try {
				client.getRequestToken(accessor, OAuthMessage.POST, null);
			} catch (Exception exception) {
				exception.printStackTrace();
				System.out.println("Exception in Getting temporary token...");
				throw exception;
			}
			
			// Authorize the RequestToken, receive a Verifier
			HttpClient httpClient = new HttpClient();
			PostMethod method = new PostMethod(authorizeURL + "/confirm");
			method.setParameter(OAuth.OAUTH_TOKEN, accessor.requestToken);
			method.setParameter("xoauth_end_user_decision", "yes");
			httpClient.executeMethod(method);
			String callbackURI = method.getResponseBodyAsString();
			String verifier = null;
			if (callbackURI != null
					&& callbackURI.indexOf("oauth_verifier=") > 0) {
				for (String params : callbackURI.split("&")) {
					if (params.contains("=")) {
						if (params.split("=")[0].equals("oauth_verifier")) {
							verifier = params.split("=")[1];
						}
					}
				}
			}
			if (verifier == null || verifier == "") {
				System.out.println("Exception in Getting verifier...");
				throw new Exception("Exception in Getting verifier...");
			}
			
			// Use the Verifier to trade the authorized RequestToken for an AccessToken
			try {
				client.getAccessToken(accessor, OAuthMessage.POST,
						OAuth.newList(OAuth.OAUTH_VERIFIER, verifier));
			} catch (Exception exception) {
				exception.printStackTrace();
				System.out.println("Exception in Getting access token...");
				throw exception;
			}
			
			/*
			 * POST Service (to be used for RASE-CBT communication)
			 * 
			 * The sample xml as delivered by Craig has been read here for testing purpose
			 * "responseString" below is holding the response message and sent to CBT by "assessmentResult" form parameter
			 * "serviceURLPOST" is the CBT service url, as of now this is kept as "http" as we are still working with SSL
			 */
			String responseString = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader("news-feed-sample1.xml"));
				String str;
				while ((str = in.readLine()) != null) {
					responseString = responseString + str;
				}
				in.close();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			final OAuthMessage responsePOST = client.invoke(accessor,
					OAuthMessage.POST, serviceURLPOST,
					OAuth.newList("newsFeed", URIUtil.encodeAll(responseString)));
			InputStream isPOST = responsePOST.getBodyAsStream();
			if (isPOST != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(isPOST));
				final StringBuffer output = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					output.append(line);
				}
				System.out.println(output.toString());
			}
			
			/*
			 * Get News Feed
			 * Sample REST GET Request
			 */
			final OAuthMessage responseGET = client.invoke(accessor,
					OAuthMessage.GET, serviceURLGET, null);
			InputStream isGET = responseGET.getBodyAsStream();
			if (isGET != null) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						isGET));
				final StringBuffer output = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					output.append(line);
				}
				System.out.println(output.toString());
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
