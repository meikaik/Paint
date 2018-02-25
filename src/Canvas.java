import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;

public class Canvas extends JComponent implements Observer {

    private Model model;

    public Canvas(Model model) {
        System.out.println("Canvas called");

        this.model = model;

        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {
                    model.freeHands.add(Arrays.asList(new Point(e.getX(), e.getY())));
                }
                model.clickBegin = new Point(e.getX(), e.getY());
                model.clickEnd = model.clickBegin;
                System.out.println("mouse pressed");
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                Shape shape = null;
                switch (model.getDrawingMode()) {
                    case ELLIPSE:
                        shape = drawEllipse(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y);
                        break;
                    case RECTANGLE:
                        shape = drawRectangle(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y);
                        break;
                    case STRAIGHT:
                        shape = drawLine(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y);

                }
                    model.shapes.add(shape);
                    model.fillColors.add(model.getFillColor());
                    model.strokeColors.add(model.getStrokeColor());
                    model.strokeWidths.add(model.getStrokeThickness());
                    model.clickBegin = null;
                    model.clickEnd = null;
                    System.out.println("mouse released");

                repaint();

            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {

                }
                model.clickEnd = new Point(e.getX(), e.getY());
                System.out.println("mouse dragged");
                repaint();
            }
        });

        model.addObserver(this);
        this.setVisible(true);
    }

    public void paint(Graphics g) {
        System.out.println("Paint called");
        Graphics2D gc = (Graphics2D) g;

        Iterator<Color> stroke = model.strokeColors.iterator();
        Iterator<Integer> strokeWidth = model.strokeWidths.iterator();
        Iterator<Color> fill = model.fillColors.iterator();
        gc.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Shape s : model.shapes) {
            gc.setStroke(new BasicStroke(strokeWidth.next()));
            gc.setPaint(stroke.next());
            gc.draw(s);
            gc.setPaint(fill.next());
            gc.fill(s);
        }

        if (model.clickBegin != null && model.clickEnd != null) {
            // draw a semi transparent line
            gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            gc.setPaint(Color.GRAY);
            float[] dashPattern = {3.0f, 3.0f};
            gc.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashPattern, 3.0f));
            switch (model.getDrawingMode()) {
                case ELLIPSE:
                    gc.draw(drawEllipse(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                case RECTANGLE:
                    gc.draw(drawRectangle(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                case STRAIGHT:
                    gc.draw(drawLine(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                case FREEFORM:
                    //
            }
        }
    }

    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.
        System.out.println("Canvas Model changed!");
        repaint();
    }

    private Rectangle2D.Float drawRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    private Ellipse2D.Float drawEllipse(int x1, int y1, int x2, int y2) {
        return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    private Line2D.Float drawLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }

    }