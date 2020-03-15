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
package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;

public class DebouncedCollisionProc implements CollisionProcessorIntrf {

    private int contactCount;
    private int contactBucket;
    private int contactBucketFull = 60;

    public DebouncedCollisionProc(){ //mt
    }

    public DebouncedCollisionProc(int contactBucketFull){

        this.contactBucketFull = contactBucketFull;
    }

    @Override
    public void onCollision(Entity myCollisionObject) {

        contactCount += 1; // always increment (zero'd out when bucket is emptied)

        // bucket fills faster to ensure that it takes time to empty the bucket once the contacts have rung out ....
        contactBucket = contactBucketFull; // idfk ... allow at least 1 frames worth of updates to ensure object is at rest?
    }

    @Override
    public boolean processCollision(Entity ee) {

        if (contactCount > 0) {

            if (contactBucket > 0) { // each update, either empty 1 drop from the bucket and return, or if bucket empty then process it

                contactBucket -= 1;

            } else
            if (0 == contactBucket) {

                Gdx.app.log(" asdfdfd", "object is at rest ?? ");
                contactCount = 0;

                return true;
            }
        }
        return false;
    }
}
