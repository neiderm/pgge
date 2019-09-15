package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.StatusComponent;

public class DebouncedCollisionProcessor implements CollisionProcessorIntrf {

    private int contactCount;
    private int contactBucket;

    public void onCollision(Entity myCollisionObject) {

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = 60; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?
    }

    public void processCollision(Entity ee) {

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it

                contactBucket -= 1;

            } else
            if (0 == contactBucket) {

                Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                contactCount = 0;

                CompCommon.releasePayload(ee); // spawnNewGameObject

                ee.add(new StatusComponent(true)); // delete me!
            }
        }
    }
}
