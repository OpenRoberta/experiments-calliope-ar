package de.fhg.iais.roberta.ar.visualization;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Light;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import de.fhg.iais.roberta.ar.R;

public class CameraActivity extends AppCompatActivity {

    // KONSTANTEN
    private static final float SCALE_FACTOR = 5.0f;
    private static final int CAMP_NUMBER = 1;
    private static final int COMPASS_NUMBER = 2;
    private static final int TEMPERATURE_NUMBER = 3;
    private static final int THERMOMETER_NUMBER = 4;
    private static final int LIGHT_NUMBER = 5;
    private static final int AUDIO_NUMBER = 6;
    private static final int ACCELERATION_NUMBER = 7;
    private int SENSOR_NUMBER = CAMP_NUMBER;

    private ArFragment arCoreFragment = null;
    private ImageView fitToScanView = null;
    private boolean detectedCalliope;
    private int audioObjects;
    private AugmentedImage augImage;
    private TextToSpeech ttobj;

    // SENSOR VALUES
    private int audio = 0;
    private int currentAudio = -1;
    private int temperature = 0;
    private int currentTemperature = -1;
    private float accX = 0.0F;
    private float currentAccX = -1.0f;
    private float accY = 0.0F;
    private float currentAccY = -1.0f;
    private float accZ = 0.0F;
    private float currentAccZ = -1.0f;
    private int magnetometer = 0;
    private int currentMagnetometer = -1;
    private int light = 0;
    private int currentLight = -1;
    private boolean buttonA = false;
    private boolean buttonB = false;
    private boolean[] activeSensors = null;
    private Vector3 scooterPosition = null;
    private Vector3 vecGlobal = null;

    private float temp = 0.0F;

    // BUTTONS
    private RelativeLayout compassButton = null;
    private RelativeLayout temperatureButton = null;
    private RelativeLayout thermometerButton = null;
    private RelativeLayout lightButton = null;
    private RelativeLayout audioButton = null;
    private RelativeLayout accelerationButton = null;
    private RelativeLayout campButton = null;
    private RelativeLayout campButton2 = null;
    private ImageButton bluetoothButton = null;

    private Boolean hasFinishedLoading = false;

    // NODES & ANCHOR
    private Node buttonNodeA = null;
    private Node buttonNodeB = null;
    private Node lightNode = null;
    private Node[] audioNode = null;
    private Node accNode = null;
    private Node tempFirstNode = null;
    private Node tempSecondNode = null;
    private Node tempDegreeNode = null;
    private Node thermoTempFirstNode = null;
    private Node thermoTempSecondNode = null;
    private Node thermoTempDegreeNode = null;
    private Node compassNode = null;
    private Node campNode = null;
    private Node fireNode = null;
    private Node campSun = null;
    private Node campMoon = null;
    private Node scooterNode = null;
    private final Node[] notesNode = new Node[4];
    private Node thermoBaseNode = null;
    private Node thermoBubbleNode = null;
    private Node thermoBarNode = null;
    private Node campFlag = null;
    private AnchorNode anchorNodeGlobal = null;
    private TransformableNode tfLight = null;
    private TransformableNode tfTemp = null;
    private TransformableNode tfCompass = null;
    private TransformableNode tfAcc = null;
    private TransformableNode tfAudio = null;
    private TransformableNode tfCamp = null;
    private TransformableNode tfThermo = null;
    private boolean setAnchor = false;

