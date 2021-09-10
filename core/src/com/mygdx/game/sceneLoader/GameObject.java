/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.mygdx.game.animations.AnimAdapter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

public class GameObject {

    private GameObject() {
        this.isShadowed = true;
        this.isShootable = false;
    }

    public GameObject(float mass) {
        this();
        this.mass = mass;
    }

    public GameObject(String objectName) {
        this();
        this.objectName = objectName;
    }

    public GameObject(String objectName, float mass, Vector3 scale, InstanceData instanceData) {
        this(objectName);
        this.mass = mass;
        this.scale = scale;
        this.instanceData.add(instanceData);
    }

    private final Array<InstanceData> instanceData = new Array<InstanceData>();

    public Vector3 scale; // NOT per-instance, all instances should be same scale (share same collision Shape)
    public String meshShape; // triangleMeshShape, convexHullShape ... rename me e.g. meshshapename (in json also )
    public String objectName;
    public boolean isPickable;
    public boolean isShadowed;
    public boolean iSWhatever;
    public float mass;

    private boolean isShootable;
    @SuppressWarnings("unused")
    private String featureName; // if Entity is to be part of a feature

    boolean isCharacter;
    boolean isKinematic;  // "is Platform" ?
    boolean isPlayer;

    public Array<InstanceData> getInstanceData() {

        return instanceData;
    }

