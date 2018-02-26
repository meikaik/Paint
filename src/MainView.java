
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.*;


public class MainView extends JFrame implements Observer {

    private Model model;
    private BufferedImage fillColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
    private BufferedImage strokeColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);

    // Menubar
    private JRadioButtonMenuItem selectionMode;
    private JRadioButtonMenuItem drawingMode;
    private JMenuItem deleteShape;
    private JMenuItem transformShape;
    private JMenu strokeWidth;

    // Toolbar
    private JButton selectButton;
    private JButton drawButton;
    private JComboBox drawingModes;
    private JComboBox strokes;

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
        add(new Canvas(model), BorderLayout.CENTER);
        setVisible(true);
    }
    /**
     * Update with data from the model.
     */
    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.
        System.out.println("Model changed!");

        // Update draw modes
        System.out.println("Draw mode? " + model.getDrawMode());
        selectButton.setSelected(!model.getDrawMode());
        selectionMode.setSelected(!model.getDrawMode());
        drawButton.setSelected(model.getDrawMode());
        drawingMode.setSelected(model.getDrawMode());
        deleteShape.setEnabled(!model.getDrawMode());
        transformShape.setEnabled(!model.getDrawMode());

        // Update stroke width
        strokeWidth.getItem(model.getStrokeThickness() - 1).setSelected(true);
        strokes.setSelectedIndex(model.getStrokeThickness() - 1);

        // if in select mode, update the stroke width, stroke color, and fill of selected Shape
        if (!model.getDrawMode()) {
            for (Model.CanvasShape cs : model.canvasShapes) {
                if (cs.selected) {
                    cs.strokeWidth = model.getStrokeThickness();
                    cs.strokeColor = model.getStrokeColor();
                    cs.fillColor = model.getFillColor();
                }
            }
        }
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
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(new1);
        file.add(exit);
        return file;
    }

    private JMenu createEditMenu() {
        JMenu edit = new JMenu("Edit");
        ButtonGroup radiogroup = new ButtonGroup();
        selectionMode = new JRadioButtonMenuItem("Selection Mode");
        selectionMode.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        radiogroup.add(selectionMode);

        drawingMode = new JRadioButtonMenuItem("Drawing Mode", true);
        drawingMode.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        radiogroup.add(drawingMode);

        deleteShape = new JMenuItem("Delete Shape");
        deleteShape.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_DELETE, 0));
        deleteShape.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Model.CanvasShape cs : model.canvasShapes) {
                    if (cs.selected) {
                        cs.shape = null;
                        cs.freeHandPoints = null;
                        cs.selected = false;
                        // TODO: FIX THIS, we need the model to call notifyObservers() after we make cs.seleected = false
                        model.setDrawMode();
                    }
                }
            }
        });

        transformShape = new JMenuItem("Transform Shape");
        transformShape.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        deleteShape.setEnabled(false);
        transformShape.setEnabled(false);

        selectionMode.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (model.getDrawMode()) {
                    model.setDrawMode();
                }
            }
        });

        drawingMode.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.getDrawMode()) {
                    model.setDrawMode();
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
        strokeWidth = new JMenu("Stroke Width");
        String[] strokeThickness = { "1px", "2px", "3px", "4px", "5px", "6px", "7px", "8px", "9px", "10px" };
        ButtonGroup strokes = new ButtonGroup();

        ActionListener radioActionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AbstractButton button = (AbstractButton) e.getSource();
                updateStrokeThickness(button.getText());
            }
        };

        for (String stroke : strokeThickness) {
            JRadioButtonMenuItem temp = new JRadioButtonMenuItem(stroke);
            strokes.add(temp);
            strokeWidth.add(temp);
            temp.addActionListener(radioActionListener);
        }
        strokeWidth.getItem(0).setSelected(true);

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
        selectButton = new JButton("Select", cursor);
        drawButton = new JButton("Draw", paintBrush);
        drawButton.setSelected(true);

        selectButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e){
                if (model.getDrawMode()) {
                    model.setDrawMode();
                }
            }
        });

        drawButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.getDrawMode()) {
                    model.setDrawMode();
                }
            }
        });

        drawingModes = addDrawingModesDropdown();
        strokes = addStrokesDropdown();
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
        JComboBox strokes = new JComboBox<>(strokeThickness);
        strokes.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                // TODO: refactor
                String temp = (String) strokes.getSelectedItem();
                int tempp = 0;
                if (temp == "10px") {
                    tempp = 10;
                }
                else {
                    tempp = Character.getNumericValue(temp.charAt(0));
                }
                if ( model.getStrokeThickness() != tempp) {
                    updateStrokeThickness(temp);
                }
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

    private void updateStrokeThickness(String selectedStrokeThickness) {
        if ("10px" == selectedStrokeThickness) {
            model.setStrokeThickness(10);
        }
        else {
            int val = Character.getNumericValue(selectedStrokeThickness.charAt(0));
            model.setStrokeThickness(val);
        }
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
