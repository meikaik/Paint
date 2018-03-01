
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.awt.event.*;


public class MainView extends JFrame implements Observer {

    private Model model;
    private BufferedImage fillColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
    private BufferedImage strokeColorImage =
            new BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_RGB);
    // TODO: fix bug where when a shape is selected it takes on the attributes of the model
    // Menubar
    private JRadioButtonMenuItem selectionMode;
    private JRadioButtonMenuItem drawingMode;
    private JMenuItem deleteShape;
    private JMenuItem transformShape;
    private JMenu strokeWidth;
    private JMenuItem fillColor;
    private JMenuItem strokeColor;

    // Toolbar
    private JButton selectButton;
    private JButton drawButton;
    private JComboBox drawingModes;
    private JComboBox strokes;
    private JButton fillColorButton;
    private JButton strokeColorButton;

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
        if (!model.getDrawMode()) {
            deleteShape.setEnabled(true);
            transformShape.setEnabled(true);
        }
        else {
            deleteShape.setEnabled(false);
            transformShape.setEnabled(false);
            for (Model.CanvasShape cs : model.getCanvasShapes()) {
                cs.selected = false;
            }
        }
        if (model.getdeleteTransformOverride()) {
            deleteShape.setEnabled(false);
            transformShape.setEnabled(false);
        }

        // Update colors on toolbar/menu
        strokeColorButton.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));
        fillColorButton.setIcon(changeColor(fillColorImage, model.getFillColor()));
        fillColor.setIcon(changeColor(fillColorImage, model.getFillColor()));
        strokeColor.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));


        // Update stroke width
        strokeWidth.getItem(model.getStrokeThickness() - 1).setSelected(true);
        strokes.setSelectedIndex(model.getStrokeThickness() - 1);
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
        new1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                model.resetCanvasShapes();
            }
        });

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
                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        cs.shape = null;
                        cs.freeHandPoints = null;
                        cs.selected = false;
                        model.setDeleteTransformOverride(true);
                    }
                }
            }
        });

        transformShape = new JMenuItem("Transform Shape");
        transformShape.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        transformShape.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addTransformShapeModal();
            }
        });


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
                // TODO: Refactor this... its so ugly
                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        if ("10px" == button.getText()) {
                            cs.strokeWidth = 10;
                        }
                        else {
                            int val = Character.getNumericValue(button.getText().charAt(0));
                            cs.strokeWidth = val;
                        }
                    }
                }
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
        fillColor = new JMenuItem("Fill Colour...", fill);
        fillColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Fill Colour", model.getFillColor());
                if (newColor != null) {
                    model.setFillColor(newColor);
                }
                System.out.println("New fill color: " + model.getFillColor());
                fillColor.setIcon(changeColor(fillColorImage, model.getFillColor()));

                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        cs.fillColor = model.getFillColor();
                    }
                }
            }
        });
        return fillColor;
    }

    private JMenuItem addStrokeColorMenuItem() {
        ImageIcon stroke = changeColor(strokeColorImage,  model.getStrokeColor());
        strokeColor = new JMenuItem("Stroke Colour...", stroke);
        strokeColor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Stroke Colour", model.getStrokeColor());
                if (newColor != null) {
                    model.setStrokeColor(newColor);
                }
                System.out.println("New stroke color: " + model.getStrokeColor());
                strokeColor.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));

                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        cs.strokeColor = model.getStrokeColor();
                    }
                }
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
                    model.setDeleteTransformOverride(true);
                }
            }
        });

        drawButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if (!model.getDrawMode()) {
                    model.setDrawMode();
                    model.setDeleteTransformOverride(true);
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
                // TODO: Refactor this... its so ugly
                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        if ("10px" == temp) {
                            cs.strokeWidth = 10;
                        }
                        else {
                            int val = Character.getNumericValue(temp.charAt(0));
                            cs.strokeWidth = val;
                        }
                    }
                }
            }
        });
        return strokes;
    }

    private JButton addFillColorButton() {
        ImageIcon fill = changeColor(fillColorImage,  model.getFillColor());
        fillColorButton = new JButton("Fill Colour", fill);
        fillColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Fill Colour", model.getFillColor());
                if (newColor != null) {
                    model.setFillColor(newColor);
                }
                System.out.println("New fill color: " + model.getFillColor());
                fillColorButton.setIcon(changeColor(fillColorImage, model.getFillColor()));

                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        cs.fillColor = model.getFillColor();
                    }
                }
            }
        });
        return fillColorButton;
    }

    private JButton addStrokeColorButton() {
        ImageIcon stroke = changeColor(strokeColorImage, model.getStrokeColor());
        strokeColorButton = new JButton("Stroke Colour", stroke);
        strokeColorButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                Color newColor = JColorChooser.showDialog(null, "Choose Stroke Colour", model.getStrokeColor());
                if (newColor != null) {
                    model.setStrokeColor(newColor);
                }
                System.out.println("New stroke color: " + model.getStrokeColor());
                strokeColorButton.setIcon(changeColor(strokeColorImage, model.getStrokeColor()));

                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    if (cs.selected) {
                        cs.strokeColor = model.getStrokeColor();
                    }
                }
            }
        });
        return strokeColorButton;
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

    private void addTransformShapeModal() {
        Model.CanvasShape selectedShape = null;
        for (Model.CanvasShape cs : model.getCanvasShapes()) {
            if (cs.selected) {
                selectedShape = cs;
            }
        }
        JSpinner translateX = new JSpinner(
                new SpinnerNumberModel(selectedShape.translateX, -1000, 1000, 1));
        JSpinner translateY = new JSpinner(
                new SpinnerNumberModel(selectedShape.translateY, -1000, 1000, 1));
        JSpinner rotate = new JSpinner(
                new SpinnerNumberModel(selectedShape.rotate, -360, 360, 1));
        JSpinner scaleX = new JSpinner(
                new SpinnerNumberModel(selectedShape.scaleX, -10, 10, 0.1));
        JSpinner scaleY = new JSpinner(
                new SpinnerNumberModel(selectedShape.scaleY, -10, 10, 0.1));
        JPanel panel = new JPanel(new GridLayout(3, 3));
        JPanel subpanel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Translate (px):"));
        subpanel.add(new JLabel("                        x:"));
        subpanel.add(translateX);
        panel.add(subpanel);
        JPanel subpanel2 = new JPanel(new GridLayout(1, 2));
        subpanel2.add(new JLabel("                        y:"));
        subpanel2.add(translateY);
        panel.add(subpanel2);
        panel.add(new JLabel("Rotate (degrees):"));
        panel.add(rotate);
        panel.add(new JLabel(""));
        panel.add(new JLabel("Scale (times):"));
        JPanel subpanel3 = new JPanel(new GridLayout(1, 2));
        subpanel3.add(new JLabel("                        x:"));
        subpanel3.add(scaleX);
        panel.add(subpanel3);
        JPanel subpanel4 = new JPanel(new GridLayout(1, 2));
        subpanel4.add(new JLabel("                        y:"));
        subpanel4.add(scaleY);
        panel.add(subpanel4);
        int result = JOptionPane.showConfirmDialog(null, panel, "Transform Shape",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            selectedShape.translateX = (int) translateX.getValue();
            selectedShape.translateY = (int) translateY.getValue();
            selectedShape.rotate = (int) rotate.getValue();
            selectedShape.scaleX = (double) scaleX.getValue();
            selectedShape.scaleY = (double) scaleY.getValue();
            model.notifyObservers();
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
