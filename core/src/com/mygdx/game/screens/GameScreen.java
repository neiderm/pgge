/*
 * Copyright (c) 2021-2022 Glenn Neidermeier
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.GameWorld;
import com.mygdx.game.characters.CameraMan;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.FeatureComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.components.PickRayComponent;
import com.mygdx.game.components.StatusComponent;
import com.mygdx.game.controllers.CharacterController;
import com.mygdx.game.controllers.GunPlatform;
import com.mygdx.game.controllers.SteeringEntity;
import com.mygdx.game.controllers.TankController;
import com.mygdx.game.controllers.TrackerSB;
import com.mygdx.game.features.Crapium;
import com.mygdx.game.features.FeatureAdaptor;
import com.mygdx.game.sceneLoader.GameFeature;
import com.mygdx.game.sceneLoader.GameObject;
import com.mygdx.game.sceneLoader.InstanceData;
import com.mygdx.game.sceneLoader.ModelGroup;
import com.mygdx.game.sceneLoader.SceneData;
import com.mygdx.game.sceneLoader.SceneLoader;
import com.mygdx.game.systems.BulletSystem;
import com.mygdx.game.systems.CharacterSystem;
import com.mygdx.game.systems.FeatureSystem;
import com.mygdx.game.systems.PickRaySystem;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.GfxUtil;
import com.mygdx.game.util.ModelInstanceEx;
import com.mygdx.game.util.PrimitivesBuilder;

import static com.mygdx.game.util.GameEvent.EventType.EVT_HIT_DETECT;
import static com.mygdx.game.util.GameEvent.EventType.EVT_SEE_OBJECT;

/**
 * Created by neiderm on 12/18/17.
 */
public class GameScreen extends BaseScreenWithAssetsEngine {

    private static final String CLASS_STRING = "GameScreen";
    private final Signal<GameEvent> gameEventSignal = new Signal<>();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    private Vector3 camDefPosition = new Vector3(1.0f, 13.5f, 02f); // hack: position of fixed camera at 'home" location
    private final Vector3 camDefLookAt = new Vector3(1.0f, 10.5f, -5.0f);

    private BulletSystem bulletSystem; //for invoking removeSystem (dispose)
    private CameraMan cameraMan;
    private CameraInputController camController;
    private BitmapFont debugPrintFont;
    private OrthographicCamera guiCam;
    private SpriteBatch batch = new SpriteBatch();
    private GameUI playerUI;
    private Entity pickedPlayer;
    private Gunrack gunrack;

