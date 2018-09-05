package com.mygdx.game.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.mygdx.game.components.CharacterComponent;
import com.mygdx.game.components.ModelComponent;
import com.mygdx.game.util.GameEvent;
import com.mygdx.game.util.ModelInstanceEx;

import static com.mygdx.game.util.GameEvent.EventType.RAY_DETECT;

/**
 * Created by mango on 2/10/18.
 */

public class CharacterSystem extends IteratingSystem implements EntityListener {

    Signal<GameEvent> gameEventSignal;

    public CharacterSystem(Signal<GameEvent> gameEventSignal) {

        super(Family.all(CharacterComponent.class).get());
        this.gameEventSignal = gameEventSignal;
    }

    @Override
    public void addedToEngine(Engine engine) {

        super.addedToEngine(engine);

        // listener for these so that their bullet objects can be dispose'd
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {

        engine.removeEntityListener(this); // Ashley bug (doesn't remove listener when system removed?
    }

    @Override
    public void entityAdded(Entity entity) {
        //empty
    }

    @Override
    public void entityRemoved(Entity entity) {
        //empty
    }


    private Vector3 position = new Vector3();
    private Quaternion rotation = new Quaternion();
    private Vector3 direction = new Vector3(0, 0, -1); // vehicle forward


    @Override
    protected void processEntity(Entity entity, float deltaTime) {

        CharacterComponent comp = entity.getComponent(CharacterComponent.class);

        if (null != comp.character)
            comp.character.update(entity, deltaTime, comp.lookRay); // cameraMan!


        if (null != comp.steerable) {
            comp.steerable.update(deltaTime);

            // different things have different means of setting their lookray
            Matrix4 transform = entity.getComponent(ModelComponent.class).modelInst.transform;
            transform.getTranslation(position);
            transform.getRotation(rotation);

            if (null != comp.gameEvent) { // not all support gameeventsignal
                Ray lookRay = entity.getComponent(CharacterComponent.class).lookRay;
                lookRay.set(position, ModelInstanceEx.rotateRad(direction.set(0, 0, -1), rotation));

//                        try {
                gameEventSignal.dispatch(comp.gameEvent.set(RAY_DETECT, lookRay, 0));
//                        } catch (NullPointerException ex) {
//                            System.out.println("NumberFormatException is occured");
//                        }
            }
        }
    }
}
