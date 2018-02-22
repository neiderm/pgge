package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
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
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.CameraComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Components.PlayerComponent;

import javafx.print.PageLayout;

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


//*
                    PlayerComponent pc = e.getComponent(PlayerComponent.class);
if (null != pc) {
    ModelInstance lineInstance = raytest(mc.modelInst, pc, bc.body);
    modelBatch.render(lineInstance, environment);
}
//*/
                }
            }

            modelBatch.render(mc.modelInst, environment);
        }


        modelBatch.end();
    }


    private ModelInstance raytest(ModelInstance instance,
                                  PlayerComponent pc,
                                  btRigidBody body) {

        Matrix4 transform = new Matrix4();
        body.getWorldTransform(transform);

        Quaternion rotation = new Quaternion();
        Vector3 down = new Vector3();

        rotation = body.getOrientation();
//        instance.transform.getRotation(rotation);
        down.set(0, -1, 0);
        down.rotateRad(rotation.getPitchRad(), 1, 0, 0);
        down.rotateRad(rotation.getRollRad(), 0, 0, 1);
        down.rotateRad(rotation.getYawRad(), 0, 1, 0);

        Vector3 position = new Vector3();
//        instance.transform.getTranslation(tmp);
        transform.getTranslation(position);

//        line (new Vector3(0.0f, 5.0f, -5.0f), new Vector3( 0.0f, 5.0f, 5.0f));
//        return line(position, down);
//        return line(position, new Vector3(position.x, position.y + 1, position.z));
        return line(position, pc.down);
    }

    /*
    https://stackoverflow.com/questions/38928229/how-to-draw-a-line-between-two-points-in-libgdx-in-3d
     */
    ModelInstance line(Vector3 a, Vector3 b) {
        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("line", 1, 3, new Material());
        builder.setColor(Color.RED);

        Vector3 from = new Vector3(a);
Vector3 to = new Vector3(from.x + b.x, from.y + b.y, from.z - b.z); // idfk
        builder.line(from, to);

        Model lineModel = modelBuilder.end();
        ModelInstance lineInstance = new ModelInstance(lineModel);

        return lineInstance;
    }


    @Override
    public void entityAdded(Entity entity) {

    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
