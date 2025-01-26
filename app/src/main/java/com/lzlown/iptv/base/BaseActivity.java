package com.lzlown.iptv.base;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import com.lzlown.iptv.R;
import com.lzlown.iptv.util.AppManager;
import com.lzlown.iptv.util.HawkConfig;
import com.lzlown.iptv.videoplayer.util.CutoutUtil;
import com.orhanobut.hawk.Hawk;
import me.jessyan.autosize.AutoSizeCompat;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.internal.CustomAdapt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;

public abstract class BaseActivity extends AppCompatActivity implements CustomAdapt {
    protected Context mContext;
    private static float screenRatio = -100.0f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        try {
            if (screenRatio < 0) {
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int screenWidth = dm.widthPixels;
                int screenHeight = dm.heightPixels;
                screenRatio = (float) Math.max(screenWidth, screenHeight) / (float) Math.min(screenWidth, screenHeight);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        super.onCreate(savedInstanceState);
        setContentView(getLayoutResID());
        mContext = this;
        CutoutUtil.adaptCutoutAboveAndroidP(mContext, true);//设置刘海
        AppManager.getInstance().addActivity(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSysBar();
//        changeWallpaper(false);
    }

    public void hideSysBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public Resources getResources() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            AutoSizeCompat.autoConvertDensityOfCustomAdapt(super.getResources(), this);
        }
        return super.getResources();
    }

    public boolean hasPermission(String permission) {
        boolean has = true;
        try {
            has = PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return has;
    }

    protected abstract int getLayoutResID();

    protected abstract void init();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppManager.getInstance().finishActivity(this);
    }

    public void jumpActivity(Class<? extends BaseActivity> clazz) {
        Intent intent = new Intent(mContext, clazz);
        startActivity(intent);
    }

    public void jumpActivity(Class<? extends BaseActivity> clazz, Bundle bundle) {
        Intent intent = new Intent(mContext, clazz);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    protected String getAssetText(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assets = getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(assets.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public float getSizeInDp() {
        return isBaseOnWidth() ? 1280 : 720;
    }

    @Override
    public boolean isBaseOnWidth() {
        return !(screenRatio >= 4.0f);
    }

    public float getTargetDensity() {
        if (isBaseOnWidth()) {
            return (float) AutoSizeConfig.getInstance().getScreenWidth() / getSizeInDp();
        } else {
            return (float) AutoSizeConfig.getInstance().getScreenHeight() / getSizeInDp();

        }
    }

    protected static BitmapDrawable globalWp = null;

    public void changeWallpaper(boolean force) {
        if (!force && globalWp != null)
            getWindow().setBackgroundDrawable(globalWp);
//        try {
//            File wp = new File(getFilesDir().getAbsolutePath() + "/wp");
//            if (wp.exists()) {
//                BitmapFactory.Options opts = new BitmapFactory.Options();
//                opts.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(wp.getAbsolutePath(), opts);
//                int imageHeight = opts.outHeight;
//                int imageWidth = opts.outWidth;
//                int picHeight = 720;
//                int picWidth = 1080;
//                int scaleX = imageWidth / picWidth;
//                int scaleY = imageHeight / picHeight;
//                int scale = 1;
//                if (scaleX > scaleY && scaleY >= 1) {
//                    scale = scaleX;
//                }
//                if (scaleX < scaleY && scaleX >= 1) {
//                    scale = scaleY;
//                }
//                opts.inJustDecodeBounds = false;
//                opts.inSampleSize = scale;
//                globalWp = new BitmapDrawable(BitmapFactory.decodeFile(wp.getAbsolutePath(), opts));
//            } else {
//                globalWp = null;
//            }
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            globalWp = null;
//        }
        if (globalWp != null)
            getWindow().setBackgroundDrawable(globalWp);
        else
            getWindow().setBackgroundDrawableResource(R.drawable.home_bg);
    }
}