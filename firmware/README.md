# Calliope
Dieses Repositorium beinhaltet den Code um alle Sensorwerte des Calliope V1.3 auszulesen und über Bluetooth LE UART zu versenden. 
Nach erfolgreicher Kopplung mit dem Calliope fängt dieser an, die Sensorwerte zu senden.

Kodiert sind die Sensorwerte wie folgt:
* M: Mikrophonelautstärke (Bereich 0-100), 
* T: Temperatur (° C) , 
* L: Lichtintensität (Bereich 0-100 , dunkel -> hell) , 
* C: Grad (Bereich 0 - 360 , (360° = Nord, 90° = Ost, 180° = Süd , 270° = West)), 
* AX: Beschleunigung auf der x-Achse (in millig (Gravitation)) , 
* AY: Beschleunigung auf der y-Achse (in millig), 
* AZ: Beschleunigung auf der z-Achse (in millig), 
* AS: aktuell wirkende Gravitation auf dem Calliope, 
* BA: Button A betätigt (0 falsch, 1 wahr) , 
* BB: Button B betätigt (0 falsch, 1 wahr)

## How to use

###### Installationsanleitung auf Calliope & Smartphone
1. Firmware auf Calliope flashen
   1. Calliope per USB an den PC anschließen
   1. Hex Datei welche unter build/calliope-mini-classic-gcc/source/fh-frankfurt-ss2020-combined.hex zu finden ist auf den Calliope kopieren
   1. Nach erfolgreichem flashen des Calliope leuchtet eine weiße LED
1. Smartphone mit Calliope koppeln
   1. Calliope starten
   1. Button A und Button B drücken
   1. Reset Button kurz antippen
   1. Sobald das Bluetooth Symbol auf dem Bildschirm des Calliope erscheint, Button A und Button B loslassen
   1. Calliope auf dem Smartphone Bluetooth Manager auswählen
   1. Zahlencode (PIN) der auf dem Calliope erscheint notieren und bei der Verbindungsanfrage auf dem Smartphone eingeben
   1. Mit folgender App können die Daten auf dem Smartphone angezeigt werden --> https://play.google.com/store/apps/details?id=de.kai_morich.serial_bluetooth_terminal
   
   
   ## Hardware
   
   <img src="https://github.com/calliope-mini/calliope-demo/blob/master/calliope-mini-v1.0.png" width="350"/>
   
   Version 1.3
 
   
   ## Entwicklungsumgebung
   
| Umgebung | Betriebssystem |
| ------------- |-------------|
| Visual Studio Code | Windows |
| Script, Yotta, Docker  | Linux |
   
