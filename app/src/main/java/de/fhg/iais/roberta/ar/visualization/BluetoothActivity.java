package de.fhg.iais.roberta.ar.visualization;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import de.fhg.iais.roberta.ar.R;

public class BluetoothActivity extends AppCompatActivity {

    private Button buttonStartScan = null;
    private Button buttonStopScan = null;
    private ListView bluetoothListview = null;
    private TextView bluetoothText = null;
    private TextView foundDevices = null;
    private long currTime = 0L;
    private TextView textViewScanning = null;
    private final List<BluetoothDevice> deviceList = new ArrayList<>();
    private final List<String> listElementsArrayList = new ArrayList<>();

    // Bluetooth
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private final BLEService leService = new BLEService();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_bluetooth);

        this.buttonStartScan = this.findViewById(R.id.button_startScan);
        this.buttonStopScan = this.findViewById(R.id.button_stopScan);
        this.bluetoothListview = this.findViewById(R.id.bluetooth_listview);
        this.textViewScanning = this.findViewById(R.id.textView_scanningDevices);
        this.bluetoothText = this.findViewById(R.id.tutorial_text);
        this.foundDevices = this.findViewById(R.id.text_found_devices);

        // Größe des Popups
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        this.getWindow().setLayout((int) (width * 0.8f), (int) (height * 0.65f));

        // Position des Popups
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -1 * (int) (height * 0.07f);
        this.getWindow().setAttributes(params);

        this.checkBluetoothAndLocationEnabled();
        if ( this.leService.btAdapter != null && this.leService.btAdapter.isEnabled() ) {
            this.startScanning();
        }

        this.buttonStartScan.setOnClickListener(v -> {
            // STARTE BLUETOOTH SCAN
            this.deviceList.clear();
            this.listElementsArrayList.clear();
            this.bluetoothListview.removeAllViewsInLayout();
            this.startScanning();
        });

        this.buttonStopScan.setOnClickListener(v -> {
            // STOPPE BLUETOOTH SCAN
            this.stopScanning();
        });

        this.bluetoothListview.setOnItemClickListener((adapterView, view, i, l) -> {
            // VERBINDUNG ZU GERÄT IN DER LISTE HERSTELLEN

            BluetoothDevice device = this.deviceList.get(i);

            this.buttonStartScan.setVisibility(View.VISIBLE);
            this.buttonStopScan.setVisibility(View.INVISIBLE);
            this.textViewScanning.setVisibility(View.INVISIBLE);
            this.stopScanning();
            this.leService.connectToDeviceSelected(device);

            long startTime = System.currentTimeMillis();
            long currentTime = System.currentTimeMillis();
            boolean connected = false;

            // VERSUCHT 2 SEKUNDEN LANG EINE VERBINDUNG HERZUSTELLEN
            while ( currentTime < startTime + 2000 ) {
                if ( BLEService.getGlobalGatt() != null ) {
                    Toast.makeText(this, "Verbindung mit " + this.deviceList.get(i).getName() + " hergestellt", Toast.LENGTH_LONG).show();
                    connected = true;
                    this.finish();
                    break;
                }
                currentTime = System.currentTimeMillis();
            }

            if ( !connected ) {
                Toast.makeText(this, "Verbindung mit " + this.deviceList.get(i).getName() + " konnte nicht hergestellt werden", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Fügt den Namen und die MAC-Adresse von einem gefundenen Bluetooth-Gerät in eine Liste von
     * Strings ein, welche dann in der ListView und somit in der App angezeigt wird. Fälle, wo ein
     * Bluetooth-Gerät keinen Namen hat werden abgefangen und mit "NULL" angezeigt.
     *
     * @param device Ein gefundenes Bluetooth-Gerät
     */
    public void addToListView(BluetoothDevice device) {
        Log.i("addToListView", "nr devices: " + this.deviceList.size());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.listview_entry, this.listElementsArrayList);
        this.bluetoothListview.setAdapter(adapter);

        if ( device != null ) {
            if ( device.getName() != null && !device.getName().isEmpty() ) {
                this.listElementsArrayList.add(device.getName() + " - " + device.getAddress());
            } else {
                this.listElementsArrayList.add("NULL" + " - " + device.getAddress());
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * onScanResult wird automatisch aufgerufen, wenn ein Bluetooth-Gerät in Reichweite gefunden
     * wurde. Das Gerät wird an die "addToListView" Methode übergeben und somit in der App angezeigt.
     */
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("onScanResult", "Remote device name: " + result.getDevice().getName() + " Mac: " + result.getDevice().getAddress());
            if ( !BluetoothActivity.this.deviceList.contains(result.getDevice()) && result.getDevice().getName() != null && !result.getDevice()
                                                                                                                                   .getName()
                                                                                                                                   .isEmpty() ) {
                if ( result.getDevice().getName().contains("Calliope mini") ) {
                    BluetoothActivity.this.deviceList.add(result.getDevice());
                    BluetoothActivity.this.addToListView(result.getDevice());
                }
            }
            //System.out.println("curr: " + currTime + " now: " + System.currentTimeMillis());
            if ( BluetoothActivity.this.currTime + 5000 <= System.currentTimeMillis() ) {
                if ( BluetoothActivity.this.deviceList.size() == 1 ) {
                    BluetoothActivity.this.stopScanning();
                    BluetoothActivity.this.leService.connectToDeviceSelected(BluetoothActivity.this.deviceList.get(0));
                    BluetoothActivity.this.finish();
                } else if ( BluetoothActivity.this.deviceList.size() > 1 ) {
                    BluetoothActivity.this.bluetoothListview.setVisibility(View.VISIBLE);
                    BluetoothActivity.this.foundDevices.setVisibility(View.VISIBLE);
                    BluetoothActivity.this.bluetoothText.setVisibility(View.GONE);
                }
            }
        }
    };

    /**
     * Startet den Scan nach Bluetooth-Geräten
     */
    public void startScanning() {
        Log.i("startScanning", "start scanning");
        this.checkBluetoothAndLocationEnabled();
        if ( this.leService.btAdapter != null && this.leService.btAdapter.isEnabled() ) {
            this.currTime = System.currentTimeMillis();
            this.buttonStartScan.setVisibility(View.INVISIBLE);
            this.buttonStopScan.setVisibility(View.VISIBLE);
            this.textViewScanning.setVisibility(View.VISIBLE);
            this.bluetoothText.setVisibility(View.VISIBLE);
            this.foundDevices.setVisibility(View.INVISIBLE);
            this.bluetoothListview.setVisibility(View.INVISIBLE);
            AsyncTask.execute(() -> this.leService.btScanner.startScan(this.leScanCallback));
        }
    }

    /**
     * Stoppt den Scan nach Bluetooth-Geräten
     */
    public void stopScanning() {
        Log.i("stopScanning", "stopping scanning");
        this.buttonStartScan.setVisibility(View.VISIBLE);
        this.buttonStopScan.setVisibility(View.INVISIBLE);
        this.textViewScanning.setVisibility(View.INVISIBLE);
        this.bluetoothText.setVisibility(View.GONE);
        AsyncTask.execute(() -> this.leService.btScanner.stopScan(this.leScanCallback));
    }

    /**
     * Methode prüft ob Bluetooth angeschaltet ist und ob die Lokalisierung freigegeben wurde.
     * Zweiteres ist notwendig um den Calliope erkennen zu können.
     */
    private void checkBluetoothAndLocationEnabled() {

        this.leService.btManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        this.leService.btAdapter = this.leService.btManager.getAdapter();
        this.leService.btScanner = this.leService.btAdapter.getBluetoothLeScanner();

        if ( this.leService.btAdapter != null && !this.leService.btAdapter.isEnabled() ) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        // Falls die Freigabe zur Lokalisierung nicht aktiv ist, wird der Nutzer dazu aufgefordert
        // die Lokalisierung freizugeben. Dies ist nötig damit Bluetooth-Geräte gefunden werden können.
        if ( this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.need_location_access);
            builder.setMessage(R.string.grant_location_access);
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> this.requestPermissions(
                new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                PERMISSION_REQUEST_COARSE_LOCATION));
            builder.show();
        }

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch ( RuntimeException ex ) {
            ex.printStackTrace();
        }

        try {
            networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch ( RuntimeException ex ) {
            ex.printStackTrace();
        }

        if ( !gpsEnabled && !networkEnabled ) {
            // Nutzer benarichtigen
            new AlertDialog.Builder(this).setMessage(R.string.location_not_active_message)
                                         .setPositiveButton(R.string.location_activate,
                                                            (paramDialogInterface, paramInt) -> this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                                         .setNegativeButton(R.string.cancel, null)
                                         .show();
        }
    }
}
