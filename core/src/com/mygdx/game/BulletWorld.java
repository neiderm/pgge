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
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btManifoldPoint;
import com.badlogic.gdx.physics.bullet.collision.btTriangleInfoMap;
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

    private Array<Object> userToEntityLUT;
    private Array<btTriangleInfoMap> savedTriangleInfoMapRefs;

    private MyContactListener mcl;         // don't delete me!~!!!!!!


    class MyContactListener extends ContactListener {

        /*
I sure am glad other people figured out the thing with collision normals and edge contacts:
 https://hub.jmonkeyengine.org/t/physics-shapes-collide-on-flat-surfaces-triangle-boundaries/35946/2
 https://github.com/libgdx/libgdx/issues/2534
 https://stackoverflow.com/questions/26805651/find-a-way-of-fixing-wrong-collision-normals-in-edge-collisions
 https://stackoverflow.com/questions/25605659/avoid-ground-collision-with-bullet

 https://stackoverflow.com/questions/26805651/find-a-way-of-fixing-wrong-collision-normals-in-edge-collisions
 https://gist.github.com/FyiurAmron/3fa2d6b55fd96e50a6cce93f5859f680
 */
        @Override
//        public boolean onContactAdded(btManifoldPoint cp, btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
        public boolean onContactAdded(btManifoldPoint cp,
                                      btCollisionObjectWrapper colObj0Wrap, int partId0, int index0, boolean match0,
                                      btCollisionObjectWrapper colObj1Wrap, int partId1, int index1, boolean match1) {
///*
            btCollisionObject co0 = colObj0Wrap.getCollisionObject();
            btCollisionObject co1 = colObj1Wrap.getCollisionObject();
            int uv0 = co0.getUserValue();
            int uv1 = co1.getUserValue();

            if (uv0 != 0 /* match0 */ ) {
                if ( 0 != (OBJECT_FLAG | co1.getContactCallbackFlag())
                        && 0 != (GROUND_FLAG | co1.getContactCallbackFilter()) ) {
                    // match0, but do nothing ... collision filtering setup to notify on object->ground
                    // collision, whereas it needs to just handle the edge contact filtering for the terrain ...
                }
            }
// . ..... so it seems then the terrain will be found in the other side of the collision point so apparently
//        here is all the handling that needs done for edge filtering - go ahead and check for the ground flag anyway
            if ( /*uv1 != 0*/ 0 != (GROUND_FLAG | co1.getContactCallbackFlag()) ) {
//            btCollisionShape cs1 = co1.getCollisionShape();
//                if (cs1.className.equals("btBvhTriangleMeshShape"))
                Collision.btAdjustInternalEdgeContacts(cp, colObj1Wrap, colObj0Wrap, partId1, index1 /* int normalAdjustFlags */);
            }

            return true;
        }

        @Override
        public void onContactStarted(
                btCollisionObject colObj0, boolean match0, btCollisionObject colObj1, boolean match1) {

            int userValue0 = colObj0.getUserValue();
            int userValue1 = colObj1.getUserValue();

            Entity ee0 = null;
            Entity ee1 = null;

            if (userValue0 < userToEntityLUT.size) {
                ee0 = (Entity) userToEntityLUT.get(userValue0);
            }

            if (userValue1 < userToEntityLUT.size) {
                ee1 = (Entity) userToEntityLUT.get(userValue1);
            }

            if (match0 && null != ee0) {

                BulletComponent bc = ee0.getComponent(BulletComponent.class);// tmp?

                if (null != bc) { //  getting the bc is rather useless

                    FeatureComponent comp = ee0.getComponent(FeatureComponent.class);

                    if (null != comp) {
                        FeatureAdaptor fa = comp.featureAdpt;

                        if (null != fa) {
                            if (null != fa.collisionProcessor){
                                fa.collisionProcessor.onCollision(ee1);
                            }
                        }
                    }
                } else { // no longer has bullet comp, can be ignored
                    Gdx.app.log("onContactEnded", "no Bullet Comp (0)");
                }
            }

            if (match1 && null != ee1) {
                // collision processors typically result in desctruction which may likely be bullet comp being removed
                BulletComponent bc = ee1.getComponent(BulletComponent.class);// tmp?

                if (null != bc) {

                    FeatureComponent comp = ee1.getComponent(FeatureComponent.class);

                    if (null != comp) {
                        FeatureAdaptor fa = comp.featureAdpt;

                        if (null != fa) {
                            if (null != fa.collisionProcessor){
                                fa.collisionProcessor.onCollision(ee0);
                            }
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

    public void initialize() {

        if (null != collisionWorld) {
            Gdx.app.log("BulletWorld", "(collisionWorld != null)");
        }

        savedTriangleInfoMapRefs = new Array<btTriangleInfoMap>();

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
    }


    public Entity getCollisionEntity(int userValue0){
        return  (Entity) userToEntityLUT.get(userValue0);
    }

    // Note:
    //            is dispose'd by BulletSystem
    @Override
    public void dispose() {

        for ( btTriangleInfoMap infoMap : savedTriangleInfoMapRefs){

            infoMap.dispose();
        }

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
    }

    public void update(float deltaTime, Camera camera) {

        update(deltaTime);

        // https://gamedev.stackexchange.com/questions/75186/libgdx-draw-bullet-physics-bounding-box
        debugDrawer.begin(camera);
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

    public ClosestRayResultCallback rayResultCallback(Ray ray, float length) {

        rayFrom.set(ray.origin);
        rayTo.set(ray.direction).scl(length).add(rayFrom);

        // we reuse the ClosestRayResultCallback, thus we need to reset its values
        rayResultCallback.setCollisionObject(null);
        rayResultCallback.setClosestHitFraction(1f);

        collisionWorld.rayTest(rayFrom, rayTo, rayResultCallback);

        return rayResultCallback;
    }

    private btCollisionObject rayTest(Ray ray, float length) {

        rayFrom.set(ray.origin);
        rayTo.set(ray.direction).scl(length).add(rayFrom);

        // we reuse the ClosestRayResultCallback, thus we need to reset its values
        rayResultCallback.setCollisionObject(null);
        rayResultCallback.setClosestHitFraction(1f);
//        rayResultCallback.getRayFromWorld(outV);
//        outV.set(rayFrom.x, rayFrom.y, rayFrom.z);
//        rayResultCallback.getRayToWorld(outV);
//        outV.set(rayTo.x, rayTo.y, rayTo.z);

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

    /*
     * for static "terrain" mesh only right now, should n't need to be removed "dynamicallyt"
     */
    public void addTriangleInfoMap(btTriangleInfoMap tim) {

        savedTriangleInfoMapRefs.add(tim);
    }



    // try collision flags
    //    https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part1/
    public static final short GROUND_FLAG = 1<<8;
    public static final short OBJECT_FLAG = 1<<9;
    public static final short NONE_FLAG = 0;

    /*
     * setup Bullet body to handle collision notification and map entity to lookup by userValue of collision notifcaitob
     * "onContactAdded callback will only be triggered if at least one of the two colliding bodies has the CF_CUSTOM_MATERIAL_CALLBACK set"
     *  (ref https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Contact-callbacks#contact-filtering)
     */
    public void addBodyWithCollisionNotif(Entity ee){

        addBodyWithCollisionNotif(ee, OBJECT_FLAG, GROUND_FLAG);
    }

    public void addBodyWithCollisionNotif(Entity ee,
                                          int flag, int filter){

        BulletComponent bc = ee.getComponent(BulletComponent.class);

        if (null != bc) {

            btCollisionObject body = bc.body;
            if (null != body) {
                // set collision flags and map entities to int index
                int next = userToEntityLUT.size;
                body.setUserValue(next);
                body.setCollisionFlags(
                        body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                userToEntityLUT.add(ee); // what if e (body) removed?

// ground ->object collision
//                    body.setContactCallbackFlag(GROUND_FLAG);
//                    body.setContactCallbackFilter(0);
// object->ground collision
                //body.setContactCallbackFlag(OBJECT_FLAG);
                // body.setContactCallbackFilter(GROUND_FLAG);

// Developer mode flag to turn off Contact callback filtering  for debug pps????????
///*
                body.setContactCallbackFlag(flag);
                body.setContactCallbackFilter(filter);
//*/
            }
        }
    }
}
