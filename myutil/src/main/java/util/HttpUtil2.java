package util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



/**
 * Created by bwfadmin on 2016/9/4.
 */
public class HttpUtil2 {
    /*3. 封装的框架中应满足以下要求：
		a)	可以进行POST和GET请求
		b)	网络请求的操作应当封装在子线程中
		c)	网络请求结束后应该有回调
		d)	应该能够直接在回调方法中更新UI
		e)	线程应该使用线程池进行管理
		f)	框架的工具类应该是单例模式*/
    //实现单例模式。
    //声明无私有的无参构造函数。

    public static  HttpUtil2 util2;  //声明工具类对象的引用。
    public static ExecutorService service ; // 声明线程池管理对象，实现资源的合理使用。
    //private OnCompleteListener mListener;  //请求结束回调接口。
    int index = -1;
    HashMap<Integer, OnCompleteListener> maps = new HashMap<>();
    public HttpUtil2(){
    }

    public interface  OnCompleteListener{
        void onComplete(String result);  //抽象方法。
    }

    //为引用赋值。
    public static HttpUtil2 newInstance(){
        if(util2 == null){
            synchronized (HttpUtil2.class){
                if(util2 == null){
                    util2 = new HttpUtil2();
                    service = Executors.newFixedThreadPool(3);
                }
            }
        }
        return util2;
    }

    /*
     * 获取网络数据的方法
     * @param	String url, 网络请求接口地址
     * @param	String method, 请求方法,比如POST,GET等
     * @param	Map map, 请求参数
     * @param	OnCompleteListener mListener, 请求结束的回调
    */

    public void  getData(String url , String method , Map<String,String> map , OnCompleteListener mListener){
        //this.mListener = mListener;
        maps.put(++index,  mListener);
        service.submit(new GetRunnable(url,method,map, index));
    }

    public class GetRunnable implements Runnable{
        private String url;
        private String method;
        private Map<String,String> map;
        private int key;
        public GetRunnable(String url, String method ,Map<String ,String> map, int index){
            this.key = index;
            this.url = url;
            this.method = method;
            this.map = map;
        }

        public  class Method{  //内部类。
            final static String GET = "GET";
            final static String POST = "POST";
        }
        @Override
        public void run() {
            HttpURLConnection connection  = null; //HttpURLConnection连接对象。
            URL path = null;  //路径
            int code = -1 ; //响应码。
            BufferedReader br = null;
            BufferedWriter bw = null;
            Message msg = Message.obtain();
            msg.what = 1;
            msg.arg1 = key;
            try {
                if(Method.GET.equals(method)){  //请求方式为GET
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(url).append("?");
                    for(Map.Entry entry : map.entrySet()){  //遍历键集合。
                        buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                    }

                    //拼接完条件后删除最后一个"&"
                    buffer.deleteCharAt(buffer.length()-1);
                    Log.i("aa", buffer.toString());
                    path = new URL(buffer.toString());
                    connection = (HttpURLConnection) path.openConnection();
                    connection.setRequestMethod(method);// 设置请求方法。
                }else {
                    if (Method.POST.equals(method)) { //请求方式为POST
                        path = new URL(url); //POST方式是直接通过url来创建URL,然后打开连接，再将查询条件写入其中，等待回应。
                        connection = (HttpURLConnection) path.openConnection();
                        connection.setDoInput(true); //可读
                        connection.setDoOutput(true); //可写
                        connection.setRequestMethod(method);
                        bw = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                        StringBuffer buffer = new StringBuffer();
                        //buffer.append(url).append("?"); //POST方式请求拼接时不要？
                        for (Map.Entry entry : map.entrySet()) {
                            buffer.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                        }
                        buffer.deleteCharAt(buffer.length() - 1);
                        bw.write(buffer.toString());
                        bw.flush();
                    }

                }
                code = connection.getResponseCode();

                if(code == 200){
                    br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = null;
                    StringBuffer buffer = new StringBuffer();
                    while((line = br.readLine()) != null){
                        buffer.append(line);
                    }
                    String infor = buffer.toString();  //访问请求得到的字符串。
                    msg.obj = infor;
                    //System.out.println("请求成功！");
                }else{
                    msg.obj = null;
                    System.out.println("请求失败！");
                    System.out.println("响应码为：.........."+code);  //500:服务器端异常。
                }
                handler.sendMessage(msg);  //发送消息
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.toString()+"请求抛异常！");
                msg.obj = 1;
            } finally {
                try {
                    br.close();
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //System.out.println("处理请求数据");
            switch (msg.what){
                case 1:
                    OnCompleteListener mListener = maps.get(msg.arg1);  //从maps集合中提取出mListener对象。
                    if(mListener != null){
                        if(msg.obj instanceof String){
                            mListener.onComplete(msg.obj.toString());  //回调结束，将msg.obj属性返回
                            //System.out.println("获取反馈数据成功！");
                        }else{
                            mListener.onComplete(null);
                            System.out.println("获取反馈数据失败！");
                        }
                    }
                    break;
            }
        }
    };
    public void clearListener(){
        maps.clear();
        maps = null;
    }
}
