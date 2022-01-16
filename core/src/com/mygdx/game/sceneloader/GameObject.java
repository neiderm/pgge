/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
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
package com.mygdx.game.sceneloader;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.physics.bullet.collision.CollisionConstants;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.animations.AnimAdapter;
import com.mygdx.game.components.PhysicsComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.features.CollisionSfx;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

public class GameObject {

    private GameObject() {
        this.isShadowed = true;
        this.isShootable = false;
    }

    public GameObject(Vector3 translation, Quaternion rotation) {
        this();
        this.mass = 1.0f;
        this.getInstanceData().add(new InstanceData(translation, rotation));
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

    public boolean isPickable;
    public boolean isShadowed;
    public boolean iSWhatever;
    public float mass;
    public String featureName; // if Entity is to be part of a Feature
    public String objectName;
    // all instances should be at same scale (share same collision Shape)
    public Vector3 scale;
    // never assigned (from json)
    @SuppressWarnings("unused")
    private String meshShape; // triangleMeshShape, convexHullShape ... rename me e.g. meshshapename (in json also )
    private boolean isShootable;
    private final Array<InstanceData> instanceData = new Array<>();
    boolean isCharacter;
    boolean isKinematic; // "isPlatform" ?
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
            //  e.g. landscape, goonpatrol models end up here, however it's only the exploding rig that needs
            // special sauce:  if object name was '*' than resulting nodename length would be 0
            nodeArray = (Array<Node>) model.nodes.get(0).getChildren();
            nodeName = null;
        }

