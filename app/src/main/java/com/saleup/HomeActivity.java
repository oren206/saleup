package com.saleup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.iid.InstanceID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class HomeActivity extends ActionBarActivity {

    @InjectView(R.id.lblUserName) TextView _userNameLabel;
    @InjectView(R.id.btnNotify) Button _notifyButton;
    @InjectView(R.id.btn_disconnect) Button _disconnectButton;
    @InjectView(R.id.btnPhoto) Button _photoButton;
    @InjectView(R.id.imageView) ImageView _photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        _notifyButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendPush();
            }
        });

        _photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        String user = (String) Cache.GetInstance().Get(HomeActivity.this, "UserData");
        try {
            JSONTokener tokener = new JSONTokener(user);
            JSONObject finalResult = new JSONObject(tokener);

            _userNameLabel.setText(finalResult.getString("UserName"));

        }
        catch (JSONException ex){

        }

    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void takePhoto(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), REQUEST_IMAGE_CAPTURE);
                */
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            _photoView.setImageBitmap(imageBitmap);
        }
    }



    public void sendPush(){

        final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        new Thread(new MyThread(
                new OnRunMe(){public Result run(){
                    try  {

                        String token = "";
                        String user = (String) Cache.GetInstance().Get(HomeActivity.this, "UserData");
                        try {
                            JSONTokener tokener = new JSONTokener(user);
                            JSONObject finalResult = new JSONObject(tokener);

                            token = finalResult.getString("DeviceId");


                        }
                        catch (JSONException ex){

                        }

                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost("http://saleup.azurewebsites.net/api/User/TestNotifications");

                        post.addHeader("Content-type", "application/x-www-form-urlencoded");
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("DeviceId", token));

                        post.setEntity(new UrlEncodedFormEntity(urlParameters));


                        HttpResponse responseGet = httpClient.execute(post);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseGet.getEntity().getContent(), "UTF-8"));
                        String json = reader.readLine();

                        JSONTokener tokener = new JSONTokener(json);
                        JSONObject finalResult = new JSONObject(tokener);

                        httpClient.close();

                        Result result = new Result();
                        result.status = true;
                        result.data = finalResult;
                        return  result;

                    } catch (IOException e) {
                        Result result = new Result();
                        result.status = false;
                        return  result;
                    }
                    catch (JSONException e) {
                        Result result = new Result();
                        result.status = false;
                        return  result;
                    }
                }},
                new OnCallback(){public void callback(final Result result){
                    HomeActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Object data = result.data;
                            progressDialog.dismiss();
                        }
                    });

                }},
                new OnCallback(){public void callback(Result result){
                    HomeActivity.this.runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();
                        }
                    });

                }}
        )).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
