package com.edu.nju.tree.treemeasure.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loick on 2018/5/7.
 */

public class ImageProcess {

    private static final int scope = 20;

    private static int redcolor = 0;
    private static int bluecolor = 1;

    private static double realDistance = 1;

    /**
     * 将mat中不同区域缩为一个点，返回
     * @param mat, 只有（0，0，0）和（255，255，255）两种点，（255，255，255）是感兴趣的，处理的点
     * @param color
     * @return
     */
    private static List<Spot> getPoints(Mat mat, int color){
        List<TargetArea> areas = new ArrayList<>();

        //把获得的所有点分类
        for ( int i = 0; i<mat.rows(); i++ ){
            for ( int j = 0; j<mat.cols(); j++ ){
                if ( mat.get(i, j)[0] != 0 ){
                    boolean inserted = false;      //是否被放到一个区域中
                    for ( int k = 0; k<areas.size(); k++ ){
                        if ( i <= areas.get(k).maxX+scope && i >= areas.get(k).minX-scope &&
                                j <= areas.get(k).maxY+scope && j >=areas.get(k).minY-scope){

                            //点在区域内，加入区域
                            Spot spot = new Spot();
                            spot.x = i;
                            spot.y = j;
                            areas.get(k).spots.add(spot);
                            inserted = true;

                            //更新区域的范围
                            areas.get(k).maxX = i > areas.get(k).maxX? i:areas.get(k).maxX;
                            areas.get(k).minX = i < areas.get(k).minX? i:areas.get(k).minX;

                            areas.get(k).maxY = j > areas.get(k).minY? j:areas.get(k).maxY;
                            areas.get(k).minY = j < areas.get(k).minY? j:areas.get(k).minY;

                        }
                    }
                    if ( !inserted ){
                        TargetArea area = new TargetArea();
                        Spot spot = new Spot();
                        spot.x = i;
                        spot.y = j;

                        area.maxY = area.minY = j;
                        area.maxX = area.minX = i;
                        area.spots.add(spot);

                        areas.add(area);
                    }
                }
            }
        }

        //把区域缩成一个点,红蓝不一样
        if ( color == bluecolor ){
            List<TargetArea> blueAreas = new ArrayList<>();
            int indexs[] = {-1, -1, -1};
            for ( int i = 0; i<3; i++ ){
                int maxsize = 0;
                int index = 0;
                for ( int j = 0; j<areas.size(); j++ ) {
                    if (areas.get(j).spots.size() > maxsize && j != indexs[0] && j != indexs[1]) {
                        maxsize = areas.get(j).spots.size();
                        index = j;
                        indexs[i] = j;
                    }
                }

                blueAreas.add(areas.get(index));

            }

            areas = blueAreas;
        }


        List<Spot> resultSpots = new ArrayList<>();
        for ( int i = 0; i<areas.size(); i++){

            List<Spot> spots = areas.get(i).spots;
            int sumx = 0;
            int sumy = 0;
            for ( int j = 0; j<spots.size(); j++){
                sumx += spots.get(j).x;
                sumy += spots.get(j).y;
            }
            int meanx = sumx / spots.size();
            int meany = sumy / spots.size();

            Spot spot = new Spot();
            spot.x = meanx;
            spot.y = meany;

            resultSpots.add(spot);

        }

        return resultSpots;

    }

    /**
     * 得到两个红点之间的距离
     * @param bitmap
     * @return
     */
    public static double redDistance(Bitmap bitmap){

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Mat hsvImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, src);

        Imgproc.cvtColor(src, hsvImage, 41);

        for ( int i = 0; i<hsvImage.rows(); i++ ){
            for ( int j = 0; j<hsvImage.cols(); j++ ){
                double H, S, V = 0;
                H = hsvImage.get(i, j)[0];
                S = hsvImage.get(i, j)[1];
                V = hsvImage.get(i, j)[2];

                if ( H>=156 && H<=180 &&  S > 100 && V > 200 ){
                    hsvImage.put(i, j, 255.0,255.0,255.0);

                }else{
                    hsvImage.put(i, j, 0.0,0.0,0.0);
                }
            }
        }

        List<Spot> pointRed = getPoints(hsvImage, redcolor);

        Spot red1 = new Spot();
        Spot red2 = new Spot();
        int ROW = hsvImage.rows();
        int COL = hsvImage.cols();
        for ( int i = 0; i<pointRed.size(); i++ ){
            if ( (pointRed.get(i).x)<(ROW/2) && ((pointRed.get(i).x) > (ROW/2 * 0.8)
                    && (pointRed.get(i).y)<(COL/2*1.2) && (pointRed.get(i).y) > (COL/2 * 0.8) )){
                red1.y = (pointRed.get(i).y);
                red1.x = pointRed.get(i).x;
            }
            if ( (pointRed.get(i).x)>(ROW/2) && (pointRed.get(i).x) < (ROW/2 * 1.2)
                    && (pointRed.get(i).y)<(COL/2*1.2) && (pointRed.get(i).y) > (COL/2 * 0.8)){
                red2.y = (pointRed.get(i).y);
                red2.x = pointRed.get(i).x;
            }
        }

        Log.i("-------*****---------", String.valueOf(red1.x) + " "+ String.valueOf(red1.y));
        Log.i("-------*****---------", String.valueOf(red2.x) + " "+ String.valueOf(red2.y));
        double distance = Math.sqrt( (red1.x - red2.x)*(red1.x - red2.x)
                + (red1.y - red2.y)*(red1.y - red2.y) );

        Log.i("------------", String.valueOf(distance));

        return distance;


    }

    public static double blueDistance(Bitmap bitmap){

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Mat hsvImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, src);

        Imgproc.cvtColor(src, hsvImage, 41);

        for ( int i = 0; i<hsvImage.rows(); i++ ){
            for ( int j = 0; j<hsvImage.cols(); j++ ){
                double H, S, V = 0;
                H = hsvImage.get(i, j)[0];
                S = hsvImage.get(i, j)[1];
                V = hsvImage.get(i, j)[2];

                if ( H>=100 && H<=124 && S>200 && V > 200 ){
                    hsvImage.put(i, j, 255.0,255.0,255.0);

                }else{
                    hsvImage.put(i, j, 0.0,0.0,0.0);
                }
            }
        }

        List<Spot> pointBlue = getPoints(hsvImage, redcolor);

        int maxindex = 0;
        for ( int i = 1; i<3; i++ ){
            if ( pointBlue.get(i).x > pointBlue.get(maxindex).x )
                maxindex = i;
        }
        pointBlue.remove(maxindex);

        Spot blue1 = pointBlue.get(0);
        Spot blue2 = pointBlue.get(1);

        Log.i("-------blue1---------", String.valueOf(blue1.x) + " "+ String.valueOf(blue1.y));
        Log.i("-------blue2---------", String.valueOf(blue2.x) + " "+ String.valueOf(blue2.y));
        double distance = Math.sqrt( (blue1.x - blue2.x)*(blue1.x - blue2.x)
                + (blue1.y - blue2.y)*(blue1.y - blue2.y) );

        Log.i("--------blueDistance", String.valueOf(distance));

        return distance;

    }

    public static double treeWidth(Bitmap bitmap){
        double redDistance = redDistance(bitmap);

        double blueDistance = blueDistance(bitmap);

        double treeWidth = blueDistance/redDistance * realDistance;

        return treeWidth;
    }

}
