package com.babyrhythm;


import static org.apache.log4j.Logger.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.log4j.Logger;

public class FacebookGraphUtil {

    private static final Logger log = getLogger(FacebookGraphUtil.class);

    public static final String PERMISSIONS = "user_about_me";

    private HttpHost host = new HttpHost("graph.facebook.com", 443, "https");
    private BasicHttpContext context = new BasicHttpContext();
    private HttpClient httpClient = new DefaultHttpClient();

    public String get(String get) {
        InputStream is = null;
        try {
            is = doGet(new HttpGet(get));
            BufferedReader r = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String aLine = null;
            StringBuilder sb = new StringBuilder();
            while ((aLine = r.readLine()) != null) {
                sb.append(aLine);
            }
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(get, e);
        } catch (IOException e) {
            throw new RuntimeException(get, e);
        } finally {
            close(is);
        }

    }

    private InputStream doGet(HttpGet httpGet) {
        HttpResponse response = executeRequest(httpGet);

        try {
            return response.getEntity().getContent();
        } catch (Exception e) {
            System.err.println("Error reading response. " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void close(InputStream is) {
        if (is != null)
            try {
                is.close();
            } catch (Exception e) {
                log.error("Failed to close the connection", e);
            }
    }

    private HttpResponse executeRequest(HttpRequestBase request) {
        HttpResponse response = null;
        try {
            response = httpClient.execute(host, request, context);
            if (log.isInfoEnabled())
                log.info(response.toString());
        } catch (IOException e) {
            request.abort();
            System.err.println("Error executing request. " + e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

    public static String getAccessTokenUrl(String id, String secret, String redirect, String code) {
        return "/oauth/access_token?client_id=" + id + "&redirect_uri=" + redirect + "&client_secret=" + secret
                + "&code=" + code;
    }

    public static String getOAuthUrl(String id, String redirect, String email) {
        return "https://www.facebook.com/dialog/oauth?client_id=" + id + "&redirect_uri=" + redirect + "&scope="
                + PERMISSIONS + "&state=" + generateUserState(email);
    }

    public static String getOAuthUrl(String id, String redirect) {
        return "https://www.facebook.com/dialog/oauth?client_id=" + id + "&redirect_uri=" + redirect + "&scope="
                + PERMISSIONS + "&state=" + UUID.randomUUID();
    }

    public static Map<String, String> parseFacebookResponce(String out) {
        Map<String, String> res = new HashMap<String, String>();
        if (out.contains("error"))
            return res;

        for (String nameValuePair : out.split("&")) {
            String[] elements = nameValuePair.split("=");
            res.put(elements[0], elements[1]);
        }
        return res;
    }

    public static String parseEmailFromState(String state) {
        return state.replaceFirst("pyschrythm", "");
    }

    public static String generateUserState(String email) {
        return "pyschrythm" + email;
    }

}