    private void setup() {
        // must be done before any bullet object can be created .. I don't remember why the BulletWorld is only instanced once
        BulletWorld.getInstance().initialize();

        super.init(); // initialization in the base Screen class

        batch = new SpriteBatch();
        camController = new CameraInputController(cam);

        debugPrintFont = new BitmapFont(Gdx.files.internal(GameWorld.DEFAULT_FONT_FNT),
                Gdx.files.internal(GameWorld.DEFAULT_FONT_PNG), false);
        debugPrintFont.getData().setScale(GameWorld.FONT_X_SCALE, GameWorld.FONT_Y_SCALE);

        // "guiCam" etc. lifted from 'Learning_LibGDX_Game_Development_2nd_Edition' Ch. 14 example
        guiCam = new OrthographicCamera(GameWorld.VIRTUAL_WIDTH, GameWorld.VIRTUAL_HEIGHT);
        guiCam.position.set(guiCam.viewportWidth / 2f, guiCam.viewportHeight / 2f, 0);
        guiCam.update();

        bulletSystem = new BulletSystem();
        engine.addSystem(bulletSystem);
        engine.addSystem(new PickRaySystem(gameEventSignal));
        engine.addSystem(new CharacterSystem());
        engine.addSystem(new FeatureSystem());

        GfxUtil.init();
        GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_ACTIVE);
        pickedPlayer = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME).getEntity();

        if (null != pickedPlayer) {
            pickedPlayer.remove(PickRayComponent.class); // tmp ... stop picking yourself ...
            final int health = 999; // should not go to 0
            pickedPlayer.add(new StatusComponent(health));            // max damage
            Matrix4 playerTrnsfm = pickedPlayer.getComponent(ModelComponent.class).modelInst.transform;
        /*
         player character should be able to attach camera operator to arbitrary entity (e.g. guided missile control)
         The camera chaser is an entity with no visuals, but has transform and possibly AI for e.g.
         terrain following/collision-avoidance and possibly even collision body for physics interaction w/ world.
        */
            chaserTransform = new Matrix4(); // hacky crap ... steering behavior construction will not instantiate this

            chaserSteerable.setSteeringBehavior(new TrackerSB<>(
                    chaserSteerable, playerTrnsfm, chaserTransform, new Vector3(0, 2, 0)));

            cameraMan = new CameraMan(cam, camDefPosition, camDefLookAt, playerTrnsfm);

            Entity cameraEntity = new Entity();
            cameraEntity.add(new CharacterComponent(cameraMan));
            engine.addEntity(cameraEntity);

            if (null != pickedPlayer.getComponent(BulletComponent.class).body) {
                playerUI = initPlayerUI();
                // restart audio track
                GameWorld.AudioManager.playMusic(music);
            } else {
                Gdx.app.log(CLASS_STRING, "pickedPlayer collision body can't be null");
            }
        } else {
            // we're toast
            Gdx.app.log(CLASS_STRING, "pickedPlayer can't be null");
        }
    }

    /*
     * handle weapon pickups
     */
    private void onWeaponAcquired(int wtype) {
        // weaponsMenuSize =
        gunrack.onWeaponAcquired(wtype);
        playerUI.setMsgLabel(gunrack.getDescription(wtype), 2);
    }

    private GameUI initPlayerUI() {
        /*
         * here is where all the controller and model stuff comes together
         */
        return new GameUI() {
            // configures weapon to simulate energizing time on switch-over
            private CharacterController rigController;
            private InputMapper.ControlBundle cbundle; // cbundle to be inherited from parent class and call updateControlBundle()?
            // can't properly instantiate gun platform until screen initialization is complete
            private GunPlatform gunPlatform;

            //  control driving rig, hackage for auto-accelerator mode (only on screen where it is set as playerfeature userdata)
            GameFeature pf = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);

            private void makeGunPlatform(boolean withEnergizeDelay) {
                gunPlatform = new GunPlatform(
                        pickedPlayer.getComponent(ModelComponent.class).modelInst,
                        pickedPlayer.getComponent(BulletComponent.class).shape,
                        gunrack, withEnergizeDelay);

                gunPlatform.setControlBundle(cbundle);
            }

            @Override
            protected void init() {
                rigController = new TankController( // todo: model can instantiate body and pickedplayer can set it?
                        pickedPlayer.getComponent(BulletComponent.class).body,
                        pickedPlayer.getComponent(BulletComponent.class).mass); /* should be a property of the rig? */

                cbundle = rigController.getControlBundle();
                cbundle.setButtons(
                        new InputMapper.CtrlButton(InputMapper.VirtualButtonCode.BTN_A, true, 60),
                        new InputMapper.CtrlButton(InputMapper.VirtualButtonCode.BTN_B));

                gunrack = new Gunrack(hitDetectEvent, debugPrintFont) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        // check for new platform or exhausted weapon
                        if (getRoundsAvailable() <= 0) {
                            // selected weapon has no rounds available - initialize and reset to standard ammo
                            resetStandard();
                            // no energizing time required if switch to std. ammo due to 0 ammo (or starting new round)
                            makeGunPlatform(false);
                        }
                    }
                };
                addActor(gunrack);
            }

            @Override
            public void onSwitchView() {
                if (cameraMan.nextOpMode())
                    multiplexer.addProcessor(camController);
                else
                    multiplexer.removeProcessor(camController);
            }

            @Override
            protected void onInputX() {
                super.onInputX();
                // if the gunrack menu is active then the selected weapon will be enabled
                if (gunrack.onInputX()) {
                    // a new weapon has been selected, null the old one (will be regenerated on main thread)
                    gunPlatform.destroy();
                    gunPlatform = null; // triggers platform re-init on main thread
                }
            }

            @Override
            void onPaused() {
                super.onPaused();
                GameWorld.AudioManager.pauseMusic(music);
            }

            @Override
            void onUnPaused() {
                super.onUnPaused();
                GameWorld.AudioManager.playMusic(music);
            }

            @Override
            protected void onL1MenuOpen() {
                gunrack.onMenuEvent();
            }

            private void updateModelSpace(/* InputMapper.ControlBundle cbundle */) {
                // the only external reference to mapper ... could be private in parent and updateControlBundle() from parent act()
                mapper.updateControlBundle(cbundle); // sample axes and switches inputs

                if (null == gunPlatform) {
                    makeGunPlatform(true);
                }
                gunPlatform.updateControls(0 /* unused */);

                // user data is hacked in flag applied on levels that set the player Rig to have auto-forward movement
                float yInput = cbundle.getAxis(InputMapper.VIRTUAL_WS_AXIS);
                if (Math.abs(yInput) < 0.4f) {
                    // forces forward motion but doesn't affect reverse, idfk provide "bucket" of reversing/brake power?
                    cbundle.setAxis(InputMapper.VIRTUAL_WS_AXIS, (-1) * pf.userData / 100.0f); // percent
                }
                rigController.updateControls(0 /* unused */);
            }

            final int screenContinueTime = 10 * 60; // FPS

            @Override
            public void act(float delta) {

                super.act(delta);

                // handle a (hopefully) small subset of control actions specific to the model or whatever
                switch (GameWorld.getInstance().getRoundActiveState()) {
                    case ROUND_ACTIVE:
                    case ROUND_COMPLETE_WAIT:
                        StatusComponent sc = pickedPlayer.getComponent(StatusComponent.class);
                        int lc = 0;
                        if (null != sc) {
                            lc = sc.lifeClock;

                            // hackity crap setting flag here because lol GameUI.java has not yet got access to local player entity
                            canExit = sc.canExit;
                            prizeCount = sc.prizeCount;
                            setScore(sc.bounty); // spare me your judgement ... at least do it with a setter ..

                            boolean isDead = updateDamage(sc.damage);
                            if (isDead) {
                                lc = 0;
                            }
                            sc.lifeClock = lc;
                        }
                        if (0 == lc) {
                            continueScreenTimeUp = getScreenTimer() - screenContinueTime;
                            GameWorld.getInstance().setRoundActiveState(GameWorld.GAME_STATE_T.ROUND_OVER_KILLED);

                        } else if (!GameWorld.getInstance().getIsPaused()) {
                            // update model if game state is valid
                            updateModelSpace();
                        }
                        break;
                    case ROUND_OVER_QUIT:
                        gunPlatform.destroy();
                        gunrack.setVisible(false);

                        if (null != music) {
                            music.stop();
                        }
                        break;
                    default:
                        break;
                }
                updateRays();
            }

            final Ray lookRay = new Ray();
            final Vector3 tmpV3 = new Vector3();
            final Quaternion rotation = new Quaternion();
            final Vector3 direction = new Vector3(0, 0, -1); // vehicle forward

            void updateRays() {

                ModelComponent mc = pickedPlayer.getComponent(ModelComponent.class);
                if (null != mc) {
                    Matrix4 transform = mc.modelInst.transform;

                    lookRay.set(transform.getTranslation(tmpV3),
                            ModelInstanceEx.rotateRad(direction.set(0, 0, -1), transform.getRotation(rotation)));

                    gameEventSignal.dispatch(hitDetectEvent.set(EVT_HIT_DETECT, lookRay, 0)); // maybe pass transform and invoke lookRay there
                    gameEventSignal.dispatch(seeObjectEvent.set(EVT_SEE_OBJECT, lookRay, 0)); // maybe pass transform and invoke lookRay there
                }
            }

            Color shldColorBG = new Color();
            Color shldColorFG = new Color();

            boolean updateDamage(int[] damageArray) {

                int x = GameWorld.VIRTUAL_WIDTH * 7 / 8;
                int y = GameWorld.VIRTUAL_HEIGHT * 6 / 8;
                int w = GameWorld.VIRTUAL_HEIGHT / 8;
                int h = GameWorld.VIRTUAL_HEIGHT / 8;
                int cX = x + w / 2;
                int cY = y + h / 2;
                float radius = w / 2.0f;
                boolean isdead = false;

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                for (int n = 0; n < 4; n++) {
                    int damage = damageArray[n];
                    if (damage < 20) {
                        shldColorFG.set(Color.GREEN);
                    } else if (damage < 40) {
                        shldColorFG.set(Color.YELLOW);
                    } else if (damage < 60) {
                        shldColorFG.set(Color.ORANGE);
                    } else if (damage < 80) {
                        shldColorFG.set(Color.RED);
                    } else if (damage < 100) {
                        shldColorFG.set(Color.DARK_GRAY); // shield is gone
                    } else {
                        isdead = true; // sc.lifeClock = 0; // shield is gone, rig destroyed
                    }
                    shldColorFG.a = 0.5f;
                    shapeRenderer.setColor(shldColorFG);
                    shapeRenderer.arc(cX, cY, radius, (4 - n) * 90.0f + 45.0f, 90.0f);
                }
                shldColorBG.set(0, 0, 0, 0.5f);
                shapeRenderer.setColor(shldColorBG);
                shapeRenderer.circle(x + radius, y + radius, radius * 7.0f / 8.0f);
                shapeRenderer.setColor(Color.BLACK);
                shapeRenderer.line(x + radius, y, x + radius, y + radius);
                shapeRenderer.end();
                return isdead;
            }
        };
    }

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
    private final GameEvent seeObjectEvent = new GameEvent() {

        final Vector3 tmpV = new Vector3();
        final Vector3 posV = new Vector3();
        final GfxUtil lineInstance = new GfxUtil();

        @Override
        public void handle(Entity picked, GameEvent.EventType eventType) {

            if (EVT_SEE_OBJECT == eventType && null != picked) {
                ModelComponent mc = pickedPlayer.getComponent(ModelComponent.class);

                if (null != mc) {
                    GfxBatch.draw(lineInstance.lineTo(
                            mc.modelInst.transform.getTranslation(posV),
                            picked.getComponent(ModelComponent.class).modelInst.transform.getTranslation(tmpV),
                            Color.LIME));
                }
            }
        }
    };

    private final Vector3 tmpV = new Vector3();
    private final Vector3 tmpPos = new Vector3();
    private final GfxUtil camDbgLineInstance = new GfxUtil();
    private final SteeringEntity chaserSteerable = new SteeringEntity();
    private Matrix4 chaserTransform;

    @Override
    public void render(float delta) {
        // plots debug graphics
        super.render(delta);
        // put in any debug graphics to the render pipeline
        ModelComponent mc = pickedPlayer.getComponent(ModelComponent.class);
        if (null != mc) {
            GfxBatch.draw(
                    camDbgLineInstance.lineTo(mc.modelInst.transform.getTranslation(tmpPos),
                            chaserTransform.getTranslation(tmpV), Color.PURPLE));
        }
        BulletWorld.getInstance().update(delta, cam);
        chaserSteerable.update(delta);
        camController.update();
        playerUI.act(Gdx.graphics.getDeltaTime());
        playerUI.draw();
        // update entities queued for spawning
        runCleanerSpawner();

        if (GameWorld.GAME_STATE_T.ROUND_OVER_RESTART == GameWorld.getInstance().getRoundActiveState()) {

            screenTeardown();

            GameFeature localPlayer = GameWorld.getInstance().getFeature(GameWorld.LOCAL_PLAYER_FNAME);
            if (null != localPlayer) {
                GameWorld.getInstance().reloadSceneData(localPlayer.getObjectName());
            }
            SceneLoader.doneLoading();
            setup();
        }
    }

    private void runCleanerSpawner() {

        purgeExpiredEntities();

        SceneData sd = GameWorld.getInstance().getSceneData();
        ModelGroup mg = sd.modelGroups.get(ModelGroup.SPAWNERS_MGRP_KEY);

        if (null != mg /* && mg.size > 0 */) {
            mg.build(engine, true); // delete objects flag not really needed if rmv the group each frame update
            sd.modelGroups.remove(ModelGroup.SPAWNERS_MGRP_KEY); // delete the group
        }
    }

    /**
     * sweep entities queued for deletion (needs to be done outside of engine/simulation step)
     */
    private void purgeExpiredEntities() {

        for (Entity e : engine.getEntitiesFor(Family.all(StatusComponent.class).get())) {

            StatusComponent sc = e.getComponent(StatusComponent.class);

            if (0 == sc.lifeClock) {
                ModelComponent mc = e.getComponent(ModelComponent.class);
                // explode effect only available for models w/ child nodes
                if (null != mc) {
                    BulletComponent bc = e.getComponent(BulletComponent.class);

                    if (null != bc) {
                        explodacopia(engine, bc.shape, mc.modelInst);
                    }
                }

                FeatureComponent fc = e.getComponent(FeatureComponent.class);

                if (null != fc && null != fc.featureAdpt) {
                    fc.featureAdpt.onDestroyed(e);

                    int bounty = fc.featureAdpt.bounty;
                    // bounty provides either points or powerups
                    if (bounty >= Crapium.BOUNTY_POWERUP) {

                        if (null != fc.featureAdpt.fSubType) {
                            // map "sub-feature" to a weapon type
                            int wtype =
                                    fc.featureAdpt.fSubType.ordinal() - FeatureAdaptor.F_SUB_TYPE_T.FT_WEAAPON_0.ordinal(); // i don't know about this

                            if (wtype > 0) {
                                onWeaponAcquired(wtype);
                            }
                        }
                    } else if (bounty > 0) {
                        StatusComponent psc = pickedPlayer.getComponent(StatusComponent.class);
                        if (null != psc) {
                            psc.bounty += bounty;
                        }
                    }
                }
                engine.removeEntity(e); // calls BulletSystem:entityRemoved(), but the bc is not useable :(
            } else {
                if (2 == sc.deleteFlag) { // will use flags for comps to remove
                    // only the BC is removed, but the entity is not destroyed
                    e.remove(BulletComponent.class); // triggers BulletSystem:entityRemoved() ?????
                }
            }
            sc.deleteFlag = 0;
        }
    }

    /**
     * Exploding effect for Compound shapes
     *
     * @param engine    the engine
     * @param shape     collision shape
     * @param modelInst model instance
     */
    private void explodacopia(Engine engine, btCollisionShape shape, ModelInstance modelInst) {

        final String key = "020";
        Vector3 slocation = new Vector3();
        GameWorld.AudioManager.playSound(key, modelInst.transform.getTranslation(slocation));

        if ((null != shape) && shape.className.equals("btCompoundShape")) {
            Vector3 translation = new Vector3();
            Quaternion rotation = new Quaternion();

            /*
             *todo cosolidate game object instantiation and instance
             */
            GameObject gameObject = new GameObject(1);

            gameObject.getInstanceData().add(new InstanceData(
                    modelInst.transform.getTranslation(translation),
                    modelInst.transform.getRotation(rotation))
            );
            // build nodes by iterating the node id list, which hopefully is in same index order as when the comp shape was builtup
            gameObject.buildChildNodes(engine, modelInst.model, (btCompoundShape) shape);
        } else {
            Gdx.app.log(CLASS_STRING, "Compound shape only valid for btCompoundShape");
        }
    }

    @SuppressWarnings("unused")
    /*
     * debug only (batch is ended each call)
     */
    private void debugPrint(String string, Color color, int row, int col) {

        int y = (int) ((float) row * debugPrintFont.getLineHeight() + debugPrintFont.getLineHeight());
        int x = (int) ((float) col * debugPrintFont.getLineHeight());
        batch.setProjectionMatrix(guiCam.combined);
        batch.begin();
        debugPrintFont.setColor(color);
        debugPrintFont.draw(batch, string, x, y);
        batch.end();
    }

    @Override
    public void show() {
        setup();
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
    private void screenTeardown() {

        engine.removeSystem(bulletSystem); // make the system dispose its stuff
        engine.removeAllEntities(); // allow listeners to be called (for disposal)

        BulletWorld.getInstance().dispose();
        playerUI.dispose();
        debugPrintFont.dispose(); // only instantiated on show() for some reason

        PrimitivesBuilder.clearShapeRefs();

        // other Systems may have run after last Render System update, so be sure
        // to clear this queue of model instances first before invalidating the underlying models.
        GfxUtil.clearRefs();
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
