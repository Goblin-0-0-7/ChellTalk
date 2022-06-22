package le.ideen.chelltalk;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
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

    //public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    //public static BluetoothAdapter getDefaultAdapter();

    //public Context context = getContext();
    public BluetoothManager BTManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
    public BluetoothAdapter BTAdapter = BTManager.getAdapter();
    private static final String TAG = "CommsActivity";
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    public class ConnectThread extends Thread {
        private ConnectThread(BluetoothDevice device) throws IOException {
            /*if (mmSocket != null) {
                if(mmSocket.isConnected()) {
                    send();
                }
            }*/
            BluetoothSocket tmp = null;
            mmDevice = device;
            try {
                UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            if (ActivityCompat.checkSelfPermission(CommsActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                askforbluetoothpermission();
                return;
            }
            BTAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Log.v(TAG, "Connection exception!");
                try {
                    mmSocket.close();
                    /*mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
                    mmSocket.connect();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } */
                } catch (IOException closeException) {

                }
            }
            send();
        }

        public void askforbluetoothpermission() {
            if (ContextCompat.checkSelfPermission(CommsActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(CommsActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);
                    ActivityCompat.requestPermissions(CommsActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, 2);
                    ActivityCompat.requestPermissions(CommsActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2);
                    return;
                }
            }
        }

        public void send() throws IOException {
            String msg = "voltage";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());
            receive();
        }

        public void receive() throws IOException {
            InputStream mmInputStream = mmSocket.getInputStream();
            byte[] buffer = new byte[256];
            int bytes;

            try {
                bytes = mmInputStream.read(buffer);
                String readMessage = new String(buffer, 0, bytes);
                Log.d(TAG, "Received: " + readMessage);
                TextView voltageLevel = (TextView) findViewById(R.id.Voltage);
                voltageLevel.setText("Voltage level\n" +"DC " + readMessage + " V");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Problems occurred!");
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comms);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final Intent intent = getIntent();
        final String address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        Button voltButton = (Button) findViewById(R.id.measVoltage);

        voltButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final BluetoothDevice device = BTAdapter.getRemoteDevice(address);
                try {
                    new ConnectThread(device).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
