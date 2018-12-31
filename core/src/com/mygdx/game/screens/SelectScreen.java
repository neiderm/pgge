package com.mygdx.game.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
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
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.characters.InputStruct;
import com.mygdx.game.characters.PlayerCharacter;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import static com.mygdx.game.util.GameEvent.EventType.RAY_PICK;

class SelectScreen implements Screen {

    private Engine engine;

    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)

    private PerspectiveCamera cam;

    private CameraInputController camController;
    private Environment environment;
    private DirectionalShadowLight shadowLight;
    private Vector3 lightDirection = new Vector3(0.5f, -1f, 0f);

    private BitmapFont font;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private static final int GAME_BOX_W = Gdx.graphics.getWidth();
    private static final int GAME_BOX_H = Gdx.graphics.getHeight();

    private final Color hudOverlayColor = new Color(1, 0, 0, 0.2f);
    private PlayerCharacter stage;

    private Signal<GameEvent> pickRayEventSignal;
    private boolean isPicked = false;

    private InputStruct mapper;

    private Entity platform;


    SelectScreen() {

        pickRayEventSignal = new Signal<GameEvent>();

        engine = new Engine();

        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
//        Vector3 lightDirection = new Vector3(1f, -0.8f, -0.2f); // new Vector3(-1f, -0.8f, -0.2f);
        environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));

        cam = new PerspectiveCamera(67, GAME_BOX_W, GAME_BOX_H);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);

        // Font files from ashley-superjumper
        font = new BitmapFont(
                Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
        font.getData().setScale(1.0f);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();


        if (null != shadowLight)  // if this is a new round but not new gamescreen
            environment.remove(shadowLight);
        shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, lightDirection);
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        addSystems();


// build the platform moanually (not from data file) for simplicity of retrieving entity
        platform = PrimitivesBuilder.getCylinderBuilder().create(0, new Vector3(0, 10, -5), new Vector3(4, 1, 4));
        engine.addEntity(platform);

        newRound();
    }


    /*
"gun sight" will be draggable on the screen surface, then click to pick and/or shoot that direction
*/
    private InputListener makeButtonGSListener(final GameEvent gameEvent) {

        final Ray pickRay = new Ray();

        return new InputListener() {

            private Ray setPickRay(float x, float y) {
                // offset button x,y to screen x,y (button origin on bottom left) (should not have screen/UI geometry crap in here!)
                float nX = (Gdx.graphics.getWidth() / 2f) + (x - 75);
                float nY = (Gdx.graphics.getHeight() / 2f) - (y - 75) - 75;
                Ray rayTmp = cam.getPickRay(nX, nY);
                return pickRay.set(rayTmp.origin, rayTmp.direction);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) { /*empty*/ }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // only do this if FPV mode (i.e. cam controller is not handling game window input)
//            if (!isController) // TODO: remove the GS from the gameUI if !isController (in GameScreen)
                {
                    pickRayEventSignal.dispatch(gameEvent.set(RAY_PICK, setPickRay(x, y), 0));
                    //Gdx.app.log(this.getClass().getName(), String.format("GS touchDown x = %f y = %f, id = %d", x, y, id));
                }
                return true;
            }
        };
    }


    private void newRound() {

        GameWorld.sceneLoader.buildTanks(engine);
        GameWorld.sceneLoader.buildArena(engine);

        final GameEvent playerPickedGameEvent = new GameEvent() {
            @Override
            public void callback(Entity picked, EventType eventType) {
                switch (eventType) {
                    case RAY_PICK:
                        if (null != picked) {
                            GameWorld.getInstance().setPlayerObjectName(picked.getComponent(PickRayComponent.class).objectName); // whatever
                            body = picked.getComponent(BulletComponent.class).body;
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        mapper = new InputStruct() {     // TODO: InputStruct is abstract, cannot be instantiated
            @Override
            public void update(float deltaT) { }
        };

        stage = new PlayerCharacter(mapper, null);


        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(150, 150, Pixmap.Format.RGBA8888);
        button.setColor(1, 1, 1, .3f);
        button.fillRectangle(0, 0, 150, 150);

        stage.addInputListener(
                makeButtonGSListener(playerPickedGameEvent),
                button,
                (Gdx.graphics.getWidth() / 2f) - 75, (Gdx.graphics.getHeight() / 2f) + 0);

        button.dispose();

        // ok so you can add a label to the stage
        Label label = new Label("Pick Your Rig ... ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);


        CameraMan cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);
    }

    // point the camera to platform
    private final Vector3 camDefPosition = new Vector3(0f, 13.5f, 02f);
    private final Vector3 camDefLookAt = new Vector3(0f, 10f, -5.0f);


    private void addSystems() {

        // must be done before any bullet object can be created
        BulletWorld.getInstance().initialize(cam);

        engine.addSystem(renderSystem = new RenderSystem(shadowLight, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(BulletWorld.getInstance()));
        engine.addSystem(new PickRaySystem(pickRayEventSignal));
    }


    @Override
    public void show() {
        // empty
    }

    // tmp for the pick marker
    private btRigidBody body;
    private GfxUtil gfxLine = new GfxUtil();
    private Vector3 trans = new Vector3();
    private Matrix4 tmpM = new Matrix4();
    private Quaternion rotation = new Quaternion();
    private Vector3 down = new Vector3();
    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {

        if (null != body) {
            body.getWorldTransform(tmpM);
            tmpM.getTranslation(trans);

            RenderSystem.debugGraphics.add(gfxLine.line(trans,
                    ModelInstanceEx.rotateRad(down.set(0, -1, 0), tmpM.getRotation(rotation)),
                    Color.RED));
        }

        // game box viewport
        Gdx.gl.glViewport(0, 0, GAME_BOX_W, GAME_BOX_H);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camController.update(); // this can probaly be pause as well

        engine.update(delta);

        // semi-opaque filled box over touch area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hudOverlayColor);
        shapeRenderer.rect(0, 0, GAME_BOX_W, GAME_BOX_H / 4.0f);
        shapeRenderer.end();

        // note: I protected for null camera system on the input hhandler ... do
        // we want to update the stage if not Done Loading?
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        /*
           TODO:? wouldn't have to poll if i override the PlayerCharacter::jumpButtonListener()
         */
            if (0 != mapper.jumpButtonGet()) { // mapper.buttonGet(InputStruct.ButtonsEnum.BUTTON_1);

                if (null != body) { // tmp, hack, using this as a stupid flag to indicate wether or not a tank has been picked

                    isPicked = true;
            }
        }

        if (isPicked) {
            isPicked = false;
            Gdx.app.log("SelectScreen", " isPicked ->  showScreen() ");
            GameWorld.getInstance().showScreen(new LoadingScreen("GameData.json"));
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

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
        stage.dispose();
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
