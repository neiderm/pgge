package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;

public interface CollisionProcessorIntrf {

    void onCollision(Entity myCollisionObject) ;

    boolean processCollision(Entity myCollisionObject) ;
}
