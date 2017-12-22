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
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.Managers.EntityFactory;

/**
 * Created by mango on 12/18/17.
 */

public class RenderSystem extends EntitySystem implements EntityListener {

    //    private Engine engine;
    private ImmutableArray<Entity> entities;

    private final ModelBuilder modelBuilder = new ModelBuilder();

    public RenderSystem(Engine engine, Environment environment, PerspectiveCamera cam ) {

        Vector3 tmpV = new Vector3();
        Matrix4 tmpM = new Matrix4();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        Model cube = modelBuilder.createBox(2f, 2f, 2f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        EntityFactory.boxTemplateModel = cube;  // must set the visual templates before using.

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        Model ball = modelBuilder.createSphere(2f, 2f, 2f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        EntityFactory.ballTemplateModel = ball;

if (false) {
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

    }

    @Override
    public void update(float deltaTime) {


        for (Entity e : entities) {

        }
    }

    @Override
    public void entityAdded(Entity entity) {

    }

    @Override
    public void entityRemoved(Entity entity) {
    }
}
