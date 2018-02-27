import javafx.scene.transform.Affine;

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
                Point p = e.getPoint();
                model.setClickBegin(p);
                model.setClickEnd(p);
                // unselect all shapes
                for (Model.CanvasShape cs : model.getCanvasShapes()) {
                    cs.selected = false;
                }
                if (model.getDrawMode()) {
                    if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {
                        java.util.List<Point> freeHandPoints = new ArrayList<>();
                        freeHandPoints.add(model.getClickBegin());
                        Model.CanvasShape cs = new Model.CanvasShape(
                                freeHandPoints,
                                model.getDrawingMode(),
                                model.getFillColor(),
                                model.getStrokeColor(),
                                model.getStrokeThickness());
                        model.addCanvasShape(cs);
                    }
                    repaint();
                }
                else {
                    java.util.List<Model.CanvasShape> shapesArray = model.getCanvasShapes();
                    ListIterator<Model.CanvasShape> it = shapesArray.listIterator(shapesArray.size());
                    model.setDeleteTransformOverride(true);
                    while(it.hasPrevious()) {
                        Model.CanvasShape cs = it.previous();
                        // perform hit-test
                        if (hitTest(cs, model.getClickBegin(), 5 + model.getStrokeThickness())) {
                            System.out.println("HIT shape/line/freeform!");
                            cs.selected = true;
                            model.setDeleteTransformOverride(false);
                            model.strokeThickness = (cs.strokeWidth);
                            model.strokeColor = (cs.strokeColor);
                            model.setFillColor(cs.fillColor);
                            break;
                        }
                    }
                }
                System.out.println("mouse pressed");
            }

            public void mouseReleased(MouseEvent e) {
                if (model.getDrawMode()) {
                    Shape shape = null;
                    Point clickBegin = model.getClickBegin();
                    Point clickEnd = model.getClickEnd();
                    switch (model.getDrawingMode()) {
                        case ELLIPSE:
                            shape = drawEllipse(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y);
                            break;
                        case RECTANGLE:
                            shape = drawRectangle(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y);
                            break;
                        case STRAIGHT:
                            shape = drawLine(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y);
                            break;
                        case FREEFORM:
                            model.getCanvasShapes().get(model.getCanvasShapesSize()).freeHandPoints.add(clickEnd);
                            break;

                    }
                    if (model.getDrawingMode() != Model.drawingModeType.FREEFORM) {
                        Model.CanvasShape cs = new Model.CanvasShape(
                                shape,
                                model.getDrawingMode(),
                                model.getFillColor(),
                                model.getStrokeColor(),
                                model.getStrokeThickness());
                        model.addCanvasShape(cs);
                    }
                    model.incrementCanvasShapesSize();
                    model.setClickBegin(null);
                    model.setClickEnd(null);
                }
                System.out.println("mouse released");
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (model.getDrawMode()) {
                    model.setClickEnd(e.getPoint());
                    if (model.getDrawingMode() == Model.drawingModeType.FREEFORM) {
                        // Add the intermediate point to the freeform point model
                        model.getCanvasShapes().get(model.getCanvasShapesSize()).freeHandPoints.add(model.getClickEnd());
                    }
                }
                System.out.println("mouse dragged");
            }
        });
        model.addObserver(this);
        // TODO: Background color
        this.setVisible(true);
    }

    public void update(Object observable) {
        // XXX Fill this in with the logic for updating the view when the model
        // changes.
        System.out.println("Canvas Model changed!");
        repaint();
    }

    public void paint(Graphics g) {
        System.out.println("Paint called");
        Graphics2D g2 = (Graphics2D) g;
        // antiliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Model.CanvasShape cs : model.getCanvasShapes()) {
            drawShape(cs, g2);
            drawSelectRectangle(cs, g2);
        }
        drawPreviewLine(g2);
    }

    private void drawShape(Model.CanvasShape cs, Graphics2D g2) {
        g2.setStroke(new BasicStroke(cs.strokeWidth));
        g2.setPaint(cs.strokeColor);
        if (cs.shape != null) {
            if (cs.rotate != 0 || cs.scaleX != 1 || cs.scaleY != 1 || cs.translateX != 0 || cs.translateY != 0) {
                Shape newShape = transformShape(cs);
                g2.draw(newShape);
                g2.setPaint(cs.fillColor);
                g2.fill(newShape);
            }
            else {
                // TODO: implement transform for freeform lines
                g2.draw(cs.shape);
                g2.setPaint(cs.fillColor);
                g2.fill(cs.shape);
            }

        } else if (cs.freeHandPoints != null) {
            // draw the Freeform line
            for (int i = 0; i < cs.freeHandPoints.size() - 1; i++) {
                g2.drawLine(
                        cs.freeHandPoints.get(i).x,
                        cs.freeHandPoints.get(i).y,
                        cs.freeHandPoints.get(i + 1).x,
                        cs.freeHandPoints.get(i + 1).y
                );
            }
        }
    }

    private void drawSelectRectangle(Model.CanvasShape cs, Graphics2D g2) {
        // Draw a rectangular box around shape if it is selected
        if (cs.selected) {
            g2.setStroke(new BasicStroke(1));
            g2.setPaint(Color.CYAN);
            if (cs.shape != null) {
                // TODO: Impelment transform for select box
                int x1 = 0;
                int x2 = 0;
                int y1 = 0;
                int y2 = 0;
                if (cs.shape instanceof Rectangle2D) {
                    x1 = (int) ((Rectangle2D) cs.shape).getX();
                    y1 = (int) ((Rectangle2D) cs.shape).getY();
                    int width = (int) ((Rectangle2D) cs.shape).getWidth();
                    int height = (int) ((Rectangle2D) cs.shape).getHeight();
                    x2 = x1 + width;
                    y2 = y1 + height;
                } else if (cs.shape instanceof Ellipse2D) {
                    x1 = (int) ((Ellipse2D) cs.shape).getX();
                    y1 = (int) ((Ellipse2D) cs.shape).getY();
                    int width = (int) ((Ellipse2D) cs.shape).getWidth();
                    int height = (int) ((Ellipse2D) cs.shape).getHeight();
                    x2 = x1 + width;
                    y2 = y1 + height;
                } else if (cs.shape instanceof Line2D) {
                    // TODO: add the custom x1 and y1 conditions again to make sure the select box is correct
                    x1 = (int) ((Line2D) cs.shape).getX1();
                    y1 = (int) ((Line2D) cs.shape).getY1();
                    x2 = (int) ((Line2D) cs.shape).getX2();
                    y2 = (int) ((Line2D) cs.shape).getY2();
                }
                Rectangle.Float rect = drawRectangle(x1 - 10, y1 - 10, x2 + 10, y2 + 10);
                g2.draw(rect);
            } else if (cs.freeHandPoints != null) {
                int xMin = Integer.MAX_VALUE;
                int yMin = Integer.MAX_VALUE;
                int xMax = Integer.MIN_VALUE;
                int yMax = Integer.MIN_VALUE;

                for (int i = 0; i < cs.freeHandPoints.size() - 1; i++) {
                    if (cs.freeHandPoints.get(i).x < xMin) {
                        xMin = cs.freeHandPoints.get(i).x;
                    }
                    if (cs.freeHandPoints.get(i).x > xMax) {
                        xMax = cs.freeHandPoints.get(i).x;
                    }
                    if (cs.freeHandPoints.get(i).y < yMin) {
                        yMin = cs.freeHandPoints.get(i).y;
                    }
                    if (cs.freeHandPoints.get(i).y > yMax) {
                        yMax = cs.freeHandPoints.get(i).y;
                    }
                }
                Rectangle.Float rect = drawRectangle(xMin - 10, yMin - 10, xMax + 10, yMax + 10);
                g2.draw(rect);
            }
        }
    }

    private void drawPreviewLine(Graphics2D g2) {
        if (model.getClickBegin() != null && model.getClickEnd() != null && model.getDrawMode()) {
            // draw a semi transparent line
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2.setPaint(Color.GRAY);
            float[] dashPattern = {3.0f, 3.0f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dashPattern, 3.0f));
            Point clickBegin = model.getClickBegin();
            Point clickEnd = model.getClickEnd();
            switch (model.getDrawingMode()) {
                case ELLIPSE:
                    g2.draw(drawEllipse(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y));
                    break;
                case RECTANGLE:
                    g2.draw(drawRectangle(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y));
                    break;
                case STRAIGHT:
                    g2.draw(drawLine(clickBegin.x, clickBegin.y, clickEnd.x, clickEnd.y));
                    break;
                // We don't need a preview for FREEFORM
            }
        }
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

    private Shape transformShape(Model.CanvasShape cs) {
        AffineTransform affine = new AffineTransform();
        affine.translate(cs.translateX, cs.translateY);
        affine.translate(cs.getMidPoint().x, cs.getMidPoint().y);
        affine.rotate(Math.toRadians(cs.rotate));
        affine.scale(cs.scaleX, cs.scaleY);
        affine.translate(-cs.getMidPoint().x, -cs.getMidPoint().y);
        return affine.createTransformedShape(cs.shape);
    }

    private Boolean hitTest(Model.CanvasShape cs, Point mouse, int threshold) {
        if (cs.shape != null) {
            if (cs.shape instanceof Line2D.Float) {
                return lineHitTest(cs, mouse, threshold);
            } else {
                return polyHitTest(cs.shape, mouse, threshold);
            }
        }
        else if (cs.freeHandPoints != null) {
            for (int i = 0; i < cs.freeHandPoints.size() - 1; i++) {
                double d2 = Line2D.ptSegDist(
                        cs.freeHandPoints.get(i).x,
                        cs.freeHandPoints.get(i).y,
                        cs.freeHandPoints.get(i + 1).x,
                        cs.freeHandPoints.get(i + 1).y,
                        mouse.x,
                        mouse.y);
                if (d2 < threshold) {
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean lineHitTest(Model.CanvasShape cs, Point mouse, int threshold) {
        double d2 = ((Line2D.Float) cs.shape).ptSegDist(mouse);
        if (d2 < threshold + model.getStrokeThickness()) {
            return true;
        }
        return false;
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