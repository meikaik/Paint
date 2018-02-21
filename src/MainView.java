
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

public class MainView extends JFrame implements Observer {

    private Model model;

    /**
     * Create a new View.
     */
    public MainView(Model model) {
        // Set up the window.
        setTitle("CS 349 W18 A2");
        setMinimumSize(new Dimension(128, 128));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createMenuBar();
        createToolBar();

        // Hook up this observer so that it will be notified when the model
        // changes.
        this.model = model;
        model.addObserver(this);

        setVisible(true);
    }
    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.
        System.out.println("Model changed!");
    }

    private void createMenuBar() {
        JMenuBar menubar=new JMenuBar();
        JMenu file = new JMenu("File");
        JMenu edit = new JMenu("Edit");
        JMenu format = new JMenu("Format");
        menubar.add(file);
        menubar.add(edit);
        menubar.add(format);
        setJMenuBar(menubar);
    }

    private void createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        ImageIcon cursor = new ImageIcon("src/icons/cursor.png");
        ImageIcon paintBrush = new ImageIcon("src/icons/paintbrush.png");
        JToggleButton cursorButton = new JToggleButton("Select", cursor, true);
        JToggleButton paintBrushButton = new JToggleButton("Draw", paintBrush, false);

        String[] drawingModeValues = { "Freeform line", "Straight line", "Rectangle", "Ellipse" };
        JComboBox drawingModes = new JComboBox(drawingModeValues);

        String[] strokeThickness = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px" };
        JComboBox strokes = new JComboBox(strokeThickness);

        JButton fillColor = new JButton("Fill Colour");
        JButton strokeColor = new JButton("Stroke Colour");

        toolbar.add(cursorButton);
        toolbar.add(paintBrushButton);
        toolbar.add(drawingModes);
        toolbar.add(strokes);
        toolbar.add(fillColor);
        toolbar.add(strokeColor);
        add(toolbar, BorderLayout.NORTH);
    }
}