###### Windows
1. Installation von python [LINK](https://www.python.org/downloads/)
      * Wichtig während der Installation die Option “add to path” auswählen um die Umgebungsvariable zu setzen.
1. Installtion von CMAKE (Yotta nutzt CMake für den Buildprozess) [LINK](http://www.cmake.org/download/)
      * Wichtig während der Installation die Option “add to path” auswählen um die Umgebungsvariable zu setzen.
1. Installation von Ninja 1.10 [LINK](https://github.com/ninja-build/ninja/releases/download/v1.10.0/ninja-win.zip)
      * Die ninja.exe unter C:\ninja speichern und unter PATH in der Umgebungsvariablen angeben.
1. Installation von GNU Arm Embedded Toolchain [LINK](https://developer.arm.com/-/media/Files/downloads/gnu-rm/9-2020q2/gcc-arm-none-eabi-9-2020-q2-update-win32.exe?revision=50c95fb2-67ca-4df7-929b-55396266b4a1&la=en&hash=DE1CD6E7A15046FD1ADAF828EA4FA82228E682E2)
1. Yotta installieren über Powershell `pip install -U yotta`
1. Installation von SRecord [LINK](http://srecord.sourceforge.net/)
      * SRecord unter C:\srecord speichern und unter PATH in der Umgebungsvariablen angeben. 
1. Installation von Visual Studio Code [LINK](https://code.visualstudio.com/)
1. Repositorium herunterladen und Daten in einem seperaten Ordner entpacken. Anschließend diesen mit VSC öffnen.
1. Terminal in VSC öffnen und target compiler definieren `yt target calliope-mini-classic-gcc`
1. Über den Befehl `yt clean` und `yt build` wird die Firmware gebaut und unter build/calliope-mini-classic-gcc/source/fh-frankfurt-ss2020-combined.hex gespeichert.
* **Wichtig kein `yt update` durchführen ansonsten muss MicroBitConfig.h & MicroBitUARTService.h angepasst werden**


##### Linux (Script)

1. Repositorium der Calliope Projektgruppe herunterladen.
1. Innerhalb des Calliope_WORKING Repositoriums den Ordner "target" anlegen. `mkdir target`
1. Repositorium der OpenRoberta Lab Crosscompiler Resources herunterladen. [LINK](https://github.com/OpenRoberta/ora-cc-rsc)
1. Via Compile Script,welches sich im RobotMbed Ordner befindet, wird das Projekt zusammen mit vorkompilierten statischen Bibliotheken kompiliert.
`./compile.sh /usr/bin/ Main ./Calliope_WORKING/ ./RobotMbed/libs2017/ -b`
* **Wichtig: auf Grund der statischen Bibliotheken ist keine Anpassung der MicroBitConfig.h bzw. MicroBitUARTService.h möglich, ohne diese selbständig neu zu kompilieren. Dies führt je nach Umfang des Projektcodes zu Instabilität der Firmware.
   
##### Linux (yotta)

1. Installation von python, cmake, ninja, arm embedded toolchain, yotta (siehe Windows)
1. Der Kompilierprozess per yotta scheitert mit der Fehlermeldung: `Region RAM overflowed with stack`
   Dies scheint auf die Bibliotheken zurückzuführen, welche der ARM Compiler während des Linking Vorganges verwendet.

##### Linux (docker)
1. Installation von Docker gemäß offizieller Docker Installationsanleitung
1. Im Calliope_WORKING Repositorium muss zunächst das /build/ Verzeichnis geleert werden.
1. Ausführen von `docker run -v "/absoluter/Pfad/experiments-calliope-ar/firmware/:/build/" boonto/yotta:1`. 
   Das Docker Image ist zu finden unter [LINK](https://hub.docker.com/r/boonto/yotta/) 
1. Die erzeugte firmware befindet sich im Verzeichnis /Calliope_WORKING/build/calliope-mini-classic-gcc/source/ und trägt die Bezeichnung fh-frankfurt-ss2020-combined.hex
 
 ## Parametrisierung
 Micro:bit DAL beinhaltet eine Menge an freiparametisierbaren Werten. Die untengenannten Parameter sind speziell für die Firmware angepasst. Eine genaue Aufschlüsselung jedes Parameters ist unter [LINK](https://lancaster-university.github.io/microbit-docs/advanced/#compile-time-options-with-microbitconfigh) zu finden. Zusätzlich muss der UART Buffer [LINK](https://github.com/NWittich/Calliope_WORKING/blob/master/yotta_modules/microbit-dal/inc/bluetooth/MicroBitUARTService.h) '''MICROBIT_UART_S_DEFAULT_BUF_SIZE''' auf 40 erhöht werden.
 ##### config.json
```json 
{
  "microbit-dal": {
    "bluetooth": {
      "enabled": 1,
      "pairing_mode": 1,
      "private_addressing": 0,
      "open": 1,
      "security_level": null,
      "whitelist": 0,
      "advertising_timeout": 0,
      "tx_power": 7,
      "dfu_service": 0,
      "event_service": 0,
      "device_info_service": 1
    },
    "gatt_table_size": "0x700",
    "debug": 0,
    "heap_debug": 0,
    "reuse_sd": 0,
    "default_pullmode": "PullDown",
    "heap_allocator": 0,
    "nested_heap_proportion": 0.5,
    "system_tick_period": 6,
    "system_components": 10,
    "idle_components": 6,
    "use_accel_lsb": 0,
    "min_display_brightness": 1,
    "max_display_brightness": 255,
    "display_scroll_speed": 120,
    "display_scroll_stride": -1,
    "display_print_speed": 400,
    "panic_on_heap_full": 0,
    "stack_size": 2560,
    "sram_base": "0x20000008",
    "sram_end": "0x20004000",
    "sd_limit": "0x20002000"
  }
}
```
