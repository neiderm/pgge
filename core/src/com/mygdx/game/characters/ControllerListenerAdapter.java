package com.mygdx.game.characters;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

public class ControllerListenerAdapter implements ControllerListener {

    @Override
    public void connected(Controller controller) { // mt
    }

    @Override
    public void disconnected(Controller controller) { // mt
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
        return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderIndex, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderIndex, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerIndex, Vector3 value) {
        return false;
    }
}
