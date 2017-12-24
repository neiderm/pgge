package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Managers.EntityFactory;
import com.mygdx.game.screens.physObj;

/**
 * Created by mango on 12/18/17.
 */

public class RenderSystem extends EntitySystem implements EntityListener {

    private Environment environment;
    private PerspectiveCamera cam;

    private ModelBatch modelBatch;

    private Model cube;
    private Model ball;

    private Vector3 tmpV = new Vector3();
    private Matrix4 tmpM = new Matrix4();

    //    private Engine engine;
    private ImmutableArray<Entity> entities;

    private final ModelBuilder modelBuilder = new ModelBuilder();


    public RenderSystem(Engine engine, Environment environment, PerspectiveCamera cam ) {

        this.environment = environment;
        this.cam = cam;

        modelBatch = new ModelBatch();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        cube = modelBuilder.createBox(2f, 2f, 2f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        EntityFactory.boxTemplateModel = cube;  // must set the visual templates before using.

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        ball = modelBuilder.createSphere(2f, 2f, 2f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        EntityFactory.ballTemplateModel = ball;

if (true == EntityFactory.BIGBALL_IN_RENDER ) {
    // uncomment for a terrain alternative;
    //tmpM.idt().trn(0, -4, 0);
    //new physObj(physObj.pType.BOX, tmpV.set(20f, 1f, 20f), 0, tmpM);	// zero mass = static
    tmpM.idt().trn(10, -5, 0);
    EntityFactory.CreateEntity(engine, EntityFactory.pType.SPHERE, tmpV.set(8f, 8f, 8f), 0, tmpM);
}
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

        ball.dispose();
        cube.dispose();
        modelBatch.dispose();
    }

    @Override
    public void update(float deltaTime) {

        modelBatch.begin(cam);

        for (Entity e : entities) {

            ModelComponent mc = e.getComponent(ModelComponent.class);

            if (null != mc) {
if (true == EntityFactory.RENDER) {
//    mc.modelInst.transform.mul(tmpM.setToScaling(mc.scale));
    modelBatch.render(mc.modelInst, environment);
}
            }
//            modelBatch.render(landscapeInstance, environment);

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
