package com.example.simplerestcachingstrategy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestOptions;
import com.example.simplerestcachingstrategy.config.ApiRetrofitClient;
import com.example.simplerestcachingstrategy.model.Employee;
import com.example.simplerestcachingstrategy.model.RecyclerAdapter;
import com.example.simplerestcachingstrategy.model.UploadFileResponse;
import com.example.simplerestcachingstrategy.retrofit_apiservice.RetrofitApiService;
import com.example.simplerestcachingstrategy.springrest_apiservice.SpringRestApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String TAG = MainActivity.class.getSimpleName();

    static int REST_CLIENT_METHOD = 3;  //   1. Spring Rest Template Client (Kita Tidak Membahasnya) 2. Retrofit, 3. Volley
    static int PICTURE_SHOW = 1;        //   1. Glide, 2. Picasso ==>> *NOTE  (Rest Mehod Harus 1. Spring Rest Template Client)
    public String authHeader = ""; //Untuk REtrofit2 springboot security

    Employee employee = new Employee();
    List<Employee> listEmployee = new ArrayList<>();

    @BindView(R.id.textView1)
    TextView textView1;

    @BindView(R.id.recyclerView1)
    RecyclerView recyclerView;

    @BindView(R.id.imageView1)
    ImageView imageView1;

    @BindView(R.id.floatingActionButton1)
    FloatingActionButton floatingActionButton1;
    @BindView(R.id.floatingActionButton2)
    FloatingActionButton floatingActionButton2;
    @BindView(R.id.floatingActionButton3)
    FloatingActionButton floatingActionButton3;
    @BindView(R.id.floatingActionButton4)
    FloatingActionButton floatingActionButton4;
    @BindView(R.id.floatingActionButton5)
    FloatingActionButton floatingActionButton5;

    RecyclerAdapter recyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        recyclerAdapter = new RecyclerAdapter(getApplicationContext(), new ArrayList<>()); //Pasti kosong dulu
        recyclerView.setAdapter(recyclerAdapter);
        //RecyclerView harus diberi layout Manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /**
         * RETROFIT 2
         * SPRING BASIC AUTH
         */
        String stringBaseAuth = AppConfig.BASIC_AUTH_USERNAME + ":" + AppConfig.BASIC_AUTH_PASSWORD;
        authHeader = "Basic " + Base64.encodeToString(stringBaseAuth.getBytes(), Base64.NO_WRAP);


        if (REST_CLIENT_METHOD == 1) {
//            simpleRestClient_WithSpringRestClient();
            //Kita tidak membahas ini. karena teknology lama yang tidak melangalami perkembangan pada android
        } else if (REST_CLIENT_METHOD == 2) {
            simpleRestClient_WithRetrofit2();
        } else if (REST_CLIENT_METHOD == 3) {
            simpleRestClient_WithVolley();
        }


        floatingActionButton1.setOnClickListener(e -> {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            fileIntent.setType("*/*");
            fileIntent.setType("image/*");
            startActivityForResult(fileIntent, 10);
        });
        floatingActionButton2.setOnClickListener(e -> {
            Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
//            fileIntent.setType("*/*");
            fileIntent.setType("application/pdf");
            startActivityForResult(fileIntent, 11);
        });

        floatingActionButton3.setOnClickListener(e -> {
            askCameraPermission();
        });

        floatingActionButton4.setOnClickListener(e -> {
            if (REST_CLIENT_METHOD == 2) {
                simpleRestClient_WithRetrofit2();
                Toast.makeText(this, "Reload Retrofit", Toast.LENGTH_LONG).show();
            } else if (REST_CLIENT_METHOD == 3) {
                simpleRestClient_WithVolley();
                Toast.makeText(this, "Reload Volley", Toast.LENGTH_LONG).show();
            }
//            glideAndPicassoChaceLoad(); //OTOMATIS MENGGUNAKAN TEKNIK CHACHING
        });

        floatingActionButton5.setOnClickListener(e -> {
            Intent intent = new Intent(this, PhotoGalleryActivity.class);
            intent.putExtra("parameter1", "Hello dari Parameter1, dikirim ya?");
            startActivity(intent);
        });

    }


    private void askCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 121);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 121 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Tidak dapat mendapatkan akses kamera", Toast.LENGTH_LONG).show();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, 12);
    }

    public void simpleRestClient_WithRetrofit2() {
        RetrofitApiService apiService = ApiRetrofitClient.getClient(this).create(RetrofitApiService.class); //Base Url disediakan pda ApiRetrofit

        Call<Employee> call = apiService.getEmployeeRetrofitPath(authHeader, 3);
        call.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful()) {
                    employee = response.body();
                    if (employee != null)
                        textView1.setText(employee.getName() + " | " + employee.getDesignation());
                    ApiRetrofitClient.okHttpClient.dispatcher().executorService().shutdown();
                } else {
                    textView1.setText("");
                }
            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.e("ERROR >> ######", t.getMessage());
            }

        });

        Call<List<Employee>> callList = apiService.getListEmployeeRetrofit(authHeader);
        callList.enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                if (response.isSuccessful()) {
                    listEmployee = response.body();
                    recyclerAdapter.setList(listEmployee);
                    ApiRetrofitClient.okHttpClient.dispatcher().executorService().shutdown();

                } else {
                    recyclerAdapter.setList(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
            }
        });

        employee.setId(6);
        employee.setName("Tambahan Bos");
        employee.setDesignation("operator");
        Call<Employee> callCreateEmployee = apiService.createEmployee(authHeader, employee);
        callCreateEmployee.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful()) {
                    listEmployee.add(response.body());
                    recyclerAdapter.setList(listEmployee);
                    ApiRetrofitClient.okHttpClient.dispatcher().executorService().shutdown();
                } else {
                    recyclerAdapter.setList(new ArrayList<>());
                }

            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.e("Error", "Error Create");
            }
        });

        Call<Employee> callPutEmployee = apiService.putEmployee(authHeader, 2, employee);
        callPutEmployee.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful()) {
                    Log.d("Put", "Put Berhasil");
                    ApiRetrofitClient.okHttpClient.dispatcher().executorService().shutdown();
                }
            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.e("Error", "Error Put");
            }
        });

