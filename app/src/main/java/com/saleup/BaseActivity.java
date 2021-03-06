package com.saleup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.app.Fragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar = null;
    NavigationView navigationView = null;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Bitmap imageBitmap = null;
    byte[] byteArray = {1,2,3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //Set the fragment initially
        HomeFragment fragment = new HomeFragment();
        /*android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null).commit();*/

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                /*Intent k = new Intent(BaseActivity.this, CameraActivity.class);
                startActivity(k);
                finish();
                */

                takePhoto();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView textView = (TextView) findViewById(R.id.lblDisplayName);
        TextView textView2 = (TextView) findViewById(R.id.lblDisplayEmail);

        String user = (String) Cache.GetInstance().Get(this, "UserData");
        try {
            JSONTokener tokener = new JSONTokener(user);
            JSONObject finalResult = new JSONObject(tokener);

            textView.setText(finalResult.getString("UserName"));
            textView2.setText(finalResult.getString("Email"));

        }
        catch (JSONException ex){

        }
    }


    @Override
    public void onBackPressed() {
        /*
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
        super.onBackPressed();
    }
    */
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            HomeFragment fragment = new HomeFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if (id == R.id.nav_my_sales) {
            MySalesFragment fragment = new MySalesFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if(id == R.id.nav_my_sales_history) {
            MySalesHistoryFragment fragment = new MySalesHistoryFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if (id == R.id.nav_my_offers) {
            MyOffersFragment fragment = new MyOffersFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if (id == R.id.nav_my_offers_history) {
            MyOffersHistoryFragment fragment = new MyOffersHistoryFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if (id == R.id.nav_hot_items) {
            HotItemsFragment fragment = new HotItemsFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if(id == R.id.nav_profile) {
            MyProfileFragment fragment = new MyProfileFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if(id == R.id.nav_about) {
            AboutFragment fragment = new AboutFragment();

            android.support.v4.app.FragmentManager fragmentManager = BaseActivity.this.getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

        }
        else if(id == R.id.nav_disconnect){
            disconnect();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void disconnect(){
        final ProgressDialog progressDialog = new ProgressDialog(BaseActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Disconnecting...");
        progressDialog.show();

        new Thread(new MyThread(
                new OnRunMe(){public Result run(){
                    try  {

                        String token = "";
                        String deviceId = "";
                        String user = (String) Cache.GetInstance().Get(BaseActivity.this, "UserData");
                        try {
                            JSONTokener tokener = new JSONTokener(user);
                            JSONObject finalResult = new JSONObject(tokener);

                            token = finalResult.getString("Token");
                            deviceId = finalResult.getString("DeviceId");


                        }
                        catch (JSONException ex){

                        }

                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost("http://saleup.azurewebsites.net/api/User/DissconnectDevice");

                        post.addHeader("Content-type", "application/x-www-form-urlencoded");
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("Token", token));
                        urlParameters.add(new BasicNameValuePair("DeviceId", deviceId));

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
                    BaseActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) result.data;

                                if(data.getInt("ResultNumber") == 1){
                                    Toast.makeText(BaseActivity.this, "Disconnected successfully!",
                                            Toast.LENGTH_LONG).show();

                                    Intent k = new Intent(BaseActivity.this, LoginActivity.class);
                                    startActivity(k);
                                }
                                else{
                                    Toast.makeText(BaseActivity.this, "Error! try again",
                                            Toast.LENGTH_LONG).show();
                                }

                            }catch (JSONException ex){
                                Toast.makeText(BaseActivity.this, "Unknown error occurred!",
                                        Toast.LENGTH_LONG).show();
                            }
                            progressDialog.dismiss();
                        }
                    });

                }},
                new OnCallback(){public void callback(Result result){
                    BaseActivity.this.runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();
                        }
                    });

                }}
        )).start();
    }

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
        //ImageView _photoView = (ImageView) findViewById(R.id.img_preview);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            //_photoView.setImageBitmap(imageBitmap);

            /*
            int width = imageBitmap.getWidth();
            int height = imageBitmap.getHeight();

            int size = imageBitmap.getRowBytes() * imageBitmap.getHeight();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);
            imageBitmap.copyPixelsToBuffer(byteBuffer);
            byteArray = byteBuffer.array();
            */

            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOS);
            byteArray = byteArrayOS.toByteArray();

            Cache.GetInstance().Set(BaseActivity.this, "byteArray", byteArray);

            Intent k = new Intent(BaseActivity.this, CameraActivity.class);
            startActivity(k);
            finish();
        }
    }
}
