/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fhg.iais.roberta.ar.helper;

import android.app.Activity;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

/**
 * Helper to manage the sample snackbar. Hides the Android boilerplate code, and exposes simpler
 * methods.
 */
public final class SnackbarHelper {
    private static final int BACKGROUND_COLOR = 0xbf323232;
    private static final SnackbarHelper THE_INSTANCE = new SnackbarHelper();
    private Snackbar messageSnackbar = null;

    private enum DismissBehavior {
        HIDE,
        SHOW,
        FINISH
    }

    public static SnackbarHelper getInstance() {
        return THE_INSTANCE;
    }

    public boolean isShowing() {
        return this.messageSnackbar != null;
    }

    /**
     * Shows a snackbar with a given message.
     */
    public void showMessage(Activity activity, String message) {
        this.show(activity, message, DismissBehavior.HIDE);
    }

    /**
     * Shows a snackbar with a given message, and a dismiss button.
     */
    public void showMessageWithDismiss(Activity activity, String message) {
        this.show(activity, message, DismissBehavior.SHOW);
    }

    /**
     * Shows a snackbar with a given error message. When dismissed, will finish the activity. Useful
     * for notifying errors, where no further interaction with the activity is possible.
     */
    public void showError(Activity activity, String errorMessage) {
        this.show(activity, errorMessage, DismissBehavior.FINISH);
    }

    /**
     * Hides the currently showing snackbar, if there is one. Safe to call from any thread. Safe to
     * call even if snackbar is not shown.
     */
    public void hide(Activity activity) {
        activity.runOnUiThread(() -> {
            if ( this.messageSnackbar != null ) {
                this.messageSnackbar.dismiss();
            }
            this.messageSnackbar = null;
        });
    }

    private void show(
        Activity activity, String message, DismissBehavior dismissBehavior) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SnackbarHelper.this.messageSnackbar =
                    Snackbar.make(activity.findViewById(android.R.id.content), message, BaseTransientBottomBar.LENGTH_INDEFINITE);
                SnackbarHelper.this.messageSnackbar.getView().setBackgroundColor(BACKGROUND_COLOR);
                if ( dismissBehavior != DismissBehavior.HIDE ) {
                    SnackbarHelper.this.messageSnackbar.setAction("Dismiss", v -> SnackbarHelper.this.messageSnackbar.dismiss());
                    if ( dismissBehavior == DismissBehavior.FINISH ) {
                        SnackbarHelper.this.messageSnackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                                activity.finish();
                            }
                        });
                    }
                }
                SnackbarHelper.this.messageSnackbar.show();
            }
        });
    }
}
