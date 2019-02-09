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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.characters.Chaser;
import com.mygdx.game.characters.InputStruct;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SimpleVehicleModel;
import com.mygdx.game.controllers.SteeringBulletEntity;
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

import java.util.Locale;

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
    private Signal<GameEvent> gameEventSignal = new Signal<GameEvent>();
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
        engine.addSystem(new PickRaySystem(gameEventSignal));
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

        sc.statusUpdater = new BulletEntityStatusUpdate() {

            Vector3 origin = new Vector3(0, 0, 0); // the reference point for determining an object has exitted the level
            Vector3 bounds = new Vector3(50, 50, 50);
            // the reference point for determining an object has exitted the level
            float boundsDst2 = bounds.dst2(origin);
            Vector3 v = new Vector3();
            @Override
            public void update(Entity e) {
                v = e.getComponent(ModelComponent.class).modelInst.transform.getTranslation(v);
                if (v.dst2(origin) > boundsDst2) {
                    roundOver = true; // respawn() ... can't do it in this context??
                }
            }};

        setupVehicle(pickedPlayer);

        multiplexer.addProcessor(playerUI);
        Gdx.input.setInputProcessor(multiplexer);
    }

/*
 game event object for signalling to pickray system
 */
    private final GameEvent gameEvent = new GameEvent() {
        @Override
        public void callback(Entity picked, EventType eventType) {
            if (RAY_PICK == eventType && null != picked) {
                ModelInstanceEx.setMaterialColor(
                        picked.getComponent(ModelComponent.class).modelInst, Color.RED);
            }
        }};

    private void cameraSwitch(){
        if (cameraMan.nextOpMode())
            multiplexer.addProcessor(camController);
        else
            multiplexer.removeProcessor(camController);
    }

    private void setupVehicle(final Entity pickedPlayer){

// setup the vehicle model so it can be referenced in the mapper
        final SimpleVehicleModel vehicleModel = new TankController(
                pickedPlayer.getComponent(BulletComponent.class).body,
                pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);
/*
 .... override stage.act() ... ?
  */
        InputStruct mapper = new InputStruct() {

            @Override
            public void update(float deltaT) {

                InputState nowInputState = getInputState();

                // have to read the button to be sure it's state is delatched and not activate in a pause!
                if ( ! GameWorld.getInstance().getIsPaused()) {
/* AIs and Player act thru same interface to rig model (updateControols()) but the AIs are run by
the ECS via the CharacgterSystem, whereas the Player update directly here with controller inputs.
So we have to pause it explicitly as it is not governed by ECS
 */
                    vehicleModel.updateControls(getAxisY(0), getAxisX(0),
                            (InputState.INP_B2 == nowInputState),0); // need to use Vector2

                    if (InputState.INP_ESC == nowInputState) {

                        GameWorld.getInstance().setIsPaused(true);
                        //                    gameEventSignal.dispatch(gameEvent.set(IS_PAUSED, null, 0));
                    }
                    if (InputState.INP_SELECT == nowInputState) {

                        gameEventSignal.dispatch(
                                gameEvent.set(RAY_PICK, cam.getPickRay(getPointerX(), getPointerY()), 0));
                    }
                } else {
                    if (InputState.INP_CAMCTRL == nowInputState) {

                        GameWorld.getInstance().setIsPaused(false); // any of the on-screen menu button should un-pause if clicked
                        cameraSwitch();
                    }

                    if (InputState.INP_ESC == nowInputState) {

                        roundOver = true;
                    }
                    if (InputState.INP_SELECT == nowInputState) {

                        GameWorld.getInstance().setIsPaused(false);
                    }
                }}
        };
        playerUI = new PlayerCharacter(mapper);
    }

    /*
     * this is kind of a hack to test some ray casting
     */
    private GameEvent nearestObjectToPlayerEvent = new GameEvent() {

        Vector3 tmpV = new Vector3();
        Vector3 posV = new Vector3();
        GfxUtil lineInstance = new GfxUtil();
        /*
        we have no way to invoke a callback to the picked component.
        Pickable component required to implment some kind of interface to provide a
        callback method e.g.
          pickedComp = picked.getComponent(PickRayComponent.class).pickInterface.picked( blah foo bar)
          if (null != pickedComp.pickedInterface)
             pickInterface.picked( myEntityReference );

@picked: simpler type (not Entity) eg. Vector3 .... ?

         */
        @Override
        public void callback(Entity picked, GameEvent.EventType eventType) {

            if (RAY_DETECT == eventType && null != picked) {
                        RenderSystem.debugGraphics.add(lineInstance.lineTo(
                                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform.getTranslation(posV),
                                picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                                        Color.LIME));
            }}
    };

    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward
    private Ray lookRay = new Ray();
//private String s = new String(); // doesn't help ... String.format calls new Formatter()!
    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        String s;
        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update(); // this can probaly be pause as well
        engine.update(delta);

        Matrix4 transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        transform.getTranslation(position);
        transform.getRotation(rotation);
        lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));
        gameEventSignal.dispatch(nearestObjectToPlayerEvent.set(RAY_DETECT, lookRay, 0)); // maybe pass transform and invoke lookRay there


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
        if (false) {
            // calls new Formatter() which we dont want!
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());
            s = String.format(Locale.ENGLISH, "%+2.1f %+2.1f %+2.1f", 0f, 0f, 0f);
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }
        batch.end();

            float visibleCount = renderSystem.visibleCount;
            float renderableCount = renderSystem.renderableCount;
            //s = String.format("fps=%d vis.cnt=%d rndrbl.cnt=%d", Gdx.graphics.getFramesPerSecond(), renderSystem.visibleCount, renderSystem.renderableCount);
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            stringBuilder.append(" Visible: ").append(visibleCount);
            stringBuilder.append(" / ").append(renderableCount);
/*
            fpsLabel.setText(stringBuilder);
*/
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

if (GameWorld.getInstance().getIsPaused()) {
    // example of using ShapeRender to draw directly to screen
    //        shapeRenderer.setProjectionMatrix ????
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
    shapeRenderer.setColor(hudOverlayColor);
    shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H);
    shapeRenderer.end();
}

        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();

        /* "Falling off platform" ... let a "sensor" or some suitable means to detect "fallen off platform" at which point, set gameOver.
          This way user can't pause during falling sequence. Once fallen past certain point, then allow screen switch.
         */
        if (roundOver) {
            roundOver = false;
            Gdx.app.log("GameScreen:render", "new MainMenuScreen()");
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

        batch.dispose();
        shapeRenderer.dispose();
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
