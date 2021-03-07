/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */
package com.almasb.fxgl.physics;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.entity.components.BoundingBoxComponent;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.physics.box2d.collision.shapes.Shape;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

import java.io.Serializable;

import static com.almasb.fxgl.core.math.FXGLMath.cosDegF;
import static com.almasb.fxgl.core.math.FXGLMath.sinDegF;

/**
 * A bounding collision box.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public final class HitBox implements Serializable {

    /**
     * Name of this hit box.
     */
    private String name;

    /**
     * Shape of this hit box.
     */
    private BoundingShape shape;

    /**
     * Bounding box (computed from shape) of this hit box.
     */
    private Bounds bounds;

    /**
     * Axes: 1 formed from the transform angle and its +90 deg perpendicular.
     */
    Vec2[] axes = new Vec2[2];

    /**
     * Corner points top-left, top-right, bot-right, bot-left.
     */
    Vec2[] corners = new Vec2[4];

    /**
     * Creates a hit box with the given shape.
     * The name of the {@code {@link HitBox}} will be auto generated
     * based upon the {@code hashCode} of the given {@code {@link BoundingShape}}.
     *
     * @param shape bounding shape
     */
    public HitBox(BoundingShape shape) {
        this(String.valueOf(shape.hashCode()), shape);
    }

    /**
     * Creates a hit box with the given local origin and shape.
     * The name of the {@code {@link HitBox}} will be auto generated
     * based upon the {@code hashCode} of the given {@code {@link BoundingShape}}.
     *
     * @param localOrigin origin of hit box
     * @param shape bounding shape
     */
    public HitBox(Point2D localOrigin, BoundingShape shape){
        this(String.valueOf(shape.hashCode()), localOrigin, shape);
    }

    /**
     * Creates a hit box with given name and shape.
     * Local origin is set to default (0, 0).
     *
     * @param name name of the hit box
     * @param shape bounding shape
     */
    public HitBox(String name, BoundingShape shape) {
        this(name, Point2D.ZERO, shape);
    }

    /**
     * Creates a hit box with given name, local origin (top-left) and shape.
     *
     * @param name name of the hit box
     * @param localOrigin origin of hit box
     * @param shape bounding shape
     */
    public HitBox(String name, Point2D localOrigin, BoundingShape shape) {
        this.name = name;
        this.shape = shape;
        this.bounds = new BoundingBox(localOrigin.getX(), localOrigin.getY(),
                shape.getSize().getWidth(), shape.getSize().getHeight());

        axes[0] = new Vec2();
        axes[1] = new Vec2();

        for (int i = 0; i < 4; i++) {
            corners[i] = new Vec2();
        }
    }
