package com.mygdx.game.characters;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.controllers.SteeringEntity;

/**
 * Created by neiderm on 2/4/18.
 * <p>
 * - work on avoiding jitter experienced when subject is basically still:
 * need the actor/subject to provide its velocity and don't move camera until
 * minimum amount of velocity exceeded
 * <p>
 * Can camera have a kinematic body to keep it from going thru dynamic bodies, but yet that
 * wouldn't exert any forces on other bodies?
 * <p>
 * LATEST IDEA:
 * multiple camera system instances and types ...
 */

public class CameraMan extends SteeringEntity {

    private PerspectiveCamera cam;
    private CameraOpMode cameraOpMode;
    private int nodeIndex = 0;

    // tmp variables
    private Vector3 tmpPosition = new Vector3();
    private Vector3 tmpLookAt = new Vector3();

    private Matrix4 tgtTransfrm;
    private Matrix4 camTransform;


    // https://stackoverflow.com/questions/17664445/is-there-an-increment-operator-for-java-enum/17664546
    public enum CameraOpMode {
        FIXED_PERSPECTIVE,
        CHASE_CLOSE,
        CHASE_FAR
// TODO: fixed+lookAT (only implementes the facing part of seek behavior i.e. doesn' update position
                // FP_PERSPECTIVE,
                // FOLLOW
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

        Matrix4 positionRef;
        Matrix4 lookAtRef;
        CameraOpMode opMode; // e.g. FIXED_PERSPECTIVE

        CameraNode(CameraOpMode  opMode, Matrix4 positionRef, Matrix4 lookAtRef) {
            this.opMode = opMode;
            this.positionRef = positionRef;
            this.lookAtRef = lookAtRef;
        }

        Matrix4 getPositionRef() {
            return positionRef;
        }

        Matrix4 getLookAtRef() {
            return lookAtRef;
        }
    }

    private ArrayMap<String, CameraNode> cameraNodes =
            new ArrayMap<String, CameraNode>(String.class, CameraNode.class);


    private void setCameraNode(String key, Matrix4 posM, Matrix4 lookAtM, CameraOpMode  flags) {

        CameraNode cameraNode = new CameraNode(flags, posM, lookAtM);

        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode);
        } else
            cameraNodes.put(key, cameraNode);


        setOpModeByKey("chaser1");
    }

    private boolean setOpModeByKey(String key) {
        int index = cameraNodes.indexOfKey(key);
        if (index > -1) {
            return setOpModeByIndex(index);
        }
        return false;
    }

    public boolean nextOpMode() {
        int index = nodeIndex;
        if (++index >= cameraNodes.size) {
            index = 0;
        }
        return setOpModeByIndex(index);
    }

    private boolean setOpModeByIndex(int index) {

        CameraNode prevNode = cameraNodes.getValueAt(nodeIndex);
        nodeIndex = index;

        boolean isController = false;
        CameraNode node = cameraNodes.getValueAt(index);
        cameraOpMode = node.opMode;
        Vector3 tmp = cam.position.cpy();

        if (node.opMode == CameraOpMode.CHASE_CLOSE){
            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!
            node.getPositionRef().setToTranslation(tmp);

            setSteeringBehavior(new TrackerSB<Vector3>(this, tgtTransfrm, camTransform, /*spOffs*/new Vector3(0, 1, 2.5f)));
        }
        if (node.opMode == CameraOpMode.CHASE_FAR){
            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!
            node.getPositionRef().setToTranslation(tmp);

            setSteeringBehavior(new TrackerSB<Vector3>(this, tgtTransfrm, camTransform, /*spOffs*/new Vector3(0, 1, 3f)));
        }
        else // if (node.opMode == CameraOpMode.FIXED_PERSPECTIVE)
        {
// offset the cam position on y axis. LookAt doesn't change so grab it from the previous node.
            tmp.y += 1;
            setCameraLocation(tmp, prevNode.getLookAtRef().getTranslation(tmpLookAt));

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

    public CameraMan(PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV) {

        this.cam = cam;
        setCameraLocation(positionV, lookAtV);
    }

    public CameraMan(final PerspectiveCamera cam, Vector3 positionV, Vector3 lookAtV, Matrix4 tgtTransfrm){

        this(cam, positionV, lookAtV);

        camTransform = new Matrix4();

        this.tgtTransfrm = tgtTransfrm; // make sure set this before setCameraNode() !!

// note: when node is set, it also gets its OP Mode set, so last one set will be the op Mode !
//        setCameraNode("fixed", null, null, CameraOpMode.FIXED_PERSPECTIVE); // don't need transform matrix for fixed camera
        setCameraNode("chaser1", camTransform, tgtTransfrm, CameraOpMode.CHASE_FAR);
        setCameraNode("chaser1", camTransform, tgtTransfrm, CameraOpMode.CHASE_CLOSE);
        //        setOpModeByKey("chaser1");

//        setSteeringBehavior(new TrackerSB<Vector3>(this, tgtTransfrm, camTransform, /*spOffs*/new Vector3(0, 1, 2.5f)));
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
