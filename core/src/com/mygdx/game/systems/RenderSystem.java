package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;

/**
 * Created by mango on 12/18/17.
 */

public class RenderSystem extends EntitySystem {

    public int visibleCount;
    public int renderableCount;
    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();

    private Environment environment;
    private PerspectiveCamera cam;

    private ModelBatch modelBatch;
    private DirectionalShadowLight shadowLight;
    private ModelBatch shadowBatch;

    //    private Engine engine;
    private ImmutableArray<Entity> entities;


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
            BulletComponent bc = e.getComponent(BulletComponent.class);

            if (null != mc && null != bc) {
                if (null != mc.modelInst && null != bc.body) {

                    // tmp, hack
                    PlayerComponent pc = e.getComponent(PlayerComponent.class);
                    if (null != pc) {
                        ModelInstance lineInstance = raytest(mc.modelInst.transform, bc.body);
                        modelBatch.render(lineInstance, environment);
                    }
                }
            }
            renderableCount += 1;

            mc.modelInst.transform.getTranslation(tmpV);
            tmpV.add(mc.center);
            if (true){
//            if (cam.frustum.sphereInFrustum(mc.modelInst.transform.getTranslation(tmpV), mc.boundingRadius)) {
//            if (cam.frustum.pointInFrustum(mc.modelInst.transform.getTranslation(tmpV))) {
                visibleCount += 1;
                modelBatch.render(mc.modelInst, environment);
            }
        } // for
        modelBatch.end();

        // now the modelinstance is (re)scaled, so do shadows now
///***
        shadowLight.begin(Vector3.Zero, cam.direction);
        shadowBatch.begin(shadowLight.getCamera());

        for (Entity e : entities) {
            ModelComponent mc = e.getComponent(ModelComponent.class);
            if (null != mc && null != mc.modelInst) {
                if (mc.isShadowed)
                    shadowBatch.render(mc.modelInst);
            }
        }
        shadowBatch.end();
        shadowLight.end();
//***/
    }


    private Vector3 axis = new Vector3();
    private Vector3 down = new Vector3();
    private Vector3 position = new Vector3();
    Quaternion rotation = new Quaternion();

    private ModelInstance raytest(
                                  Matrix4 transform,
//                                  PlayerComponent pc,
                                  btRigidBody body) {
/*
        body.getWorldTransform(transform);
        rotation = body.getOrientation();
*/
        transform.getRotation(rotation);

        down.set(0, -1, 0);

        float rad = rotation.getAxisAngleRad(axis);
        down.rotateRad(axis, rad);

        transform.getTranslation(position);
//        transform.getTranslation(position);

        return line(position, down);
//        return line(position, pc.down);
    }


    private Vector3 to = new Vector3();
    private ModelBuilder modelBuilder = new ModelBuilder();
    /*
    https://stackoverflow.com/questions/38928229/how-to-draw-a-line-between-two-points-in-libgdx-in-3d
     */
    private ModelInstance line(Vector3 from, Vector3 b) {

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("line", 1, 3, new Material());
        builder.setColor(Color.RED);

        to.set(from.x + b.x, from.y + b.y, from.z + b.z);

        builder.line(from, to);

        Model lineModel = modelBuilder.end();
        ModelInstance lineInstance = new ModelInstance(lineModel);

        return lineInstance;
    }
}
