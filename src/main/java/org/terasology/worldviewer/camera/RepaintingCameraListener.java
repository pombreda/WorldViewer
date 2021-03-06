/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.worldviewer.camera;

import java.awt.Component;

/**
 * Repaints a component when the camera moves
 * or changes zoom.
 * @author Martin Steiger
 */
public class RepaintingCameraListener implements CameraListener {
    private Component comp;

    public RepaintingCameraListener(Component comp) {
        this.comp = comp;
    }

    @Override
    public void onZoomChange() {
        comp.repaint();
    }

    @Override
    public void onPosChange() {
        comp.repaint();
    }
}
