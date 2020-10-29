package com.example.simplerestcachingstrategy;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;

public class MyFileUtils {
    public static File convertBitmapToFile_UsingViaByteArrayOs(Context context, Bitmap reducedBitmap) {
//        File mediaStorageDir = Environment.getExternalStorageDirectory() ;
//        File file = new File(mediaStorageDir + File.separator + "reduced_file");
        String newFileName = "camera_" + String.valueOf(System.currentTimeMillis()+ ".png");
        File mediaStorageDir = context.getFilesDir();
        File file = new File(mediaStorageDir, newFileName);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        reducedBitmap.compress(Bitmap.CompressFormat.PNG, 40, byteArrayOutputStream);
        byte[] imgbytes = byteArrayOutputStream.toByteArray(); //Proses Isi nomor. 1
        try {
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file); //Proses Write to File
            fos.write(imgbytes); //Proses Isi Nomor. 2
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File convertBitmapToFile_UsingOsLangsung(Context context, Bitmap reducedBitmap) {
        File mediaStorageDir = context.getFilesDir();

        File file = new File(mediaStorageDir, "nama_baru.png");
        OutputStream os;
        try {
            os = new FileOutputStream(file); //Proses Write to File
            reducedBitmap.compress(Bitmap.CompressFormat.PNG, 40, os); //Proses Isi Nomor. 1
            os.flush();
            os.close();
        } catch (Exception e) {
        }
        return file;
    }

    public static File convertBitmapToFile_UsingOsLangsung(Context context, Uri uri){
        MediaType mediaType = MediaType.parse(context.getContentResolver().getType(uri));
        File file = null;

        if (mediaType.toString().toLowerCase().contains("image/")) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

                File mediaStorageDir = context.getFilesDir();
                String newFileName = "image_" + String.valueOf(System.currentTimeMillis());
                if (mediaType.toString().toLowerCase().equals("image/jpeg") || mediaType.toString().toLowerCase().equals("image/jpg") ) {
                    newFileName += ".jpeg";
                }else if (mediaType.toString().toLowerCase().equals("image/png")){
                    newFileName += ".png";
                }
                file = new File(mediaStorageDir, newFileName);

                OutputStream os = new FileOutputStream(file); //Proses Write to File

                if (mediaType.toString().toLowerCase().equals("image/jpeg") || mediaType.toString().toLowerCase().equals("image/jpg") ) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os); //Proses Isi Nomor. 1
                }else if (mediaType.toString().toLowerCase().equals("image/png")) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 40, os); //Proses Isi Nomor. 1
                }

                os.flush();
                os.close();
            } catch (Exception e) {
            }
        }
        return file;
    }

    public static File convertPdfToFile_UsingOsLangsung(Context context, Uri uri){
        MediaType mediaType = MediaType.parse(context.getContentResolver().getType(uri));
        File file = null;
        ContentResolver resolver = context.getContentResolver();

        byte[] buffer = null;
        try {
            InputStream initialStream = resolver.openInputStream(uri);
            buffer = new byte[initialStream.available()];
            initialStream.read(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaType.toString().toLowerCase().contains("application/pdf")) {
            try {

                File mediaStorageDir = context.getFilesDir();
                String newFileName = "pdf_" + String.valueOf(System.currentTimeMillis());
                newFileName += ".pdf";
                file = new File(mediaStorageDir, newFileName);

                OutputStream os = new FileOutputStream(file); //Proses Write to File
//              bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os); //Proses Isi Nomor. 1
                os.write(buffer);

                os.flush();
                os.close();
            } catch (Exception e) {
            }
        }
        return file;
    }


    public static void CopyStream(InputStream is, OutputStream os){
        final int buffer_size=1024 * 8;
        try{
//            byte[] bytes=new byte[buffer_size];
//            for(;;)
//            {
//                int count=is.read(bytes, 0, buffer_size);
//                if(count==-1)
//                    break;
//                os.write(bytes, 0, count);
//            }

            byte[] buf = new byte[buffer_size];
            int length;
            while ((length = is.read(buf)) > 0) {
                os.write(buf, 0, length);
            }
        }
        catch(Exception ex){}
    }

}
