/*
 * Copyright (c) 2019 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mygdx.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btIDebugDraw;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.features.FeatureAdaptor;

// ref:
//   https://github.com/xoppa/blog/blob/master/tutorials/src/com/xoppa/blog/libgdx/g3d/bullet/dynamics/
//   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Contact-callbacks#contact-filtering

public class BulletWorld implements Disposable {

    private static BulletWorld instance = null;

    public static boolean USE_DDBUG_DRAW = false;
    private DebugDrawer debugDrawer;
    private btCollisionConfiguration collisionConfiguration;
    private btCollisionDispatcher dispatcher;
    private btBroadphaseInterface broadphase;
    private btConstraintSolver solver;
    private btDynamicsWorld collisionWorld;
    private Camera camera; // for debug drawing

    public Array<Object> userToEntityLUT;
    private MyContactListener mcl;

    class MyContactListener extends ContactListener {

        @Override
        public void onContactStarted(btCollisionObject colObj0, btCollisionObject colObj1) {

            int userValue0, userValue1;
            userValue0 = colObj0.getUserValue();
            userValue1 = colObj1.getUserValue();

            Entity ee;
            int lutSize = userToEntityLUT.size;

            if ( null != colObj0 /*userValue0 > 0*/) {

                if (userValue0 < lutSize) {

                    ee = (Entity) userToEntityLUT.get(userValue0);

                    if (null != ee) {

                        BulletComponent bc = ee.getComponent(BulletComponent.class);// tmp?

                        if (null != bc) {

                            FeatureComponent comp = ee.getComponent(FeatureComponent.class);

                            if (null != comp) {
                                FeatureAdaptor fa = comp.featureAdpt;

                                if (null != fa) {
                                    if (null != fa.collisionProcessor){
                                        fa.collisionProcessor.onCollision(ee);
                                    }
                                }
                            }
                        } else { // no longer has bullet comp, can be ignored
                            Gdx.app.log("onContactEnded", "no Bullet Comp (0)");
                        }
                    }
                }
            }

            if (null != colObj1 /*userValue1 > 0*/) {

                 if (userValue1 < lutSize) {

                    ee = (Entity) userToEntityLUT.get(userValue1);

                    if (null != ee) {
                        // collision processors typically result in desctruction which may likely be bullet comp being removed
                        BulletComponent bc = ee.getComponent(BulletComponent.class);// tmp?

                        if (null != bc) {

                            FeatureComponent comp = ee.getComponent(FeatureComponent.class);

                            if (null != comp) {
                                FeatureAdaptor fa = comp.featureAdpt;

                                if (null != fa) {
                                    if (null != fa.collisionProcessor){
                                        fa.collisionProcessor.onCollision(ee);
                                    }
                                }
                            }
                        } else {
                            Gdx.app.log("onContactEnded", "no Bullet Comp (1)");
                        }
                    }
                }
            }
//            return true;
        }
    }

    private BulletWorld() {
        //        throw new GdxRuntimeException("not allowed, use bulletWorld = BulletWorld.getInstance() ");
    }

    public static BulletWorld getInstance() {

        if (null == instance) {
//            Bullet.init();
            instance = new BulletWorld();
        }
        return instance;
    }

    public void initialize(Camera camera) {

        if (null != collisionWorld) {
            Gdx.app.log("BulletWorld", "(collisionWorld != null)");
        }

        userToEntityLUT = new Array<Object>();
        userToEntityLUT.add(null); // make sure that valid entry will have index non-zero so we can ensure non-zero userValue on Contact

        rayResultCallback = new ClosestRayResultCallback(rayFrom, rayTo);

        // Create the bullet world
        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphase = new btDbvtBroadphase(); //  new btAxisSweep3 ?
        solver = new btSequentialImpulseConstraintSolver();
        collisionWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
        collisionWorld.setGravity(new Vector3(0, -9.81f, 0));
        mcl = new MyContactListener();

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

        btCollisionObjectArray objs = collisionWorld.getCollisionObjectArray();

        for (int i = 0; i < objs.size(); i++) {
            btCollisionObject body = objs.at(i);
            if (body instanceof btRigidBody) {
                Gdx.app.log("Bulletwrld:dispose()", "btRigidBody has NOT been dispose() !!!!!");
            }
        }
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
        if (!GameWorld.getInstance().getIsPaused()) {

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
