package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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

    static private final float forceScl = 0.2f;
    static private final float vLossLin = -0.5f;
    static private final float vLossAng = -5.0f;

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

    public void update(float delta) {

        ModelInstance inst = playerEntity.getComponent(ModelComponent.class).modelInst;

        float mass = playerComp.mass;
        btRigidBody body = playerEntity.getComponent(BulletComponent.class).body;

if (true) {
    body.applyCentralForce(playerComp.vvv.scl(forceScl * mass));

    // always apply loss of energy (torque negative of vA, linear negative of vL, fraction of mass)
//    body.applyTorque(body.getAngularVelocity().scl(vLossAng * mass)); // freaks out if angular scale factor > ~11 ???
//    body.applyCentralForce(body.getLinearVelocity().scl(vLossLin * mass));
}

        body.getWorldTransform(inst.transform);

        Vector3 trans = new Vector3();
        inst.transform.getTranslation(trans);

        if (trans.y < -20) {
            game.setScreen(new MainMenuScreen(game)); // TODO: status.alive = false ...
        }
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
