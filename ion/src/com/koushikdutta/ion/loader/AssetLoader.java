package com.koushikdutta.ion.loader;

import android.content.Context;
import android.net.Uri;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.stream.InputStreamDataEmitter;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.bitmap.BitmapInfo;

import java.io.InputStream;

/**
 * Created by koush on 6/27/14.
 */
public class AssetLoader extends StreamLoader {
    @Override
    public Future<BitmapInfo> loadBitmap(final Context context, final Ion ion, final String key, final String uri, final int resizeWidth, final int resizeHeight, final boolean animateGif) {
        if (!uri.startsWith("file://android_asset/"))
            return null;

        return super.loadBitmap(context, ion, key, uri, resizeWidth, resizeHeight, animateGif);
    }

    @Override
    protected InputStream getInputStream(Context context, String uri) throws Exception {
        return context.getAssets().open(Uri.parse(uri).getPath().substring(1));
    }

    @Override
    public Future<DataEmitter> load(final Ion ion, final AsyncHttpRequest request, final FutureCallback<LoaderEmitter> callback) {
        if (!request.getUri().getScheme().startsWith("file://android_asset/"))
            return null;

        final InputStreamDataEmitterFuture ret = new InputStreamDataEmitterFuture();
        ion.getHttpClient().getServer().post(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream stream = ion.getContext().getContentResolver().openInputStream(Uri.parse(request.getUri().toString()));
                    if (stream == null)
                        throw new Exception("Unable to load content stream");
                    int available = stream.available();
                    InputStreamDataEmitter emitter = new InputStreamDataEmitter(ion.getHttpClient().getServer(), stream);
                    ret.setComplete(emitter);
                    callback.onCompleted(null, new LoaderEmitter(emitter, available, LoaderEmitter.LOADED_FROM_CACHE, null, null));
                }
                catch (Exception e) {
                    ret.setComplete(e);
                    callback.onCompleted(e, null);
                }
            }
        });
        return ret;
    }
}
