package com.mygdx.game.characters;

/**
 * Created by mango on 2/10/18.
 */

public interface Character {

    void update(float delta);

    /*
    this would change to something generic like Object
     */
    public void inputSet(float x, float y);
}
