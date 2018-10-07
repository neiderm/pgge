package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 4/1/18.

 * abandoned 10/2018

 */

public class BulletEntityBuilder extends BaseEntityBuilder {


    /*
     *  For the case of a static model, the Bullet wrapper provides a convenient method to create a
     *  collision shape of it:
     *   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  But in some situations having issues (works only if single node in model, and it has no local translation - see code in Bullet.java)
     */
}
