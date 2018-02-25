
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
    private int strokeThickness = 1;
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;

    // Canvas
    public List<CanvasShape> canvasShapes = new ArrayList<>();
    public int canvasShapesSize = 0;
    public Point clickBegin, clickEnd;
    public static class CanvasShape {
        public List<Point> freeHandPoints = null;
        public Shape shape = null;
        public drawingModeType drawingMode;
        public Color fillColor;
        public Color strokeColor;
        public int strokeWidth;
        public boolean selected = false;

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
