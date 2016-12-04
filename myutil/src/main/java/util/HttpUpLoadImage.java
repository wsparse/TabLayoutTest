package util;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by Admin on 2016/9/22.
 */
public class HttpUpLoadImage {
    /*3. 封装的框架中应满足以下要求：
    a)	可以进行POST和GET请求
	b)	网络请求的操作应当封装在子线程中
	c)	网络请求结束后应该有回调
	d)	应该能够直接在回调方法中更新UI
	e)	线程应该使用线程池进行管理
	f)	框架的工具类应该是单例模式*/
    /**
     * 主要流程:
     * 1. 把Bitmap对象转成流对象,方便后续读取
     * 先把Bitmap压缩到输出流中
     * 然后再转换成输入流
     * 2. 读物图片数据
     * 3. 把读到的数据,写入网络流
     * 4. 写完之后,需要读出服务器返回的结果
     */

    public static HttpUpLoadImage httpUpLoadImage;  //实例对象。
    //线程池对象。
    private static ExecutorService service;
    //消息抬头what.常量。
    public static final int WHTA = 1;
    //请求结束回调接口。换成Map集合对象存储各个回调接口，避免异步冲突。
    HashMap<Integer,OnCompleteListener> listenerHashMap ;

    //私有化构造器，便于实现单例。
    private HttpUpLoadImage(){

    }
    int key = -1; //存储OnCompleteListener接口的键。
    //唯一获取实例的方法。
    public static HttpUpLoadImage newInstance() {
        if (httpUpLoadImage == null) {
            synchronized (HttpUpLoadImage.class) {
                if (httpUpLoadImage == null) {
                    httpUpLoadImage = new HttpUpLoadImage();
                    service = Executors.newFixedThreadPool(3);
                }
            }
        }
        return httpUpLoadImage;
    }



    //图片上传的方法
    public void upLoadImages(String imageName, Bitmap bitmap,String path,long imageId,OnCompleteListener mListener){
        if(listenerHashMap == null ){
            listenerHashMap = new HashMap<>();
        }
        listenerHashMap.put(++key,mListener);
        service.execute(new UpLoadImageRun(imageName,bitmap,path,imageId,key));
    }


    public class UpLoadImageRun implements Runnable{
        private String imageName;
        private Bitmap bitmap;
        private String path;
        private long imageId = 0;
        private int key = -1;

        public UpLoadImageRun(String imageName,Bitmap bitmap,String path,long imageId, int key){
            this.imageName = imageName;
            this.bitmap = bitmap;
            this.path = path;
            this.imageId = imageId ;
            this.key = key;
        }
        @Override
        public void run() {
            //图片上传的线程。
            /**1. 把Bitmap对象转成流对象,方便后续读取
             先把Bitmap压缩到输出流中
             然后再转换成输入
             2  读物图片数据
             3  把读到的数据,写入网络流
             4 写完之后,需要读出服务器返回的结果
             */


               Message msg = Message.obtain();
                msg.what = WHTA;
                msg.arg1 = key;
            try {
                URL url = new URL(path);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                //获取HttpURLConnection对象。设置可读可写，不使用Cache
                con.setDoInput(true);
                con.setDoOutput(true);
                con.setUseCaches(false);
            //设置传送的method,setRequestProperty属性。
               con.setRequestMethod(MethodName.POST);
                con.setRequestProperty("Connection","Keep-Alive");
                con.setRequestProperty("Charset","UTF-8");
                con.setRequestProperty("Content-Type","multipart/form-data;boundary=*****");
            //设置DataOutputStream
                DataOutputStream dos = new DataOutputStream(con.getOutputStream());
                dos.writeBytes("--*****\r\nContent-Disposition: form-data; " + "name=\""
                        + imageId + "\";filename=\"" + imageName + "\"" + "\r\n\r\n");

                System.out.println("线程中...........imageId.............."+imageId);
            //bitmap转流 ，获取输出流ByteArrayOutputStream
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            //bitmap 压缩到字节数组流中
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteOut);
            //把输出流中的数据转化到输入流中，方便读取。
                InputStream in = new  ByteArrayInputStream(byteOut.toByteArray());
            //设置每次写入的数据字节长度，指定读取得到的初始长度。
                byte[] buf = new byte[1024];
                int lenght = -1;
            //从文件读取数据至缓冲区，将缓冲区中的数据写入到DataOutputStream中。
                while((lenght =in.read(buf)) != -1){
                    dos.write(buf,0,lenght);//将buf中指定长度的数据添加到dos流中。
                }
                System.out.println("图片写入完成.........");
            //关闭输入流，刷新数据输出流。
                dos.writeBytes("\r\n--******--\r\n");   //写入分隔标记。
                in.close();
                dos.flush();
            //字符输入流取得Response内容。
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line = null;
                StringBuffer sbuffer = new StringBuffer();
                while((line = br.readLine()) != null){
                    sbuffer.append(line);
                }
            //关闭数据输出流 。
                dos.close();
            //msg.obj绑定服务器反馈回来的消息。
                System.out.println("服务器反馈回来的数据为:"+sbuffer.toString());
                msg.obj = sbuffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
                msg.obj = null;
            }finally {
                //finally发送消息到handler.
                handler.sendMessage(msg);
            }
        }
    }

    //回调接口声明

   public interface OnCompleteListener{
       void onComplete(String result);
    }

    //封装两种请求方法名称的常量。
    public static class MethodName{
        public static final String POST = "POST";
        public static final String GET = "GET";
    }
    //请求结束收到消息，执行回调方法的调用。 //handler处理。

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case WHTA:
                    OnCompleteListener mListener =  listenerHashMap.get(msg.arg1);
                    if(mListener != null){
                        mListener.onComplete(msg.obj.toString());
                    }
                    break;
            }
        }
    };
}