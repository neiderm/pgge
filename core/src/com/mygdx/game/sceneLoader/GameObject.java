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

package com.mygdx.game.sceneLoader;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

public class GameObject {

    public GameObject() {

        this.isShadowed = true;
    }

//    public GameObject(String objectName, String shapeName) {
//        this.objectName = objectName;
//        this.meshShape = shapeName;
//        this.isShadowed = true;
////        this.isKinematic = true;
//        this.isPickable = false;
//        this.scale = new Vector3(1, 1, 1); // placeholder
//    }


    private Array<InstanceData> instanceData = new Array<InstanceData>();

    public String objectName;
    private String featureName; // if Entity is to be part of a feature
    //            Vector3 translation; // needs to be only per-instance
    public Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
    public float mass;
    public String meshShape; // triangleMeshShape, convexHullShape
    boolean isKinematic;  //  "isStatic" ?
    private boolean isPickable;
    public boolean isShadowed;
    boolean iSWhatever;
    boolean isCharacter;


    public Array<InstanceData> getInstanceData() {

        return instanceData;
    }


    /*
     * searching the group model for the given gameObject.objectName* ...
     * may not be super efficient and  ... increasing number of model nodes ???
     * However walking the model is needed for globbed object name, not
     * seeing a more efficient way right now.
     */

    void buildNodes(Engine engine, Model model) {

        buildNodes(engine, model, null, false);
    }

    public void buildNodes(Engine engine, Model model, Vector3 translation, boolean useLocalTranslation) {

        /* load all nodes from model that match /objectName.*/
        for (Node node : model.nodes) {

            String unGlobbedObjectName = objectName.replaceAll("\\*$", "");

            if (node.id.contains(unGlobbedObjectName)) {

                // specified node ID means this object is loaded from mondo scene model (where everything should be either static or kinematic )
                ModelInstance mi = ModelInstanceEx.getModelInstance(model, node.id, scale);

                if (null != translation) {
                    mi.transform.setTranslation(0, 0, 0); // set trans only (absolute)
                    mi.transform.trn(translation);   // set trans only (offset)
                }

                if (useLocalTranslation) {
                    mi.transform.trn(node.localTransform.getTranslation(new Vector3()));
                }

                btCollisionShape shape = null;

                // TODO find another way to get shape - depends on the instance which is bass-ackwards
                // shouldn't need a new shape for each instace - geometery scale etc. belongs to gameObject
                if (null != meshShape) {
                    BoundingBox boundingBox = new BoundingBox();
                    Vector3 dimensions = new Vector3();
                    mi.calculateBoundingBox(boundingBox);

                    shape = PrimitivesBuilder.getShape(
                            meshShape, boundingBox.getDimensions(dimensions), node); // instance.getNode(node.id),
                }
        /*
        scale is in parent object (not instances) because object should be able to share same bullet shape!
        HOWEVER ... seeing below that bullet comp is made with mesh, we still have duplicated meshes ;... :(
         */
                buildGameObject(model, engine, mi, shape);
            } // else  ... bail out if matched an un-globbed name ?
        }
    }

    // gameObject.build() ?      NOTE : copies the passed "instance" ... so caller should discard the reference
    void buildGameObject(
            Model model, Engine engine, ModelInstance modelInst, btCollisionShape btcs) {

        InstanceData id = new InstanceData();
        int n = 0;

        String playerFeatureName = null;
        Entity playerFeatureEntity = null;

        GameFeature playerFeature = GameWorld.getInstance().getFeature("Player"); // assumes already loaded Characters group ;)

        if (null != playerFeature) {
            playerFeatureName = playerFeature.featureName;
            playerFeatureEntity = playerFeature.getEntity();
        }

        do { // for (InstanceData i : gameObject.instanceData) ... but not, because game objects may have no instance data

            if (instanceData.size > 0) {
                id = instanceData.get(n++);
            }

            btCollisionShape shape = null;
            if (isKinematic || mass > 0) { // note does not use the gamObject.meshSHape name

                shape = btcs; // note: 1 shape re-used
            }

            Entity e = buildObjectInstance(modelInst.copy(), shape, id);
            engine.addEntity(e);

            ModelComponent mc = e.getComponent(ModelComponent.class);
            mc.model = model;  // bah

            Vector3 position = new Vector3();
            position = mc.modelInst.transform.getTranslation(position);

            FeatureAdaptor adaptor = null;

            if (null != id.adaptr) {

                adaptor = id.adaptr.makeFeatureAdapter(position, playerFeatureEntity); // needs the origin location ... might as well send in the entire instance transform

                // for now, assign Entity ref to bullet body userValue (only for feature entity right now)
                BulletComponent bc = e.getComponent(BulletComponent.class);

                if (null != bc) {
                    btCollisionObject body = bc.body;
                    if (null != body) {
                        // build a map associating these entities with an int index
                        int next = BulletWorld.getInstance().userToEntityLUT.size;
                        body.setUserValue(next);
                        body.setCollisionFlags(body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
                        BulletWorld.getInstance().userToEntityLUT.add(e); // what if e (body) removed?
                    }
                }
            }

            GameFeature gf = GameWorld.getInstance().getFeature(featureName);  // obviously gameObject.feature Name is used as the key

            if (null != gf || null != adaptor) {

                e.add(new FeatureComponent(adaptor));

                if (null != gf) {
                    // stash data in the gameFeature ... this is pretty sketchy as gameFeatures are singular whereas there is possibly multiple GgameObjects/instances that could reference a single GameFeature
                    gf.setEntity(e); ///   bah assigning one entity to a Game Feature

                    if (null == gf.v3data) {
                        gf.v3data = new Vector3(position); // object position from model transform
                    }
                }
            }

            if (/*null != playerFeatureName && */ objectName.equals(playerFeatureName)) {
                playerFeature.setEntity(e);                        // ok .. only 1 player entity per player Feature
                e.getComponent(CharacterComponent.class).isPlayer = true;
            }

        } while (/*null != id && */ n < instanceData.size);
    }

    /* could end up "gameObject.build()" ?? */
    private Entity buildObjectInstance(
            ModelInstance instance, btCollisionShape shape, InstanceData id) {

        Entity e = new Entity();

        if (null != id) {

            if (null != id.rotation) {
                instance.transform.idt();
                instance.transform.rotate(id.rotation);
            }
            if (null != id.translation) {
                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(id.translation);
            }
            if (null != id.color) {
                ModelInstanceEx.setColorAttribute(instance, id.color, id.color.a); // kind of a hack ;)
            }
        }

        ModelComponent mc = new ModelComponent(instance);
        mc.isShadowed = isShadowed; // disable shadowing of skybox)
        e.add(mc);

        if (null != shape) {
            BulletComponent bc = new BulletComponent(shape, instance.transform, mass);
            e.add(bc);

            // special sauce here for static entity
            if (isKinematic) {  // if (0 == mass) ??
// set these flags in bullet comp?
                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
                bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
            }
        }

        if (isCharacter) {
            e.add(new CharacterComponent());
        }

        if (isPickable) {
            e.add(new PickRayComponent());
        }

        return e;
    }
}
