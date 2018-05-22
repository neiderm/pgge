package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.Components.ModelComponent;

/**
 * Created by mango on 12/18/17.
 */

public class RenderSystem extends EntitySystem {

    public int visibleCount;
    public int renderableCount;

    private Environment environment;
    private PerspectiveCamera cam;

    private ModelBatch modelBatch;
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;

    //    private Engine engine;
    private ImmutableArray<Entity> entities;

    private Vector3 position = new Vector3();
    public static final Array<ModelInstance> otherThings = new Array<ModelInstance>();



    public RenderSystem(Engine engine, Environment environment, PerspectiveCamera cam ) {

        this.environment = environment;
        this.cam = cam;

        modelBatch = new ModelBatch();
///***
        shadowLight = new DirectionalShadowLight(1024, 1024, 60, 60, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, -1f, -.8f, -.2f);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;
        shadowBatch = new ModelBatch(new DepthShaderProvider());
//***/
    }

    @Override
    public void addedToEngine(Engine engine) {

//        this.engine = engine;

        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(ModelComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {

        modelBatch.dispose();
///***
        shadowBatch.dispose();
        shadowLight.dispose();
//***/
    }

    @Override
    public void update(float deltaTime) {

        renderableCount = 0;
        visibleCount = 0;

        modelBatch.begin(cam);

        for (Entity e : entities) {

            ModelComponent mc = e.getComponent(ModelComponent.class);

            if (null != mc) {

                ModelInstance modelInst = mc.modelInst;

//                if (null != modelInst)
                {
                    renderableCount += 1;

                    if (isVisible(cam, modelInst.transform.getTranslation(position), mc.boundingRadius)) {
                        visibleCount += 1;
                        modelBatch.render(modelInst, environment);
                    }
                }
            }
        } // for

        for (ModelInstance modelInst : otherThings) {
            modelBatch.render(modelInst, environment);
        }
        otherThings.clear();

        modelBatch.end();


        // now the modelinstance is (re)scaled, so do shadows now
///***
        shadowLight.begin(Vector3.Zero, cam.direction);
        shadowBatch.begin(shadowLight.getCamera());

        for (Entity e : entities) {
            ModelComponent mc = e.getComponent(ModelComponent.class);
            if (null != mc && null != mc.modelInst && mc.isShadowed &&
                    isVisible(cam, mc.modelInst.transform.getTranslation(position), mc.boundingRadius)) {
                shadowBatch.render(mc.modelInst);
            }
        }
        shadowBatch.end();
        shadowLight.end();
//***/
    }


    /*
       https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
         to use radius:
          mc.modelInst.transform.getTranslation(position);
          cam.frustum.sphereInFrustum(position, mc.boundingRadius );
    */
    private boolean isVisible(PerspectiveCamera cam, Vector3 position, float radius) {

        return cam.frustum.sphereInFrustum(position, radius);
//        return cam.frustum.boundsInFrustum(position, mc.dimensions);
    }
}
