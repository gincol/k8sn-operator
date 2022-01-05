package com.vn.utils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Utils {
	
	public static Map<String, Object> createDockerConfig(String dockerServer, String username, String password) {
        Map<String, Object> dockerConfigMap = new HashMap<>();
        Map<String, Object> auths = new HashMap<>();
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        String usernameAndPasswordAuth = username + ":" + password;
        credentials.put("auth", Base64.getEncoder().encodeToString(usernameAndPasswordAuth.getBytes()));
        auths.put(dockerServer, credentials);
        dockerConfigMap.put("auths", auths);

        return dockerConfigMap;
    }

}
