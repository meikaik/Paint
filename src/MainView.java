
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.event.*;

public class MainView extends JFrame implements Observer {

    private Model model;
    private boolean toggled = false;
    /**
     * Create a new View.
     */
    public MainView(Model model) {
        // Set up the window.
        setTitle("CS 349 W18 A2");
        setMinimumSize(new Dimension(128, 128));
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Hook up this observer so that it will be notified when the model
        // changes.
        this.model = model;
        model.addObserver(this);

        createMenuBar();
        createToolBar();

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
        JButton selectButton = new JButton("Select", cursor);
        JButton drawButton = new JButton("Draw", paintBrush);
        selectButton.setSelected(true);

//        WTF IS THE USE OF A BUTTONGROUP
//        ButtonGroup selectOrDraw = new ButtonGroup();
//        selectOrDraw.add(selectButton);
//        selectOrDraw.add(drawButton);

        selectButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                if (model.drawMode) {
                    model.drawMode = false;
                    System.out.println("Select mode");
                    selectButton.setSelected(true);
                    drawButton.setSelected(false);

                }
            }
        });
        drawButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.drawMode) {
                    model.drawMode = true;
                    System.out.println("Draw mode");
                    selectButton.setSelected(false);
                    drawButton.setSelected(true);

                }
            }
        });

        String[] drawingModeValues = { "Freeform line", "Straight line", "Rectangle", "Ellipse" };
        JComboBox drawingModes = new JComboBox(drawingModeValues);
        drawingModes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String selectedDrawingMode = (String) drawingModes.getSelectedItem();
                switch (selectedDrawingMode) {
                    case "Freeform line":
                        model.drawingMode = Model.drawingModeType.FREEFORM;
                        break;
                    case "Straight line":
                        model.drawingMode = Model.drawingModeType.STRAIGHT;
                        break;
                    case "Rectangle":
                        model.drawingMode = Model.drawingModeType.RECTANGLE;
                        break;
                    case "Ellipse":
                        model.drawingMode = Model.drawingModeType.ELLIPSE;
                        break;
                }
                System.out.println("Drawing mode is now: " + model.drawingMode);
            }
        });

        String[] strokeThickness = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px" };
        JComboBox strokes = new JComboBox(strokeThickness);
        strokes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String selectedStrokeThickness = (String) strokes.getSelectedItem();
                if ("10px" == selectedStrokeThickness) {
                    model.strokeThickness = 10;
                }
                else {
                    int val = Character.getNumericValue(selectedStrokeThickness.charAt(0));
                    model.strokeThickness = val;
                }
                System.out.println("Stroke thickness is now: " + model.strokeThickness);
            }
        });

        ImageIcon fill = createIcon(model.fillColor);
        JButton fillColor = new JButton("Fill Colour", fill);
        fillColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose a color", model.fillColor);
                if (newColor != null) {
                    model.fillColor = newColor;
                }
                System.out.println("New fill color: " + model.fillColor);
                fillColor.setIcon(createIcon(model.fillColor));
                toolbar.repaint();
            }
        });

        ImageIcon stroke = createIcon(model.strokeColor);
        JButton strokeColor = new JButton("Stroke Colour", stroke);
        strokeColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose a color", model.strokeColor);
                if (newColor != null) {
                    model.strokeColor = newColor;
                }
                System.out.println("New stroke color: " + model.strokeColor);
                strokeColor.setIcon(createIcon(model.strokeColor));
                toolbar.repaint();
            }
        });

        toolbar.add(selectButton);
        toolbar.add(drawButton);
        toolbar.add(drawingModes);
        toolbar.add(strokes);
        toolbar.add(fillColor);
        toolbar.add(strokeColor);
        add(toolbar, BorderLayout.NORTH);
    }

    public static ImageIcon createIcon(Color color){
        BufferedImage image = new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, 16, 16);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}