//        Call<Employee> callDeleteEmployee = apiService.deleteEmployee(1);
//        callDeleteEmployee.enqueue(new Callback<Employee>() {
//            @Override
//            public void onResponse(Call<Employee> call, Response<Employee> response) {
//                Log.d("Delete", "Delete Berhasil");
//            }
//            @Override
//            public void onFailure(Call<Employee> call, Throwable t) {
//                Log.e("Error", "Error Delete");
//            }
//        });

        Call<ResponseBody> callRetrievePicture = apiService.downloadImageOrFile(authHeader, "aa.png");
        callRetrievePicture.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        InputStream is = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        imageView1.setImageBitmap(bitmap);
                        ApiRetrofitClient.okHttpClient.dispatcher().executorService().shutdown();
                    } catch (Exception ex) {
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }



    public void simpleRestClient_WithVolley() {
        /**
         * RECOMMENDED USE THIS
         *  https://github.com/DWorkS/VolleyPlus
         */
        /**
         * Deklarasi Quey For All
         */

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        String urlSingle = AppConfig.BASE_URL + "getEmployeePath/3";
//        requestVolleySingleData(urlSingle); //Ini Bisa
        // Ini di persingkat
        MyCachingStringRequest cachingStringRequest = new MyCachingStringRequest(com.android.volley.Request.Method.GET, urlSingle, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Gson gson = new GsonBuilder().create();
                Employee result = gson.fromJson(response.toString(), Employee.class);
                textView1.setText(result.getName() + " >> " + result.getDesignation());
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyerror) {
                textView1.setText("");
            }
        });
        cachingStringRequest.setTag(TAG);
        requestQueue.add(cachingStringRequest);


        /**
         * Get List
         * Yang Dahuluan Selesaia akan menghentikan requestQueue
         */
        String urlList = AppConfig.BASE_URL + "getListEmployee";

        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.GET, urlList, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
//                    Gson gson=new Gson();
                Gson gson = new GsonBuilder().create();
                Employee[] results = gson.fromJson(response.toString(), Employee[].class);
                //Or
                Type userListType = new TypeToken<ArrayList<Employee>>() {
                }.getType();
                ArrayList<Employee> employeList = gson.fromJson(response.toString(), userListType);

                recyclerAdapter.setList(employeList);
