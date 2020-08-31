package de.fhg.iais.roberta.ar.visualization;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BLEService extends AppCompatActivity {

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    private static int lastLoggedAcceleration_X = 0;
    private static int lastLoggedAcceleration_Y = 0;
    private static int lastLoggedAcceleration_Z = 0;
    private static int lastLoggedTemperature = 0;
    private static int lastLoggedMagnetometer = 0;
    private static int lastLoggedLight = 0;
    private static int lastLoggedMicro = 0;
    private static boolean pressedAbutton = false;
    private static boolean pressedBbutton = false;
    private static final List<Integer> microInputs = new ArrayList<>();

    private static int lastLoggedCompas = 0;
    private static BluetoothGatt globalGatt = null;

    public static final String ACTION_GATT_CONNECTED = "de.fhg.iais.roberta.ar.bluetooth.le.ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = "de.fhg.iais.roberta.ar.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED = "de.fhg.iais.roberta.ar.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE = "de.fhg.iais.roberta.ar.bluetooth.le.ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA = "de.fhg.iais.roberta.ar.bluetooth.le.EXTRA_DATA";

    public void connectToDeviceSelected(BluetoothDevice device) {
        device.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_AUTO);
    }

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        /**
         * Is executed if a device gets connected or disconnected.
         * Makes sure the global available gatt gets declared and null if not available anymore.
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if ( newState == BluetoothProfile.STATE_CONNECTED ) {
                Log.i("onConnectionStateChange", "Connected to GATT server.");
                gatt.discoverServices();
                globalGatt = gatt;
            } else if ( newState == BluetoothProfile.STATE_DISCONNECTED ) {
                Log.i("onConnectionStateChange", "Disconnected from GATT server.");
                globalGatt = null;
            }
        }

        /**
         * Gives an Overview of the Services and there Characteristics.
         * If Descriptors are available, they are also displayed.
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> gattServices = gatt.getServices();

            if ( gattServices == null ) {
                return;
            }

            // Loops through available GATT Services.
            for ( BluetoothGattService gattService : gattServices ) {

                String uuid = gattService.getUuid().toString();
                Log.i("onServicesDiscovered", "Service discovered: " + uuid);

                new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();

                // Loops through available Characteristics.
                Log.i("onServicesDiscovered", "Loops through available Characteristics");
                for ( BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics ) {

                    String charUuid = gattCharacteristic.getUuid().toString();
                    Log.i("onServicesDiscovered", "Characteristic discovered for service: " + charUuid);

                    List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
                    for ( BluetoothGattDescriptor gattDescriptor : gattDescriptors ) {
                        Log.i("onServicesDiscovered", "Descriptor Info:\n");
                        Log.i("onServicesDiscovered", "Describe Content: " + gattDescriptor.describeContents());
                        //System.out.println("Char it belongs to: "+ gattDescriptor.getCharacteristic()); // Might not work
                        Log.i("onServicesDiscovered", "Char it belongs to: " + gattDescriptor.getCharacteristic().getUuid().toString());
                        Log.i("onServicesDiscovered", "Descr. UUID: " + gattDescriptor.getUuid());
                        //String str = new String(gattDescriptor.getValue(), StandardCharsets.UTF_8);
                        //System.out.println("Value: "+str);
                        // TODO Ausgaben in Log? Nicht benötigte entfernen
                    }
                }
            }
            indicateUART();
        }

        /**
         * Is triggered after a characteristic read operation.
         * Checks from which characteristic the read took place
         * and saves the read value to the corresponding variable,
         * to be read by the method getTemperature or the like.
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if ( status == BluetoothGatt.GATT_SUCCESS ) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                if ( characteristic.getUuid().toString().equals("e95d9715-251d-470a-a062-fa1922dfa9a8") ) {

                    // https://stackoverflow.com/questions/736815/2-bytes-to-short-java
                    byte[] b = characteristic.getValue();
                    ByteBuffer bb = ByteBuffer.allocate(2);
                    bb.order(ByteOrder.LITTLE_ENDIAN);
                    bb.put(b[0]);
                    bb.put(b[1]);
                    short shortVal = bb.getShort(0);
                    Log.i("onCharacteristicRead", "Magnetometer gelesen: Wert " + shortVal);

                    lastLoggedMagnetometer = shortVal;
                } else if ( characteristic.getUuid().toString().equals("e95d9250-251d-470a-a062-fa1922dfa9a8") ) {
                    Log.i("onCharacteristicRead", "Temperatur gelesen: Wert " + bytesToInt(characteristic.getValue()));
                    lastLoggedTemperature = bytesToInt(characteristic.getValue());
                } else {
                    Log.i("onCharacteristicRead", "Nicht bekannter Wert eingelesen.");
                }
            }
        }

        /**
         * Wird ausgeführt wenn die Characteristic sich ändert, also wenn eine neue
         * Nachricht vom Calliope vorliegt. Die Nachricht wird entsprechend geteilt und
         * die Werte in den jeweiligen Variablen gespeichert
         */
        @Override
        // Characteristic notification
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            String uartInput = new String(characteristic.getValue(), StandardCharsets.UTF_8);
            Log.i("onCharacteristicChanged", "UART : " + uartInput);
            if ( uartInput.contains(":") ) {

                String[] digits = BLEService.this.splitString(uartInput, "[^\\d-]");
                String[] descriptors = BLEService.this.splitString(uartInput, "[^A-Z]");

                for ( int i = 0; i < descriptors.length; i++ ) {
                    switch ( descriptors[i] ) {
                        case "L":
                            lastLoggedLight = Integer.parseInt(digits[i]);
                            break;
                        case "M":
                            addMicroInput(Integer.parseInt(digits[i]));
                            lastLoggedMicro = Integer.parseInt(digits[i]);
                            break;
                        case "C":
                            lastLoggedCompas = Integer.parseInt(digits[i]);
                            break;
                        case "T":
                            lastLoggedTemperature = Integer.parseInt(digits[i]);
                            break;
                        case "AX":
                            lastLoggedAcceleration_X = Integer.parseInt(digits[i]);
                            break;
                        case "AY":
                            lastLoggedAcceleration_Y = Integer.parseInt(digits[i]);
                            break;
                        case "AZ":
                            lastLoggedAcceleration_Z = Integer.parseInt(digits[i]);
                            break;
                        case "BA":
                            pressedAbutton = (Integer.parseInt(digits[i]) == 1);
                            break;
                        case "BB":
                            pressedBbutton = (Integer.parseInt(digits[i]) == 1);
                            break;

                    }
                }

            }
        }
    };

    /**
     * Teilt einen String entsprechend des übergebenen regulären Ausdrucks und gibt ein String-Array
     * mit den einzelnen Werten zurück.
     * Reguläre Ausdrücke (Regex): https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html
     *
     * @param str   Der zu teilende String
     * @param regex Der reguläre Ausdruck
     * @return Array mit geteilten Strings
     */
    private String[] splitString(String str, String regex) {
        // Entferne alles außer gewählte Regex vom String und ersetze alles durch " "
        String uartValues = str.replaceAll(regex, " ");
        // Entferne führende und folgende Leerstellen
        uartValues = uartValues.trim();
        // Ersetze mehrere Leerstellen durch eine einzige Leerstelle
        uartValues = uartValues.replaceAll(" +", " ");
        // Erstellt ein Array, indem an den Leerstellen geteilt wird
        return uartValues.split(" ");
    }

    /**
     * Wandelt einen uint8 Wert in einen für Java verwendbaren int Wert um.
     *
     * @param x der zu wandelnde Wert
     * @return der nutzbare Wert
     */
    public static int bytesToInt(byte[] x) {
        return new BigInteger(x).intValue();
    }

    /**
     * Ausgabe der Uuid einer Characteristic
     */
    private void broadcastUpdate(
        String action, BluetoothGattCharacteristic characteristic) {
        Log.i("broadcastUpdate", characteristic.getUuid().toString());
    }

    /**
     * Get-Methoden verschiedener Sensorwerte
     */

    public static boolean getAbutton() {
        return globalGatt != null && pressedAbutton;
    }

    public static boolean getBbutton() {
        return globalGatt != null && pressedBbutton;
    }

    public static int getCompass() {
        return globalGatt != null ? lastLoggedCompas : 0;
    }

    public static int getTemperature() {
        return globalGatt != null ? lastLoggedTemperature : 0;
    }

    public static int getAcceleration_X() {
        return globalGatt != null ? lastLoggedAcceleration_X : 0;
    }

    public static int getAcceleration_Y() {
        return globalGatt != null ? lastLoggedAcceleration_Y : 0;
    }

    public static int getAcceleration_Z() {
        return globalGatt != null ? lastLoggedAcceleration_Z : 0;
    }

    public static int getLight() {
        return globalGatt != null ? lastLoggedLight : 0;
    }

    public static int getMicro() {
        return globalGatt != null ? lastLoggedMicro : 0;
    }

    public static List<Integer> getMicroValues() {
        return globalGatt != null ? microInputs : null;
    }

    public static void addMicroInput(int input) {
        while ( microInputs.size() >= 10 ) {
            microInputs.remove(0);
        }
        microInputs.add(input);
    }

    public static BluetoothGatt getGlobalGatt() {
        return globalGatt;
    }

    /**
     * Aktivieren des Uart-Services und empfangen/speichern der Daten
     */
    public static void indicateUART() {
        Log.i("indicateUART", "UART wird gelesen.");
        BluetoothGattService uartService = globalGatt.getService(java.util.UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E"));
        BluetoothGattCharacteristic uartCharacteristic = uartService.getCharacteristic(java.util.UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E"));
        BluetoothGattDescriptor descriptor = uartCharacteristic.getDescriptor(java.util.UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        globalGatt.writeDescriptor(descriptor);
        // Eventuell überflüssig
        globalGatt.setCharacteristicNotification(uartCharacteristic, true);
        globalGatt.readCharacteristic(uartCharacteristic);
    }
}