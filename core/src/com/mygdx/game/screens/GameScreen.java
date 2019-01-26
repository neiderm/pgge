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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.characters.Chaser;
import com.mygdx.game.characters.InputStruct;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.characters.PlayerInput;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SimpleVehicleModel;
import com.mygdx.game.controllers.SteeringBulletEntity;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.SteeringTankController;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.systems.StatusSystem;
import com.mygdx.game.util.BulletEntityStatusUpdate;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import java.util.Random;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;

/**
 * Created by neiderm on 12/18/17.
 */
class GameScreen implements Screen {

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private CameraMan cameraMan;
    private PerspectiveCamera cam;
    private CameraInputController camController; // FirstPersonCameraController camController;
    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch = new SpriteBatch();
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();
    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private PlayerCharacter playerUI;
    private InputMultiplexer multiplexer = new InputMultiplexer();
    private StringBuilder stringBuilder = new StringBuilder();
    private Label label;
    private Signal<GameEvent> pickRayEventSignal = new Signal<GameEvent>();
    private boolean roundOver = false;
    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);
    private Entity pickedPlayer;
    private Entity platformEntity;
    private float colorAlpha = 0.9f;
    private Color platformColor = new Color(255, 0, 0, colorAlpha);


    GameScreen() {

        GameWorld.getInstance().setIsPaused(false);

        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        Environment environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDirection));

        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
//        cam.position.set(3f, 7f, 10f);
//        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();

        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, new Vector3(0.5f, -1f, 0f));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;


        engine = new Engine();
        engine.addSystem(renderSystem = new RenderSystem(shadowLight, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new PickRaySystem(pickRayEventSignal));
        engine.addSystem(new StatusSystem());
        engine.addSystem(new CharacterSystem());

        Vector3 scale = new Vector3(4, 1, 4);
        Vector3 trans = new Vector3(0, 10, -5);
        platformEntity =
                PrimitivesBuilder.getBoxBuilder("box").create(0.0f, trans, scale);
        ModelInstanceEx.setColorAttribute(
                platformEntity.getComponent(ModelComponent.class).modelInst, platformColor);
        engine.addEntity(platformEntity);

        GameWorld.sceneLoader.buildArena(engine);

        onPlayerPicked();
    }


    private void onPlayerPicked() {

        GameWorld.sceneLoader.onPlayerPicked(engine); // creates test objects

// load the rigs and search for matching name (name of rig as read from model is stashed in PickRayComp as a hack ;)
        Array<Entity> characters = new Array<Entity>();
        GameWorld.sceneLoader.buildCharacters(characters, engine, "tanks", true); // hack object name embedded into pick component

        String objectName = GameWorld.getInstance().getPlayerObjectName();

        for (Entity e : characters) {
            if (e.getComponent(PickRayComponent.class).objectName.equals(objectName)) {
                pickedPlayer = e;
                pickedPlayer.remove(PickRayComponent.class); // component no longer needed, remove  it
            }
//            else
//                engine.removeEntity(e); // let'em become zombies ;)
        }

        characters = new Array<Entity>();

        GameWorld.sceneLoader.buildCharacters(characters, engine, "characters", false);

        for (Entity e : characters) {

            TankController tc = new TankController(e.getComponent(BulletComponent.class).body,
                    e.getComponent(BulletComponent.class).mass);/* should be a property of the tank? */
            e.add(new CharacterComponent(new SteeringTankController(tc, e,
                    new SteeringBulletEntity(pickedPlayer.getComponent(BulletComponent.class).body))));
            engine.addEntity(e);
        }

        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
          */
        Chaser chaser = new Chaser();
        engine.addEntity(chaser.create(
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform));


        cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt,
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform);

        CharacterComponent comp = new CharacterComponent(cameraMan);
        Entity cameraEntity = new Entity();
        cameraEntity.add(comp);
        engine.addEntity(cameraEntity);

// plug in the picked player
        final StatusComponent sc = new StatusComponent();
        pickedPlayer.add(sc);
        sc.transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;

        sc.statusUpdater = new BulletEntityStatusUpdate() {
            Vector3 v = new Vector3();
            @Override
            public void update() {
                v = sc.transform.getTranslation(v);
                if (v.dst2(sc.origin) > sc.boundsDst2) {
                    roundOver = true; // respawn() ... can't do it in this context??
                }
            }
        };

        setupVehicle(pickedPlayer);
    }

