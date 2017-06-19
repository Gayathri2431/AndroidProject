package com.cameracountmodule.manager;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.cameracountmodule.Utils.Global;
import com.cameracountmodule.interfaceUI.DrawingCallback;
import com.cameracountmodule.model.Circles;
import com.cameracountmodule.model.DataClass;
import com.cameracountmodule.model.LineModel;
import com.cameracountmodule.model.Lines;
import com.cameracountmodule.model.Rectangles;

import org.jcodec.common.model.Point;

import java.util.ArrayList;

public class DrawingPad implements View.OnTouchListener {
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int MOVE = 3;

    private ScrollView scrollView;
    private int mode = NONE;

    private boolean isTraining = false;
    public Bitmap backgroundImage;
    private float oldDist = 1f;

    private boolean pendingDrag;
    public ImageView overlayImageView;
    private PointF start = new PointF();
    private int moveCount = 0;
    private Context ctx;
    private ArrayList<Point> tempPoints = new ArrayList<>();
    private DataClass dataClass;
    private GestureDetector detector;

    private int circlePosition = -1;
    private float radius = 0;
    private int smallCirclePosition = -1;
    private float smallRadius = 0;
    private int linePosition = -1;
    private float startx,starty,endx,endy;
    private float x1,y1,x2,y2,x3,y3,x4,y4;
    private int rectPosition = -1;
    private float perimeter = 0;

    private DrawingCallback drawingCallback;

    public DrawingPad(Context context, DrawingCallback callback) {
        ctx = context;
        dataClass = DataClass.getInstance();
        drawingCallback = callback;
        detector = new GestureDetector(context, new GestureTap());
    }

