package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;

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


    public CameraSystem() {
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

        // maintain camera altitude
        subVect.y += 5.0f;

        tmpV = subVect.sub(camVect);

        tmpV.scl(0.1f); // proportional

//        camVect.x += 0.1f;

        camVect.add(tmpV);

        camMdlInst.transform.setTranslation(camVect);

        /*
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4, 0); //         cam.lookAt(0, -2, -4);
        cam.update();
*/

    }


    public void setSubject(Entity e) {

        plrMdlInst = e.getComponent(ModelComponent.class).modelInst;
    }

}
