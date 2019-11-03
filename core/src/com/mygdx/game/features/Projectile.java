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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.util.ModelInstanceEx;

public class Projectile extends KillSensor {

    public Projectile() { // mt
    }

//    public Projectile(Vector3 vvv) {
    //  }

    public Projectile(Object target) {

        this.userData = target;// target entiy ... could use ModelComponent.class).modelInst.transform ??????

// proj. sense radius (provde constructor arg)
// TODO: generaly this would be radius or half-extent of projectile geometry!
        this.vS.set(0.76f, .76f, .76f); // radiys of the kill sensor

/*
tmp this part  is obviously hardlocked to the player!
 */
        GameFeature pf = GameWorld.getInstance().getFeature("Player");
        Entity pp = pf.getEntity();
        ModelComponent mc = pp.getComponent(ModelComponent.class);

        // i believe sensor was intended to be able to exist w/o a model instance, thus sensor origin
        // may be found stored explicitly in vT in some sensors. however, doesn't seem to need use of
        // vT for antyhing else and instead is needed to have a 3d transform of originating "shooter"
        // in order to set up the moving step vector (in term of the shooter facing orientiation)
        vT.set( getDirectionVector( mc.modelInst.transform ));
    }

    public Projectile(Object target, Matrix4 mtransform) {

        this.userData = target;// target entiy ... could use ModelComponent.class).modelInst.transform ??????

// proj. sense radius (provde constructor arg)
// TODO: generaly this would be radius or half-extent of projectile geometry!
        this.vS.set(0.76f, .76f, .76f); // radiys of the kill sensor

        // i believe sensor was intended to be able to exist w/o a model instance, thus sensor origin
        // may be found stored explicitly in vT in some sensors. however, doesn't seem to need use of
        // vT for antyhing else and instead is needed to have a 3d transform of originating "shooter"
        // in order to set up the moving step vector (in term of the shooter facing orientiation)
        vT.set( getDirectionVector( mtransform ));
    }

    /*
     *   had to change Feature Adaptor to call init() regdless of passing null 'object')
     */
    @Override
    public void init(Object object) {

        // be sure appropriate vS (proj. sense radis) and vT (0, 0, 0) which IAIR is an (relative/offset) positioning vector!
//        this.vS.set(0.6f, 0.6f, 0.6f); // radiys of the kill sensor
//        this.vT.set(0,0,0); // (relative/offset) positioning vector!
        /*
        this.omniRadius.set(vS);
        this.sensorOrigin.set(vT);
         */
        super.init(object); // sets vS->omniradius && vT->sensorOrigin (offset... usually 0,0,0 for proj.)

        /*
         * todo can be moved in to the sensor!!!
         */
//        if (null != object)
        {
            this.target = (Entity) object; // it's a moving target
        }
    }


    // working variables
    private Vector3 tmpV = new Vector3();
    private Quaternion orientation = new Quaternion();

    /*
     * doesn't do much but get a vector for the shooters forwared-orientation and scale to projectile movement delta vector
     */
    private Vector3 getDirectionVector(Matrix4 shootersTransform) {

        Vector3 vvv = new Vector3();

        shootersTransform.getRotation(orientation);

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

        // update the sensor: BUT check target validity, because of omni sensor auto-player-target selection default kludgey behavior!!!!!
        if (null != target) {

            super.update(featureEnt);


            if (isTriggered) {

                // simple obvious action is to terminate this Projectile
                featureEnt.add(new StatusComponent(true));

// some problems w/ this include ... projectile still triggered but fwtfer still on the go, ... and target in-the-process-of-being-destroyed ..
                StatusComponent sc = target.getComponent(StatusComponent.class);
                if (null != sc) {

                    System.out.println("already got one");
                } else// if (null == sc )
                {
                    // mark dead entity for deletion        could Status Comp use "die" to time "fade-out"?
                    //            target.getComponent(StatusComponent.class).lifeClock = 0; ... use target clock maybe idk
                    target.add(new StatusComponent(true));

                    FeatureComponent fc = target.getComponent(FeatureComponent.class);

                    if (null != fc) {

                        FeatureAdaptor fa = fc.featureAdpt;
// h mmmm better b carful here
//                        fa.init(engine); // ha hackity BS !
// "tANKS" etc. ?? "
                        fa.update(target); // hmmm ... hadn't anticicpated this being called directly, pass target as arg to update()!!!

                    } else {

// "tANKS" etc. presenetly are hare .. there were characters before there were features so that needs figured out
                        ModelComponent tmc = target.getComponent(ModelComponent.class);
                        CompCommon.exploducopia(tmc.modelInst, tmc.modelInfoIndx);
                    }
                }
            }
        }

        // move the projectile by one step vector
        Vector3 vF = vT;
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        // could cache this model comp lookup?
        ModelComponent fmc = featureEnt.getComponent(ModelComponent.class);

        if (null != rayPickObject) {

            if (fmc.modelInst.materials.size > 0) {
                ModelInstanceEx.setColorAttribute(fmc.modelInst, new Color(Color.PINK), 0.5f); //tmp?
            }
/*
 now what?     tmp test ... paintball effect splatters signboarded to wall would look cool and be fun to do
 */
// otherwise, the project feature can be stopped here
//            featureEnt.add(new StatusComponent(true));

        } else {
            // no collision imminient so keep it moving along
            fmc.modelInst.transform.trn(vF);
        }
    }
}
