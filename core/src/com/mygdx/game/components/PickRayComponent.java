package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;

public class PickRayComponent implements Component {

    public String objectName;

    public PickRayComponent(String objectName) {

        this.objectName = objectName;
    }
}
