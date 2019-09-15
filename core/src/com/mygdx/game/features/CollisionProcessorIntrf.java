package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;

public interface CollisionProcessorIntrf {

    public void onCollision(Entity myCollisionObject) ;

    public boolean processCollision(Entity myCollisionObject) ;
}
