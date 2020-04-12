/*
 * Copyright (c) 2020 Glenn Neidermeier
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
        UNDEFINED
    }

    private static final float WPN_MENU_FADE_ALPH = 0.9f; // alpha value at which change the rate of menu fadeout effect
    private static final float WPN_MENU_FADE_THRD = 0.8f; // alpha value at which change the rate of menu fadeout effect

    private int menuPointer;
    private int menuSelection;

    private static Color menuColor = new Color(Color.WHITE);

    private WeaponType selectedWeapon = WeaponType.UNDEFINED; // weapon array element 0 ^H^H^H -1 is a hack to force it to be (re)initialized

    private Array<WeaponSpec> weaponsSpecs = new Array<WeaponSpec>();

    private Array<WeaponInstance> weaponsMenu;

    private GameEvent hitDetectEvent; // because it needs to be passed to gunPlatfrom ... sue me

    private Label selectionLabel;
    private Label roundsLabel;

    /*
     * need some kind of type in order to put the menu items in an array
     */
    class WeaponInstance {
        int type;

        //        int roundsAvailable; ... no rounds available must persist outside the menu array
        WeaponInstance(int type) {
            this.type = type;
        }
    }

    class WeaponSpec {
        //        private int roundsCap = OUT_OF_AMMO; // could set a real default here .. was trying to catch the other plce it shouldn't have been set!
        private int roundsCap; // set a default here ?
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


    public Gunrack(GameEvent hitDetectEvent, BitmapFont font) {

        this.hitDetectEvent = hitDetectEvent;

        weaponsSpecs.add(new WeaponSpec(5555, true)); // std ammo
        weaponsSpecs.add(new WeaponSpec(5)); // where to set this "const" value?

        onWeaponAcquired(0);

//        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
//        selectionLabel = new Label("asedfasdf", skin);
        selectionLabel = new Label("wselect", new Label.LabelStyle(font, Color.CHARTREUSE));
        roundsLabel = new Label("rounds", new Label.LabelStyle(font, Color.TEAL));

        this.setFillParent(true);

        selectionLabel.setVisible(false); // only see this when weaopon select menu active
        this.add(selectionLabel);
        this.bottom().left();

        this.add(roundsLabel).padRight(1);
        this.roundsLabel.setVisible(false);

        this.setVisible(false);
//        this.setDebug(true);
//        parent.addActor(this);
    }

    @Override
    public void act(float delta) {

        update();
    }

    /*
     * act()
     */
    void update() {

        roundsLabel.setVisible(false);

        int rounds = getRoundsAvailable();

        if (WeaponType.UNDEFINED != selectedWeapon && WeaponType.STANDARD_AMMO != selectedWeapon) {
            roundsLabel.setText("=" + rounds);
            roundsLabel.setVisible(true);
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
            //  treat as is select button has been pressed (auto-select item at the menu pointer)
            menuSelection = menuPointer;
        }

        clr.a = alpha;
        selectionLabel.setColor(clr);
    }

    void onMenuEvent() {
        // try to advance the selection
        menuPointer = menuPointer + 1;
        if (menuPointer >= weaponsMenu.size /* nrAvailable */) {
            menuPointer = 0;
        }

        // key is struck, so set start alpha of fadeout effect
        selectionLabel.setColor(menuColor.r, menuColor.g, menuColor.b, Gunrack.WPN_MENU_FADE_ALPH);
        selectionLabel.setText(getMenuInfo(menuPointer));
        selectionLabel.setVisible(true);

        this.setVisible(true);
    }

    /*
     * forces a phony menu selection of 0 for chaning to std ammo after weapon rounds exhausted
     */
    void onSelectMenu(int value) {

        menuPointer = value;
        onSelectMenu(); // forces menu activation
    }

    boolean onSelectMenu() {
//        if (bWepnMenuActive)
        {
            if (menuSelection != menuPointer) {
                // selection updated - instead of hiding menu immediately, set color to indicate and let menu do "normal "fadeout
                Color clr = selectionLabel.getColor();
                clr.a = Gunrack.WPN_MENU_FADE_THRD;
//                selectionLabel.setColor(clr);
                menuSelection = menuPointer;
            }
        }

        int w = selectedWeapon.ordinal();

        if (menuSelection != w) {
            WeaponType wt;
///
            switch (menuSelection) {
                case 0:
                    wt = WeaponType.STANDARD_AMMO;
                    break;
                case 1:
                    wt = WeaponType.HI_IMPACT_PRJ;
                    break;
                default:
                    wt = WeaponType.UNDEFINED;
                    break;
            }
            this.selectedWeapon = wt;
///
            return true;
        }

        return false;
    }


    int onWeaponAcquired(int weaponType) {

        WeaponSpec ws = weaponsSpecs.get(weaponType);
        if (null != ws) {
            ws.reset(); // got a refill !
        }

        return rebuildWeaponCache();
    }


    public WeaponType getSelectedWeapon() {

        return selectedWeapon;
    }

    public int fireWeapon() {

        int rounds = -1;
        WeaponSpec spec = null;

        if (true /*WeaponType.UNDEFINED != selectedWeapon*/) {
            int iws = selectedWeapon.ordinal();
            spec = weaponsSpecs.get(iws);
        }
//        else {
//            System.out.println("weapon index out of range = " + selectedWeapon);
//        }
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

    /*
     * return text string descriptor (for display onscreen UI)
     */
    String getDescription(int wtype) {
        String mesg;
        int rounds = getRoundsAvailable(wtype);

        switch (wtype) {
            default:
            case 0:
                mesg = "STANDARD AMMO";
                break;
            case 1:
                mesg = "HI IMPACT PROJECTILE (" + rounds + ")";
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

    /*
     * returns nr weapons available
     */
    private int rebuildWeaponCache() {
// first reset the active cache and count ... actual array not doing much right now but is used for tracking the number of menu elements,
        weaponsMenu = new Array<WeaponInstance>();

// walk the spec list and add any weapon spec with any rounds remaining > 0
        for (int i = 0; i < weaponsSpecs.size; i++) {

            WeaponSpec ws = weaponsSpecs.get(i);

            if (null != ws) {
                if (i == 0) { // slot 0 always fully loaded w/ std ammo
//                    ws.roundsCap = 9999; // no don't set this default here it is confusing!
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
