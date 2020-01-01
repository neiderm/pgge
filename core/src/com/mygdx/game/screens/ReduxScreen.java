/*
 * Copyright (c) 2019 Glenn Neidermeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mygdx.game.screens;


import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;

/*
 * crude test screen with ECS, Bullet Physics, but no Scene Loader or much anything else
 *
 * Original libGDX 3D and Bullet physics demo from
 *   "http://bedroomcoders.co.uk/libgdx-bullet-redux-2/",
 * and modified to Ashley ECS (entity component system architecture).
 */
class ReduxScreen implements Screen {

    private Engine engine = new Engine();
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)

    private PerspectiveCamera cam; // has to be sent to bullet world for update debug draw
    private CameraInputController camController; // FirstPersonCameraController camController;
    private AssetManager assets;
    private Model landscapeModel;
    private ModelInstance landscapeInstance;
    private final ModelBuilder modelBuilder = new ModelBuilder();

    private Texture cubeTex;
    private Model cube ;
    private Texture sphereTex;
    private Model ball;


    @Override
    public void render(float delta) {

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0, 0, 0, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        BulletWorld.getInstance().update(delta, cam);
    }


    @Override
    public void show() {

        Environment environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        Vector3 lightDirection = new Vector3(0.5f, -1f, 0f);
        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, lightDirection);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        cam = new PerspectiveCamera(67, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        cam.up.set(0, 1, 0);
        cam.position.set(10f, 10f, 40f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        BulletWorld.getInstance().initialize();              //  screen could inherit from e.g. "ScreenWithBulletWorld" ???

        engine = new Engine();
        renderSystem = new RenderSystem(shadowLight, environment, cam);
        bulletSystem = new BulletSystem();
        engine.addSystem(renderSystem);
        engine.addSystem(bulletSystem);

        assets = new AssetManager();
        assets.load("data/landscape.g3db", Model.class);

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);

        assets.finishLoading();

        //
        // here onwards all assets ready!
        //
        cubeTex = new Texture(Gdx.files.internal("data/crate.png"), false);
        cube =
                modelBuilder.createBox(1, 1, 1, // 2f, 2f, 2f,
                        new Material(TextureAttribute.createDiffuse(cubeTex)),
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

        sphereTex = new Texture(Gdx.files.internal("data/day.png"), false);
        ball =
//                modelBuilder.createSphere(2f, 2f, 2f, 16, 16,
                modelBuilder.createSphere(1, 1, 1, 16, 16,
                        new Material(TextureAttribute.createDiffuse(sphereTex)),
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);


        // little point putting static meshes in a convenience wrapper
        // as you only have a few and don't spawn them repeatedly

        landscapeModel = assets.get("data/landscape.g3db", Model.class);

        btCollisionShape triMesh = (btCollisionShape) new btBvhTriangleMeshShape(landscapeModel.meshParts);

        // put the landscape at an angle so stuff falls of it...
//        physObj.MotionState motionstate = new physObj.MotionState(new Matrix4().idt().rotate(new Vector3(1,0,0), 20f));
        MotionState motionstate = new MotionState(
                new Matrix4().idt().rotate(new Vector3(1, 0, 0), 20f)
        );
        btRigidBody landscape = new btRigidBody(0, motionstate, triMesh);
        landscapeInstance = new ModelInstance(landscapeModel);
        landscapeInstance.transform = motionstate.transform;
//        collisionWorld.addRigidBody(landscape);

        Entity e = new Entity();
        e.add(new ModelComponent(landscapeInstance));

        BulletComponent bc = new BulletComponent(triMesh, landscapeInstance.transform, 0);
        // special sauce here for static entity
        bc.body.setCollisionFlags(
                bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
        bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
        e.add(bc);

        engine.addEntity(e);


        Vector3 size;

        size = new Vector3(20, 1, 20);

        e = PrimitivesBuilder.load(
                new ModelInstance(cube),
//                PrimitivesBuilder.getModel(), "boxTex",
                PrimitivesBuilder.getShape("boxTex", size),
                size, 0, new Vector3(0, -4, 0)); // trans

        engine.addEntity(e);


        size = new Vector3(8, 8, 8);

        e = PrimitivesBuilder.load(
                new ModelInstance(ball),
//                PrimitivesBuilder.getModel(), "sphereTex",
                PrimitivesBuilder.getShape("sphereTex", size),
                size, 0, new Vector3(10, -5, 0)); // trans

        engine.addEntity(e);


        createTestObjects(engine);
    }


    private void createTestObjects(Engine engine) {

        Random rnd = new Random();

        int N_ENTITIES = 300;
        final int N_BOXES = 200;

        Vector3 size = new Vector3();

        for (int i = 0; i < N_ENTITIES; i++) {

            size.set(rnd.nextFloat() + .1f, rnd.nextFloat() + .1f, rnd.nextFloat() + .1f);
            size.scl(2.0f); // this keeps object "same" size relative to previous primitivesModel size was 2x

            Vector3 translation =
                    new Vector3(rnd.nextFloat() * 10.0f - 5f, rnd.nextFloat() + 25f, rnd.nextFloat() * 10.0f - 5f);

            if (i < N_BOXES) {
                btCollisionShape shape = PrimitivesBuilder.getShape("boxTex", size); // note: 1 shape re-used
                engine.addEntity(
                        PrimitivesBuilder.load(
                                new ModelInstance(cube),
                                shape, size, size.x, translation));

            } else {
                btCollisionShape shape = PrimitivesBuilder.getShape("sphereTex", size); // note: 1 shape re-used
                engine.addEntity(
                        PrimitivesBuilder.load(
                                new ModelInstance(ball),
                                shape, new Vector3(size.x, size.x, size.x), size.x, translation));
            }
        }
    }


    @Override
    public void resize(int width, int height) {
    }


    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        BulletWorld.getInstance().dispose();

        assets.dispose();

        cube.dispose();
        cubeTex.dispose();
        ball.dispose();
        sphereTex.dispose();

        // I guess not everything is handled by ECS ;)
        PrimitivesBuilder.clearShapeRefs();
    }


    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */

    @Override
    public void pause() {
        // Android "Recent apps" (square on-screen button), Android "Home" (middle o.s. btn ... Game.pause()->Screen.pause()
        Gdx.app.log("TestScreen", "pause");
    }

    @Override
    public void resume() {
        // Android resume from "minimized" (Recent Apps button selected)
        Gdx.app.log("TestScreen", "resume");
    }

    @Override
    public void hide() {
        Gdx.app.log("TestScreen", "hide");
        dispose();
    }


    public static class MotionState extends btMotionState {

        public Matrix4 transform;

        public MotionState(final Matrix4 transform) {
            this.transform = transform;
        }

        @Override
        public void getWorldTransform(Matrix4 worldTrans) {
            worldTrans.set(transform);
        }

        @Override
        public void setWorldTransform(Matrix4 worldTrans) {
            transform.set(worldTrans);
        }
    }
}
