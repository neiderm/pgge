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
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Disposable;
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

        callback = new ClosestRayResultCallback(rayFrom, rayTo);

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

        instance = null;
    }

    public void update(float deltaTime) {

        collisionWorld.stepSimulation(deltaTime /* Gdx.graphics.getDeltaTime() */, 5);

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
    private static ClosestRayResultCallback callback;
    private static final Vector3 outV = new Vector3();


    private static btCollisionObject rayTest(btCollisionWorld collisionWorld, Ray ray, float length) {

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

    private Ray ray = new Ray();

    public btCollisionObject rayTest(Vector3 origin, Vector3 direction, float length) {

        return rayTest(this.collisionWorld, ray.set(origin, direction), length);
    }

    public void addBody(btRigidBody body) {

        collisionWorld.addRigidBody(body);
    }

    public void removeBody(btRigidBody body) {

        collisionWorld.removeRigidBody(body);
    }
}
