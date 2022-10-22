package le.ideen.chelltalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

public class PiActivity extends AppCompatActivity {

    SeekBar sb_red, sb_green, sb_blue;
    private int[] rgb_value = {0,0,0};
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

    public void sendColorCode(){
        String red_value = Integer.toString(rgb_value[0]);
        String green_value = Integer.toString(rgb_value[1]);
        String blue_value = Integer.toString(rgb_value[2]);
        String msg = red_value + "," + green_value + "," + blue_value;
        try{
            BTThread.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final LayoutInflater factory = getLayoutInflater();
        final View piView = factory.inflate(R.layout.activity_pi, null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi);

        sb_red = (SeekBar) findViewById(R.id.seekBar_red);
        sb_green = (SeekBar) findViewById(R.id.seekBar_green);
        sb_blue = (SeekBar) findViewById(R.id.seekBar_blue);
        sb_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                rgb_value[0] = sb_red.getProgress();
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                rgb_value[1] = sb_green.getProgress();
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                rgb_value[2] = sb_blue.getProgress();
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