package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.mygdx.game.Components.BulletComponent;

import java.util.Random;

/**
 * Created by mango on 12/18/17.
 * a bullet and libgdx test from
 *  "from http://bedroomcoders.co.uk/libgdx-bullet-redux-2/"
 */

public class BulletSystem extends EntitySystem implements EntityListener {

    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();

    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;

    private btDynamicsWorld collisionWorld;


    private Vector3 gravity = new Vector3(0, -9.81f, 0);
    private Random rnd = new Random();

//    private Engine engine;
    private ImmutableArray<Entity> entities;


    public BulletSystem(Engine engine) {

//        Bullet.init();
        // Create the bullet world
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        collisionWorld.setGravity(gravity);
    }

    @Override
    public void update(float deltaTime) {

        collisionWorld.stepSimulation(deltaTime /* Gdx.graphics.getDeltaTime() */, 5);

        for (Entity e : entities) {

            BulletComponent bc = e.getComponent(BulletComponent.class);
            btRigidBody body = bc.body;

            if (null != bc
                    && null != bc.modelInst && null != bc.motionstate
                    && null != bc.scale) // landscape mesh has no scale
            {

                if (body.isActive()) {  // gdx bullet used to leave scaling alone which was rather useful...

                    bc.modelInst.transform.mul(tmpM.setToScaling(bc.scale));

                    bc.motionstate.getWorldTransform(tmpM);
                    tmpM.getTranslation(tmpV);

                    if (tmpV.y < -10) {
                        tmpM.setToTranslation(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);
                        // did idt, so need to scl
                        body.setWorldTransform(tmpM);
                        body.setAngularVelocity(Vector3.Zero.cpy());
                        body.setLinearVelocity(Vector3.Zero.cpy());
                    }
                } else{
                    //bc.modelInst.transform.mul(tmpM.setToScaling(bc.scale)); // why not?
                }
            } else {
                bc = null;
            }
        }
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(BulletComponent.class).get());

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(Family.all(BulletComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        collisionWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfiguration.dispose();


        // tmp ... loop all Bullet entities to destroy resources
        for (Entity e : entities) {

            BulletComponent bc = e.getComponent(BulletComponent.class);

            if (null != bc && null != bc.shape && null != bc.body) {
                bc.shape.dispose();
                bc.body.dispose();
            }
        }
    }

    @Override
    public void entityAdded(Entity entity) {

        BulletComponent bc = entity.getComponent(BulletComponent.class);

        if (null != bc) {
            if (null != bc.body) {
                collisionWorld.addRigidBody(bc.body);
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
