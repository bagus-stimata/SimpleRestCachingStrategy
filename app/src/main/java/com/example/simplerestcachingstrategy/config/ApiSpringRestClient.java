package com.example.simplerestcachingstrategy.config;

import com.example.simplerestcachingstrategy.AppConfig;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApiSpringRestClient {

    private HttpHeaders requestHeaders;
    private HttpHeaders requestHeadersMultiPart;

//    private String baseUrl = "http://ssp-surabaya.ddns.net:8989/rest/";
    private String username = AppConfig.BASIC_AUTH_USERNAME;
    private String password = AppConfig.BASIC_AUTH_PASSWORD;

    private static ApiSpringRestClient ourInstance;

    public static synchronized ApiSpringRestClient getInstance() {
        if (ourInstance==null) {
            ourInstance = new ApiSpringRestClient();
        }
        return ourInstance;
    }


    public HttpHeaders getHttpHeaders(){
        requestHeaders = new HttpHeaders();
        /**
         * Cuma ini aja bagian securitynya
         */
        HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
        requestHeaders.setAuthorization(authHeader);

        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return  requestHeaders;
    }
    public HttpHeaders getHttptHeaders_FileDownload(){
        requestHeadersMultiPart = new HttpHeaders();

        /**
         * Cuma ini aja bagian securitynya
         */
        HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
        requestHeadersMultiPart.setAuthorization(authHeader);

        /**
         *  requestHeadersMultiPart.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
         * harus dipasangkan dengan
         *  restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
         * pada restTemplate
         * atau malah di Kosongkan malah lebih baik
         */
//       Ingat Ini yang buat tidak mau Jalan >>  requestHeadersMultiPart.setContentType(MediaType.MULTIPART_FORM_DATA);
        requestHeadersMultiPart.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        List<MediaType> acceptableMediaTypes = new ArrayList<>();

        acceptableMediaTypes.add(MediaType.IMAGE_JPEG);
        acceptableMediaTypes.add(MediaType.IMAGE_PNG);
        acceptableMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        requestHeaders.setAccept(acceptableMediaTypes);
        
        return requestHeadersMultiPart;
    }
//    public ApiSpringRestClient setBaseUrl(String baseUrl) {
//        this.baseUrl = baseUrl;
//        if (!baseUrl.substring(baseUrl.length() - 1).equals("/")) {
//            this.baseUrl += "/";
//        }
//        return this;
//    }

    public void setRequestHeaders(HttpHeaders requestHeaders) {
        this.requestHeaders = requestHeaders;
    }


}
