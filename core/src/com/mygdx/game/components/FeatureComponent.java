package com.mygdx.game.components;

import com.badlogic.ashley.core.Component;
import com.mygdx.game.features.FeatureAdaptor;

public class FeatureComponent implements Component {

    public FeatureAdaptor featureAdpt;

//    public String name;

    public FeatureComponent() {

//        this.name = name;
    }

    public FeatureComponent(FeatureAdaptor featureAdpt){
        this .featureAdpt = featureAdpt;
    }
}
