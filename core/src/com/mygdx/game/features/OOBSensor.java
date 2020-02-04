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
package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 8/24/2019.
 * <p>
 * Out-of-bounds sensor is simply an inverted kill sensor ...
 *  or was ..... tmp ?  possibly re-integrate as sub-class of KS.
 */
public class OOBSensor extends OmniSensor {

    @Override
    public void init(Object obj){

        super.init(obj);

        inverted = true; // instead of requiing this flag, it could just get by by doing the subclass
    }

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isTriggered) {
            target.getComponent(StatusComponent.class).lifeClock = 0;
        }
    }
}
