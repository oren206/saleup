package com.saleup;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
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
public class HomeFragment extends Fragment {

    View view = null;
    int lastId = Integer.MAX_VALUE;

    Item[] items = null;

    public HomeFragment() {
        // Required empty public constructor



    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_home, container, false);

        /*TextView textView = (TextView) view.findViewById(R.id.lblName);

        Activity a = getActivity();
        String user = (String) Cache.GetInstance().Get(a, "UserData");
        try {
            JSONTokener tokener = new JSONTokener(user);
            JSONObject finalResult = new JSONObject(tokener);

            textView.setText(finalResult.getString("UserName"));

        }
        catch (JSONException ex){

        }
*/

        Button _loadMoreButton = (Button) view.findViewById(R.id.btnLoadMore);
        _loadMoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                load();
            }
        });


        load();


        return view;
    }

    private void load(){
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
                        HttpPost post = new HttpPost("http://saleup.azurewebsites.net/api/Item/GetItemsByPage");

                        post.addHeader("Content-type", "application/x-www-form-urlencoded");
                        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                        urlParameters.add(new BasicNameValuePair("Token", token));
                        urlParameters.add(new BasicNameValuePair("Page", Integer.toString(lastId)));

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
                            try {JSONObject data = (JSONObject) result.data;

                                if(data.getInt("ResultNumber") == 1){
                                    String jsonMyObject = data.getString("Data");
                                    final Item[] newItems = new Gson().fromJson(jsonMyObject, Item[].class);

                                    if(items == null) {
                                        items = newItems;
                                    }
                                    else {
                                        for (int i = 0; i < newItems.length; i++) {
                                            items = Utils.appendArray(items, newItems[i]);
                                        }
                                    }

                                    lastId = newItems[newItems.length - 1].ItemId;

                                    ExpandableHeightGridView grid = (ExpandableHeightGridView) view.findViewById(R.id.gridview);
                                    Adapter adapter = new Adapter(getActivity(), items);
                                    grid.setAdapter(adapter);
                                    grid.setExpanded(true);

                                    grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            ItemFragment fragment = new ItemFragment();
                                            Bundle b = new Bundle();
                                            b.putString("myObject", new Gson().toJson(items[position]));
                                            fragment.setArguments(b);

                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            fragmentManager.beginTransaction()
                                                    .replace(R.id.fragment_container, fragment)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    });




                                }
                                else{
                                    Toast.makeText(getActivity(), "Error! try again!",
                                            Toast.LENGTH_LONG).show();
                                }

                            }catch (JSONException ex){
                                Toast.makeText(getActivity(), "Unknown error occurred!",
                                        Toast.LENGTH_LONG).show();
                            }
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
