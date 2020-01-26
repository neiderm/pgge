package com.mygdx.game.animations;

import com.badlogic.ashley.core.Entity;

/*
 * based on FeatureInterface
 */
public interface AnimInterface {

    void update(Entity e);
    void init(Object asdf);
}
