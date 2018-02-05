package com.restapi;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class LoginActivity extends AppCompatActivity {

    private String username;

    private static final String clientID = "006538d11e78ace0816f";
    private static final String clientSecret = "defa5a7f68fd2fd78b081824a9abc75d80ca8bef";
    private static final String redirectUri = "restapi://callback";
    private String code;

    private String token;

    private Button create,delete,back;
    private ListView listView;
    private TextView nameOfRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setUpUI();
        setUpOnClickForAllButton();
    }
    private void getUsername(){
        GetUsername getUsername = new GetUsername();
        getUsername.execute();
    }

    private void listOfRepoAsyncTaskRun(){
        GetListOfRepository getListOfRepository = new GetListOfRepository();
        getListOfRepository.execute();
    }

    private void setUpUI(){
        create = findViewById(R.id.createRepositoryButton);
        delete = findViewById(R.id.deleteButton);
        back = findViewById(R.id.backButton);
        listView = findViewById(R.id.listView);
        nameOfRepo = findViewById(R.id.nameOfRepository);
    }
    private void setUpOnClickForAllButton(){
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameOfRepo.getText().toString().isEmpty()){
                    UseTokenAsyncTask useTokenAsyncTask = new UseTokenAsyncTask();
                    String [] toSend = new String[2];
                    toSend[0] = "create";
                    toSend[1] = nameOfRepo.getText().toString();
                    useTokenAsyncTask.execute(toSend);
                }
                else{
                    Toast.makeText(LoginActivity.this,"Name is ether null or empty", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity","Name is ether null or empty");
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nameOfRepo.getText().toString().isEmpty()){
                    UseTokenAsyncTask useTokenAsyncTask = new UseTokenAsyncTask();
                    String [] toSend = new String[2];
                    toSend[0] = "delete";
                    toSend[1] = nameOfRepo.getText().toString();
                    useTokenAsyncTask.execute(toSend);
                }
                else{
                    Toast.makeText(LoginActivity.this,"Name is ether null or empty", Toast.LENGTH_LONG).show();
                    Log.e("MainActivity","Name is ether null or empty");
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginActivity.super.onBackPressed();
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        Uri uri = getIntent().getData();

        if(uri != null && uri.toString().startsWith(redirectUri)){
            code = uri.getQueryParameter("code");
            GetTokenAsyncTask getTokenAsyncTask = new GetTokenAsyncTask();
            getTokenAsyncTask.execute();
            getUsername();
            listOfRepoAsyncTaskRun();
        }
    }

    private class GetListOfRepository extends  AsyncTask<Void,Integer,Void>{
        private ArrayList<String> listOfRepository;
        @Override
        protected Void doInBackground(Void... voids) {
            listOfRepository = new ArrayList<>();
            HttpsURLConnection myConnection = setUpConnection("https://api.github.com/user/repos","GET");
            try {
                if(myConnection.getResponseCode()==200){
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);

                    jsonReader.beginArray();
                    while (jsonReader.hasNext()) {
                        listOfRepository.add(readMessage(jsonReader));
                    }
                    jsonReader.endArray();
                } else{
                    Log.i("Error", "Could not get list of repository:" + myConnection.getResponseMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            String[] values = new String[listOfRepository.size()];
            for(int idOfValues = 0; idOfValues<listOfRepository.size(); idOfValues++){
                Log.i("List",listOfRepository.get(idOfValues));
                values[idOfValues] = listOfRepository.get(idOfValues);
                Log.i("Valuse",values[idOfValues]);
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(LoginActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,values);
            listView.setAdapter(arrayAdapter);
        }

        private String readMessage(JsonReader reader)throws IOException{
            String name=null;
            reader.beginObject();
            while(reader.hasNext()){
                String key = reader.nextName();
                if (key.equals("name")) {
                    Log.i("iteracja","1");
                    name =  reader.nextString();
                }
                else{
                    reader.skipValue();
                }
            }
            reader.endObject();
            return name;
        }
    }
    private class GetUsername extends  AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            HttpsURLConnection getUsernameConnection = setUpConnection("https://api.github.com/user","GET");
            try{
                if(getUsernameConnection.getResponseCode() == 200){
                    InputStream responseBody = getUsernameConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    JsonReader jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject();
                    while(jsonReader.hasNext()){
                        String key = jsonReader.nextName();
                        if (key.equals("login")) {
                            username = jsonReader.nextString();
                        }
                        else{
                            jsonReader.skipValue();
                        }
                    }
                }
                else{
                    Log.i("Error","Could not get username from server:" + getUsernameConnection.getResponseMessage());
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
    private class GetTokenAsyncTask extends AsyncTask<String, Integer, String>{
        JsonReader jsonReader;
        HttpsURLConnection myConnection;
        @Override
        protected String doInBackground(String... strings) {
            try {
                sendPOSTMethodForToken();
                if (myConnection.getResponseCode() == 200) {
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    jsonReader = new JsonReader(responseBodyReader);
                    jsonReader.beginObject();
                    jsonReader.nextName();
                    return jsonReader.nextString();
                } else {
                    Log.d("Tag","Could not connect");
                    // Error handling code goes here
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String strings) {
            super.onPostExecute(strings);
            token = strings;
        }
        private void sendPOSTMethodForToken() throws IOException {
            URL accessTokenEndPoint = new URL("https://github.com/login/oauth/access_token");
            myConnection = (HttpsURLConnection) accessTokenEndPoint.openConnection();
            myConnection.setRequestProperty("Accept", "application/json");
            myConnection.setRequestMethod("POST");
            myConnection.setDoInput(true);
            myConnection.setDoOutput(true);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("client_id",clientID)
                    .appendQueryParameter("client_secret",clientSecret)
                    .appendQueryParameter("code",code);
            String query = builder.build().getEncodedQuery();

            OutputStream os = myConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8")));
            writer.write(query);
            writer.flush();
            writer.close();
            myConnection.connect();
        }

    }
    private class UseTokenAsyncTask extends AsyncTask<String, Integer, String[]>{
        @Override
        protected String[] doInBackground(String ... strings) {
            Log.i("ef",String.valueOf(strings.length));
            if(("create").equals(strings[0])){
                try{
                    Log.i("sad",token);
                    HttpsURLConnection myConnection = setUpConnection("https://api.github.com/user/repos","POST");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("name", strings[1]);
                    jsonObject.put("description", "This is your repository");
                    jsonObject.put("homepage","https://github.com");
                    jsonObject.put("private", false);
                    jsonObject.put("has_issues", false);
                    jsonObject.put("has_wiki", false);
                    Log.i("JSON",jsonObject.toString());
                    DataOutputStream os = new DataOutputStream(myConnection.getOutputStream());
                    os.writeBytes(jsonObject.toString());
                    os.flush();
                    os.close();
                    if(myConnection.getResponseCode() != 200){
                        Toast.makeText(LoginActivity.this,"Could not create repository: " + myConnection.getResponseMessage(),Toast.LENGTH_SHORT);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else if("delete".equals((strings[0]))){
                //HttpsURLConnection getUsernameConnection = setUpConnection("https://api.github.com/user","GET");
                try {
                        Log.i("endpoint","https://api.github.com/repos/"+username+"/"+strings[1]);
                        HttpsURLConnection myConnection = setUpConnection("https://api.github.com/repos/"+username+"/"+strings[1],"DELETE");
                        if(myConnection.getResponseCode()!=200){
                            Log.i("Error: ",myConnection.getResponseMessage());
                        }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            listOfRepoAsyncTaskRun();
        }
    }
    private HttpsURLConnection setUpConnection(String endpoint, String method){
        try{
            URL httpEndpoint = new URL(endpoint);
            HttpsURLConnection myConnection = (HttpsURLConnection) httpEndpoint.openConnection();
            myConnection.setRequestMethod(method);
            myConnection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
            myConnection.setRequestProperty("Accept","application/json");
            myConnection.setRequestProperty("Authorization","token "+token);
            if("POST".equals(method)){
                myConnection.setDoInput(true);
                myConnection.setDoOutput(true);
            }
            return myConnection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
