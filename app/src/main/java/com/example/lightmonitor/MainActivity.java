package com.example.lightmonitor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.slider.Slider;
import com.google.gson.Gson;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

class SavedValues{
    int[] selectedColor = {0,0,0};
    int[] color1 = {0,0,0};
    int[] color2 = {255,255,255};
    int speed = 0;
    SavedValues(){

    }
}

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LightMonitor";
    private Context c;
    private boolean permissionGranted;
    private static final int REQUEST_PERMISSION_WRITE = 1001;
    private static final String FILE_NAME = "exercises.json";
    public ColorPickerView color;
    public ConstraintLayout layout;
    public BrightnessSlideBar brightnessSlideBar;
    public CardView box1;
    public CardView box2;
    public CardView border1;
    public CardView border2;
    public Slider slider;
    public SavedValues colors;
    public int selectMode = 0;

    public EffectsAdapter adapter;
    public ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = this;
        color = findViewById(R.id.colorPickerView);
        layout = findViewById(R.id.layout);
        box1 = findViewById(R.id.color1);
        box2 = findViewById(R.id.color2);
        border1 = findViewById(R.id.border1);
        border2 = findViewById(R.id.border2);
        slider = findViewById(R.id.speed);
        colors = new SavedValues();

        adapter = new EffectsAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        FileReader fr = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            fr = new FileReader(f);
            colors = new Gson().fromJson(fr, SavedValues.class);
            slider.setValue(colors.speed > slider.getValueFrom() ? colors.speed : slider.getValueFrom());
            box1.setCardBackgroundColor((0xff) << 24 | (colors.color1[0] & 0xff) << 16 | (colors.color1[1] & 0xff) << 8 | (colors.color1[2] & 0xff));
            box2.setCardBackgroundColor((0xff) << 24 | (colors.color2[0] & 0xff) << 16 | (colors.color2[1] & 0xff) << 8 | (colors.color2[2] & 0xff));
            Log.i(TAG, "Color: "+ Arrays.toString(colors.selectedColor));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(fr != null){
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        brightnessSlideBar = findViewById(R.id.brightnessSlide);
        color.attachBrightnessSlider(brightnessSlideBar);

        color.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                Log.i(TAG, "Color: "+ envelope.getColor());
                Log.i(TAG, "Color: "+ envelope.getHexCode());
                for(int i = 0 ; i < envelope.getArgb().length ; i++){
                    Log.i(TAG, "Color: "+ envelope.getArgb()[i]);
                }

                if(selectMode == 0){
                    pager.setBackgroundColor(envelope.getColor());
                    colors.selectedColor[0] = envelope.getArgb()[1];
                    colors.selectedColor[1] = envelope.getArgb()[2];
                    colors.selectedColor[2] = envelope.getArgb()[3];
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(c);
                    boolean changeOnSelect = sharedPreferences.getBoolean ("changeOnSelect", true);
                    if(fromUser && changeOnSelect){
                        sendRequest(color.getContext(), "solid", false);
                    }
                }else if(selectMode == 1){
                    box1.setCardBackgroundColor(envelope.getColor());
                    colors.color1[0] = envelope.getArgb()[1];
                    colors.color1[1] = envelope.getArgb()[2];
                    colors.color1[2] = envelope.getArgb()[3];
                }else if(selectMode == 2){
                    box2.setCardBackgroundColor(envelope.getColor());
                    colors.color2[0] = envelope.getArgb()[1];
                    colors.color2[1] = envelope.getArgb()[2];
                    colors.color2[2] = envelope.getArgb()[3];
                }
            }
        });
        box1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "box1");
                selectMode = 1;
                border1.setVisibility(VISIBLE);
                border2.setVisibility(INVISIBLE);
            }
        });
        box2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "box2");
                selectMode = 2;
                border2.setVisibility(VISIBLE);
                border1.setVisibility(INVISIBLE);
            }
        });
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "background");
                hideBorders();
            }
        });
        slider.addOnChangeListener(new Slider.OnChangeListener(){

            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                colors.speed = (int) value;
                sendRequest(slider.getContext(), "speed", false);
            }
        });

        color.setPreferenceName("MyColorPicker");

        if(!permissionGranted){
            checkPermissions();
        }
    }



    public void hideBorders(){
        selectMode = 0;
        border1.setVisibility(INVISIBLE);
        border2.setVisibility(INVISIBLE);
    }

    @Override
    protected void onStop() {
        FileOutputStream fos = null;
        File f = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
        try {
            fos = new FileOutputStream(f);
            String s = new Gson().toJson(colors);
            Log.d("debug", "JSON: " + s);
            fos.write(s.getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(fos != null){
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        ColorPickerPreferenceManager.getInstance(layout.getContext()).saveColorPickerData(color);
        super.onStop();
    }

    public void sendRequest(Context c, String type, Boolean twoColors){

        hideBorders();
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(c);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(c);
        String ip = sharedPreferences.getString ("ip", "");
        String port = sharedPreferences.getString ("port", "");
        String offset = sharedPreferences.getString ("offset", "0");
        String url ="http://" + ip + ":" + port + "/";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("t", type);
            jsonBody.put("s", Integer.toString(colors.speed));
            if(twoColors){
                jsonBody.put("r1", Integer.toString(colors.color1[0]));
                jsonBody.put("g1", Integer.toString(colors.color1[1]));
                jsonBody.put("b1", Integer.toString(colors.color1[2]));
                jsonBody.put("r2", Integer.toString(colors.color2[0]));
                jsonBody.put("g2", Integer.toString(colors.color2[1]));
                jsonBody.put("b2", Integer.toString(colors.color2[2]));
            }else{
                jsonBody.put("r1", Integer.toString(colors.selectedColor[0]));
                jsonBody.put("g1", Integer.toString(colors.selectedColor[1]));
                jsonBody.put("b1", Integer.toString(colors.selectedColor[2]));
                jsonBody.put("off", offset);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String mRequestBody = jsonBody.toString();

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        int max = response.length() > 500 ? 500 : response.length();
                        String res = response.substring(0,max);
                        Log.i(TAG, "Response is: "+ res);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i(TAG, "Error: "+error.getMessage());
            }
        }){
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return mRequestBody.getBytes(StandardCharsets.UTF_8);
            }

        };

// Add the request to the RequestQueue.
        Log.i(TAG, "Body is: " + mRequestBody);
        queue.add(stringRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Initiate request for permissions.
    private void checkPermissions() {

        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
        }
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_WRITE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true;
                Toast.makeText(this, "External storage permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
