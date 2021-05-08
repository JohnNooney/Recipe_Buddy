package com.example.testrecipeadder.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class PermissionUtils {
    //Request codes for permissions
    public static final int READ_EXTERNAL_REQUEST = 0;
    public static final int WRITE_EXTERNAL_REQUEST = 1;
    public static final int INTERNET_REQUEST = 2;
    public static final int NETWORK_REQUEST = 3;

    private boolean readPermission = false;
    private boolean writePermission = false;
    private boolean internetPermission = false;
    private boolean networkPermission = false;

    private String[] phonePermissions;

    private Context mContext;
    private Activity mActivity;
    private ConnectivityManager connManager;
    private NetworkInfo netInfo;

    //inits variables and determines if permissions need to be set
    public PermissionUtils(Context context, Activity activity){
        mContext = context;
        mActivity = activity;

        //setup permissions array for later use
        phonePermissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE};

        //check if any permissions are set
        readPermission = (context.checkSelfPermission(phonePermissions[0]) == PackageManager.PERMISSION_GRANTED);
        writePermission = (context.checkSelfPermission(phonePermissions[1]) == PackageManager.PERMISSION_GRANTED);
        internetPermission = (context.checkSelfPermission(phonePermissions[2]) == PackageManager.PERMISSION_GRANTED);
        networkPermission = (context.checkSelfPermission(phonePermissions[3]) == PackageManager.PERMISSION_GRANTED);

        //set up internet listener to check when user loses connection or regains connection
        connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = connManager.getActiveNetworkInfo();
    }

    //checks if the user is able to connect to the internet
    public boolean CheckInternetStatus(){
        netInfo = connManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isAvailable() && netInfo.isConnected();
    }

    //returns if any permission is not accepted
    public boolean PermissionCheck(){
        return readPermission && writePermission && internetPermission && networkPermission;
    }

    //prompt user to select needed permissions if permission not enabled
    public void SelectPermissions(){
        //check for permissions and ask if permissions are needed
        int checkResultRead = mContext.checkSelfPermission(phonePermissions[0]);
        int checkResultWrite = mContext.checkSelfPermission(phonePermissions[1]);
        int checkResultInternet = mContext.checkSelfPermission(phonePermissions[2]);
        int checkResultNetwork = mContext.checkSelfPermission(phonePermissions[3]);

        //make sure if using multiple permissions to be selected individually
        if (checkResultRead != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mActivity, phonePermissions, READ_EXTERNAL_REQUEST); //requires permissions need array of permissions + request code
        }
        if (checkResultWrite != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mActivity, phonePermissions, WRITE_EXTERNAL_REQUEST); //requires permissions need array of permissions + request code
        }
        if (checkResultInternet != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mActivity, phonePermissions, INTERNET_REQUEST); //requires permissions need array of permissions + request code
        }
        if (checkResultNetwork != PackageManager.PERMISSION_GRANTED){
            requestPermissions(mActivity, phonePermissions, NETWORK_REQUEST); //requires permissions need array of permissions + request code
        }
    }

    //assign permissions based on granting status
    public void RequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case READ_EXTERNAL_REQUEST: {
                //check if permissions have been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Toast.makeText(mContext, "Permission granted", Toast.LENGTH_SHORT).show();
                    readPermission = true;
                } else {
                    // permission denied
                    Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case WRITE_EXTERNAL_REQUEST: {
                if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    //Toast.makeText(mContext, "Permission granted", Toast.LENGTH_SHORT).show();
                    writePermission = true;
                } else {
                    // permission denied
                    //Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case INTERNET_REQUEST: {
                if (grantResults.length > 0 && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    //Toast.makeText(mContext, "Permission granted", Toast.LENGTH_SHORT).show();
                    internetPermission = true;
                } else {
                    // permission denied
                    //Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            case NETWORK_REQUEST: {
                if (grantResults.length > 0 && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    //Toast.makeText(mContext, "Permission granted", Toast.LENGTH_SHORT).show();
                    networkPermission = true;
                } else {
                    // permission denied
                    //Toast.makeText(mContext, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
