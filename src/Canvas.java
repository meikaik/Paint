
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
                        ArrayList<Point> freeHandPoints = new ArrayList<>();
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
                } else {
                    // perform hit test
                    ArrayList<Model.CanvasShape> shapesArray = model.getCanvasShapes();
                    ListIterator<Model.CanvasShape> it = shapesArray.listIterator(shapesArray.size());
                    model.setDeleteTransformOverride(true);
                    while(it.hasPrevious()) {
                        Model.CanvasShape cs = it.previous();
                        if (hitTest(cs, model.getClickBegin(), 5 + cs.strokeWidth)) {
                            System.out.println("****** HIT ******");
                            cs.selected = true;
                            model.setSelected(false, cs.strokeWidth, cs.strokeColor, cs.fillColor);
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
                    repaint();
                }
                System.out.println("mouse dragged");
            }
        });
        model.addObserver(this);
        // Enable double buffering
        setDoubleBuffered(true);
        this.setVisible(true);
    }

    public void update(Object observable) {
        System.out.println("Canvas Model changed!");
        repaint();
    }

    public void paint(Graphics g) {
        System.out.println("Paint called");

        Graphics2D g2 = (Graphics2D) g;

        // enable antiliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // First paint a white rectangle as the background
        g2.setPaint(Color.WHITE);
        g2.fillRect(0, 0, getSize().width, getSize().height);

        for (Model.CanvasShape cs : model.getCanvasShapes()) {
            drawShape(cs, g2);
        }

        drawPreviewLine(g2);
    }

    private void drawShape(Model.CanvasShape cs, Graphics2D g2) {
        g2.setStroke(new BasicStroke(cs.strokeWidth));
        g2.setPaint(cs.strokeColor);
        if (cs.shape != null) {
            if (checkTranslated(cs)) {
                AffineTransform oldAffine = g2.getTransform();
                AffineTransform newAffine = generateAffine(cs, oldAffine);
                cs.AT  = generateAffine(cs, null);
                g2.setTransform(newAffine);
                g2.draw(cs.shape);
                g2.setPaint(cs.fillColor);
                g2.fill(cs.shape);
                drawSelectRectangle(cs, g2);
                g2.setTransform(oldAffine);
            } else {
                g2.draw(cs.shape);
                g2.setPaint(cs.fillColor);
                g2.fill(cs.shape);
                drawSelectRectangle(cs, g2);
            }
        } else if (cs.freeHandPoints != null) {
            if (checkTranslated(cs)) {
                AffineTransform oldAffine = g2.getTransform();
                AffineTransform newAffine = generateAffine(cs, oldAffine);
                g2.setTransform(newAffine);
                cs.AT  = generateAffine(cs, null);
                drawFreeHandLine(cs.freeHandPoints, g2);
                drawSelectRectangle(cs, g2);
                g2.setTransform(oldAffine);
            } else {
                drawFreeHandLine(cs.freeHandPoints, g2);
                drawSelectRectangle(cs, g2);
            }
        }
    }

    private void drawSelectRectangle(Model.CanvasShape cs, Graphics2D g2) {
        // Draw a rectangular box around shape if it is selected
        if (cs.selected) {
            g2.setStroke(new BasicStroke(1));
            g2.setPaint(Color.CYAN);
            Point[] pointArray = null;
            if (cs.shape != null) {
                pointArray = getBoundingPoints(cs.shape);

            } else if (cs.freeHandPoints != null) {
                pointArray = cs.getFreehandMinMax();
            }
            if (cs.shape != null || cs.freeHandPoints != null) {
                Point p1 = pointArray[0];
                Point p2 = pointArray[1];
                Rectangle.Float rect = drawRectangle(
                        Math.min(p1.x, p2.x) - 10,
                        Math.min(p1.y, p2.y) - 10,
                        Math.max(p1.x, p2.x) + 10,
                        Math.max(p1.y, p2.y) + 10);
                g2.draw(rect);
            }
        }
    }

    private void drawPreviewLine(Graphics2D g2) {
        if (model.getClickBegin() != null && model.getClickEnd() != null && model.getDrawMode()) {
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

    private AffineTransform generateAffine(Model.CanvasShape cs, AffineTransform affine) {
        Point midpoint = cs.getMidPoint();
        AffineTransform affineNew = null;
        if (affine == null) {
            affineNew = new AffineTransform();
        } else {
            affineNew = new AffineTransform(affine);
        }
        affineNew.translate(cs.translateX, cs.translateY);
        affineNew.translate(midpoint.x, midpoint.y);
        affineNew.rotate(Math.toRadians(cs.rotate));
        affineNew.scale(cs.scaleX, cs.scaleY);
        affineNew.shear(cs.shearX, cs.shearY);
        affineNew.translate(-(midpoint.x), -(midpoint.y));
        return affineNew;
    }

    private boolean hitTest(Model.CanvasShape cs, Point mouse, int threshold) {
        Point mouseTransformed = new Point();
        try {
            if (checkTranslated(cs)) {
                AffineTransform	IAT	= cs.AT.createInverse();
                IAT.transform(mouse, mouseTransformed);
            } else {
                mouseTransformed = mouse;
            }
            if (cs.shape != null) {
                if (cs.shape instanceof Line2D.Float) {
                    return lineHitTest(((Line2D.Float) cs.shape), mouseTransformed, threshold);
                } else {
                    return polyHitTest(cs.shape, mouseTransformed, threshold);
                }
            }
            else if (cs.freeHandPoints != null) {
                return freeFormHitTest(cs.freeHandPoints, mouseTransformed, threshold);
            }

        } catch (NoninvertibleTransformException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean lineHitTest(Line2D.Float s, Point mouse, int threshold) {
        double d2 = s.ptSegDist(mouse);
        if (d2 < threshold + model.getStrokeThickness()) {
            return true;
        }
        return false;
    }

    private boolean polyHitTest(Shape s, Point mouse, int threshold) {
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

    private boolean freeFormHitTest(ArrayList<Point> freeHandPoints, Point mouse, int threshold) {
        for (int i = 0; i < freeHandPoints.size() - 1; i++) {
            double d2 = Line2D.ptSegDist(
                    freeHandPoints.get(i).x,
                    freeHandPoints.get(i).y,
                    freeHandPoints.get(i + 1).x,
                    freeHandPoints.get(i + 1).y,
                    mouse.x,
                    mouse.y);
            if (d2 < threshold) {
                return true;
            }
        }
        return false;
    }

    private boolean checkTranslated(Model.CanvasShape cs) {
        return cs.rotate != 0 ||
                cs.scaleX != 1 ||
                cs.scaleY != 1 ||
                cs.translateX != 0 ||
                cs.translateY != 0 ||
                cs.shearX != 0 ||
                cs.shearY != 0;
    }

    private Point[] getBoundingPoints(Shape s) {
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        if (s instanceof Rectangle2D) {
            x1 = (int) ((Rectangle2D) s).getX();
            y1 = (int) ((Rectangle2D) s).getY();
            int width = (int) ((Rectangle2D) s).getWidth();
            int height = (int) ((Rectangle2D) s).getHeight();
            x2 = x1 + width;
            y2 = y1 + height;
        } else if (s instanceof Ellipse2D) {
            x1 = (int) ((Ellipse2D) s).getX();
            y1 = (int) ((Ellipse2D) s).getY();
            int width = (int) ((Ellipse2D) s).getWidth();
            int height = (int) ((Ellipse2D) s).getHeight();
            x2 = x1 + width;
            y2 = y1 + height;
        } else if (s instanceof Line2D) {
            x1 = (int) ((Line2D) s).getX1();
            y1 = (int) ((Line2D) s).getY1();
            x2 = (int) ((Line2D) s).getX2();
            y2 = (int) ((Line2D) s).getY2();
        }
        return new Point[]{new Point(x1, y1), new Point(x2, y2)};
    }

    private void drawFreeHandLine(ArrayList<Point> freeHandPoints, Graphics2D g2){
        for (int i = 0; i < freeHandPoints.size() - 1; i++) {
            g2.drawLine(
                    freeHandPoints.get(i).x,
                    freeHandPoints.get(i).y,
                    freeHandPoints.get(i + 1).x,
                    freeHandPoints.get(i + 1).y
            );
        }
    }


}