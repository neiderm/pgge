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

    public GameObject(String objectName) {

        this();

        this.objectName = objectName;
    }

    private Array<InstanceData> instanceData = new Array<InstanceData>();

    public String objectName;
    private String featureName; // if Entity is to be part of a feature

    //            Vector3 translation; // needs to be only per-instance
    public Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
    public float mass;
    public String meshShape; // triangleMeshShape, convexHullShape ... rename me e.g. meshshapename (in json also )
    boolean isKinematic;  //  "is Platform" ?
    private boolean isPickable;
    public boolean isShadowed;
    public boolean iSWhatever;
    boolean isCharacter;
    boolean isPlayer; // i guess
//    int lifetime = 1;

    public Array<InstanceData> getInstanceData() {

        return instanceData;
    }


    /*
     * searching the group model for the given gameObject.objectName*
     */
    public void buildNodes(Engine engine, Model model) {
        // default to search top level of model (allows match globbing)
        Array<Node> nodeArray = model.nodes;
        String nodeName = objectName.replaceAll("\\*$", "");

        // special sausce if model has all nodes as children parented under node(0) ... (cherokee and military-jeep)
        if (model.nodes.get(0).hasChildren()) {
            //  e.g. landscape,goonpatrol models end up here, howerver its only the exploding rig that needs
            // special sauce:  if object name was '*' than ressuling nodename length would be 0
            if (nodeName.length() < 1) {

                nodeArray = (Array<Node>) model.nodes.get(0).getChildren();
                nodeName = null;
            }
        }

        for (Node node : nodeArray) {
            ModelInstance mi = null;

            if (null != nodeName && node.id.contains(nodeName)) {
                // specified node ID means this object is loaded from mondo scene model (where everything should be either static or kinematic )
                mi = getModelInstance(model, node.id, scale);

            } else if (null == nodeName) {
// special sauce asssumes  loading from single parent-node, requires recursive search to find the specified _node.id_ in the model hierarchy
                mi = new ModelInstance(model, node.id, true, false, false);
            }

            if (null != mi) {

                btCollisionShape shape = null;
                // TODO find another way to get shape - depends on the instance which is bass-ackwards
                // shouldn't need a new shape for each instace - geometery scale etc. belongs to gameObject
                if (null != meshShape) {
                    BoundingBox boundingBox = new BoundingBox();
                    Vector3 dimensions = new Vector3();
                    mi.calculateBoundingBox(boundingBox);

                    shape = PrimitivesBuilder.getShape(
                            meshShape, boundingBox.getDimensions(dimensions), node);
                }

                buildGameObject(model, engine, mi, shape);
            } // else  ... bail out if matched an un-globbed name ?
        }
    }

    /*
     * IN:
     *
     * RETURN:
     *   ModelInstance
     *
     * Reference:
     *    https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
     *    https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
     */
    private static ModelInstance getModelInstance(Model model, String strNodeName, Vector3 scale) {

        if (null == strNodeName){
            return null; // invalid node name are handled ok, but not if null, so gth out~!
        }
        ModelInstance instance = new ModelInstance(model, strNodeName);

        if (null != instance)
        {
            Node modelNode = instance.getNode(strNodeName);

            if (null == modelNode){
                instance = null; // evidently the Node Name is not valid!
            } else {
                instance.transform.set(modelNode.globalTransform);
                modelNode.translation.set(0, 0, 0);
                modelNode.scale.set(1, 1, 1);
                modelNode.rotation.idt();

                if (null != scale) {
                    instance.nodes.get(0).scale.set(scale);
                }

                instance.calculateTransforms();
            }
        }
        return instance;
    }

    /*
     * NOTE : copies the passed "instance" ... so caller should discard the reference
     */
    void buildGameObject(
            Model model, Engine engine, ModelInstance modelInst, btCollisionShape btcs) {

        if (null == modelInst) {
            System.out.println(
                    "GameObject:buildgameObject()" + "  modelInst==null, probably bad GameObject or ModelGroup definiation");
            return;
        }

        InstanceData id = null;
        int n = 0;


        int countIndex = 0;
        int keyIndex = 0xffff;
        String strObjNameModelInfoKey;
        SceneData sd = GameWorld.getInstance().getSceneData();
        for (String key : sd.modelInfo.keySet()) {

            if (key.equals(this.objectName)) {
                keyIndex = countIndex;
                strObjNameModelInfoKey = new String(this.objectName);
                break;
            }
            countIndex += 1;
        }

        do {
            // game objects may have no instance data
            if (instanceData.size > 0) {
                id = instanceData.get(n++);
            }

            btCollisionShape shape = null;
            if (isKinematic || mass > 0) { // note does not use the gamObject.meshSHape name

                shape = btcs;
            }

            Entity e = buildObjectInstance(modelInst.copy(), shape, id);
            engine.addEntity(e);
//e.add(new StatusComponent(1)); // maybe

            ModelComponent mc = e.getComponent(ModelComponent.class);

            if (0xffff == keyIndex) {
                //System.out.println("keyindex?");
            }
            mc.modelInfoIndx = keyIndex;    // ok maybe this is dumb why not just keep the name string

        } while (/*null != id && */ n < instanceData.size);
    }

    /*
     * builds object with instance data
     */
    private Entity buildObjectInstance(
            ModelInstance instance, btCollisionShape shape, InstanceData id) {

        FeatureAdaptor instanceFeatureAdapter = null; // note ... use below for collision handling setup .. hackage

        Entity e = new Entity();
//e.add(new StatusComponent(1)); // maybe

        if (null != id) {

            if (null != id.rotation) {
// do not idt() this!
//                instance.transform.idt();
                instance.transform.rotate(id.rotation);
            }

            if (null != id.translation) {
// don't wipe the translation from the incoming modelInance! (this is where panzer tank getting scrwed up ... its nodes have offsets in the local transform! )
//                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(id.translation);
            }

            if (null != id.color) {
                ModelInstanceEx.setColorAttribute(instance, id.color, id.color.a); // kind of a hack ;)
            }

            if (null != id.adaptr) {
// translation can now be passed in to feature adapter
                Vector3 position = new Vector3();
                position = instance.transform.getTranslation(position);

                instanceFeatureAdapter = id.adaptr.makeFeatureAdapter(position); // needs the origin location ... might as well send in the entire instance transform
                e.add(new FeatureComponent(instanceFeatureAdapter));
            }
        }

        ModelComponent mc = new ModelComponent(instance);
        mc.isShadowed = isShadowed; // disable shadowing of skybox)
        e.add(mc);
// grab the game object name string anyway
        mc.strObjectName = objectName; // could be invalid ?

        if (null != shape) {
            BulletComponent bc = new BulletComponent(shape, instance.transform, mass);
            e.add(bc);

            // "CF_KINEMATIC_OBJECT informs Bullet that the ground is a kinematic body and that we might want to change its transformation"
            // ref https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
            // explicit flag from json for this: need for e.g. so as not to fall thru "moving platforms" .. for
            // landscape 'disable deactivation'   wake it when "velocity of the body is below the threshold"  (character resting on it would get "stuck")
            //
            if (isKinematic) {   // if (0 == mass) ??

                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

                bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
            }

            // try to enable collision handling callbacks on select objects ...  this crap here needs to go with bullet body setup  BAH
            if (isKinematic   // if (0 == mass) ??
                    || isPlayer             // make sure gound contact colllision filtering works with player character!
                    || null != instanceFeatureAdapter) {
//
                // tmp hac, crap
                if (isKinematic) {
                    // filter out reporting collisions w/ terrain/platform (only process colliding objects of interest)
                    BulletWorld.getInstance().addBodyWithCollisionNotif(
                            e, // needs the Entity to add to the table BLAH
                            BulletWorld.GROUND_FLAG, BulletWorld.NONE_FLAG);
                } else {
                    // any "feature" objects will allow to proecess contacts w/ any "terrain/platform" surface
                    BulletWorld.getInstance().addBodyWithCollisionNotif(
                            e, // needs the Entity to add to the table BLAH
                            BulletWorld.OBJECT_FLAG, BulletWorld.GROUND_FLAG);
                }
            }
        }

        if (isCharacter) {
            e.add(new CharacterComponent());
        }

        if (isPickable) {
            e.add(new PickRayComponent());
        }

        if (isPlayer) {
            GameFeature playerFeature =
                    GameWorld.getInstance().getFeature(SceneData.LOCAL_PLAYER_FNAME);
//if (null != playerFeature)
            playerFeature.setEntity(e);                        // ok .. only 1 player entity per player Feature
        }

        return e;
    }
}
