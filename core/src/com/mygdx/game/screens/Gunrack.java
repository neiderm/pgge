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

    private static final float WPN_MENU_FADE_ALPH = 0.9f; // alpha value at which change the rate of menu fadeout effect
    private static final float WPN_MENU_FADE_THRD = 0.8f; // alpha value at which change the rate of menu fadeout effect
    private static final Color menuColor = new Color(Color.WHITE);
    private final Array<WeaponSpec> weaponsSpecs = new Array<>();
    private final GameEvent hitDetectEvent; // because it needs to be passed to gunPlatfrom ... sue me
    private final Label selectionLabel;
    private final Label roundsLabel;

    private int menuPointer;
    private int menuSelection;
    private WeaponType selectedWeapon = WeaponType.UNDEFINED; // weapon array element 0 ^H^H^H -1 is a hack to force it to be (re)initialized
    private Array<WeaponInstance> weaponsMenu;

    /*
     * need some kind of type in order to put the menu items in an array
     */
    static class WeaponInstance {
        final int type;

        WeaponInstance(int type) {
            this.type = type;
        }
    }

    static class WeaponSpec {
        private int roundsCap; // should default to 0
        private int roundsAvail;

        void reset() {
            roundsAvail = roundsCap;
        }

        WeaponSpec(int roundsCap) {
            this.roundsCap = roundsCap;
        }

        WeaponSpec(int roundsCap, boolean reset) {
            this.roundsCap = roundsCap;
            if (reset) {
                reset();
            }
        }

        int fire() {
            if (roundsAvail > 0) {
                roundsAvail -= 1;
            }
            return roundsAvail;
        }
    }

    Gunrack(GameEvent hitDetectEvent, BitmapFont font) {

        this.hitDetectEvent = hitDetectEvent;
        // always add a Standard Ammo weapon spec
        weaponsSpecs.add(new WeaponSpec(5555, true)); // std ammo
// todo - - need to be added when the item is acquired!
        weaponsSpecs.add(new WeaponSpec(5)); // where to set this "const" value?
        weaponsSpecs.add(new WeaponSpec(10)); // where to set this "const" value?

        // weaponsMenuSize =
        onWeaponAcquired(0);

        selectionLabel = new Label("wselect", new Label.LabelStyle(font, Color.CHARTREUSE));
        selectionLabel.setVisible(false); // only see this when weaopon select menu active
        add(selectionLabel);
        bottom().left();

        roundsLabel = new Label("rounds", new Label.LabelStyle(font, Color.TEAL));
        add(roundsLabel).padRight(1);
        roundsLabel.setVisible(false);

        setFillParent(true);
        setVisible(false);
//        this.setDebug(true);
    }

    @Override
    public void act(float delta) {
        // make sure Actions get invoked!
        super.act(delta);

        int rounds = getRoundsAvailable();

        if (WeaponType.UNDEFINED != selectedWeapon && WeaponType.STANDARD_AMMO != selectedWeapon) {
            roundsLabel.setText("=" + rounds);
            roundsLabel.setVisible(true);
        } else {
            roundsLabel.setVisible(false);
        }

        Color clr = selectionLabel.getColor();
        float alpha = clr.a;

        if (alpha > Gunrack.WPN_MENU_FADE_THRD) {
            alpha -= 0.001f;
        } else if (alpha > 0.10f) {
            // fade out until low threshold of visibility is reached
            alpha -= 0.01f;
        } else {
            // menu timeout, make it disappear
            selectionLabel.setVisible(false);
            //  select the item at the menu pointer
            setSelection(menuPointer);
        }
        clr.a = alpha;
        selectionLabel.setColor(clr);
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
        selectionLabel.setColor(menuColor.r, menuColor.g, menuColor.b, Gunrack.WPN_MENU_FADE_ALPH);
        selectionLabel.setText(getMenuInfo(menuPointer));
        selectionLabel.setVisible(true);
        this.setVisible(true);
    }

    /**
     * Handle X input on weapon menu
     *
     * @return true if menu is active and a new weapon has been selected, otherwise false
     */
    boolean updateMenu() {
        if (menuSelection != menuPointer) {
            // selection updated - instead of hiding menu immediately, set color to indicate and let menu do "normal "fadeout
            Color clr = selectionLabel.getColor();
            clr.a = Gunrack.WPN_MENU_FADE_THRD;
            menuSelection = menuPointer;
        }

        int w = selectedWeapon.ordinal();
        if (menuSelection != w) {
            WeaponType wt;

            switch (menuSelection) {
                case 0:
                    wt = WeaponType.STANDARD_AMMO;
                    break;
                case 1:
                    wt = WeaponType.HI_IMPACT_PRJ;
                    break;
                case 2:
                    wt = WeaponType.PLASMA_GRENADES;
                    break;
                default:
                    wt = WeaponType.UNDEFINED;
                    break;
            }
            this.selectedWeapon = wt;
            return true;
        }
        return false;
    }

    boolean onInputX() {
        if (isVisible()) {
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
     * @return number of weapons available
     */
    int onWeaponAcquired(int weaponType) {

        WeaponSpec ws = null;

        if (weaponType < weaponsSpecs.size) {
            ws = weaponsSpecs.get(weaponType);
        }
        if (null != ws) {
            ws.reset(); // initialize rounds count etc.
        }
        // weaponsMenuSize =
        return rebuildWeaponCache();
    }


    public WeaponType getSelectedWeapon() {

        return selectedWeapon;
    }

    public int fireWeapon() {

        int rounds = -1;
        WeaponSpec spec = null;
//        if (WeaponType.UNDEFINED != selectedWeapon)
        {
            int iws = selectedWeapon.ordinal();
            spec = weaponsSpecs.get(iws);
        }
        if (null != spec) {
            rounds = spec.fire();
        }
        return rounds;
    }

    // for future use
    public GameEvent getHitDectector() {
        return this.hitDetectEvent;
    }

    int getRoundsAvailable() {
        int iwt = selectedWeapon.ordinal();
        return getRoundsAvailable(iwt);
    }

    private int getRoundsAvailable(int index) {

        int rounds = 0;
        WeaponSpec spec = null;

        if (index >= 0 && index < weaponsSpecs.size) {
            spec = weaponsSpecs.get(index);
        }
        if (null != spec) {
            rounds = spec.roundsAvail;
        }
        return rounds;
    }

    /**
     * @param wtype weapon type
     * @return text string descriptor (for display onscreen UI)
     */
    String getDescription(int wtype) {
        String mesg;
        int rounds = getRoundsAvailable(wtype);
// TODO: make is a String[]
        switch (wtype) {
            default:
            case 0:
                mesg = "STANDARD AMMO";
                break;
            case 1:
                mesg = "HI IMPACT PROJECTILE (" + rounds + ")";
                break;
            case 2:
                mesg = "PLASMA GRENADES (" + rounds + ")";
                break;
        }
        return mesg;
    }

    /*
     * return text string descriptor (for weapon menu onscreen UI)
     */
    private String getMenuInfo(int wtype) {

        String mesg;
        int rounds = getRoundsAvailable(wtype);

        switch (wtype) {
            default:
            case 0:
                mesg = "STD AMMO";
                break;
            case 1:
                mesg = "HI IMPACT PRJ (" + rounds + ")";
                break;
        }
        return mesg;
    }

    /**
     * @return number of weapons available
     */
    private int rebuildWeaponCache() {
        // first reset the active cache and count ... actual array not doing much right now but is
        // used for tracking the number of menu elements,
        weaponsMenu = new Array<>();

        // walk the spec list and add any weapon spec with any rounds remaining > 0
        for (int i = 0; i < weaponsSpecs.size; i++) {

            WeaponSpec ws = weaponsSpecs.get(i);

            if (null != ws) {
                if (i == 0) { // slot 0 always fully loaded w/ std ammo
                    ws.reset();
                }
                if (weaponsSpecs.get(i).roundsAvail > 0) {
                    weaponsMenu.add(new WeaponInstance(i));
                }
            }
        }
        return weaponsMenu.size;
    }
}
