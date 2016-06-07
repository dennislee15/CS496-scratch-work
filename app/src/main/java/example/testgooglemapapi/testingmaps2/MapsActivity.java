package example.testgooglemapapi.testingmaps2;

import android.*;
import android.Manifest;
import android.content.ContentProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import example.testgooglemapapi.testingmaps2.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private final static int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    Context context;
    EditText ed1;
    String dataLat;
    String dataLog;
    String concatData;
    private String file = "mydata";
    TextView tvLocInfo;
    boolean markerClicked;
    private String android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button btnRemove = (Button) (this.findViewById(R.id.removeLocation));
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeMarker(mMap);
            }
        });
        Button btnAdd = (Button) (this.findViewById(R.id.addLocation));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarker(mMap);
            }
        });
        Button btnHistory = (Button) (this.findViewById(R.id.showHistory));
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayHistory(mMap);
            }
        });
        /*GoogleMap.OnMarkerDragListener listener = new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                tvLocInfo.setText("Marker " + marker.getId() + " DragStart");
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                tvLocInfo.setText("Marker " + marker.getId() + " Drag@" + marker.getPosition());
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                tvLocInfo.setText("Marker " + marker.getId() + " DragEnd");
            }
        };
        mMap.setOnMarkerDragListener(listener);*/
        checkHistory();
        markerClicked = false;
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                try {
                    //The address corresponds to the "localhost" address for genymotion emulator,
                    //For the android studio emulator you must use  http://10.0.2.2:8888 instead
                    String mydate = "5";
                    String latitude = "123456";
                    String longitude = "98765";
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String android_id = tm.getDeviceId();
                    Server server = new Server(getApplicationContext(),android_id,mydate, latitude, longitude);
                    try{
                        server.send();
                    }catch (Exception E){
                        System.out.println(E);
                    }
                    /*String url = "http://10.0.3.2:8888/tutorialauth?op=showAllBooks";
                    return new HttpGet(url, "UTF-8").finish();*/
                    //The JSON string is returned to onPostExecute then passed to populateList

                } catch (final Exception e) {
                    // toastError(e);
                    e.printStackTrace();
                    return "";
                }
                return "Completed async task";
            }

            protected void onPostExecute(String result) {
                if (result != null && result.length() > 0 && result.startsWith("[")) {
                    Toast.makeText(getBaseContext(), "IT WAS SUCCESSFUL", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getBaseContext(), "GAWDDD", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void displayHistory(GoogleMap mmap){
        Toast.makeText(getBaseContext(), "Displaying History", Toast.LENGTH_SHORT).show();
    }

    public boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public void deleteInternalFile(){
        boolean deleted = deleteFile(file);
        if(deleted) {
            Toast.makeText(getBaseContext(), "Removed Location", Toast.LENGTH_SHORT).show();
        }
    }

    public void restoreMarker(String data){
        Toast.makeText(getBaseContext(), "Restoring Previous Marker Position", Toast.LENGTH_SHORT).show();
        String[] splitStringArray = data.split(" ");
        String stringLat = splitStringArray[0];
        String stringLog = splitStringArray[1];
        double lat = Double.parseDouble(stringLat);
        double log = Double.parseDouble(stringLog);

        LatLng user = new LatLng(lat,log);

        // Add previous location marker
        mMap.addMarker(new MarkerOptions()
                .position(user)
                .draggable(true)
                .title("You parked here!!"));

        // Set the type of terrain and move to location specified above
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                user, 16));

    }

    public void checkHistory(){
        if(fileExistance(file)) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
            dialog.setMessage("Restore Previous Location?");
            dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    try {
                        FileInputStream fin = openFileInput(file);
                        int c;
                        String temp = "";

                        while ((c = fin.read()) != -1) {
                            temp = temp + Character.toString((char) c);
                        }
                        String totalData = temp;
                        restoreMarker(totalData);
                    } catch (Exception e) {
                    }
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    deleteInternalFile();
                }
            });
            dialog.show();
        }
        else{
            try {
                FileOutputStream fOut = openFileOutput(file, MODE_APPEND);
                fOut.write(concatData.getBytes());
                fOut.close();
                Toast.makeText(getBaseContext(), "file saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //Toast.makeText(getBaseContext(), "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }

    public void storeLocation(double lat, double log){
        String latitude = String.valueOf(lat);
        String longitude = String.valueOf(log);
        // get date --------------------------------------------
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        System.out.println(formattedDate);
        // formattedDate now holds the date
        Server server = new Server(this,android_id,formattedDate, latitude, longitude);
        try{
            server.send();
        }catch (Exception E){
            System.out.println(E);
        }

        dataLat=latitude;
        dataLog=longitude;

        concatData = dataLat.concat(" ").concat(dataLog);

        if(fileExistance(file)) {
            try {
                FileOutputStream fOut = openFileOutput(file, MODE_APPEND);
                fOut.write(concatData.getBytes());
                fOut.close();
                Toast.makeText(getBaseContext(), "Location Saved", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        else{
            //Toast.makeText(getBaseContext(), "File does not exist", Toast.LENGTH_SHORT).show();
        }
    }


    public void removeMarker(GoogleMap googlemap) {
        mMap.clear();
        deleteInternalFile();
    }

    public void addMarker(GoogleMap googleMap) {

        LatLng user = mMap.getCameraPosition().target;
        double lat = user.latitude;
        double log = user.longitude;

        mMap = googleMap;
        // Add a marker somewhere at current location
        mMap.addMarker(new MarkerOptions()
                //.icon(BitmapDescriptorFactory.fromResource(R.drawable.parking))
                .position(user)
                .draggable(true)
                .title("You parked here!!"));

        // Set the type of terrain and move to location specified above
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                user, 16));

        // STORE LOCATION TO INTERNAL FILE
        storeLocation(lat,log);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*mMap = googleMap;
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
        }
        context = getBaseContext();
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gps_enabled){
            AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);

            dialog.setMessage("GPS NOT ENABLED");
            dialog.setPositiveButton("OPEN LOCATION SETTINGS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
        else{
            System.out.println("GPS IS ENABLED");
        }*/
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }
}