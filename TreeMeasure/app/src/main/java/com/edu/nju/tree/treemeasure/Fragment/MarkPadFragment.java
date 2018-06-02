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
    private Button mBtnOK,mBtnCancel;
    private byte[] bytes;
    private Bitmap photo;
    private Matrix matrix; //矩阵，空间变换
    private BitmapFactory.Options options;
    private int right = 0;
    private int bottom = 0;
    //饱和度最高,对比度和亮度最低
    //饱和度, 为0时表示灰度图，为1表示饱和度不变，大于1显示为过饱和
    private float mSaturationValue = 0F;
    //亮度
    private float mLumValue = 1F;
    //色相
    private float mHueValue = 0F;

    //对比度和亮度最低


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
        matrix = new Matrix();
        matrix.setRotate(90);
        photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

//        double widthRatio = screenWidth/(photo.getWidth()+0.0);
//        double heightRatio = screenHeight/(photo.getHeight()+0.0);
//        double photoRatio = photo.getWidth()/(photo.getHeight()+0.0);
//        if(widthRatio<heightRatio && widthRatio<1){
//            right = screenWidth;
//            bottom = (int)(screenHeight*photoRatio);
//        }else if(heightRatio<widthRatio && heightRatio<1){
//            right = (int)(screenWidth/heightRatio);
//            bottom = screenHeight;
//        }else{
//            right = photo.getWidth();
//            bottom = photo.getHeight();
//        }
        //修改饱和度
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(2);

        //亮度
//        colorMatrix.setScale(0,0,0,1);
        //色相
//        colorMatrix.setRotate(0,90);
//        Bitmap finalBitmap = Bitmap.createBitmap(photo, photo.getWidth()/5*2, photo.getHeight()/10*3,
//                photo.getWidth()/5, photo.getHeight()/10*4);
        Bitmap finalBitmap = Bitmap.createBitmap(photo,
                photo.getWidth()/10*3, photo.getHeight()/5*2,
                photo.getWidth()/10*4, photo.getHeight()/5);
        Canvas canvas = new Canvas(finalBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(finalBitmap, 0,0, paint);
//        imageView.setImageBitmap(photo);
        //todo 这里的finalBitmap是最后改完饱和度的图片
        imageView.setImageBitmap(finalBitmap);


        mBtnOK = (Button) view.findViewById(R.id.write_pad_ok);
        mBtnCancel = (Button) view.findViewById(R.id.write_pad_cancel);

        mBtnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 保存
//                saveImage();
                Toast.makeText(mContext, "开始计算...", Toast.LENGTH_LONG).show();

//                double treeWidth = ImageProcess.treeWidth(photo);
                double treeWidth = 0;

                BigDecimal bg = new BigDecimal(treeWidth);
                treeWidth = bg.setScale(2, BigDecimal.ROUND_UP).doubleValue();


                Toast.makeText(mContext, "胸径: "+ treeWidth, Toast.LENGTH_LONG).show();
                

            }
        });


        mBtnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 取消
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, Camera2BasicFragment.newInstance(), null)
                        .commit();
//                getActivity().getSupportFragmentManager().popBackStack();
                Toast.makeText(mContext, "取消", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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
            photo.compress(Bitmap.CompressFormat.JPEG, 100, output);
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

}