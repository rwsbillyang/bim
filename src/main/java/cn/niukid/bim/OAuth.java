package cn.niukid.bim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.autodesk.client.auth.OAuth2TwoLegged;


public class OAuth {
	private static final Logger log = LoggerFactory.getLogger(OAuth.class);
	public static class credentials {
		// public static String client_id = System.getenv("FORGE_CLIENT_ID");
		// public static String client_secret = System.getenv("FORGE_CLIENT_SECRET");


	};

	// Required scopes for your application on server-side
	private static ArrayList<String> scopeInternal = new ArrayList<String>() {
		{
			add("bucket:create");
			add("bucket:read");
			add("bucket:update");
			add("bucket:delete");
			
			add("data:search");
			add("data:read");
			add("data:create");
			add("data:write");
			
			add("viewables:read");
		}
	};

	// Required scope of the token sent to the client
	private static ArrayList<String> scopePublic = new ArrayList<String>() {
		{
			add("viewables:read");
		}
	};

	//private static Credentials twoLeggedCredentials = null;

	private static Map<String, OAuth2TwoLegged> _cached = new HashMap<String, OAuth2TwoLegged>();

	private static OAuth2TwoLegged getOAuth2TwoLegged(ArrayList<String> scopes, String cache) throws Exception {

		// API call of Forge SDK will refresh credentials (token etc) automatically
		// so, store the oauth objects only
		// public scope and internal scope separately
		if (_cached.containsKey(cache)) {
			return (OAuth2TwoLegged) _cached.get(cache);
		} else {

			OAuth2TwoLegged forgeOAuth = createOAuth2TwoLegged(scopes);
			// in the first time, call authenticate once to initialize the credentials
			forgeOAuth.authenticate();
			
			log.info("scopes.size="+scopes.size()+",accessToken="+forgeOAuth.getCredentials().getAccessToken());
			
			_cached.put(cache, forgeOAuth);
			return forgeOAuth;
		}
	}

	// build the oAuth object with public scope
	public static OAuth2TwoLegged getOAuthPublic() throws Exception {
		return getOAuth2TwoLegged(scopePublic, "public");
	}

	// build the oAuth object with internal scope
	public static OAuth2TwoLegged getOAuthInternal() throws Exception {
		return getOAuth2TwoLegged(scopeInternal, "internal");
	}

	public static OAuth2TwoLegged createOAuth2TwoLegged(ArrayList<String> scopes) throws Exception {

		if (scopes == null)
			scopes = scopeInternal;

		// by the 3rd parameter, the oAuth object will refresh credentials (token etc)
		// automatically
		return new OAuth2TwoLegged(Config.ClientId, Config.ClientSecret, scopes, Boolean.valueOf(true));

	}
}
