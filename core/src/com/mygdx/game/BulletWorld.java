package com.mygdx.game;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.Bullet;
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


public class BulletWorld implements Disposable {

    public static BulletWorld instance = null;

    private static final boolean USE_DDBUG_DRAW = false;
    private DebugDrawer debugDrawer;
    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld collisionWorld;

    private Camera camera; // for debug drawing

/*
    private BulletWorld() {
//        throw new GdxRuntimeException("not allowed, use bulletWorld = BulletWorld.getInstance() ");
    }
*/
    public static BulletWorld getInstance(Camera camera) {

        if (null == instance) {

            Bullet.init();
            instance = new BulletWorld();
            instance.init();
            instance.camera = camera;
        }
        return instance;

    }

    private void init() {

            callback = new ClosestRayResultCallback(rayFrom, rayTo);

            final Vector3 gravity = new Vector3(0, -9.81f, 0);

            // Create the bullet world
            collisionConfiguration = new btDefaultCollisionConfiguration();
            dispatcher = new btCollisionDispatcher(collisionConfiguration);
            broadphase = new btDbvtBroadphase();
            solver = new btSequentialImpulseConstraintSolver();
            collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
            collisionWorld.setGravity(gravity);

            debugDrawer = new DebugDrawer();
            debugDrawer.setDebugMode(btIDebugDraw.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);

            if (USE_DDBUG_DRAW) {
                collisionWorld.setDebugDrawer(debugDrawer);
            }
    }



    @Override
    public void dispose() {

        collisionWorld.dispose();
        solver.dispose();
        broadphase.dispose();
        dispatcher.dispose();
        collisionConfiguration.dispose();
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
    private static  ClosestRayResultCallback callback = null; // = new ClosestRayResultCallback(rayFrom, rayTo);
    private static final Vector3 outV = new Vector3();


    public static btCollisionObject rayTest(btCollisionWorld collisionWorld, Ray ray, float length) {

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


    /*
    make sure player is "upright" - this is so we don't apply motive force if e.g.
    rolled over falling etc. or otherwise not in contact with some kind of
    "tractionable" surface (would it belong in it's own system?)
     */
    private Ray ray = new Ray();
    private Vector3 axis = new Vector3();

    public boolean surfaceContact(Quaternion bodyOrientation, Vector3 origin, Vector3 direction ) {

        btCollisionObject rayPickObject;

        // get quat from world transfrom ... or not? seems equivalent to body.getOrientation()
//        bodyWorldTransform.getRotation(bodyOrientation);
// bodyOrientation = plyrPhysBody.getOrientation()

//        Vector3 down = playerComp.down; // tmp: globalize this value for debuggery
        direction.set(0, -1, 0);
        float rad = bodyOrientation.getAxisAngleRad(axis);
        direction.rotateRad(axis, rad);

        ray.set(origin, direction);
        // 1 meters max from the origin seems to work pretty good
        rayPickObject = this.rayTest(this.collisionWorld, ray, 1f);

        return (null != rayPickObject);
    }

    public void addBody(btRigidBody body) {

        collisionWorld.addRigidBody(body);
    }

    public void removeBody(btRigidBody body) {

        collisionWorld.removeRigidBody(body);
    }

}
