package com.example.beautyhub.utils;

import android.util.Log;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseStorageHelper {
    private static final String supabaseUrl = "https://ijlqhxxhetmhakdakmzh.supabase.co";
    // החזרתי את המפתח המקורי והנכון
    private static final String supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImlqbHFoeHhoZXRtaGFrZGFrbXpoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUwODY5MzEsImV4cCI6MjA4MDY2MjkzMX0.V9LHaFA8ncP36YeV_FV-tyZ3TWhwc-C6mjeJFmBzWUc";
    
    private static final String SUPABASE_BUCKET = "my-bucket";
    private static final String TAG = "SupabaseStorageHelper";

    public static void uploadPicture(final File file, final String filePath, OnResultCallback callback) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(supabaseUrl + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            SupabaseStorageService service = retrofit.create(SupabaseStorageService.class);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);

            String bearer = "Bearer " + supabaseKey;
            Call<ResponseBody> call = service.uploadFile(
                    supabaseKey,
                    bearer,
                    "true",
                    "image/jpeg",
                    SUPABASE_BUCKET,
                    filePath,
                    requestFile
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String publicUrl = getFileSupabaseUrl(filePath);
                        callback.onResult(true, publicUrl, null);
                    } else {
                        String detail = "";
                        try { if (response.errorBody() != null) detail = response.errorBody().string(); } catch (Exception e) {}
                        callback.onResult(false, null, "Error " + response.code() + ": " + detail);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    callback.onResult(false, null, t.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onResult(false, null, e.getMessage());
        }
    }

    public static String getFileSupabaseUrl(String filePath) {
        return supabaseUrl + "/storage/v1/object/public/" + SUPABASE_BUCKET + "/" + filePath;
    }

    public interface OnResultCallback {
        void onResult(boolean success, String url, String error);
    }
}
