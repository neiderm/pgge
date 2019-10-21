package com.mygdx.game.features;

import com.badlogic.ashley.core.Entity;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;

/**
 * Created by neiderm on 7/5/2019.
 * <p>
 * This can be a "generic" handler for a sensor. assigned a single target Entity to be sensing for.
 */

public class KillSensor extends OmniSensor {

    @Override
    public void update(Entity sensor) {

        ModelComponent mc = sensor.getComponent(ModelComponent.class);

        // default bounding radius determined from mesh dimensions unless specified overridden in vS (tmp ... should be done in  base-class )
        if (mc.boundingRadius > 0 && vS.x == 0) {
            float adjRadius = mc.boundingRadius; // calc bound radius e.g. sphere will be larger than actual as it is based on dimensions of extents (box) so in many cases will look not close enuff ... but brute force collsision detect based on center-to-center dx of objects so that about as good as it gets (troubel detect collision w/  "longer" object e.g. the APC tank)
            this.omniRadius.set(adjRadius, adjRadius, adjRadius);
        }

// tmp test
//        Vector3 dimensions = new Vector3();
//        Vector3 center = new Vector3();
//        float boundingRadius;
//        BoundingBox  boundingBox = mc.modelInst.calculateBoundingBox(new BoundingBox());
//        boundingBox.getDimensions(dimensions);
//        boundingBox.getCenter(center);
//        float sdf = dimensions.len();
//        boundingRadius = dimensions.len() / 2f;

        super.update(sensor);

        if (isTriggered) {
// clock target probly for player, other wise probly no status comp
            StatusComponent sc = target.getComponent(StatusComponent.class);
            if (null != sc) {
                target.getComponent(StatusComponent.class).lifeClock = 0;
            }
        }
    }
}
