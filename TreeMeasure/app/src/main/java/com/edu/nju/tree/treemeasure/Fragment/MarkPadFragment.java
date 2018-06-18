package com.edu.nju.tree.treemeasure.Fragment;

import android.app.AlertDialog;
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

import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.spec.ECField;
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
    private ImageView debugView;        // 显示处理过的图片
    private ImageView debugView2;        // 显示处理过的图片
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
        debugView = view.findViewById(R.id.debugImage);
        mBtnOK = (Button) view.findViewById(R.id.write_pad_ok);
        mBtnCancel = (Button) view.findViewById(R.id.write_pad_cancel);
        debugView2 = view.findViewById(R.id.debugImage2);

        // 获取屏幕尺寸
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

        //之前Camera2BasicFragment传过来的图片
        bytes = (byte[])getArguments().get("bytes");
        if(bytes!=null) {
            options = new BitmapFactory.Options();
            photo =BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

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

        mContext = getContext();
        matrix = new Matrix();
        matrix.setRotate(90);
        photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

        // 显示的图片
        final Bitmap finalBitmap = Bitmap.createBitmap(photo, 750, 1700 , 1500, 800);
        imageView.setImageBitmap(finalBitmap);

        mBtnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                //todo 保存
//                saveImage(finalBitmap);
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Toast.makeText(mContext, "开始计算...", Toast.LENGTH_LONG).show();

                double treeWidth = 0;
                long time1 = System.currentTimeMillis();
                Bitmap copy1 = finalBitmap.copy(finalBitmap.getConfig(), true);
                Bitmap copy2 = finalBitmap.copy(finalBitmap.getConfig(), true);
                Bitmap copy2_re = Bitmap.createBitmap(copy2, 0, 300, copy2.getWidth(), 200);


                try {
                    treeWidth = ImageProcess.treeWidth(finalBitmap);
                }catch (RuntimeException e){
                    new AlertDialog.Builder(mContext).setTitle("treewidth "+e.getMessage()).setPositiveButton("确定",null).show();
                }

                //保存图片
                Canvas canvas = new Canvas(photo);
                Paint p = new Paint();
                p.setColor(Color.RED);
                p.setTextSize(150);
                canvas.drawText(treeWidth+"",50,150,p);
                saveImage(photo);

                try{
                    Utils.matToBitmap(ImageProcess.getMat(copy1), copy1);
                    debugView.setImageBitmap(copy1);
                }catch (RuntimeException e){
                    new AlertDialog.Builder(mContext).setTitle("Mat1 " + e.getMessage()).setPositiveButton("确定",null).show();
                }

                // 显示处理过的图片
                Utils.matToBitmap(ImageProcess.getMat2(copy2_re), copy2_re);
                debugView2.setImageBitmap(copy2_re);

                long time2 = System.currentTimeMillis();


                BigDecimal bg = new BigDecimal(treeWidth);
                treeWidth = bg.setScale(2, BigDecimal.ROUND_UP).doubleValue();

                new AlertDialog.Builder(mContext).setTitle("计算结果")
                        .setMessage("胸径： "+treeWidth + "  "+ "时间： "+(time2-time1) )
                        .setPositiveButton("确定",null)
                        .show();



            }
        });


        mBtnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //todo 给图片打上标签后保存
//                Bitmap tempBitmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(photo);
                Paint p = new Paint();
                p.setColor(Color.RED);
                p.setTextSize(150);
//                canvas.drawBitmap(photo, 0, 0, null);
                canvas.drawText("无效",50,150,p);
                saveImage(photo);

                //todo 返回到拍照界面
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


    private void saveImage(Bitmap bitmap){
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
                    Log.d("outputSuccess", mFile.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}