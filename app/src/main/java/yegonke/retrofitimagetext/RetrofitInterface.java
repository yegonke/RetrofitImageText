package yegonke.retrofitimagetext;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface RetrofitInterface {
    @Multipart
    @POST("/api/upload/")
    Call<Response> uploadImage(@Part MultipartBody.Part image,
                               @Part("category") RequestBody categoryUnsent,
                               @Part("title") RequestBody titleUnsent,
                               @Part("description") RequestBody descriptionUnsent,
                               @Part("location") RequestBody locationUnsent);
}
