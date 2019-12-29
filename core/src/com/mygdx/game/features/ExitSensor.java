package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

public class ExitSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        super.update(sensor);

        if (isActivated) {

            ModelComponent mc = sensor.getComponent(ModelComponent.class);

            if (mc.modelInst.materials.size > 0)
                ModelInstanceEx.setColorAttribute(mc.modelInst, new Color(Color.TEAL)); // tmp test code
//else
//    Gdx.app.log("sdf", "sdf"); //  doesn't necessarily have a material

            if (isTriggered) {

                StatusComponent sc = target.getComponent(StatusComponent.class);
                sc.canExit = true;
            }
        }
    }

    /*
     * TBD ... late adding physics body? not sure what purpose
     * WIP experiment with "dynamic" i.e. no mesh geometry or physics characterisitics are given in the
     * json, only the feature (Exit SEnsor)
     *  a Collision Processor class is designated ... which here is the cue to arbitrarily assigns a physics
     * body, setting it at VT0 as usual .. Vt0 assigned from vT since there is no usual body instance translation avialble
     * Intend to generalize but for now it was cluttering up the feature/sensor class hierarchy
     * Also experimented with assigning position relative to target/player whatever reason
     *
     * specificed debouncedCollisionProc and as noted here is given a mesh and physics body
     * default onProcessedCollision() is in featureAdaptor  and simply spawns a new object defaulty body "sphere"
     *
     */
///*
    @Override
    public void onActivate(Entity ee) {

//        super.onActivate(ee);
//        Vector3 translation = new Vector3();
//
//        ModelComponent mc = target.getComponent(ModelComponent.class);
//        translation = mc.modelInst.transform.getTranslation(translation);
//
//        /* position vector offsets by user value from json, to be loaded by super method */
//        vT0.set(vT).add(translation);
//        vT0.set(vT);             //          vT0.add(vT);    ????????????
        vT0.add(vT); // vT0 is set to a body position if one was given (otherwise 0'd) otherwise vT may be set
        // so by adding 3rd possibility is vT0 of a body is offset by vT on activation ... fwr

        if (null != collisionProcessor) // .... hmmm let's see, apparently this should involve collisions
            CompCommon.entityAddPhysicsBody(ee, vT0); // rioght now this is only a boring old box shape!
    }
//*/
}
