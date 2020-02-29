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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 * <p>
 * https://free3d.com/3d-model/t-55-12298.html
 * https://free3d.com/3d-model/veteran-tiger-tank-38672.html
 */
public class KillSensor extends OmniSensor {


    public enum ImpactType {
        FATAL,
        DAMAGING,
        ACQUIRE
    }


    /*
     */
    static void makeBurnOut(ModelInstance mi, ImpactType useFlags) {

        Material saveMat = mi.materials.get(0);

        TextureAttribute tmpTa = (TextureAttribute) saveMat.get(TextureAttribute.Diffuse);
        TextureAttribute fxTextureAttrib = null;

        if (null != tmpTa) {
            Texture tt = tmpTa.textureDescription.texture;
            fxTextureAttrib = TextureAttribute.createDiffuse(tt);

//            fxTextureAttrib = (TextureAttribute)tmpTa.copy(); // idfk maybe toodo
        }

        Vector3 translation = new Vector3(); // tmp for new vector instance .. only need to feed the GC relavitvely few of thsesei guess

        CompCommon.spawnNewGameObject(
                null, mi.transform.getTranslation(translation),
                new BurnOut(fxTextureAttrib, useFlags), "sphere");
    }

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Quaternion orientation = new Quaternion();
    private final Vector3 vF = new Vector3();

    public KillSensor() {

        impactType = ImpactType.DAMAGING; // can be set in scene file
    }

    public KillSensor(Entity target, Vector3 vFp) {

        this();

        this.target = target;

        // proj. sense radius (provde constructor arg)
        this.vS.set(1, 0, 0); // vS.x + projectile_radius = radiys of the kill sensor

        this.vF.set(vFp);
    }

    // probably get rid of this one
    public KillSensor(Entity target, Matrix4 mtransform) {

        this();

        this.target = target;

// proj. sense radius (provde constructor arg)
        this.vS.set(1, 0, 0); // vS.x + projectile_radius = radiys of the kill sensor

        vF.set(getDirectionVector(mtransform));
    }


    /*
     * doesn't do much but get a vector for the shooters forwared-orientation and scale to projectile movement delta vector
     */
    // probably get rid of this one
    private Vector3 getDirectionVector(Matrix4 shootersTransform) {

        Vector3 vvv = new Vector3();

        shootersTransform.getRotation(orientation);

        // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
        float mag = -0.15f; // scale the '-1' accordingly for magnitifdue of forward "velocity"

        vvv.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation));

        return vvv;
    }


    @Override
    public void update(Entity projectile) {

        // update the sensor: BUT check target validity, because of
        // omni sensor auto-player-target selection default kludgey behavior!!!!!
        if (null != target) {

            super.update(projectile);

            if (isTriggered && isActivated) {

                isActivated = false; // don't let it "detonate" again!

                // simple obvious action is to terminate this Projectile
                if (null == projectile.getComponent(StatusComponent.class)) {                         // be careful not to overwrite!
                    projectile.add(new StatusComponent(0));
                }else{
                    projectile.getComponent(StatusComponent.class).lifeClock = 0;
                }

// some problems w/ this include ... projectile still triggered but fwtfer still on the go, ... and target in-the-process-of-being-destroyed ..
                if (null == target.getComponent(StatusComponent.class)) {
                    target.add(new StatusComponent(0)); // default lifeclock should be 0
                }

                FeatureComponent fc = target.getComponent(FeatureComponent.class);

                if (null != fc) {
                    FeatureAdaptor fa = fc.featureAdpt;
                    fa.update(target); // hmmm ... hadn't anticicpated this being called directly, pass target as arg to update()!!!
                }
            }
        }

        // move the projectile by one step vector
        ModelComponent fmc = projectile.getComponent(ModelComponent.class);// could cache this model comp lookup?

        fmc.modelInst.transform.getTranslation(tmpV);
        btCollisionObject rayPickObject = BulletWorld.getInstance().rayTest(tmpV, vF, 1.0f);

        if (null != rayPickObject) {
// stopped projectile
            if (fmc.modelInst.materials.size > 0) {
                ModelInstanceEx.setColorAttribute(fmc.modelInst, new Color(Color.PINK), 0.5f); //tmp?
            }
        } else {
            // no collision imminient so keep it moving along
            fmc.modelInst.transform.trn(vF);
        }
    }
}
