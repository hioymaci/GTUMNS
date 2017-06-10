package com.haliloymaci.moodlenotifymaven;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class LoginWebSite {

    private List<String> cookies;
    private static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, Exception {

        String url = Main.MOODLE_COURSES_BASE_URL + Main.BITIRME_PROJESI_GROUP_URL;
        String userName = null;
        String passWord = null;

        // login moodle
        String loginUrl = Main.GTU_MOODLE_URL;
        LoginWebSite.setNoCertificateReady();
        CookieHandler.setDefault(new CookieManager());
        LoginWebSite loginWebSite = new LoginWebSite();
        String page = loginWebSite.getPageContent(loginUrl);

//        System.out.println(page);
        String postParams = loginWebSite.getFormParams(page, userName, passWord);
//        System.out.println(postParams);
        loginWebSite.sendPost(loginUrl, postParams);

        // read web-page as html
        String result = loginWebSite.getPageContent(url);

        // show entering web-page on browser
        Main.writeToFile(result, "browser", null);

    }

    public String getPageContent(String url) throws MalformedURLException, IOException {

        URL obj = new URL(url);

        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();

        // default is GET
        httpURLConnection.setRequestMethod("GET");

        httpURLConnection.setUseCaches(false);

        // act like a browser
        httpURLConnection.setRequestProperty("User-Agent", USER_AGENT);

        httpURLConnection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        httpURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        if (cookies != null) {
            for (String cookie : this.cookies) {
                httpURLConnection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = httpURLConnection.getResponseCode();

        // read web-page content
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // Get the response cookies
        setCookies(httpURLConnection.getHeaderFields().get("Set-Cookie"));
        return response.toString();

    }

    public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException {

        Document doc = Jsoup.parse(html);

        // Google form id
        Element loginform = doc.getElementById("login");
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<String>();
        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            if (key.equals("username")) {
                value = username;
            } else if (key.equals("password")) {
                value = password;
            }
            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
        }

        // build parameters list
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&" + param);
            }
        }
        return result.toString();
    }

    public String sendPost(String url, String postParams) throws MalformedURLException, ProtocolException, IOException {

        URL obj = new URL(url);

        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) obj.openConnection();

        // Acts like a browser
        httpsURLConnection.setUseCaches(false);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Host", "accounts.google.com");
        httpsURLConnection.setRequestProperty("User-Agent", USER_AGENT);
        httpsURLConnection.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpsURLConnection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        for (String cookie : this.cookies) {
            httpsURLConnection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        httpsURLConnection.setRequestProperty("Connection", "keep-alive");
        httpsURLConnection.setRequestProperty("Referer", url);
        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setRequestProperty("Content-Length", Integer.toString(postParams.length()));

        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(httpsURLConnection.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = httpsURLConnection.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + url);
//        System.out.println("Post parameters : " + postParams);
//        System.out.println("Response Code : " + responseCode);

        BufferedReader in
                = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String returnString = response.toString();

        if (returnString.contains("Invalid login") || returnString.contains("Hatalı giriş")) {
            System.err.println("Invalid username or password. Review " + Main.CONFIG_FILE_NAME);
            System.exit(-1);
        }
        return response.toString();
    }

    public static void setNoCertificateReady() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

}
