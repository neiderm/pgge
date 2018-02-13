package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ArrayMap;
import com.mygdx.game.Components.CameraComponent;

import static com.mygdx.game.systems.CameraSystem.CameraOpMode.CHASE;
import static com.mygdx.game.systems.CameraSystem.CameraOpMode.FIXED_PERSPECTIVE;

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

public class CameraSystem extends EntitySystem implements EntityListener {

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

    public static final int FIXED = 1; // idfk

    public static class CameraNode{
        private Matrix4 positionRef;
        private Matrix4 lookAtRef;
        private int flags; // e.g. FIXED_PERSPECTIVE

        public CameraNode(Matrix4 positionRef, Matrix4 lookAtRef){
            this(0, positionRef, lookAtRef);
        }

        public CameraNode(int flags, Matrix4 positionRef, Matrix4 lookAtRef){
            this.flags = flags;
            this.positionRef = positionRef;
            this.lookAtRef = lookAtRef;
        }
    }

    private ArrayMap<String, CameraNode> cameraNodes = new ArrayMap<String, CameraNode>(String.class, CameraNode.class);

    public void addCameraNode(String key, CameraNode cameraNode){
        cameraNodes.put(key, cameraNode);
    }

    public void setCameraNode(String key, CameraNode cameraNode) {
        int index = cameraNodes.indexOfKey(key);
        if (-1 != index) {
            cameraNodes.setValue(index, cameraNode);
        }
        else addCameraNode(key, cameraNode);
    }

    // remove camera node



    private CameraOpMode cameraOpMode = FIXED_PERSPECTIVE;

    // these reference whatever the camera is supposed to be chasing
    private Matrix4 positionMatrixRef;
    private Matrix4 lookAtMatrixRef;

    private static int nodeIndex = 0;


    /*
    pass these in as matrix, so we don't care if they are stationary points, or "live" characters transforms
     */
    public boolean nextOpMode(Matrix4 positionTransform, Matrix4 lookAtTransform) {

        boolean isController = false;

        if (++nodeIndex >= cameraNodes.size) {
            nodeIndex = 0;
        }

        CameraNode node = cameraNodes.getValueAt(nodeIndex);

        cameraOpMode = CHASE;

        if (node.flags == FIXED){

            cameraOpMode = FIXED_PERSPECTIVE;

            Vector3 lookAtV = new Vector3();
            Vector3 positionV = new Vector3();

            lookAtTransform.getTranslation(lookAtV);
            positionTransform.getTranslation(positionV);

            setCameraLocation(positionV, lookAtV);

// we only set the fixed camera once, so it may not matter to save these values
            node.positionRef.set(positionTransform);
            node.lookAtRef.set(lookAtTransform);

            isController = true;
        }

        positionMatrixRef = node.positionRef;
        lookAtMatrixRef = node.lookAtRef;

        return isController;
    }


    public void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();
    }

    public CameraSystem(PerspectiveCamera cam) {

        this.cam = cam;

        // set some fixed default
        setCameraLocation(new Vector3(3, 7, 10), new Vector3(0, 4, 0));


// values will get overwritten ... this may be temporary
//        positionMatrixRef = new Matrix4();
//        positionMatrixRef.setToTranslation(position);
//        lookAtMatrixRef = new Matrix4();
//        lookAtMatrixRef.setToTranslation(lookAt);
    }

    @Override
    public void entityAdded(Entity entity) {
    }

    @Override
    public void entityRemoved(Entity entity) {
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(CameraComponent.class).get(), this);
    }


    private Vector3 tmpPpositionV = new Vector3();
    private Vector3 tmpLookAtV = new Vector3();

    @Override
    public void update(float delta) {

        if (FIXED_PERSPECTIVE == cameraOpMode) {

        } else if (CHASE == cameraOpMode) {
            positionMatrixRef.getTranslation(tmpPpositionV);
            lookAtMatrixRef.getTranslation(tmpLookAtV);
            setCameraLocation(tmpPpositionV, tmpLookAtV);
        }
    }
}
