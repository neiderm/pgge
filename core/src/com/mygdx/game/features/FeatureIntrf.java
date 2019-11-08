package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;

/**
 * Created by neiderm on 7/5/2019.
 */

public interface FeatureIntrf {

    void update(Entity e);
//    void onCollision(Entity e, int id);
    void onActivate(Entity e);
    void init(Object asdf);
}
