package com.edu.nju.tree.treemeasure.Activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.edu.nju.tree.treemeasure.Fragment.Camera2BasicFragment;
import com.edu.nju.tree.treemeasure.R;
import com.edu.nju.tree.treemeasure.utils.ImageProcess;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;

    private Mat mat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        Button button = (Button)findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                tutorial();
//            }
//        });
        //整个项目只有一个activity，启动以后都是activity上fragment之间的切换
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2BasicFragment.newInstance())
                    .commit();
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void tutorial(){

        Log.i("---------------", "before image process");

        InputStream is = null;
        try {

            is = getAssets().open("tree1.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            System.out.println(bitmap.getWidth() + "  "+ bitmap.getHeight());
            double treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("1   " + treewidth);

            is = getAssets().open("tree2.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                    bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                    bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("2   " + treewidth);

            is = getAssets().open("tree3.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                    bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                    bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("3   " + treewidth);

            is = getAssets().open("tree4.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                    bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                    bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("4   " + treewidth);

            is = getAssets().open("tree5.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                    bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                    bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("5   " + treewidth);

            is = getAssets().open("tree6.jpg");
            bitmap = BitmapFactory.decodeStream(is);
            bitmap = Bitmap.createBitmap(bitmap,
                    bitmap.getWidth()/5, bitmap.getHeight()/4+50,
                    bitmap.getWidth()/5*3, bitmap.getHeight()/2);
            treewidth = ImageProcess.treeWidth(bitmap);
            System.out.println("6   " + treewidth);

        } catch (IOException e) {
            e.printStackTrace();
        }


//
//        Bitmap roiMap = Bitmap.createBitmap(bitmap,
//                bitmap.getWidth()/5*2, bitmap.getHeight()/10*3,
//                bitmap.getWidth()/5, bitmap.getHeight()/10*4);

//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        bitmap = Bitmap.createBitmap(roiMap, 0, 0, roiMap.getWidth(), roiMap.getHeight(), matrix, true);




//        Mat treeWidth = Process.treeWidth(bitmap);
//
//        Log.i("----------treewidth: ", String.valueOf(treeWidth));
//
//        Bitmap bmp= null;
//        bmp =Bitmap.createBitmap( treeWidth.width(),  treeWidth.height(),  Bitmap.Config.ARGB_8888);
//
//        Utils.matToBitmap(treeWidth, bmp);
//
//        mImageView = (ImageView) findViewById(R.id.image);
//        mImageView.setImageBitmap(bmp);


    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
}
