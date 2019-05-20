package br.com.franklin.ikigai_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Region;
import android.provider.MediaStore;
import android.support.v4.print.PrintHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import br.com.franklin.ikigai_app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DoodleViewManual extends View {
    //limiar para decidir se o usuário moveu o dedo o suficiente
    private static final float TOUCH_TOLERANCE = 10;
    private Bitmap bitmap; //Tela inicial
    private Canvas bitmapCanvas; //Para desenhar no bitmap
    private final Paint paintScreen; //Para desenhar na tela
    private final Paint paintLine; //Para fazer as linhas
    private final Map<Integer, Path> pathMap = new HashMap<>(); //mapa dos paths atualmente sendo desenhados
    private final  Map <Integer, Point> previousPointMap = new HashMap<>(); //mapa que armazena o último ponto em cada path
    ArrayList<Path> circulos = new ArrayList<>();
    private Path h;
    private int i1,i2;
    private float mX, mY;
    private Path mPath;
    private AlertDialog alerta;
    private AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


    ArrayList<Path> paths = new ArrayList<Path>();
    public DoodleViewManual (Context context, AttributeSet attributes){
        super (context, attributes);
        paintScreen = new Paint();
        paintLine = new Paint();
        paintLine.setAntiAlias(true); //menos tremida
        paintLine.setColor(Color.BLACK); // inicializa preto
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setStrokeWidth(5);
        paintLine.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmap.eraseColor(Color.WHITE);

    }

    public void clear (){
        circulos.clear();
        pathMap.clear();
        previousPointMap.clear();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
    }

    public void setDrawingColor (int color){
        paintLine.setColor(color);
    }
    public int getDrawingColor(){

        return paintLine.getColor();
    }
    public void setLineWidth (int width){
        paintLine.setStrokeWidth(width);
    }
    public int getLineWidth (){
        return (int) this.paintLine.getStrokeWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paintScreen);
        for (Integer key : pathMap.keySet()){
            canvas.drawPath(pathMap.get(key), paintLine);
        }
    }


    private void touchStart(float x, float y) {
        mPath = new Path();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);


        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            h.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
        h.lineTo(mX, mY);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int actionIndex = event.getActionIndex();


        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event.getX(actionIndex),
                    event.getY(actionIndex),
                    event.getPointerId(actionIndex)
            );

        } else if (action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP) {

            touchEnded(event.getPointerId(actionIndex));

        } else {
            touchMoved(event);
        }


        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                h = new Path();
                h.moveTo(x, y);
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                PointF point = new PointF(x, y);
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                circulos.add(h);
                if (circulos.size() >= 4) {
                    if (isIkigai()) {
                        builder.setMessage(getResources().getString(R.string.isIkigai))
                                .setCancelable(false)
                                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                    } else {
                        builder.setMessage(getResources().getString(R.string.isNotIkigai1) + " " + (i1 + 1) + " " + getResources().getString(R.string.isNotIkigai2) + " " + (i2 + 1))
                                .setCancelable(false)
                                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        clear();
                                    }
                                });
                    }
                    alerta = builder.create();
                    alerta.show();
                }
                invalidate();
                break;
        }
                return true;
    }


    boolean hasIntersection(int c1, int c2){
        Region clip = new Region(0, 0, getWidth(), getHeight());
        Region region1 = new Region();
        region1.setPath(circulos.get(c1), clip);
        Region region2 = new Region();
        region2.setPath(circulos.get(c2), clip);

        if (!region1.quickReject(region2) && region1.op(region2, Region.Op.INTERSECT)) {
            return true;
        }
        return false;
    }
