package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.mygdx.game.Managers.EntityFactory;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.RenderSystem;


/**
 * Created by mango on 12/18/17.
 */

public class GameScreen implements Screen {

    private MyGdxGame game;
//    public AssetManager assets;
//    private Model landscapeModel;

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)

    private PerspectiveCamera cam;
    //    public ModelBatch modelBatch;
    private Model model;
    private ModelInstance instance;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    //    Sprite box;
    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

//    private PlayerComponent playerComp;
//    private btRigidBody playerBody;

    private static int touchBoxW, touchBoxH, gameBoxW, gameBoxH;


    /*
     * my multiplexed input adaptor

TODO: something screwing up camera when virtual touchpad is used
     */
    private class MyInputAdapter extends InputAdapter {

        private int touchDownCt = 0;
        private int touchUpCt = 0;

        private boolean isTouchInPad = false;

        // create a location rectangle for touchbox (in terms of screen coordinates!)
        private Rectangle touchBoxRect = new Rectangle(
                Gdx.graphics.getWidth() / 2 - touchBoxW / 2,
                Gdx.graphics.getHeight() - touchBoxH,
                touchBoxW, touchBoxH);

        private void setVector(int screenX, int screenY) {

            Vector2 ctr = new Vector2();
            touchBoxRect.getCenter(ctr);
            //          playerComp.vvv.x = screenX - ctr.x;
            //        playerComp.vvv.y = 0;
            //      playerComp.vvv.z = screenY - ctr.y;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

            if (touchBoxRect.contains(screenX, screenY)) {

                Gdx.app.log(this.getClass().getName(), String.format("touchDown%d x = %d y = %d", touchDownCt++, screenX, screenY));

                isTouchInPad = true;
                setVector(screenX, screenY);

                return true;
            }
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {

            if (touchBoxRect.contains(screenX, screenY)) {

//                Gdx.app.log(this.g0etClass().getName(), String.format("x = %d y = %d", screenX, screenY));

                isTouchInPad = true;
                setVector(screenX, screenY);
                return true;
            } else if (isTouchInPad) {
                // still touching, but out of bounds, so escape it
//                isTouchInPad = false; // keep handling the touch, but no movement, and no transition to camera movement until touch is released

//                playerComp.vvv = new Vector3(0,0,0); // let motion continue while touch down?
                return true;
            }
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {

            Gdx.app.log(this.getClass().getName(), String.format("touch up %d x = %d y = %d", touchUpCt++, screenX, screenY));

            if (isTouchInPad) {
//                isTouchInPad = false;
                //              playerComp.vvv = new Vector3(0,0,0);

// TODO: ? on touchUP: counter the force applied by the "joystick", but allow energy of a bounce to persist

                return true;
            }
            return false;
        }
    }


    public GameScreen(MyGdxGame game) {

        this.game = game;

        touchBoxW = Gdx.graphics.getWidth() / 4;
        touchBoxH = Gdx.graphics.getHeight() / 4;
        gameBoxW = Gdx.graphics.getWidth();
        gameBoxH = Gdx.graphics.getHeight() - touchBoxH;
//        gameBoxH = Gdx.graphics.getHeight();

        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(
                new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, gameBoxW, gameBoxH);
        cam.position.set(3f, 7f, 10f);
        cam.lookAt(0, 4, 0); //         cam.lookAt(0, -2, -4);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f,
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);

        instance.transform.scale(2.0f, 2.0f, 2.0f);

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new MyInputAdapter());
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);


        // make sure add system first before other entity creation crap, so that the system can get entityAdded!
/*
            assets = new AssetManager();
            assets.load("data/landscape.g3db", Model.class);
            assets.finishLoading();
*/
        addSystems();
        addEntities();


        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);

        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();
        //      box = new Sprite(new Texture("cube.png"));
        //      box = new Sprite();
        //      box.setPosition(0, 0);
        shapeRenderer = new ShapeRenderer();
    }


    void addEntities() {

        EntityFactory.CreateEntities(engine /*, assets */);
//        engine.addEntity(EntityFactory.createGround(new Vector3(0, 0, 0)));
        //      engine.addEntity(EntityFactory.createWall(new Vector3(0, 1, 12)));
        //    engine.addEntity(EntityFactory.createWall(new Vector3(0, 1, -12)));
        //  engine.addEntity(EntityFactory.createRamp(new Vector3(0, 0.5f, 0)));

//        Entity e = EntityFactory.createPlayer(new Vector3(0, 1.5f, 0), 5.0f);
//        engine.addEntity(e);

        //      playerComp = e.getComponent(PlayerComponent.class);
        //    playerBody = e.getComponent(BulletComponent.class).body;
    }

    private void addSystems() {

        Bullet.init(); // must be done before any bullet object can be created

        engine = new Engine();

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));

//        landscapeModel = assets.get("data/landscape.g3db", Model.class);
        engine.addSystem(bulletSystem = new BulletSystem(engine));

        //    engine.addSystem(new EnemySystem());
        //  engine.addSystem(new PlayerSystem(this.game));
    }


    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {

        camController.update();

        // game box viewport
        Gdx.gl.glViewport(0, touchBoxH, gameBoxW, gameBoxH);
        //         Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


//        modelBatch.begin(cam);
//        modelBatch.render(instance);

        engine.update(delta);

//        modelBatch.end();

        // GUI viewport (full screen)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();

        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());

        //if (null != playerBody)
        {
            String s;
            s = String.format("one");
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("two");
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            s = String.format("three");
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }
//        box.draw(batch);

        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(
                (float)Gdx.graphics.getWidth() / 2 - touchBoxW / 2, 0,
                touchBoxW, touchBoxH);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff

// The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the model will automatically make all instances invalid!

        EntityFactory.dispose(); // static dispose models

//        modelBatch.dispose();
        model.dispose();

        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();

//        landscapeModel.dispose(); ... hmmm ok that's what asset mgr for ! ;)
//        assets.dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }
}