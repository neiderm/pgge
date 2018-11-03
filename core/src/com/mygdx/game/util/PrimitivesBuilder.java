package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by mango on 12/18/17.
 */

public class PrimitivesBuilder extends BulletEntityBuilder {

    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    protected static final float DIM_UNIT = 1.0f;
    protected static final float DIM_HE = 1f / 2f; // primitives half extent constant
    protected static final float DIM_CAPS_HT = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)


//    public static final PrimitivesModel instance = new PrimitivesModel();

    /* private */ public // hakakakakakakak
    static /*final */ Model primitivesModel;

    private PrimitivesBuilder() {
        model = primitivesModel;
    }

    public static void init() {

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
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(1f * DIM_HE, DIM_CAPS_HT, 10); // note radius and height vs. bullet
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 1f, 1f, 10);

        attributes |= VertexAttributes.Usage.TextureCoordinates;

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


    public static Entity loadSphere(float r, Vector3 pos) {
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


    /*
    Generate bullet shapes by applying the same scale/size as shall be applied to the vertices of the instance mesh.
    primitive meshes should use unit value (1.0) for the extent dimensions, thus those base dimensions don't have to be multiplied in explicitly in the shape sizing calculation below.
    In some cases we have to take special care as bullet shapes don't all parameterize same way as gdx model primitives.
    Constant "DIM_HE" (primitives-half-extent) is used interchangeably to compute radius from size.x, as well as half extents where needed.
    */

    public static PrimitivesBuilder getSphereBuilder(final String nodeID) {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, nodeID, new btSphereShape(size.x * DIM_HE), size, mass, trans);
            }
        };
    }

    public static PrimitivesBuilder getBoxBuilder(final String nodeID) {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, nodeID, new btBoxShape(size.cpy().scl(DIM_HE)), size, mass, trans);
            }
        };
    }

    private static PrimitivesBuilder getSphereBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "sphere", new btSphereShape(size.x * DIM_HE), size, mass, trans);
            }
        };
    }

    private static PrimitivesBuilder getBoxBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "box", new btBoxShape(size.cpy().scl(DIM_HE)), size, mass, trans);
            }
        };
    }

    private static PrimitivesBuilder getConeBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "cone", new btConeShape(size.x * DIM_HE, size.y), size, mass, trans);
            }
        };
    }

    private static PrimitivesBuilder getCapsuleBuilder() {
        return new PrimitivesBuilder() {
            @Override
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
                //  so we apply half extent factor to our size.x here.
                float radius = size.x * DIM_HE;
                // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
                //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
                // determine the equivalent bullet-compatible height parameter by explicitly scaling
                // the base mesh height and then subtracting the (scaled) end radii
                float height = DIM_CAPS_HT * size.y - size.x * DIM_HE - size.x * DIM_HE;
                return load(this.model, "capsule", new btCapsuleShape(radius, height), size, mass, trans);
            }
        };
    }

    private static PrimitivesBuilder getCylinderBuilder() {
        return new PrimitivesBuilder() {
            @Override
            // cylinder shape apparently allow both width (x) and height (y) to be specified
            public Entity create(float mass, Vector3 trans, Vector3 size) {
                return load(this.model, "cylinder", new btCylinderShape(size.cpy().scl(DIM_HE)), size, mass, trans);
            }
        };
    }

    /*
     *  For the case of a static model, the Bullet wrapper provides a convenient method to create a
     *  collision shape of it:
     *   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  But in some situations having issues (works only if single node in model, and it has no local translation - see code in Bullet.java)
     */
    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        // we can roll the instance scale transform into the getModelInstance ;)
        ModelInstance instance = ModelInstanceEx.getModelInstance(model, nodeID);

        //        if (null != size)
// https://stackoverflow.com/questions/21827302/scaling-a-modelinstance-in-libgdx-3d-and-bullet-engine
        // note : modelComponent creating bounding box
        instance.nodes.get(0).scale.set(size);
        instance.calculateTransforms();

        // leave translation null if using translation from the model layout
//        if (null != translation)
        instance.transform.trn(translation);


        BulletComponent bc = new BulletComponent(shape, instance.transform, mass);

        if (0 == mass) {
            // special sauce here for static entity
// set these flags in bullet comp?
            bc.body.setCollisionFlags(
                    bc.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_KINEMATIC_OBJECT);
            bc.body.setActivationState(Collision.DISABLE_DEACTIVATION);
        }

        Entity e  = new Entity();
        e.add(new ModelComponent(instance));
        e.add(bc);

        return e;
    }

    public static void dispose() {
        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the primitivesModel will automatically make all instances invalid!
        primitivesModel.dispose();
        primitivesModel = null;
    }
}
