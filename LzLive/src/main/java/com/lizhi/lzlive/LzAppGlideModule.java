package com.lizhi.lzlive;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.executor.GlideExecutor;
import com.bumptech.glide.module.AppGlideModule;
import com.yibasan.lizhifm.library.GlideImageLoaderStrategy;
import com.yibasan.lizhifm.library.XLog;
import com.yibasan.lizhifm.library.glide.diskCache.CustomDiskLruCacheFactory;
import com.yibasan.lizhifm.library.glide.loader.SpeedUtil;
import com.yibasan.lizhifm.library.glide.rds.RdsResourceListener;

@GlideModule
public class LzAppGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        if (!GlideImageLoaderStrategy.getDiskCacheDir().exists() || !GlideImageLoaderStrategy.getDiskCacheDir().isDirectory()) {
            GlideImageLoaderStrategy.getDiskCacheDir().mkdirs();
        }
        String diskCachePath = GlideImageLoaderStrategy.getDiskCacheDir().isDirectory() ?
                GlideImageLoaderStrategy.getDiskCacheDir().getAbsolutePath()
                : GlideImageLoaderStrategy.DEFAULT_DISK_CACHE_DIR;
        builder.setDiskCache(new CustomDiskLruCacheFactory(diskCachePath,
                GlideImageLoaderStrategy.DEFAULT_DISK_CACHE_SIZE));
        builder.addGlobalRequestListener(new RdsResourceListener());
        builder.setDiskCacheExecutor(GlideExecutor.newDiskCacheExecutor(3, "Glide-Cache", XLog::e));

        XLog.d("glide ---> CustomImageSizeGlideModule  applyOptions , diskCachePath is %s", diskCachePath);

        int coreSize = SpeedUtil.getInstance().getCores();
        builder.setSourceExecutor(GlideExecutor.newSourceExecutor(coreSize, "Glide-Source", XLog::e));
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}