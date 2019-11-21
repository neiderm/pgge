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
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.CompCommon;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.SimpleVehicleModel;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.controllers.TrackerSB;
import com.mygdx.game.features.Projectile;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.sceneLoader.SceneLoader;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.FeatureSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.systems.RenderSystem;
import com.mygdx.game.systems.StatusSystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import static com.mygdx.game.util.GameEvent.EventType.EVT_HIT_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.EVT_SEE_OBJECT;

/**
 * Created by neiderm on 12/18/17.
 */
public class GameScreen extends TimedGameScreen {

    private Engine engine;
    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private RenderSystem renderSystem; //for invoking removeSystem (dispose)
    private CameraMan cameraMan;
    private CameraInputController camController; // FirstPersonCameraController camController;
//    private BitmapFont font;
//    private OrthographicCamera guiCam;
//    private SpriteBatch batch = new SpriteBatch();
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private GameUI playerUI;
    private InputMultiplexer multiplexer;
    private Signal<GameEvent> gameEventSignal = new Signal<GameEvent>();
    private final Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);
    private Entity pickedPlayer;


    private void init(){

        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("data/font.fnt"),
                Gdx.files.internal("data/font.png"), false);
//        font.setColor(1, 1, 1, 0.5f);

        float fontGetDensity = Gdx.graphics.getDensity();

        if (fontGetDensity > 1)
            font.getData().setScale(fontGetDensity);

        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_ACTIVE);

        // been using same light setup as ever
        //  https://xoppa.github.io/blog/loading-a-scene-with-libgdx/
        // shadow lighting lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        Environment environment = new Environment();
        environment.set(
                new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, lightDirection));

        PerspectiveCamera cam = new PerspectiveCamera(67, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
//        cam.position.set(3f, 7f, 10f);
//        cam.lookAt(0, 4, 0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        camController = new CameraInputController(cam);
//        camController = new FirstPersonCameraController(cam);

        // must be done before any bullet object can be created .. I don't remember why the BulletWorld is only instanced once
        BulletWorld.getInstance().initialize(cam);              //  screen could inherit from e.g. "ScreenWithBulletWorld" ???

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();

        DirectionalShadowLight shadowLight = new DirectionalShadowLight(1024, 1024, 120, 120, 1f, 300);
        shadowLight.set(0.8f, 0.8f, 0.8f, new Vector3(0.5f, -1f, 0f));
        environment.add(shadowLight);
        environment.shadowMap = shadowLight;

        engine = new Engine();
        renderSystem = new RenderSystem(shadowLight, environment, cam);
        bulletSystem = new BulletSystem();
        engine.addSystem(renderSystem);
        engine.addSystem(bulletSystem);
        engine.addSystem(new PickRaySystem(gameEventSignal));
        engine.addSystem(new StatusSystem());
        engine.addSystem(new CharacterSystem());
        engine.addSystem(new FeatureSystem());

        SceneLoader.buildScene(engine);
        GfxUtil.init();

        GameFeature pf = GameWorld.getInstance().getFeature("Player"); // make tag a defined string
        pickedPlayer = pf.getEntity();
        pickedPlayer.remove(PickRayComponent.class); // tmp ... stop picking yourself ...

        Matrix4 playerTrnsfm = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
 (a "chaser-camera" entity - no visual but has transform and possibly AI and/or physics chaacterisitics - can be active at most time
 and camera may or may not actually be on it)
          */
        chaserSteerable.setSteeringBehavior(
                new TrackerSB<Vector3>(chaserSteerable, playerTrnsfm, chaserTransform, new Vector3(0, 2, 0)));

        cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt, playerTrnsfm);

        Entity cameraEntity = new Entity();
        cameraEntity.add(new CharacterComponent(cameraMan));
        /*
        cameraEntity.add(new CharacterComponent());
        CharacterComponent cc = cameraEntity.getComponent(CharacterComponent.class);
        cc.setSteerable(cameraMan);
*/
        engine.addEntity(cameraEntity);

        playerUI = initPlayerUI();
        pickedPlayer.add(new StatusComponent(40));
        multiplexer = new InputMultiplexer(playerUI); // make sure get a new one since there will be a new Stage instance ;)
        Gdx.input.setInputProcessor(multiplexer);
    }


    private GameUI initPlayerUI(){
        /*
         * here is where all the controller and model stuff comes together
         */
        return new GameUI() {

            // setup the vehicle model so it can be referenced in the mapper
            final SimpleVehicleModel controlledModel = new TankController( // todo: model can instantiate body and pickedplayer can set it?
                    pickedPlayer.getComponent(BulletComponent.class).body,
                    pickedPlayer.getComponent(BulletComponent.class).mass /* should be a property of the tank? */);

            // working variables
            Matrix4 tmpM = new Matrix4();
            Vector3 trans = new Vector3();
            Quaternion orientation = new Quaternion();

            // allowing this to be here so it can be basis of setting forwared vector for projectile/weaopon
            void gunSight( Entity target, Entity player ){

                ModelComponent mc = player.getComponent(ModelComponent.class);

                if (null != mc && null != mc.modelInst){
                    tmpM = mc.modelInst.transform;
                }
                tmpM.getRotation(orientation);

                // offset the trans  because the model origin is free to be adjusted in Blender e.g. at "surface level"
                // depending where on the model origin is set (done intentionally for adjustmestment of decent steering/handling physics)
                tmpV.set(0, +0.7f, 0); // using +y for up vector ...
                ModelInstanceEx.rotateRad(tmpV, orientation); // ... and rotsting the vector to orientation of transform matrix
                tmpM.getTranslation(trans).add(tmpV); // start coord of projectile now offset "higher" wrt to vehicle body

                // set unit vector for direction of travel for theoretical projectile fired perfectly in forwared direction
  //              float mag = -0.1f; // scale the '-1' accordingly for magnitifdue of forward "velocity"
//                Vector3 vvv = ModelInstanceEx.rotateRad(tmpV.set(0, 0, mag), orientation); // don't need to get Rotaion again ;)
                /*
                 * pass "picked" thing to projectile to use as sensor target (so it's actually only sensing for the one target!
                 */
                CompCommon.spawnNewGameObject( new Vector3(0.1f, 0.1f, 0.1f),
                        trans,
                        new Projectile( target, tmpM ),
                        "cone");
            }

            @Override
            public void onSelectEvent() {

                super.onSelectEvent(); //

                gunSight(hitDetectEvent.getEntity(), pickedPlayer);
            }

            @Override
            public void onCameraSwitch(){

                if (cameraMan.nextOpMode())
                    multiplexer.addProcessor(camController);
                else
                    multiplexer.removeProcessor(camController);
            }

            @Override
            public void act (float delta) {

// get the controller to model update out of the way before start messing with any screen transitions/disposals

                // how expensive are these get Comps ???   could be cached
                BulletComponent bc = pickedPlayer.getComponent(BulletComponent.class);

                if (null != bc){
//                    if ( ! bc.iHaveBeenDisposed )
                    {
                        controlledModel.updateControls(mapper.getAxisY(0), mapper.getAxisX(0),
                                (mapper.isInputState(InputMapper.InputState.INP_B2)), 0); // need to use Vector2
                    }
                }

                super.act(delta);

                // handle a (hopefully) small subset of control actions specific to the model or whatever
                switch (GameWorld.getInstance().getRoundActiveState()) {

                    case ROUND_ACTIVE:
                    case ROUND_COMPLETE_WAIT:
                        StatusComponent sc = pickedPlayer.getComponent(StatusComponent.class);
                        int lc = sc.lifeClock;

                        if (0 == lc){
//                            CompCommon.explode(pickedPlayer);   //   don't really want it here (why not?)
                            ModelComponent mc = pickedPlayer.getComponent(ModelComponent.class);
                            CompCommon.exploducopia(mc.modelInst, mc.modelInfoIndx);

                            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_MORTE);

                            continueScreenTimeUp = getScreenTimer() - GameUI.SCREEN_CONTINUE_TIME;
                        }

                        // hackity crap setting flag here because lol GameUI.java has not yet got access to local player entity
                        canExit = sc.canExit;
                        prizeCount = sc.prizeCount;
                        setScore(sc.bounty); // spare me your judgement ... at least do it with a setter ..
                        break;

                    case ROUND_OVER_RESTART:
                        screenTeardown();
                        init();
                        break;

                    default:
                        break;
                }

                updateRays();
            }

            Ray lookRay = new Ray();
            Vector3 tmpV3 = new Vector3();
            Quaternion rotation = new Quaternion();
            Vector3 direction = new Vector3(0, 0, -1); // vehicle forward

            void updateRays(){

                Matrix4 transform = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;

                lookRay.set(transform.getTranslation(tmpV3),
                        ModelInstanceEx.rotateRad(direction.set(0, 0, -1), transform.getRotation(rotation)));

                //                gameEventSignal.dispatch( gameEvent.set(RAY_PICK, cam.getPickRay(mapper.getPointerX(), mapper.getPointerY()), 0)); // touch screen mapper
                gameEventSignal.dispatch(hitDetectEvent.set(EVT_HIT_DETECT, lookRay, 0)); // maybe pass transform and invoke lookRay there
                gameEventSignal.dispatch(seeObjectEvent.set(EVT_SEE_OBJECT, lookRay, 0)); // maybe pass transform and invoke lookRay there

            }
        };
    }
