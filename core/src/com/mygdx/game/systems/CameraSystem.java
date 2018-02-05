package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;

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

    private ModelComponent modelComp;
//    private PlayerComponent playerComp;
//    private CameraComponent cameraComp;

    Matrix4 m = new Matrix4();


    public CameraSystem() {

    }

    @Override
    public void entityAdded(Entity entity) {

        modelComp = entity.getComponent(ModelComponent.class);
//        cameraComp = entity.getComponent(CameraComponent.class);

        m = new Matrix4(modelComp.modelInst.transform);
    }

    @Override
    public void entityRemoved(Entity entity) {

    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(CameraComponent.class).get(), this);
    }

    Vector3 vect = new Vector3();

    @Override
    public void update(float delta) {

        /*
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4, 0); //         cam.lookAt(0, -2, -4);
        cam.update();
*/

        modelComp.modelInst.transform.getTranslation(vect);

//        vect.x += 0.1f;
        modelComp.modelInst.transform.setTranslation(vect);
    }


    Entity subject;

    public void setSubject(Entity e) {
        subject = e;
    }

}
