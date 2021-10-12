/*
 * Copyright (c) 2021 Glenn Neidermeier
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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.util.GameEvent;

/*
 * extends Table which means that it can implement act() which will be called by the parent ..
 * Intended to be added as a child actor to GameScreen which is an extended Stage
 *
 */
public class Gunrack extends Table {

    public enum WeaponType {
        STANDARD_AMMO,
        HI_IMPACT_PRJ,
        PLASMA_GRENADES,
        UNDEFINED
    }

    // contains all possible weapon types, and rounds count - any with rounds > 0 are added to the menu
    private final Array<WeaponSpec> weaponsSpecs = new Array<>();

    private final GameEvent hitDetectEvent; // because it needs to be passed to gunPlatform
    private final Label selectionLabel;
    private final Label roundsLabel;

    private int menuPointer;
    private int menuSelection;
    private WeaponType selectedWeapon;
    private Array<WeaponInstance> weaponsMenu;

    /**
     * Object definition for an element of the weapon menu
     * Superfluous ... todo menu to be array of object WeaponType
     */
    static class WeaponInstance {
        final WeaponType weaponType;

        WeaponInstance(WeaponType type) {
            this.weaponType = type;
        }
    }

    static class WeaponSpec {
        WeaponType weaponType;
        private String descriptionText;
        private String buttonLabelText;
        private int roundsCap = 1; // default is to have at least 1 round available
        private int roundsAvail;

        WeaponSpec() {
            this(WeaponType.STANDARD_AMMO, "Standard Ammo", 999999999);
            reset();
        }

        WeaponSpec(int roundsCap) {
            this.roundsCap = roundsCap;
        }

        WeaponSpec(WeaponType weaponType, String descriptionText, int roundsCap) {
            this.roundsCap = roundsCap;
//            this.roundsAvail = 0; // caller resets it if desired
            this.weaponType = weaponType;
            this.descriptionText = descriptionText;
            this.buttonLabelText = descriptionText.substring(0, 5).toUpperCase();
        }

        private int fire() {
            if (roundsAvail > 0) {
                roundsAvail -= 1;
            }
            return roundsAvail;
        }

        void reset() {
            roundsAvail = roundsCap;
        }
    }

    Gunrack(GameEvent hitDetectEvent, BitmapFont font) {

        this.hitDetectEvent = hitDetectEvent;
        // always add a Standard Ammo weapon spec at index 0
        weaponsSpecs.add(new WeaponSpec()); // std ammo
        weaponsSpecs.add(new WeaponSpec(WeaponType.HI_IMPACT_PRJ, "Hi Impact Projectile", 5));
        weaponsSpecs.add(new WeaponSpec(WeaponType.PLASMA_GRENADES, "Plasma Grenades", 10));

        selectionLabel = new Label("wselect", new Label.LabelStyle(font, Color.CHARTREUSE));
        selectionLabel.setVisible(false); // only see this when weapon select menu active
        add(selectionLabel);
        bottom().left();

        roundsLabel = new Label("rounds", new Label.LabelStyle(font, Color.TEAL));
        add(roundsLabel).padRight(1);
        roundsLabel.setVisible(false);

        // setSelection
        menuPointer = 0;
        menuSelection = 0;
        selectedWeapon = WeaponType.STANDARD_AMMO;
        onWeaponAcquired(selectedWeapon.ordinal());

        setFillParent(true);
        setVisible(false);
//        this.setDebug(true);
    }

    @Override
    public void act(float delta) {
        // make sure Actions get invoked!
        super.act(delta);

        if (WeaponType.UNDEFINED != selectedWeapon && WeaponType.STANDARD_AMMO != selectedWeapon) {
            roundsLabel.setText("=" + getRoundsAvailable());
            roundsLabel.setVisible(true);
        } else {
            roundsLabel.setVisible(false);
        }
    }

    /**
     * set the menu selection
     * "energizing" the newly selected weapon should take about 0.75 seconds
     *
     * @param mp the index of the selected menu item
     */
    private int setSelection(int mp) {
        menuSelection = mp;
        return menuSelection;
    }

