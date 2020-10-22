# Calliope AR Sensor Visualization

This was created as a student project in collaboration with [Nils Wittich](https://github.com/nwittich), [Dominic Gibietz](https://github.com/dogib), Daniel Helmer, two unnamed students and [Prof. Christoph Thomas](https://www.frankfurt-university.de/de/hochschule/fachbereich-2-informatik-und-ingenieurwissenschaften/kontakt/professorinnen-und-professoren-im-fachbereich-2-informatik-und-ingenieurwissenschaften/christoph-thomas/) of [Frankfurt University of Applied Sciences](https://www.frankfurt-university.de/en/).
The goal was to create a real time augmented reality visualization of a Calliope mini's sensor data.

The built files are available as a GitHub Release [here](https://github.com/OpenRoberta/experiments-calliope-ar/releases).
Refer to [Quickstart](#Quickstart) for instructions, an Android version >= 7.0 is required.

The following documentation is mostly in German as it was created as part of the German coursework.

## Firmware
The accompanying Calliope mini firmware and build documentation can be found in the `firmware` subdirectory.

## AR Android App
Dieses Repositorium beinhaltet den Android Code für die Visualisierung der Sensorwerte eines Calliope in Augmented Reality (AR). Die Daten werden via Bluetooth LE UART empfangen und weiterverarbeitet. Die 3D-Objekte werden über dem Calliope dargestellt.  
Für die Nutzung dieser Applikation wird ein AR-fähiges Smartphone von benötigt.  
<img align="right" src="/app/src/main/assets/calliope_pictures/calliope_mini_whitebg.png" width="250">

Folgende Sensoren werden in AR dargestellt:
* Kompass (zeigt stets nach Norden)
* Temperatur (in Celsius) als Zahl und als Thermometer
* Lichtintensität (in Form einer leuchtenen Sonne)
* Mikrofonlautstärke ("Ausschläge" der letzten Töne)
* Beschleunigung (zeigt in Richtung Boden (Erdanziehung))

Temperatur- und Mikrofonvisualisierungen haben zusätzlich noch Sprachausgabe und Vibration (nur Mikrofon). 

Des Weiteren kann eine Camp-Szenerie dargestellt werden, in der alle Sensoren visualisiert worden sind. Die Sensoren sind wie folgt verteilt:
* Fahne die Richtung Norden zeigt (Kompass)
* Größe des Camp-Feuers (Temperatur)
* Sonne-/Mond-Wechsel (Lichtintensität)
* Drehende und skalierende Noten (Lautstärke)
* Fahrendes Mofa beim Kippen nach links und rechts (Beschleunigung)

### Quickstart
1. Google Play-Dienste für AR aus dem Play-Store installieren (siehe [hier](https://play.google.com/store/apps/details?id=com.google.ar.core))
2. Diese Applikation auf dem Smartphone installieren
3. Die aktuelle Calliope-Firmware auf dem Calliope installieren (siehe [hier](/firmware/README.md#how-to-use)) und starten
4. Applikation auf dem Smartphone starten
5. Das rote Bluetooth-Symbol in der oberen rechten Ecke drücken
6. Nach einigen Sekunden sollte automatisch eine Verbindung zum Calliope hergestellt werden (siehe [Bluetooth](#bluetooth-verbindung-herstellen))
7. Den Calliope mit dem Smartphone erfassen 
   - Calliope gerade über den Calliope halten   
   - Wenn der Calliope erkannt wurde, erscheint eine entsprechende Nachricht
8. Beliebig zwischen den Sensoren am unteren Rand wechseln

### <a name="Bluetooth"></a>Bluetooth-Verbindung herstellen
Nach dem Start der Applikation öffnet sich die Kamera mit einem einfachen Overlay. Zunächst sollte eine Bluetooth-Verbindung mit einem Calliope hergestellt werden.  
Um sich mit einem Calliope zu verbinden, auf das rote Bluetooth Symbol oben rechts drücken. Es wird automatisch nach Calliope-Geräten in der Nähe gescannt und das entsprechende Overlay geöffnet. Sollten mehrere Calliope in der Nähe sein, so wird eine Liste mit gefundenen Geräten angezeigt. Wenn nach 5 Sekunden nur ein Gerät gefunden wurde, so wird sich automatisch mit diesem Gerät verbunden, ohne dass der Nutzer etwas drücken muss.  
Das Bluetooth-Symbol ändert die Farbe zu Grün und eine entsprechende Meldung erscheint auf dem Bildschirm, wenn eine Verbindung hergestellt wurde. Geht die Verbindung verloren, wechselt die Farbe auf rot.

### Benutzeroberfläche

<img src="/app/src/main/assets/UI.png" width="120">
In der App können die Visualisierungen der einzelnen Sensoren in der unteren Leiste ausgewählt werden. Es wird immer nur ein Sensor visualisiert. Außnahme sind hier die Sensoren für das Drücken der Knöpfe A und B auf dem Calliope. Diese werden zusätzlich zu jedem Sensor dargestellt, wenn einer der Buttons gedrückt wird. 

### Entwicklungsumgebung
Für die Entwicklung wurde die aktuellste Version von Android Studio genutzt (Version 4.0, Stand 28.06.20). Für das Testen der Applikation mit einem Calliope wird ein AR-fähiges Smartphone benötigt (siehe [hier](https://developers.google.com/ar/discover/supported-devices)). Das Testen in einem Emulator/Virtual Device ist nur möglich, solange keine Bluetooth-Services genutzt werden. Der Emulator muss natürlich auch AR-fähig sein und die AR-Services installiert haben (siehe [hier](https://developers.google.com/ar/develop/java/emulator#update-arcore)).

### Erstellen einer Datenbank mit verschiedenen Calliope Bildern
1. Siehe [Google Developer Beschreibung](https://developers.google.com/ar/develop/c/augmented-images/arcoreimg#create_image_database_from_a_directory_of_images)
2. Datei in Ordner app/src/main/assets speichern
3. Sicherstellen, dass USE_SINGLE_IMAGE auf false steht (CustomArFragment - Zeile 30)
4. Gegebenenfalls Namen der Datenbank anpassen (CustomArFragment - Zeile 26)  

Es wird empfohlen möglichst wenig Bilder in der Datenbank einzufügen, da sonst mehrere 3D-Objekte inneinander erscheinen könnten.
 



