package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.components.CompCommon;

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
                    CompCommon.mkStaticFromDynamicEntity(sensor);
                }
            }
        }
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
