package de.fhg.iais.roberta.ar.visualization;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import de.fhg.iais.roberta.ar.helper.SnackbarHelper;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.IOException;
import java.io.InputStream;

public class CustomArFragment extends ArFragment {

    private static final String TAG = "CustomArFragment";

    // Name of the image in the sample database.
    // Image is located in the assets directory (app/src/main/assets)
    private static final String DEFAULT_IMAGE_NAME = "calliope_pictures/calliope_mini_bright.png";

    // Pre-created database containing the sample image.
    private static final String SAMPLE_IMAGE_DATABASE = "calliope_db_normal.imgdb";

    // Augmented image configuration and rendering.
    // Load a single image (true) or a pre-generated image database (false).
    private static final boolean USE_SINGLE_IMAGE = false;

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);

        // Use setFocusMode to configure auto-focus.
        config.setFocusMode(Config.FocusMode.AUTO);

        if ( !this.setupAugmentedImageDatabase(config, session) ) {
            SnackbarHelper.getInstance().showError(this.getActivity(), "Could not setup augmented image database");
        }
        return config;
    }

    /**
     * Je nach Einstellung von USE_SINGLE_IMAGE wird eine neue Bilddatenbank mit einem Foto
     * (DEFAULT_IMAGE_NAME) erstellt oder eine gegebene Bilddatenbank (SAMPLE_IMAGE_DATABASE)
     * genutzt. Die App lädt deutlich schneller wenn eine vorhandene Bilderdatenbank genutzt wird.
     * Außerdem müssen dann keine Bilder in die .apk gepackt werden, was eine kleinere .apk Datei
     * ermöglicht.
     *
     * @param config  config
     * @param session session
     * @return true für erfolgreiches Aufsetzen der Datenbank, false bei Fehlern
     */
    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;

        AssetManager assetManager = this.getContext() != null ? this.getContext().getAssets() : null;
        if ( assetManager == null ) {
            Log.e(TAG, "Context is null, cannot intitialize image database.");
            return false;
        }

        // Einzelnes Bild, mit Erstellung von Datenbank
        if ( USE_SINGLE_IMAGE ) {
            Bitmap augmentedImageBitmap = loadAugmentedImageBitmap(assetManager);
            if ( augmentedImageBitmap == null ) {
                return false;
            }
            augmentedImageDatabase = new AugmentedImageDatabase(session);
            augmentedImageDatabase.addImage(DEFAULT_IMAGE_NAME, augmentedImageBitmap);
        } else {
            // Nutzen einer vorhandenen Datenbank
            try (InputStream is = this.getContext().getAssets().open(SAMPLE_IMAGE_DATABASE)) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch ( IOException e ) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private static Bitmap loadAugmentedImageBitmap(AssetManager assetManager) {
        try (InputStream is = assetManager.open(DEFAULT_IMAGE_NAME)) {
            return BitmapFactory.decodeStream(is);
        } catch ( IOException e ) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }
}
