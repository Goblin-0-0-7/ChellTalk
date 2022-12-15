package le.ideen.chelltalk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.Console;
import java.util.Calendar;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PiActivity extends AppCompatActivity {

    SeekBar sb_red, sb_green, sb_blue, sb_fade_speed;
    EditText et_red_value, et_green_value, et_blue_value, et_alarm_title;
    Button bt_fade;
    TimePicker timePicker;
    CheckBox cb_monday, cb_tuesday, cb_wednesday, cb_thursday, cb_friday, cb_saturday, cb_sunday;
    //defaults
    private int[] rgb_value = {0,0,0};
    private boolean fade_status = false;
    private int fade_speed_max = 200;
    private int fade_speed_min = 1;
    private String screen_page = "RGB";

    private String alarm_title = "";

    private CheckBox[] cb_weekdays = new CheckBox[7];

    public BluetoothAdapter mmBTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "PiActivity";
    private BluetoothSocket mmBTSocket;
    private BluetoothDevice mmBTDevice;
    private final Handler msgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            handleThatShitUwU(msg);
        };
    };

    private ConnectThread BTThread;

    public void checkforbluetoothpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) +
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) +
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(PiActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_SCAN}, 101);
            return;
        }
        return;
    }

    public BluetoothSocket createConnection(BluetoothDevice device){
        BluetoothSocket tmpSocket = null;
        mmBTDevice = device;
        checkforbluetoothpermission();
        try {
            UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
            tmpSocket = mmBTDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmBTSocket = tmpSocket;
        mmBTAdapter.cancelDiscovery();
        try {
            mmBTSocket.connect();
        } catch (IOException connectException) {
            Log.v(TAG, "Connection exception!");
            try {
                mmBTSocket.close();
            } catch (IOException closeException) {
                Log.v(TAG, "Close Exception!");
            }
        }
        return mmBTSocket;
    }

    public void handleThatShitUwU(Message msg){
        switch (msg.what) {
            case 0: //MESSAGE_READ
                String receivedMSG = new String((byte[]) msg.obj, 0, msg.arg1);
                Toast.makeText(getApplicationContext(), receivedMSG, Toast.LENGTH_LONG).show();

                break;

            case 1: //MESSAGE_WRITE
                String writtenMSG = msg.toString();
                System.out.println(writtenMSG);

                break;

            case 2: //MESSAGE_TOAST

                break;
        }
    }

    public void connectToServer(View v){
        try {
            BluetoothSocket socket = createConnection(mmBTDevice);
            BTThread = new ConnectThread(socket, msgHandler);
            BTThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg){
        try{
            BTThread.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendColorCode(){
        String red_value = Integer.toString(rgb_value[0]);
        String green_value = Integer.toString(rgb_value[1]);
        String blue_value = Integer.toString(rgb_value[2]);
        String msg = "-rgbCode-" + red_value + "," + green_value + "," + blue_value + ","; //the "," at the end is for separation if two msg get sent together
        sendMsg(msg);
    }

    public void sendFade(View v){
        fade_status = !fade_status;
        if(fade_status){
            sendMsg("-fade-");
            bt_fade.setText("End Fade");
            sb_fade_speed.setVisibility(View.VISIBLE);
        }
        else{
            sendMsg("-black-");
            bt_fade.setText("Start Fade");
            sb_fade_speed.setVisibility(View.INVISIBLE);
        }
    }

    public void sendFadeSpeed(int speed){
        String msg = "-fadeSpeed-" + speed;
        sendMsg(msg);
    }

    public void switchScreens(View v){
        if (screen_page == "RGB"){
            screen_page = "AlarmClock";
            setScreen();
        }
        else if (screen_page == "AlarmClock"){
            screen_page = "RGB";
            setScreen();
        }
    }

    public void setScreen(){
        switch (screen_page){
            case "RGB":
                setContentView(R.layout.activity_pi);
                break;
            case "AlarmClock":
                setContentView(R.layout.pi_clock_layout);
                break;
        }
        findViews();
    }

    public void findViews(){
        switch (screen_page){
            case "RGB":
                bt_fade = (Button) findViewById(R.id.button_fade);
                sb_fade_speed = (SeekBar) findViewById(R.id.seekBar_fade_speed);

                et_red_value = (EditText) findViewById(R.id.editTextNumber_red_value);
                et_green_value = (EditText) findViewById(R.id.editTextNumber_green_value);
                et_blue_value = (EditText) findViewById(R.id.editTextNumber_blue_value);

                //set defaults
                sb_fade_speed.setMax(fade_speed_max);
                sb_fade_speed.setMin(fade_speed_min);
                sb_fade_speed.setProgress(fade_speed_max/2);

                //editText Listeners on Done
                et_red_value.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et_red_value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            try {
                                if (Integer.parseInt(et_red_value.getText().toString()) > 255) {
                                    sb_red.setProgress(255);
                                } else {
                                    sb_red.setProgress(Integer.parseInt(et_red_value.getText().toString()));
                                }
                            }
                            catch (NumberFormatException e){
                                sb_red.setProgress(0);
                            }
                            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                });

                et_green_value.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et_green_value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            try {
                                if (Integer.parseInt(et_green_value.getText().toString()) > 255) {
                                    sb_green.setProgress(255);
                                } else {
                                    sb_green.setProgress(Integer.parseInt(et_green_value.getText().toString()));
                                }
                            }
                            catch (NumberFormatException e){
                                sb_green.setProgress(0);
                            }
                            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                });

                et_blue_value.setImeOptions(EditorInfo.IME_ACTION_DONE);
                et_blue_value.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            try {
                                if (Integer.parseInt(et_blue_value.getText().toString()) > 255) {
                                    sb_blue.setProgress(255);
                                } else {
                                    sb_blue.setProgress(Integer.parseInt(et_blue_value.getText().toString()));
                                }
                            }
                            catch (NumberFormatException e){
                                sb_blue.setProgress(0);
                            }
                            InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                            return true;
                        }
                        return false;
                    }
                });

                sb_red = (SeekBar) findViewById(R.id.seekBar_red);
                sb_green = (SeekBar) findViewById(R.id.seekBar_green);
                sb_blue = (SeekBar) findViewById(R.id.seekBar_blue);
                //seekBar listeners
                sb_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        rgb_value[0] = sb_red.getProgress();
                        et_red_value.setText(Integer.toString(rgb_value[0]));
                        sendColorCode();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });
                sb_green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        rgb_value[1] = sb_green.getProgress();
                        et_green_value.setText(Integer.toString(rgb_value[1]));
                        sendColorCode();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });
                sb_blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        rgb_value[2] = sb_blue.getProgress();
                        et_blue_value.setText(Integer.toString(rgb_value[2]));
                        sendColorCode();
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });
                sb_fade_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int fadeSpeed = sb_fade_speed.getProgress();
                        sendFadeSpeed(fadeSpeed);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }
                });
                break;
            case "AlarmClock":
                et_alarm_title = (EditText) findViewById(R.id.editText_alarmTitle);

                cb_monday = (CheckBox) findViewById(R.id.checkBox_monday);
                cb_tuesday = (CheckBox) findViewById(R.id.checkBox_tuesday);
                cb_wednesday = (CheckBox) findViewById(R.id.checkBox_wednesday);
                cb_thursday = (CheckBox) findViewById(R.id.checkBox_thursday);
                cb_friday = (CheckBox) findViewById(R.id.checkBox_friday);
                cb_saturday = (CheckBox) findViewById(R.id.checkBox_saturday);
                cb_sunday = (CheckBox) findViewById(R.id.checkBox_sunday);
                cb_weekdays[0] = cb_monday;
                cb_weekdays[1] = cb_tuesday;
                cb_weekdays[2] = cb_wednesday;
                cb_weekdays[3] = cb_thursday;
                cb_weekdays[4] = cb_friday;
                cb_weekdays[5] = cb_saturday;
                cb_weekdays[6] = cb_sunday;

                timePicker = (TimePicker) findViewById(R.id.timePicker);

                timePicker.setIs24HourView(true);
                timePicker.setCurrentHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                break;
        }
    }

    public void newAlarm(View v){
        String alarm_title = et_alarm_title.getText().toString();
        System.out.println("alarmTitle is " + alarm_title);
        String hour = Integer.toString(timePicker.getHour());
        String min = Integer.toString(timePicker.getMinute());
        String sec = "0";
        String repetition = "";
        for (int i = 0; i < 7; i++){
            if (cb_weekdays[i].isChecked()){
                repetition += "1,";
            }
            else {
                repetition += "0,";
            }
            cb_weekdays[i].setChecked(false);
        }
        repetition += "";

        if (alarm_title == ""){
            System.out.println("title empty");
            alarm_title = hour + "-" + min + "-" + sec;
        }
        String alarm_time = alarm_title + "," + hour + "," + min + "," + sec + "," + repetition + ",";
        String msg = "-newAlarm-" + alarm_time;
        sendMsg(msg);
    }

    public void newTestAlarm(View v){
        sendMsg("-newTestAlarm-");
    }

    public void stopAlarm(View v){
        sendMsg("-stopAlarm-");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final LayoutInflater factory = getLayoutInflater();
        final View rgbView = factory.inflate(R.layout.activity_pi, null);
        final View clockView = factory.inflate(R.layout.pi_clock_layout, null);

        super.onCreate(savedInstanceState);
        setScreen();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent intent = getIntent();
        final String address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        mmBTDevice = mmBTAdapter.getRemoteDevice(address);

        connectToServer(null);

        if (!mBluetoothAdapter.isEnabled()) {
            checkforbluetoothpermission();
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            mmBTSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}