//    private void sillyFontFx(Color cc){
//        font.setColor(cc);
//        float scaleX = font.getScaleX();
//        float scaleY = font.getScaleY();
//        font.getData().setScale(scaleY  * 1.5f);
//    }

    /*
    game event object for signalling to pickray system
    */
    private final GameEvent hitDetectEvent = new GameEvent() {

        @Override
        public void handle(Entity picked, EventType eventType) {

            super.setEntity(picked);

            if (EVT_HIT_DETECT == eventType) {
// if (null != picked){
// }
            }
        }
    };

    /*
     * this is kind of a hack to test some ray casting
     */
    private GameEvent seeObjectEvent = new GameEvent() {

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
        public void handle(Entity picked, GameEvent.EventType eventType) {

            if (EVT_SEE_OBJECT == eventType && null != picked) {

                //  picked.onSelect();  // I know you're lookin at me ...
///*
                RenderSystem.debugGraphics.add(lineInstance.lineTo(
                        pickedPlayer.getComponent(ModelComponent.class).modelInst.transform.getTranslation(posV),
                        picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                        Color.LIME));
//*/
            }
        }
    };

    private Vector3 tmpV = new Vector3();
    private Vector3 tmpPos = new Vector3();
    private GfxUtil camDbgLineInstance = new GfxUtil();
    private Matrix4 chaserTransform = new Matrix4();
    private SteeringEntity chaserSteerable = new SteeringEntity();
    private Color color = new Color(Color.CYAN);

    //private String s = new String(); // doesn't help ... String.format calls new Formatter()!
    /*
     * https://xoppa.github.io/blog/3d-frustum-culling-with-libgdx/
     * "Note that using a StringBuilder is highly recommended against string concatenation in your
     * render method. The StringBuilder will create less garbage, causing almost no hick-ups due to garbage collection."
     */
    @Override
    public void render(float delta) {
        // game box viewport
        Gdx.gl.glViewport(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // put in any debug graphics to the render pipeline
        chaserSteerable.update(delta);

        RenderSystem.debugGraphics.add(camDbgLineInstance.lineTo(
                pickedPlayer.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpPos),
                chaserTransform.getTranslation(tmpV), Color.PURPLE));

        camController.update(); // this can probaly be pause as well

        engine.update(delta);

        BulletWorld.getInstance().update(delta);

/*
        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        String s = String.format(Locale.ENGLISH, "%s", "*");
        font.draw(batch, s, 10, font.getLineHeight());
        batch.end();
*/
        debugPrint("**" + pickedPlayer.getComponent(StatusComponent.class).lifeClock, color, 0, 0);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        // example of using ShapeRender to draw directly to screen
        //        shapeRenderer.setProjectionMatrix ????
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
/*
            shapeRenderer.setColor(color);
            shapeRenderer.rect(0, 0, GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
*/
        shapeRenderer.end();

        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();

        // update entities queued for deletion (seems like this needs to be done outside of engine/simulation step)
        cleaner();

        // update entities queued for spawning
        spawner();
    }

    private void spawner(){

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get(ModelGroup.SPAWNERS_MGRP_KEY);

        if (null != mg /* && mg.size > 0 */ ) {
            mg.build(engine, true); // delete objects flag not really needed if rmv the group each frame update
            sd.modelGroups.remove(ModelGroup.SPAWNERS_MGRP_KEY); // delete the group;

//            System.out.println("Built model group (model name = " + mg.modelName   );
        }
    }

    private void cleaner() {
        // update entities queued for deletion (needs to be done outside of engine/simulation step)
        for (Entity e : engine.getEntitiesFor(Family.all(StatusComponent.class).get())) {
//            if (null != e)
            StatusComponent sc = e.getComponent(StatusComponent.class);

            //  check for entities to be removed first ... there would bw no point in separate comps deleteion
            if (sc.deleteMe) {

                Gdx.app.log("GameScreen", "cleanr: remove ENTITY.");
                removeBulletComp(e);
                engine.removeEntity(e); // ... calls BulletSystem:entityRemoved() .. but the bc is no useable :(

                StatusComponent psc = pickedPlayer.getComponent(StatusComponent.class);
                psc.bounty += sc.bounty; //  "points value of picked or destroyed thing

            } else {
                if (2 == sc.deleteFlag) { // will use flags for comps to remove

                    removeBulletComp(e);
                }
            }

            sc.deleteFlag = 0;
        }
    }

    private void removeBulletComp(Entity ee){

        BulletComponent bc = ee.getComponent(BulletComponent.class);

        if (null != bc) {

            ee.remove(BulletComponent.class); // triggers BulletSystem:entityRemoved()
            Gdx.app.log("GameScreen", "cleanr: .... BulletComponent being disposed!!!!!");

            if (null != bc.motionstate) {
                bc.motionstate.dispose();
            }
            BulletWorld.getInstance().removeBody(bc.body);
            bc.shape.dispose();
            bc.body.dispose();
            bc.body = null; // idfk ... is this useful?
        } // if
    }

    private  SpriteBatch batch = new SpriteBatch();
    private  BitmapFont font;
    private  OrthographicCamera guiCam;

    /*
     * debug only (betch is ended each call)
     */
    private void debugPrint(String string, Color color, int row, int col){

        int y = (int) ((float) row * font.getLineHeight() + font.getLineHeight());
        int x = (int) ((float) col * font.getLineHeight());
        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        font.setColor(color);
        font.draw(batch, string, x, y);
        batch.end();
    }


    @Override
    public void show() {

        init();
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

    /*
     * because some stuff but not all stuff done every screen (re)start
     */
    private void screenTeardown(){

        Gdx.app.log("GameScreen", "screenTearDown");

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeSystem(renderSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        BulletWorld.getInstance().dispose();
        playerUI.dispose();

        font.dispose(); // only instantiated on show()  for some reaseon

        // I guess not everything is handled by ECS ;)
        PrimitivesBuilder.clearShapeRefs();

        // other Systems may have run after last Render System update, so be sure clear this queue
        // of model instances first before ....
        RenderSystem.debugGraphics.clear();
        GfxUtil.clearRefs();                 // ... invalidating the underlying models!
    }

    @Override
    public void dispose() {

        screenTeardown();

        batch.dispose();
        shapeRenderer.dispose();

        // screens that load assets must calls assetLoader.dispose() !
        super.dispose();
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
