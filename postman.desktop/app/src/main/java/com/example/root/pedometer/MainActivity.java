package com.example.root.pedometer;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends Activity implements SensorEventListener {

    private final int NUMBER_OF_STEPS_TRACKED = 35;//1000;
    private final int ABNORNAL_THRESHOLD = 150; //13

    RequestQueue queue;
    // Tag used to log messages
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView statusView;
    private TextView readingsView;

    private long pastTime = -1;
    private LinkedBlockingQueue<String> msPerStep = new LinkedBlockingQueue<>();

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusView = (TextView) findViewById(R.id.status);
        readingsView = (TextView) findViewById(R.id.readings);

        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager
                .getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Setup Volley networking request
        queue = Volley.newRequestQueue(this); // Need to set up a queue that holds all Volley requests
        String url = "http://www.reddit.com/r/pics.json"; // The url we are getting data from

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.toString(), error);
                    }
                });

        // Add the request to the Volley request queue
        queue.add(request);
    }

    @Override
    protected void onResume() {

        super.onResume();

        mSensorManager.registerListener(this, mStepCounterSensor,

                SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor,

                SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this, mStepCounterSensor);
        mSensorManager.unregisterListener(this, mStepDetectorSensor);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        int abnormals = 0;

        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            long nowTime = System.currentTimeMillis();
            if (pastTime != -1) {
                if (msPerStep.size() >= NUMBER_OF_STEPS_TRACKED + 1) {
                    String[] msPerStepArray = msPerStep.toArray(new String[NUMBER_OF_STEPS_TRACKED + 1]);
                    for (int i = 0; i < msPerStepArray.length; i++) {
                        if (i < msPerStepArray.length - 1) {
                            Long dx_msPerStep = Math.abs(Long.valueOf(msPerStepArray[i]) - Long.valueOf(msPerStepArray[i + 1]));
                            if (dx_msPerStep > ABNORNAL_THRESHOLD) {
                                abnormals++;
                            }
                        }
                    }
                    msPerStep.poll();
                }
                long timeDifference = nowTime - pastTime;
                msPerStep.offer(String.valueOf(timeDifference));

                if (msPerStep.size() < NUMBER_OF_STEPS_TRACKED) {
                    int difference = NUMBER_OF_STEPS_TRACKED - msPerStep.size();
                    statusView.setText("Needs " + difference + " more steps!");
                } else {
                    statusView.setText(abnormals + " abnormals in " + NUMBER_OF_STEPS_TRACKED + " steps");
//                    if (abnormals > 13) {
//                        // SEND POST REQUEST
//                    }
                }

                String print = "";
                String[] msPerStepArray = msPerStep.toArray(new String[NUMBER_OF_STEPS_TRACKED + 1]);
                for (int i = msPerStep.size() - 1; i >= 0; i--) {
                    print += msPerStepArray[i] + '\n';
                }
                readingsView.setText(print);
//                textView.setText(textView.getText().toString() + '\n' + timeDifference);

            }
            pastTime = nowTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void sendPostRequest() {
        // Setup Volley networking request

    }
}
