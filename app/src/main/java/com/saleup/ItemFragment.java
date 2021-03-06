package com.saleup;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class ItemFragment extends Fragment {

    Item myObject = null;
    int locationId = 0;

    TextView _txtOffer = null;
    ExpandableHeightGridView _gridView = null;

    public ItemFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        Button _offerButton = (Button) view.findViewById(R.id.btnMakeOffer);
        _offerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitOffer();
            }
        });

        String jsonMyObject = getArguments().getString("myObject");

        myObject = new Gson().fromJson(jsonMyObject, Item.class);

        String user = (String) Cache.GetInstance().Get(getActivity(), "UserData");
        try {
            JSONTokener tokener = new JSONTokener(user);
            JSONObject finalResult = new JSONObject(tokener);

            locationId = Integer.parseInt(finalResult.getString("LocationId"));

        } catch (JSONException ex){

        }

        TextView text = (TextView) view.findViewById(R.id.txtDesc_item);
        text.setText(myObject.Description);

        _txtOffer = (TextView) view.findViewById(R.id.txtMakeOffer);

        _gridView = (ExpandableHeightGridView) view.findViewById(R.id.gridviewItem);

        if(myObject.Image != null) {
            byte[] bitmapdata = Base64.decode(myObject.Image, 0);
            Bitmap bmp = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

            ImageView imageView = (ImageView) view.findViewById(R.id.img_item);
            imageView.setImageBitmap(bmp);
        }

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Collecting data...");
        progressDialog.show();

        new Thread(new MyThread(
                new OnRunMe(){public Result run(){
                    try  {

                        String token = "";
                        String user = (String) Cache.GetInstance().Get(getActivity(), "UserData");
                        try {
                            JSONTokener tokener = new JSONTokener(user);
                            JSONObject finalResult = new JSONObject(tokener);

                            token = finalResult.getString("Token");


                        }
                        catch (JSONException ex){

                        }

                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost("http://saleup.azurewebsites.net/api/Item/UpdateViews");

                        post.addHeader("Content-type", "application/x-www-form-urlencoded");
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("Token", token));
                        urlParameters.add(new BasicNameValuePair("ItemId", Integer.toString(myObject.ItemId)));

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
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) result.data;

                                if(data.getInt("ResultNumber") == 1){
                                    String jsonMyObject = data.getString("Data");
                                    final Item[] items = new Gson().fromJson(jsonMyObject, Item[].class);

                                    Adapter adapter = new Adapter(getActivity(), items);
                                    _gridView.setAdapter(adapter);
                                    _gridView.setExpanded(true);

                                    _gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            ItemFragment fragment = new ItemFragment();
                                            Bundle b = new Bundle();
                                            b.putString("myObject", new Gson().toJson(items[position]));
                                            fragment.setArguments(b);

                                            android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            fragmentManager.beginTransaction()
                                                    .replace(R.id.fragment_container, fragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    });

                                }
                                else{
                                    /*Toast.makeText(getActivity(), "Error! try again!",
                                            Toast.LENGTH_LONG).show();*/
                                }


                            }catch (JSONException ex){
                                Toast.makeText(getActivity(), "Unknown error occurred!",
                                        Toast.LENGTH_LONG).show();
                            }
                            _txtOffer.setText("");
                            progressDialog.dismiss();
                        }
                    });

                }},
                new OnCallback(){public void callback(Result result){
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                        }
                    });

                }}
        )).start();

        return view;
    }

    public void submitOffer(){
        final String offer = _txtOffer.getText().toString();

        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Sending...");
        progressDialog.show();

        new Thread(new MyThread(
                new OnRunMe(){public Result run(){
                    try  {

                        String token = "";
                        String user = (String) Cache.GetInstance().Get(getActivity(), "UserData");
                        try {
                            JSONTokener tokener = new JSONTokener(user);
                            JSONObject finalResult = new JSONObject(tokener);

                            token = finalResult.getString("Token");


                        }
                        catch (JSONException ex){

                        }

                        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                        HttpPost post = new HttpPost("http://saleup.azurewebsites.net/api/Item/SubmitOffer");

                        post.addHeader("Content-type", "application/x-www-form-urlencoded");
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("Token", token));
                        urlParameters.add(new BasicNameValuePair("ItemId", Integer.toString(myObject.ItemId)));
                        urlParameters.add(new BasicNameValuePair("Amount", offer));
                        urlParameters.add(new BasicNameValuePair("LocationId", Integer.toString(locationId)));

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
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) result.data;

                                if(data.getInt("ResultNumber") == 1){
                                    Toast.makeText(getActivity(), "Offer submitted successfully!",
                                            Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(getActivity(), "Error! try again!",
                                            Toast.LENGTH_LONG).show();
                                }

                            }catch (JSONException ex){
                                Toast.makeText(getActivity(), "Unknown error occurred!",
                                        Toast.LENGTH_LONG).show();
                            }
                            _txtOffer.setText("");
                            progressDialog.dismiss();
                        }
                    });

                }},
                new OnCallback(){public void callback(Result result){
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();
                        }
                    });

                }}
        )).start();
    }

}
