
import java.util.*;
import java.awt.Color;

public class Model {

    public enum drawingModeType {
        FREEFORM, STRAIGHT, RECTANGLE, ELLIPSE
    }

    /** The observers that are watching this model for changes. */
    private List<Observer> observers;

    public Boolean drawMode = false; // true for Draw mode, false for Select mode
    public drawingModeType drawingMode = drawingModeType.FREEFORM; // any of FREEFORM, STRAIGHT, RECTANGLE, ELLIPSE
    public int strokeThickness = 1;
    public Color fillColor = Color.WHITE;
    public Color strokeColor = Color.BLACK;

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
