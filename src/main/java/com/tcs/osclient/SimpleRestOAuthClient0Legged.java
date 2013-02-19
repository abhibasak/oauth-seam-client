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

import org.apache.commons.httpclient.util.URIUtil;

/**
 * 0-legged OAuth client.
 * No token (request / access) involved in this process, authentication only done on the basis
 * of the Consumer Key
 * 
 * @author 256396
 *
 */
public class SimpleRestOAuthClient0Legged {
	
	public static void main(String args[]) {
		String baseUrl = "http://localhost:8080";
		
		final String serviceURLGET = baseUrl + "/oauth-seam/seam/resource/oauth-seam-rest/news-feed/cricket";
		final String serviceURLPOST = baseUrl + "/oauth-seam/seam/resource/oauth-seam-rest/news-feed/post-news";
		
		final String CLIENT_IDENTIFIER = "OAUTH-WEB-SERVICE";
		final String CLIENT_SECRET = "2de8a308-1fe5-4216-89f9-963c0bd17cd5";
		
		try {
			OAuthServiceProvider provider = new OAuthServiceProvider(
					null, null, null);
			
			OAuthConsumer consumer = new OAuthConsumer(null, CLIENT_IDENTIFIER,
					CLIENT_SECRET, provider);
			consumer.setProperty(OAuthClient.PARAMETER_STYLE,
					ParameterStyle.BODY);
			
			OAuthAccessor accessor = new OAuthAccessor(consumer);
			
			OAuthClient client = new OAuthClient(new HttpClient4());
			
			/*
			 * POST Service (to be used for RASE-CBT communication)
			 * 
			 * The sample xml as delivered by Craig has been read here for testing purpose
			 * "responseString" below is holding the response message and sent to CBT by "assessmentResult" form parameter
			 * "serviceURLPOST" is the CBT service url, as of now this is kept as "http" as we are still working with SSL
			 */
			String responseString = "";
			try {
				BufferedReader in = new BufferedReader(new FileReader("news-feed-sample2.xml"));
				String str;
				while ((str = in.readLine()) != null) {
					responseString = responseString + str;
				}
				in.close();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			
			String[] postParam = new String[2]; 
			postParam[0] = "newsFeed";
			postParam[1] = URIUtil.encodeAll(responseString);
			final OAuthMessage responsePOST = client.invoke(accessor,
					OAuthMessage.POST, serviceURLPOST,
					OAuth.newList(postParam));
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
