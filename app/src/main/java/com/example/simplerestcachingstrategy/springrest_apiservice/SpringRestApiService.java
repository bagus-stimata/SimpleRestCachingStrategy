package com.example.simplerestcachingstrategy.springrest_apiservice;

import android.os.AsyncTask;
import android.util.Log;

import com.example.simplerestcachingstrategy.AppConfig;
import com.example.simplerestcachingstrategy.config.ApiSpringRestClient;
import com.example.simplerestcachingstrategy.model.Employee;
import com.example.simplerestcachingstrategy.model.UploadFileResponse;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SpringRestApiService {
    protected static final String TAG = SpringRestApiService.class.getSimpleName();
    private ApiSpringRestClient apiSpringRestClient  = new ApiSpringRestClient();

    public SpringRestApiService(){
    }
    public Employee getItemById(int id) {
        CrudAsyncTask asyncTask = new CrudAsyncTask(apiSpringRestClient, id, true);
        Employee domain = null;
        try {
            domain = asyncTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (domain==null) domain =new Employee();
        return domain;
    }

    public void createItem(Employee domain) {
        new SpringRestApiService.CrudAsyncTask(apiSpringRestClient, domain).execute();
    }
    public void updateItem(Integer id, Employee domain) {
        new SpringRestApiService.CrudAsyncTask(apiSpringRestClient, id, domain).execute();
    }
    public void deleteItem(Integer id) {
        new SpringRestApiService.CrudAsyncTask(apiSpringRestClient, id).execute();
    }

    public List<Employee> getAllItems() {
        FecthItemsAsyncTask asyncTask = new FecthItemsAsyncTask(apiSpringRestClient);
        List<Employee> listItems = new ArrayList<>();
        try {
            listItems = asyncTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
        }
        return listItems;
    }

    public class CrudAsyncTask extends AsyncTask<Void, Void, Employee> {

        String operation = "";
        Employee newAccAccount = null;
        Integer id = 0;
        private ApiSpringRestClient apiAuthenticationClient;

        private CrudAsyncTask(ApiSpringRestClient apiAuthenticationClient, Integer id_find, boolean isGetById ) {
            this.apiAuthenticationClient = apiAuthenticationClient;
            if (isGetById) {
                this.id = id_find;
                operation = "GET_BY_ID";
            }
        }
        private CrudAsyncTask(ApiSpringRestClient apiAuthenticationClient, Employee newDomain){
            this.apiAuthenticationClient = apiAuthenticationClient;
            this.newAccAccount = newDomain;
            operation = "ADD_NEW";
        }
        private CrudAsyncTask(ApiSpringRestClient apiAuthenticationClient, Integer id_update, Employee updateDomain){
            this.apiAuthenticationClient = apiAuthenticationClient;
            this.newAccAccount = updateDomain;
            this.id = id_update;
            operation = "UPDATE";
        }
        private CrudAsyncTask(ApiSpringRestClient apiAuthenticationClient, Integer id_delete){
            this.apiAuthenticationClient = apiAuthenticationClient;
            this.id = id_delete;
            operation = "DELETE";
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Employee doInBackground(Void... voids) {
            String url = AppConfig.BASE_URL;
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            try {

//                ResponseEntity<AccAccount> response = restTemplate.exchange(url, HttpMethod.POST, AccAccount.class);
//                HttpEntity<Object> httpEntity = new HttpEntity<Object>(newAccAccount, apiAuthenticationClient.getRequestHeaders());
//                ResponseEntity<AccAccount> response = restTemplate.postForEntity(url, httpEntity,  AccAccount.class);
                ResponseEntity<Employee> response = null;
                try {
                    if (operation.equals("ADD_NEW")) {
                        url += "createEmployee";
                        response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(newAccAccount, apiAuthenticationClient.getHttpHeaders()), Employee.class);
                    } else if (operation.equals("UPDATE")) {
                        url += "putEmployee/" + id;
                        response = restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<Object>(newAccAccount, apiAuthenticationClient.getHttpHeaders()), Employee.class);
                    } else if (operation.equals("DELETE")) {
                        url += "deleteEmployee/" + id;
                        response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<Object>(apiAuthenticationClient.getHttpHeaders()), Employee.class);
                    } else if (operation.equals("GET_BY_ID")) {
                        url += "getEmployeePath/" + id;
                        response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(apiAuthenticationClient.getHttpHeaders()), Employee.class);
                    }
                    Log.d(TAG, url + " >> " + response.toString());
                }catch (Exception ex){
                }

                return response!=null? response.getBody(): new Employee();

            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new Employee();
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new Employee();
            }
        }

        @Override
        protected void onPostExecute(Employee result) {
        }
    }


    public class FecthItemsAsyncTask extends  AsyncTask<Void, Void, List<Employee>>{
        private ApiSpringRestClient apiAuthenticationClient;

        private FecthItemsAsyncTask(ApiSpringRestClient apiAuthenticationClient){
            this.apiAuthenticationClient = apiAuthenticationClient;
        }
        @Override
        protected void onPreExecute() {
        }
        @Override
        protected List<Employee> doInBackground(Void... voids) {
            final String url = AppConfig.BASE_URL + "getListEmployee";

            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

            try {
                // Make the network request
                Log.d(TAG, url);
                ResponseEntity<Employee[]> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(apiAuthenticationClient.getHttpHeaders()), Employee[].class);
                List<Employee> list = Arrays.asList(response.getBody());
                return list;

            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new ArrayList<Employee>();
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new ArrayList<Employee>();
            }
        }
        @Override
        protected void onPostExecute(List<Employee> result) {
        }
    }

    /**
     * UPLOAD PICTURE AND FILE
     */
    public  byte[] downloadFileByFileName(String fileName) {
        byte[] domain = null;
        try {
            FileDownloadAsyncTask asyncTask = new FileDownloadAsyncTask(apiSpringRestClient, fileName);
            domain = asyncTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
//        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain;
    }

    public UploadFileResponse uploadFileResponse( File file) {
        UploadFileResponse domain = null;
        try {
            FileUploadAsyncTask asyncTask = new FileUploadAsyncTask(apiSpringRestClient, file);
            domain = asyncTask.execute().get();
        } catch (InterruptedException | ExecutionException e) {
//        } catch (Exception e) {
            e.printStackTrace();
        }
        return domain;
    }

    /**
     * Hanya bisa menggunaakn type -> byte[]
     * sudah di coba tipe yang lain tidak bisa
     */
    public class FileDownloadAsyncTask extends AsyncTask<Void, Void,  byte[]> {

        byte[] responseByteArray = null;
        String fileName = "";
        private ApiSpringRestClient apiAuthenticationClient;

        private FileDownloadAsyncTask(ApiSpringRestClient apiAuthenticationClient, String fileName) {
            this.apiAuthenticationClient = apiAuthenticationClient;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected  byte[] doInBackground(Void... voids) {
            String url = AppConfig.BASE_URL;
            try {

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new ByteArrayHttpMessageConverter()); //Hanya bisa digunakan untuk type -> byte[]
                    restTemplate.getMessageConverters().add(new FormHttpMessageConverter()); //Pasangan dari -> requestHeadersMultiPart.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

//                    HttpHeaders headers = new HttpHeaders();
//                    headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
//                    HttpEntity<String> entity = new HttpEntity<String>(headers);
                    HttpEntity<ByteArrayResource> httpEntity = new HttpEntity<ByteArrayResource>(apiSpringRestClient.getHttptHeaders_FileDownload());

//                    ResponseEntity<byte[]> response = restTemplate.exchange(AppConfig.BASE_URL + "downloadFile/abc.jpg", HttpMethod.GET, entity, byte[].class, "1");  //uriVariable "1" TIDAK WAJIB, tapi sebaiknya
                    ResponseEntity<byte[]> response = restTemplate.exchange(AppConfig.BASE_URL + "downloadFile/" + fileName, HttpMethod.GET, httpEntity, byte[].class, "1");

                    if (response.getStatusCode() == HttpStatus.OK) {
//                        Files.write(Paths.get("google.png"), response.getBody());
                            responseByteArray = response.getBody();
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                return responseByteArray;
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return responseByteArray;
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return responseByteArray;
            }
        }

        @Override
        protected void onPostExecute( byte[] result) {
        }
    }



    public class FileUploadAsyncTask extends AsyncTask<Void, Void,  UploadFileResponse> {

        UploadFileResponse responseDomain = null;
        File  file;
        private ApiSpringRestClient apiAuthenticationClient;

        private FileUploadAsyncTask(ApiSpringRestClient apiAuthenticationClient,  File  file) {
            this.apiAuthenticationClient = apiAuthenticationClient;
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected UploadFileResponse doInBackground(Void... voids) {
            String url = AppConfig.BASE_URL;
            try {

                try {
                    RestTemplate restTemplate = new RestTemplate();
                    restTemplate.getMessageConverters().add(new FormHttpMessageConverter()); // Pasangan untuk Request -> requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
//                    restTemplate.getMessageConverters().add(new StringHttpMessageConverter()); //Pasangan untuk Response -> ResponseEntity<String> response
                    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());  //Pasangan untuk Response -> ResponseEntity<UploadFileResponse> response


                    Resource resource = new FileSystemResource(file); //--> harus Resource
//                    byte[] fileAsResource = FileCopyUtils.copyToByteArray(file);//Tidak bisa menggunakan ini *Note Sangat berbeda dengan download ya


                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);//Main request's headers

//                    LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                    body.add("file", resource);
//Tidak boleh dan tidak bisa menggunakan ini oke -> body.add("file", fileAsResource);

                    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, requestHeaders);

                    ResponseEntity<UploadFileResponse> response = restTemplate.exchange(AppConfig.BASE_URL + "uploadFile",
                            HttpMethod.POST, requestEntity, UploadFileResponse.class);


                    if (response.getStatusCode() == HttpStatus.OK) {
                        responseDomain = response.getBody();
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }

                return responseDomain;
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return responseDomain;
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return responseDomain;
            }
        }

        @Override
        protected void onPostExecute( UploadFileResponse result) {
        }
    }
}