    class GestureTap extends GestureDetector.SimpleOnGestureListener {
        //Removing the object
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (backgroundImage == null) {
                return false;
            }
            Point imagePoint = getImagePoint(e, overlayImageView); // get tapped point value
            if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                for(int i=0; i<dataClass.circles.size();i++) {
                    Circles circle = dataClass.circles.get(i);
                    Rect frame = circle.getFrame();
                    if(frame.contains(imagePoint.getX(),imagePoint.getY())) {
                        dataClass.circles.remove(i);
                        for (int j = i; j < dataClass.circles.size(); j++) {
                            dataClass.circles.get(j).setId(dataClass.circles.get(j).getId() - 1);
                        }
                        drawOverlay();
                    }
                }
            } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                for(int i=0; i<dataClass.smallCircles.size();i++) {
                    Circles circle = dataClass.smallCircles.get(i);
                    Rect frame = circle.getFrame();
                    if(frame.contains(imagePoint.getX(),imagePoint.getY())) {
                        dataClass.smallCircles.remove(i);
                        for (int j = i; j < dataClass.smallCircles.size(); j++) {
                            dataClass.smallCircles.get(j).setId(dataClass.smallCircles.get(j).getId() - 1);
                        }
                        drawOverlay();
                    }
                }
            } else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                for (int i = 0; i < dataClass.lines.size(); i++) {
                    Lines model = dataClass.lines.get(i);
                    Path deletedLine = model.getPath();
                    RectF rectF = new RectF();
                    deletedLine.computeBounds(rectF, true);

                    if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                        dataClass.lines.remove(i);
                    }
                    drawOverlay();
                }
            } else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                for (int i = 0; i < dataClass.rectangles.size(); i++) {
                    Rectangles model = dataClass.rectangles.get(i);
                    Path deletedLine = model.getPath();
                    RectF rectF = new RectF();
                    deletedLine.computeBounds(rectF, true);

                    if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                        dataClass.rectangles.remove(i);
                        for (int j = i; j < dataClass.rectangles.size(); j++) {
                            dataClass.rectangles.get(j).setId(dataClass.rectangles.get(j).getId() - 1);
                        }
                    }
                    drawOverlay();
                }
            } else {
                for(int i=0; i<dataClass.circles.size();i++) {
                    Circles circle = dataClass.circles.get(i);
                    Rect frame = circle.getFrame();
                    if(frame.contains(imagePoint.getX(),imagePoint.getY())) {
                        dataClass.circles.remove(i);
                        for (int j = i; j < dataClass.circles.size(); j++) {
                            dataClass.circles.get(j).setId(dataClass.circles.get(j).getId() - 1);
                        }
                        drawOverlay();
                    }
                }

                for(int i=0; i<dataClass.smallCircles.size();i++) {
                    Circles circle = dataClass.smallCircles.get(i);
                    Rect frame = circle.getFrame();
                    if(frame.contains(imagePoint.getX(),imagePoint.getY())) {
                        dataClass.smallCircles.remove(i);
                        for (int j = i; j < dataClass.smallCircles.size(); j++) {
                            dataClass.smallCircles.get(j).setId(dataClass.smallCircles.get(j).getId() - 1);
                        }
                        drawOverlay();
                    }
                }

                for (int i = 0; i < dataClass.lines.size(); i++) {
                    Lines model = dataClass.lines.get(i);
                    Path deletedLine = model.getPath();
                    RectF rectF = new RectF();
                    deletedLine.computeBounds(rectF, true);

                    if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                        dataClass.lines.remove(i);
                    }
                    drawOverlay();
                }

                for (int i = 0; i < dataClass.rectangles.size(); i++) {
                    Rectangles model = dataClass.rectangles.get(i);
                    Path deletedLine = model.getPath();
                    RectF rectF = new RectF();
                    deletedLine.computeBounds(rectF, true);

                    if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                        dataClass.rectangles.remove(i);
                        for (int j = i; j < dataClass.rectangles.size(); j++) {
                            dataClass.rectangles.get(j).setId(dataClass.rectangles.get(j).getId() - 1);
                        }
                    }
                    drawOverlay();
                }
            }
            return true;
        }

        //Adding the object
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (backgroundImage == null) {
                return false;
            }
            final Point imagePoint = getImagePoint(e, overlayImageView); // get tapped point value

            if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                int radius = 0;
                if (dataClass.circles.size() != 0) {
                    radius = (int)dataClass.circles.get(0).getRadius();
                } else {
                    radius = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                }
                Circles newCircle = new Circles(imagePoint.getX(),imagePoint.getY(),radius,createNextId());
                dataClass.circles.add(newCircle);
                drawOverlay();
            } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                int radius = 0;
                if (dataClass.smallCircles.size() != 0) {
                    radius = (int)dataClass.smallCircles.get(0).getRadius();
                } else {
                    radius = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                }
                Circles newCircle = new Circles(imagePoint.getX(),imagePoint.getY(),radius,createNextId());
                dataClass.smallCircles.add(newCircle);
                drawOverlay();
            } else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                tempPoints.add(imagePoint);
                if(tempPoints.size() == 2) {
                    Lines line = new Lines(tempPoints.get(0).getX(),tempPoints.get(0).getY(),tempPoints.get(1).getX(),tempPoints.get(1).getY());
                    dataClass.lines.add(line);
                    tempPoints.clear();
                }
                drawOverlay();
            } else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                int perimeter = 0;
                if (dataClass.rectangles.size() != 0) {
                    perimeter = (int)dataClass.rectangles.get(0).getPerimeter();
                } else {
                    perimeter = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                }
                Rect rect = new Rect(imagePoint.getX() - perimeter/2,imagePoint.getY() - perimeter/2,imagePoint.getX() + perimeter/2, imagePoint.getY() + perimeter/2);
                Rectangles rectangle = new Rectangles(rect.left,rect.top,rect.left + perimeter,rect.top,rect.right,rect.bottom,rect.right - perimeter,rect.bottom,imagePoint.getX(),imagePoint.getY(),perimeter,createNextId());
                dataClass.rectangles.add(rectangle);
                drawOverlay();
            } else {
                CharSequence colors[] = new CharSequence[] {"Circle", "Smallcircle", "Rectangle", "Line", "Cancel"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setTitle("Select the Shape");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       if(which == 0) {
                           int radius = 0;
                           if (dataClass.circles.size() != 0) {
                               radius = (int)dataClass.circles.get(0).getRadius();
                           } else {
                               radius = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                           }
                           Circles newCircle = new Circles(imagePoint.getX(),imagePoint.getY(),radius,createNextId());
                           dataClass.circles.add(newCircle);
                           drawOverlay();
                       } else if(which == 1) {
                           int radius = 0;
                           if (dataClass.smallCircles.size() != 0) {
                               radius = (int)dataClass.smallCircles.get(0).getRadius();
                           } else {
                               radius = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                           }
                           Circles newCircle = new Circles(imagePoint.getX(),imagePoint.getY(),radius,createNextId());
                           dataClass.smallCircles.add(newCircle);
                           drawOverlay();
                       } else if(which == 2) {
                           int perimeter = 0;
                           if (dataClass.rectangles.size() != 0) {
                               perimeter = (int)dataClass.rectangles.get(0).getPerimeter();
                           } else {
                               perimeter = 100 * backgroundImage.getWidth() / overlayImageView.getWidth();
                           }
                           Rect rect = new Rect(imagePoint.getX() - perimeter/2,imagePoint.getY() - perimeter/2,imagePoint.getX() + perimeter/2, imagePoint.getY() + perimeter/2);
                           Rectangles rectangle = new Rectangles(rect.left,rect.top,rect.left + perimeter,rect.top,rect.right,rect.bottom,rect.right - perimeter,rect.bottom,imagePoint.getX(),imagePoint.getY(),perimeter,createNextId());
                           dataClass.rectangles.add(rectangle);
                           drawOverlay();
                       } else if(which == 3){
                           tempPoints.add(imagePoint);
                       }
                    }
                });

                if(tempPoints.size() == 1) {
                    tempPoints.add(imagePoint);
                    if(tempPoints.size() == 2) {
                        Lines line = new Lines(tempPoints.get(0).getX(),tempPoints.get(0).getY(),tempPoints.get(1).getX(),tempPoints.get(1).getY());
                        dataClass.lines.add(line);
                        tempPoints.clear();
                    }
                    drawOverlay();
                } else {
                    builder.show();
                }
            }

            return true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (backgroundImage == null) {
            return false;
        }
        ImageView imageView = (ImageView) v;
        overlayImageView = imageView;
        if (detector.onTouchEvent(event)) {
            return true;
        }
        Point imagePoint = getImagePoint(event, imageView);

        //Circle detection on touch point
        if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
            for(int i=0; i< dataClass.circles.size();i++) {
                if(dataClass.circles.get(i).getFrame().contains(imagePoint.getX(), imagePoint.getY())) {
                    circlePosition = i;
                    pendingDrag = true;
                    radius = dataClass.circles.get(i).getRadius();
                }
            }
        }

        //SmallCircles detection on touch point
        else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
            for(int i=0; i< dataClass.smallCircles.size();i++) {
                if(dataClass.smallCircles.get(i).getFrame().contains(imagePoint.getX(), imagePoint.getY())) {
                    smallCirclePosition = i;
                    pendingDrag = true;
                    smallRadius = dataClass.smallCircles.get(i).getRadius();
                }
            }
        }

        //Line detection in touch point
        else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
            for (int i = 0; i < dataClass.lines.size(); i++) {
                Lines model = dataClass.lines.get(i);
                Path deletedLine = model.getPath();
                RectF rectF = new RectF();
                deletedLine.computeBounds(rectF, true);

                if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                    linePosition = i;
                    pendingDrag = true;
                    startx = dataClass.lines.get(i).getStartx();
                    starty = dataClass.lines.get(i).getStarty();
                    endx = dataClass.lines.get(i).getEndx();
                    endy = dataClass.lines.get(i).getEndy();
                }
            }
        }

        //Rectangle detection in touch point
        else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
            for (int i = 0; i < dataClass.rectangles.size(); i++) {
                Rectangles model = dataClass.rectangles.get(i);
                Path deletedLine = model.getPath();
                RectF rectF = new RectF();
                deletedLine.computeBounds(rectF, true);

                if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                    rectPosition = i;
                    pendingDrag = true;
                    perimeter = dataClass.rectangles.get(i).perimeter;
                    x1 = dataClass.rectangles.get(i).getX1();
                    y1 = dataClass.rectangles.get(i).getY1();
                    x2 = dataClass.rectangles.get(i).getX2();
                    y2 = dataClass.rectangles.get(i).getY2();
                    x3 = dataClass.rectangles.get(i).getX3();
                    y3 = dataClass.rectangles.get(i).getY3();
                    x4 = dataClass.rectangles.get(i).getX4();
                    y4 = dataClass.rectangles.get(i).getY4();
                }
            }
        }

        //MultiShape detection in touch point
        else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
            for(int i=0; i< dataClass.smallCircles.size();i++) {
                if(dataClass.smallCircles.get(i).getFrame().contains(imagePoint.getX(), imagePoint.getY())) {
                    smallCirclePosition = i;
                    linePosition = -1;
                    circlePosition = -1;
                    rectPosition = -1;
                    pendingDrag = true;
                    smallRadius = dataClass.smallCircles.get(i).getRadius();
                }
            }

            for(int i=0; i< dataClass.circles.size();i++) {
                if(dataClass.circles.get(i).getFrame().contains(imagePoint.getX(), imagePoint.getY())) {
                    circlePosition = i;
                    linePosition = -1;
                    smallCirclePosition = -1;
                    rectPosition = -1;
                    pendingDrag = true;
                    radius = dataClass.circles.get(i).getRadius();
                }
            }

            for (int i = 0; i < dataClass.lines.size(); i++) {
                Lines model = dataClass.lines.get(i);
                Path deletedLine = model.getPath();
                RectF rectF = new RectF();
                deletedLine.computeBounds(rectF, true);

                if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                    linePosition = i;
                    rectPosition = -1;
                    circlePosition = -1;
                    smallCirclePosition = -1;
                    pendingDrag = true;
                    startx = dataClass.lines.get(i).getStartx();
                    starty = dataClass.lines.get(i).getStarty();
                    endx = dataClass.lines.get(i).getEndx();
                    endy = dataClass.lines.get(i).getEndy();
                }
            }

            for (int i = 0; i < dataClass.rectangles.size(); i++) {
                Rectangles model = dataClass.rectangles.get(i);
                Path deletedLine = model.getPath();
                RectF rectF = new RectF();
                deletedLine.computeBounds(rectF, true);

                if (rectF.contains(imagePoint.getX(), imagePoint.getY())) {
                    rectPosition = i;
                    linePosition = -1;
                    circlePosition = -1;
                    smallCirclePosition = -1;
                    pendingDrag = true;
                    perimeter = dataClass.rectangles.get(i).perimeter;
                    x1 = dataClass.rectangles.get(i).getX1();
                    y1 = dataClass.rectangles.get(i).getY1();
                    x2 = dataClass.rectangles.get(i).getX2();
                    y2 = dataClass.rectangles.get(i).getY2();
                    x3 = dataClass.rectangles.get(i).getX3();
                    y3 = dataClass.rectangles.get(i).getY3();
                    x4 = dataClass.rectangles.get(i).getX4();
                    y4 = dataClass.rectangles.get(i).getY4();
                }
            }

            if(rectPosition == -1 && circlePosition == -1 && linePosition == -1) {
                pendingDrag = false;
            } else {
                pendingDrag = true;
            }
        }

        if(!pendingDrag) {
            circlePosition = -1;
            linePosition = -1;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
                if (scrollView != null)
                    scrollView.requestDisallowInterceptTouchEvent(false);
                if (mode != DRAG && mode != ZOOM && mode != MOVE) {
                    detector.onTouchEvent(event);
                    return true;
                }
                mode = NONE;
                pendingDrag = false;
                moveCount = 0;
                if (scrollView != null)
                    scrollView.requestDisallowInterceptTouchEvent(false);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    oldDist = spacing(event);
                    mode = ZOOM;
                    if (scrollView != null)
                        scrollView.requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (scrollView != null)
                    scrollView.requestDisallowInterceptTouchEvent(false);
                moveCount = 0;
                break;

            case MotionEvent.ACTION_MOVE:
                moveCount++;
                if (moveCount <= 10)
                    break;

                if (pendingDrag && mode != ZOOM) {
                    if (scrollView != null)
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    Point rawImagePoint = getImagePoint(event, imageView);

                    //Drag Operation for circle
                    if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                        dataClass.circles.get(circlePosition).setX(rawImagePoint.getX());
                        dataClass.circles.get(circlePosition).setY(rawImagePoint.getY());
                        drawOverlay();
                    }

                    //Drag Operation for SmallCircles
                    else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                        dataClass.smallCircles.get(smallCirclePosition).setX(rawImagePoint.getX());
                        dataClass.smallCircles.get(smallCirclePosition).setY(rawImagePoint.getY());
                        drawOverlay();
                    }

                    //Drag Operation for lines
                    else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                        if(linePosition != -1) {
                            float newX = event.getX();
                            float newY = event.getY();

                            float deltaX = newX - start.x;
                            float deltaY = newY - start.y;
                            if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                dataClass.lines.get(linePosition).setStartx(startx);
                                dataClass.lines.get(linePosition).setStarty(starty + deltaY * 0.02f);
                                dataClass.lines.get(linePosition).setEndx(endx);
                                dataClass.lines.get(linePosition).setEndy(endy + deltaY * 0.02f);

                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            } else {
                                dataClass.lines.get(linePosition).setStartx(startx + deltaX * 0.02f);
                                dataClass.lines.get(linePosition).setStarty(starty);
                                dataClass.lines.get(linePosition).setEndx(endx + deltaX * 0.02f);
                                dataClass.lines.get(linePosition).setEndy(endy);

                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            }
                            drawOverlay();
                        }
                    }

                    //Drag Operation for Rectangles
                    else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                        if(rectPosition != -1) {
                            float newX = event.getX();
                            float newY = event.getY();

                            float deltaX = newX - start.x;
                            float deltaY = newY - start.y;
                            if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                //Motion in Y direction.
                                dataClass.rectangles.get(rectPosition).setCenterx(rawImagePoint.getX());
                                dataClass.rectangles.get(rectPosition).setCentery(rawImagePoint.getY());

                                dataClass.rectangles.get(rectPosition).setX1(x1);
                                dataClass.rectangles.get(rectPosition).setY1(y1 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX2(x2);
                                dataClass.rectangles.get(rectPosition).setY2(y2 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX3(x3);
                                dataClass.rectangles.get(rectPosition).setY3(y3 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX4(x4);
                                dataClass.rectangles.get(rectPosition).setY4(y4 + deltaY * 0.02f);

                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();

                            } else {
                                // Motion in X direction.
                                dataClass.rectangles.get(rectPosition).setCenterx(rawImagePoint.getX());
                                dataClass.rectangles.get(rectPosition).setCentery(rawImagePoint.getY());

                                dataClass.rectangles.get(rectPosition).setX1(x1 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY1(y1);
                                dataClass.rectangles.get(rectPosition).setX2(x2 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY2(y2);
                                dataClass.rectangles.get(rectPosition).setX3(x3 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY3(y3);
                                dataClass.rectangles.get(rectPosition).setX4(x4 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY4(y4);

                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();
                            }
                            drawOverlay();
                        }
                    }

                    //Drag Operation for MultiShape
                    else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
                        if(linePosition != -1) {
                            float newX = event.getX();
                            float newY = event.getY();

                            float deltaX = newX - start.x;
                            float deltaY = newY - start.y;
                            if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                dataClass.lines.get(linePosition).setStartx(startx);
                                dataClass.lines.get(linePosition).setStarty(starty + deltaY * 0.02f);
                                dataClass.lines.get(linePosition).setEndx(endx);
                                dataClass.lines.get(linePosition).setEndy(endy + deltaY * 0.02f);

                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            } else {
                                dataClass.lines.get(linePosition).setStartx(startx + deltaX * 0.02f);
                                dataClass.lines.get(linePosition).setStarty(starty);
                                dataClass.lines.get(linePosition).setEndx(endx + deltaX * 0.02f);
                                dataClass.lines.get(linePosition).setEndy(endy);

                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            }
                            drawOverlay();
                        } else if(rectPosition != -1) {
                            float newX = event.getX();
                            float newY = event.getY();

                            float deltaX = newX - start.x;
                            float deltaY = newY - start.y;
                            if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                //Motion in Y direction.
                                dataClass.rectangles.get(rectPosition).setCenterx(rawImagePoint.getX());
                                dataClass.rectangles.get(rectPosition).setCentery(rawImagePoint.getY());

                                dataClass.rectangles.get(rectPosition).setX1(x1);
                                dataClass.rectangles.get(rectPosition).setY1(y1 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX2(x2);
                                dataClass.rectangles.get(rectPosition).setY2(y2 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX3(x3);
                                dataClass.rectangles.get(rectPosition).setY3(y3 + deltaY * 0.02f);
                                dataClass.rectangles.get(rectPosition).setX4(x4);
                                dataClass.rectangles.get(rectPosition).setY4(y4 + deltaY * 0.02f);

                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();

                            } else {
                                // Motion in X direction.
                                dataClass.rectangles.get(rectPosition).setCenterx(rawImagePoint.getX());
                                dataClass.rectangles.get(rectPosition).setCentery(rawImagePoint.getY());

                                dataClass.rectangles.get(rectPosition).setX1(x1 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY1(y1);
                                dataClass.rectangles.get(rectPosition).setX2(x2 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY2(y2);
                                dataClass.rectangles.get(rectPosition).setX3(x3 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY3(y3);
                                dataClass.rectangles.get(rectPosition).setX4(x4 + deltaX * 0.02f);
                                dataClass.rectangles.get(rectPosition).setY4(y4);

                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();
                            }
                            drawOverlay();
                        } else if(circlePosition != -1) {
                            dataClass.circles.get(circlePosition).setX(rawImagePoint.getX());
                            dataClass.circles.get(circlePosition).setY(rawImagePoint.getY());
                            drawOverlay();
                        } else if(smallCirclePosition != -1) {
                            dataClass.circles.get(smallCirclePosition).setX(rawImagePoint.getX());
                            dataClass.circles.get(smallCirclePosition).setY(rawImagePoint.getY());
                            drawOverlay();
                        }
                    }
                    mode = DRAG;
                } else if (mode == ZOOM) {
                    if (scrollView != null)
                        scrollView.requestDisallowInterceptTouchEvent(true);
                    float newDist = spacing(event);
                    if (newDist == -9999999)
                        break;
                    if (newDist > oldDist) {
                        float scale = newDist / oldDist;
                        //Zoom in for Circles
                        if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                            if(circlePosition != -1) {
                                dataClass.circles.get(circlePosition).setRadius(radius + scale);
                                drawOverlay();
                            }
                        }
                        //Zoom in for SmallCircles
                        else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                            if(smallCirclePosition != -1) {
                                dataClass.smallCircles.get(smallCirclePosition).setRadius(radius + scale);
                                drawOverlay();
                            }
                        }

                        //Zoom in for Lines
                        else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                            if(linePosition != -1) {
                                float newX = event.getX();
                                float newY = event.getY();

                                float deltaX = newX - start.x;
                                float deltaY = newY - start.y;
                                if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx);
                                    dataClass.lines.get(linePosition).setEndy(endy + scale * 5f);
                                    drawOverlay();
                                } else {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx + scale * 5f);
                                    dataClass.lines.get(linePosition).setEndy(endy);
                                    drawOverlay();
                                }
                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            }
                        }

                        //Zoom in for Rectangles
                        else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                            if(rectPosition != -1) {
                                /*float newX = event.getX();
                                float newY = event.getY();

                                float deltaX = newX - start.x;
                                float deltaY = newY - start.y;
                                if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                    dataClass.rectangles.get(rectPosition).setX1(x1);
                                    dataClass.rectangles.get(rectPosition).setY1(y1);
                                    dataClass.rectangles.get(rectPosition).setX2(x2 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY2(y2);
                                    dataClass.rectangles.get(rectPosition).setX3(x3 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY3(y3 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setX4(x4);
                                    dataClass.rectangles.get(rectPosition).setY4(y4 + scale * 5f);
                                    drawOverlay();
                                } else {
                                    dataClass.rectangles.get(rectPosition).setX1(x1);
                                    dataClass.rectangles.get(rectPosition).setY1(y1);
                                    dataClass.rectangles.get(rectPosition).setX2(x2 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY2(y2);
                                    dataClass.rectangles.get(rectPosition).setX3(x3 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY3(y3);
                                    dataClass.rectangles.get(rectPosition).setX4(x4);
                                    dataClass.rectangles.get(rectPosition).setY4(y4);
                                    drawOverlay();
                                }*/
                                dataClass.rectangles.get(rectPosition).setX1(x1 + scale);
                                dataClass.rectangles.get(rectPosition).setY1(y1 + scale);
                                dataClass.rectangles.get(rectPosition).setX2(x2 + scale);
                                dataClass.rectangles.get(rectPosition).setY2(y2 + scale);
                                dataClass.rectangles.get(rectPosition).setX3(x3 + scale);
                                dataClass.rectangles.get(rectPosition).setY3(y3 + scale);
                                dataClass.rectangles.get(rectPosition).setX4(x4 + scale);
                                dataClass.rectangles.get(rectPosition).setY4(y4 + scale);
                                drawOverlay();

                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();
                            }
                        }

                        //Zoom in for multi shape
                        else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
                            if(linePosition != -1) {
                                float newX = event.getX();
                                float newY = event.getY();

                                float deltaX = newX - start.x;
                                float deltaY = newY - start.y;
                                if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx);
                                    dataClass.lines.get(linePosition).setEndy(endy + scale * 5f);
                                    drawOverlay();
                                } else {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx + scale * 5f);
                                    dataClass.lines.get(linePosition).setEndy(endy);
                                    drawOverlay();
                                }
                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            } else if(rectPosition != -1) {
                                /*float newX = event.getX();
                                float newY = event.getY();

                                float deltaX = newX - start.x;
                                float deltaY = newY - start.y;
                                if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                    dataClass.rectangles.get(rectPosition).setX1(x1);
                                    dataClass.rectangles.get(rectPosition).setY1(y1 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setX2(x2);
                                    dataClass.rectangles.get(rectPosition).setY2(y2 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setX3(x3);
                                    dataClass.rectangles.get(rectPosition).setY3(y3 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setX4(x4);
                                    dataClass.rectangles.get(rectPosition).setY4(y4 + scale * 5f);
                                    drawOverlay();
                                } else {
                                    dataClass.rectangles.get(rectPosition).setX1(x1 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY1(y1);
                                    dataClass.rectangles.get(rectPosition).setX2(x2 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY2(y2);
                                    dataClass.rectangles.get(rectPosition).setX3(x3 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY3(y3);
                                    dataClass.rectangles.get(rectPosition).setX4(x4 + scale * 5f);
                                    dataClass.rectangles.get(rectPosition).setY4(y4);
                                    drawOverlay();
                                }
                                x1 = dataClass.rectangles.get(rectPosition).getX1();
                                y1 = dataClass.rectangles.get(rectPosition).getY1();
                                x2 = dataClass.rectangles.get(rectPosition).getX2();
                                y2 = dataClass.rectangles.get(rectPosition).getY2();
                                x3 = dataClass.rectangles.get(rectPosition).getX3();
                                y3 = dataClass.rectangles.get(rectPosition).getY3();
                                x4 = dataClass.rectangles.get(rectPosition).getX4();
                                y4 = dataClass.rectangles.get(rectPosition).getY4();*/
                                drawSpecificShape(rectPosition,scale);
                            } else if(smallCirclePosition != -1) {
                                dataClass.smallCircles.get(smallCirclePosition).setRadius(radius + scale);
                                drawOverlay();
                            } else if(circlePosition != -1) {
                                dataClass.circles.get(circlePosition).setRadius(radius + scale);
                                drawOverlay();
                            }
                        }

                    } else {
                        float scale = newDist / oldDist;
                        //Zoom out for Circles
                        if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                            if(circlePosition != -1) {
                                dataClass.circles.get(circlePosition).setRadius(radius - scale);
                                drawOverlay();
                            }
                        }

                        //Zoom out for SmallCircles
                        else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                            if(circlePosition != -1) {
                                dataClass.smallCircles.get(circlePosition).setRadius(radius - scale);
                                drawOverlay();
                            }
                        }

                        //Zoom out for Rectangles
                        else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                            if(rectPosition != -1) {

                                //drawOverlay();
                            }
                        }

                        //Zoom out for Lines
                        else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                            if(linePosition != -1) {
                                float newX = event.getX();
                                float newY = event.getY();

                                float deltaX = newX - start.x;
                                float deltaY = newY - start.y;
                                if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx);
                                    dataClass.lines.get(linePosition).setEndy(endy - scale * 5f);
                                    drawOverlay();
                                } else {
                                    dataClass.lines.get(linePosition).setStartx(startx);
                                    dataClass.lines.get(linePosition).setStarty(starty);
                                    dataClass.lines.get(linePosition).setEndx(endx - scale * 5f);
                                    dataClass.lines.get(linePosition).setEndy(endy);
                                    drawOverlay();
                                }
                                startx = dataClass.lines.get(linePosition).getStartx();
                                starty = dataClass.lines.get(linePosition).getStarty();
                                endx = dataClass.lines.get(linePosition).getEndx();
                                endy = dataClass.lines.get(linePosition).getEndy();
                            }
                        }

                        //Zoom out for multi shapes
                        else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
                            if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                                if(circlePosition != -1) {
                                    dataClass.circles.get(circlePosition).setRadius(radius - scale);
                                    drawOverlay();
                                }
                            } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                                if(circlePosition != -1) {
                                    dataClass.smallCircles.get(circlePosition).setRadius(radius - scale);
                                    drawOverlay();
                                }
                            } else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                                if(rectPosition != -1) {

                                    //drawOverlay();
                                }
                            } else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                                if(linePosition != -1) {
                                    float newX = event.getX();
                                    float newY = event.getY();

                                    float deltaX = newX - start.x;
                                    float deltaY = newY - start.y;
                                    if(Math.abs(deltaY)>Math.abs(deltaX)) {
                                        dataClass.lines.get(linePosition).setStartx(startx);
                                        dataClass.lines.get(linePosition).setStarty(starty);
                                        dataClass.lines.get(linePosition).setEndx(endx);
                                        dataClass.lines.get(linePosition).setEndy(endy - scale * 5f);
                                        drawOverlay();
                                    } else {
                                        dataClass.lines.get(linePosition).setStartx(startx);
                                        dataClass.lines.get(linePosition).setStarty(starty);
                                        dataClass.lines.get(linePosition).setEndx(endx - scale * 5f);
                                        dataClass.lines.get(linePosition).setEndy(endy);
                                        drawOverlay();
                                    }
                                    startx = dataClass.lines.get(linePosition).getStartx();
                                    starty = dataClass.lines.get(linePosition).getStarty();
                                    endx = dataClass.lines.get(linePosition).getEndx();
                                    endy = dataClass.lines.get(linePosition).getEndy();
                                }
                            }
                        }
                    }
                } else {
                    mode = MOVE;
                }

                break;
        }
        return true;
    }

    private Bitmap currentBitmap;

    private void drawSpecificShape(int position, float scale) {
        currentBitmap = Bitmap.createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(currentBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(5);
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setAlpha(100);
        textPaint.setTextSize(16);
        textPaint.setColor(Color.CYAN);
        textPaint.setTextAlign(Paint.Align.CENTER);

        Rectangles rect = dataClass.rectangles.get(position);
        Path path = new Path();
        path.moveTo(rect.getX1(),rect.getY1());
        path.lineTo(rect.getX2(),rect.getY2());
        path.lineTo(rect.getX3(),rect.getY3());
        path.lineTo(rect.getX4(),rect.getY4());
        path.close();
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        path.transform(matrix);
        canvas.drawPath(path, paint);
        canvas.drawText(String.valueOf(rect.getId()), rect.getCenterx() - 3, rect.getCentery() + 20, textPaint);
    }

    private void drawPipesOnImage() {

        if (backgroundImage == null) {
            return;
        }

        if (currentBitmap != null)
            currentBitmap.recycle();

        currentBitmap = null;

        try {
            currentBitmap = Bitmap.createBitmap(backgroundImage.getWidth(), backgroundImage.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(currentBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(5);
            paint.setColor(Color.CYAN);
            paint.setStyle(Paint.Style.STROKE);

            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setAlpha(100);
            textPaint.setTextSize(16);
            textPaint.setColor(Color.CYAN);
            textPaint.setTextAlign(Paint.Align.CENTER);

            System.out.println("Rect Size "+dataClass.rectangles.size());

            if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
                for (int i = 0; i < dataClass.circles.size(); i++) {
                    Circles circle = dataClass.circles.get(i);
                    canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
                    canvas.drawText(String.valueOf(circle.getId()), circle.getX() - 3, circle.getY() + 20, textPaint);
                }
            } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
                for (int i = 0; i < dataClass.smallCircles.size(); i++) {
                    Circles circle = dataClass.smallCircles.get(i);
                    canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
                    canvas.drawText(String.valueOf(circle.getId()), circle.getX() - 3, circle.getY() + 20, textPaint);
                }
            } else if(Global.settingsManager.getShapeType() == Global.SHEET_TYPE_PIPE) {
                for (int i = 0; i < dataClass.lines.size(); i++) {
                    Lines line = dataClass.lines.get(i);
                    Path path = new Path();
                    path.moveTo(line.getStartx(),line.getStarty());
                    path.lineTo(line.getEndx(),line.getEndy());
                    path.close();
                    canvas.drawPath(path, paint);
                }
            } else if(Global.settingsManager.getShapeType() == Global.RECTANGLE_TYPE_PIPE) {
                for (int i = 0; i < dataClass.rectangles.size(); i++) {
                    Rectangles rect = dataClass.rectangles.get(i);
                    Path path = new Path();
                    path.moveTo(rect.getX1(),rect.getY1());
                    path.lineTo(rect.getX2(),rect.getY2());
                    path.lineTo(rect.getX3(),rect.getY3());
                    path.lineTo(rect.getX4(),rect.getY4());
                    path.close();
                    canvas.drawPath(path, paint);
                    canvas.drawText(String.valueOf(rect.getId()), rect.getCenterx() - 3, rect.getCentery() + 20, textPaint);
                }
            } else if(Global.settingsManager.getShapeType() == Global.MULTI_TYPE_PIPE) {
                for (int i = 0; i < dataClass.circles.size(); i++) {
                    Circles circle = dataClass.circles.get(i);
                    canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
                    canvas.drawText(String.valueOf(circle.getId()), circle.getX() - 3, circle.getY() + 20, textPaint);
                }

                for (int i = 0; i < dataClass.smallCircles.size(); i++) {
                    Circles circle = dataClass.smallCircles.get(i);
                    canvas.drawCircle(circle.getX(), circle.getY(), circle.getRadius(), paint);
                    canvas.drawText(String.valueOf(circle.getId()), circle.getX() - 3, circle.getY() + 20, textPaint);
                }

                for (int i = 0; i < dataClass.lines.size(); i++) {
                    Lines line = dataClass.lines.get(i);
                    Path path = new Path();
                    path.moveTo(line.getStartx(),line.getStarty());
                    path.lineTo(line.getEndx(),line.getEndy());
                    path.close();
                    canvas.drawPath(path, paint);
                }

                for (int i = 0; i < dataClass.rectangles.size(); i++) {
                    Rectangles rect = dataClass.rectangles.get(i);
                    Path path = new Path();
                    path.moveTo(rect.getX1(),rect.getY1());
                    path.lineTo(rect.getX2(),rect.getY2());
                    path.lineTo(rect.getX3(),rect.getY3());
                    path.lineTo(rect.getX4(),rect.getY4());
                    path.close();
                    canvas.drawPath(path, paint);
                    canvas.drawText(String.valueOf(rect.getId()), rect.getCenterx() - 3, rect.getCentery() + 20, textPaint);
                }
            }
            drawingCallback.changedOverlay();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void drawOverlay() {
        if(backgroundImage != null) {
            drawPipesOnImage();
            overlayImageView.setImageBitmap(currentBitmap);
        }
    }

    private Point getImagePoint(MotionEvent event, ImageView imageView) {
        float scaleX = (float) backgroundImage.getWidth() / imageView.getWidth();
        float scaleY = (float) backgroundImage.getHeight() / imageView.getHeight();

        float scale;
        if (!isTraining)
            scale = Math.max(scaleX, scaleY);
        else
            scale = Math.min(scaleX, scaleY);
        float deltaX = (imageView.getWidth() * scale - backgroundImage.getWidth()) / 2;
        float deltaY = (imageView.getHeight() * scale - backgroundImage.getHeight()) / 2;
        float realX = event.getX() * scale - deltaX;
        float realY = event.getY() * scale - deltaY;
        Point imagePoint = new Point((int) realX, (int) realY);
        return imagePoint;
    }

    private float spacing(MotionEvent event) {
        if (event.getPointerCount() > 1) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return (float) Math.sqrt(x * x + y * y);
        } else return -9999999;
    }

    private int createNextId() {
        int maxId = 0;
        if(Global.settingsManager.getShapeType() == Global.CIRCLE_TYPE_PIPE) {
            for (int i = 0; i < dataClass.circles.size(); i++) {
                if (dataClass.circles.get(i).getId() > maxId) {
                    maxId = dataClass.circles.get(i).getId();
                }
            }
        } else if(Global.settingsManager.getShapeType() == Global.SMALL_CIRCLE_TYPE_PIPE) {
            for (int i = 0; i < dataClass.smallCircles.size(); i++) {
                if (dataClass.smallCircles.get(i).getId() > maxId) {
                    maxId = dataClass.smallCircles.get(i).getId();
                }
            }
        } else {
            for (int i = 0; i < dataClass.rectangles.size(); i++) {
                if (dataClass.rectangles.get(i).getId() > maxId) {
                    maxId = dataClass.rectangles.get(i).getId();
                }
            }
        }
        maxId++;
        return maxId;
    }
}