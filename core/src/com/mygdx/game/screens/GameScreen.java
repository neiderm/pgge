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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.mygdx.game.Components.BulletComponent;
import com.mygdx.game.Components.PlayerComponent;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.PlayerSystem;
import com.mygdx.game.systems.RenderSystem;


/**
 * Created by mango on 12/18/17.
 */

public class GameScreen implements Screen {

    private MyGdxGame game;

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private PlayerSystem playerSystem; //for reference to player entity

    private PerspectiveCamera cam;

    private CameraInputController camController;
    //    public FirstPersonCameraController camController;
    private Environment environment;

    //    Sprite box;
    private BitmapFont font;
    private OrthographicCamera guiCam;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private PlayerComponent playerComp;
    private BulletComponent bulletComp;
//private ModelComponent modelComp;
    //    private btRigidBody playerBody;

    private static final int touchBoxW = Gdx.graphics.getWidth() / 4;
    private static final int touchBoxH = touchBoxW ; // Gdx.graphics.getHeight() / 4;
    private static final int gameBoxW = Gdx.graphics.getWidth();
//    private static final int gameBoxH = Gdx.graphics.getHeight() - touchBoxH;
private static final int gameBoxH = Gdx.graphics.getHeight();

private final Color hudOverlayColor = new Color(1, 0, 0, 0.3f);


    /*
     * my multiplexed input adaptor
     * TODO:
     *   inputSystem.update(virtualPadX, virtualPadY, virtualButtonStates);
     *
     *   inputSystem:update() would handle (what would presumably) be one certain entity that should
     *   respond to inputs (note: input response not necessarily limited to the player, as maybe we
     *   would want to also drive inputs to e.g. guided missile ;)
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

        private Vector2 ctr = new Vector2();

        private void setVector(int screenX, int screenY) {
            float normalize = (touchBoxH / 2);
            touchBoxRect.getCenter(ctr);
            playerComp.vvv.x = (screenX - ctr.x) / normalize;
            playerComp.vvv.y = 0;
            playerComp.vvv.z = (screenY - ctr.y) / normalize;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

            if (touchBoxRect.contains(screenX, screenY)) {

                Gdx.app.log(this.getClass().getName(),
                        String.format("touchDown%d x = %d y = %d", touchDownCt++, screenX, screenY));

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

            Gdx.app.log(this.getClass().getName(),
                    String.format("touch up %d x = %d y = %d", touchUpCt++, screenX, screenY));

            if (isTouchInPad) {
                isTouchInPad = false;
                playerComp.vvv = Vector3.Zero.cpy();
                return true;
            }
            return false;
        }
    }


    public GameScreen(MyGdxGame game) {

        this.game = game;

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


        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(new MyInputAdapter());
        multiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(multiplexer);


        // make sure add system first before other entity creation crap, so that the system can get entityAdded!
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

        physObj.createEntities(engine);

//        Entity e = EntityFactory.createPlayer(new Vector3(0, 1.5f, 0), 5.0f);
//        engine.addEntity(e);
//        playerComp = e.getComponent(PlayerComponent.class);
        //    playerBody = e.getComponent(BulletComponent.class).body;

        playerComp = playerSystem.playerEntity.getComponent(PlayerComponent.class);
        bulletComp = playerSystem.playerEntity.getComponent(BulletComponent.class);
//modelComp = playerSystem.playerEntity.getComponent(ModelComponent.class);
    }

    private void addSystems() {

        Bullet.init(); // must be done before any bullet object can be created

        engine = new Engine();

        engine.addSystem(renderSystem = new RenderSystem(engine, environment, cam));
        engine.addSystem(bulletSystem = new BulletSystem(engine, cam));

        //    engine.addSystem(new EnemySystem());
          engine.addSystem(playerSystem = new PlayerSystem(this.game));
    }


    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {

        camController.update();

        // game box viewport
//        Gdx.gl.glViewport(0, touchBoxH, gameBoxW, gameBoxH);
        Gdx.gl.glViewport(0, 0, gameBoxW, gameBoxH);
        //         Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        engine.update(delta);

        // GUI viewport (full screen)
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();

        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 0, Gdx.graphics.getHeight());

        //if (null != playerBody)
        {
            btRigidBody playerBody = bulletComp.body;
            String s;
            s = String.format("%+2.1f %+2.1f %+2.1f",
//                    playerBody.getLinearVelocity().x, playerBody.getLinearVelocity().y, playerBody.getLinearVelocity().z);
                    playerComp.vVelocity.x, playerComp.vVelocity.y, playerComp.vVelocity.z);
            font.draw(batch, s, 100, Gdx.graphics.getHeight());

            s = String.format("%+2.1f %+2.1f %+2.1f",
                    playerComp.vvv.x, playerComp.vvv.y, playerComp.vvv.z);
            font.draw(batch, s, 250, Gdx.graphics.getHeight());

            Matrix4 mmm = bulletComp.motionstate.transform;
            Quaternion r = new Quaternion();
            mmm.getRotation(r);
            r = bulletComp.body.getOrientation(); /// same as getRotation?
            s = String.format("%+2.1f %+2.1f %+2.1f", r.getPitch(), r.getYaw(), r.getRoll());
            font.draw(batch, s, 400, Gdx.graphics.getHeight());
        }
//        box.draw(batch);

        batch.end();

//        shapeRenderer.setProjectionMatrix ????
        // semi-opaque filled box over touch area
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(hudOverlayColor);
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), touchBoxH);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(
                Gdx.graphics.getWidth() / 2.0f - touchBoxW / 2.0f, 0, touchBoxW, touchBoxH);
        shapeRenderer.circle(Gdx.graphics.getWidth() / 2.0f, touchBoxH / 2.0f, 1.0f);
        shapeRenderer.circle(Gdx.graphics.getWidth() / 2.0f, touchBoxH / 2.0f, touchBoxH / 2.0f);
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void dispose() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff

        physObj.dispose(); // static dispose models

        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        font.dispose();
        batch.dispose();
        shapeRenderer.dispose();
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
