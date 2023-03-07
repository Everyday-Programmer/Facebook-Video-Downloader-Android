package com.example.facebookvideodownloader;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.yausername.youtubedl_android.DownloadProgressCallback;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLRequest;

import java.io.File;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    ProgressDialog dialog;
    ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
        if (result) {
            Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        }
    });
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float v, long l, String s) {

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextInputLayout urlLayout = findViewById(R.id.urlLayout);
        TextInputEditText urlET = findViewById(R.id.urlET);
        MaterialButton download = findViewById(R.id.download);
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Downloading...");

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Facebook Videos");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activityResultLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    if (Objects.requireNonNull(urlET.getText()).toString().isEmpty()) {
                        urlLayout.setError("This field is required");
                    } else {
                        dialog.show();
                        YoutubeDLRequest request = new YoutubeDLRequest(urlET.getText().toString());
                        request.addOption("--no-mtime");
                        request.addOption("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best");
                        request.addOption("-o", dir.getAbsolutePath() + "/%(title)s.%(ext)s");
                        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(youtubeDLResponse -> {
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Downloaded Successfully!", Toast.LENGTH_SHORT).show();
                                }, e -> {
                                    dialog.dismiss();
                                    Toast.makeText(MainActivity.this, "Download Failed " + e.getCause(), Toast.LENGTH_SHORT).show();
                                });
                        compositeDisposable.add(disposable);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }
}