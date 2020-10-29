package com.example.simplerestcachingstrategy;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.simplerestcachingstrategy.ui.GlideImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoGalleryActivity extends AppCompatActivity {

    public String authHeader = ""; //Untuk REtrofit2 springboot security

    @BindView(R.id.imageView1)
    ImageView imageView1;
    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.imageView3)
    ImageView imageView3;
    @BindView(R.id.imageView4)
    ImageView imageView4;

    @BindView(R.id.pbLoading1)
    ProgressBar pbLoading1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_gallery);

        ButterKnife.bind(this);

        String bundle = getIntent().getStringExtra("parameter1");
        if (bundle !=null) Toast.makeText(this, "Hello: " + bundle, Toast.LENGTH_LONG).show();

        /**
         * RETROFIT 2
         * SPRING BASIC AUTH
         */
        String stringBaseAuth = AppConfig.BASIC_AUTH_USERNAME + ":" + AppConfig.BASIC_AUTH_PASSWORD;
        authHeader = "Basic " + Base64.encodeToString(stringBaseAuth.getBytes(), Base64.NO_WRAP);

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
        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        RequestOptions requestOptions = RequestOptions
                .placeholderOf(circularProgressDrawable)
                .error(circularProgressDrawable)
                .skipMemoryCacheOf(true)
                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                .diskCacheStrategy(DiskCacheStrategy.NONE);


        Glide.with(this)
                .load(glideUrl)
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .error(circularProgressDrawable)
                .apply(requestOptions)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView1);
        Glide.with(this)
                .load(glideUrl)
                .circleCrop()
//                .placeholder(circularProgressDrawable)
//                .error(circularProgressDrawable)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        pbLoading1.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        pbLoading1.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
//                .apply(requestOptions)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView2);



        Glide.with(this)
//                .load(glideUrl)
                .load("https://raw.githubusercontent.com/bumptech/glide/master/static/glide_logo.png")
                .circleCrop()
                .placeholder(circularProgressDrawable)
                .error(circularProgressDrawable)
                .apply(requestOptions)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView3);


        /**
         * TRY LOAD INDICATOR
         */
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .circleCrop()
                .override(400, 400)
                .placeholder(R.drawable.ic_baseline_image_24)
                .error(R.drawable.ic_baseline_broken_image_24)
                .priority(Priority.HIGH);

//        Glide.with(this)
//                .load(glideUrl)
//                .circleCrop()
//                .apply(requestOptions)
//                .apply(new RequestOptions().override(300, 300))
//                .into(imageView4);

        new GlideImageLoader(imageView4, pbLoading1).load(url, options);

    }
}