    // MODELS
    private ModelRenderable accelerationModel = null;
    private ModelRenderable compassModel = null;
    private ModelRenderable fireModel = null;
    private ModelRenderable scooterModel = null;
    private final ModelRenderable[] notesModels = new ModelRenderable[4];
    private ModelRenderable audioModel = null;
    private ModelRenderable campModel = null;
    private ModelRenderable thermoBaseModel = null;
    private ModelRenderable thermoBubbleModel = null;
    private ModelRenderable thermoBarModel = null;
    private final ModelRenderable[] numberModels = new ModelRenderable[10];
    private ModelRenderable sunModel = null;
    private ModelRenderable buttonModelA = null;
    private ModelRenderable buttonModelB = null;
    private ModelRenderable campFlagModel = null;
    private ModelRenderable campMoonModel = null;
    private ModelRenderable campSunModel = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_camera);

        View decorView = this.getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> this.showSystemUI());

        // Buttons
        this.compassButton = this.findViewById(R.id.button_compass);
        this.temperatureButton = this.findViewById(R.id.button_temperature);
        this.thermometerButton = this.findViewById(R.id.button_thermometer);
        this.lightButton = this.findViewById(R.id.button_light);
        this.audioButton = this.findViewById(R.id.button_audio);
        this.accelerationButton = this.findViewById(R.id.button_acceleration);
        this.campButton = this.findViewById(R.id.button_camp);
        this.bluetoothButton = this.findViewById(R.id.bluetooth_button);

        // Bluetooth Check
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(this.mReceiver, filter);

        // Laden der Modelle
        this.buildRenderables();

        this.activeSensors = new boolean[10];
        Arrays.fill(this.activeSensors, Boolean.FALSE);

        this.activeSensors[this.SENSOR_NUMBER] = true;
        Log.i("CameraActivity - onCreate", "SENSOR_NUMBER: " + this.SENSOR_NUMBER);
        this.updateUI();

        this.bluetoothButton.setOnClickListener(v -> {
            Intent intent = new Intent(this.getApplicationContext(), BluetoothActivity.class);
            this.startActivity(intent);
        });

        this.compassButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = COMPASS_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.temperatureButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = TEMPERATURE_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.lightButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = LIGHT_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.audioButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = AUDIO_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.accelerationButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = ACCELERATION_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.campButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = CAMP_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.thermometerButton.setOnClickListener(v -> {
            this.disableAll();
            this.SENSOR_NUMBER = THERMOMETER_NUMBER;
            this.activeSensors[this.SENSOR_NUMBER] = !this.activeSensors[this.SENSOR_NUMBER];
            this.updateUI();
        });

        this.ttobj = new TextToSpeech(this.getApplicationContext(), i -> {
            // check for successful instantiation
            if ( i == TextToSpeech.SUCCESS ) {
                if ( this.ttobj.isLanguageAvailable(Locale.GERMAN) == TextToSpeech.LANG_AVAILABLE ) {
                    this.ttobj.setLanguage(Locale.GERMAN);
                }
            } else if ( i == TextToSpeech.ERROR ) {
                Log.i("onCreate", "Sorry! Text To Speech failed...");
            }
        });

        this.fitToScanView = this.findViewById(R.id.image_view_fit_to_scan);
        this.fitToScanView.setVisibility(View.GONE);

        this.arCoreFragment = (ArFragment) this.getSupportFragmentManager().findFragmentById(R.id.main_fragment);
        // hide introduction
        this.arCoreFragment.getPlaneDiscoveryController().hide();
        this.arCoreFragment.getPlaneDiscoveryController().setInstructionView(null);
        // disable dots (plane detection)
        this.arCoreFragment.getArSceneView().getPlaneRenderer().setVisible(false);
        this.arCoreFragment.getArSceneView().getPlaneRenderer().setShadowReceiver(false);

        this.arCoreFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

    }

    /**
     * Ändert die Farbe des Bluetooth-Symbols, je nach Verbindungsstatus.
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ) {
                CameraActivity.this.bluetoothButton.setColorFilter(Color.argb(255, 0, 255, 0));
            } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action) ) {
                CameraActivity.this.bluetoothButton.setColorFilter(Color.argb(255, 255, 0, 0));
            } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action) ) {
                CameraActivity.this.bluetoothButton.setColorFilter(Color.argb(255, 255, 0, 0));
            }
        }
    };

    /**
     * Gibt Text als Sprache aus
     *
     * @param text der Text der gesprochen werden soll
     */
    private void callOut(String text) {
        //CharSequence numCharSeq = String.valueOf(num);
        this.ttobj.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    /**
     * Setzt das Array der aktiven Sensoren auf false. Außerdem werden die aktuellen Sensorwerte
     * auf -1 gesetzt, sodass die Objekte erneut dargestellt werden, wenn der beliebige Sensor
     * ausgewählt wird.
     */
    void disableAll() {
        Arrays.fill(this.activeSensors, Boolean.FALSE);
        this.currentTemperature = -1;
        this.currentMagnetometer = -1;
        this.currentLight = -1;
        this.currentAccX = -1.0f;
        this.currentAccY = -1.0f;
        this.currentAccZ = -1.0f;
        this.currentAudio = -1;
    }

    /**
     * Setzt die Umrandung eines Buttons auf grün, wenn er im Array "active_sensors" als true
     * gesetzt wurde, ansonsten auf weiß.
     */
    void updateUI() {
        if ( this.activeSensors[COMPASS_NUMBER] ) {
            this.compassButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.compassButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[TEMPERATURE_NUMBER] ) {
            this.temperatureButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.temperatureButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[LIGHT_NUMBER] ) {
            this.lightButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.lightButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[AUDIO_NUMBER] ) {
            this.audioButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.audioButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[ACCELERATION_NUMBER] ) {
            this.accelerationButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.accelerationButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[CAMP_NUMBER] ) {
            this.campButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.campButton.setBackgroundResource(R.drawable.background_border);
        }
        if ( this.activeSensors[THERMOMETER_NUMBER] ) {
            this.thermometerButton.setBackgroundResource(R.drawable.background_border_green);
        } else {
            this.thermometerButton.setBackgroundResource(R.drawable.background_border);
        }
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = this.arCoreFragment.getArSceneView().getArFrame();
        // If there is no frame or ARCore is not tracking yet, just return.
        if ( frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING ) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for ( AugmentedImage augmentedImage : updatedAugmentedImages ) {
            this.augImage = augmentedImage;
            switch ( augmentedImage.getTrackingState() ) {
                case PAUSED:
                    // ARCore has paused tracking this instance, but may resume tracking it in
                    // the future. This can happen if device tracking is lost, or if the user
                    // enters a new space, or if the Session is currently paused. When in this
                    // state the properties of the instance may be wildly inaccurate and should
                    // generally not be used.
                    String text = "Detected Image " + augmentedImage.getIndex();
                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                    break;
                case TRACKING:
                    //The Trackable is currently tracked and its pose is current.
                    this.fitToScanView.setVisibility(View.GONE);

                    this.buttonA = BLEService.getAbutton();
                    this.buttonB = BLEService.getBbutton();

                    if ( !this.setAnchor ) {
                        this.setAnchor = true;
                        this.createAnchorAndTfNodes(augmentedImage);
                    }

                    if ( !this.detectedCalliope ) {
                        final String txt = "Calliope erkannt!";
                        Toast.makeText(this, txt, Toast.LENGTH_LONG).show();
                        this.detectedCalliope = true;
                    }

                    if ( this.hasFinishedLoading ) {

                /*
                  KOMPASS SENSOR
                */
                        if ( this.activeSensors[COMPASS_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfCompass);
                            this.magnetometer = BLEService.getCompass();
                            Log.i("SENSOR Megnetometer", "magnetometer: " + this.magnetometer + " | currMag.: " + this.currentMagnetometer);

                            if ( this.currentMagnetometer != this.magnetometer ) {
                                if ( !this.tfCompass.getChildren().contains(this.compassNode) ) {
                                    this.compassNode =
                                        ArMethods.createNode(this.tfCompass, this.compassModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.2f, 0.0f));

                                    float scale = ArMethods.calculateScaling(this.augImage, this.compassNode, SCALE_FACTOR);
                                    this.compassNode.setLocalScale(new Vector3(scale, scale, scale));
                                }
                                this.currentMagnetometer = this.magnetometer;

                                // Rotation um Kompasswert
                                this.compassNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), this.currentMagnetometer));

                                this.tfCompass.addChild(this.compassNode);
                                this.anchorNodeGlobal.addChild(this.tfCompass);
                            }
                        }

                /*
                  TEMPERATUR SENSOR
                */
                        if ( this.activeSensors[TEMPERATURE_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfTemp);
                            this.temperature = BLEService.getTemperature();
                            Log.i("SENSOR Temperature", "temperature: " + this.temperature + " | currTemp: " + this.currentTemperature);

                            if ( this.currentTemperature != this.temperature ) {
                                if ( this.tfTemp.getChildren().contains(this.tempFirstNode) ) {
                                    this.tfTemp.removeChild(this.tempFirstNode);
                                    this.tfTemp.removeChild(this.tempSecondNode);
                                    this.tfTemp.removeChild(this.tempDegreeNode);
                                }

                                this.currentTemperature = this.temperature;

                                int tempFirst = this.currentTemperature;
                                int tempSecond = tempFirst;

                                // Trennen in einzelne Ziffern
                                tempFirst /= 10;
                                tempSecond %= 10;

                                // Erstellen der Ziffern-Knoten mit entsprechendem Modell
                                this.tempFirstNode =
                                    ArMethods.createNode(this.tfTemp,
                                                         this.numberModels[tempFirst],
                                                         new Vector3(0.5f, 0.5f, 0.5f),
                                                         new Vector3(-0.1f, 0.03f, 0.0f));

                                this.tempSecondNode =
                                    ArMethods.createNode(this.tfTemp,
                                                         this.numberModels[tempSecond],
                                                         new Vector3(0.5f, 0.5f, 0.5f),
                                                         new Vector3(0.1f, 0.03f, 0.0f));

                                this.tempDegreeNode =
                                    ArMethods.createNode(this.tfTemp, this.numberModels[0], new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.1f, 0.03f, 0.0f));

                                // Skalierung der Ziffern
                                float scale = ArMethods.calculateScaling(this.augImage, this.tempFirstNode, SCALE_FACTOR) / 2.0f;
                                this.tempFirstNode.setLocalScale(new Vector3(scale, scale, scale));
                                this.tempSecondNode.setLocalScale(new Vector3(scale, scale, scale));
                                this.tempDegreeNode.setLocalScale(new Vector3(scale / 3.0f, (scale / 3.0f) / 2.0f, scale / 3.0f));

                                // Berechnung der Positionen
                                Box boxFirst = (Box) this.tempFirstNode.getRenderable().getCollisionShape();
                                Box boxSecond = (Box) this.tempSecondNode.getRenderable().getCollisionShape();
                                float offset = (boxFirst.getSize().x + boxSecond.getSize().x) / 40.0f;
                                float pos = ((boxFirst.getSize().x + boxSecond.getSize().x) / 4.0f + offset) * scale;
                                this.tempFirstNode.setLocalPosition(new Vector3(-pos, 0.25f, 0.0f));
                                this.tempSecondNode.setLocalPosition(new Vector3(pos, 0.25f, 0.0f));
                                // X: Position zweiter Ziffer + Hälfte der Breite der Ziffer + Offset
                                this.tempDegreeNode.setLocalPosition(new Vector3(
                                    pos + ((boxSecond.getSize().x / 2.0f + offset * 5.0f) * scale),
                                    boxFirst.getSize().y * scale + 0.2f,
                                    0.0f));

                                // Einfärben der Ziffern, je nach Temperatur
                                float rValue;
                                float bValue;
                                final int threshold = 20;

                                if ( this.currentTemperature < threshold ) {
                                    rValue = 0.0f;
                                    bValue = (threshold - this.currentTemperature);
                                } else {
                                    rValue = (-threshold + this.currentTemperature);
                                    bValue = 0.0f;
                                }

                                final float lightUpValue = 0.15f;
                                final float colorScale = 0.1f;
                                ArMethods.colorAsset(
                                    this.tempFirstNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    1.0f);
                                ArMethods.colorAsset(
                                    this.tempSecondNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    0.0f);
                                ArMethods.colorAsset(
                                    this.tempDegreeNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    0.0f);

                                this.callOut("ES SIND " + this.currentTemperature + " GRAD.");

                                this.tfTemp.addChild(this.tempFirstNode);
                                this.tfTemp.addChild(this.tempSecondNode);
                                this.tfTemp.addChild(this.tempDegreeNode);
                                this.anchorNodeGlobal.addChild(this.tfTemp);
                            }
                        }

                /*
                  LICHT SENSOR
                */

                        if ( this.activeSensors[LIGHT_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfLight);
                            this.light = BLEService.getLight();
                            Log.i("SENSOR Light", "light: " + this.light + " | currLight: " + this.currentLight);

                            if ( this.currentLight != this.light ) {
                                if ( !this.tfLight.getChildren().contains(this.lightNode) ) {
                                    this.lightNode =
                                        ArMethods.createNode(this.tfLight, this.sunModel, new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.0f, 0.5f, 0.0f));

                                    float scale = ArMethods.calculateScaling(this.augImage, this.lightNode, SCALE_FACTOR);
                                    this.lightNode.setLocalScale(new Vector3(scale, scale, scale));
                                }

                                this.currentLight = this.light;

                                ArMethods.colorAsset(this.lightNode, this.currentLight * 0.01f, this.currentLight * 0.01f, 0.0f, 0.0f);

                                this.tfLight.addChild(this.lightNode);
                                this.anchorNodeGlobal.addChild(this.tfLight);
                            }
                        }

                /*
                  AUDIO SENSOR
                */
                        if ( this.activeSensors[AUDIO_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfAudio);

                            List<Integer> audioList;
                            audioList = BLEService.getMicroValues();

                            if ( audioList != null ) {
                                audioObjects = audioList.size();
                                if ( this.currentAudio == -1 ) {
                                    if ( this.audioNode != null ) {
                                        for ( Node node : this.audioNode ) {
                                            if ( this.tfAudio.getChildren().contains(node) ) {
                                                this.tfAudio.removeChild(node);
                                            }
                                        }
                                    }
                                    this.audioNode = new Node[audioObjects];
                                }

                                Log.i("SENSOR Audio", "audio: " + this.audio + " | currAudio: " + this.currentAudio + " | objects: " + audioObjects);
                                this.audio = audioList.get(audioObjects - 1);

                                if ( this.currentAudio != this.audio ) {
                                    for ( Node node : this.audioNode ) {
                                        if ( this.tfAudio.getChildren().contains(node) ) {
                                            this.tfAudio.removeChild(node);
                                        }
                                        //System.out.print("i: " + i + " s: " + tfAudio.getChildren().size());
                                    }
                                    //System.out.println("");
                                    this.currentAudio = this.audio;

                                    for ( int i = 0; i < audioObjects; i++ ) {
                                        this.audioNode[i] =
                                            ArMethods.createNode(this.tfAudio, this.audioModel, new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.0f, 0.5f, 0.0f));
                                    }

                                    Box box = (Box) this.audioNode[0].getRenderable().getCollisionShape();
                                    float
                                        scale =
                                        ArMethods.calculateScaling(this.augImage, this.audioNode[0], SCALE_FACTOR) / 10.0f; // Verkleinern der Skalierung

                                    float width = box.getSize().x;
                                    float barCompressionFactor = 3.0f;
                                    for ( int i = 0; i < audioObjects; i++ ) {
                                        this.audioNode[i].setLocalScale(new Vector3(scale, scale * audioList.get(i) / barCompressionFactor, scale));
                                        // Positionierung jedes Balkens auf der X-Achse
                                        float pos = ((i * width - ((width * audioObjects) / 2.0f)) + width / 2.0f) * scale;
                                        // Verschiebt Balken so, dass sie auf einer Ebene sind.
                                        //float barHeight = 0.15f + (width * scale * audioList.get(i)) / (2 * barCompressionFactor);
                                        // Alternative, wenn Balken nicht auf einer Ebene sein sollen.
                                        final float barHeight = 0.2f;
                                        this.audioNode[i].setLocalPosition(new Vector3(pos, barHeight, 0.0f));
                                        // FARBE
                                        ArMethods.colorAsset(this.audioNode[i], 0.0f, audioList.get(i) * 0.01f, audioList.get(i) * 0.01f, 0.0f);

                                        this.tfAudio.addChild(this.audioNode[i]);
                                    }

                                    if ( this.currentAudio > 0 ) {
                                        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                                            v.vibrate(VibrationEffect.createOneShot(this.currentAudio, VibrationEffect.DEFAULT_AMPLITUDE));
                                        } else {
                                            //deprecated in API 26
                                            v.vibrate(this.currentAudio);
                                        }
                                    }

                                    this.anchorNodeGlobal.addChild(this.tfAudio);
                                }
                            }
                        }

                /*
                  BESCHLEUNIGUNGS SENSOR
                */
                        if ( this.activeSensors[ACCELERATION_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfAcc);
                            this.accX = BLEService.getAcceleration_X();
                            this.accY = BLEService.getAcceleration_Y();
                            this.accZ = BLEService.getAcceleration_Z();
                            Log.i("SENSOR Acceleration", "AccX: " + this.accX + " | currAccX: " + this.currentAccX);
                            Log.i("SENSOR Acceleration", "AccY: " + this.accY + " | currAccY: " + this.currentAccY);
                            Log.i("SENSOR Acceleration", "AccZ: " + this.accZ + " | currAccZ: " + this.currentAccZ);

                            if ( this.currentAccX != this.accX || this.currentAccY != this.accY || this.currentAccZ != this.accZ ) {
                                if ( !this.tfAcc.getChildren().contains(this.accNode) ) {
                                    this.accNode =
                                        ArMethods.createNode(this.tfAcc, this.accelerationModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.3f, 0.0f));

                                    float scale = ArMethods.calculateScaling(this.augImage, this.accNode, SCALE_FACTOR);
                                    this.accNode.setLocalScale(new Vector3(scale, scale, scale));
                                }
                                this.currentAccX = this.accX;
                                this.currentAccY = this.accY;
                                this.currentAccZ = this.accZ;

                                float max_acc = Math.max(this.currentAccX, this.currentAccY);
                                max_acc = Math.max(max_acc, this.currentAccZ);
                                if ( max_acc < 1000 ) {
                                    max_acc = 1000;
                                }

                                float winkel_x = this.currentAccX * 90.0f / max_acc; // Z
                                float winkel_y = (this.currentAccY * 90.0f / max_acc) * -1.0f; // X

                                if ( this.currentAccZ < 0.0f ) {
                                    if ( winkel_x < 0.0f ) {
                                        winkel_x = -90.0f - (90.0f - winkel_x);
                                    } else {
                                        winkel_x = 90.0f + (90.0f - winkel_x);
                                    }

                                    if ( winkel_y < 0.0f ) {
                                        winkel_y = -90.0f - (90.0f - winkel_y);
                                    } else {
                                        winkel_y = 90.0f + (90.0f - winkel_y);
                                    }
                                }

                                if ( winkel_x > 180.0f ) {
                                    winkel_x = 180.0f;
                                }
                                if ( winkel_x < -180.0f ) {
                                    winkel_x = -180.0f;
                                }
                                if ( winkel_y > 180.0f ) {
                                    winkel_y = 180.0f;
                                }
                                if ( winkel_y < -180.0f ) {
                                    winkel_y = -180.0f;
                                }

                                Log.i("WINKEL", "winkel x: " + winkel_x + " winkel y: " + winkel_y);

                                this.accNode.setLocalRotation(Quaternion.axisAngle(new Vector3(winkel_y, 0.0f, winkel_x), 1.0f));

                                this.tfAcc.addChild(this.accNode);
                                this.anchorNodeGlobal.addChild(this.tfAcc);
                            }
                        }

                /*
                  CAMP - DARSTELLUNG ALLER SENSOREN
                */

                        if ( this.activeSensors[CAMP_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfCamp);

                            // Einmalige Erstellung der Camp-Knoten
                            if ( this.campNode == null ) {

                                //campSun_model.setShadowReceiver(false);
                                this.campSunModel.setShadowCaster(false);

                                this.campNode = ArMethods.createNode(this.tfCamp, this.campModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.3f, 0.0f));

                                this.fireNode =
                                    ArMethods.createNode(this.campNode, this.fireModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.3f, 0.0f));

                                this.scooterNode =
                                    ArMethods.createNode(this.tfCamp, this.scooterModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.3f, 0.0f));

                                this.campFlag =
                                    ArMethods.createNode(this.campNode, this.campFlagModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.3f, 0.0f));

                                this.campSun =
                                    ArMethods.createNode(this.tfCamp, this.campSunModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.25f, 0.0f));

                                this.campMoon =
                                    ArMethods.createNode(this.tfCamp, this.campMoonModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.25f, 0.0f));

                                for ( int i = 0; i < this.notesNode.length; i++ ) {
                                    this.notesNode[i] =
                                        ArMethods.createNode(this.campNode, this.notesModels[i], new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0, 0.3f, 0.0f));
                                }

                                // Positionen aus Objekt-Position in Blender übernommen. Das Objekt dann in Blender
                                // auf auf Position 0,0,0 gesetzt und gerendert. Durch dieses Workaround kann
                                // ein Objekt, welches nicht mittig über dem Calliope dargestellt wird, skaliert werden
                                // ohne dass die Position verrutscht.
                                this.fireNode.setLocalPosition(new Vector3(0.511806f, 1.48118f, 1.71165f));
                                this.notesNode[0].setLocalPosition(new Vector3(1.35728f, 3.39887f, 2.01575f));
                                this.notesNode[1].setLocalPosition(new Vector3(2.68024f, 2.43193f, 1.20667f));
                                this.notesNode[2].setLocalPosition(new Vector3(1.88062f, 3.90522f, 1.188f));
                                this.notesNode[3].setLocalPosition(new Vector3(1.43085f, 2.68927f, 0.225592f));
                                this.campFlag.setLocalPosition(new Vector3(-0.559019f, 2.28788f, 0.461503f));

                                // Sonne / Mond Start-Rotation
                                this.campSun.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), 45.0f));
                                this.campMoon.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), 225.0f));
                                this.arCoreFragment.getArSceneView()
                                                   .getScene()
                                                   .getSunlight()
                                                   .setLookDirection(new Vector3(45.0f / 450.0f, 0.0f + (45.0f / -450.0f), 0.0f));

                                this.scooterPosition = this.scooterNode.getLocalPosition();

                            }

                            int temperature = BLEService.getTemperature();
                            int magnetometer = BLEService.getCompass();
                            int audio = BLEService.getMicro();
                            int accX = BLEService.getAcceleration_X();
                            int light = BLEService.getLight();

                            float scale = ArMethods.calculateScaling(this.augImage, this.campNode, SCALE_FACTOR);
                            this.campNode.setLocalScale(new Vector3(scale, scale, scale));

                            // Feuer
                            final float fireThreshold = 15.0f;
                            this.fireNode.setLocalScale(new Vector3(temperature / fireThreshold, temperature / fireThreshold, temperature / fireThreshold));

                            // Noten
                            this.temp = this.temp + this.notesNode[0].getLocalRotation().x + audio / 5.0f;
                            this.temp = this.temp % 360;

                            // Skalieren und Rotieren der Noten
                            for ( Node node : this.notesNode ) {
                                node.setLocalRotation(Quaternion.axisAngle(new Vector3(audio / 3.0f, this.temp, audio / 3.0f), 1.0f));
                                node.setLocalScale(new Vector3((float) Math.log(audio) / 2.0f, (float) Math.log(audio) / 2.0f, (float) Math.log(audio) / 2.0f));
                            }

                            // Scooter
                            this.scooterNode.setLocalScale(new Vector3(scale, scale, scale));
                            Vector3 posi = this.scooterNode.getLocalPosition();
                            Box box_scooter = (Box) this.scooterNode.getRenderable().getCollisionShape();
                            posi.x = this.scooterPosition.x + box_scooter.getSize().x / 1000.0f * (accX / 9.0f);
                            this.scooterNode.setLocalPosition(posi);

                            // Flagge
                            this.campFlag.setLocalRotation(Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), magnetometer));

                            // Sonne / Mond
                            // Sonne und Mond Button Interaktion
                            this.campMoon.setLocalScale(new Vector3(scale, scale, scale));
                            this.campSun.setLocalScale(new Vector3(scale, scale, scale));

                            // XOR, wenn buttonA dann Rotation nach links, bei buttonB nach rechts
                            if ( this.buttonA ^ this.buttonB ) {
                                Quaternion qS1 = this.campSun.getLocalRotation();
                                Quaternion qS2 = Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), this.buttonA ? 1.0f : -1.0f);
                                this.campSun.setLocalRotation(Quaternion.multiply(qS1, qS2));

                                Quaternion qM1 = this.campMoon.getLocalRotation();
                                Quaternion qM2 = Quaternion.axisAngle(new Vector3(0.0f, 0.0f, 1.0f), this.buttonA ? 1.0f : -1.0f);
                                this.campMoon.setLocalRotation(Quaternion.multiply(qM1, qM2));
                            }

                            // Berechnung Winkel
                            Quaternion q = this.campSun.getLocalRotation();
                            double siny = 2 * (q.x * q.y + q.w * q.z);
                            double cosy = q.w * q.w + q.x * q.x - q.y * q.y - q.z * q.z;
                            float zAngle = (float) (Math.atan2(siny, cosy) * (180.0f / Math.PI));

                            if ( zAngle < 90.0f && zAngle > -90.0f ) {
                                if ( this.tfCamp.getChildren().contains(this.campMoon) ) {
                                    this.tfCamp.removeChild(this.campMoon);
                                }
                                this.arCoreFragment.getArSceneView().getScene().getSunlight().setEnabled(true);
                                this.fireNode.setLight(null);
                                this.tfCamp.addChild(this.campSun);
                            } else {
                                if ( this.tfCamp.getChildren().contains(this.campSun) ) {
                                    this.tfCamp.removeChild(this.campSun);
                                }
                                this.arCoreFragment.getArSceneView().getScene().getSunlight().setEnabled(false);
                                this.fireNode.setLight(Light.builder(Light.Type.POINT)
                                                            .setColor(new com.google.ar.sceneform.rendering.Color(-863292))
                                                            .setShadowCastingEnabled(true)
                                                            .setIntensity(10.0f)
                                                            .build());
                                this.tfCamp.addChild(this.campMoon);
                            }

                            // Lichteinfall berechnen
                            Vector3 p = this.anchorNodeGlobal.getWorldPosition();
                            float yT = zAngle < 0.0f ? zAngle * -1.0f : zAngle;
                            float yLook = -0.2f + (yT / 450.0f);
                            float xLook = zAngle / 450.0f;
                            this.arCoreFragment.getArSceneView()
                                               .getScene()
                                               .getSunlight()
                                               .setLookDirection(new Vector3(xLook + (this.vecGlobal.x - p.x), yLook, 0.0f + (this.vecGlobal.z - p.z)));
                            //Log.i("TTTT", "t: " + zAngle + " x: " + xLook + " y: " + yLook);

                            /* Sonne und Mond je nach Lichtverhältnissen
                            if (light < 50) {
                                if(tfCamp.getChildren().contains(campSun)) tfCamp.removeChild(campSun);
                                tfCamp.addChild(campMoon);
                            } else {
                                if(tfCamp.getChildren().contains(campMoon)) tfCamp.removeChild(campMoon);
                                tfCamp.addChild(campSun);
                            }

                            campMoon.setLocalScale(new Vector3(scale, scale, scale));
                            campSun.setLocalScale(new Vector3(scale, scale, scale));
                            campMoon.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 0, 1f), light * 1.8f));
                            campSun.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 0, 1f), (light * 1.8f)+180));
                            */
                            this.tfCamp.addChild(this.campNode);
                            this.tfCamp.addChild(this.scooterNode);
                            this.anchorNodeGlobal.addChild(this.tfCamp);
                        }

                /*
                  THERMOMETER
                */

                        if ( this.activeSensors[THERMOMETER_NUMBER] ) {
                            this.removeAllAnchorChildrenExcept(this.tfThermo);

                            if ( this.thermoBaseNode == null ) {
                                this.thermoBaseNode =
                                    ArMethods.createNode(this.tfThermo, this.thermoBaseModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.25f, 0.0f));
                                float scale = ArMethods.calculateScaling(this.augImage, this.thermoBaseNode, SCALE_FACTOR) * 2.0f;
                                this.thermoBaseNode.setLocalScale(new Vector3(scale, scale, scale));

                                this.thermoBubbleNode =
                                    ArMethods.createNode(this.tfThermo, this.thermoBubbleModel, new Vector3(1.0f, 1.0f, 1.0f), new Vector3(0.0f, 0.25f, 0.0f));
                                this.thermoBubbleNode.setLocalScale(new Vector3(scale, scale, scale));

                                this.thermoBarNode =
                                    ArMethods.createNode(this.thermoBubbleNode,
                                                         this.thermoBarModel,
                                                         new Vector3(1.0f, 1.0f, 1.0f),
                                                         new Vector3(0.0f, 0.25f, 0.0f));
                                //thermoBarNode.setLocalScale(new Vector3(scale, scale, scale));

                                this.thermoBarNode.setLocalPosition(new Vector3(0.0f, 2.92748f, 0.029766f));
                            }

                            this.temperature = BLEService.getTemperature();

                            if ( this.currentTemperature != this.temperature ) {

                                // THERMOMETER
                                this.currentTemperature = this.temperature;

                                //float scale = ArMethods.calculateScaling(augImage, thermoBaseNode, SCALE_FACTOR);
                                //thermoBarNode.setLocalScale(new Vector3(scale, (float) (scale * (((1.0 / 17.0) * temp) - (18.0 / 17.0))), scale)); // Nils Rechnung

                                Box boxThermo = (Box) this.thermoBarNode.getRenderable().getCollisionShape();
                                // Y wird durch 35 geteilt, da das Thermometer 35 Temperatur-Markierungen hat
                                this.thermoBarNode.setLocalScale(new Vector3(1.0f, (1.0f / 35.0f) * this.currentTemperature, 1.0f));
                                // Rest zum vollen Balken (35 Markierungen am Thermometer)
                                float rest = 1.0f - (1.0f / 35.0f) * this.currentTemperature;
                                // Anteil am gesamten Balken von Ursprungsposition abgezogen; Geteilt durch 2 da Skalierung nach oben und unten skaliert
                                this.thermoBarNode.setLocalPosition(new Vector3(0.0f, 2.92748f - (boxThermo.getSize().y * (rest / 2.0f)), 0.029766f));

                                // ZIFFERN
                                if ( this.tfThermo.getChildren().contains(this.thermoTempFirstNode) ) {
                                    this.tfThermo.removeChild(this.thermoTempFirstNode);
                                    this.tfThermo.removeChild(this.thermoTempSecondNode);
                                    this.tfThermo.removeChild(this.thermoTempDegreeNode);
                                }

                                int tempFirst = this.currentTemperature;
                                int tempSecond = tempFirst;

                                tempFirst /= 10;
                                tempSecond %= 10;

                                this.thermoTempFirstNode =
                                    ArMethods.createNode(this.tfThermo,
                                                         this.numberModels[tempFirst],
                                                         new Vector3(0.5f, 0.5f, 0.5f),
                                                         new Vector3(-0.1f, 0.03f, 0.0f));

                                this.thermoTempSecondNode =
                                    ArMethods.createNode(this.tfThermo,
                                                         this.numberModels[tempSecond],
                                                         new Vector3(0.5f, 0.5f, 0.5f),
                                                         new Vector3(0.1f, 0.03f, 0.0f));

                                this.thermoTempDegreeNode =
                                    ArMethods.createNode(this.tfThermo, this.numberModels[0], new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.1f, 0.03f, 0.0f));

                                // Skalierung der Ziffern
                                float scale = ArMethods.calculateScaling(this.augImage, this.thermoTempFirstNode, SCALE_FACTOR) / 2.0f;
                                this.thermoTempFirstNode.setLocalScale(new Vector3(scale, scale, scale));
                                this.thermoTempSecondNode.setLocalScale(new Vector3(scale, scale, scale));
                                this.thermoTempDegreeNode.setLocalScale(new Vector3(scale / 3.0f, (scale / 3.0f) / 2.0f, scale / 3.0f));

                                // Berechnung der Positionen
                                Box boxFirst = (Box) this.thermoTempFirstNode.getRenderable().getCollisionShape();
                                Box boxSecond = (Box) this.thermoTempSecondNode.getRenderable().getCollisionShape();
                                float offset = (boxFirst.getSize().x + boxSecond.getSize().x) / 40.0f;
                                float
                                    pos =
                                    (((boxFirst.getSize().x + boxSecond.getSize().x) / 4.0f + offset) * scale)
                                    + 0.1f; // 0.1 um rechts neben Thermometer zu sein
                                this.thermoTempFirstNode.setLocalPosition(new Vector3(pos, 0.7f, 0.0f));
                                this.thermoTempSecondNode.setLocalPosition(new Vector3(pos * 2.0f, 0.7f, 0.0f));
                                // Darstellen des Gradzeichens
                                this.thermoTempDegreeNode.setLocalPosition(new Vector3(
                                    pos * 2.0f + ((boxSecond.getSize().x / 2.0f + offset * 5.0f) * scale),
                                    boxFirst.getSize().y * scale + 0.65f,
                                    0.0f));

                                // Einfärben der Ziffern, je nach Temperatur
                                float rValue;
                                float bValue;
                                final int threshold = 20;

                                if ( this.currentTemperature < threshold ) {
                                    rValue = 0.0f;
                                    bValue = (threshold - this.currentTemperature);
                                } else {
                                    rValue = (-threshold + this.currentTemperature);
                                    bValue = 0.0f;
                                }

                                final float lightUpValue = 0.15f;
                                final float colorScale = 0.1f;
                                ArMethods.colorAsset(
                                    this.thermoTempFirstNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    1.0f);
                                ArMethods.colorAsset(
                                    this.thermoTempSecondNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    0.0f);
                                ArMethods.colorAsset(
                                    this.thermoTempDegreeNode,
                                    (rValue * colorScale) + lightUpValue,
                                    0.0f + lightUpValue,
                                    (bValue * colorScale) + lightUpValue,
                                    0.0f);

                                this.callOut("ES SIND " + this.currentTemperature + " GRAD.");

                                this.tfThermo.addChild(this.thermoTempFirstNode);
                                this.tfThermo.addChild(this.thermoTempSecondNode);
                                this.tfThermo.addChild(this.thermoTempDegreeNode);
                                this.tfThermo.addChild(this.thermoBaseNode);
                                this.tfThermo.addChild(this.thermoBubbleNode);
                                this.anchorNodeGlobal.addChild(this.tfThermo);
                            }
                        }

                        /*
                          BUTTONS AUF CALLIOPE
                        */
                        if ( this.buttonA && !this.activeSensors[CAMP_NUMBER] ) {
                            if ( !this.anchorNodeGlobal.getChildren().contains(this.buttonNodeA) ) {
                                this.buttonNodeA = ArMethods.createNode(null,
                                                                        this.accelerationModel,
                                                                        new Vector3(0.5f, 0.5f, 0.5f),
                                                                        new Vector3(this.augImage.getExtentX() * -0.3f, 0.02f, -0.005f));

                                this.buttonNodeA.setParent(this.anchorNodeGlobal);
                                float scale = ArMethods.calculateScaling(this.augImage, this.buttonNodeA, SCALE_FACTOR) / 10.0f;
                                this.buttonNodeA.setLocalScale(new Vector3(scale, scale, scale));

                                ArMethods.colorAsset(this.buttonNodeA, 0.0f, 0.0f, 1.0f, 0.0f);

                                this.anchorNodeGlobal.addChild(this.buttonNodeA);
                                this.callOut("BLAUES A");
                                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                                // Vibrate for 500 milliseconds
                                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                                    v.vibrate(VibrationEffect.createOneShot(500L, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(500L);
                                }
                            }
                        } else {
                            if ( this.anchorNodeGlobal.getChildren().contains(this.buttonNodeA) ) {
                                this.anchorNodeGlobal.removeChild(this.buttonNodeA);
                                this.buttonNodeA = null;
                            }
                        }
                        if ( this.buttonB && !this.activeSensors[CAMP_NUMBER] ) {
                            if ( !this.anchorNodeGlobal.getChildren().contains(this.buttonNodeB) ) {
                                this.buttonNodeB = ArMethods.createNode(null,
                                                                        this.accelerationModel,
                                                                        new Vector3(0.5f, 0.5f, 0.5f),
                                                                        new Vector3(this.augImage.getExtentX() * 0.3f, 0.02f, -0.005f));

                                this.buttonNodeB.setParent(this.anchorNodeGlobal);
                                float scale = ArMethods.calculateScaling(this.augImage, this.buttonNodeB, SCALE_FACTOR) / 10.0f;
                                this.buttonNodeB.setLocalScale(new Vector3(scale, scale, scale));

                                ArMethods.colorAsset(this.buttonNodeB, 1.0f, 0.0f, 0.0f, 0.0f);

                                this.anchorNodeGlobal.addChild(this.buttonNodeB);
                                this.callOut("ROTES B");
                                Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                                // Vibrate for 500 milliseconds
                                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
                                    v.vibrate(VibrationEffect.createOneShot(500L, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    //deprecated in API 26
                                    v.vibrate(500L);
                                }
                            }

                        } else {
                            if ( this.anchorNodeGlobal.getChildren().contains(this.buttonNodeB) ) {
                                this.anchorNodeGlobal.removeChild(this.buttonNodeB);
                                this.buttonNodeB = null;
                            }
                        }

                    }
                    // Debug-Ausgaben
                    Log.i("onUpdateFrame", "children arcoreFragment: " + this.arCoreFragment.getArSceneView().getScene().getChildren().size());
                    Log.i("onUpdateFrame", "children tfLight: " + this.tfLight.getChildren().size());
                    Log.i("onUpdateFrame", "children tfTemp: " + this.tfTemp.getChildren().size());
                    Log.i("onUpdateFrame", "children tfCompass: " + this.tfCompass.getChildren().size());
                    Log.i("onUpdateFrame", "children tfAcc: " + this.tfAcc.getChildren().size());
                    Log.i("onUpdateFrame", "children tfAudio: " + this.tfAudio.getChildren().size());
                    Log.i("onUpdateFrame", "children tfAudio: " + this.tfCamp.getChildren().size());
                    Log.i("onUpdateFrame", "children tfAudio: " + this.tfThermo.getChildren().size());
                    Log.i("onUpdateFrame", "children anchorNode: " + this.anchorNodeGlobal.getChildren().size());
                    break;

                case STOPPED:
                    // ARCore has stopped tracking this Trackable and will never resume tracking it.
                    this.removeAllAnchorChildrenExcept(null);
                    break;
            }
        }
    }

    /**
     * Entfernt alle außer die übergebene TransformableNode als Kind vom Anker. Dadurch werden die
     * Modelle quasi ausgeblendet. Wird "null" übergeben, so werden jegliche Kinder entfernt (in
     * der Regel sollte der Anker nur ein Kind zur gleichen Zeit haben).
     *
     * @param tfNode Der Knoten, der nicht entfernt werden soll
     */
    private void removeAllAnchorChildrenExcept(TransformableNode tfNode) {
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfTemp) && !this.tfTemp.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfTemp);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfLight) && !this.tfLight.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfLight);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfCompass) && !this.tfCompass.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfCompass);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfAcc) && !this.tfAcc.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfAcc);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfAudio) && !this.tfAudio.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfAudio);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfCamp) && !this.tfCamp.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfCamp);
        }
        if ( this.anchorNodeGlobal.getChildren().contains(this.tfThermo) && !this.tfThermo.equals(tfNode) ) {
            this.anchorNodeGlobal.removeChild(this.tfThermo);
        }
    }

    /**
     * Erstellt einen Anker (Mittig auf dem Calliope), sowie eine TransformableNode für jeden Sensor.
     * Jeder Knoten kann beliebig in Start-, sowie Min- und Max-Größe angepasst werden.
     * Erstellt werden die Objekte in der Klasse "ArMethods".
     *
     * @param image Das erkannte Bild, auf welchem der Anker mittig gesetzt werden soll.
     */
    private void createAnchorAndTfNodes(AugmentedImage image) {
        Anchor anchor = image.createAnchor(image.getCenterPose());
        this.anchorNodeGlobal = new AnchorNode(anchor);
        this.anchorNodeGlobal.setParent(this.arCoreFragment.getArSceneView().getScene());

        this.vecGlobal = this.anchorNodeGlobal.getWorldPosition();

        this.tfLight = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                         this.anchorNodeGlobal,
                                                         true,
                                                         false,
                                                         true,
                                                         0.1f,
                                                         0.6f,
                                                         new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                         new Vector3(0.0f, 0.01f, 0.0f));

        this.tfTemp = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                        this.anchorNodeGlobal,
                                                        true,
                                                        false,
                                                        true,
                                                        0.1f,
                                                        0.6f,
                                                        new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                        new Vector3(0.0f, 0.01f, 0.0f));

        this.tfCompass = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                           this.anchorNodeGlobal,
                                                           true,
                                                           false,
                                                           false,
                                                           0.1f,
                                                           0.6f,
                                                           new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                           new Vector3(0.0f, 0.01f, 0.0f));

        this.tfAcc = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                       this.anchorNodeGlobal,
                                                       true,
                                                       false,
                                                       false,
                                                       0.1f,
                                                       0.6f,
                                                       new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                       new Vector3(0.0f, 0.01f, 0.0f));

        this.tfAudio = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                         this.anchorNodeGlobal,
                                                         true,
                                                         false,
                                                         true,
                                                         0.1f,
                                                         0.6f,
                                                         new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                         new Vector3(0.0f, 0.01f, 0.0f));

        this.tfCamp = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                        this.anchorNodeGlobal,
                                                        true,
                                                        false,
                                                        false,
                                                        0.1f,
                                                        0.6f,
                                                        new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                        new Vector3(0.0f, 0.01f, 0.0f));

        this.tfThermo = ArMethods.createTransformableNode(this.arCoreFragment.getTransformationSystem(),
                                                          this.anchorNodeGlobal,
                                                          true,
                                                          false,
                                                          true,
                                                          0.1f,
                                                          0.6f,
                                                          new Vector3(1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR, 1.0f / SCALE_FACTOR),
                                                          new Vector3(0.0f, 0.01f, 0.0f));

        this.anchorNodeGlobal.addChild(this.tfLight);
        this.anchorNodeGlobal.addChild(this.tfTemp);
        this.anchorNodeGlobal.addChild(this.tfCompass);
        this.anchorNodeGlobal.addChild(this.tfAcc);
        this.anchorNodeGlobal.addChild(this.tfAudio);
        this.anchorNodeGlobal.addChild(this.tfCamp);
        this.anchorNodeGlobal.addChild(this.tfThermo);

    }

    /**
     * Zeigt Navigationsleiste (Home-Button etc.) in der Kamera-Aktivität an.
     */
    private void showSystemUI() {
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    /**
     * Lädt alle Modelle einmalig, um Resourcen zu sparen. Sind alle geladen, wird
     * "hasFinishedLoading" auf true gesetzt und es können 3D-Objekte angezeigt werden.
     */
    private void buildRenderables() {
        CompletableFuture<ModelRenderable> futureSun = ModelRenderable.builder().setSource(this, Uri.parse("models/sun.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureFire =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/fire.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureFlag =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/flag.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureCampSun =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/sun.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureCampMoon =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/moon.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureScooter =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/scooter.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureNote1 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/note1.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureNote2 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/note2.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureNote3 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/note3.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureNote4 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/note4.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureCamp =
            ModelRenderable.builder().setSource(this, Uri.parse("models/camp/camp.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureCompass =
            ModelRenderable.builder().setSource(this, Uri.parse("models/compass.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel0 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/0.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel1 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/1.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel2 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/2.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel3 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/3.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel4 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/4.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel5 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/5.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel6 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/6.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel7 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/7.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel8 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/8.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            numberModel9 =
            ModelRenderable.builder().setSource(this, Uri.parse("models/numbers/9.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            accModel =
            ModelRenderable.builder().setSource(this, Uri.parse("models/arrow_acc.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureAudio =
            ModelRenderable.builder().setSource(this, Uri.parse("models/audio_cube.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureThermoBase =
            ModelRenderable.builder().setSource(this, Uri.parse("models/thermometer/thermometer_base.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureThermoBar =
            ModelRenderable.builder().setSource(this, Uri.parse("models/thermometer/thermometer_bar.glb")).setIsFilamentGltf(true).build();
        CompletableFuture<ModelRenderable>
            futureThermoBubble =
            ModelRenderable.builder().setSource(this, Uri.parse("models/thermometer/thermometer_bubble.glb")).setIsFilamentGltf(true).build();

        CompletableFuture.allOf(futureSun,
                                futureCompass,
                                numberModel0,
                                numberModel1,
                                numberModel2,
                                numberModel3,
                                numberModel4,
                                numberModel5,
                                numberModel6,
                                numberModel7,
                                numberModel8,
                                numberModel9,
                                accModel,
                                futureAudio,
                                futureCamp,
                                futureFire,
                                futureNote1,
                                futureNote2,
                                futureNote3,
                                futureNote4,
                                futureScooter,
                                futureThermoBubble,
                                futureThermoBar,
                                futureThermoBase,
                                futureCampSun,
                                futureCampMoon).handle((notUsed, throwable) -> {
            // When you build a Renderable, Sceneform loads its resources in the background while
            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
            // before calling get().
            if ( throwable != null ) {
                Log.e("buildRenderable", "Exception loading", throwable);
                return null;
            }

            try {
                this.compassModel = futureCompass.get();
                this.numberModels[0] = numberModel0.get();
                this.numberModels[1] = numberModel1.get();
                this.numberModels[2] = numberModel2.get();
                this.numberModels[3] = numberModel3.get();
                this.numberModels[4] = numberModel4.get();
                this.numberModels[5] = numberModel5.get();
                this.numberModels[6] = numberModel6.get();
                this.numberModels[7] = numberModel7.get();
                this.numberModels[8] = numberModel8.get();
                this.numberModels[9] = numberModel9.get();
                this.sunModel = futureSun.get();
                this.accelerationModel = accModel.get();
                this.audioModel = futureAudio.get();
                this.campModel = futureCamp.get();
                this.fireModel = futureFire.get();
                this.notesModels[0] = futureNote1.get();
                this.notesModels[1] = futureNote2.get();
                this.notesModels[2] = futureNote3.get();
                this.notesModels[3] = futureNote4.get();
                this.campFlagModel = futureFlag.get();
                this.campMoonModel = futureCampMoon.get();
                this.campSunModel = futureCampSun.get();
                this.scooterModel = futureScooter.get();
                this.thermoBaseModel = futureThermoBase.get();
                this.thermoBarModel = futureThermoBar.get();
                this.thermoBubbleModel = futureThermoBubble.get();

                // Everything finished loading successfully.
                this.hasFinishedLoading = true;
            } catch ( InterruptedException | ExecutionException ex ) {
                Log.e("buildRenderable", "Exception loading", ex);
            }
            return null;
        });
    }
}
