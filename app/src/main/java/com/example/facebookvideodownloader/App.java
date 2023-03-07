package com.example.facebookvideodownloader;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        configure();
        Completable.fromAction(this::init).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(App.this, "Init failed: " + e.getCause(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configure() {
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof InterruptedException) {
                return;
            }
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            Log.e("Tag", "Undeliverable Exception", e);
        });
    }

    private void init() throws YoutubeDLException {
        YoutubeDL.getInstance().init(this);
        FFmpeg.getInstance().init(this);
    }
}