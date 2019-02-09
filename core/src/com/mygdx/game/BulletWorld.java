package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.screens.GameWorld;
//import com.badlogic.gdx.utils.GdxRuntimeException;


public class BulletWorld implements Disposable {

    private static BulletWorld instance = null;

    private static final boolean USE_DDBUG_DRAW = false;
    private DebugDrawer debugDrawer;
    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld collisionWorld;
    private Camera camera; // for debug drawing

//    /*
        private BulletWorld() {
    //        throw new GdxRuntimeException("not allowed, use bulletWorld = BulletWorld.getInstance() ");
    }
//    */
    public static BulletWorld getInstance() {

        if (null == instance) {
//            Bullet.init();
            instance = new BulletWorld();
        }
        return instance;
    }

    public void initialize(Camera camera) {

        if (null != collisionWorld){
            Gdx.app.log("BulletWorld", "(collisionWorld != null)");
        }

        rayResultCallback = new ClosestRayResultCallback(rayFrom, rayTo);

        // Create the bullet world
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        collisionWorld.setGravity(new Vector3(0, -9.81f, 0));

        debugDrawer = new DebugDrawer();
        debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

        if (USE_DDBUG_DRAW) {
            collisionWorld.setDebugDrawer(debugDrawer);
        }
        instance.camera = camera;
    }


    // Note:
    //            is dispose'd by BulletSystem
    @Override
    public void dispose() {

        collisionWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfiguration.dispose();
        debugDrawer.dispose();

        collisionWorld = null;
        collisionConfiguration = null;

        rayResultCallback.dispose();

        instance = null;
    }

    public void update(float deltaTime) {

            // I let it pause itself, lets the gamescreen render() slightly less cluttery
        if ( ! GameWorld.getInstance().getIsPaused()) {

            collisionWorld.stepSimulation(deltaTime /* Gdx.graphics.getDeltaTime() */, 5);
        }
        // https://gamedev.stackexchange.com/questions/75186/libgdx-draw-bullet-physics-bounding-box
        debugDrawer.begin(this.camera);
        collisionWorld.debugDrawWorld();
        debugDrawer.end();
    }

    /*
     this is here for access to collisionworld
      https://stackoverflow.com/questions/24988852/raycasting-in-libgdx-3d
     */
    private static final Vector3 rayFrom = new Vector3();
    private static final Vector3 rayTo = new Vector3();
    private static ClosestRayResultCallback rayResultCallback;
    private static final Vector3 outV = new Vector3();


    private btCollisionObject rayTest(Ray ray, float length) {

        rayFrom.set(ray.origin);
        rayTo.set(ray.direction).scl(length).add(rayFrom);

        // we reuse the ClosestRayResultCallback, thus we need to reset its values
        rayResultCallback.setCollisionObject(null);
        rayResultCallback.setClosestHitFraction(1f);
        rayResultCallback.getRayFromWorld(outV);
        outV.set(rayFrom.x, rayFrom.y, rayFrom.z);
        rayResultCallback.getRayToWorld(outV);
        outV.set(rayTo.x, rayTo.y, rayTo.z);

        collisionWorld.rayTest(rayFrom, rayTo, rayResultCallback);

        if (rayResultCallback.hasHit()) {
            return rayResultCallback.getCollisionObject();
        }
        return null;
    }

    private Ray ray = new Ray();

    public btCollisionObject rayTest(Vector3 origin, Vector3 direction, float length) {

        return rayTest(ray.set(origin, direction), length);
    }

    public void addBody(btRigidBody body) {

        collisionWorld.addRigidBody(body);
    }

    public void removeBody(btRigidBody body) {

        collisionWorld.removeRigidBody(body);
    }
}
