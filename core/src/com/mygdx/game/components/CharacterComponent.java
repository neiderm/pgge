package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.controllers.SteeringEntity;

/**
 * Created by neiderm on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    public SteeringEntity steerable;
    public boolean isPlayer;
    public String objectName;

//    public CharacterComponent() { // mt
/////*
//        setSteerable(new SteeringEntity(){
//            // steeringBehavior left null so it doesn't do anything in the update()
///*
//                         public void update(float deltaTime) {
//                             float t = deltaTime;
//                         }
//  */
//        });
////*/
//    }

    public CharacterComponent(String objectName){

        this(objectName, false);
    }

    public CharacterComponent(String objectName, boolean isPlayer){

        this.isPlayer = isPlayer;
        this.objectName = new String(objectName);
    }

    public CharacterComponent(SteeringEntity steeringEntity) {

        setSteerable(steeringEntity);
    }

    public void setSteerable (SteeringEntity steeringEntity) {

        this.steerable = steeringEntity;
    }
}
