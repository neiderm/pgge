package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.SceneLoader;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.ICharacterControlManual;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.ControllerSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.systems.StatusSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.PrimitivesBuilder;

/**
 * Created by mango on 12/18/17.
 */
// make sure this not visible outside of com.mygdx.game.screens
class GameScreen implements Screen {

    //    public static SceneLoader sceneLoader = SceneLoader.instance;
    private Engine engine;

    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private CameraMan cameraMan;

    private PerspectiveCamera cam;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private IUserInterface stage;
    private GameUI gameUI;
    private GameUI setupUI;

    private InputMultiplexer multiplexer;

    private StringBuilder stringBuilder = new StringBuilder();
    private Label label;


    private Signal<GameEvent> pickRayEventSignal;
    private boolean pickBoxTouchDown = false;


    public GameScreen() {

        pickRayEventSignal = new Signal<GameEvent>();

        engine = new Engine(); // GameWorld.getInstance().engine;

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


        final InputListener buttonBListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // assert null != cameraMan
                if (cameraMan.nextOpMode())
                    multiplexer.addProcessor(camController);
                else
                    multiplexer.removeProcessor(camController);

                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // empty
            }
        };


        Pixmap button;

        gameUI = new GameUI(); // UI is not fully create()'d yet, but we can pass out the references anyways

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        gameUI.addButton(buttonBListener, button,
                (2 * Gdx.graphics.getWidth() / 4f), (Gdx.graphics.getHeight() / 9f));


        final InputListener pickBoxListener = new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {/*empty*/}
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // for now ...
                pickBoxTouchDown = true;
                return true;
            }
        };

        stage = setupUI = new GameUI();
        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, 150, 150);
        setupUI.addButton(pickBoxListener, button,
                (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);


        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);

        // ok so you can add a label to the stage
        label = new Label("Loading ...", new Label.LabelStyle(font, Color.WHITE));
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

        newRound();
    }

    void newRound() {
        stage = setupUI;
        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);
        addSystems();
        addEntities();
    }


    private Entity player;//tmp


    private void addEntities() {

        SceneLoader.buildArena(engine);

        player = SceneLoader.createShip(new Vector3(-1, 13f, -5f));
        engine.addEntity(player);
//        Entity
        player = SceneLoader.createTank(new Vector3(1, 11f, -5f));
        engine.addEntity(player);

        // a player is going to control SOMETHING. Here;s a default (we need to make it possible for AIs to operate the same character controller):
        ICharacterControlManual playerCtrlr =
                new TankController(player.getComponent(BulletComponent.class).body,
                        player.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);

        PlayerCharacter playerCharacter =
                new PlayerCharacter(player, gameUI, pickRayEventSignal, playerCtrlr);

        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        SceneLoader.createChaser1(engine, player.getComponent(ModelComponent.class).modelInst.transform);


// for now cameraMan must have a model comp in order to have position (create a new PositionComponent? that doesn't require A GRAPHUICSL OBJECT)
        Entity cameraEntity = PrimitivesBuilder.loadSphere(1f, new Vector3(0, 15f, -5f));
        engine.addEntity(cameraEntity);

// does it need to be disposed?
        cameraMan = new CameraMan(cameraEntity, gameUI, pickRayEventSignal, cam);

        cameraMan.setCameraNode("chaser1",
                null /* playerChaser.getComponent(ModelComponent.class).modelInst.transform */,
                player.getComponent(ModelComponent.class).modelInst.transform);
        cameraMan.setCameraLocation( // hack: position of fixed camera at 'home" location
                new Vector3(1.0f, 13.5f, 02f), new Vector3(1.0f, 10.5f, -5.0f));
        cameraMan.setOpModeByKey("fixed");
    }

    private void addSystems() {

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        engine.addSystem(renderSystem = new RenderSystem(environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new CharacterSystem());
        engine.addSystem(new ControllerSystem());
        engine.addSystem(new PickRaySystem(pickRayEventSignal));
        engine.addSystem(new StatusSystem());
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

        if (pickBoxTouchDown){
            multiplexer.removeProcessor(camController);
            multiplexer.removeProcessor(setupUI);
            multiplexer.addProcessor(gameUI);
            stage = gameUI;
            cameraMan.setOpModeByKey("chaser1");
            pickBoxTouchDown = false;
            SceneLoader.createObjects(engine);
        }

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update();
        engine.update(delta);


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
///*
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(255, 255, 255, 1));
        shapeRenderer.rect((Gdx.graphics.getWidth() / 2f) - 5, (Gdx.graphics.getHeight() / 2f) - 5, 10, 10);
        shapeRenderer.end();
//*/
//*//////////////////////////////

        // note: I protected for null camera system on the input hhandler ... do
        // we want to update the stage if not Done Loading?
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        // verify instance variable in current gameScreen instance (would be null until done Loading)
        if (null != player) { //tmp
            StatusComponent sc = player.getComponent(StatusComponent.class);

            if (!sc.isActive) {
//                GameWorld.getInstance().showScreen(new MainMenuScreen());
                engine.removeSystem(bulletSystem); // make the system dispose its stuff
                engine.removeSystem(renderSystem); // make the system dispose its stuff
                engine.removeAllEntities(); // allow listeners to be called (for disposal)
                newRound();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
// ??? // stage.getViewport().update(width, height, true);
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
    }

    private void trash() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

//        bulletWorld.dispose(); // ???????? ( in BulletSystem:removedFromEngine() ???????
        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        gameUI.dispose();
        setupUI.dispose();
//        SceneLoader.dispose();
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
        isPaused = false; // android clicked app icon or from "left button"
    }

    @Override
    public void hide() {
        // Game.dispose calls screen.hide()
        trash();
    }
}
