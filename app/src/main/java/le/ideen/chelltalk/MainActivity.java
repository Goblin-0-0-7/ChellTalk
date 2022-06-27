package le.ideen.chelltalk;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Set;
import java.util.ArrayList;
import java.util.UUID;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.content.Intent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class MainActivity extends AppCompatActivity {

    Button enablebt, disablebt, scanbt;
    BluetoothSocket btSocket = null;
    private BluetoothAdapter BTAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv;
    public final static String EXTRA_ADDRESS = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enablebt = (Button) findViewById(R.id.button_enablebt); //Umbennenen in btn_
        disablebt = (Button) findViewById(R.id.button_disablebt);
        scanbt = (Button) findViewById(R.id.button_scanbt);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        askforbluetoothpermission();
        lv = (ListView) findViewById(R.id.listView);
        if (BTAdapter.isEnabled()) {
            scanbt.setVisibility(View.VISIBLE);
        }
    }

    public void askforbluetoothpermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            System.out.println("here");
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_SCAN}, 101);
            return;
        }
    }

    public void on(View v) {
        if (!BTAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                askforbluetoothpermission();
                return;
            }
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
        }
        scanbt.setVisibility(View.VISIBLE);
        lv.setVisibility(View.VISIBLE);
    }

    public void off(View v) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            askforbluetoothpermission();
            return;
        }
        BTAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_SHORT).show();
        scanbt.setVisibility(View.INVISIBLE);
        lv.setVisibility(View.GONE);
    }

    public void deviceList(View v) {
        ArrayList deviceList = new ArrayList();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            askforbluetoothpermission();
            return;
        }
        pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices.size() < 1) {
            Toast.makeText(getApplicationContext(), "No paired devices found", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice bt : pairedDevices) deviceList.add(bt.getName() + " " + bt.getAddress());
            Toast.makeText(getApplicationContext(), "Showing paired devices", Toast.LENGTH_SHORT).show();
            final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(myListClickListener);
        }
    }
    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, CommsActivity.class);
            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        }
    };
}
