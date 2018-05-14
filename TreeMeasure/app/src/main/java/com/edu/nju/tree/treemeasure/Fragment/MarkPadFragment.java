package com.edu.nju.tree.treemeasure.Fragment;

import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.edu.nju.tree.treemeasure.R;
import com.edu.nju.tree.treemeasure.utils.ImageProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * todo 负责进行标注的界面
 * 直接按手机上的返回键就可以返回到上一个界面
 *
 */
public class MarkPadFragment extends Fragment {
    private Context mContext;
    private ImageView imageView;
    private Button mBtnOK, mBtnClear, mBtnCancel;
    private byte[] bytes;
    private Bitmap bitmap;
    private Bitmap photo;
    private Canvas canvas; //画布
    private Paint paint; //画笔
    private Matrix matrix; //矩阵，空间变换
    private float downX = 0, downY=0, upX=0, upY=0; //定义按下和停止位置（x,y）坐标
    private BitmapFactory.Options options;
    private int right = 0;
    private int bottom = 0;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mark, null);
        imageView = view.findViewById(R.id.iv);

        // 获取屏幕尺寸
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        int screenWidth = mDisplayMetrics.widthPixels;
        int screenHeight = mDisplayMetrics.heightPixels;

        //之前Camera2BasicFragment传过来的图片
        bytes = (byte[])getArguments().get("bytes");
        if(bytes!=null) {
            options = new BitmapFactory.Options();
            photo =BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        mContext = getContext();

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
        matrix = new Matrix();
        matrix.setRotate(90);
        photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        bitmap = Bitmap.createBitmap(screenWidth,screenHeight, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        double widthRatio = screenWidth/(photo.getWidth()+0.0);
        double heightRatio = screenHeight/(photo.getHeight()+0.0);
        double photoRatio = photo.getWidth()/(photo.getHeight()+0.0);
        if(widthRatio<heightRatio && widthRatio<1){
            right = screenWidth;
            bottom = (int)(screenHeight*photoRatio);
        }else if(heightRatio<widthRatio && heightRatio<1){
            right = (int)(screenWidth/heightRatio);
            bottom = screenHeight;
        }else{
            right = photo.getWidth();
            bottom = photo.getHeight();
        }
        canvas.drawBitmap(photo,null,new Rect(0,0,right,bottom),null);
        imageView.setImageBitmap(bitmap);

        mBtnOK = (Button) view.findViewById(R.id.write_pad_ok);
        mBtnClear = (Button) view.findViewById(R.id.write_pad_clear);
        mBtnCancel = (Button) view.findViewById(R.id.write_pad_cancel);

        mBtnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 保存
//                saveImage();
                Toast.makeText(mContext, "开始计算...", Toast.LENGTH_SHORT).show();

                double treeWidth = ImageProcess.treeWidth(bitmap);

                BigDecimal bg = new BigDecimal(treeWidth);
                Toast.makeText(mContext, "胸径: "+ bg.setScale(2), Toast.LENGTH_LONG).show();
                

            }
        });

        mBtnClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 清除
                Paint p = new Paint();
                //清屏
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawPaint(p);
                p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
                canvas.drawBitmap(photo,null,new Rect(0,0,right,bottom),null);
                Toast.makeText(mContext, "清除", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 取消
//                getActivity().getSupportFragmentManager()
//                        .beginTransaction()
//                        .replace(R.id.container, new Camera2BasicFragment(), null)
//                        .commit();
                getActivity().getSupportFragmentManager().popBackStack();
                Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                // 判断不同状态
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // 按下时记下坐标
                        downX = event.getX();
                        downY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 移动过程中不断绘制line
                        upX = event.getX();
                        upY = event.getY();
                        canvas.drawLine(downX, downY, upX, upY, paint);
                        imageView.invalidate();
                        downX = upX;
                        downY = upY;
                        break;
                    case MotionEvent.ACTION_UP:
                        // 停止时记录坐标
                        upX = event.getX();
                        upY = event.getY();
                        canvas.drawLine(downX, downY, upX, upY, paint);
                        imageView.invalidate();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        break;
                    default:
                        break;
                }
                //返回true表示，一旦事件开始就要继续接受触摸事件
                return true;
            }
        });

        return view;
    }

    /**
     * @param
     * @param bitmap 对象
     * @param w 要缩放的宽度
     * @param h 要缩放的高度
     * @return newBmp 新 Bitmap对象
     */
    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBmp = Bitmap.createBitmap(bitmap, 0, 0, width, height,
                matrix, true);
        return newBmp;
    }

    private void saveImage(){
        FileOutputStream output = null;

        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US);

        String fname = "IMG_" +
                sdf.format(new Date())
                + ".jpg";
        File mFile = new File(getActivity().getApplication().getExternalFilesDir(null), fname);


        Log.d("ImagePath", mFile.toString());

        try {
            output = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 计算 BitmapFactpry 的 inSimpleSize的值的方法
    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {
        if (reqWidth == 0 || reqHeight == 0) {
            return 1;
        }

        // 获取图片原生的宽和高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        // 如果原生的宽高大于请求的宽高,那么将原生的宽和高都置为原来的一半
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 主要计算逻辑
            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}