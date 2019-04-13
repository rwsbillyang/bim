package cn.niukid.bim.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodesk.client.auth.OAuth2ThreeLegged;
import com.autodesk.client.auth.OAuth2TwoLegged;
import com.autodesk.client.auth.ThreeLeggedCredentials;

import cn.niukid.bim.Config;
import cn.niukid.bim.OAuth;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/oauth")
public class OAuthController {
	private static final Logger log = LoggerFactory.getLogger(OAuthController.class);
	

	
	private OAuth2ThreeLegged oauth2ThreeLegged;
	private ThreeLeggedCredentials threeLeggedCredentials;
	

	
	@GetMapping(value = "/getAccessToken")
	public Mono<Map<String, String>> getAccessToken() 
	{
		Map<String, String> map = new HashMap<String, String>();
		
		try {
			OAuth2TwoLegged forgeOAuth = OAuth.getOAuthPublic();
			 String token = forgeOAuth.getCredentials().getAccessToken();
	         // this is a timestamp, not the exact value of expires_in, so calculate back
	         // client side will need this. though not necessary
	         long expire_time_from_SDK = forgeOAuth.getCredentials().getExpiresAt();
	         // because we do not know when the token is got, align to current time
	         // which will be a bit less than what Forge sets (say 3599 seconds). This makes
	         // sense.
	         Long expires_in = (expire_time_from_SDK - DateTime.now().toDate().getTime()) / 1000;
	         // send to client
	         log.info("token="+token);
			 map.put("accessToken", token);
			 
			 map.put("expiresIn", expires_in.toString());
			 map.put("token_type", "Bearer");
		} catch (Exception e) {
			e.printStackTrace();
			map.put("ok", "ko");
			map.put("msg", "Exception");
			
		}
        
		return Mono.just(map);
	}
	
	

	public String make3LeggedRequest(List<String> scopes,String redirectUrl) 
	{

		if(StringUtils.isEmpty(redirectUrl))
		{
			return null;
		}else
		{
			try {
				redirectUrl = URLEncoder.encode(redirectUrl,"utf-8");
				
				// Initialize the 3-legged OAuth 2.0 client, and optionally set specific scopes.
				// If you omit scopes, the generated token will have all scope permissions.
				// Set autoRefresh to `true` to automatically refresh the access token when it expires.
				// Note that the REDIRECT_URL must match the callback URL you provided when you created the app.
				 oauth2ThreeLegged = new OAuth2ThreeLegged(Config.ClientId, Config.ClientSecret,redirectUrl, scopes, true);

				// Generate a URL page that asks for permissions for the specified scopes.
				String oauthUrl = oauth2ThreeLegged.getAuthenticationUrl();
				
				//Redirect the user to authUrl (the user consent page).
				return oauthUrl;
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@GetMapping(value = "/callback")
	public void callback(String authorizationCode) 
	{
		if(oauth2ThreeLegged!=null) {
			try {
				threeLeggedCredentials = oauth2ThreeLegged.getAccessToken(authorizationCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			log.warn("oauth2ThreeLegged is null");
		}
	}
}
