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
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 12/18/17.
 *
 * Separate into separate RenderSystem, ShadowSystem as not all renderables are shadowed,
 * and since the shadow batch necessitates a 2nd separate iteration of entities anyway.
 * Or not. Because I don't know how to assert the order of systems update in the frame.
 */

public class RenderSystem extends EntitySystem {

    public int visibleCount;
    public int renderableCount;

    private Environment environment;
    private PerspectiveCamera cam;

    private ModelBatch modelBatch;
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;

    private Vector3 position = new Vector3();


    private ImmutableArray<Entity> entities;


    private RenderSystem() {
//        super(Family.all(ModelComponent.class).get());
    }


    public RenderSystem(DirectionalShadowLight shadowLight, Environment environment, PerspectiveCamera cam ) {

        this();
        this.environment = environment;
        this.cam = cam;

        modelBatch = new ModelBatch();

        this.shadowLight = shadowLight;
        shadowBatch = new ModelBatch(new DepthShaderProvider());
    }

    @Override
    public void addedToEngine(Engine engine) {
        // Grabs all entities with desired components
        entities = engine.getEntitiesFor(Family.all(ModelComponent.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {

        entities = null;// super.removedFromEngine(engine);
        modelBatch.dispose();

        shadowBatch.dispose();
        shadowLight.dispose();
    }

//    @Override
    private void processEntity (Entity entity, float deltaTime){

        ModelComponent mc = entity.getComponent(ModelComponent.class);
        ModelInstance modelInst = mc.modelInst;

        // only entity with valid model comp and model instance should be in here
        //assert null != mc;
        //assert null != mc.modelInst;

        renderableCount += 1;

//        if (isVisible(cam, modelInst.transform.getTranslation(position), mc.boundingRadius))
        {
            visibleCount += 1;
            modelBatch.render(modelInst, environment);
        }

        if (null != mc.animAdapter){
            mc.animAdapter.update(entity   );
        }
    }

    @Override
    public void update(float deltaTime) {

        renderableCount = 0;
        visibleCount = 0;

        modelBatch.begin(cam);

//        super.update(deltaTime);
        for (Entity e : entities)
            processEntity(e, deltaTime);

        modelBatch.end();

        // shadows or model batch first ... doesn't seem to matter
        // https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/g3d/ShadowMappingTest.java
        shadowLight.begin(Vector3.Zero.cpy(), cam.direction);
        shadowBatch.begin(shadowLight.getCamera());

        for (Entity e : entities) {
            ModelComponent mc = e.getComponent(ModelComponent.class);
            //assert null != mc;
            //assert null != mc.modelInst;
            if (mc.isShadowed
                    && isVisible(cam, mc.modelInst.transform.getTranslation(position), mc.boundingRadius)
                    ) {
                shadowBatch.render(mc.modelInst);
            }
        }
        shadowBatch.end();
        shadowLight.end();
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
