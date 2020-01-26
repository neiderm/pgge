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
package com.mygdx.game.animations;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

public class RigAnimation extends AnimAdapter {

    private Quaternion turretRotation = new Quaternion();
    private Vector3 down = new Vector3(0, 1, 0);
    private Vector3 trans = new Vector3();
    private Node featureNode;
    private int turretIndex;
    private ModelComponent mc;
    private BulletComponent bc;


    public RigAnimation() {
        //mt
    }

//    @Override
//    public void init(Object target) {
//
//        super.init(target);
//    }


    @Override
    public void update(Entity sensor) {


        GameWorld.GAME_STATE_T gameState = GameWorld.getInstance().getRoundActiveState();


        if (GameWorld.GAME_STATE_T.ROUND_ACTIVE == gameState ||
                GameWorld.GAME_STATE_T.ROUND_COMPLETE_WAIT == gameState){
        /*
         should be one-time init
         */
            if (null == featureNode) {

                mc = sensor.getComponent(ModelComponent.class);

                if (null != mc) {
                    for (int index = 0; index < mc.modelInst.nodes.size; index++) {

                        Node nnnn = mc.modelInst.nodes.get(index);
                        if (nnnn.id.equals(strMdlNode)) {
                            featureNode = nnnn;
                            turretIndex = index;
                            break;
                        }
                    }
                }

                bc = sensor.getComponent(BulletComponent.class);
            }


            /*
             * update
             */
            if (null != mc){

//                fadeIn(mc.modelInst);


                if (null != featureNode ) {

                    trans.set(featureNode.translation);
//        trans.y += .01f; // test
                    featureNode.translation.set(trans);

                    turretRotation.set(featureNode.rotation);

                    float rfloat = turretRotation.getAngleAround(down);
                    rfloat += 1; // test
                    turretRotation.set(down, rfloat);
                    featureNode.rotation.set(turretRotation);

                    if (null != mc) {
                        mc.modelInst.calculateTransforms(); // definately need this !
                    }

// update child collision shape
                    if (null != bc) {
                        btCompoundShape btcs = (btCompoundShape) bc.shape;

                        if (null != btcs && bc.shape.isCompound()) {
                            btcs.updateChildTransform(turretIndex, featureNode.globalTransform);
                        }

// Apparently this is NOT needed here (for static/kinematic bodies, YES) conflicting with BUllet
// update of the body ... puts a significant load/slo-down of bullet update for this body!
//                    bc.body.setWorldTransform(mc.modelInst.transform);
                    }
                }
            }
        }
    }

    private int faderNodeIndex = -1;
    private float alphaPcnt;
    private final int ONE_SEC = 60;
    private final float FADE_TIME = (0.6f) * ONE_SEC; // component fade time slightlty lesss than 1 frame
    private final float alphaIncrement = 100f / FADE_TIME; // 100 %cnt in 60frames (1 sec)

    private void fadeIn(ModelInstance mInstance){
        // nodes array defaults to top level of nodes in the model, but if everything is under node[0]
        // then set the array to node[0] children instead
        Array<Node> nodeArray = mInstance.nodes;


            if (faderNodeIndex < 0){
// initialize the node array according to the model structure
            if (mInstance.nodes.get(0).hasChildren()){

                nodeArray = (Array<Node>) mInstance.nodes.get(0).getChildren();
            }

            // first time iterate thru node array and set each alpha to 0
            for (Node node : nodeArray){

                Material mat  = null;
                if (node.parts.size > 0){
                    // be careful of non-mesh nodes (e.g. lights cameras from model)
                    mat = node.parts.get(0).material;

                    final float zerAlpha = 0;
                    BlendingAttribute blendingAttribute =
                            new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, zerAlpha  );

                    mat.set(blendingAttribute);
                }
            }
            faderNodeIndex = 0;
        }

        if (alphaPcnt < 100){
            alphaPcnt += alphaIncrement;
        }
        else{
            alphaPcnt = 0;
            faderNodeIndex += 1;
        }


        if (faderNodeIndex < nodeArray.size){

            Node node = nodeArray.get(faderNodeIndex);

            if (node.parts.size > 0){
                // be careful of non-mesh nodes (e.g. lights cameras from model)  need to be careful of null materials as well ;)
                Material mat = node.parts.get(0).material;

                BlendingAttribute blendingAttribute =
                        new BlendingAttribute(true, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, alphaPcnt/100 );

                mat.set(blendingAttribute);
            }
        }
    }
}
