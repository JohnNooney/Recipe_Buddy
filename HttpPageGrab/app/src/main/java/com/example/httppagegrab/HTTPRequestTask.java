package com.example.httppagegrab;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

//async task that will read and write at from the source at the same time
public class HTTPRequestTask extends AsyncTask<String, Void, String> {

    String server_response;
    MainActivity mActivity;

    public HTTPRequestTask(MainActivity mActivity)
    {
        this.mActivity = mActivity; //create singleton
    }

    @Override
    protected String doInBackground(String... strings) {
        URL url;
        HttpURLConnection urlConnection = null;


        //attempt to connect to provided url and get the input stream
        try {
            url = new URL(strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();

            Log.d("URL Recieved", url.toString());

            int responseCode = urlConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                server_response = readStream(urlConnection.getInputStream());
                Log.v("CatalogClient", server_response);
                mActivity.AsyncResult(server_response);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readStream(InputStream inputStream) {
        //create a buffer reader
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while((line = reader.readLine()) != null) //get all text until none is left
            {
                response.append(line);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        //once done building reader check
        finally{
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

}
