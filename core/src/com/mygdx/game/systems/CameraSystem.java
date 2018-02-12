package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
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
 * Also that camera would have a positin that can be changed perspecitve by the input device .. i.e.
 * first-person in tank, behind tank, or above.
 * <p>
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


    private CameraOpMode cameraOpMode = FIXED_PERSPECTIVE;

    private Vector3 camSavePerspPos = new Vector3();
    private Vector3 camSavePerspDir = new Vector3();

    // these reference whatever the camera is supposed to be chasing
    private Matrix4 chasePositionRef;
    private Matrix4 chaseLookAtRef;


    public boolean nextOpMode() {

        boolean isController = false;

        if (FIXED_PERSPECTIVE == cameraOpMode) {
            saveCameraLocation(); // save settings before changing
        }

        cameraOpMode = cameraOpMode.getNext();

        if (FIXED_PERSPECTIVE == cameraOpMode) {
            setCameraLocation(camSavePerspPos, camSavePerspDir); // load last settings
            isController = true;
        }
        return isController;
    }

    public void setCameraLocation(Vector3 position, Vector3 lookAt) {

        cam.position.set(position);
        cam.lookAt(lookAt);
        cam.up.set(0, 1, 0); // googling ... Beginning Java Game Development with LibGDX ... lookAt may have undesired result of tilting camera left or right
        cam.update();
    }

    private void saveCameraLocation() {

        camSavePerspPos.set(cam.position);
        camSavePerspDir.set(cam.direction); // direction is cam.lookAt()
    }


    public CameraSystem(PerspectiveCamera cam) {
        this.cam = cam;

        // save the location/vector of the perspective camera so we can switch back to it
        saveCameraLocation();
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


    private Vector3 pos = new Vector3();
    private Vector3 lookAt = new Vector3();

    @Override
    public void update(float delta) {

        if (FIXED_PERSPECTIVE == cameraOpMode) {

        } else if (CHASE == cameraOpMode) {

            chasePositionRef.getTranslation(pos);
            chaseLookAtRef.getTranslation(lookAt);
            setCameraLocation(pos, lookAt);
        }
    }


    public void setChaser(Matrix4 positionRef, Matrix4 lookAtRef) {

        chasePositionRef = positionRef;
        chaseLookAtRef = lookAtRef;
    }
}