    /*
     * searching the group model for the given gameObject.objectName*
     */
    void buildNodes(Engine engine, Model model) {
        // default to search top level of model (allows match globbing)
        Array<Node> nodeArray = model.nodes;
        String nodeName = objectName.replaceAll("\\*$", "");

        // special sausce if model has all nodes as children parented under node(0) ... (cherokee and military-jeep)
        if (nodeName.length() < 1 && model.nodes.get(0).hasChildren()) {
            //  e.g. landscape,goonpatrol models end up here, howerver its only the exploding rig that needs
            // special sauce:  if object name was '*' than ressuling nodename length would be 0
            nodeArray = (Array<Node>) model.nodes.get(0).getChildren();
            nodeName = null;
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

                buildGameObject(engine, mi, shape);
            } // else  ... bail out if matched an un-globbed name ?
        }
    }

    /**
     * Create new instance of a ModelInstance
     * reference:
     * https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
     * https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
     *
     * @param model       Model to be instanced
     * @param strNodeName node identifier string
     * @param scale       scale factor
     * @return instantiated ModelInstance
     */
    private static ModelInstance getModelInstance(Model model, String strNodeName, Vector3 scale) {

        if (null == strNodeName) {
            return null; // invalid node name are handled ok, but not if null, so gth out~!
        }
        ModelInstance instance = new ModelInstance(model, strNodeName);

        Node modelNode = instance.getNode(strNodeName);

        if (null != modelNode) {

            instance.transform.set(modelNode.globalTransform);
            modelNode.translation.set(0, 0, 0);
            modelNode.scale.set(1, 1, 1);
            modelNode.rotation.idt();

            if (null != scale) {
                instance.nodes.get(0).scale.set(scale);
            }

            instance.calculateTransforms();
        }
        return instance;
    }

    /*
     * NOTE : copies the passed "instance" ... so caller should discard the reference
     */
    public void buildGameObject(Engine engine, ModelInstance modelInst, btCollisionShape btcs) {

        InstanceData id = null;
        int n = 0;

        do {
            // game objects may have no instance data
            if (instanceData.size > 0) {
                id = instanceData.get(n++);
            }

            btCollisionShape shape = null;

            if (isKinematic || mass > 0) { // note does not use the gamObject.meshSHape name
                shape = btcs;
            }

            Entity e;

            if (null != modelInst) {
                e = buildObjectInstance(modelInst.copy(), shape, id);
            } else {
                e = buildObjectInstance(id);
            }

            engine.addEntity(e);

        } while (n < instanceData.size);
    }

    /*
     * builds object with instance data
     */
    private Entity buildObjectInstance(
            ModelInstance instance, btCollisionShape shape, InstanceData id) {

        FeatureAdaptor instanceFeatureAdapter = null; // note ... use below for collision handling setup .. hackage

        Entity e = new Entity();
        StatusComponent statusComp = new StatusComponent(1);

        // if no burnout then flag entity by setting status component bounty to invalid
        if (iSWhatever) {
            // entity is being excluded from having a BurnOut made on it but it should not be shootable in the first place
            statusComp.bounty = -1; // temp hack flag object as no-burnout

            // quash the node tranlation for Instance object loaded from model - do this before translation is set from ID below
            instance.transform.setTranslation(0, 0, 0); // temp hack use flag to clear model position
        }

        if (null != id) {

            if (null != id.rotation) {
// do not idt() this!
//                instance.transform.idt();
                instance.transform.rotate(id.rotation);
            }

            if (null != id.translation) {
                // don't wipe the translation from the incoming modelInance! (this is where panzer
                // tank getting screwed up ... its nodes have offsets in the local transform! )
//                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(id.translation);
            }

            if (null != id.color) {
                ModelInstanceEx.setColorAttribute(instance, id.color, id.color.a); // kind of a hack ;)
            }

            if (null != id.adaptr) {
                // translation can be passed in to a feature adapter
                Vector3 position = new Vector3();
                position = instance.transform.getTranslation(position);

                instanceFeatureAdapter = id.adaptr.makeFeatureAdapter(position); // needs the origin location ... might as well send in the entire instance transform
                e.add(new FeatureComponent(instanceFeatureAdapter));

                // bah this also done below for all non-static Bullet bodies
                e.add(statusComp); // needs an SC in order to be 'shootable', and most FAs should be shootable
            }
        }

        ModelComponent mc = new ModelComponent(instance);

        ModelInfo mi = GameWorld.getInstance().getSceneData().modelInfo.get(this.objectName);

        if (null != mi && null != mi.animAdapter) {
            //new instance (manually copy) of mi.animAdator - it will be type of subclass !
            mc.animAdapter = AnimAdapter.getAdapter(mi.animAdapter);
            // Manually copy needed fields, but only fields of the mi.animAdapter base class can be
            // known so they need to be kind of generic.
            mc.animAdapter.strMdlNode = mi.animAdapter.strMdlNode;
        }

        mc.isShadowed = isShadowed; // disable shadowing of skybox)
        e.add(mc);

        if (null != shape) {
            BulletComponent bc = new BulletComponent(shape, instance.transform, mass);
            e.add(bc);

            // "CF_KINEMATIC_OBJECT informs Bullet that the ground is a kinematic body and that we might want to change its transformation"
            // ref https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
            // explicit flag from json for this: need for e.g. so as not to fall thru "moving platforms" .. for
            // landscape 'disable deactivation'   wake it when "velocity of the body is below the threshold"  (character resting on it would get "stuck")
            //
            if (isKinematic) {

                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

                bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);

                // filter out reporting collisions w/ terrain/platform (only process colliding objects of interest)
                BulletWorld.getInstance().addBodyWithCollisionNotif(
                        e, // needs the Entity to add to the table BLAH
                        BulletWorld.GROUND_FLAG, BulletWorld.NONE_FLAG);
            } else
                // try to enable collision handling callbacks on select objects ...  this crap here needs to go with bullet body setup  BAH
                if (
                        isPlayer             // make sure gound contact colllision filtering works with player character!
                                || null != instanceFeatureAdapter
                                || isShootable
                                || isCharacter) {
                    // any "feature" objects will allow to proecess contacts w/ any "terrain/platform" surface
                    BulletWorld.getInstance().addBodyWithCollisionNotif(
                            e, // needs the Entity to add to the table BLAH
                            BulletWorld.OBJECT_FLAG, BulletWorld.GROUND_FLAG);

                    e.add(statusComp); // needs an SC in order to be 'shootable'
                }
        }

        if (isCharacter) {
            e.add(new CharacterComponent());
        }

        if (isPickable) {
            e.add(new PickRayComponent());
        }

        if (isPlayer) {
            GameFeature playerFeature = GameWorld.getInstance().getFeature(SceneData.LOCAL_PLAYER_FNAME);
//if (null != playerFeature)
            playerFeature.setEntity(e); // only 1 player Entity per player Feature
        }
        return e;
    }

    /*
     * builds object with instance data, no graphical or shape part
     */
    private Entity buildObjectInstance(InstanceData id) {

        Entity e = new Entity();

        if (null != id && null != id.adaptr) {
            // translation can be passed in to feature adapter
            Vector3 position = new Vector3(id.translation);

            FeatureAdaptor instanceFeatureAdapter = id.adaptr.makeFeatureAdapter(position); // needs the origin location ... might as well send in the entire instance transform
            e.add(new FeatureComponent(instanceFeatureAdapter));
        }
        return e;
    }
}
