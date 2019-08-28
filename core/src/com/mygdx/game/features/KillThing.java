package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;

/*
 * ....Feature  "DroppedBody" ..  .......... becomes un-dynamic
 */
public class KillThing extends KillSensor {

    private int contactCount;
    private int contactBucket;

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it
                contactBucket -= 1;
            } else {
                if (0 == contactBucket) {
                    Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                    contactCount = 0;
                    collisionHdlr(sensor);
                }
            }
        }
    }

    private void collisionHdlr(Entity sss) {

        BulletComponent bc = sss.getComponent(BulletComponent.class);
        if (null == bc) {
            Gdx.app.log("collisionHdlr", "BulletComponent bc =  === NULLLL");
            return; // bah processing object that should already be "at rest" ???? .....................................................
        }

        bc.body.setCollisionFlags( bc.body.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);

        // insert a newly created game OBject into the "spawning" model group
        GameObject gameObject = new GameObject();
        gameObject.isShadowed = true;

        Vector3 size = new Vector3(0.5f, 0.5f, 0.5f); /// size of the "box" in json .... irhnfi  bah
        gameObject.scale = new Vector3(size);

        gameObject.objectName = "box";

        Vector3 translation = new Vector3();
//                translation = bc.body.getWorldTransform().getTranslation(translation);

        ModelComponent mc = sss.getComponent(ModelComponent.class);
        Matrix4 tmpM4 = mc.modelInst.transform;
//            translation = tmpM4.getTranslation(translation);
        gameObject.getInstanceData().add(new InstanceData(tmpM4.getTranslation(translation)));

//                GameWorld.getInstance().addSpawner(gameObject);         ///    toooodllly dooodddd    object is added "kinematic" ???

        StatusComponent sc = new StatusComponent();
        sc.deleteFlag = 2;         // flag bullet Comp for deletion
        sss.add(sc);
    }

    @Override
    public void onCollision(Entity myCollisionObject, int id) {

        Gdx.app.log("onCollision", "int = " + id);

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = 60; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?

/* test ..
        BulletComponent bc = myCollisionObject.getComponent(BulletComponent.class);
        bc.body.setCollisionFlags(bc.body.getCollisionFlags() & ~btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
*/

//        int as = bc.body.getActivationState();
//        Gdx.app.log("asdf", "activation state = " + as);

//        m4 = bc.body.getWorldTransform();
//        translationASDFG = m4.getTranslation(translationASDFG);
    }
}
