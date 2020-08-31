package de.fhg.iais.roberta.ar.visualization;

import com.google.android.filament.Engine;
import com.google.android.filament.MaterialInstance;
import com.google.android.filament.RenderableManager;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.collision.Box;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.ar.sceneform.ux.TransformationSystem;

public class ArMethods extends Node {

    /**
     * Erstellt einen normalen Knoten, welcher als Kind einer TransformableNode genutzt wird.
     * Die 3D-Modelle werden mit diesen Knoten erstellt.
     *
     * @param parent          Eine TransformableNode, welche als Elternteil agiert (für Größenveränderung..)
     * @param modelRenderable Das Modell, welches dargestellt werden soll
     * @param scale           Die Skalierung des Objekts
     * @param position        Die Position des Objekts
     * @return Der erstellte Knoten
     */
    public static Node createNode(
        TransformableNode parent, ModelRenderable modelRenderable, Vector3 scale, Vector3 position) {
        Node node = new Node();
        node.setParent(parent);
        node.setRenderable(modelRenderable);
        node.setLocalScale(scale);
        node.setLocalPosition(position);

        return node;
    }

    /**
     * Erstellt einen normalen Knoten, welcher als Kind einer Node genutzt wird.
     * Die 3D-Modelle werden mit diesen Knoten erstellt.
     *
     * @param parent          Eine Node, welche als Elternteil agiert
     * @param modelRenderable Das Modell, welches dargestellt werden soll
     * @param scale           Die Skalierung des Objekts
     * @param position        Die Position des Objekts
     * @return Der erstellte Knoten
     */
    public static Node createNode(
        Node parent, ModelRenderable modelRenderable, Vector3 scale, Vector3 position) {
        Node node = new Node();
        node.setParent(parent);
        node.setRenderable(modelRenderable);
        node.setLocalScale(scale);
        node.setLocalPosition(position);

        return node;
    }

    /**
     * Erstellt eine TransformableNode. Die Skalierung, Position und Rotation einer
     * TransformableNode kann nur vor dem Aufruf von "setParent()" angepasst werden.
     *
     * @param transformationSystem Das Transformationsystem des ArFragments
     * @param parent               Der Elternteil, der dem Knoten zugewiesen werden soll
     * @param allowScaling         Ob Verändern der Größe erlaubt ist
     * @param allowDragging        Ob Verändern der Position erlaubt ist
     * @param allowRotating        Ob Verändern der Rotation erlaubt ist
     * @param minScale             die minimale Skalierung (wenn allowScaling = true)
     * @param maxScale             die maximale Skalierung (wenn allowScaling = true)
     * @param scale                die Startskalierung (für X, Y, Z gleich)
     * @param position             die Position des Knotens
     * @return die erstellte TransformableNode
     */
    public static TransformableNode createTransformableNode(
        TransformationSystem transformationSystem,
        AnchorNode parent,
        boolean allowScaling,
        boolean allowDragging,
        boolean allowRotating,
        float minScale,
        float maxScale,
        Vector3 scale,
        Vector3 position) {

        TransformableNode tfNode = new TransformableNode(transformationSystem);

        tfNode.getTranslationController().setEnabled(allowDragging);
        tfNode.getScaleController().setEnabled(allowScaling);
        tfNode.getRotationController().setEnabled(allowRotating);

        tfNode.getScaleController().setMinScale(minScale);
        tfNode.getScaleController().setMaxScale(maxScale);

        tfNode.setLocalScale(scale);
        tfNode.setLocalPosition(position);

        tfNode.setParent(parent);
        return tfNode;
    }

    /**
     * Berechnet die Skalierung eines Modells in Relation zum erkannten Bild (Calliope)
     *
     * @param image       Das Bild
     * @param node        Der Knoten / Das Objekt das skaliert werden soll
     * @param scaleFactor Der Skalierungsfaktor (z.B. 3 x so groß wie das Image)
     * @return Skalierungswert in Relation zum Bild
     */
    public static float calculateScaling(AugmentedImage image, Node node, float scaleFactor) {

        Box box = (Box) node.getRenderable().getCollisionShape();
        //System.out.println("X: " + box.getSize().x + " Y: " + box.getSize().y+" Z: " + box.getSize().z);
        float maxSizeImage = Math.max(image.getExtentX(), image.getExtentZ());
        float maxSize = 0.0f;
        if ( box != null ) {
            maxSize = Math.max(box.getSize().x, box.getSize().y);
            maxSize = Math.max(box.getSize().z, maxSize);
        }
        if ( maxSize != 0.0f ) {
            return (maxSizeImage / maxSize) * scaleFactor;
        } else {
            return 0.0f;
        }
    }

    /**
     * Färbt Modell ein
     * RGB zwischen 0 und 1
     *
     * @param node  das Modell, welches eingefärbt werden soll
     * @param r     Rot Wert
     * @param g     Grün Wert
     * @param b     Blau Wert
     * @param alpha alpha (?) Wert
     */
    public static void colorAsset(Node node, float r, float g, float b, float alpha) {

        FilamentAsset myAsset = node.getRenderableInstance().getFilamentAsset();
        Engine myEngine = EngineInstance.getEngine().getFilamentEngine();
        if ( myEngine != null ) {
            RenderableManager myRenderableManager = myEngine.getRenderableManager();
            for ( int entity : myAsset.getEntities() ) {
                if ( myRenderableManager.getInstance(entity) == 0 ) {
                    continue;
                }
                //System.out.println("NAME: " + myAsset.getName(entity));

                MaterialInstance matInstance = myRenderableManager.getMaterialInstanceAt(myRenderableManager.getInstance(entity), 0);
                matInstance.setParameter("baseColorFactor", r, g, b, alpha);
            }
        }
    }

}
