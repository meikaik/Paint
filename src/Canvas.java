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
                model.clickBegin = e.getPoint();
                model.clickEnd = model.clickBegin;
                if (model.getDrawMode()) {
                    if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {
                        java.util.List<Point> freeHandPoints = new ArrayList<>();
                        freeHandPoints.add(model.clickBegin);
                        Model.CanvasShape cs = new Model.CanvasShape(
                                freeHandPoints,
                                model.getDrawingMode(),
                                model.getFillColor(),
                                model.getStrokeColor(),
                                model.getStrokeThickness());
                        model.canvasShapes.add(cs);
                    }
                    repaint();
                }
                else {
                    for (Model.CanvasShape cs : model.canvasShapes) {
                        if (cs.shape != null) {
                            if (cs.shape instanceof Line2D.Float) {
                                double d2 = ((Line2D.Float) cs.shape).ptLineDist(model.clickBegin);
                                if (d2 < 5 + model.getStrokeThickness()) {
                                    System.out.println("HIT STRAIGHT LINE");
                                }
                            }
                            if (polyHitTest(cs.shape, model.clickBegin, 5 + model.getStrokeThickness())) {
                                System.out.println("HIT shape!");
                            }
                        }
                        else if (cs.freeHandPoints != null) {
                            for (int i = 0; i < cs.freeHandPoints.size() - 1; i+=2) {
                                double d2 = Line2D.ptSegDist(
                                        cs.freeHandPoints.get(i).x,
                                        cs.freeHandPoints.get(i).y,
                                        cs.freeHandPoints.get(i + 1).x,
                                        cs.freeHandPoints.get(i + 1).y,
                                        model.clickBegin.x,
                                        model.clickBegin.y);
                                if (d2 < 5 + model.getStrokeThickness()) {
                                    System.out.println("HIT FREEFORM");
                                }
                            }
                        }
                    }
                }
                System.out.println("mouse pressed");
            }

            public void mouseReleased(MouseEvent e) {
                if (model.getDrawMode()) {
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
                            break;
                        case FREEFORM:
                            model.canvasShapes.get(model.canvasShapesSize).freeHandPoints.add(model.clickEnd);
                            break;

                    }
                    if (model.getDrawingMode() != Model.drawingModeType.FREEFORM) {
                        Model.CanvasShape cs = new Model.CanvasShape(
                                shape,
                                model.getDrawingMode(),
                                model.getFillColor(),
                                model.getStrokeColor(),
                                model.getStrokeThickness());
                        model.canvasShapes.add(cs);
                    }
                    model.canvasShapesSize++;
                    repaint();
                }
                model.clickBegin = null;
                model.clickEnd = null;
                System.out.println("mouse released");
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                model.clickEnd = e.getPoint();
                if (model.getDrawMode()) {
                    if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {
                        // Add the intermediate point to the freeform point model
                        model.canvasShapes.get(model.canvasShapesSize).freeHandPoints.add(model.clickEnd);

                    }
                    repaint();
                }
                System.out.println("mouse dragged");
            }
        });

        model.addObserver(this);
        this.setVisible(true);
    }

    public void paint(Graphics g) {
        System.out.println("Paint called");
        Graphics2D g2 = (Graphics2D) g;
        // antiliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (Model.CanvasShape cs : model.canvasShapes) {
            g2.setStroke(new BasicStroke(cs.strokeWidth));
            g2.setPaint(cs.strokeColor);
            if (cs.shape != null) {
                g2.draw(cs.shape);
                g2.setPaint(cs.fillColor);
                g2.fill(cs.shape);
            }
            else {
                // draw the Freeform line
                for (int i = 0; i < cs.freeHandPoints.size() - 1; i++) {
                    g.drawLine(
                            cs.freeHandPoints.get(i).x,
                            cs.freeHandPoints.get(i).y,
                            cs.freeHandPoints.get(i + 1).x,
                            cs.freeHandPoints.get(i + 1).y
                    );
                }
            }
        }

        if (model.clickBegin != null && model.clickEnd != null) {
            // draw a semi transparent line
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2.setPaint(Color.GRAY);
            float[] dashPattern = {3.0f, 3.0f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashPattern, 3.0f));
            switch (model.getDrawingMode()) {
                case ELLIPSE:
                    g2.draw(drawEllipse(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                case RECTANGLE:
                    g2.draw(drawRectangle(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                case STRAIGHT:
                    g2.draw(drawLine(model.clickBegin.x, model.clickBegin.y, model.clickEnd.x, model.clickEnd.y));
                    break;
                // We don't need a preview for FREEFORM
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

    private Boolean polyHitTest(Shape s, Point mouse, int threshold) {
        return s.contains(mouse.x, mouse.y) ||
                s.contains(mouse.x + threshold, mouse.y) ||
                s.contains(mouse.x, mouse.y + threshold) ||
                s.contains(mouse.x - threshold, mouse.y) ||
                s.contains(mouse.x, mouse.y - threshold) ||
                s.contains(mouse.x + threshold, mouse.y + threshold) ||
                s.contains(mouse.x - threshold, mouse.y - threshold) ||
                s.contains(mouse.x + threshold, mouse.y - threshold) ||
                s.contains(mouse.x - threshold, mouse.y + threshold);
    }
}