//
//    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//        out.writeObject(name);
//
//        out.writeDouble(bounds.getMinX());
//        out.writeDouble(bounds.getMinY());
//        out.writeDouble(bounds.getWidth());
//        out.writeDouble(bounds.getHeight());
//
//        out.writeDouble(shape.getSize().getWidth());
//        out.writeDouble(shape.getSize().getHeight());
//
//        out.writeObject(shape.getType$fxgl_entity());
//
//        if (shape.getType$fxgl_entity() == ShapeType.CHAIN) {
//            Point2D[] points = (Point2D[]) shape.data;
//            out.writeInt(points.length);
//
//            for (Point2D p : points) {
//                out.writeDouble(p.getX());
//                out.writeDouble(p.getY());
//            }
//        }
//    }
//
//    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
//        name = (String) in.readObject();
//
//        bounds = new BoundingBox(in.readDouble(), in.readDouble(), in.readDouble(), in.readDouble());
//
//        Dimension2D size = new Dimension2D(in.readDouble(), in.readDouble());
//
//        ShapeType type = (ShapeType) in.readObject();
//
//        switch (type) {
//
//            case CIRCLE:
//                shape = BoundingShape.circle(size.getWidth() / 2);
//                break;
//
//            case POLYGON:
//                shape = BoundingShape.box(size.getWidth(), size.getHeight());
//                break;
//
//            case CHAIN:
//                int length = in.readInt();
//
//                Point2D[] points = new Point2D[length];
//                for (int i = 0; i < length; i++) {
//                    points[i] = new Point2D(in.readDouble(), in.readDouble());
//                }
//                shape = BoundingShape.chain(points);
//                break;
//
//            default:
//                throw new IllegalArgumentException("Unknown shape type");
//        }
//    }

    /**
     * @return bounds
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * @return hit box shape
     */
    public BoundingShape getShape() {
        return shape;
    }

    /**
     * @return min x
     */
    public double getMinX() {
        return bounds.getMinX();
    }

    /**
     * @return min y
     */
    public double getMinY() {
        return bounds.getMinY();
    }

    /**
     * @return maxX of internal bounds (x + width)
     */
    public double getMaxX() {
        return bounds.getMaxX();
    }

    /**
     * @return maxY of internal bounds (y + height)
     */
    public double getMaxY() {
        return bounds.getMaxY();
    }

    /**
     * @return width of this hit box
     */
    public double getWidth() {
        return getMaxXWorld() - getMinXWorld();
    }

    /**
     * @return height of this hit box
     */
    public double getHeight() {
        return getMaxYWorld() - getMinYWorld();
    }

    /**
     * @return hit box name
     */
    public String getName() {
        return name;
    }

    /*
     * These are lazily evaluated and used for caching
     * bbox data in the world coord space.
     * Otherwise these would have to be recomputed every time
     * this hit box is used for collision detection.
     */
    private transient DoubleProperty minXWorld = new SimpleDoubleProperty();
    private transient DoubleProperty maxXWorld = new SimpleDoubleProperty();
    private transient DoubleProperty minYWorld = new SimpleDoubleProperty();
    private transient DoubleProperty maxYWorld = new SimpleDoubleProperty();

    /**
     * Bind to x property of entity.
     */
    public void bindXY(TransformComponent transform) {

        // compute local min and max, then convert to world coord
        // var minXWorldNew1 = center1.x - (center1.x - box1.minX) * transform1.scaleX + transform1.x;
        // var maxXWorldNew1 = center1.x - (center1.x - box1.maxX) * transform1.scaleX + transform1.x;

        var x1 = Bindings.createDoubleBinding(() ->
                        transform.getScaleOrigin().getX() - (transform.getScaleOrigin().getX() - getMinX()) * transform.getScaleX() + transform.getX(),
                // dependencies
                transform.scaleOriginXProperty(), transform.scaleXProperty(), transform.xProperty()
        );

        var x2 = Bindings.createDoubleBinding(() ->
                        transform.getScaleOrigin().getX() - (transform.getScaleOrigin().getX() - getMaxX()) * transform.getScaleX() + transform.getX(),
                // dependencies
                transform.scaleOriginXProperty(), transform.scaleXProperty(), transform.xProperty()
        );

        var y1 = Bindings.createDoubleBinding(() ->
                        transform.getScaleOrigin().getY() - (transform.getScaleOrigin().getY() - getMinY()) * transform.getScaleY() + transform.getY(),
                // dependencies
                transform.scaleOriginYProperty(), transform.scaleYProperty(), transform.yProperty()
        );

        var y2 = Bindings.createDoubleBinding(() ->
                        transform.getScaleOrigin().getY() - (transform.getScaleOrigin().getY() - getMaxY()) * transform.getScaleY() + transform.getY(),
                // dependencies
                transform.scaleOriginYProperty(), transform.scaleYProperty(), transform.yProperty()
        );

        var isX1GreaterX2 = x1.greaterThan(x2);
        var isY1GreaterY2 = y1.greaterThan(y2);

        minXWorld.bind(
                Bindings.when(isX1GreaterX2).then(x2).otherwise(x1)
        );

        maxXWorld.bind(
                Bindings.when(isX1GreaterX2).then(x1).otherwise(x2)
        );

        minYWorld.bind(
                Bindings.when(isY1GreaterY2).then(y2).otherwise(y1)
        );

        maxYWorld.bind(
                Bindings.when(isY1GreaterY2).then(y1).otherwise(y2)
        );
    }

    /**
     * Unbind the hit box.
     */
    public void unbind() {
        minXWorld.unbind();
        maxXWorld.unbind();
        minYWorld.unbind();
        maxYWorld.unbind();
    }

    public double getMinXWorld() {
        return minXWorld.get();
    }

    public double getMaxXWorld() {
        return maxXWorld.get();
    }

    public double getMinYWorld() {
        return minYWorld.get();
    }

    public double getMaxYWorld() {
        return maxYWorld.get();
    }

    public Point2D getCenterWorld() {
        return new Point2D((getMinXWorld() + getMaxXWorld()) / 2, (getMinYWorld() + getMaxYWorld()) / 2);
    }

    /**
     * @return center point of this hit box, local to 0,0 (top,left) of the entity
     */
    public Point2D centerLocal() {
        return new Point2D((bounds.getMinX() + bounds.getMaxX()) / 2,
                (bounds.getMinY() + bounds.getMaxY()) / 2);
    }

    /**
     * @param x position x of entity
     * @param y position y of entity
     * @return center point of this hit box in world coordinates
     */
    public Point2D centerWorld(double x, double y) {
        return centerLocal().add(x, y);
    }

    public Shape toBox2DShape(BoundingBoxComponent bbox, PhysicsUnitConverter converter) {
        return shape.toBox2DShape(this, bbox, converter);
    }

    // new physics below

    float fastMinX;
    float fastMinY;
    float fastMaxX;
    float fastMaxY;

    public float getFastMinX() {
        return fastMinX;
    }

    public float getFastMinY() {
        return fastMinY;
    }

    public float getFastMaxX() {
        return fastMaxX;
    }

    public float getFastMaxY() {
        return fastMaxY;
    }

    SAT.MinMax axis1MinMax = new SAT.MinMax();
    SAT.MinMax axis2MinMax = new SAT.MinMax();

    public void applyTransform(TransformComponent transform) {
        // scale, translate

        double x1 = transform.getScaleOrigin().getX() - (transform.getScaleOrigin().getX() - getMinX()) * transform.getScaleX() + transform.getX();
        double x2 = transform.getScaleOrigin().getX() - (transform.getScaleOrigin().getX() - getMaxX()) * transform.getScaleX() + transform.getX();

        double y1 = transform.getScaleOrigin().getY() - (transform.getScaleOrigin().getY() - getMinY()) * transform.getScaleY() + transform.getY();
        double y2 = transform.getScaleOrigin().getY() - (transform.getScaleOrigin().getY() - getMaxY()) * transform.getScaleY() + transform.getY();

        fastMinX = (float) Math.min(x1, x2);
        fastMinY = (float) Math.min(y1, y2);

        fastMaxX = (float) Math.max(x1, x2);
        fastMaxY = (float) Math.max(y1, y2);

        double angle = transform.getAngle();

        float cos = cosDegF(angle);
        float sin = sinDegF(angle);

        axes[0].set(cos, sin).normalizeLocal();
        axes[1].set(cosDegF(angle + 90), sinDegF(angle + 90)).normalizeLocal();

        // top left
        corners[0].set(fastMinX, fastMinY);

        // top right
        corners[1].set(fastMaxX, fastMinY);

        // bot right
        corners[2].set(fastMaxX, fastMaxY);

        // bot left
        corners[3].set(fastMinX, fastMaxY);

        // if the angle is 0 then rotation and its origin do not matter
        if (angle != 0.0) {
            // rotate
            var origin = transform.getRotationOrigin();

            // origin in world coord
            double originX = origin.getX() + transform.getX();
            double originY = origin.getY() + transform.getY();

            // min, max are already scaled, so we just need to rotate them
            for (Vec2 v : corners) {
                v.subLocal(originX, originY);
                v.set(v.x * cos - v.y * sin, v.x * sin + v.y * cos);
                v.addLocal(originX, originY);
            }
        }

        SAT.computeMinMax(corners, axes[0], axis1MinMax);
        SAT.computeMinMax(corners, axes[1], axis2MinMax);
    }

    @Override
    public String toString() {
        return "HitBox(" + name + "," + shape + ")";
    }
}
