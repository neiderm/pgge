package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/*
 * Maybe for starters just draw a red line and player becomes dead
 */

/*
 * I wanna be a Bad Actor!!!!!!!
 * Well, a Bad Actor is presently just a Feeature (Kill Sensor) that has, most importantly, been made  Pickable. (and has a feature Adaptor that can be "update()d" from the  Pick event handler in Game Screen .
 * but more things should be destroyable (Pickable ... ahem, not all Pickable things are necessarily to be destroyed!)
 *
 * Beyond that, need to kill me, as well as bump the score.
 */

public class ElectricEye extends  VectorSensor {    // TODO: .... KILL !!!!!!!!

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class); // can this be cached?
// if (null != mc)
            if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.ROYAL)); // tmp test code
            }
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered) {
                //                Vector3 trans = new Vector3();     ... trans can be used below if exploding .,,
//                mc.modelInst.transform.getTranslation(trans);

                if (mc.modelInst.materials.size > 0) {
// well this is dumb its getting the insta.material anyway
                    ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.GOLD)); // tmp test code
                }

                isTriggered = false; // need to unlatch it !


// watch it ... i'm a BadActor  now!
                target.getComponent(StatusComponent.class).lifeClock = 0; // use flags get rid of this
            }


            /*
             * if I am pickable, then pick handler could have invoked this update() ... having added the
             * Status Comp + deleteMe ... why not let Status System handle it !!!!!
             */

            StatusComponent sc = sensor.getComponent(StatusComponent.class);

            // check this since the SC is actaully added dynamically (so no point to caching)
            if (null != sc) {

                if (0 == sc.lifeClock) {

                    // uses the Model Compont .transform translation so
                    CompCommon.makeBurnOut(sensor, 1000);
                }
            }
            // else System.out.println();
        }
    }
}
