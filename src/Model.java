
import java.awt.geom.AffineTransform;
import java.util.*;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Point;

public class Model {

    public enum drawingModeType {
        FREEFORM, STRAIGHT, RECTANGLE, ELLIPSE
    }

    /** The observers that are watching this model for changes. */
    private List<Observer> observers;

    // User selected menubar/toolbar
    private Boolean drawMode = true; // true for Draw mode, false for Select mode
    private drawingModeType drawingMode = drawingModeType.FREEFORM; // any of FREEFORM, STRAIGHT, RECTANGLE, ELLIPSE
    public int strokeThickness = 1;
    public Color fillColor = Color.WHITE;
    public Color strokeColor = Color.BLACK;
    private Boolean deleteTransformOverride = false; // false for no override, true for override make field unclickable

    // Canvas
    private List<CanvasShape> canvasShapes = new ArrayList<>();
    private int canvasShapesSize = 0;
    private Point clickBegin, clickEnd;

    // Create a CanvasShape "struct"
    public static class CanvasShape {
        public List<Point> freeHandPoints = null;
        public Shape shape = null;
        public drawingModeType drawingMode;
        public Color fillColor;
        public Color strokeColor;
        public int strokeWidth;
        public boolean selected = false;
        public int translateX = 0, translateY = 0, rotate = 0;
        public double scaleX = 1, scaleY = 1;
        public AffineTransform AT = null;

        public CanvasShape(Shape s, drawingModeType dm, Color fc, Color sc, int sw) {
            shape = s;
            drawingMode = dm;
            fillColor = fc;
            strokeColor = sc;
            strokeWidth = sw;

        }

        public CanvasShape(List<Point> points, drawingModeType dm, Color fc, Color sc, int sw) {
            freeHandPoints = points;
            drawingMode = dm;
            fillColor = fc;
            strokeColor = sc;
            strokeWidth = sw;
        }

        public Point getMidPoint() {
            if (shape != null) {
                return new Point(
                        (int) (shape.getBounds().getMinX() + shape.getBounds().getMaxX()) / 2,
                        (int) (shape.getBounds().getMinY() + shape.getBounds().getMaxY()) / 2);
            } else if (freeHandPoints != null) {
                int x1 = Integer.MAX_VALUE;
                int y1 = Integer.MAX_VALUE;
                int x2 = Integer.MIN_VALUE;
                int y2 = Integer.MIN_VALUE;

                for (int i = 0; i < freeHandPoints.size() - 1; i++) {
                    if (freeHandPoints.get(i).x < x1) {
                        x1 = freeHandPoints.get(i).x;
                    }
                    if (freeHandPoints.get(i).x > x2) {
                        x2 = freeHandPoints.get(i).x;
                    }
                    if (freeHandPoints.get(i).y < y1) {
                        y1 = freeHandPoints.get(i).y;
                    }
                    if (freeHandPoints.get(i).y > y2) {
                        y2 = freeHandPoints.get(i).y;
                    }
                }
                return new Point(
                        (x1 + x2) / 2, (y1 + y2) / 2);
            }
            return new Point(0,0);
        }
    }

    public List<CanvasShape> getCanvasShapes() {
        return canvasShapes;
    }

    public void addCanvasShape(CanvasShape cs) {
        canvasShapes.add(cs);
        notifyObservers();
    }

    public int getCanvasShapesSize() {
        return canvasShapesSize;
    }

    public void incrementCanvasShapesSize() {
        canvasShapesSize++;
        notifyObservers();
    }

    public void resetCanvasShapes() {
        canvasShapes = new ArrayList<>();
        canvasShapesSize = 0;
        notifyObservers();
    }

    public Point getClickBegin() {
        return clickBegin;
    }

    public void setClickBegin(Point p) {
        clickBegin = p;
        //notifyObservers();
    }

    public Point getClickEnd() {
        return clickEnd;
    }

    public void setClickEnd(Point p) {
        clickEnd = p;
        //notifyObservers();
    }

    public Boolean getDrawMode() {
        return drawMode;
    }

    public void setDrawMode() {
        drawMode = !drawMode;
        notifyObservers();
    }

    public drawingModeType getDrawingMode() {
        return drawingMode;
    }

    public void setDrawingMode(drawingModeType mode) {
        drawingMode = mode;
        notifyObservers();
    }

    public int getStrokeThickness() {
        return strokeThickness;
    }

    public void setStrokeThickness(int thickness) {
        strokeThickness = thickness;
        notifyObservers();
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        fillColor = color;
        notifyObservers();
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(Color color) {
        strokeColor = color;
        notifyObservers();
    }

    public Boolean getdeleteTransformOverride() {
        return deleteTransformOverride;
    }

    public void setDeleteTransformOverride(Boolean bool) {
        deleteTransformOverride = bool;
        notifyObservers();
    }

    /**
     * Create a new model.
     */
    public Model() {
        this.observers = new ArrayList<Observer>();
    }

    /**
     * Add an observer to be notified when this model changes.
     */
    public void addObserver(Observer observer) {
        this.observers.add(observer);
    }

    /**
     * Remove an observer from this model.
     */
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    /**
     * Notify all observers that the model has changed.
     */
    public void notifyObservers() {
        for (Observer observer: this.observers) {
            observer.update(this);
        }
    }
}
