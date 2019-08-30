package yegonke.retrofitimagetext;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int INTENT_REQUEST_CODE = 100;

    public static final String URL = "https://yegon.pythonanywhere.com";

    private Button mBtImageSelect;
    private Button mBtImageShow;
    private ProgressBar mProgressBar;
    private String mImageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();

    }

    private void initViews() {

        mBtImageSelect = findViewById(R.id.btn_select_image);
        mBtImageShow = findViewById(R.id.btn_show_image);
        mProgressBar = findViewById(R.id.progress);

        mBtImageSelect.setOnClickListener((View view) -> {

            mBtImageShow.setVisibility(View.GONE);

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");

            try {
                startActivityForResult(intent, INTENT_REQUEST_CODE);

            } catch (ActivityNotFoundException e) {

                e.printStackTrace();
            }

        });

        mBtImageShow.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(mImageUrl));
            startActivity(intent);

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == INTENT_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                try {

                    InputStream is = getContentResolver().openInputStream(data.getData());

                    uploadImage(getBytes(is));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();

        int buffSize = 1024;
        byte[] buff = new byte[buffSize];

        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }

        return byteBuff.toByteArray();
    }


    private void uploadImage(byte[] imageBytes) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);

        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "Report_Attachment.jpg", requestFile);

        //handling text
        RequestBody categoryUnsent = RequestBody.create(MediaType.parse("multipart/form-data"), "Terror Attack");
        RequestBody titleUnsent = RequestBody.create(MediaType.parse("multipart/form-data"), "Dusit Attack");
        RequestBody descriptionUnsent = RequestBody.create(MediaType.parse("multipart/form-data"), "Profiles of key suspects in last month's terror attack in Nairobi point to the worrying rise of a new generation of Kenyan jihadists, analysts say.\n" +
                "\n" +
                "Police are hunting Violet Kemunto Omwoyo, described as a Christian convert to Islam, after felling her 'husband' Ali Salim Gichunge, the suspected mastermind of the January 15 attack on the Dusit hotel and office complex that left 21 dead.");
        RequestBody locationUnsent = RequestBody.create(MediaType.parse("multipart/form-data"), "(JP Headquarters)-1.2655701,36.8346977");
        //submitting the multipart data
        Call<Response> call = retrofitInterface.uploadImage(body,categoryUnsent,titleUnsent,descriptionUnsent,locationUnsent);

        mProgressBar.setVisibility(View.VISIBLE);
        call.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                mProgressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {

                    Response responseBody = response.body();
                    mBtImageShow.setVisibility(View.VISIBLE);
                    mImageUrl = URL + "/media/Report_Attachment.jpg";
                    Snackbar.make(findViewById(R.id.content), "Success",Snackbar.LENGTH_SHORT).show();

                } else {
                    ResponseBody errorBody = response.errorBody();

                    Gson gson = new Gson();

                    try {

                        Response errorResponse = gson.fromJson(errorBody.string(), Response.class);
                        Snackbar.make(findViewById(R.id.content), errorResponse.getMessage(),Snackbar.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {

                mProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: "+t.getLocalizedMessage());
            }
        });
    }
}