//                Toast.makeText(getApplicationContext(), employeList.get(3).getName() + " >> " + employeList.get(3).getDesignation(), Toast.LENGTH_LONG).show();

                requestQueue.stop();
                requestQueue.cancelAll(TAG);

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyerror) {
                recyclerAdapter.setList(new ArrayList<>());
            }
        });
        stringRequest.setTag(TAG);
        requestQueue.add(stringRequest);

        /**
         * Donwnload Image
         */
        requestVolleyBitmap(AppConfig.BASE_URL + "downloadFile/aa.png");
        String urlPhoto = AppConfig.BASE_URL + "downloadFile/aa.png";
//        ImageRequest imageRequest = new ImageRequest(urlPhoto,
//                new com.android.volley.Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap bitmap) {
//                        imageView1.setImageBitmap(bitmap);
//                    }
//                }, 0, 0, Bitmap.Config.RGB_565,
//                new com.android.volley.Response.ErrorListener() {
//                    public void onErrorResponse(VolleyError error) {
////                        mImageView.setImageResource(R.drawable.image_load_error);
//                        imageView1.setImageBitmap(null);
//                    }
//                });
//
//        imageRequest.setTag("Other TAG");
//        requestQueue.add(imageRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (REST_CLIENT_METHOD == 1) {
                onActivityResult_SpringRestClient(requestCode, resultCode, data);
            } else if (REST_CLIENT_METHOD == 2) {
                onActivityResult_Retrofit2(requestCode, resultCode, data);
            } else if (REST_CLIENT_METHOD == 3) {
                onActivityResult_Volley(requestCode, resultCode, data);
            }
        }//endif
    }

    protected void onActivityResult_SpringRestClient(int requestCode, int resultCode, @Nullable Intent data) {
        SpringRestApiService service = new SpringRestApiService();
        Uri uriPath = data.getData();
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");

        switch (requestCode) {
            case 10:
                final File filePhoto = MyFileUtils.convertBitmapToFile_UsingOsLangsung(getApplicationContext(), uriPath);
                /**
                 * dibawah ini adalah metodenya Retrofit2
                 * Rekomendasi: Ini sebaiknya dijadikan satu dengan Class Upload Tersendiri
                 * Jadi Retrofit2 memempunya dua kelas
                 * 1. Kelas untuk Meload Request Body -> dan Mengamil hasil Response
                 * 2. Kelas Untuk Interfacenya
                 *
                 */
//                RequestBody requestBody_Photo = RequestBody.create(MediaType.parse(getContentResolver().getType(uriPath)), filePhoto);
//                MultipartBody.Part bodyPhoto = MultipartBody.Part.createFormData("file", filePhoto.getName(), requestBody_Photo);

                UploadFileResponse responsePhoto = service.uploadFileResponse(filePhoto);
                if (responsePhoto != null) {
                    imageView1.setImageBitmap(bitmap);
                }

//                Toast.makeText(getApplicationContext(), "hello bos : " + responsePhoto.getFileName() , Toast.LENGTH_LONG).show();
//                Toast.makeText(getApplicationContext(), "hello bos : "  , Toast.LENGTH_LONG).show();

                break;
            case 11:

                final File filePdf = MyFileUtils.convertPdfToFile_UsingOsLangsung(getApplicationContext(), uriPath);
                UploadFileResponse responseDir = service.uploadFileResponse(filePdf);
                if (responseDir != null) {
                    imageView1.setImageResource(R.drawable.ic_launcher_foreground); //Menggunakan Pada Response View
                }
                break;
            case 12:
                File fileCamera = MyFileUtils.convertBitmapToFile_UsingViaByteArrayOs(getApplicationContext(), bitmap);
                UploadFileResponse responseCamera = service.uploadFileResponse(fileCamera);
                if (responseCamera != null) {
                    imageView1.setImageBitmap(bitmap);
                }

                break;
            default:
                break;
        }

    }

    protected void onActivityResult_Retrofit2(int requestCode, int resultCode, @Nullable Intent data) {
        RetrofitApiService apiService = ApiRetrofitClient.getClient(this).create(RetrofitApiService.class);
        Uri uriPath = data.getData();

        switch (requestCode) {
            case 10:
//                    File file = convertBitmapToFile_UsingViaByteArrayOs(bitmap);
                final File filePhoto = MyFileUtils.convertBitmapToFile_UsingOsLangsung(getApplicationContext(), uriPath);

//                    MediaType mediaType = MediaType.parse(getContentResolver().getType(uriPath));
//                    Toast.makeText(this, mediaType + " >> " + file.getName() +  " >> " ,
//                            Toast.LENGTH_LONG).show();

//                    RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
                RequestBody requestBody_Photo = RequestBody.create(MediaType.parse(getContentResolver().getType(uriPath)), filePhoto);
                MultipartBody.Part bodyPhoto = MultipartBody.Part.createFormData("file", filePhoto.getName(), requestBody_Photo);

                Call<UploadFileResponse> call_Photo = apiService.uploadFileMultipart(authHeader, bodyPhoto);
                call_Photo.enqueue(new Callback<UploadFileResponse>() {
                    @Override
                    public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
                        if (response.isSuccessful()) {
                            UploadFileResponse uploadFileResponse = response.body();
                            Log.d("UploadImage", "SUCCESSFULL " + uploadFileResponse.getFileName());

                            /**
                             * Jika Berhasil: Hasilnya ditampilkan dibawah
                             */
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriPath);
                                imageView1.setImageBitmap(bitmap); //Menggunakan Pada Response View
                            } catch (Exception ex) {
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), "UPLOAD TIDAK BERHASIL ", Toast.LENGTH_LONG).show();
                            Log.d("UploadImage", "NOT SUCCESSFULL_" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                    }
                });

                break;

            case 11:
                File file = MyFileUtils.convertPdfToFile_UsingOsLangsung(getApplicationContext(), uriPath);

