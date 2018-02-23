
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.event.*;

public class MainView extends JFrame implements Observer {

    private Model model;
    private boolean toggled = false;
    private BufferedImage fillColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
    private BufferedImage strokeColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);

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

        // File Menu
        JMenu file = createFileMenu();
        // Edit Menu
        JMenu edit = createEditMenu();
        // Format Menu
        JMenu format = createFormatMenu();

        menubar.add(file);
        menubar.add(edit);
        menubar.add(format);
        setJMenuBar(menubar);
    }

    private JMenu createFileMenu() {
        JMenu file = new JMenu("File");
        JMenuItem new1 = new JMenuItem("New");
        new1.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        JMenuItem exit = new JMenuItem("Exit");
        exit.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        file.add(new1);
        file.add(exit);
        return file;
    }

    private JMenu createEditMenu() {
        JMenu edit = new JMenu("Edit");
        ButtonGroup radiogroup = new ButtonGroup();
        JRadioButtonMenuItem selectionMode = new JRadioButtonMenuItem("Selection Mode");
        selectionMode.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        radiogroup.add(selectionMode);

        JRadioButtonMenuItem drawingMode = new JRadioButtonMenuItem("Drawing Mode", true);
        drawingMode.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        radiogroup.add(drawingMode);

        JMenuItem deleteShape = new JMenuItem("Delete Shape");
        deleteShape.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0));
        JMenuItem transformShape = new JMenuItem("Transform Shape");
        transformShape.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_T, ActionEvent.CTRL_MASK));

        selectionMode.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (model.getDrawMode()) {
                    model.setDrawMode();
                    System.out.println("Select mode");
                    deleteShape.setEnabled(true);
                    transformShape.setEnabled(true);
                }
            }
        });

        drawingMode.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.getDrawMode()) {
                    model.setDrawMode();
                    System.out.println("Draw mode");
                    deleteShape.setEnabled(false);
                    transformShape.setEnabled(false);

                }
            }
        });

        edit.add(selectionMode);
        edit.add(drawingMode);
        edit.addSeparator();
        edit.add(deleteShape);
        edit.add(transformShape);
        return edit;
    }

    private JMenu createFormatMenu() {
        JMenu format = new JMenu("Format");
        JMenu strokeWidth = addStrokeWidthMenuItem();
        JMenuItem fillColor = addFillColorMenuItem();
        JMenuItem strokeColor = addStrokeColorMenuItem();

        format.add(strokeWidth);
        format.add(fillColor);
        format.add(strokeColor);
        return format;
    }

    private JMenu addStrokeWidthMenuItem() {
        JMenu strokeWidth = new JMenu("Stroke Width");
        String[] strokeThickness = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px" };
        for (String stroke : strokeThickness) {
            strokeWidth.add(stroke);
        }
        return strokeWidth;
    }

    private JMenuItem addFillColorMenuItem() {
        ImageIcon fill = changeColor(fillColorImage,  model.getFillColor());
        JMenuItem fillColor = new JMenuItem("Fill Colour...", fill);
        fillColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Fill Colour", model.getFillColor());
                if (newColor != null) {
                    model.setFillColor(newColor);
                }
                System.out.println("New fill color: " + model.getFillColor());
                fillColor.setIcon(changeColor(fillColorImage, model.getFillColor()));
            }
        });
        return fillColor;
    }

    private JMenuItem addStrokeColorMenuItem() {
        ImageIcon stroke = changeColor(strokeColorImage,  model.getStrokeColor());
        JMenuItem strokeColor = new JMenuItem("Stroke Colour...", stroke);
        strokeColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Stroke Colour", model.getStrokeColor());
                if (newColor != null) {
                    model.setStrokeColor(newColor);
                }
                System.out.println("New stroke color: " + model.getStrokeColor());
                strokeColor.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));
            }
        });
        return strokeColor;
    }

    private void createToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        ImageIcon cursor = new ImageIcon("src/icons/cursor.png");
        ImageIcon paintBrush = new ImageIcon("src/icons/paintbrush.png");
        JButton selectButton = new JButton("Select", cursor);
        JButton drawButton = new JButton("Draw", paintBrush);
        drawButton.setSelected(true);

        selectButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                if (model.getDrawMode()) {
                    model.setDrawMode();
                    System.out.println("Select mode");
                    selectButton.setSelected(true);
                    drawButton.setSelected(false);

                }
            }
        });

        drawButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.getDrawMode()) {
                    model.setDrawMode();
                    System.out.println("Draw mode");
                    selectButton.setSelected(false);
                    drawButton.setSelected(true);

                }
            }
        });

        JComboBox drawingModes = addDrawingModesDropdown();
        JComboBox strokes = addStrokesDropdown();
        JButton fillColor = addFillColorButton();
        JButton strokeColor = addStrokeColorButton();

        toolbar.add(selectButton);
        toolbar.add(drawButton);
        toolbar.add(drawingModes);
        toolbar.add(strokes);
        toolbar.add(fillColor);
        toolbar.add(strokeColor);
        add(toolbar, BorderLayout.NORTH);
    }

    private JComboBox addDrawingModesDropdown() {
        String[] drawingModeValues = { "Freeform line", "Straight line", "Rectangle", "Ellipse" };
        JComboBox drawingModes = new JComboBox(drawingModeValues);
        drawingModes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String selectedDrawingMode = (String) drawingModes.getSelectedItem();
                switch (selectedDrawingMode) {
                    case "Freeform line":
                        model.setDrawingMode(Model.drawingModeType.FREEFORM);
                        break;
                    case "Straight line":
                        model.setDrawingMode(Model.drawingModeType.STRAIGHT);
                        break;
                    case "Rectangle":
                        model.setDrawingMode(Model.drawingModeType.RECTANGLE);
                        break;
                    case "Ellipse":
                        model.setDrawingMode(Model.drawingModeType.ELLIPSE);
                        break;
                }
                System.out.println("Drawing mode is now: " + model.getDrawingMode());
            }
        });
        return drawingModes;
    }

    private JComboBox addStrokesDropdown() {
        String[] strokeThickness = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px" };
        JComboBox strokes = new JComboBox(strokeThickness);
        strokes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String selectedStrokeThickness = (String) strokes.getSelectedItem();
                if ("10px" == selectedStrokeThickness) {
                    model.setStrokeThickness(10);
                }
                else {
                    int val = Character.getNumericValue(selectedStrokeThickness.charAt(0));
                    model.setStrokeThickness(val);
                }
                System.out.println("Stroke thickness is now: " + model.getStrokeThickness());
            }
        });
        return strokes;
    }

    private JButton addFillColorButton() {
        ImageIcon fill = changeColor(fillColorImage,  model.getFillColor());
        JButton fillColor = new JButton("Fill Colour", fill);
        fillColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Fill Colour", model.getFillColor());
                if (newColor != null) {
                    model.setFillColor(newColor);
                }
                System.out.println("New fill color: " + model.getFillColor());
                fillColor.setIcon(changeColor(fillColorImage, model.getFillColor()));
            }
        });
        return fillColor;
    }

    private JButton addStrokeColorButton() {
        ImageIcon stroke = changeColor(strokeColorImage, model.getStrokeColor());
        JButton strokeColor = new JButton("Stroke Colour", stroke);
        strokeColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Stroke Colour", model.getStrokeColor());
                if (newColor != null) {
                    model.setStrokeColor(newColor);
                }
                System.out.println("New stroke color: " + model.getStrokeColor());
                strokeColor.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));
            }
        });
        return strokeColor;
    }

    private ImageIcon changeColor(BufferedImage image, Color color){
        Graphics2D graphics = image.createGraphics();
        graphics.setPaint(color);
        graphics.fillRect(0, 0, 16, 16);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}
