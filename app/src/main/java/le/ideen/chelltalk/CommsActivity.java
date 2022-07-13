package le.ideen.chelltalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zerokol.views.JoystickView;
import com.zerokol.views.JoystickView.OnJoystickMoveListener;



public class CommsActivity extends AppCompatActivity {

    EditText et_KeyboardInput;
    public BluetoothAdapter mmBTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "CommsActivity";
    private BluetoothSocket mmBTSocket;
    private BluetoothDevice mmBTDevice;
    private final Handler msgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            handleThatShitUwU(msg);
        };
    };

    private ConnectThread BTThread;

    //doppelt, auch schon in MainActivity
    public void checkforbluetoothpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) +
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) +
                ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(CommsActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,
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

    public void sendMouseInput(){

    }

    public void sendKeyboardInput(View v){
        String keyboardInput = et_KeyboardInput.getText().toString();
        try{
            BTThread.write(keyboardInput.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRightClick(View v){
        String msg = "rightclick";
        try{
            BTThread.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendLeftClick(View v){
        String msg = "leftclick";
        try{
            BTThread.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final LayoutInflater factory = getLayoutInflater();

        final View textEntryView = factory.inflate(R.layout.activity_comms, null);

        et_KeyboardInput = (EditText) textEntryView.findViewById(R.id.editText_KeyboardInput);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comms);

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
