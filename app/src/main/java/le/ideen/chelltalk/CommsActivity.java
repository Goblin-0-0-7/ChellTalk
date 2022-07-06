package le.ideen.chelltalk;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class CommsActivity extends AppCompatActivity {

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "CommsActivity";
    BluetoothSocket BTSocket;
    BluetoothDevice BTDevice;

    public class ConnectThread extends Thread {

        private ConnectThread(BluetoothDevice device) throws IOException {
            BluetoothSocket tmpSocket = null;
            BTDevice = device;
            checkforbluetoothpermission();
            try {
                UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                tmpSocket = BTDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            BTSocket = tmpSocket;
            BTAdapter.cancelDiscovery();
            try {
                BTSocket.connect();
            } catch (IOException connectException) {
                Log.v(TAG, "Connection exception!");
                try {
                    BTSocket.close();
                } catch (IOException closeException) {
                    Log.v(TAG, "Close Exception!");
                }
            }
            send();
        }

        public void send() throws IOException {
            String msg = "new string";
            OutputStream MSGOutputStream = BTSocket.getOutputStream();
            MSGOutputStream.write(msg.getBytes());
            receive();
        }

        public void receive() throws IOException {
            InputStream MSGInputStream = BTSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            try {
                bytes = MSGInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "Received: " + readMessage);
                TextView tv_Answer = (TextView) findViewById(R.id.TextView_Answer);
                tv_Answer.setText("Answer was: " + readMessage);
                BTSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Problems occurred!");
                return;
            }
        }
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comms);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent intent = getIntent();
        final String address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        Button btn_Send = (Button) findViewById(R.id.button_Send);

        btn_Send.setOnClickListener(v -> {
            final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
            try {
                new ConnectThread(device).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

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
            BTSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
