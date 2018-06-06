package com.mygdx.game.util;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;


/**
 * Created by mango on 2/4/18.
 * <p>
 * - work on avoiding jitter experienced when subject is basically still:
 * need the actor/subject to provide its velocity and don't move camera until
 * minimum amount of velocity exceeded
 * <p>
 * Can camera have a kinematic body to keep it from going thru dynamic bodies, but yet that
 * wouldn't exert any forces on other bodies?
 * <p>
 * <p>
 * ultimately, i should have a camera system that can be a listener on any entity
 * (having a transform) to lookat and track.
 * for now .... lookat player and track a position relative to it
 * <p>
 * LATEST IDEA:
 * multiple camera system instances and types ...
 * <p>
 * <p>
 * Perspective type, most basic
 * <p>
 * Chase type would be constructed with a reference to the chasee
 */

public class CameraOperator {

    private PerspectiveCamera cam;

    // https://stackoverflow.com/questions/17664445/is-there-an-increment-operator-for-java-enum/17664546
    public enum CameraOpMode {
        FIXED_PERSPECTIVE,
        CHASE
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

    private static final int FIXED = 1; // idfk

    private static class CameraNode{
        private Matrix4 positionRef;
        private Matrix4 lookAtRef;
        private int flags; // e.g. FIXED_PERSPECTIVE

        public CameraNode(Matrix4 positionRef, Matrix4 lookAtRef){
            this(FIXED, positionRef, lookAtRef);
        }

        public CameraNode(int flags, Matrix4 positionRef, Matrix4 lookAtRef){
            this.flags = flags;
            this.positionRef = positionRef;
            this.lookAtRef = lookAtRef;
        }
    }

    private ArrayMap<String, CameraNode> cameraNodes = new ArrayMap<String, CameraNode>(String.class, CameraNode.class);

    public void setCameraNode(String key, Matrix4 posM, Matrix4 lookAtM) {

        setCameraNode(key, posM, lookAtM, 0);
    }

    public void setCameraNode(String key, Matrix4 posM, Matrix4 lookAtM, int flags) {

        CameraNode cameraNode = new CameraNode(flags, posM, lookAtM);

        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode);
        } else
            cameraNodes.put(key, cameraNode);
    }

    // remove camera node



    private CameraOpMode cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;

    // these reference whatever the camera is supposed to be chasing
    private Matrix4 positionMatrixRef;
    private Matrix4 lookAtMatrixRef;

    private static int nodeIndex = 0;


    /*
     */
    public boolean setOpModeByKey(String key) {
        int index = cameraNodes.indexOfKey(key);

        if (index > -1) {

            return setOpModeByIndex(index);
        } else {
             // we're doomed ... idfk
        }
        return false;
    }


    public boolean nextOpMode() {

        if (++nodeIndex >= cameraNodes.size) {
            nodeIndex = 0;
        }
        return setOpModeByIndex(nodeIndex);
    }

    boolean setOpModeByIndex(int index){

        boolean isController = false;

        CameraNode node = cameraNodes.getValueAt(index);

        cameraOpMode = CameraOpMode.CHASE;

        Vector3 tmp = cam.position.cpy();

        if (node.flags == FIXED){

            cameraOpMode = CameraOpMode.FIXED_PERSPECTIVE;

            tmp.y += 1;
            setCameraLocation(tmp, currentLookAtV);

            isController = true;

            // they way it's setup right now, these don't matter for fixed camera
            positionMatrixRef = null;
            lookAtMatrixRef = null;

        } else {

            // set working refs to the selected node
            positionMatrixRef = node.positionRef;
            lookAtMatrixRef = node.lookAtRef;

            // set the target node to previous camera position, allowing it to zoom in from wherever it was fixed to
            // this would be nicer if we could "un-stiffen" the control gain during this zoom!

            positionMatrixRef.setToTranslation(tmp);
        }

        return isController;
    }


    public void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();

        currentPositionV.set(position);
        currentLookAtV.set(lookAt);
    }

    public CameraOperator(PerspectiveCamera cam, Vector3 pos, Vector3 lookAt) {

        this.cam = cam;

        Vector3 posV = new Vector3(pos);
        Vector3 lookAtV = new Vector3(lookAt);
// we don't really use the transform matrix for fixed camera
//        Matrix4 pos = new Matrix4();
//        Matrix4 look = new Matrix4();
//        pos.setToTranslation(posV);
//        look.setToTranslation(lookAtV);
//        setCameraNode("fixed", pos, look, FIXED);
        setCameraNode("fixed", null, null, FIXED);
        setCameraLocation(posV, lookAtV);
    }


    private Vector3 currentPositionV = new Vector3();
    private Vector3 currentLookAtV = new Vector3();

    public void update(float delta) {

        if (CameraOpMode.FIXED_PERSPECTIVE == cameraOpMode) {

        } else if (CameraOpMode.CHASE == cameraOpMode) {
            positionMatrixRef.getTranslation(currentPositionV);
            lookAtMatrixRef.getTranslation(currentLookAtV);
            setCameraLocation(currentPositionV, currentLookAtV);
        }
    }
}
