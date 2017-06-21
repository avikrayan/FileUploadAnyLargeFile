package rayan.avik.fileupload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private Button button, upload;
    TextView showPath;
    String file_path;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        upload = (Button) findViewById(R.id.upload);
        showPath = (TextView) findViewById(R.id.show_path);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return;
            }
        }

        enable_button();
    }

    private void enable_button() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialFilePicker()
                        .withActivity(MainActivity.this)
                        .withRequestCode(10)
                        .start();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            enable_button();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == 10 && resultCode == RESULT_OK) {

            final File file = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            final String content_type = getMimeType(file.getPath());
            file_path = file.getAbsolutePath();
            showPath.setText(file_path);

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    progress = new ProgressDialog(MainActivity.this);
                    progress.setTitle("Uploading");
                    progress.setMessage("Please wait...");
                    progress.show();

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {



                            OkHttpClient client = new OkHttpClient();
                            RequestBody file_body = RequestBody.create(MediaType.parse(content_type), file);

                            RequestBody request_body = new MultipartBody.Builder()
                                    .setType(MultipartBody.FORM)
                                    .addFormDataPart("type", content_type)
                                    .addFormDataPart("uploaded_file", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                                    .build();

                            Request request = new Request.Builder()
                                    .url("http://localhost/testing/save_file.php")
                                    .post(request_body)
                                    .build();

                            try {
                                Response response = client.newCall(request).execute();

                                if (!response.isSuccessful()) {
                                    throw new IOException("Error : " + response);
                                }

                                progress.dismiss();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    t.start();

                }
            });

        }
    }

    private String getMimeType(String path) {

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
