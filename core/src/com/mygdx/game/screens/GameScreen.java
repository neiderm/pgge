package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.ModelComponent;
import com.mygdx.game.inputadapters.GamePad;
import com.mygdx.game.SceneLoader;
import com.mygdx.game.actors.PlayerActor;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.CameraOperator;
import com.mygdx.game.util.GameEvent;

/**
 * Created by mango on 12/18/17.
 */
// make sure this not visible outside of com.mygdx.game.screens
class GameScreen implements Screen {

//    public static SceneLoader sceneLoader = SceneLoader.instance;
    private Engine engine;

    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private CameraOperator cameraOperator;

    private PerspectiveCamera cam;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private PlayerActor playerActor;

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private GamePad stage;

    private InputMultiplexer multiplexer;

    private StringBuilder stringBuilder = new StringBuilder();
    private Label label;

    private boolean loading;
    private AssetManager assets;

    private Signal<GameEvent> gameEventSignal;


    public GameScreen() {


        //Create the event signal
        gameEventSignal = new Signal<GameEvent>();


//        assets = SceneLoader.init(); // idfk
        loading = true;


        this.engine = new Engine(); // GameWorld.getInstance().engine;


        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(
                new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
//        cam.position.set(3f, 7f, 10f);
//        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();


        // ChangeListener, InputLister etc. implemented here, but each of those will pass off to the
        // designated receiver (object that has implemneted "InputReceiver" interface)
        stage = new GamePad();


        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);


        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);


        // ok so you can add a label to the stage
        label = new Label("", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);


        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();
        batch = new SpriteBatch();
        //      box = new Sprite(new Texture("cube.png"));
        //      box = new Sprite();
        //      box.setPosition(0, 0);
        shapeRenderer = new ShapeRenderer();


        // start this last so that other stuff will be available in render()
        assets = SceneLoader.init(); // idfk


        cameraOperator =
                new CameraOperator(cam, new Vector3(0, 7, 10), new Vector3(0, 0, 0));
    }


    private void addEntities() {

        SceneLoader.createEntities(engine);
        SceneLoader.createTestObjects(engine);

        Entity player = SceneLoader.createPlayer();
        engine.addEntity(player);

        playerActor = new PlayerActor(
                stage, // game screen decide based on the capability of the running platform
                // which GameController (abstract class derived from Stage )
                // but let actor implement the event handlers
                cameraOperator,
                player.getComponent(BulletComponent.class).body, // tmp?
                gameEventSignal);

        /*
         player actor should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        Entity playerChaser =
                SceneLoader.createChaser1(engine, player.getComponent(ModelComponent.class).modelInst.transform);

        cameraOperator.setCameraNode("chaser1",
                null /* playerChaser.getComponent(ModelComponent.class).modelInst.transform */,
                player.getComponent(ModelComponent.class).modelInst.transform);
    }

    private void addSystems() {

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new CharacterSystem());
        engine.addSystem(new PickRaySystem(gameEventSignal));
    }


    @Override
    public void show() {
        // empty
    }


    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        String s;

        cameraOperator.update(delta);
        if (cameraOperator.getIsController())
            multiplexer.addProcessor(camController);
        else
            multiplexer.removeProcessor(camController);


        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update();
        engine.update(delta);

        if (loading && assets.update()) {
            SceneLoader.doneLoading();
            // make sure add system first before other entity creation crap, so that the system can get entityAdded!
            addSystems();
            addEntities(); // this takes a long time!
            loading = false;
        }

        // verify instance variable in current gameScreen instance (would be null until done Loading)
        if (null != playerActor) playerActor.update(delta);

///*///////////////////////////////////////////
        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();

        //if (null != playerBody)
        {
            s = String.format("%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }

        if (null != renderSystem) {
            float visibleCount = renderSystem.visibleCount;
            float renderableCount = renderSystem.renderableCount;
            //s = String.format("fps=%d vis.cnt=%d rndrbl.cnt=%d", Gdx.graphics.getFramesPerSecond(), renderSystem.visibleCount, renderSystem.renderableCount);
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            stringBuilder.append(" Visible: ").append(visibleCount);
            stringBuilder.append(" / ").append(renderableCount);
            //label.setText(stringBuilder);
            font.draw(batch, stringBuilder, 0, 10);
        }

        batch.end();
//*//////////////////////////////

        //        shapeRenderer.setProjectionMatrix ????
        // semi-opaque filled box over touch area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hudOverlayColor);
        shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H / 4.0f);
        shapeRenderer.end();

//*//////////////////////////////

        // note: I protected for null camera system on the input hhandler ... do
        // we want to update the stage if not Done Loading?
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // verify instance variable in current gameScreen instance (would be null until done Loading)
        if (null != playerActor) {
            if (playerActor.died) {
                playerActor.died = false;
                GameWorld.getInstance().showScreen(new MainMenuScreen());
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
    }

    @Override
    public void dispose() {
// only reason this is here is so GameWorld can call to sceneLoader.dispose() even iff GameScreen not the active Screen
        trash();

        // HACKME HACK HACK
///*
        if (!isPaused) {
//            sceneLoader.dispose(); // static dispose models
        }
//*/
//        SceneLoader.dispose(); // static dispose models
    }

    private void trash(){

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff

        engine.removeAllEntities(); // allow listeners to be called (for disposal)

//        bulletWorld.dispose(); // ???????? ( in BulletSystem:removedFromEngine() ???????
        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        stage.dispose();
        SceneLoader.dispose();
    }


    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */
    private boolean isPaused = false;

    @Override
    public void pause() {
// android "home", "back", or "left" button all send ApplicationListener.pause() notifcation (called by Game.pause()
        isPaused = true;
    }

    @Override
    public void resume() {
        isPaused  = false; // android clicked app icon or from "left button"
    }

    @Override
    public void hide() {
        // Game.dispose calls screen.hide()
        trash();
    }
}
