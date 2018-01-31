package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.screens.MainMenuScreen;

/**
 * Created by mango on 1/23/18.
 */

public class PlayerSystem extends EntitySystem implements EntityListener {

    private static final float forceScl = 0.2f; // rolling sphere
//    static private final float forceScl = 0.7f; // box

    // create a "braking" force ... ground/landscape is not dynamic and doesn't provide friction!
    private static final float vLossLin = -1.9f; // -0.5f;     HA this is kinda like coef of friction!

//    static private final float vLossAng = -5.0f;


    private Engine engine;
    public Entity playerEntity;
    private PlayerComponent playerComp;

    private MyGdxGame game;


    public PlayerSystem(MyGdxGame game) {
        this.game = game;
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        engine.addEntityListener(Family.all(PlayerComponent.class).get(), this);
    }

    @Override
    public void update(float delta) {

        ModelInstance inst = playerEntity.getComponent(ModelComponent.class).modelInst;

        Matrix4 tmpM = new Matrix4();

        BulletComponent bc = playerEntity.getComponent(BulletComponent.class);
        btRigidBody body = bc.body;

// should only apply force if linear velocity less than some limit!
        float force = forceScl * playerComp.mass;
        body.applyCentralForce(playerComp.vvv.scl(force));

// my negative linear force is great for rolling, but should not apply while FALLING!
// need to simulate friction (function of velocity?) when collision detected
        // always apply loss of energy (torque negative of vA, linear negative of vL, fraction of mass)
        // (only works for sphere if mostly in surface contact, not if falling any significant distance)
//        body.applyTorque(body.getAngularVelocity().scl(vLossAng * mass)); // freaks out if angular scale factor > ~11 ???
        body.applyCentralForce(body.getLinearVelocity().scl(vLossLin * playerComp.mass));

// for dynamic object you should get world trans directly from rigid body!
        body.getWorldTransform(tmpM); // body.getWorldTransform(inst.transform);

        Vector3 trans = new Vector3();
        tmpM.getTranslation(trans); // inst.transform.getTranslation(trans);

        if (trans.y < -20) {
            game.setScreen(new MainMenuScreen(game)); // TODO: status.alive = false ...
        }


        // let's rotate the "tank" by a constant rate
        // eventually, will rotate @ constant rate while stick left or stick right.
        // note: rotation in model space - rotate around the Z (need to fix model export-orientation!)

        tmpM.rotate(0, 0, 1, 1); // does not touch translation ;)
        body.setWorldTransform(tmpM);
    }


    @Override
    public void entityAdded(Entity entity) {

//        if (null != entity.getComponent(PlayerComponent.class))
        {
            playerEntity = entity;

            playerComp = entity.getComponent(PlayerComponent.class);
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
