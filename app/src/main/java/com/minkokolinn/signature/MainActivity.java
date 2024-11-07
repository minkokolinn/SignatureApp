package com.minkokolinn.signature;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btnClick,btnClear,btnCancel,btnSave;
    Dialog dialog;
    LinearLayout linzz;
    View mainView;
    Signature msignature;
    Bitmap bitmap;

    File file;
    String DIRECTORY=Environment.getExternalStorageDirectory().getPath()+"/TMDigitSign/";
    String pic_name=new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault()).format(new Date());
    String StoredPath=DIRECTORY+pic_name+".png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        file=new File(DIRECTORY);
        if (!file.exists()){
            file.mkdir();
        }

        dialog=new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        findViewById(R.id.btn_click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_action();
            }
        });
    }

    public void dialog_action(){

        linzz=(LinearLayout)dialog.findViewById(R.id.zzz);

        msignature=new Signature(MainActivity.this,null);
        msignature.setBackgroundColor(Color.WHITE);


        linzz.addView(msignature, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

        btnClear=(Button)dialog.findViewById(R.id.btn_clear_am);
        btnSave=(Button)dialog.findViewById(R.id.btn_save_am);
        btnSave.setEnabled(false);
        btnCancel=(Button)dialog.findViewById(R.id.btn_cancel_am);
        mainView=linzz;

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msignature.clear();
                btnSave.setEnabled(false);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setDrawingCacheEnabled(true);
                msignature.Save(mainView,StoredPath);
                dialog.dismiss();
                Toast.makeText(MainActivity.this, "successfully Save", Toast.LENGTH_SHORT).show();
                MainActivity.this.recreate();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                MainActivity.this.recreate();
            }
        });

        dialog.show();

    }

    public class Signature extends View{
        private static final float STROKE_WIDTH=5f;
        private static final float HALF_STROKE_WIDTH=STROKE_WIDTH/2;
        private Paint paint=new Paint();
        private Path path=new Path();

        private float lastTouchX;
        private float lastTochY;
        private final RectF dirtyRect=new RectF();

        public Signature(Context context,AttributeSet attrs) {
            super(context,attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void Save(View v,String StoredPath){
            Log.v("log_tag","Width: "+v.getWidth());
            Log.v("log_tag","Height: "+v.getHeight());
            if (bitmap==null){
                bitmap=Bitmap.createBitmap(linzz.getWidth(),linzz.getHeight(),Bitmap.Config.RGB_565);
            }
            Canvas canvas=new Canvas(bitmap);
            try {
                FileOutputStream mFileOutStream=new FileOutputStream(StoredPath);
                v.draw(canvas);

                bitmap.compress(Bitmap.CompressFormat.PNG,90,mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
            }catch (Exception e){
                Log.v("log_tag",e.toString());
            }
        }
        public void clear(){
            paint.reset();
            invalidate();
        }

        protected void onDraw(Canvas canvas){
            canvas.drawPath(path,paint);
        }

        public boolean onTouchEvent(MotionEvent event){
            float eventX=event.getX();
            float eventY=event.getY();
            btnSave.setEnabled(true);

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX,eventY);
                    lastTouchX=eventX;
                    lastTochY=eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX,eventY);
                    int histroySize=event.getHistorySize();
                    for (int i=0;i<histroySize;i++){
                        float historicalX=event.getHistoricalX(i);
                        float historicalY=event.getHistoricalY(i);
                        expandDirtyRect(historicalX,historicalY);
                        path.lineTo(historicalX,historicalY);
                    }
                    path.lineTo(eventX,eventY);
                    break;

                    default:
                        debug("Ignored touch event: "+event.toString());
                        return false;
            }
            invalidate(
                    (int)(dirtyRect.left-HALF_STROKE_WIDTH),
                    (int)(dirtyRect.top-HALF_STROKE_WIDTH),
                    (int)(dirtyRect.right+HALF_STROKE_WIDTH),
                    (int)(dirtyRect.bottom+HALF_STROKE_WIDTH)
            );

            lastTouchX=eventX;
            lastTochY=eventY;

            return  true;

        }

        private void debug(String string){
            Log.v("log_tag",string);
        }

        private void expandDirtyRect(float historicalX,float historicalY){
            if (historicalX<dirtyRect.left){
                dirtyRect.left=historicalX;
            }else if (historicalX>dirtyRect.right){
                dirtyRect.right=historicalX;
            }

            if (historicalY<dirtyRect.top){
                dirtyRect.top=historicalY;
            }else if (historicalY>dirtyRect.bottom){
                dirtyRect.bottom=historicalY;
            }
        }

        private void resetDirtyRect(float eventX,float eventY){
            dirtyRect.left=Math.min(lastTouchX,eventX);
            dirtyRect.right=Math.max(lastTouchX,eventX);
            dirtyRect.top=Math.min(lastTochY,eventY);
            dirtyRect.bottom=Math.max(lastTochY,eventY);

        }


    }


}
