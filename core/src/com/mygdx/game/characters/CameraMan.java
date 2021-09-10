/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.TrackerSB;

/**
 * Created by neiderm on 2/4/18.
 */
public class CameraMan extends SteeringEntity {

    private final PerspectiveCamera cam;
    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);

    // tmp variables
    private final Vector3 tmpPosition = new Vector3();
    private final Vector3 tmpLookAt = new Vector3();

    private CameraOpMode cameraOpMode;
    private int nodeIndex = 0;

    /*
     * Something on SO is apparently relevant ... or was?
     *   https://stackoverflow.com/questions/17664445/is-there-an-increment-operator-for-java-enum/17664546
     */
    public enum CameraOpMode {
        CHASE_FAR,
        CHASE_CLOSE,
        FIXED_PERSPECTIVE
        // TODO: fixed+lookAT (only implementes the facing part of seek behavior i.e. doesn' update position
                // FP_PERSPECTIVE,
                // ABOVE
                {
                    @Override
                    public CameraOpMode getNext() {
                        return values()[0]; // rollover to the first
                    }
                };

        public CameraOpMode getNext() {
            return this.ordinal() < CameraOpMode.values().length - 1
                    ? CameraOpMode.values()[this.ordinal() + 1]
                    : CameraOpMode.values()[0];
        }
    }

    private static class CameraNode {

        final Matrix4 positionRef;
        final Matrix4 lookAtRef;
        final CameraOpMode opMode; // e.g. FIXED_PERSPECTIVE
        final SteeringBehavior sb; // Node holds its own instance of SB and when selected passes a reference to it to the Steerable ("this") at each mode switch

        CameraNode(CameraOpMode opMode, Matrix4 positionRef, Matrix4 lookAtRef, SteeringBehavior sb) {
            this.opMode = opMode;
            this.positionRef = positionRef;
            this.lookAtRef = lookAtRef;
            this.sb = sb;
        }
        Matrix4 getPositionRef() {
            return positionRef;
        }
        Matrix4 getLookAtRef() {
            return lookAtRef;
        }
    }

    private final ArrayMap<String, CameraNode> cameraNodes =
            new ArrayMap<String, CameraNode>(String.class, CameraNode.class);


    private void setCameraNode(
            String key, Matrix4 lookAtM, CameraOpMode opMode, Vector3 spOffs) {

        Matrix4 camPosition = new Matrix4();
        SteeringBehavior sb = null;

        if (null != lookAtM && null != spOffs) {
            // reminder to myself, when pass "this" ("owner:"), we can override stuff in SteerableAdapter, meaining supply overridden implementations of getPosition() etc. etc.
            sb = new TrackerSB<Vector3>(this, lookAtM, camPosition, spOffs);
        }
        CameraNode cameraNode = new CameraNode(opMode, camPosition, lookAtM, sb);

        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode); // already exists so set it
        } else {
            cameraNodes.put(key, cameraNode); // insert a new one
        }
    }

    public boolean nextOpMode() {
        int index = nodeIndex;
        if (++index >= cameraNodes.size) {
            index = 0;
        }
        return setOpModeByIndex(index);
    }

    private boolean setOpModeByIndex(int index) {

        nodeIndex = index;

        boolean isController = false;
        CameraNode node = cameraNodes.getValueAt(index);
        cameraOpMode = node.opMode;

        if (node.opMode == CameraOpMode.CHASE_CLOSE || node.opMode == CameraOpMode.CHASE_FAR){
            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!

            if (null != node.sb) { // ????? how to fix this warning??
                setSteeringBehavior(node.sb);
            }
            node.getPositionRef().setToTranslation(cam.position);
        }
        else // if (node.opMode == CameraOpMode.FIXED_PERSPECTIVE)
        {
// offset the cam position on y axis. LookAt doesn't change so grab it from the previous node.
//            Vector3 tmp = new Vector3(cam.position.x, cam.position.y + 1, cam.position.z);
//            setCameraLocation(tmp, prevNode.getLookAtRef().getTranslation(tmpLookAt));
            setCameraLocation(camDefPosition, camDefLookAt);
            isController = true;
        }
        return isController;
    }

    private void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();
    }

    public CameraMan(PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV, Matrix4 tgtTransfrm){

        this.cam = cam;
        setCameraLocation(positionV, lookAtV);
/*
 note: next will cycle thro the modes in order added here. Also, IMPORTANT: camTransform doesn't
 have to be a new instance for each node, but maybe it might need to be in some unforerseen circumstance?
*/
        setCameraNode("chaser1", tgtTransfrm, CameraOpMode.CHASE_FAR, new Vector3(0, 1, 2.5f));
        setCameraNode("chaser2", tgtTransfrm, CameraOpMode.CHASE_CLOSE, new Vector3(0, 1, 1.5f));
        setCameraNode("fixed", null, CameraOpMode.FIXED_PERSPECTIVE, null);

        setOpModeByIndex(cameraNodes.indexOfKey("chaser1"));
    }

    @Override
    protected void applySteering(SteeringAcceleration<Vector3> steering, float deltaTime) {

        CameraNode node = cameraNodes.getValueAt(nodeIndex);

        if (CameraOpMode.CHASE_CLOSE == cameraOpMode || CameraOpMode.CHASE_FAR == cameraOpMode) {

            setCameraLocation(
                    node.getPositionRef().getTranslation(tmpPosition),
                    node.getLookAtRef().getTranslation(tmpLookAt));
        }
        // else if (CameraOpMode.FIXED_PERSPECTIVE == cameraOpMode) {}
    }
}
