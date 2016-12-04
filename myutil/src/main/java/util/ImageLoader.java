package util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bwfadmin on 2016/9/6.
 */
public class ImageLoader {
    /*  1.定义框架工具类
	2.	实现单例模式
	3.	创建线程池
	4.	实现缓存策略
	5.	定义缩放图片的方法
	6.	实现子线程更新UI的方式
	7.	封装请求方法
	8.	定义图片加载的线程类*/
    public static ImageLoader mLoader;  //静态实例类对象
    private ExecutorService mService;  //线程管理池对象。
    private LruCache<String,Bitmap> mLruCache; //缓冲图片。
    String sdPath; //外部内存的路径。
    private ImageLoader(){
        mService = Executors.newFixedThreadPool(3);
        long maxMemory = Runtime.getRuntime().maxMemory();
        mLruCache = new LruCache<>((int) maxMemory/8);  //初始化缓冲集合对象。指定缓冲内存占最大内存的1/8
        sdPath = Environment.getExternalStorageDirectory().getPath()+ File.separator+"MyCache";  //初始化外部存储路径。
    }

    public static ImageLoader newInstance(){
        if(mLoader == null){
            synchronized (ImageLoader.class){
                if(mLoader == null ){
                    mLoader = new ImageLoader();
                }
            }
        }
        return mLoader;
    }

    public void imageLoad(String fileName , ImageView targetView){  //加载图片。
        Bitmap bitmap = null;
        targetView.setTag(fileName);
        //从mLruCache中读取图片
        bitmap = mLruCache.get(fileName); //读取mLruCache缓存中的图片赋值给bitmap
        if(bitmap != null){ //赋值成功
            setBitMap(fileName,targetView,bitmap);
            System.out.println("读取mLruCache缓存中的图片赋值给bitmap");
            return;
        }

        //mLruCache中没有取到想要的图片，从SD卡中取。
        String path = sdPath+File.separator+fileName;
        bitmap = BitmapFactory.decodeFile(path);  //图片赋值。
        if(bitmap != null){
            setBitMap(fileName,targetView,bitmap);
            System.out.println("读取sd卡中的图片赋值给bitmap");
            mLruCache.put(fileName,bitmap); //将从sd卡中读取的图片添加到mLruCache集合中，便于下次使用。
            return;
        }

        //上述两种方式都没有取到，则采取网路请求的方式，开启线程请求。
        LoadRun run = new LoadRun(fileName,targetView);
        mService.submit(run); //在线程池中提交线程。
    }

    private void setBitMap(String fileName, final ImageView targetView, final Bitmap bitmap) {
        String tag = targetView.getTag().toString();  //获取tag
        if(tag == null || !tag.equals(fileName)){ //tag为null或键名不为fileName
            return;
        }
        Activity activity = null;
        activity = (Activity) targetView.getContext();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                targetView.setImageBitmap(bitmap);
            }
        });
    }

    class LoadRun implements Runnable{
        String fileName;
        ImageView targetView;
        private LoadRun(String fileName , ImageView targetView){
            this.fileName = fileName;
            this.targetView = targetView;
        }
        @Override
        public void run() {
            HttpURLConnection connection = null;
            URL url = null;
            String base = "http://123.56.145.151:8080/JiuPinHui/Image/";
            String path = base+fileName;
            try {
                url = new URL(path);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(20 * 1000);
                int code = connection.getResponseCode();
                if(code == 200){
                    InputStream in = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    setBitMap(fileName,targetView,bitmap); //设置图片
                    mLruCache.put(fileName,bitmap); //将请求到的图片添加到缓冲集合mLruCache中。

                    //将请求的图片加载到sd卡上的外部存储中。
                    String imgPath = sdPath+File.separator+fileName;
                    FileOutputStream out = new FileOutputStream(imgPath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
                    System.out.println("请求成功！");
                }else{
                    System.out.println("请求失败！"+code);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("请求发生异常！");
            }
        }
    }
}