/*
 game event object for signalling to pickray system
 */
    private final GameEvent gameEvent = new GameEvent() {
        @Override
        public void callback(Entity picked, EventType eventType) {
            switch (eventType) {
                case RAY_PICK:
                    if (null != picked)
                        ModelInstanceEx.setMaterialColor(
                                picked.getComponent(ModelComponent.class).modelInst, Color.RED);
                    break;
                default:
                    break;
            }
        }};

    private final int GS_BTN_SZ = 75;
    private void setupVehicle(final Entity pickedPlayer){

// setup the vehicle model so it can be referenced in the mapper
        final SimpleVehicleModel vehicleModel = new TankController(
                pickedPlayer.getComponent(BulletComponent.class).body,
                pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);

        InputStruct mapper = new InputStruct() {

            btRigidBody body = pickedPlayer.getComponent(BulletComponent.class).body;
            Vector3 tmpV = new Vector3();
            Random rnd = new Random();
            final Vector3 impulseForceV = new Vector3();
            InputState preInputState;

            @Override
            public void update(float deltaT) {
                InputState nowInputState = getInputState(false);
                // have to read the button to be sure it's state is delatched and not activate in a pause!
// just an ginormoua hack right now .....
                if (InputState.INP_SELECT != preInputState && InputState.INP_SELECT == nowInputState) {

                    float nX = (Gdx.graphics.getWidth() / 2f) + (GS_BTN_SZ - GS_BTN_SZ);
                    float nY = (Gdx.graphics.getHeight() / 2f) - (GS_BTN_SZ - GS_BTN_SZ) - GS_BTN_SZ;
                    pickRayEventSignal.dispatch(gameEvent.set(RAY_PICK, cam.getPickRay(nX, nY), 0));
                }
                if (InputState.INP_JUMP != preInputState && InputState.INP_JUMP == nowInputState) {
                    // random flip left or right ( only enable jump if in surface conttact ??)

                    if (rnd.nextFloat() > 0.5f)
                        tmpV.set(0.1f, 0, 0);
                    else
                        tmpV.set(-0.1f, 0, 0);

                    body.applyImpulse(impulseForceV.set(0, rnd.nextFloat() * 10.f + 40.0f, 0), tmpV);
                }
                vehicleModel.updateControls(getAxisY(0), getAxisX(0), 0);
                preInputState = nowInputState;
            }
        };

        // select the Steering Bullet Entity here and pass it to the character
        SteeringEntity sbe = new SteeringEntity();
        final PlayerInput<Vector3> playerInpSB = new PlayerInput<Vector3>(mapper);
        sbe.setSteeringBehavior(playerInpSB);
        pickedPlayer.add(new CharacterComponent(sbe));

        setupPlayerUI(mapper);
    }


    private void setupPlayerUI(final InputStruct mapper){

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        InputListener gsListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
/*
    mapper.setInputState(InputStruct.InputState.INP_SELECT);
 */
                if (GameWorld.getInstance().getIsPaused()) {  // would like to allow engine to be actdive if ! paused but on-screen menu is up
                    roundOver = true; // will have to do for now ;)
                }
                else {
                    float nX = (Gdx.graphics.getWidth() / 2f) + (x - GS_BTN_SZ);
                    float nY = (Gdx.graphics.getHeight() / 2f) - (y - GS_BTN_SZ) - GS_BTN_SZ;
                    pickRayEventSignal.dispatch(gameEvent.set(RAY_PICK, cam.getPickRay(nX, nY), 0));
                }
                return false;
            }
        };

        InputListener buttonBListener = new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // assert null != cameraMan
                if (cameraMan.nextOpMode())
                    multiplexer.addProcessor(camController);
                else
                    multiplexer.removeProcessor(camController);

                return false;
            }
        };


        Array<InputListener> listeners = new Array<InputListener>();
