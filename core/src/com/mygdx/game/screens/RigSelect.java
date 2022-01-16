/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mygdx.game.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.ModelComponent;

class RigSelect {

    private static final float PLATFORM_START_PT_Y = -50.0f;//tbd
    private static final float PLATFORM_END_PT_Y = -0.1f;

    private final Vector3 tmpRigVec3 = new Vector3();
    private final Vector3 down = new Vector3();
    private final Vector3 platformPositionVec = new Vector3(); // tmp vector for reuse

    private ImmutableArray<Entity> characters;
    private Entity platform;
    private int actorCount;
    private int idxRigSelection;
    private float degreesInst; // instantaneous commanded rotation of platform
    private float degreesSetp; // demanded rotation of platform
    private float degreesStep; // magnitude of control output (can be ramp rate limited by control algo)

    RigSelect(Entity platform, ImmutableArray<Entity> characters) {

        this.actorCount = characters.size();
        this.characters = characters;
        this.platform = platform;

        degreesSetp = 90; // 90 - idxRigSelection * platformIncDegrees()
        degreesInst = degreesSetp; // no movement at screen start
        degreesInst = 0; // does 90 degree rotation

        Matrix4 pTransform = platform.getComponent(ModelComponent.class).modelInst.transform;
        pTransform.setToTranslation(new Vector3(0, PLATFORM_START_PT_Y, 0));
    }

    int getSelectedIndex() {
        return idxRigSelection;
    }

    /**
     * Size of 1 platform sector in degrees
     *
     * @return sector size in degrees
     */
    private float platformSectorDegrees() {
        return (360.0f / actorCount);
    }

    /*
     * steps to the current orientation by applying proportional control with ramp-up
     */
    private void updateRotation() {

        float kP = 0.2f;
        float step = (degreesSetp - degreesInst) * kP; // step = error * kP

        if (Math.abs(step) > 0.01f) { // deadband around control point, Q-A-D lock to the setpoint and suppress ringing, normally done by integral term
            if (Math.abs(degreesStep) < 2) { // output is ramped up from 0 to this value, after which 100% of step is accepted
                int sign = (degreesStep < 0) ? -1 : 1;
                degreesStep += 0.1f * sign;
            }
            degreesInst += step;
        } else {
            degreesInst = degreesSetp;
            degreesStep = 0;
        }
    }

    /*
     * platformDegrees: currently commanded (absolute) orientation of platform
     */
    void updatePlatformRotation(int step) {
        // fixed amount to get the model pointing toward the viewer when selected
        final int ROTATE_FORWARD_ANGLE = 90;
        // Rigs are positioned in terms of offset from Platform origin
        // Platform height is buried in Selectscreen unfortunately ("PlayerIsPlatform")
        final float PLATFORM_HEIGHT = 0.2f;
        final float SELECTED_RIG_OFFS_Y = 0.3f;
        final float UNSELECTED_RIG_OFFS_Y = 0.05f;
        // scalar applies to x/y (cos/sin) terms to "push" the Rigs slightly out from the origin
        final float RIG_PLACEMENT_RADIUS_SCALAR = 1.1f;

        float platformDegrees = degreesInst;

        updateRotation();

        for (int n = 0; n < actorCount; n++) {
            // angular offset of unit to position it relative to platform
            float positionDegrees = platformSectorDegrees() * n;
            // final rotation of unit is Platform Degrees plus angular rotation to orient unit relative to platform
            float orientionDegrees = positionDegrees - platformDegrees - ROTATE_FORWARD_ANGLE;

            // add Platform Degrees to the unit angular position on platform
            double rads = Math.toRadians(positionDegrees + platformDegrees); // distribute number of rigs around a circle
            tmpRigVec3.x = (float) Math.cos(rads) * RIG_PLACEMENT_RADIUS_SCALAR;
            tmpRigVec3.y = (PLATFORM_HEIGHT / 2) + UNSELECTED_RIG_OFFS_Y; // raise slightly above platform
            tmpRigVec3.z = (float) Math.sin(rads) * RIG_PLACEMENT_RADIUS_SCALAR;

            Entity e = characters.get(n);
            Matrix4 transform = e.getComponent(ModelComponent.class).modelInst.transform;
            transform.setToTranslation(0, 0, 0);
            transform.setToRotation(down.set(0, 1, 0), positionDegrees + orientionDegrees);
            transform.trn(tmpRigVec3);

            if (idxRigSelection == n) { // raise selected for arbitrary effect ;)
                transform.trn(down.set(0, SELECTED_RIG_OFFS_Y, 0));
            }
        }
        Matrix4 transform = platform.getComponent(ModelComponent.class).modelInst.transform;
        transform.setToRotation(down.set(0, 1, 0), 360 - platformDegrees);
        transform.trn(0, PLATFORM_END_PT_Y, 0);

        idxRigSelection = checkedUpDown(step, idxRigSelection);
        // Necessary to increment the degrees because we are controlling to it like a setpoint
        // rotating past 360 must not wrap around to o0, it must go to e.g. 480, 600 etc. maybe this is wonky)
        degreesSetp -= platformSectorDegrees() * step;   // negated (matches to left/right of object nearest to front of view)
    }

    // based on InGameMenu. checkedUpDown()
    private int checkedUpDown(int step, int checkedIndex) {

        int selectedIndex = checkedIndex;
        selectedIndex += step;

        if (selectedIndex >= actorCount) {
            selectedIndex = 0;
        } else if (selectedIndex < 0) {
            selectedIndex = actorCount - 1;
        }
        return selectedIndex;
    }

    /**
     * update position of object
     *
     * @return boolean true if platform moved, otherwise false (i.e. in position)
     */
    boolean updatePlatformPosition() { // update

        Matrix4 pTransform = platform.getComponent(ModelComponent.class).modelInst.transform;
        pTransform.getTranslation(platformPositionVec);
        float error;

        if (platformPositionVec.y < PLATFORM_END_PT_Y) {
            // swipe-in the logo text block ...
            error = PLATFORM_END_PT_Y - platformPositionVec.y;
            final float kPplatform = 0.10f;
            // if present position is Less Than (going up ... )
            if ((error > kPplatform) && (platformPositionVec.y < PLATFORM_END_PT_Y)) {
                platformPositionVec.y = platformPositionVec.y + (error * kPplatform);
            } else {
                // lock title text in place ...
                platformPositionVec.y = PLATFORM_END_PT_Y;
            }
            pTransform.setToTranslation(platformPositionVec);

            return true;
        }
        return false;
    }
}
