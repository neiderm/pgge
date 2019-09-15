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
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;

/*
 extends the sensor adaptor only for means of obtaining target
 */
/* Bomb ... bomb w/ payloaad (payloadcan be exit sensor ;) */
public class DroppedThing extends SensorAdaptor {

    @Override
    public void onActivate(Entity ee) {

        FeatureAdaptor newFa = getFeatureAdapter(this);

        Vector3 translation = new Vector3();
/*
        BulletComponent bc = target.getComponent(BulletComponent.class);
        translation = bc.body.getWorldTransform().getTranslation(translation);
*/
        ModelComponent mc = target.getComponent(ModelComponent.class);
        translation = mc.modelInst.transform.getTranslation(translation);
        translation.y += 8; // idfkk ... make it fall from the sky!

        CompCommon.entityAddPhysicsBody(ee, translation);
    }
}
