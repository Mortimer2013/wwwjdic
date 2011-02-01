/**
 * 
 */
package org.nick.wwwjdic.sod;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

public class StrokePath {

    private static final String TAG = StrokePath.class.getSimpleName();

    private static final float STROKE_WIDTH = 6f;

    private PointF moveTo;
    private List<Curve> curves = new ArrayList<Curve>();

    private Paint strokePaint;
    private Paint strokeAnnotationPaint;

    public StrokePath(PointF moveTo) {
        this.moveTo = moveTo;

        strokePaint = new Paint();
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStyle(Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(STROKE_WIDTH);

        strokeAnnotationPaint = new Paint();
        strokeAnnotationPaint.setColor(Color.GREEN);
        strokeAnnotationPaint.setStyle(Style.FILL);
        strokeAnnotationPaint.setAntiAlias(true);
        strokeAnnotationPaint.setStrokeWidth(4f);
    }

    public void addCurve(Curve curve) {
        curves.add(curve);
    }

    public PointF getMoveTo() {
        return moveTo;
    }

    public List<Curve> getCurves() {
        return curves;
    }

    public void draw(final Canvas canvas, final float scale, int strokeNum,
            boolean annotate) {
        Path path = new Path();
        path.moveTo(moveTo.x, moveTo.y);
        path.setFillType(FillType.WINDING);

        PointF firstPoint = moveTo;
        PointF lastPoint = moveTo;
        PointF lastP2 = null;

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        float[] cs = new float[2];
        cs[0] = firstPoint.x;
        cs[1] = firstPoint.y;
        matrix.mapPoints(cs);

        // canvas.drawCircle(cs[0], cs[1], 2, paint);

        if (annotate) {
            String strokeNumStr = Integer.toString(strokeNum);
            canvas.drawText(strokeNumStr, cs[0] + 7, cs[1] - 9,
                    strokeAnnotationPaint);
        }

        int idx = 0;
        for (Curve c : curves) {
            PointF p1 = null;
            PointF p2 = null;
            PointF p3 = null;

            if (c.isRelative()) {
                p1 = calcAbsolute(lastPoint, c.getP1());
                p2 = calcAbsolute(lastPoint, c.getP2());
                p3 = calcAbsolute(lastPoint, c.getP3());
            } else {
                p1 = c.getP1();
                p2 = c.getP2();
                p3 = c.getP3();
            }

            if (c.isSmooth()) {
                p1 = calcReflectionRelToCurrent(lastP2, lastPoint);
            }

            path.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

            if (c.isRelative()) {
                lastP2 = new PointF(lastPoint.x + c.getP2().x, lastPoint.y
                        + c.getP2().y);
                lastPoint = new PointF(lastPoint.x + c.getP3().x, lastPoint.y
                        + c.getP3().y);
            } else {
                lastPoint = c.getP3();
                lastP2 = c.getP2();
            }
            idx++;
        }

        path.transform(matrix);

        canvas.drawPath(path, strokePaint);
    }

    private void walkPath(final Canvas canvas, Path path) {
        PathMeasure pm = new PathMeasure(path, false);
        float length = pm.getLength();
        Path segment = new Path();
        float start = 0;
        float delta = 10;

        final List<Path> segments = new ArrayList<Path>();
        while (start <= length) {
            float end = start + delta;
            if (end > length) {
                end = length;
            }
            pm.getSegment(start, end, segment, true);
            segments.add(segment);
            start += delta;
        }

        // for (Path s : segments) {
        // canvas.drawPath(s, strokePaint);
        // }

        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                case 10:
                    int last = msg.arg1;
                    for (int i = 0; i <= last; i++) {
                        Path s = segments.get(i);
                        canvas.save();
                        canvas.drawPath(s, strokePaint);
                        canvas.restore();
                    }
                    break;
                default:
                    super.handleMessage(msg);
                }
            }
        };

        Runnable animationTask = new Runnable() {
            public void run() {
                for (int i = 0; i < segments.size(); i++) {
                    Message msg = h.obtainMessage(10);
                    msg.arg1 = i;
                    h.sendMessage(msg);

                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        };
        Thread t = new Thread(animationTask);
        t.start();
    }

    private PointF calcAbsolute(PointF currentPoint, PointF p) {
        // p1 can be null for smooth curves
        if (p == null) {
            return null;
        }

        return new PointF(p.x + currentPoint.x, p.y + currentPoint.y);
    }

    private PointF calcReflectionRelToCurrent(PointF p, PointF currentPoint) {
        return new PointF((float) (2.0 * currentPoint.x - p.x),
                (float) (2.0 * currentPoint.y - p.y));
    }

    public static StrokePath parsePath(String path) {
        Log.d(TAG, "parsing " + path);

        boolean isInMoveTo = false;

        StringBuffer buff = new StringBuffer();
        Float x = null;
        Float y = null;

        PointF p1 = null;
        PointF p2 = null;
        PointF p3 = null;

        StrokePath result = null;
        boolean relative = false;
        boolean smooth = false;

        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == 'M' || c == 'm') {
                isInMoveTo = true;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                buff.append(Character.toString(c));
            }

            if (c == ',' || c == '-' || c == 'c' || c == 'C' || c == 's'
                    || c == 'S' || i == (path.length() - 1)) {
                String floatStr = buff.toString();
                // System.out.println("i: " + i);
                // System.out.println("c: " + c);
                // System.out.println("floastStr: " + floatStr);
                buff = new StringBuffer();
                if (c == '-') {
                    buff.append(c);
                }

                if ("".equals(floatStr)) {
                    continue;
                }

                float f = Float.parseFloat(floatStr);
                if (x == null) {
                    x = f;
                } else {
                    y = f;
                }
            }

            if (x != null && y != null) {
                PointF p = new PointF(x, y);
                x = null;
                y = null;

                if (isInMoveTo) {
                    result = new StrokePath(p);
                } else {
                    if (p1 == null) {
                        p1 = p;
                    } else if (p1 != null && p2 == null) {
                        p2 = p;
                    } else if (p1 != null && p2 != null && p3 == null) {
                        p3 = p;
                    }

                    if (!smooth) {
                        if (p1 != null && p2 != null && p3 != null) {
                            result.addCurve(new Curve(p1, p2, p3, relative,
                                    smooth));
                            p1 = null;
                            p2 = null;
                            p3 = null;
                        }
                    } else {
                        if (p1 != null && p2 != null) {
                            result.addCurve(new Curve(null, p1, p2, relative,
                                    smooth));
                            p1 = null;
                            p2 = null;
                            p3 = null;
                        }
                    }
                }
            }

            if (c == 'c' || c == 'C' || c == 's' || c == 'S') {
                relative = (c == 'c' || c == 's');
                smooth = (c == 's' || c == 'S');
                isInMoveTo = false;
            }
        }

        return result;
    }

    public static List<StrokePath> parseKangiVgXml(File f) {
        List<StrokePath> strokes = new ArrayList<StrokePath>();
        XmlPullParser parser = Xml.newPullParser();

        try {
            // auto-detect the encoding from the stream
            parser.setInput(new FileInputStream(f), null);
            int eventType = parser.getEventType();
            boolean done = false;

            while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                String name = null;
                switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("stroke")) {
                        String path = parser.getAttributeValue(null, "path");
                        Log.d(TAG, "parsing " + path);
                        if (path != null && !"".equals(path)) {
                            StrokePath strokePath = StrokePath.parsePath(path);
                            strokes.add(strokePath);
                        }
                    }
                    if (name.equalsIgnoreCase("kanji")) {
                        String unicode = parser.getAttributeValue(null, "id");
                        Log.d(TAG, unicode);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();

                    break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return strokes;
    }
}