//

    public boolean isIkigai(){
        i1 = 0;
        i2 = 0;
        if(hasIntersection(0,1)){
            if(hasIntersection(0,2)){
                if(hasIntersection(0,3)){
                    if(hasIntersection(1,0)){
                        if(hasIntersection(1,2)){
                            if(hasIntersection(1,3)){
                                if(hasIntersection(2,0)){
                                    if(hasIntersection(2,1)){
                                        if(hasIntersection(2,3)){
                                            if(hasIntersection(3,0)){
                                                if(hasIntersection(3,1)){
                                                    if(hasIntersection(3,2)){
                                                        return true;
                                                    }
                                                    else{
                                                        i1 = 3;
                                                        i2 = 0;
                                                        return false;
                                                    }
                                                }
                                                else{
                                                    i1 = 3;
                                                    i2 = 1;
                                                    return false;
                                                }
                                            }
                                            else{
                                                i1 = 3;
                                                i2 = 0;
                                                return false;
                                            }
                                        }
                                        else{
                                            i1 = 2;
                                            i2 = 3;
                                            return false;
                                        }
                                    }
                                    else{
                                        i1 = 2;
                                        i2 = 1;
                                        return false;
                                    }
                                }
                                else{
                                    i1 = 2;
                                    i2 = 0;
                                    return false;
                                }
                            }
                            else{
                                i1 = 1;
                                i2 = 3;
                                return false;
                            }
                        }
                        else{
                            i1 = 1;
                            i2 = 2;
                            return false;
                        }
                    }
                    else{
                        i1 = 1;
                        i2 = 0;
                        return false;
                    }
                }
                else{
                    i1 = 0;
                    i2 = 3;
                    return false;
                }
            }
            else{
                i1 = 0;
                i2 = 2;
                return false;
            }
        }
        i1 = 0;
        i2 = 1;
        return false;
    }

   private void touchStarted (float x, float y, int lineId){
        Path path;
        Point point;
        if (pathMap.containsKey(lineId)) {
            path = pathMap.get(lineId);
            path.reset();
            ;
            point = previousPointMap.get(lineId);

        }
        else{
            path = new Path();
            pathMap.put(lineId, path);
            point = new Point();
            previousPointMap.put(lineId, point);

        }
        path.moveTo(x, y);
        point.x = (int) x;
        point.y = (int) y;
    }
    private void touchMoved (MotionEvent event) {
        for (int i = 0; i < event.getPointerCount(); i++) {
            int pointerID = event.getPointerId(i);                //numero persistente
            int pointerIndex = event.findPointerIndex(pointerID); //numero nao persistente
            if (pathMap.containsKey(pointerID)) {                 //Sera que ja tem alguem associado?
                float newX = event.getX(pointerIndex);
                float newY = event.getY(pointerIndex);
                Path path = pathMap.get(pointerID);             //Pega o Path associado ao pointId
                Point point = previousPointMap.get(pointerID);
                float deltaX = Math.abs(newX - point.x);
                float deltaY = Math.abs(newY - point.y);
                if (deltaX >= TOUCH_TOLERANCE || deltaY >= TOUCH_TOLERANCE) {
                    path.quadTo(point.x, point.y,
                            (newX + point.x) / 2,
                            (newY + point.y) / 2);
                    point.x = (int) newX;
                    point.y = (int) newY;
                }
            }
        }
    }

    private void  touchEnded(int lineID){
        Path path = pathMap.get(lineID);

        bitmapCanvas.drawPath(path, paintLine);
        path.reset();
    }

    public void saveImage(){
        final String name = "Doodlz-" + System.currentTimeMillis() + ".jpg";
        String location =
                MediaStore.
                        Images.Media.insertImage(
                        getContext().getContentResolver(),
                        bitmap,
                        name,
                        "Um desenho feito no Doodlz"
                );
        Toast message = null;
        if (location != null){
            message =
                    Toast.makeText(getContext(),
                            R.string.message_saved,
                            Toast.LENGTH_SHORT
                    );
        }
        else{
            message =
                    Toast.makeText(getContext(),
                            R.string.message_error_saving,
                            Toast.LENGTH_SHORT
                    );
        }
        message.setGravity(
                Gravity.CENTER,
                message.getXOffset() / 2,
                message.getYOffset() / 2);
        message.show();
    }

    public void printImage(){
        if (PrintHelper.systemSupportsPrint()){
            PrintHelper printHelper = new PrintHelper(getContext());
            printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            printHelper.printBitmap("Doodlz Image", bitmap);
        }
        else{
            Toast message =
                    Toast.makeText(getContext(),
                            R.string.message_error_printing,
                            Toast.LENGTH_SHORT
                    );
            message.setGravity(
                    Gravity.CENTER,
                    message.getXOffset() / 2,
                    message.getYOffset() / 2);
            message.show();
        }
    }

}
