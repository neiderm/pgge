package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by mango on 4/1/18.
 */

public abstract class EntityBuilder {

    protected Model model;
    protected Vector3 size;
    protected String rootNodeId;

    public abstract Entity create(float mass, Vector3 trans, Vector3 size);
}