listeners.add(buttonBListener);
listeners.add(gsListener);


        playerUI = new PlayerCharacter(mapper, listeners);

        label = new Label("Whatever ... ", new Label.LabelStyle(font, Color.WHITE));
        playerUI.addActor(label);


        Table table = new Table();
        table.setFillParent(true);
        table.setDebug(true);
        playerUI.addActor(table);

        Pixmap button;
        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(GS_BTN_SZ * 2, GS_BTN_SZ * 2, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, GS_BTN_SZ * 2, GS_BTN_SZ * 2);
        button.setColor(1, 0, 0, .1f);
        button.fillCircle(GS_BTN_SZ, GS_BTN_SZ, GS_BTN_SZ);   /// I don't know how you would actually do a circular touchpad area like this


        playerUI.addInputListener(gsListener,
                button, (Gdx.graphics.getWidth() / 2f) - GS_BTN_SZ, (Gdx.graphics.getHeight() / 2f) + 0);
        button.dispose();

        Pixmap.setBlending(Pixmap.Blending.None);
        button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillCircle(25, 25, 25);
        playerUI.addInputListener(
                buttonBListener, button, (2 * Gdx.graphics.getWidth() / 4f), (Gdx.graphics.getHeight() / 9f));

        button.dispose();

        multiplexer.addProcessor(playerUI);
        Gdx.input.setInputProcessor(multiplexer);
    }


    /*
     * this is kind of a hack to test some ray casting
     */
    private GameEvent nearestObjectToPlayerEvent = new GameEvent() {

        private Vector3 tmpV = new Vector3();
        private Vector3 posV = new Vector3();
        private GfxUtil lineInstance = new GfxUtil();
        /*
        we have no way to invoke a callback to the picked component.
        Pickable component required to implment some kind of interface to provide a
        callback method e.g.
          pickedComp = picked.getComponent(PickRayComponent.class).pickInterface.picked( blah foo bar)
          if (null != pickedComp.pickedInterface)
             pickInterface.picked( myEntityReference );
         */
        @Override
        public void callback(Entity picked, GameEvent.EventType eventType) {

            final btRigidBody btRigidBodyPlayer = pickedPlayer.getComponent(BulletComponent.class).body;

            switch (eventType) {
                case RAY_DETECT:
                    if (null != picked) {
                        // we have an object in sight so kil it, bump the score, whatever
                        RenderSystem.debugGraphics.add(
                                lineInstance.lineTo(
                                        btRigidBodyPlayer.getWorldTransform().getTranslation(posV),
                                        picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                                        Color.LIME));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray lookRay = new Ray();

    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update(); // this can probaly be pause as well

        engine.update(delta);

            CharacterComponent comp = pickedPlayer.getComponent(CharacterComponent.class);
            ModelComponent mc = pickedPlayer.getComponent(ModelComponent.class);

            if (null != comp) {
//                comp.steerable.update(delta); // hmmmmm ....we have no hook to do regular player update stuff? There used to be a player system ...

                mc.modelInst.transform.getTranslation(position);
                mc.modelInst.transform.getRotation(rotation);
                lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));
                pickRayEventSignal.dispatch(nearestObjectToPlayerEvent.set(RAY_DETECT, lookRay, 0));
            }

            // crude  hack for platform disappear effect
            if (platformColor.a > 0.1f) {
                platformColor.a -= 0.005f;
                ModelInstanceEx.setColorAttribute(platformEntity.getComponent(ModelComponent.class).modelInst, platformColor);
            } else if (null != platformEntity) {
                engine.removeEntity(platformEntity);
                //platformEntity.remove(BulletComponent.class); // idfk
                platformColor.a = 0;
            }


        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
/*
        String s;
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
*/
        if (null != renderSystem) {
            float visibleCount = renderSystem.visibleCount;
            float renderableCount = renderSystem.renderableCount;
            //s = String.format("fps=%d vis.cnt=%d rndrbl.cnt=%d", Gdx.graphics.getFramesPerSecond(), renderSystem.visibleCount, renderSystem.renderableCount);
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            stringBuilder.append(" Visible: ").append(visibleCount);
            stringBuilder.append(" / ").append(renderableCount);
            label.setText(stringBuilder);
        }
        batch.end();


        //        shapeRenderer.setProjectionMatrix ????
        // semi-opaque filled box over touch area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hudOverlayColor);
        shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H / 4.0f);
        shapeRenderer.end();


        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();

        /* "Falling off platform" ... let a "sensor" or some suitable means to detect "fallen off platform" at which point, set gameOver.
          This way user can't pause during falling sequence. Once fallen past certain point, then allow screen switch.
         */
        if (roundOver) {
            roundOver = false;
            GameWorld.getInstance().showScreen(new MainMenuScreen());
        }
    }

    @Override
    public void show() {        // empty
    }

    @Override
    public void resize(int width, int height) {
    /*
    https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
    We need to update the stage's viewport in the resize method. The last Boolean argument set the origin to the lower left coordinate, causing the label to be drawn at that location.
     */
// resizes the stage UI view port but the 3D camera is not!
        //        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();

        //maybe we should do something more elegant here ...
// fixed the case where first time in setupUI, it blew chow here when I try to dispose gameUI .. duh yeh gameUI would still be null
        playerUI.dispose();
    }

    /*
     * android "back" button sends ApplicationListener.pause(), but then sends ApplicationListener.dispose() !!
     */

    @Override
    public void pause() {
        // Android "Recent apps" (square on-screen button), Android "Home" (middle o.s. btn ... Game.pause()->Screen.pause()
    }

    @Override
    public void resume() {
        // Android resume from "minimized" (Recent Apps button selected)
    }

    @Override
    public void hide() {
        dispose();
    }
}
