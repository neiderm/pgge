package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.mygdx.game.Components.BulletComponent;

import java.util.Random;

/**
 * Created by mango on 12/18/17.
 * a bullet and libgdx test from
 * "from http://bedroomcoders.co.uk/libgdx-bullet-redux-2/"
 */

public class BulletSystem extends EntitySystem implements EntityListener {

    private static final boolean useDdbugDraw = false;

    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();

    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld collisionWorld;

    private Random rnd = new Random();

    //    private Engine engine;
    private ImmutableArray<Entity> entities;

    private DebugDrawer debugDrawer;
    private PerspectiveCamera camera; // for debug drawing

    public BulletSystem(Engine engine, PerspectiveCamera cam) {

        this.camera = cam;

        Vector3 gravity = new Vector3(0, -9.81f, 0);

//        Bullet.init();
        // Create the bullet world
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        collisionWorld.setGravity(gravity);
        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
        if (useDdbugDraw) {
            collisionWorld.setDebugDrawer(debugDrawer);
        }
    }

    @Override
    public void update(float deltaTime) {

        collisionWorld.stepSimulation(deltaTime /* Gdx.graphics.getDeltaTime() */, 5);

        for (Entity e : entities) {

            // https://gamedev.stackexchange.com/questions/75186/libgdx-draw-bullet-physics-bounding-box
            debugDrawer.begin(camera);
            collisionWorld.debugDrawWorld();
            debugDrawer.end();

            BulletComponent bc = e.getComponent(BulletComponent.class);
            btRigidBody body = bc.body;

            if (null != bc
                    && null != bc.motionstate
                    ) {
                if (true /* body.isActive() */) {
                    bc.motionstate.getWorldTransform(tmpM);
                    tmpM.getTranslation(tmpV);

                    if (tmpV.y < -20) {
// might end up putting this w/ a different system / component ... should be same to get translation from transformation matrix?
                        tmpM.setTranslation(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);
                        body.setWorldTransform(tmpM);
                        body.setAngularVelocity(Vector3.Zero.cpy());
                        body.setLinearVelocity(Vector3.Zero.cpy());
                    }
                }
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

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?

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

                // link to collisionworld for other systems to pass in for ray testin
                bc.collisionWorld = collisionWorld;
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
    }


    /*
     this is here for access to collisionworld
      https://stackoverflow.com/questions/24988852/raycasting-in-libgdx-3d
     */
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();
    private static final ClosestRayResultCallback callback = new ClosestRayResultCallback(rayFrom, rayTo);
    private static final Vector3 outV = new Vector3();

    public static btCollisionObject rayTest(
            btCollisionWorld collisionWorld, Ray ray, float length) {
        rayFrom.set(ray.origin);
        rayTo.set(ray.direction).scl(length).add(rayFrom);

        // we reuse the ClosestRayResultCallback, thus we need to reset its values
        callback.setCollisionObject(null);
        callback.setClosestHitFraction(1f);
        callback.getRayFromWorld(outV);
        outV.set(rayFrom.x, rayFrom.y, rayFrom.z);
        callback.getRayToWorld(outV);
        outV.set(rayTo.x, rayTo.y, rayTo.z);

        collisionWorld.rayTest(rayFrom, rayTo, callback);

        if (callback.hasHit()) {
            return callback.getCollisionObject();
        }

        return null;
    }

}
