package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;

/**
 * Created by mango on 12/18/17.
 */

public class PrimitivesBuilder extends BulletEntityBuilder {

    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    protected static final float primUnit = 1.0f;
    protected static final float primHE = 1f / 2f; // primitives half extent constant
    protected static final float primCapsuleHt = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)


//    public static final PrimitivesModel instance = new PrimitivesModel();

    private static /*final */Model primitivesModel;

    protected PrimitivesBuilder() {
        model = primitivesModel;
    }

    static {
        final ModelBuilder mb = new ModelBuilder();
        long attributes =
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        Texture tex;

        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.GREEN))).sphere(1f, 1f, 1f, 10, 10);
//                new Material(ColorAttribute.createDiffuse(Color.GREEN), IntAttribute.createCullFace(GL_BACK))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW))).cone(1f, 1f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(1f * primHE, primCapsuleHt, 10); // note radius and height vs. bullet
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 1f, 1f, 10);

         attributes |=  VertexAttributes.Usage.TextureCoordinates;

        tex = new Texture(Gdx.files.internal("data/crate.png"), false);
        mb.node().id = "boxTex";
        mb.part("box", GL20.GL_TRIANGLES, attributes,
                new Material(TextureAttribute.createDiffuse(tex))).box(1f, 1f, 1f);

        tex = new Texture(Gdx.files.internal("data/day.png"), false);
        mb.node().id = "sphereTex";
        mb.part("sphere", GL20.GL_TRIANGLES, attributes,
                new Material(TextureAttribute.createDiffuse(tex))).sphere(1f, 1f, 1f, 10, 10);

        primitivesModel = mb.end();
    }


    public static Entity loadSphere(float r, Vector3 pos){
        return BaseEntityBuilder.load(
                primitivesModel, "sphere", new Vector3(r, r, r), pos);
    }

    public static Entity loadCone(float mass, Vector3 trans, Vector3 size) {
        return getConeBuilder().create(mass, trans, size);
    }
    public static Entity loadCapsule(float mass, Vector3 trans, Vector3 size) {
        return getCapsuleBuilder().create(mass, trans, size);
    }
    public static Entity loadCylinder(float mass, Vector3 trans, Vector3 size) {
        return getCylinderBuilder().create(mass, trans, size);
    }
    public static Entity loadBox(float mass, Vector3 trans, Vector3 size) {
        return getBoxBuilder().create(mass, trans, size);
    }
    public static Entity loadSphere(float mass, Vector3 trans, float r) {
        return getSphereBuilder().create(mass, trans, new Vector3(r, r, r));
    }

    // not sure to keep these
    private static Entity loadBoxTex(float mass, Vector3 trans, Vector3 size) {
        return getBoxBuilder("data/crate.png").create(mass, trans, size);
    }
    private static Entity loadSphereTex(float mass, Vector3 trans, float r) {
        return getSphereBuilder("data/day.png").create(mass, trans, new Vector3(r, r, r));
    }

    /*
    Generate bullet shapes by applying the same scale/size as shall be applied to the vertices of the instance mesh.
    primitive meshes should use unit value (1.0) for the extent dimensions, thus those base dimensions don't have to be multiplied in explicitly in the shape sizing calculation below.
    In some cases we have to take special care as bullet shapes don't all parameterize same way as gdx model primitives.
    Constant "primHE" (primitives-half-extent) is used interchangeably to compute radius from size.x, as well as half extents where needed.
    */

    public static PrimitivesBuilder getSphereBuilder(String texFile) {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "sphereTex", new btSphereShape(size.x * primHE), size, mass, trans);
            }
        };
    }
    public static PrimitivesBuilder getBoxBuilder(String texFile) {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "boxTex", new btBoxShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
    }

    public static PrimitivesBuilder getSphereBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "sphere", new btSphereShape(size.x * primHE), size, mass, trans);
            }
        };
    }
    public static PrimitivesBuilder getBoxBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "box", new btBoxShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
    }
    public static PrimitivesBuilder getConeBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "cone", new btConeShape(size.x * primHE, size.y), size, mass, trans);
            }
        };
    }
    public static PrimitivesBuilder getCapsuleBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
                //  so we apply half extent factor to our size.x here.
                float radius = size.x * primHE;
                // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
                //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
                // determine the equivalent bullet-compatible height parameter by explicitly scaling
                // the base mesh height and then subtracting the (scaled) end radii
                float height = primCapsuleHt * size.y - size.x * primHE - size.x * primHE;
                return load(this.model, "capsule", new btCapsuleShape(radius, height), size, mass, trans);
            }
        };
    }
    public static PrimitivesBuilder getCylinderBuilder() {
        return new PrimitivesBuilder() {
            @Override
            // cylinder shape apparently allow both width (x) and height (y) to be specified
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "cylinder", new btCylinderShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
    }

// another load method for "static" objects
    // Entity load(Model primitivesModel, String rootNodeId, Vector3 size, Vector3 translation)

    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        Entity e;

        if (0 != mass)
            e = load(model, nodeID, size, mass, translation, shape);
        else
            e = load(model, nodeID, shape, translation, size);

        return e;
    }



/*    @Override
    public void dispose() {
        trash();
    }*/

    public static void trash(){
        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the primitivesModel will automatically make all instances invalid!
        primitivesModel.dispose();
        primitivesModel = null;
    }
}
