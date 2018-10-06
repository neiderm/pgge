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
import com.mygdx.game.characters.Chaser;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SteeringTankController;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.systems.StatusSystem;
import com.mygdx.game.util.BulletEntityStatusUpdate;
import com.mygdx.game.util.GameEvent;

import java.util.Locale;

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
    private IUserInterface playerUI;
    private IUserInterface setupUI;

    private InputMultiplexer multiplexer;

    private StringBuilder stringBuilder = new StringBuilder();
    private Label label;


    private Signal<GameEvent> pickRayEventSignal;
    private boolean isPicked = false;
    private boolean roundOver = false;


    GameScreen() {

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

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(0.5f);

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

        // ok so you can add a label to the stage
        label = new Label("Loading ...", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
    }

    private void makeCameraSwitchHandler(IUserInterface ui)
    {
        Pixmap button;

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);

        ui.addInputListener(buttonBListener, button,
                (2 * Gdx.graphics.getWidth() / 4f), (Gdx.graphics.getHeight() / 9f));
        button.dispose();
    }

    private final InputListener buttonBListener = new InputListener() {
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
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {/* empty */ }
    };


    private Entity setupUICameraEntity;

    private void newRound() {

        addSystems();
        GameWorld.sceneLoader.buildArena(engine);
final Entity tank =        GameWorld.sceneLoader.createTank(engine, new Vector3(1, 11f, -5f));
final Entity ship =        GameWorld.sceneLoader.createShip(engine, new Vector3(-1, 13f, -5f));

        stage = setupUI = new IUserInterface();
        // .... setupUI is passed to CameraMan constructor to add button and handler

        // now we can make camera Man (depends on setupUI)
        setupUICameraEntity = new Entity();
        engine.addEntity(setupUICameraEntity);

        cameraMan = new CameraMan(setupUICameraEntity, this.setupUI, pickRayEventSignal, cam,
                camDefPosition, camDefLookAt,
                new GameEvent() {
                    @Override
                    public void callback(Entity picked, EventType eventType) {
                        switch (eventType) {
                            case RAY_DETECT:
                                if (null != picked) {
                                    isPicked = true; // onPlayerPicked(); ... can't do it in this context??
                                    pickedPlayer = picked;
                                    picked.remove(PickRayComponent.class);
                                    //// tmp .......
                                    if (tank == pickedPlayer)
                                        enemyTank = ship;
                                    else if (ship == pickedPlayer)
                                        enemyTank = tank;
                                    else
                                        enemyTank = null; // wtf?

                                    if (null != enemyTank)
                                        enemyTank.remove(PickRayComponent.class);
                                }
                                break;
                            case RAY_PICK:
                            default:
                                break;
                        }
                    }
                });

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);
    }


    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);
    private Entity pickedPlayer;
    private Entity enemyTank;


    private void addSystems() {

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        engine.addSystem(renderSystem = new RenderSystem(environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new CharacterSystem(pickRayEventSignal));
        engine.addSystem(new PickRaySystem(pickRayEventSignal));
        engine.addSystem(new StatusSystem());
    }


    @Override
    public void show() {
        // empty
    }

    private void onPlayerPicked() {

        isPicked = false;
        GameWorld.sceneLoader.createObjects(engine);

// plug in the picked player
        final StatusComponent sc = new StatusComponent();
        pickedPlayer.add(sc);
        sc.transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;

        sc.statusUpdater = new BulletEntityStatusUpdate() {
            private Vector3 v = new Vector3();
            @Override
            public void update() {
                v = sc.transform.getTranslation(v);
                if (v.dst2(sc.origin) > sc.boundsDst2) {
                    roundOver = true; // respawn() ... can't do it in this context??
                }
            }
        };

        // select the Steering Bullet Entity here and pass it to the character
        playerUI = new PlayerCharacter(pickedPlayer,
                        new TankController(pickedPlayer.getComponent(BulletComponent.class).body,
                                pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */));
        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        Chaser asdf = new Chaser();
        engine.addEntity(asdf.create(pickedPlayer.getComponent(ModelComponent.class).modelInst.transform));

        enemyTank.add(new CharacterComponent(
                new SteeringTankController(enemyTank, pickedPlayer.getComponent(BulletComponent.class).body)));

        makeCameraSwitchHandler(playerUI);

        multiplexer.removeProcessor(camController);
        multiplexer.removeProcessor(setupUI);
        multiplexer.addProcessor(playerUI);
        this.stage = playerUI;

//setupUI.dispose(); // catch this when we trash() ...
        engine.removeEntity(setupUICameraEntity); /// BAH

        Entity cameraEntity = new Entity();
        engine.addEntity(cameraEntity);

        cameraMan = new CameraMan(cameraEntity, playerUI, pickRayEventSignal, cam, camDefPosition, camDefLookAt,
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform);
    }

    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        String s;

        if (isPicked) {
            onPlayerPicked();
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
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
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
            label.setText(stringBuilder);
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

        if (roundOver) {
            roundOver = false;
            respawn();
        }
    }

    private void respawn() {
        //                GameWorld.getInstance().showScreen(new MainMenuScreen());
        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)
        pickedPlayer = null; // removeallentities does not nullify the entity itself?
        newRound();
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
//maybe we should do something more elegant here ...
// fixed the case where first time in setupUI, it blew chow here when I try to dispose gameUI .. duh yeh gameUI would still be null
        if (null != playerUI)
            playerUI.dispose();
        if (null != setupUI)
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
