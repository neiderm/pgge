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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.util.ModelInstanceEx;

public class Projectile extends KillSensor {

    private Vector3 dVector = new Vector3();

    public Projectile() { // mt
    }

//    public Projectile(Vector3 vvv) {
    //  }

    public Projectile(Entity picked) {

        this.userData = picked;//.getComponent(ModelComponent.class).modelInst.transform;
    }

    /*
     *   had to change Feature Adaptor to call init() regdless of passing null 'object')
     */
    @Override
    public void init(Object object) {

        // object==target
        // super.init(object):
        //        setTarget((Entity) target, vS /* radius */, vT /* origin */);

        // is fine, but vT needs to be reset at each update anywaty!!!!
        this.dVector = getDirectionVector();
        this.vT0 = getDirectionVector(); // uhhhh don't think the originating posiotun used right now

        this.vS = new Vector3(0.9f, 0.9f, 0.9f); // idfk think it need vS for radiys of the kill sensor

//        if (null != object)
        {
            this.target = (Entity) object; // it's a moving target
        }
    }


    // working variables
    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Quaternion orientation = new Quaternion();

    private Vector3 getDirectionVector() {

        Vector3 vvv = new Vector3();

        GameFeature pf = GameWorld.getInstance().getFeature("Player"); // make tag a defined string
        Entity pp = pf.getEntity(); // picked player

        BulletComponent bc = pp.getComponent(BulletComponent.class); // picked player

        if (null != bc && null != bc.body) {

            bc.body.getWorldTransform(tmpM);
        }

        tmpM.getRotation(orientation);

        // offset the trans  because the model origin is free to be adjusted in Blender e.g. at "surface level"
        // depending where on the model origin is set (done intentionally for adjustmestment of decent steering/handling physics)
//        tmpV.set(0, +0.75f, 0); // using +y for up vector ...
//        ModelInstanceEx.rotateRad(tmpV, orientation); // ... and rotsting the vector to orientation of transform matrix

//        tmpM.getTranslation(trans).add(tmpV); // start coord of projectile now offset "higher" wrt to vehicle body

        // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
        float mag = -0.15f; // scale the '-1' accordingly for magnitifdue of forward "velocity"

        vvv.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation));

        return vvv;
    }


    @Override
    public void update(Entity featureEnt) {

        // i believe sensor was intended to be able to exist w/o a model instance, thus origin
        // has to be stored sepeatetely in vT

        // so i need to reset vT to present position
        ModelInstance instance = featureEnt.getComponent(ModelComponent.class).modelInst;

        instance.transform.getTranslation(tmpV);    ///  set  vT  to getTranslation() ????

        vT.set(tmpV); ///  set  vT  to getTranslation() ????

        // update the sensor: BUT check target validity, because of omni sensor auto-player-target selection default kludgey behavior!!!!!
        if (null != target) {

            super.update(featureEnt);


            if (isTriggered) {
//            target.getComponent(StatusComponent.class).lifeClock = 0;

                featureEnt.add(new StatusComponent(true)); // kill this projectile


                // mark dead entity for deletion        could Status Comp use "die" to time "fade-out"?
                target.add(new StatusComponent(true));

                FeatureComponent fc = target.getComponent(FeatureComponent.class);

                if (null != fc) {

                    FeatureAdaptor fa = target.getComponent(FeatureComponent.class).featureAdpt;
// h mmmm better b carful here
//                        fa.init(engine); // ha hackity BS !


// "tANKS" etc. ?? "
                    fa.update(target); // hmmm ... hadn't anticicpated this being called directly, pass the picked as update()!!!

                } else {

// "tANKS" etc. presenetly are hare ..

                    // do it the "common" way!
                CompCommon.explode(null, target);
                }
            }
        }

        // move the projectile by one step vector
        Vector3 vF = dVector;

        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        if (null != rayPickObject) {

            ModelComponent mc = featureEnt.getComponent(ModelComponent.class);
            ModelInstance mi = mc.modelInst;
            if (mi.materials.size > 0) {
                ModelInstanceEx.setColorAttribute(mi, new Color(Color.PINK), 0.5f); //tmp?
            }

            // tmp test (stick to wall ;)
//            featureEnt.add(new StatusComponent(true));
        } else {

            // no collision imminient so keep it moving along

            instance.transform.trn(vF);
        }
    }
}