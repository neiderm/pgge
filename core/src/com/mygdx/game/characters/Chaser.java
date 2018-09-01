package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.util.PrimitivesBuilder;


/*
    A character object that tracks the given "node" ... a position (transform) plus offset for the
    PID control to track. So if it is handed the the world transform of the camera, it makes for a
    crude camera controller. "Plant" output is simply an offset displacement added to present position
    The sphere is just eye candy.

    This is really nothing more than a trivial example. Once there is a proper "non-bullet" Steerable available
    then this can be the Character for the cameraman.
*/

public class Chaser implements IGameCharacter  {

    private SteeringEntity character;


    public Chaser(){ /* empty */ }
    

    public Entity create(Matrix4 tgtTransform) {

        float r = 0.5f;
        Entity e = PrimitivesBuilder.loadSphere(r, new Vector3(0, 15f, -5f));

        ModelInstance instance = e.getComponent(ModelComponent.class).modelInst;

        instance.userData = 0xaa55;

        final Vector3 spOffs = new Vector3(0, 1, 2);

//        e.add(new ControllerComponent( new PIDcontrol(tgtTransform, instance.transform, spOffs, 0.1f, 0, 0)));

        e.add(new CharacterComponent(this, null /* tmp? */));

        // don't really need a bullet component but it needs the transform from the body ... so be it
        e.add(new BulletComponent(new btSphereShape(r), new Matrix4(), 0.f));

        this.character = new SteeringEntity();

        character.setSteeringBehavior(new TrackerSB<Vector3>(character, tgtTransform, instance.transform, spOffs));

        return e;
    }

    @Override
    public void update(Entity entity, float deltaTime, Object whatever /* comp.lookRay */) {

        this.character.update(deltaTime);
    }
}