        for (Node node : nodeArray) {
            ModelInstance mi = null;

            if (null != nodeName && node.id.contains(nodeName)) {
                // specified node ID means this object is loaded from mondo scene model (where everything should be either static or kinematic )
                mi = getModelInstance(model, node.id, scale);

            } else if (null == nodeName) {
                // special sauce - assumes loading from single parent-node model i.e. requires
                // recursive search to find the specified node ID in the model hierarchy
                mi = new ModelInstance(model, node.id, true, false, false);
            }

            if (null != mi) {
                btCollisionShape shape = null;
                // TODO find another way to get shape - depends on the instance which is bass-ackwards
                // shouldn't need a new shape for each instance - geometery scale etc. belongs to gameObject
                if (null != meshShape) {
                    shape = PrimitivesBuilder.getShape(meshShape, getDimensionsBB(mi), node);
                }
                buildGameObject(engine, mi, shape);
            } // else ... bail out if matched an un-globbed name?
        }
    }

    public void buildChildNodes(Engine engine, Model model, btCompoundShape compShape) {

        Array<Node> nodeArray = new Array<>();
        PrimitivesBuilder.getNodeArray(model.nodes, nodeArray);

        int index = 0;
        // have to iterate each node, can't assume that all nodes in array are valid and associated
        // with a child-shape (careful of non-graphical nodes!)
        for (Node node : nodeArray) {
            // protect for non-graphical nodes in models (they should not be counted in index of child shapes)
            if (node.parts.size > 0) {
                // recursive
                ModelInstance instance =
                        new ModelInstance(model, node.id, true, false, false);
                Node modelNode = instance.getNode(node.id);

                if (null != modelNode) {
                    // seems only the "panzerwagen" because it's nodes are not central to the rig model which makes it handled like scenery
                    instance.transform.set(modelNode.globalTransform);
                    modelNode.translation.set(0, 0, 0);
                    modelNode.scale.set(1, 1, 1);
                    modelNode.rotation.idt();
                }

                if (index < compShape.getNumChildShapes()) {
                    buildGameObject(engine, index, instance, compShape.getChildShape(index));
                }
                index += 1;
            }
        }
    }

    private void buildGameObject(
            Engine engine, int index, ModelInstance instance, btCollisionShape shape) {

        if (shape.getUserIndex() == index) {
            // select the sound according to size of object
            String key;
            final double dimensions_len = getDimensionsBB(instance).len();

            if (dimensions_len > 1.0f) {
                key = "021";
            } else if (dimensions_len > 0.5f) {
                key = "022";
            } else {
                key = "023";
            }
            Vector3 slocation = new Vector3();
            slocation = instance.transform.getTranslation(slocation);
            getInstanceData().get(0).adaptr = new FeatureAdaptor(new CollisionSfx(key, slocation));
            buildGameObject(engine, instance, shape);
        }
    }

    private Vector3 getDimensionsBB(ModelInstance instance) {
        Vector3 dimensions = new Vector3();
        BoundingBox boundingBox = new BoundingBox();
        instance.calculateBoundingBox(boundingBox);
        boundingBox.getDimensions(dimensions);

        return dimensions;
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
            return null; // invalid node name are handled ok, but not if null
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
     * NOTE: copies the passed "instance" ... so caller should discard the reference
     */
    void buildGameObject(Engine engine, ModelInstance modelInst, btCollisionShape btcs) {

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

        Entity e = new Entity();
        StatusComponent statusComp = new StatusComponent(1);

        ModelComponent mc = new ModelComponent(instance);
        ModelInfo mi = GameWorld.getInstance().getSceneData().modelInfo.get(this.objectName);

        if (null != mi && null != mi.animAdapter) {
            // new instance (manually copy) of mi.animAdapter - it will be type of subclass
            mc.animAdapter = AnimAdapter.getAdapter(mi.animAdapter);
            // Manually copy needed fields, but only fields of the mi.animAdapter base class can be
            // known so they need to be kind of generic.
            mc.animAdapter.strMdlNode = mi.animAdapter.strMdlNode;
        }

        mc.isShadowed = isShadowed; // disable shadowing of skybox
        e.add(mc);

        // if no burnout then flag entity by setting status component bounty to invalid
        if (iSWhatever) {
            // entity is being excluded from having a BurnOut made on it (should not be shoot-able in the first place?)
            statusComp.bounty = -1; // temp hack flag object as no-burnout

            // quash the node translation for Instance object loaded from model - do this before translation is set from ID below
            instance.transform.setTranslation(0, 0, 0); // temp hack use flag to clear model position
        }

        if (isCharacter) {
            e.add(new CharacterComponent());
        }
        if (isPickable) {
            e.add(new PickRayComponent());
        }
        if (isPlayer) {
            GameFeature playerFeature = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);
            playerFeature.setEntity(e); // only 1 player Entity per player Feature
        }

        if (null != id) {
            if (null != id.rotation) {
// do not idt() this!
//                instance.transform.idt();
                instance.transform.rotate(id.rotation);
            }

            if (null != id.translation) {
                // Don't wipe the translation from the incoming Model Instance! (this is where
                // panzerwagen getting screwed up ... its nodes have offsets in the local transform!)
//                instance.transform.setTranslation(0, 0, 0);
                instance.transform.trn(id.translation);
            }

            if (null != id.color) {
                ModelInstanceEx.setColorAttribute(instance, id.color, id.color.a); // kind of a hack ;)
            }

            if (null != id.adaptr) {
                Vector3 position = new Vector3();
                position = instance.transform.getTranslation(position);

                // needs the origin location ... might as well send in the entire instance transform
                e.add(new FeatureComponent(id.adaptr.makeFeatureAdapter(position)));

                // bah this also done below for all non-static Bullet bodies
                e.add(statusComp); // needs an SC in order to be 'shootable', and most FAs should be shootable
            }
        }
        buildPhysObjInstance(e, statusComp, instance, shape);

        return e;
    }

    private void buildPhysObjInstance(
            Entity e, StatusComponent statusComp, ModelInstance instance, btCollisionShape shape) {

        if (null != shape) {
            PhysicsComponent bc = new PhysicsComponent(shape, instance.transform, mass);
            e.add(bc);
            // "CF_KINEMATIC_OBJECT informs Bullet that the ground is a kinematic body and that we might want to change its transformation"
            // ref https://xoppa.github.io/blog/using-the-libgdx-3d-physics-bullet-wrapper-part2/
            // Explicit flag from json for this: needed e.g. so as not to fall thru "moving platforms"
            // For landscape, 'disable deactivation' and wake it when "velocity of the body is below the
            // threshold" (body resting on it would get "stuck")
            //
            if (isKinematic) {
                bc.body.setCollisionFlags(
                        bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);

                bc.body.setActivationState(CollisionConstants.DISABLE_DEACTIVATION);

                // filter out reporting collisions w/ terrain/platform (only process colliding objects of interest)
                BulletWorld.getInstance().addBodyWithCollisionNotif(
                        e, BulletWorld.GROUND_FLAG, BulletWorld.NONE_FLAG);
            } else {
                FeatureAdaptor fa = null;
                FeatureComponent fc = e.getComponent(FeatureComponent.class);
                if (null != fc) {
                    fa = fc.featureAdpt;
                }
                if (isPlayer // make sure ground contact collision filtering works with player character!
                        || null != fa || isShootable || isCharacter) {
                    // try to enable collision handling callbacks on select objects (needs to go with bullet body setup?)
                    // any "feature" objects will allow to process contacts w/ any "terrain/platform" surface
                    BulletWorld.getInstance().addBodyWithCollisionNotif(
                            e, // needs the Entity to add to the table BLAH
                            BulletWorld.OBJECT_FLAG, BulletWorld.GROUND_FLAG);

                    e.add(statusComp); // needs a SC in order to be shoot-able
                }
            }
        }
    }

    /*
     * builds object with instance data, no graphical or shape part
     */
    private Entity buildObjectInstance(InstanceData id) {

        Entity e = new Entity();

        if (null != id && null != id.adaptr) {
            // translation can be passed in to Feature Adapter
            Vector3 position = new Vector3(id.translation);
            // needs the origin location ... might as well send in the entire instance transform
            e.add(new FeatureComponent(id.adaptr.makeFeatureAdapter(position)));
        }
        return e;
    }
}
