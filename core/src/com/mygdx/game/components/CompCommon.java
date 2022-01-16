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
package com.mygdx.game.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.sceneloader.GameObject;
import com.mygdx.game.sceneloader.InstanceData;
import com.mygdx.game.util.PrimitivesBuilder;

/*
 * the catch-all of the moment ...
 */
public class CompCommon {

    private CompCommon() {
        throw new IllegalStateException("Utility class");
    }

    public static void spawnNewGameObject(
            Vector3 scale, Vector3 translation, FeatureAdaptor fa, String objectName) {

        spawnNewGameObject(scale, translation, fa, objectName, 0);
    }

    public static void spawnNewGameObject(
            Vector3 scale, Vector3 translation, FeatureAdaptor fa, String objectName, float mass) {

        InstanceData id = new InstanceData(translation);
        id.adaptr = fa;

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject(objectName);
        gameObject.mass = mass;
        gameObject.scale = scale;
        gameObject.getInstanceData().add(id);

// any sense for Game Object have its own static addspawner() method ?  (need the Game World import/reference here ?)
        GameWorld.getInstance().addSpawner(gameObject); // toooodllly dooodddd    object is added "kinematic" ???
    }

    /*
     * doesn't do much more than flag the comp for removal
     * set the collision flags is probably pointless
     */
    public static void physicsBodyMarkForRemoval(Entity ee) {

        StatusComponent sc = ee.getComponent(StatusComponent.class);
        if (null == sc) {
            sc = new StatusComponent(1);
        }
        sc.deleteFlag = 2; // flag bullet Comp for deletion
        ee.add(sc);

    }

    /*
     * dynamically "activate" a template entity and set its location
     *  creates a graphical mesh-shape and a matching physics body, adds it to the bullet world
     */
    public static void entityAddPhysicsBody(Entity ee, Vector3 translation) {

        // tooooo dooo how to handle shape?
        btCollisionShape shape =
                PrimitivesBuilder.getShape("box", new Vector3(1, 1, 1));

        // add Bullet Component and link to the model comp xform
        ModelComponent mc = ee.getComponent(ModelComponent.class);
        Matrix4 transform = mc.modelInst.transform;
        transform.setTranslation(translation);

        ee.add(new PhysicsComponent(shape, transform, 1f)); // how to set mass?

        /* add body to bullet world  default adds as 'OBJECT FLAG'*/
        BulletWorld.getInstance().addBodyWithCollisionNotif(
                ee // needs the Entity to add to the table BLAH
        );
    }
}
