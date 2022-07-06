package le.ideen.chelltalk;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectThread extends Thread {

    public BluetoothAdapter BTAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String TAG = "ConnectThread";
    BluetoothSocket BTSocket;
    BluetoothDevice BTDevice;

    public ConnectThread(BluetoothDevice device) throws IOException {
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
        byte[] buffer = new byte[256];
        int bytes;

        try {
            bytes = MSGInputStream.read(buffer);
            String readMessage = new String(buffer, 0, bytes);
            Log.d(TAG, "Received: " + readMessage);
            BTSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Problems occurred!");
            return;
        }
    }

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
}
