package com.example.testrecipeadder.recipeController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.testrecipeadder.recipe_form;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HTTPBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "HTTPBroadcastReceiver";
    public static String html = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String url = intent.getExtras().get("webUrl").toString();
        //Log.d("Receiver", url);
        //Toast.makeText(context, url, Toast.LENGTH_LONG).show();

        //* AsyncTask is necessary for performing a HttpURLConnection in a broadcast receiver
        final PendingResult pendingResult = goAsync(); //flags receiver that more time is needed to complete task (normally 10sec limit)
        Task asyncTask = new Task(pendingResult, url, context);
        asyncTask.execute();

    }

    //async task that will read and write from the source URL to get the HTML of the page
    private static class Task extends AsyncTask<String, Integer, String> {

        private final PendingResult pendingResult;
        private final String weburl;
        private Context context;

        private Task(PendingResult pendingResult, String weburl, Context context) {
            this.pendingResult = pendingResult;
            this.weburl = weburl;
            this.context = context;
        }

        @Override
        protected String doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;

            //attempt to connect to provided url and get the input stream
            try {
                url = new URL(weburl);
                urlConnection = (HttpURLConnection) url.openConnection();

                Log.d("URL Recieved", url.toString());

                int responseCode = urlConnection.getResponseCode();

                //ensure connection to URL
                if(responseCode == HttpURLConnection.HTTP_OK){
                    html = readStream(urlConnection.getInputStream());
                    Log.v("URLResponse", html);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return html;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            //create intent to send to main activity
            Intent intent = new Intent(context, recipe_form.class);
            intent.putExtra("htmlCallback", html);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);

            // Must call finish() so the BroadcastReceiver can be recycled.
            pendingResult.finish();
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

}
