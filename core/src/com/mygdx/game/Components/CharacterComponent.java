package com.mygdx.game.Components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Matrix4;
import com.mygdx.game.systems.PIDcontrol;

/**
 * Created by mango on 2/10/18.
 */

/*
 the custom "character controller" class (i.e. for non-dynamic collision objects) would be able
 to instantiate with inserted instance of a suitable simple controller e.g. PID controller etc.
 */
public class CharacterComponent implements Component {

    // idea for using controller instance e.g. PICcontrol etc.
    /*
    CharacterComponent(CharacterController controller){
    }
   */
    public PIDcontrol controller;
    public Matrix4 transform = new Matrix4();
}
