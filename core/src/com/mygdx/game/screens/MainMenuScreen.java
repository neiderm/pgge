package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.GameWorld;

/**
 * Created by mango on 12/18/17.
 */

// https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java#L37

public class MainMenuScreen implements Screen {

    private GameWorld world;
    private BitmapFont font;
    private Stage stage;


    public MainMenuScreen(GameWorld world) {

        this.world = world;

        int WWW = Gdx.graphics.getWidth() / 2;
        int HHH = Gdx.graphics.getHeight() / 2;

//        Gdx.graphics.setWindowedMode(800,600);
        font = new BitmapFont();

        //https://github.com/dfour/box2dtut/blob/master/box2dtut/core/src/blog/gamedevelopment/box2dtutorial/views/EndScreen.java
        // create stage and set it as input processor
        stage = new Stage(new ScreenViewport());

    // create table to layout items we will add
    Table table = new Table();
    table.setFillParent(true);
    table.setDebug(true);

        Pixmap background = new Pixmap(WWW, HHH, Pixmap.Format.RGBA8888);
                background.setColor(1, 0, 223/255f, 1);
        background.fillRectangle(0, 0, WWW /2, HHH /2);
        table.setBackground(new TiledDrawable(new TextureRegion(new Texture(background))));


            //create a Labels showing the score and some credits
    Skin skin = new Skin();
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    skin.add("white", new Texture(pixmap)); //https://github.com/libgdx/libgdx/blob/master/tests/gdx-tests/src/com/badlogic/gdx/tests/UISimpleTest.java


        // GdxRuntimeException: No com.badlogic.gdx.scenes.scene2d.ui.Label$LabelStyle registered with name: default
        Label.LabelStyle textStyle = new Label.LabelStyle(font, Color.WHITE);
//        textStyle.font = font;
        skin.add("default", textStyle);

        // Store the default libgdx font under the name "default".
        skin.add("default", new BitmapFont());

        // Configure a TextButtonStyle and name it "default". Skin resources are stored by type, so this doesn't overwrite the font.
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.down = skin.newDrawable("white", Color.DARK_GRAY);
        textButtonStyle.checked = skin.newDrawable("white", Color.BLUE);
        textButtonStyle.over = skin.newDrawable("white", Color.LIGHT_GRAY);
        textButtonStyle.font = skin.getFont("default");
        skin.add("default", textButtonStyle);

        // Create a button with the "default" TextButtonStyle. A 3rd parameter can be used to specify a name other than "default".
        final TextButton textButton = new TextButton("Click me!", skin);
        table.add(textButton);


    Label labelCredits1 = new Label("Silly Tank Nonsense", skin);
    // add items to table
    table.row().padTop(10);
    table.add(labelCredits1).uniformX().align(Align.left);


        Pixmap.setBlending(Pixmap.Blending.None);
        Pixmap button = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        button.setColor(1, 0, 0, 1);
        button.fillCircle(25, 25, 25);
        Texture myTexture = new Texture(button);
        TextureRegion myTextureRegion = new TextureRegion(myTexture);
        TextureRegionDrawable myTexRegionDrawable = new TextureRegionDrawable(myTextureRegion);

        ImageButton buttonB = new ImageButton(myTexRegionDrawable); //Set the buttonA up
        buttonB.setPosition(3 * Gdx.graphics.getWidth() / 4f, Gdx.graphics.getHeight() / 9f);
        buttonB.addListener(buttonBListener);
table.row().padLeft(10);
        table.add(buttonB);

//        https://stackoverflow.com/questions/17127201/libgdx-add-scores-display-it-at-top-left-corner-of-the-screen
//font.setUseIntegerPositions(false);// (Optional)
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = font;

        Label text = new Label("Play!", style);
text.setText("Play___!");
        //        text.setBounds(0,.2f,Room.WIDTH,2);
        text.setFontScale(1f,1f);

        table.row(); // .padTop(10);
//        table.add(text).uniformX().align(Align.left);
        table.add(text);

        //add table to stage
        stage.addActor(table);

        Gdx.input.setInputProcessor(stage);
    }


    private final InputListener buttonBListener = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
        }
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            world.game.setScreen(new GameScreen(world));
        }
    };



    @Override
    public void render(float delta) {

//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//                 Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        stage.act( /* Gdx.graphics.getDeltaTime() */);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void dispose() {
        font.dispose();
    }

    @Override
    public void show() {
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
