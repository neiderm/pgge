package com.mygdx.game.characters;

import com.badlogic.ashley.core.Entity;

public interface IGameCharacter {

    void update(Entity entity, float delta, Object whatever);
}