//                    Toast.makeText(this,file.getName() +  "" ,
//                            Toast.LENGTH_LONG).show();

                RequestBody requestBody_File = RequestBody.create(MediaType.parse(getContentResolver().getType(uriPath)), file);
                MultipartBody.Part body_File = MultipartBody.Part.createFormData("file", file.getName(), requestBody_File);


                Call<UploadFileResponse> call_File = apiService.uploadFileMultipart(authHeader, body_File);
                call_File.enqueue(new Callback<UploadFileResponse>() {
                    @Override
                    public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
                        if (response.isSuccessful()) {
                            UploadFileResponse uploadFileResponse = response.body();
                            Log.d("UploadPdf", "SUCCESSFULL " + uploadFileResponse.getFileName());
                            imageView1.setImageResource(R.drawable.ic_launcher_foreground); //Menggunakan Pada Response View

                        } else {
                            Toast.makeText(getApplicationContext(), "UPLOAD TIDAK BERHASIL ", Toast.LENGTH_LONG).show();
                            Log.d("UploadPdf", "NOT SUCCESSFULL_" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                        imageView1.setImageBitmap(null); //Menggunakan Pada Response View
                    }
                });

                break;
            case 12:
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                File fileCamera = MyFileUtils.convertBitmapToFile_UsingViaByteArrayOs(getApplicationContext(), bitmap);

                RequestBody requestBody_Camera = RequestBody.create(MediaType.parse("image/*"), fileCamera);
                MultipartBody.Part bodyCamera = MultipartBody.Part.createFormData("file", fileCamera.getName(), requestBody_Camera);

                Call<UploadFileResponse> call_Camera = apiService.uploadFileMultipart(authHeader, bodyCamera);
                call_Camera.enqueue(new Callback<UploadFileResponse>() {
                    @Override
                    public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
                        if (response.isSuccessful()) {
                            imageView1.setImageBitmap(bitmap);
                        } else {
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadFileResponse> call, Throwable t) {
                    }
                });

                break;
            default:
                break;
        }
    }

    protected void onActivityResult_Volley(int requestCode, int resultCode, @Nullable Intent data) {
        /**
         * RECOMMENDED USE THIS
         *  https://github.com/DWorkS/VolleyPlus
         */
        String remoteUrl = AppConfig.BASE_URL + "uploadFile";
        Uri uriPath = data.getData();

        switch (requestCode) {
            case 10:
                final File filePhoto = MyFileUtils.convertBitmapToFile_UsingOsLangsung(getApplicationContext(), uriPath);


                break;

            case 11:
                File file = MyFileUtils.convertPdfToFile_UsingOsLangsung(getApplicationContext(), uriPath);

//                    Toast.makeText(this,file.getName() +  "" ,
//                            Toast.LENGTH_LONG).show();

                RequestBody requestBody_File = RequestBody.create(MediaType.parse(getContentResolver().getType(uriPath)), file);
                MultipartBody.Part body_File = MultipartBody.Part.createFormData("file", file.getName(), requestBody_File);


                break;
            case 12:
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                File fileCamera = MyFileUtils.convertBitmapToFile_UsingViaByteArrayOs(getApplicationContext(), bitmap);

                RequestBody requestBody_Camera = RequestBody.create(MediaType.parse("image/*"), fileCamera);
                MultipartBody.Part bodyCamera = MultipartBody.Part.createFormData("file", fileCamera.getName(), requestBody_Camera);

                break;
            default:
                break;
        }

    }

    protected void glideAndPicassoChaceLoad() {
        /**
         * Untuk penggunaaan secara manual harusnya mengunakan Image Loader tapi
         * ImageLoader pada Klass dalam project ini kurang efisien
         * yang benar harusnya menggunakan teknik pada Glide dan buat sendiri
         * Glide Caching Srategy
         *
         * https://android.jlelse.eu/best-strategy-to-load-images-using-glide-image-loading-library-for-android-e2b6ba9f75b2
         *
         */

        if (PICTURE_SHOW == 1) {
            //USING GLIDE
            String url = AppConfig.BASE_URL + "downloadFile/abc.jpg";
            GlideUrl glideUrl = new GlideUrl(url,
                    new LazyHeaders.Builder()
                            .addHeader("Authorization", authHeader)
                            //                                .addHeader("Cookie", AUTHORIZATION)
                            //                                .addHeader("Accept", ABC)
                            .build());

            /**
             * Glide Caching Srategy
             * https://android.jlelse.eu/best-strategy-to-load-images-using-glide-image-loading-library-for-android-e2b6ba9f75b2
             */
            RequestOptions requestOptions = RequestOptions
                    .diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC);

            Glide.with(this)
                    .load(glideUrl)
                    .circleCrop()
                    .apply(requestOptions)
//                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(imageView1);

        } else if (PICTURE_SHOW == 2) {
            String url = AppConfig.BASE_URL + "downloadFile/abc.jpg";
            //                Picasso.get().load(url).resize(50, 50).into(imageView1);

            /**
             * USING AUTHENTICATION
             */
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request newRequest = chain.request().newBuilder()
                                    .addHeader("Authorization", authHeader)
                                    .build();
                            return chain.proceed(newRequest);
                        }
                    }).build();

            Picasso picassoBuilder = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(client))
                    .build();
            picassoBuilder.setIndicatorsEnabled(true);

            /**
             * Picasso Caching Strategy Doesn't Work
             * Must Create Mannual Store to Memory or Disk To enable Caching Strategy
             */

            //Show Image
            picassoBuilder.load(url)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .rotate(-90)
                    .into(imageView1, new com.squareup.picasso.Callback.EmptyCallback() {
                        @Override
                        public void onError(Exception e) {
                            picassoBuilder.load(url)
                                    .rotate(-90)
                                    .into(imageView1);
                            Log.d("Picasso", "Error Bos");
                        }
                    });
        }

    }

    /**
     * Voley
     */
    private void requestVolleySingleData(String url) {
        /**
         * SOURCE
         * https://medium.com/android-grid/how-to-use-volley-cache-android-studio-be59cba08861
         */

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Gson gson = new GsonBuilder().create();
                Employee result = gson.fromJson(response.toString(), Employee.class);
                textView1.setText(result.getName() + " >> " + result.getDesignation());
//                textView1.setText("Tiaya elek");
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                textView1.setText("");
            }
        }) {
            /**
             * INi otentikasi Basic
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers =  new HashMap<>();
//                String credentials = AppConfig.BASIC_AUTH_USERNAME + ":" + AppConfig.BASIC_AUTH_PASSWORD;
//                String encoded = "Basic "+ Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", authHeader);
                return headers;
            }

            /**
             * Ini baru caching method
             */
            @Override
            protected com.android.volley.Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
//                    Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
//                    if (cacheEntry == null) {
//                        cacheEntry = new Cache.Entry();
//                    }
//                    final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
//                    final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
//                    long now = System.currentTimeMillis();
//                    final long softExpire = now + cacheHitButRefreshed;
//                    final long ttl = now + cacheExpired;
//                    cacheEntry.data = response.data;
//                    cacheEntry.softTtl = softExpire;
//                    cacheEntry.ttl = ttl;
//                    String headerValue;
//                    headerValue = response.headers.get("Date");
//                    if (headerValue != null) {
//                        cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                    }
//                    headerValue = response.headers.get("Last-Modified");
//                    if (headerValue != null) {
//                        cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
//                    }
//                    cacheEntry.responseHeaders = response.headers;

                    final String jsonString_AsResult = new String(response.data,
                            HttpHeaderParser.parseCharset(response.headers));

//                    return com.android.volley.Response.success(new JSONObject(jsonString_AsResult),    cacheEntry.responseHeaders);
                    return com.android.volley.Response.success(new JSONObject(jsonString_AsResult),   CacheManager.parseCacheHeaderEntry(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return com.android.volley.Response.error(new ParseError(e));
                }
            }

            @Override
            protected void deliverResponse(JSONObject response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

    private void requestVolleyBitmap(String url) {
        /**
         * SOURCE
         * https://medium.com/android-grid/how-to-use-volley-cache-android-studio-be59cba08861
         */

        ImageRequest imageReques = new ImageRequest(url,
            new com.android.volley.Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    imageView1.setImageBitmap(response);
                }
            }, 0, 0, Bitmap.Config.RGB_565,
                new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                imageView1.setImageBitmap(null);
            }
            }) {
            @Override
            protected com.android.volley.Response<Bitmap> parseNetworkResponse(NetworkResponse response) {

                try {
                    final byte[] nilai = response.data;
                    InputStream is = new ByteArrayInputStream(response.data);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

                    return com.android.volley.Response.success(bitmap, CacheManager.parseCacheHeaderEntry(response));
                }catch (Exception ex){
                    return com.android.volley.Response.error(new ParseError(ex));

                }
            }

            @Override
            protected void deliverResponse(Bitmap response) {
                super.deliverResponse(response);
            }

            @Override
            public void deliverError(VolleyError error) {
                super.deliverError(error);
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError volleyError) {
                return super.parseNetworkError(volleyError);
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(imageReques);
    }

}


