package com.example.simplerestcachingstrategy.retrofit_apiservice;

import com.example.simplerestcachingstrategy.model.Employee;
import com.example.simplerestcachingstrategy.model.UploadFileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApiService {

    /**
     * Hilangkan -> @Header("Authorization") jika tanpa basic authentikasi
     * Tapi jika tidak dihilankan pun, masih akan tetap berjalan pada Rest Server biasa(tanpa authenticasi)
     * Karna pada prinsipnya, basic authentikasi adalah menciptakan "Token Static yang dienkripsi" yang di bungkus dilewatkan Header
     */
//    @GET("getEmployee")
//      Call<Employee> getEmployeeRetrofit(String authHeader);
    @GET("getEmployee")
    Call<Employee> getEmployeeRetrofit(@Header("Authorization") String authHeader);


    @GET("getEmployeeParam")
    Call<Employee> getEmployeeRetrofitParam(@Query("id") Integer id);

    @GET("getEmployeePath/{id}")
    Call<Employee> getEmployeeRetrofitPath(@Header("Authorization") String authHeader, @Path("id") Integer id);

    @GET("getListEmployee")
    Call<List<Employee>> getListEmployeeRetrofit(@Header("Authorization") String authHeader);



    @POST("createEmployee")
    Call<Employee> createEmployee(@Header("Authorization") String authHeader, @Body Employee employee);

    @PUT("putEmployee")
    Call<Employee> putEmployee(@Header("Authorization") String authHeader, @Query("id") int id, @Body Employee employee);

    @HTTP(method = "DELETE", path = "deleteEmployee/{id}", hasBody = true)
//    @DELETE("deleteEmployee/{id}")
    Call<Employee> deleteEmployee(@Header("Authorization") String authHeader, @Path("id") int id);


    /**
     * FILE UPLOAD & DOWNLOAD
     */
    @GET("downloadFile/{fileName}")
    Call<ResponseBody> downloadImageOrFile(@Header("Authorization") String authHeader, @Path("fileName") String fileName);

    @Multipart
    @POST("uploadFile")
//    @HTTP(method = "POST", path = "uploadFile", hasBody = true)
    Call<UploadFileResponse> uploadFileMultipart(@Header("Authorization") String authHeader, @Part MultipartBody.Part file);

}