    /**
     * handle the weapon menu input
     */
    void onMenuEvent() {
        // set pointer and selection to next menu item
        menuPointer = setSelection(menuPointer + 1);
        // if pointer equal to number of available menu items, wrap it around to 0
        if (menuPointer >= weaponsMenu.size) {
            menuPointer = 0;
        }
        // key is struck, so set start alpha of fadeout effect
        final float WPN_MENU_FADE_ALPH = 0.9f; // alpha value at which change the rate of menu fadeout effect
        Color selectMenuColor = new Color(Color.WHITE);
        selectMenuColor.a = WPN_MENU_FADE_ALPH;
        selectionLabel.setColor(selectMenuColor);

        WeaponType wtype = weaponsMenu.get(menuPointer).weaponType;
        selectionLabel.setText(getWeaponSpec(wtype.ordinal(), false).buttonLabelText);
        selectionLabel.clearActions();

        // when the menu times out, it is hidden and the selected weapon is activated
        Action setMenuSelection = new Action() {
            public boolean act(float delta) {
                menuSelection = menuPointer;
                onInputX(); // send key-event to enable the selection
                return true;
            }
        };
        selectionLabel.addAction(Actions.sequence(
                Actions.show(),
                Actions.delay(2.0f),
                Actions.alpha(0.1f, 0.75f),
                setMenuSelection,
                Actions.hide())
        );
        this.setVisible(true);
    }

    /**
     * Handle X input on weapon menu
     *
     * @return true if menu is active and a new weapon has been selected, otherwise false
     */
    private boolean updateMenu() {

        WeaponType wtype = weaponsMenu.get(menuPointer).weaponType;

        if (menuSelection != menuPointer) {
            menuSelection = menuPointer; // set the selection
            // menu item is selected (X controller button) - restart menu fade
            if (WeaponType.STANDARD_AMMO != wtype) {
                // similar to action on menu select key, but no setMenuSelection()
                selectionLabel.addAction(Actions.sequence(
                        Actions.show(),
                        Actions.delay(2.0f),
                        Actions.alpha(0.1f, 0.75f),
                        // setMenuSelection, .. selection already has been set ... does it matter?
                        Actions.hide())
                );
            }
        }
        if (menuSelection != selectedWeapon.ordinal()) {
            selectedWeapon = getWeaponSpec(wtype.ordinal(), false).weaponType;
            return true;
        }
        return false;
    }

    boolean onInputX() {
        if (selectionLabel.isVisible()) {
            return updateMenu();
        }
        return false;
    }

    /**
     * initialize and reset to standard ammo
     */
    void resetStandard() {
        menuPointer = 0;
        onWeaponAcquired(menuPointer);
        updateMenu(); // forces menu activation
    }

    /**
     * @return number of rounds available
     */
    int onWeaponAcquired(int weaponType) {

        WeaponSpec newWS = null;
        int rounds = 0;
        if (weaponType < weaponsSpecs.size) {
            newWS = weaponsSpecs.get(weaponType);
        }
        if (null != newWS) {
            newWS.reset(); // initialize rounds count etc.
            rounds = newWS.roundsAvail;
        }
        // reset the menu
        weaponsMenu = new Array<>();

        // walk the spec list and add any weapon spec with any rounds remaining > 0
        for (WeaponSpec ws : weaponsSpecs) {
            if (null != ws) {
                if (ws.weaponType == WeaponType.STANDARD_AMMO) { // slot 0 always fully loaded w/ std ammo
                    ws.reset();
                }
                if (ws.roundsAvail > 0) {
                    weaponsMenu.add(new WeaponInstance(ws.weaponType));
                }
            }
        }
        return rounds;
    }

    public WeaponType getSelectedWeapon() {

        return selectedWeapon;
    }

    public int fireWeapon() {

        int rounds = -1;

        WeaponSpec spec = weaponsSpecs.get(selectedWeapon.ordinal());
        if (null != spec) {
            rounds = spec.fire();
        }
        return rounds;
    }

    // for future use
    public GameEvent getHitDetector() {
        return this.hitDetectEvent;
    }

    int getRoundsAvailable() {
        return getRoundsAvailable(selectedWeapon.ordinal());
    }

    private int getRoundsAvailable(int index) {
        return getWeaponSpec(index, false).roundsAvail;
    }

    /**
     * @param index weapon type
     * @return text string descriptor (for display onscreen UI)
     */
    String getDescription(int index) {
        WeaponSpec spec = getWeaponSpec(index, false);
        return spec.descriptionText + " (" + getRoundsAvailable(index) + ")";

    }

    private WeaponSpec getWeaponSpec(int index, boolean doReset) {

        WeaponSpec ws = weaponsSpecs.get(index);
        if (null != ws) {
            if (doReset) {
                ws.reset();
            }
        } else {
            ws = new WeaponSpec(); // std ammo
        }
        return ws;
    }
}
