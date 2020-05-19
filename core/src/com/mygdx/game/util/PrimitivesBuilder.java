/*
 * Copyright (c) 2019 Glenn Neidermeier
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
package com.mygdx.game.util;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.Collision;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBvhTriangleMeshShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btCompoundShape;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btConvexHullShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.collision.btTriangleInfoMap;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.BulletWorld;
import com.mygdx.game.components.BulletComponent;
import com.mygdx.game.components.ModelComponent;

/**
 * Created by neiderm on 12/18/17.
 */

public class PrimitivesBuilder /* implements Disposable */ {

    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    protected static final float DIM_UNIT = 1.0f;
    protected static final float DIM_HE = 1f / 2f; // primitives half extent constant
    protected static final float DIM_CAPS_HT = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)

    private static Array<btCollisionShape> savedShapeRefs = new Array<btCollisionShape>();

    public static Model model;

    /* instances only access the protected reference to the model */
//    private PrimitivesBuilder() { }

    public static Model getModel() {
        return model;
    }

    /* one instance of the primitives model is allowed to persist for the entire app lifetime */
    public static void init() {

        final ModelBuilder mb = new ModelBuilder();
        long attributes =
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

// hackaround to make sure attributes are consistent between "simple" (color only) vs textured shapes see below
// null pointer exception thrown in render shadow batch due to texture attribute but invalid texture
// didn't exactly pin down what was happening, must be  a race condition lets the material work most of the time, not much to go on ...
//  https://github.com/libgdx/libgdx/issues/4166
        attributes |= VertexAttributes.Usage.TextureCoordinates;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture tex = new Texture(pixmap);

        mb.begin();

        mb.node().id = "sphere";
        mb.part("sphere", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.GREEN), TextureAttribute.createDiffuse(tex))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.BLUE), TextureAttribute.createDiffuse(tex))).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW), TextureAttribute.createDiffuse(tex))).cone(1f, 1f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.CYAN), TextureAttribute.createDiffuse(tex))).capsule(1f * DIM_HE, DIM_CAPS_HT, 10); // note radius and height vs. bullet
        mb.node().id = "cylinder";
        mb.part("cylinder", GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA), TextureAttribute.createDiffuse(tex))).cylinder(1f, 1f, 1f, 10);

        /*
         * images used for texture test objects, which are not normally enabled so these extra PNG files bring some
         * bloatware to mobile device, the main thing is to keep a cap on number of textures loaded into the rendering context
         */
        tex = new Texture(Gdx.files.internal("data/crate.png"), true);
        mb.node().id = "boxTex";
        mb.part("box", GL20.GL_TRIANGLES, attributes,
                new Material(TextureAttribute.createDiffuse(tex))).box(1f, 1f, 1f);

        tex = new Texture(Gdx.files.internal("data/day.png"), true);
        mb.node().id = "sphereTex";
        mb.part("sphere", GL20.GL_TRIANGLES, attributes,
                new Material(TextureAttribute.createDiffuse(tex))).sphere(1f, 1f, 1f, 10, 10);

        model = mb.end();
    }

    private static btCollisionShape saveShapeRef(btCollisionShape shape) {
        savedShapeRefs.add(shape);
        return shape;
    }

    /*
     * workaround for - shapes that are estranged from an owning entity ? (exploding model?)
     * - single instance of collision shape to be shared between multiple (same size/scale) entity/geometries?
     *
     * THIS IS NOT DONE IN DISPOSE because dispose trashses the entire Model which is presently
     *   persists entire app lifecycle  so that those meshses are always available for e.g. lo-level
     *   UI work etc.   This maybe rethought.
     */
    public static void clearShapeRefs() {
        int n = 0;
        for (btCollisionShape shape : savedShapeRefs) {
            n += 1;

            if (null != shape) {
                shape.dispose();
            }
        }
        Gdx.app.log("Primtive:clearShapeRefs", "Removed shapes ct = " + n);
    }

    /*
    Generate bullet shapes by applying the same scale/size as shall be applied to the vertices of the instance mesh.
    primitive meshes should use unit value (1.0) for the extent dimensions, thus those base dimensions don't have to be multiplied in explicitly in the shape sizing calculation below.
    In some cases we have to take special care as bullet shapes don't all parameterize same way as gdx model primitives.
    Constant "DIM_HE" (primitives-half-extent) is used interchangeably to compute radius from size.x, as well as half extents where needed.
    */
    public static btCollisionShape getShape(final String objectName, Vector3 size) {

        if (null == objectName) {
            return null; // sorry charlie
        }

        if (null == size) {
            size = new Vector3(1, 1, 1);
        }

        btCollisionShape shape = null;

        if (objectName.contains("box")) {
// bulletshape given in file but get box builder is tied to it already
            shape = new btBoxShape(size.cpy().scl(DIM_HE));

        } else if (objectName.contains("sphere")) {
// bulletshape given in file but get Sphere builder is tied to it already
            shape = new btSphereShape(size.x * DIM_HE);

        } else if (objectName.contains("cylinder")) {
            shape = new btCylinderShape(size.cpy().scl(DIM_HE));

        } else if (objectName.contains("capsule")) {
            // btcapsuleShape() takes actual radius parameter (unlike cone/cylinder which use width+depth)
            //  so we apply half extent factor to our size.x here.
            float radius = size.x * DIM_HE;
            // btcapsuleShape total height is height+2*radius (unlike gdx capsule mesh where height specifies TOTAL height!
            //  (http://bulletphysics.org/Bullet/BulletFull/classbtCapsuleShape.html#details)
            // determine the equivalent bullet-compatible height parameter by explicitly scaling
            // the base mesh height and then subtracting the (scaled) end radii
            float height = DIM_CAPS_HT * size.y - size.x * DIM_HE - size.x * DIM_HE;

            shape = new btCapsuleShape(radius, height);

        } else if (objectName.contains("cone")) {

            shape = new btConeShape(size.x * DIM_HE, size.y);
        } else {
            Gdx.app.log("Prim", "object name not found");
        }

        // if object name doesn't match then ... no shape
        if (null == shape) {
            return null;
        }

        return saveShapeRef(shape);
    }

    /*
     *  re "obtainStaticNodeShape()"
     *   https://github.com/libgdx/libgdx/wiki/Bullet-Wrapper---Using-models
     *  "collision shape will share the same data (vertices) as the model"
     *
     *  need to look at this comment again ? ...
     *   "in some situations having issues (works only if single node in model, and it has no local translation - see code in Bullet.java)"
     */
    public static btCollisionShape getShape(String shapeName, Vector3 dimensions, Node node) {

        btCollisionShape shape = null;

        if (null == node) {
            Gdx.app.log("Pblder", "getShape() null == node");
            // return   .... probably
        }

        if (null == dimensions)
            dimensions = new Vector3(1, 1, 1);

        if (shapeName.equals("convexHullShape")) {

//                if (null != node) { // assert
            shape = getShape(node); // saves the shape ref, shouldn't hurt anything if gets saved again

            if (null == shape) {
                Gdx.app.log("Pblder", "null == shape shapeName ==   \"" + "\" )");
            } else {
                Gdx.app.log("Pblder", "btConvexHullShape getNumPoints"
                        + ((btConvexHullShape) shape).getNumPoints());
            }

        } else if (shapeName.equals("triangleMeshShape")) {

            btBvhTriangleMeshShape trimeshShape =
                    (btBvhTriangleMeshShape) Bullet.obtainStaticNodeShape(node, false);

            shape = trimeshShape;

            //  btTriangleInfoMap will need to be disposed (and reference kept other wise GC will eat them!
            if (null != trimeshShape) {
                btTriangleInfoMap tim = new btTriangleInfoMap();
                Collision.btGenerateInternalEdgeInfo(trimeshShape, tim);
                BulletWorld.getInstance().addTriangleInfoMap(tim);
            } // else ... error in model/mesh?
        } else if (shapeName.equals("btBoxShape")) {

            shape = new btBoxShape(dimensions.scl(0.5f));
        }

        if (null == shape) { // default

            shape = new btSphereShape(dimensions.scl(0.5f).x);
        }

        return saveShapeRef(shape);
    }

    private static btCollisionShape getShape(Node node) {

        btCollisionShape shape = null;

        if (null != node) {
            if (node.parts.size > 0) {
                shape = MeshHelper.createConvexHullShape(node.parts.get(0).meshPart);
/*
 there is some problem here  with parent+child[n] models (commanche, military jeeP) ... but only on "shootme" test screen ?? !!!!!!
                shape = MeshHelper.createConvexHullShape(node.parts.get(0).meshPart.mesh);
                */
            }
        }

        return saveShapeRef(shape);
    }

    private static btCollisionShape getShape(Mesh mesh) {

        btCollisionShape shape = null;

        if (null != mesh) {
            shape = MeshHelper.createConvexHullShape(mesh);
        }

        return saveShapeRef(shape);
    }

    public static btCollisionShape getsingleMeshShape(Model model) {

        btCollisionShape shape;

        Mesh mesh = singleMesh(model);

        shape = getShape(mesh);

        if (null != mesh) {
            mesh.dispose();
        }

        return shape;
    }

    /*
recursively get a flat array of node  from the model
 */
    public static void getNodeArray(Array<Node> srcNodeArray, Array<Node> destNodeArray) {

        for (Node childNode : srcNodeArray) {
            // protect for non-graphical nodes in models (they should not be counted in index of child shapes)
            if (childNode.parts.size > 0) {

                destNodeArray.add(childNode);
            }

            if (childNode.hasChildren()) {
                getNodeArray((Array<Node>) childNode.getChildren(), destNodeArray);
            }
        }
    }

    public static int getNodeIndex(Array<Node> srcNodeArray, String strMdlNode) {

        int rVal = -1;

        // "unroll" the nodes list so that the index to the bullet child shape will be consisten
        Array<Node> nodeFlatArray = new Array<Node>();
        getNodeArray(srcNodeArray, nodeFlatArray);

        int index = 0;

        for (Node node : nodeFlatArray) {
            if (node.id.equals(strMdlNode)) {
                rVal = index;
                break;
            }
            index += 1;
        }

        return rVal;
    }

    public static btCollisionShape getCompShape(Model model) {

        btCollisionShape compShape = getCompShape(new btCompoundShape(), model.nodes);
        return saveShapeRef(compShape); // comp shapes have to be disposed as well
    }

    private static btCollisionShape getCompShape(btCompoundShape compoundShape, Array<Node> nodeArray) {

        for (Node node : nodeArray) {
            // adds a convex hull shape for each child - child shapes added in order of nodes, so setting the
// shape user index isn't absolutely necessary  - but set the index anyway just because ;)
            if (node.parts.size > 0) { // avoid non-graphic nodes (lamps etc)

                btCollisionShape comp = PrimitivesBuilder.getShape(node);

                if (null != comp) {
                    // buildChildNodes() can follow the same (recursive as necessary) order of iterating the nodes, so by
                    // setting this index can be checked to assert that the order is matched
                    comp.setUserIndex(compoundShape.getNumChildShapes());
                    compoundShape.addChildShape(new Matrix4(node.localTransform), comp);
                }
            }
//  recursive
            if (node.hasChildren()) {
                nodeArray = (Array<Node>) node.getChildren();
                getCompShape(compoundShape, nodeArray);
            }
        }

        return compoundShape;//saveShapeRef(compoundShape); // comp shapes have to be disposed as well
    }

    /*
     * combine nodes into single mesh for generating convex hull shape
     */
    private static Mesh singleMesh(Model model) {

        // "demodularize" model - combine modelParts into single Node for generating the physics shape
        // (with a "little" work - multiple renderable instances per model component -  the model could remain "modular" allowing e.g. spinny bits on rigs)
        MeshBuilder meshBuilder = new MeshBuilder();
        meshBuilder.begin(model.meshParts.get(0).mesh.getVertexAttributes(), GL20.GL_TRIANGLES);

        for (Node node : model.nodes) {

            if (node.parts.size > 0) {

                MeshPart meshPart = node.parts.get(0).meshPart;
                meshBuilder.setVertexTransform(node.localTransform); // apply node transformation
                meshBuilder.addMesh(meshPart);
            }
        }

        return meshBuilder.end();
    }


    /*
     *  test objects only, not part of Game Object loading stack
     */
    public static Entity load(
            Model model, String nodeID, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        ModelInstance instance = new ModelInstance(model, nodeID);

        return load(instance, shape, size, mass, translation);
    }

    /*
     *  used by redux screen
     */
    public static Entity load(
            ModelInstance instance, btCollisionShape shape, Vector3 size, float mass, Vector3 translation) {

        Entity e = new Entity();
        e.add(new ModelComponent(instance));

        if (null != size) {
            if (instance.nodes.size > 0) {
                instance.nodes.get(0).scale.set(size);
            }
            instance.calculateTransforms();
        }
        // leave translation null if using translation from the model layout
        if (null != translation) {
            instance.transform.trn(translation);
        }

//        if (null != shape)
        {
            BulletComponent bc = new BulletComponent(shape, instance.transform, mass);
            e.add(bc);
        }
        return e;
    }


    public static void dispose() {
        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed. Therefor, the Model must outlive all its ModelInstances
//  Disposing the primitivesModel will automatically make all instances invalid!
        model.dispose();
        model = null;
    }
}
