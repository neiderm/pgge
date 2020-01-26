/*
 * Copyright (c) 2020 Glenn Neidermeier
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
package com.mygdx.game.animations;

import com.badlogic.ashley.core.Entity;

/*
 based on FeatureAadapter
 */
public class AnimAdapter  implements AnimInterface {

    public boolean enabled ;

    public String strMdlNode; // id of node to be animated

    public void update(Entity e){
        //mt
    }

    public    void init(Object asdf){
        //mt
    }

    /*
here is some nice hackery to get an instance of the type of sub-class ...constructor of
sub-class is invoked but that's about it ... far from beging much of an actual "clone" at this point
*/

    public static AnimAdapter getAdapter(AnimAdapter thisFa) {

        AnimAdapter adaptor = null;

        Class c = thisFa.getClass();

        try {
            adaptor = (AnimAdapter) c.newInstance(); // have to cast this ... can cast to the base-class and it will still take the one of the intended sub-class!!

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return adaptor;
    }
}
