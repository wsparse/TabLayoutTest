package util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;


/**
 * Created by Admin on 2016/11/3.
 *
 * volly采用jar包依赖
 */

public class VollySingleton {  //Volly请求的单例模式。



    private static VollySingleton singleton;   //单例对象的引用。
    private ImageLoader mImageLoader;  //图片加载器
    private RequestQueue mRequestQueue; //请求队列
    private Context mContext;  //上下文

    private VollySingleton(Context context){ //单例的无参构造函数。
        this.mContext = context;
        mRequestQueue = getRequestQueue();
        mImageLoader  = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
           private final LruCache<String,Bitmap> lruCache = new LruCache<>(20);
            @Override
            public Bitmap getBitmap(String url) {
                return lruCache.get(url); //从缓存对象lruCache中获取键为url的bitmap对象。
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                lruCache.put(url,bitmap); //将键为路径的bitmap对象添加到lruCache缓存中。
            }
        });
    }

    public  RequestQueue getRequestQueue() { //声明为public权限时可以在外部获取方法。
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public static VollySingleton newInstance(Context context){
        if(singleton == null ){
            synchronized (VollySingleton.class){
                if(singleton == null){
                    singleton = new VollySingleton(context);
                }
            }
        }
        return singleton;
    }
    public ImageLoader getmImageLoader(){  //提供给外部获取mImageLoder对象的方法
        return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req) { //获取请求队列将请求放入其中。
        getRequestQueue().add(req);
    }
}