class CacheManager{
    public static Cache.Entry parseCacheHeaderEntry(NetworkResponse response) {
        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
        if (cacheEntry == null) {
            cacheEntry = new Cache.Entry();
        }
        final long cacheHitButRefreshed = 3 * 60 * 1000; // in 3 minutes cache will be hit, but also refreshed on background
        final long cacheExpired = 24 * 60 * 60 * 1000; // in 24 hours this cache entry expires completely
        long now = System.currentTimeMillis();
        final long softExpire = now + cacheHitButRefreshed;
        final long ttl = now + cacheExpired;
        cacheEntry.data = response.data;
        cacheEntry.softTtl = softExpire;
        cacheEntry.ttl = ttl;
        String headerValue;
        headerValue = response.headers.get("Date");
        if (headerValue != null) {
            cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }
        headerValue = response.headers.get("Last-Modified");
        if (headerValue != null) {
            cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
        }
        cacheEntry.responseHeaders = response.headers;

        return cacheEntry;
    }
}

class MyCachingStringRequest extends StringRequest {
    public MyCachingStringRequest(int method, String url, com.android.volley.Response.Listener<String> listener, com.android.volley.Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public MyCachingStringRequest(String url, com.android.volley.Response.Listener<String> listener, com.android.volley.Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    protected com.android.volley.Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
//        return com.android.volley.Response.success(parsed, parseIgnoreCacheHeaders(response));
        return com.android.volley.Response.success(parsed, CacheManager.parseCacheHeaderEntry(response));
    }
    @Override
    protected void deliverResponse(String response) {
        super.deliverResponse(response);
    }

    @Override
    public void deliverError(VolleyError error) {
        super.deliverError(error);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    /**
     * INi otentikasi Basic
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers =  new HashMap<>();
        String credentials = AppConfig.BASIC_AUTH_USERNAME + ":" + AppConfig.BASIC_AUTH_PASSWORD;
        String authHeader = "Basic "+ Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        headers.put("Authorization", authHeader);
        return headers;
    }

}


