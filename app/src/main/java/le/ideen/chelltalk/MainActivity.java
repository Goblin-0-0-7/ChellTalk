package le.ideen.chelltalk;

import android.Manifest;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Set;
import java.util.ArrayList;

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

    Button btn_enable, btn_disable, btn_scan;
    private BluetoothAdapter BTAdapter;
    private Set<BluetoothDevice> pairedDevices;
    ListView lv_pairedDevices;
    public final static String EXTRA_ADDRESS = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_enable = (Button) findViewById(R.id.button_enable);
        btn_disable = (Button) findViewById(R.id.button_disable);
        btn_scan = (Button) findViewById(R.id.button_scan);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        checkforbluetoothpermission();
        lv_pairedDevices = (ListView) findViewById(R.id.ListView_pairedDevices);
        if (BTAdapter.isEnabled()) {
            btn_scan.setVisibility(View.VISIBLE);
        }
    }

    public void checkforbluetoothpermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) +
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) +
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                                                                                     Manifest.permission.BLUETOOTH_ADVERTISE,
                                                                                     Manifest.permission.BLUETOOTH_SCAN}, 101);
            return;
        }
        return;
    }

    public void on(View v) {
        if (!BTAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            checkforbluetoothpermission();
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_SHORT).show();
        }
        btn_scan.setVisibility(View.VISIBLE);
        lv_pairedDevices.setVisibility(View.VISIBLE);
    }

    public void off(View v) {
        checkforbluetoothpermission();
        BTAdapter.disable();
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_SHORT).show();
        btn_scan.setVisibility(View.INVISIBLE);
        lv_pairedDevices.setVisibility(View.GONE);
    }

    public void deviceList(View v) {
        ArrayList deviceList = new ArrayList();
        checkforbluetoothpermission();
        pairedDevices = BTAdapter.getBondedDevices();

        if (pairedDevices.size() < 1) {
            Toast.makeText(getApplicationContext(), "No paired devices found", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice bt : pairedDevices) deviceList.add(bt.getName() + " " + bt.getAddress());
            Toast.makeText(getApplicationContext(), "Showing paired devices", Toast.LENGTH_SHORT).show();
            final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList);
            lv_pairedDevices.setAdapter(adapter);
            lv_pairedDevices.setOnItemClickListener(myListClickListener);
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
