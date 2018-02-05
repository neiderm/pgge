package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;

/**
 * Created by mango on 2/4/18.
 */

/*
    ultimately, i should have a camera system that can be a listener on any entity
     (having a transform) to lookat and track.
    for now .... lookat player and track a position relative to it
    Also that camera would have a positin that can be changed perspecitve by the input device .. i.e.
first-person in tank, behind tank, or above.
*/

public class CameraSystem extends EntitySystem implements EntityListener {

    //    private PlayerComponent playerComp;
//    private CameraComponent cameraComp;
    //    private ModelComponent modelComp;

    private ModelInstance camMdlInst;
    private ModelInstance plrMdlInst;

    private PerspectiveCamera cam;

    public CameraSystem(PerspectiveCamera cam) {
        this.cam = cam;
    }

    @Override
    public void entityAdded(Entity entity) {

        //        cameraComp = entity.getComponent(CameraComponent.class);
//        modelComp = entity.getComponent(ModelComponent.class);

        camMdlInst = entity.getComponent(ModelComponent.class).modelInst;

//        m = new Matrix4(modelComp.modelInst.transform);   // doesn't work :(
    }

    @Override
    public void entityRemoved(Entity entity) {

    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(CameraComponent.class).get(), this);
    }

    private Vector3 camVect = new Vector3();
    private Vector3 subVect = new Vector3();
    private Vector3 tmpV = new Vector3();

    @Override
    public void update(float delta) {

        camMdlInst.transform.getTranslation(camVect);

        plrMdlInst.transform.getTranslation(subVect);


        // TODO: target point should be "behind" player
        // take negative of unit vector of players orientation
        Quaternion r = new Quaternion();
        plrMdlInst.transform.getRotation(r);
// hackme ! this is not truly in 3D!
float yaw = r.getYawRad();
float dX = sin(yaw);
float dZ = cos(yaw);
camVect.x += dX * 1f;
camVect.z += dZ * 1f;

        // maintain camera altitude
        subVect.y += 2.0f;


        tmpV = subVect.sub(camVect);

        tmpV.scl(0.1f); // proportional

//        camVect.x += 0.1f;

        camVect.add(tmpV);

        camMdlInst.transform.setTranslation(camVect);

/*
        cam.position.set(camVect);
        cam.lookAt(subVect); //         cam.lookAt(0, -2, -4);
        cam.update();
*/

    }


    public void setSubject(Entity e) {

        plrMdlInst = e.getComponent(ModelComponent.class).modelInst;
    }

}
