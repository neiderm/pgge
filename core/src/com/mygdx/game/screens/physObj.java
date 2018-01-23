package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.mygdx.game.Managers.EntityFactory.*;



import java.util.Random;

/**
 * Created by mango on 12/18/17.
 */

public class physObj {

    private static Model model;
    private static final AssetManager assets;
    private static final Model landscapeModel;
    private static Model boxTemplateModel;
    private static Model ballTemplateModel;

    private physObj(){
    }

    static {
        final ModelBuilder mb = new ModelBuilder();

        Texture cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        boxTemplateModel  = mb.createBox(1f, 1f, 1f,
                new Material(TextureAttribute.createDiffuse(cubeTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        Texture sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        ballTemplateModel = mb.createSphere(1f, 1f, 1f, 16, 16,
                new Material(TextureAttribute.createDiffuse(sphereTex)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        assets = new AssetManager();
        assets.load("data/landscape.g3db", Model.class);
        assets.finishLoading();
        landscapeModel = assets.get("data/landscape.g3db", Model.class);


        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW))).cone(1f, 2f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(0.5f, 2f, 10);
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 2f, 1f, 10);

        model = mb.end();
    }


    public static void createEntities(Engine engine) {

        final int N_ENTITIES = 21;
        final int N_BOXES = 10;

        Vector3 tmpV = new Vector3(); // size
        Random rnd = new Random();

        for (int i = 0; i < N_ENTITIES; i++) {

            tmpV.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            tmpV.scl(2.0f); // this keeps object "same" size relative to previous model size was 2x

            Vector3 translation =
                    new Vector3 (rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                engine.addEntity(
                        new BoxObject(tmpV, boxTemplateModel).create(rnd.nextFloat() + 0.5f, translation ));
            } else {
                engine.addEntity(
                        new SphereObject(tmpV.x, ballTemplateModel).create(rnd.nextFloat() + 0.5f, translation ));
            }
        }


        Vector3 t = new Vector3(0, 0 + 25f, 0 - 5f);
        Vector3 s = new Vector3(1, 1, 1);

        engine.addEntity(
                new GameObject(s, model, "cone").create(rnd.nextFloat() + 0.5f, t,
                        new btConeShape(0.5f, 2.0f)));

        engine.addEntity(
                new GameObject(s, model, "capsule").create(rnd.nextFloat() + 0.5f, t,
                        new btCapsuleShape(0.5f, 0.5f * 2.0f)));

        engine.addEntity(
                new GameObject(s, model, "cylinder").create(rnd.nextFloat() + 0.5f, t,
                        new btCylinderShape(new Vector3(0.5f * 1.0f, 0.5f * 2.0f, 0.5f * 1.0f))));

        
        StaticEntiteeFactory<GameObject> staticFactory =
                new StaticEntiteeFactory<GameObject>();

        float yTrans = -10.0f;
        Vector3 tran = new Vector3(0, -4 + yTrans, 0);

        engine.addEntity( staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), boxTemplateModel),tran) );

        engine.addEntity( staticFactory.create(
                new SphereObject(16, ballTemplateModel), new Vector3(10, 5 + yTrans, 0)) );

        engine.addEntity( staticFactory.create(
                new BoxObject(new Vector3(40f, 2f, 40f), model, "box"), new Vector3(-15, 1, -20) ) );

        // put the landscape at an angle so stuff falls of it...
        Matrix4 transform = new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f);
        transform.trn(0, 0 + yTrans, 0);
        engine.addEntity(new LandscapeObject().create(landscapeModel, transform));
    }


    public static void dispose(){

        boxTemplateModel.dispose();
        ballTemplateModel.dispose();
        model.dispose();
        assets.dispose();
    }

}
