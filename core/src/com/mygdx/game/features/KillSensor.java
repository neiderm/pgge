package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;

/*
 * ....Feature  "DroppedBody" ..  .......... adds a "spawner"
 */
public class KillSensor extends OmniSensor {

    private boolean hasCollidedFlag;

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (hasCollidedFlag) {

            if (preContactCount == contactCount) {

                Gdx.app.log("asdf", "preContactCount == contactCount");

                hasCollidedFlag = false;

                BulletComponent bc = sensor.getComponent(BulletComponent.class);
                bc.body.setCollisionFlags(bc.body.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

                // insert a newly created game OBject into the "spawning" model group
                GameObject gameObject = new GameObject();
                gameObject.isShadowed = true;

                Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
                gameObject.scale = new Vector3(size);

                gameObject.objectName = "box";

                Vector3 translation = new Vector3();
//                Matrix4 tmpM4 = bc.body.getWorldTransform();
//                translation = tmpM4.getTranslation(translation);

                ModelComponent mc = sensor.getComponent(ModelComponent.class);
                Matrix4 tmpM4 = mc.modelInst.transform;
                translation = tmpM4.getTranslation(translation);


                gameObject.getInstanceData().add(new InstanceData(translation));


                GameWorld.getInstance().addSpawner(gameObject);         ///    toooodllly dooodddd    object is added "kinematic" ???

                // delete the old one
                sensor.add(new StatusComponent(true));
            }

            preContactCount = contactCount;
        }
    }


    //    Vector3 translationASDFG = new Vector3();
//    Matrix4 m4 = new Matrix4();
    private int contactCount;
    private int preContactCount;

    @Override
    public void onCollision(Entity sensor) {

        contactCount += 1;

        hasCollidedFlag = true;

//        BulletComponent bc = sensor.getComponent(BulletComponent.class);
//        m4 = bc.body.getWorldTransform();
//        translationASDFG = m4.getTranslation(translationASDFG);
    }
}
