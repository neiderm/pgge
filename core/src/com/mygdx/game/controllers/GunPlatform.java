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
package com.mygdx.game.controllers;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.features.Projectile;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * rigs models that have some kind of rotating gun - if the barrel is separate node from turret then
 * the barrel can be childed to the turret
 * <p>
 * https://free3d.com/3d-model/t-55-12298.html
 * https://free3d.com/3d-model/veteran-tiger-tank-38672.html
 */
public class GunPlatform implements SimpleVehicleModel {

    public enum ImpactType {
        FATAL,
        DAMAGING,
        ACQUIRE
    }


    private Node gunNode;
    private int gunIndex = -1;
    private Node turretNode;
    private int turretIndex = -1;
    private final Vector3 yAxisN = new Vector3(0, -1, 0);
    private final Vector3 yAxis = new Vector3(0, 1, 0);
    private final Vector3 xAxis = new Vector3(1, 0, 0);
    private final Quaternion tmpRotation = new Quaternion();
    private final Quaternion rotTurret = new Quaternion(); // rotation
    private final Quaternion rotBarrel = new Quaternion(); // elevation
    private float rTurret;
    private float rBarrel;
    private final Vector3 prjectileS0 = new Vector3(); // projectile initial vector is body+turret+barrel orientations

    private ModelInstance mi;
    private btCompoundShape btcs;
    private btRigidBody body;


    public GunPlatform(ModelInstance mi, btCollisionShape bs, btRigidBody body) {

        this.mi = mi;

        if (bs.isCompound()) {

            this.btcs = (btCompoundShape) bs;
            this.body = body;
        }

//        String strMdlNode = "Tank_01.003";
//        String strTurretNode = "Main_Turre"; //"tank_cabine";
        String strTurretNode = "tank_cabine";

        int index;
        // "unroll" the nodes list so that the index to the bullet child shape will be consistent
        index = PrimitivesBuilder.getNodeIndex(mi.nodes, strTurretNode);

        if (index >= 0) { // index != -1
            turretNode = mi.getNode(strTurretNode, true);  // recursive
            turretIndex = index;
        }

//        String strBarrelNode = "Main_Gun"; // https://free3d.com/3d-model/veteran-tiger-tank-38672.html
        String strBarrelNode = "tenk_canhao";

        // "unroll" the nodes list so that the index to the bullet child shape will be consistent
        index = PrimitivesBuilder.getNodeIndex(mi.nodes, strBarrelNode);

        if (index >= 0) { // index != -1
            gunNode = mi.getNode(strBarrelNode, true);  // recursive
            gunIndex = index;
        }
    }

    @Override
    public void updateControls(float[] analogs, boolean[] switches, float time) {

        // model may or may not have functional turrent/barrel so set default of rotation/elevation Quats
        // to that of the owning/parent body
        this.mi.transform.getRotation(rotTurret);
        this.mi.transform.getRotation(rotBarrel);

        // start point of projectile, relative to rig (tried to manually place on muzzle of gun)
        tmpV.set(0, 0.5f, 0 - 1.3f);             // this is definately not onesizefitsall


        if (null != turretNode) {
            rotTurret.set(turretNode.rotation);
            /// hackage, turret control enable needs a key
            if (switches[2] /*Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) */) {
                float rfloat = rotTurret.getAngleAround(yAxisN);
                rfloat += analogs[0];
                tmpRotation.set(yAxisN, rfloat);
                turretNode.rotation.set(tmpRotation);

                updateTransforms();

                rTurret = rotTurret.getAngleAround(yAxisN) - 180;
            }

            tmpV.z *= -1;     // can make turret work with opposite Z .. bah
            ModelInstanceEx.rotateRad(tmpV, rotTurret); //  rotate the offset vector to orientation of vehicle
        }

        if (null != gunNode) {
            rotBarrel.set(gunNode.rotation);
            /// hackage, turret control enable needs a key
            if (switches[2] /*Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) */) {
                float rfloat = rotBarrel.getAngleAround(xAxis);
                rfloat += analogs[1];
                tmpRotation.set(xAxis, rfloat);
                gunNode.rotation.set(tmpRotation);

                updateTransforms();

                // check rotation angle for sign?

// gun barrel is screwed up and to be rotated properlyn  must be translated back to origin
//            ModelInstanceEx.rotateRad(tmpV, rotBarrel); //  rotate the offset vector to orientation of vehicle

                rBarrel = rotBarrel.getAngleAround(xAxis);
//                System.out.println("rBarrel = " + rBarrel);
            }
        }

        prjectileS0.set(tmpV);
    }

    /*
     * carve this out so it can be done selectively (only on control update actual i.e. enabled)
     */
    private void updateTransforms() {

        mi.calculateTransforms(); // definately need this !

        if (null != btcs && null != body && null != mi.transform) {
// update child collision shape
            if (null != turretNode) {
                btcs.updateChildTransform(turretIndex, turretNode.globalTransform);
            }
            if (null != gunNode) {
                btcs.updateChildTransform(gunIndex, gunNode.globalTransform);
            }

            body.setWorldTransform(mi.transform);
        }
    }


    private final Vector3 vFprj = new Vector3();
    private final Vector3 trans = new Vector3();
    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    private final Quaternion qTemp = new Quaternion();
    private final Quaternion qBody = new Quaternion();


    public void fireProjectile(Entity target, Matrix4 srcTrnsfm /*ModelInstance pMI*/) {

//        Matrix4 srcTrnsfm = null;
//        if (null != pMI) {
//            srcTrnsfm = pMI.transform;
//        }
        if (null != srcTrnsfm) {
// get model rotation & translation from instance (copy) before the Matrix gets rotated to gun/turret orientation
            tmpM.set(srcTrnsfm);

            tmpM.getRotation(qBody);

            ModelInstanceEx.rotateRad(prjectileS0, qBody); //  rotate the resulting offset vector to orientation of vehicle

            tmpM.getTranslation(trans); // start coord of projectile = vehicle center + offset

            trans.add(prjectileS0); // start coord of projectile = vehicle center + offset


            tmpM.rotate(/*down*/ yAxisN, rTurret);
// rBarrel ?????????????????

            // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
            float mag = -0.15f; // scale the '-1' accordingly for magnitifdue of forward "velocity"
            vFprj.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), tmpM.getRotation(qTemp)));


            CompCommon.spawnNewGameObject(
                    new Vector3(0.1f, 0.1f, 0.1f), trans,
                    new Projectile(target, vFprj),
                    "cone");
        }
    }
}
