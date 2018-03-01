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
    }

    @Override
    public void removedFromEngine(Engine engine) {

        modelBatch.dispose();
    }

    @Override
    public void update(float deltaTime) {

        modelBatch.begin(cam);

        for (Entity e : entities) {

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
if (null != pc)
{
    ModelInstance lineInstance = raytest(mc.modelInst.transform, bc.body);
    modelBatch.render(lineInstance, environment);
}
//*/
                }
            }
            modelBatch.render(mc.modelInst, environment);
        }
        modelBatch.end();
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
    ModelInstance line(Vector3 from, Vector3 b) {

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
