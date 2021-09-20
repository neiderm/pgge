/*
 * Copyright (c) 2021 Glenn Neidermeier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.mygdx.game.controllers;

public class ControllerAdapter implements ControllerAbstraction {

    public static class ControlBundle {

        public ControlBundle() { // MT
        }

        public float analogX;
        public float analogY;
        public float analogX1;
        public float analogY1;
        public float analogL;
        public float analogR;
        public boolean switch0;
        public boolean switch1;
    }

    // if client code needs to instantiate their own Control Bundle this one can be left to GC
    ControlBundle controlBundle = new ControllerAdapter.ControlBundle();

    public void updateControls(float time) { // MT
    }

    ControlBundle getControlBundle() {
        return controlBundle;
    }

//    // todo
//    enum ButtonAbstraction {
//        SW_FIRE1,
//        SW_FIRE21,
//        SW_SQUARE2,
//        SW_TRIANGL3
//    }

    ;
}
