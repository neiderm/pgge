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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */
public class KillSensor {

    public enum ImpactType {
        FATAL,
        DAMAGING,
        ACQUIRE
    }


    private Node featureNode;
    private int turretIndex = -1;
    private final Vector3 down = new Vector3(0, -1, 0);
    private final Quaternion turretRotation = new Quaternion();

    private ModelInstance mi;
    private btCompoundShape btcs;
    private btRigidBody body;


    public KillSensor(ModelInstance mi, btCollisionShape bs, btRigidBody body) {

        this.mi = mi;

        if (bs.isCompound()) {

            this.btcs = (btCompoundShape) bs;
            this.body = body;
        }

        String strMdlNode = "Tank_01.003";

        // "unroll" the nodes list so that the index to the bullet child shape will be consistent
        int index = PrimitivesBuilder.getNodeIndex(mi.nodes, strMdlNode);

        if (index >= 0) { // index != -1
            featureNode = mi.getNode(strMdlNode, true);  // recursive
            turretIndex = index;
        }
    }

    public void updateControls(float[] analogs, boolean[] switches) {
//        if (null != mc)
        {
            if (null != featureNode) {
//                trans.set(featureNode.translation);
//            trans.y += .01f; // test
//            featureNode.translation.set(trans);
                turretRotation.set(featureNode.rotation);

                float rfloat = turretRotation.getAngleAround(down);

                rfloat += analogs[0];

                turretRotation.set(down, rfloat);
                featureNode.rotation.set(turretRotation);
            }


            mi.calculateTransforms(); // definately need this !


            if (null != btcs && null != body && null != featureNode && null != mi.transform) {
// update child collision shape
                btcs.updateChildTransform(turretIndex, featureNode.globalTransform);

                body.setWorldTransform(mi.transform);
            }
        }
    }


    /*
    The thing that is going 'gaBoom' should be able to specify Material texture,  Color Attr. only)
    (or else if no Texture Attrib. then we assign a default (fire-y!!) one! ?

     IN: points : because floating signboarded  points
    */
    static void makeBurnOut(ModelInstance mi, KillSensor.ImpactType useFlags) {

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
