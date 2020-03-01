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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.util.ModelInstanceEx;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 */
public class KillSensor extends OmniSensor {

    // working variables
    private final Vector3 tmpV = new Vector3();
    private final Quaternion orientation = new Quaternion();


    public KillSensor() {

        impactType = ImpactType.DAMAGING; // can be set in scene file
    }

    public KillSensor(Entity target) {

        this();

        this.target = target;

        // proj. sense radius (provde constructor arg)
        this.vS.set(1, 0, 0); // vS.x + projectile_radius = radiys of the kill sensor
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
        //terminate this Projectile
        projectile.getComponent(StatusComponent.class).lifeClock = 0;

        // updates the damage status on the target
        super.update(projectile);
    }


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
}
