package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by mango on 4/1/18.
 */

/*
 * extended objects ... a bullet object, optionally a "kinematic" body.
 * primitive object templates have an appropriately sized basic bullet shape bound to them automatically
 * by the overloaded create() method.
 * You would want to be able set a custom material color/texture on them.
 * if same shape instance to be used for multiple entities, then shape would need to be instanced only
 * once in the constructor so you would need to do it a different way/ ???? investigate?.
 * Bounding box only works on mesh and doesn't work if we are scaling the mesh :(
 */
public abstract class SizeableEntityBuilder extends BulletEntityBuilder {

    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    public static final float primUnit = 1.0f;
    public static final float primHE = 1f / 2f; // primitives half extent constant
    public static final float primCapsuleHt = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)

    public static final ArrayMap<String, EntityBuilder> primitiveModels;


    public enum PrimitiveTypes {
        SPHERE,
        BOX,
        CONE,
        CAPSULE,
        CYLINDER
/*                {
                    @Override
                    public PrimitiveTypes getNext() {
                        return values()[0]; // rollover to the first
                    }
                };

        public PrimitiveTypes getNext() {
            return this.ordinal() < PrimitiveTypes.values().length - 1
                    ? PrimitiveTypes.values()[this.ordinal() + 1]
                    : PrimitiveTypes.values()[0];
        }*/
    }


//    private static Model primitivesModel;

    public static SizeableEntityBuilder sphereTemplate;
    public static SizeableEntityBuilder boxTemplate;
    public static SizeableEntityBuilder coneTemplate;
    public static SizeableEntityBuilder capsuleTemplate;
    public static SizeableEntityBuilder cylinderTemplate;

    static {


/*        final ModelBuilder mb = new ModelBuilder();

        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GREEN))).sphere(1f, 1f, 1f, 10, 10);
//                new Material(ColorAttribute.createDiffuse(Color.GREEN), IntAttribute.createCullFace(GL_BACK))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BLUE))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW))).cone(1f, 1f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.CYAN))).capsule(1f * SizeableEntityBuilder.primHE, SizeableEntityBuilder.primCapsuleHt, 10); // note radius and height vs. bullet
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA))).cylinder(1f, 1f, 1f, 10);

        primitivesModel = mb.end();*/


        /*
  Generate bullet shapes by applying the same scale/size as shall be applied to the vertices of the instance mesh.
  primitive meshes should use unit value (1.0) for the extent dimensions, thus those base dimensions don't have to be multiplied in explicitly in the shape sizing calculation below.
  In some cases we have to take special care as bullet shapes don't all parameterize same way as gdx model primitives.
  Constant "primHE" (primitives-half-extent) is used interchangeably to compute radius from size.x, as well as half extents where needed.
 */
        sphereTemplate = new SizeableEntityBuilder() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btSphereShape(size.x * primHE), size, mass, trans);
            }
        };
        boxTemplate = new SizeableEntityBuilder() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btBoxShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };
        coneTemplate = new SizeableEntityBuilder() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btConeShape(size.x * primHE, size.y), size, mass, trans);
            }
        };
        capsuleTemplate = new SizeableEntityBuilder() {
            @Override
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
                //  so we apply half extent factor to our size.x here.
                float radius = size.x * primHE;
                // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
                //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
                // determine the equivalent bullet-compatible height parameter by explicitly scaling
                // the base mesh height and then subtracting the (scaled) end radii
                float height = primCapsuleHt * size.y - size.x * primHE - size.x * primHE;
                return load(model, rootNode, new btCapsuleShape(radius, height), size, mass, trans);
            }
        };
        cylinderTemplate = new SizeableEntityBuilder() {
            @Override
            // cylinder shape apparently allow both width (x) and height (y) to be specified
            public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                return load(model, rootNode, new btCylinderShape(size.cpy().scl(primHE)), size, mass, trans);
            }
        };




        /*
          each anonymous game object element will implement
           load(Vector3 trans, Vector3 size)
          which will in turn correctly call
           load(Model model, String nodeID, btCollisionShape shape, Vector3 trans, Vector3 size)
         */
        primitiveModels = new ArrayMap<String, EntityBuilder>(String.class, EntityBuilder.class);

        for (PrimitiveTypes p : PrimitiveTypes.values()) {
            // insert each template
            switch (p) {
                case SPHERE:
                    primitiveModels.put("sphere", new SizeableEntityBuilder() {
                        @Override
                        public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                            return load(model, rootNode, new btSphereShape(size.x * primHE), size, mass, trans);
                        }
                    });
                    break;
                case BOX:
                    primitiveModels.put("sphere", new SizeableEntityBuilder() {
                        @Override
                        public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                            return load(model, rootNode, new btBoxShape(size.cpy().scl(primHE)), size, mass, trans);
                        }
                    });
                    break;
                case CONE:
                    primitiveModels.put("cone", new SizeableEntityBuilder() {
                        @Override
                        public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                            return load(model, rootNode, new btConeShape(size.x * primHE, size.y), size, mass, trans);
                        }
                    });
                    break;
                case CAPSULE:
                    primitiveModels.put("capsule",new SizeableEntityBuilder() {
                        @Override
                        public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                            // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
                            //  so we apply half extent factor to our size.x here.
                            float radius = size.x * primHE;
                            // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
                            //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
                            // determine the equivalent bullet-compatible height parameter by explicitly scaling
                            // the base mesh height and then subtracting the (scaled) end radii
                            float height = primCapsuleHt * size.y - size.x * primHE - size.x * primHE;
                            return load(model, rootNode, new btCapsuleShape(radius, height), size, mass, trans);
                        }
                    });
                    break;
                case CYLINDER:
                    primitiveModels.put("cylinder",new SizeableEntityBuilder() {
                        @Override
                        // cylinder shape apparently allow both width (x) and height (y) to be specified
                        public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
                            return load(model, rootNode, new btCylinderShape(size.cpy().scl(primHE)), size, mass, trans);
                        }
                    });
                    break;
            }
        }
    }


    // needed for method override (make this class from an interface, derived GameObject?)
    // can't override declare static method in anonymous inner class
    public Entity create(Model model, String rootNode, float mass, Vector3 trans, Vector3 size) {
        return null;
    }

    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        Entity e;

        if (0 != mass)
            e = load(model, nodeID, size, mass, translation, shape);
        else
            e = load(model, nodeID, shape, translation, size);

        return e;
    }

    /*
    DO I NEED TO DISPOSE THOSE TEMPLATES ???????????????????????????
     */
    public static void dispose() {
/*        primitivesModel.dispose();*/
    }
}
