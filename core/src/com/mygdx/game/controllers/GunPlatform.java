/*
 * Copyright (c) 2021 Glenn Neidermeier
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
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.features.PhysProjectile;
import com.mygdx.game.features.Projectile;
import com.mygdx.game.features.SensProjectile;
import com.mygdx.game.screens.Gunrack;
import com.mygdx.game.screens.InputMapper;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * rigs models that have some kind of rotating gun - if the barrel is separate node from turret then
 * the barrel can be childed to the turret
 * <p>
 * https://free3d.com/3d-model/t-55-12298.html
 * https://free3d.com/3d-model/veteran-tiger-tank-38672.html
 */
public class GunPlatform extends CharacterController {
    // projectile initial vector is body+turret+barrel orientation
    private final Vector3 prjectileS0 = new Vector3();
    private final Vector3 yAxis = new Vector3(0, 1, 0);
    private final Vector3 xAxis = new Vector3(1, 0, 0);
    private final ModelInstance mi;

    private btCompoundShape btcs;
    private Node gunNode;
    private Node turretNode;
    private Gunrack gunrack;
    private int energizeTime = 70; // based on the menu timing (70 frames is just over 1 second)
    private int gunIndex = -1;
    private int turretIndex = -1;
    private float rTurret;
    private float rBarrel;

    private static final int BUTTON_0 = 0;

    public GunPlatform(ModelInstance mi, btCollisionShape bs, Gunrack gunrack, boolean delay) {

        this.gunrack = gunrack;
        this.mi = mi;

        if (!delay) {
            this.energizeTime = 0;
        }

        if (bs.isCompound()) {
            this.btcs = (btCompoundShape) bs;
        }

        String strTurretNode = "Turret";
        // "unroll" the nodes list so that the index to the bullet child shape will be consistent
        int index = PrimitivesBuilder.getNodeIndex(mi.nodes, strTurretNode);

        if (index >= 0) {
            turretNode = mi.getNode(strTurretNode, true);  // recursive
            turretIndex = index;
        }

        String strBarrelNode = "Main_Gun";
        // "unroll" the nodes list so that the index to the bullet child shape will be consistent
        index = PrimitivesBuilder.getNodeIndex(mi.nodes, strBarrelNode);

        if (index >= 0) {
            gunNode = mi.getNode(strBarrelNode, true);  // recursive
            gunIndex = index;
        }
    }

    @Override
    public void updateControls(float time) {

        float anlgAxisX = controlBundle.getAxis(InputMapper.VIRTUAL_X1_AXIS); // analogX1;
        float anlgAxisY = controlBundle.getAxis(InputMapper.VIRTUAL_Y1_AXIS); // analogY1;
        boolean button0 = controlBundle.getCbuttonState(BUTTON_0);

        if (energizeTime > 0) {
            energizeTime -= 1;
            return;
        }

        if (null != turretNode) {
            // turret origin would probably be center of rig model and will look screwy if it goes too far out of range
            float rfloat = turretNode.rotation.getAngleAround(yAxis) - anlgAxisX;
            // center is at 180
            if (rfloat > 120 && rfloat < 240) {
                rfloat -= anlgAxisX;
                turretNode.rotation.set(yAxis, rfloat);
            }
            rTurret = turretNode.rotation.getAngleAround(yAxis) - 180;
        }

        if (null != gunNode) {
            // gun barrel origin would probably be center of rig model and will look screwy if it goes too far out of range
            float rfloat = gunNode.rotation.getAngleAround(xAxis) + anlgAxisY;
            // offset the gun angle to a range that makes sense
            float elevation = -rfloat;
            if (rfloat > 180) {
                elevation = 360 - rfloat;
            }
            // allow a small emount of negative elevation (below level)
            if (elevation > -10 && elevation < 30) {
                rfloat += anlgAxisY;
                gunNode.rotation.set(xAxis, rfloat);
            }
            // check rotation angle for sign?
            rBarrel = (180 - gunNode.rotation.getAngleAround(xAxis) + 180);
        }

        // update Transforms
        mi.calculateTransforms(); // definately need this !

        if (null != btcs && null != mi.transform) {
            // update child collision shape
            if (null != turretNode) {
                btcs.updateChildTransform(turretIndex, turretNode.globalTransform);
            }
            if (null != gunNode) {
                btcs.updateChildTransform(gunIndex, gunNode.globalTransform);
            }
        }

        /*
         *set the basic gun sight vector, but is definately not onesizefitsall
         */
        prjectileS0.set(0, 0.6f, 0 - 1.3f);
        prjectileS0.rotate(xAxis, rBarrel);
        prjectileS0.rotate(yAxis, rTurret);

        if (button0) {
            if (null != gunrack && gunrack.fireWeapon() >= 0) {
                //if (gunrack.fireWeapon() >= 0)
                {
                    // a shot can be fired
                    fireProjectile(mi.transform, gunrack.getSelectedWeapon());
                }
            } else {
                // gunrack is null, but a standard projectile can still be fired
                fireProjectile(mi.transform, Gunrack.WeaponType.UNDEFINED);
            }
        }
    }

    private final Vector3 vFprj = new Vector3();
    private final Vector3 trans = new Vector3();
    private final Vector3 tmpV = new Vector3();
    private final Matrix4 tmpM = new Matrix4();
    private final Quaternion qTemp = new Quaternion();
    private final Quaternion qBody = new Quaternion();

    private void fireProjectile(Matrix4 srcTrnsfm, Gunrack.WeaponType weapon) {

        if (null != srcTrnsfm) {

            srcTrnsfm.getRotation(qBody);
            ModelInstanceEx.rotateRad(prjectileS0, qBody); // rotate the resulting offset vector to orientation of Rig

            srcTrnsfm.getTranslation(trans); // start coord of projectile = Rig center + offset
            trans.add(prjectileS0); // start coord of projectile = Rig center + offset

            // copy src rotation & translation into the tmp transform, then rotate to gun/turret orientation
            tmpM.set(srcTrnsfm);
            tmpM.rotate(yAxis, rTurret); // rotate the body transform by turrent rotation about Y-axis (float degrees)
            tmpM.rotate(xAxis, rBarrel);

            // set unit vector for direction of travel for theoretical projectile fired perfectly in forward direction
            float mag = -0.15f; // scale accordingly for magnitidue of forward "velocity"
            vFprj.set(ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), tmpM.getRotation(qTemp)));

            switch (weapon) {
                default:
                case UNDEFINED:
                case STANDARD_AMMO:
                    Entity target = gunrack.getHitDetector().getEntity();
                    if (null != target) {
                        CompCommon.spawnNewGameObject(
                                new Vector3(0.1f, 0.1f, 0.1f), trans,
                                new SensProjectile(target, vFprj),
                                "box");
                    } else {
                        CompCommon.spawnNewGameObject(
                                new Vector3(0.1f, 0.1f, 0.1f), trans,
                                new Projectile(vFprj),
                                "cone");
                    }
                    break;
                case HI_IMPACT_PRJ:
                    CompCommon.spawnNewGameObject(
                            new Vector3(0.1f, 0.1f, 0.1f), trans,
                            new PhysProjectile(vFprj),
                            "sphere", 0.2f);
                    break;
                case PLASMA_GRENADES:
                    CompCommon.spawnNewGameObject(
                            new Vector3(0.1f, 0.1f, 0.1f), trans,
                            new PhysProjectile(vFprj),
                            "capsule", 0.2f);
                    break;
            }
        }
    }
}
