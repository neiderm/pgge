package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.screens.physObj;

/**
 * Created by utf1247 on 12/21/2017.
 */

public class BulletComponent implements Component {

    // private
public
    physObj pob;


    public physObj.MotionState motionstate;
    public btCollisionShape shape;
    public btRigidBody body;

    public BulletComponent(){

    }

    public BulletComponent(Matrix4 transform){

//        motionstate = new physObj.MotionState(transform);

    }
}
