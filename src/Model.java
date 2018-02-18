
import java.util.*;

public class Model {
    /** The observers that are watching this model for changes. */
    private List<Observer> observers;

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
