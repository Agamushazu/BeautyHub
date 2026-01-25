package com.example.beautyhub.utils;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SupabaseStorageService {
    // השתמשנו ב-encoded = true כדי שהסלאש בתוך שם הקובץ (למשל profiles/id.jpg) לא יהפוך לתווים מוזרים
    @POST("/storage/v1/object/{bucket}/{filename}")
    Call<ResponseBody> uploadFile(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authorization,
            @Header("x-upsert") String upsert,
            @Header("Content-Type") String contentType,
            @Path("bucket") String bucket,
            @Path(value = "filename", encoded = true) String filename,
            @Body RequestBody file
    );
}
