package com.example.myasyntakpicturepreloadsavesdcard;




import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    URL ImageUrl = null;
    InputStream is = null;
    Bitmap bmImg = null;
    ImageView imageView= null;
    AsyncTaskExample asyncTask=null;
    ProgressDialog p;
    String pictureURL = "http://nonstopcode.com/rigo/img/yo4.png";
    Context context = this;
    Button button, back;


    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    OutputStream output;
    Boolean sdCard =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView=findViewById(R.id.image);
        back = findViewById(R.id.back);
        back.setVisibility(View.GONE);
        button=findViewById(R.id.asyncTask);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncTask=new AsyncTaskExample();
                asyncTask.execute(pictureURL);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageBitmap(null);
                button.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
            }
        });


    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        asyncTask.cancel(true);
                        Toast.makeText(MainActivity.this,"AsyncTask is stopped",Toast.LENGTH_LONG).show();

                    }
                });
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    private class AsyncTaskExample extends AsyncTask<String, String, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
            pDialog.onStart();
            pDialog.setProgress(0);
            back.setVisibility(View.GONE);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                int count;


                ImageUrl = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) ImageUrl
                        .openConnection();
                conn.setDoInput(true);
                conn.connect();
                int lenghtOfFile = conn.getContentLength();
                InputStream input = new BufferedInputStream(ImageUrl.openStream(), 8192);
                File[] storages = ContextCompat.getExternalFilesDirs(context, null);
                if (storages.length > 1 && storages[0] != null && storages[1] != null){
                    output = new FileOutputStream("/sdcard/downloadedfile.jpg");
                    sdCard= true;
                }
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    if (sdCard==true) {
                        output.write(data, 0, count);
                    }

                }

                //output.flush();
                //output.close();
                input.close();

                is = conn.getInputStream();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                bmImg = BitmapFactory.decodeStream(is, null, options);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return bmImg;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(imageView!=null) {
                dismissDialog(progress_bar_type);
                imageView.setImageBitmap(bitmap);
                button.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
            }
        }
    }
}

