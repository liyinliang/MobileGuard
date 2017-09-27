package cn.edu.gdmec.android.mobileguard.m1home.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;
import android.os.Handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cn.edu.gdmec.android.mobileguard.R;
import cn.edu.gdmec.android.mobileguard.m1home.HomeActivity;
import cn.edu.gdmec.android.mobileguard.m1home.entity.VersionEntity;

/**
 * Created by LYL on 2017/9/27.
 */

public class VersionUpdateUtils {


    private String mVersion;
    private Activity context;
    private ProgressDialog mProgressDialog;
    private VersionEntity versionEntity;

    private static final int MESSAGE_NET_EEOR=101;
    private static final int MESSAGE_IO_EEOR=102;
    private static final int MESSAGE_JSON_EEOR=103;
    private static final int MESSAGE_SHOEW_DIALOG=104;
    private static final int MESSAGE_ENTERHOME=105;

    private Handler handler=new Handler() {
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case MESSAGE_IO_EEOR:
                    Toast.makeText(context, "IO异常", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_JSON_EEOR:
                    Toast.makeText(context, "JSON解析异常", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_NET_EEOR:
                    Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
                    enterHome();
                    break;
                case MESSAGE_SHOEW_DIALOG:
                    showUpdateDialog(versionEntity);
                    break;
                case MESSAGE_ENTERHOME:
                    Intent intent=new Intent(context,HomeActivity.class);
                    context.startActivity(intent);
                    context.finish();
                    break;
            }
        };
    };

    public VersionUpdateUtils(String mVersion,Activity context){
        this.mVersion=mVersion;
        this.context=context;
    }

    public void getCloudVersion(){
        try {
            HttpClient httpclient=new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(httpclient.getParams(),5000);
            HttpConnectionParams.setSoTimeout(httpclient.getParams(),5000);
            HttpGet httpGet=new HttpGet("http://android2017.duapp.com/updateinfo.html");
            HttpResponse execute=httpclient.execute(httpGet);
            if(execute.getStatusLine().getStatusCode()==200){
                HttpEntity httpEntity=execute.getEntity();
                String result= EntityUtils.toString(httpEntity,"utf-8");
                JSONObject jsonObject=new JSONObject(result);

                versionEntity=new VersionEntity();
                versionEntity.versioncode=jsonObject.getString("code");
                versionEntity.description=jsonObject.getString("des");
                versionEntity.apkurl=jsonObject.getString("apkurl");
                if(!mVersion.equals(versionEntity.versioncode)){
                   handler.sendEmptyMessage(MESSAGE_SHOEW_DIALOG);
                }
            }
        } catch (IOException e) {
            handler.sendEmptyMessage(MESSAGE_IO_EEOR);
            e.printStackTrace();
        } catch (JSONException e) {
            handler.sendEmptyMessage(MESSAGE_JSON_EEOR);
            e.printStackTrace();
        }
    }
    private void showUpdateDialog(final VersionEntity versionEntity){
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("检查到新版本："+versionEntity.versioncode);
        builder.setMessage(versionEntity.description);

        builder.setCancelable(false);
        builder.setIcon(R.mipmap.ic_launcher_round);

        builder.setPositiveButton("立即升级",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int i){
                downloadNewApk(versionEntity.apkurl);
            }
        });
        builder.setNegativeButton("暂不升级",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface,int i){
                dialogInterface.dismiss();
                enterHome();
            }
        });
        builder.show();
    }
    private void initProgressDialog(){
        mProgressDialog=new ProgressDialog(context);
        mProgressDialog.setMessage("准备下载...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.show();
    }


    private void enterHome(){
        handler.sendEmptyMessageDelayed(MESSAGE_ENTERHOME,2000);
    }

    private void downloadNewApk(String apkurl){
        DownloadUtils downloadUtils=new DownloadUtils();
        downloadUtils.downloadApk(apkurl,"mobileguard.apk",context);
    }
}
