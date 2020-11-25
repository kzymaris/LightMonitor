package com.example.lightmonitor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
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
import java.io.UnsupportedEncodingException;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

class SavedValues{
    public int[] selectedColor = {0,0,0};
    public int[] color1 = {0,0,0};
    public int[] color2 = {255,255,255};
    public int speed = 0;
    SavedValues(){

    }
}

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "LightMonitor";
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
    public View back;
    public Slider slider;
    public SavedValues colors;
    public int selectMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        color = findViewById(R.id.colorPickerView);
        layout = findViewById(R.id.layout);
        back = findViewById(R.id.back);
        box1 = findViewById(R.id.color1);
        box2 = findViewById(R.id.color2);
        border1 = findViewById(R.id.border1);
        border2 = findViewById(R.id.border2);
        slider = findViewById(R.id.speed);
        colors = new SavedValues();


        FileReader fr = null;
        try {
            File f = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            fr = new FileReader(f);
            colors = new Gson().fromJson(fr, SavedValues.class);
            slider.setValue(colors.speed > slider.getValueFrom() ? colors.speed : slider.getValueFrom());
            box1.setCardBackgroundColor((255 & 0xff) << 24 | (colors.color1[0] & 0xff) << 16 | (colors.color1[1] & 0xff) << 8 | (colors.color1[2] & 0xff));
            box2.setCardBackgroundColor((255 & 0xff) << 24 | (colors.color2[0] & 0xff) << 16 | (colors.color2[1] & 0xff) << 8 | (colors.color2[2] & 0xff));
            Log.i(TAG, "Color: "+ colors.selectedColor);
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
                    back.setBackgroundColor(envelope.getColor());
                    colors.selectedColor[0] = envelope.getArgb()[1];
                    colors.selectedColor[1] = envelope.getArgb()[2];
                    colors.selectedColor[2] = envelope.getArgb()[3];
                    if(fromUser){
                        sendRequest(color.getContext(), "wipe", false);
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
            return;
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

    public void rainbow(View view){
        sendRequest(view.getContext(), "rainbow", false);
    }

    public void doFade(View view){
        sendRequest(view.getContext(), "fade", true);
    }

    public void snake(View view){
        sendRequest(view.getContext(), "snake", true);
    }

    public void turnOff(View view){
        colors.selectedColor = new int[]{0, 0, 0};
        sendRequest(view.getContext(), "wipe", false);
    }


    public void sendRequest(Context c, String type, Boolean twoColors){

        hideBorders();
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(c);
        String url ="http://108.20.217.88:301/";

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

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

        };

// Add the request to the RequestQueue.
        Log.i(TAG, "Body is: " + mRequestBody);
        queue.add(stringRequest);
    }


    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Initiate request for permissions.
    private boolean checkPermissions() {

        if (!isExternalStorageWritable()) {
            Toast.makeText(this, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
            return false;
        } else {
            return true;
        }
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                    Toast.makeText(this, "External storage permission granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


}
