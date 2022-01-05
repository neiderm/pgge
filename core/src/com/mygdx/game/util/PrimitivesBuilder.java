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
package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
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

/**
 * Created by neiderm on 12/18/17.
 */
public class PrimitivesBuilder /* implements Disposable */ {

    private static final String CLASS_STRING = "PrimitivesBuilder";
    // use unit (i.e. 1.0f) for all dimensions - primitive objects will have scale applied by load()
    private static final float DIM_UNIT = 1.0f;
    private static final float DIM_HE = 1.0f / 2.0f; // primitives half extent constant
    private static final float DIM_CAPS_HT = 1.0f + 0.5f + 0.5f; // define capsule height ala bullet (HeightTotal = H + 1/2R + 1/2R)

    private static Array<btCollisionShape> savedShapeRefs = new Array<>();
    private static Model model;

    private PrimitivesBuilder() { // MT
    }

    public static Model getModel() {
        return model;
    }

    /* one instance of the primitives model is allowed to persist for the entire app lifetime */
    public static void init() {

        final String SPHERE_STRING = "sphere";
        final String BOX_STRING = "box";
        final String CYLINDER_STRING = "cylinder";
        final String CONE_STRING = "cone";
        final String CAPSULE_STRING = "capsule";

        final ModelBuilder mb = new ModelBuilder();
        long attributes = VertexAttributes.Usage.Position |
                VertexAttributes.Usage.Normal |
                VertexAttributes.Usage.TextureCoordinates;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture tex = new Texture(pixmap);

        mb.begin();

        mb.node().id = SPHERE_STRING;
        mb.part(SPHERE_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.GREEN), TextureAttribute.createDiffuse(tex))).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = BOX_STRING;
        mb.part(BOX_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.BLUE), TextureAttribute.createDiffuse(tex))).box(1f, 1f, 1f);
        mb.node().id = CONE_STRING;
        mb.part(CONE_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.YELLOW), TextureAttribute.createDiffuse(tex))).cone(1f, 1f, 1f, 10);
        mb.node().id = CAPSULE_STRING;
        mb.part(CAPSULE_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.CYAN), TextureAttribute.createDiffuse(tex))).capsule(1f * DIM_HE, DIM_CAPS_HT, 10); // note radius and height vs. bullet
        mb.node().id = CYLINDER_STRING;
        mb.part(CYLINDER_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(ColorAttribute.createDiffuse(Color.MAGENTA), TextureAttribute.createDiffuse(tex))).cylinder(1f, 1f, 1f, 10);
        /*
         * todo: images used for texturing test objects, which are only for test screen i.el extra PNG files bring some
         * bloatware to mobile device, the main thing is to limit the number of textures loaded into the rendering context.
         */
        tex = new Texture(Gdx.files.internal("data/crate.png"), true);
        mb.node().id = "boxTex";
        mb.part(BOX_STRING, GL20.GL_TRIANGLES, attributes,
                new Material(TextureAttribute.createDiffuse(tex))).box(1f, 1f, 1f);

        tex = new Texture(Gdx.files.internal("data/day.png"), true);
        mb.node().id = "sphereTex";
        mb.part(SPHERE_STRING, GL20.GL_TRIANGLES, attributes,
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
        Gdx.app.log(CLASS_STRING, "Removed shapes ct = " + n);
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
            // bulletshape given in json file but get box builder is tied to it already
            shape = new btBoxShape(size.cpy().scl(DIM_HE));

        } else if (objectName.contains("sphere")) {
            // bulletshape given in json file but get Sphere builder is tied to it already
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
            Gdx.app.log(CLASS_STRING, "object name not found");
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

        btCollisionShape shape;

        if (null == node) {
            Gdx.app.log(CLASS_STRING, "getShape() null == node");
        }

        if (null == dimensions)
            dimensions = new Vector3(1, 1, 1);

        switch (shapeName) {
            case "btBoxShape":
                shape = new btBoxShape(dimensions.scl(0.5f));
                break;
            case "convexHullShape":
                shape = getShape(node); // saves the shape ref, shouldn't hurt anything if gets saved again

                if (null == shape) {
                    Gdx.app.log(CLASS_STRING, "null == shape shapeName ==   \"" + "\" )");
                } else {
                    Gdx.app.log(CLASS_STRING, "btConvexHullShape getNumPoints"
                            + ((btConvexHullShape) shape).getNumPoints());
                }
                break;
            case "triangleMeshShape":
                btBvhTriangleMeshShape trimeshShape = null;
                if (null != node) {
                    trimeshShape =
                            (btBvhTriangleMeshShape) Bullet.obtainStaticNodeShape(node, false);
                }
                shape = trimeshShape;
                //  btTriangleInfoMap will need to be disposed (and reference kept other wise GC will eat them!
                if (null != trimeshShape) {
                    btTriangleInfoMap tim = new btTriangleInfoMap();
                    Collision.btGenerateInternalEdgeInfo(trimeshShape, tim);
                    BulletWorld.getInstance().addTriangleInfoMap(tim);
                } // else ... error in model/mesh?
                break;
            case "btSphereShape":
            default: // null
                shape = new btSphereShape(dimensions.scl(0.5f).x);
                break;
        }
        return saveShapeRef(shape);
    }

    private static btCollisionShape getShape(Node node) {

        btCollisionShape shape = null;

        if (null != node && node.parts.size > 0) {
            shape = MeshHelper.createConvexHullShape(node.parts.get(0).meshPart);
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
     * Recursively get a flat array of node  from the model
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
        Array<Node> nodeFlatArray = new Array<>();
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
            // shape user index isn't absolutely necessary - but set the index anyway just because ;)
            if (node.parts.size > 0) { // avoid non-graphic nodes (lamps etc)

                btCollisionShape comp = PrimitivesBuilder.getShape(node);

                if (null != comp) {
                    // buildChildNodes() can follow the same (recursive as necessary) order of iterating the nodes, so by
                    // setting this index can be checked to assert that the order is matched
                    comp.setUserIndex(compoundShape.getNumChildShapes());
                    compoundShape.addChildShape(new Matrix4(node.localTransform), comp);
                }
            }
            // recursive
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

        // "de-modularize" model - combine modelParts into single Node for generating the physics shape
        // (with a "little" work - multiple renderable instances per model component -  the model
        // could remain "modular" allowing e.g. spinny bits on rigs)
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

    public static void dispose() {
        // The Model owns the meshes and textures, to dispose of these, the Model has to be disposed.
        // Therefore, the Model must outlive all its ModelInstances.
        //  Disposing the primitivesModel will automatically make all instances invalid!
        model.dispose();
        model = null;
    }
}
