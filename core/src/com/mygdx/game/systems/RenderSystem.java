package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;

/**
 * Created by mango on 12/18/17.
 */

public class RenderSystem extends EntitySystem implements EntityListener {

    private Matrix4 tmpM = new Matrix4();

    private Environment environment;
    private PerspectiveCamera cam;

    private ModelBatch modelBatch;

    //    private Engine engine;
    private ImmutableArray<Entity> entities;


    public RenderSystem(Engine engine, Environment environment, PerspectiveCamera cam ) {

        this.environment = environment;
        this.cam = cam;

        modelBatch = new ModelBatch();
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(ModelComponent.class).get());

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(Family.all(ModelComponent.class).get(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        modelBatch.dispose();
    }

    @Override
    public void update(float deltaTime) {

        modelBatch.begin(cam);

        for (Entity e : entities) {

//            CameraComponent cc = e.getComponent(CameraComponent.class);
//if (null != cc)
//    cc.test = 1;

            ModelComponent mc = e.getComponent(ModelComponent.class);
            BulletComponent bc = e.getComponent(BulletComponent.class);

            if (null != mc && null != bc) {
                if (null != mc.modelInst
                        && null != mc.scale // landscape mesh has no scale
                        && null != bc.body) {
                    if (bc.body.isActive()) {  // gdx bullet used to leave scaling alone which was rather useful...

                        if (!bc.sFlag) {
                            mc.modelInst.transform.mul(tmpM.setToScaling(mc.scale));
//                        mc.modelInst.transform.scl(mc.scale); // nfg idfk, should be same but objects are skewed on 1 or more axis
                        }
                    }
                }
            }

            modelBatch.render(mc.modelInst, environment);
        }

        modelBatch.end();
    }

    @Override
    public void entityAdded(Entity entity) {

    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
