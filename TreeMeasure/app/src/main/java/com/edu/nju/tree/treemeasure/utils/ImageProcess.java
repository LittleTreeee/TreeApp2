package com.edu.nju.tree.treemeasure.utils;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loick on 2018/5/7.
 */

public class ImageProcess {

    private static final int scope = 20;

    private static int redcolor = 255;
    private static int bluecolor = 100;

    private static double realDistance = 10.75;

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
                if ( mat.get(i, j)[0] == color ){
                    boolean inserted = false;      //是否被放到一个区域中
                    for ( int k = 0; k<areas.size(); k++ ){
                        if ( i <= areas.get(k).maxX+scope && i >= areas.get(k).minX-scope &&
                                j <= areas.get(k).maxY+scope && j >=areas.get(k).minY-scope){

                            //点在区域内，加入区域
                            Spot spot = new Spot(i, j);
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
            if ( spots.size() <= 5 )
                continue;
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
     * @param hsvImage
     * @return
     */
    public static double[] redDistance(Mat hsvImage){

        double[] distance = new double[2];

        long time1 = System.currentTimeMillis();

        for ( int i = 0; i<hsvImage.rows(); i++ ){
            for ( int j = 0; j<hsvImage.cols(); j++ ){
                double H, S, V = 0;
                H = hsvImage.get(i, j)[0];
                S = hsvImage.get(i, j)[1];
                V = hsvImage.get(i, j)[2];

                //红色
                if ( H>=156 && H<=180 &&  S > 100 && V > 200 ){
                    hsvImage.put(i, j, 255.0,255.0,255.0);
                }
                //蓝色
                else if ( H>=100 && H<=124 && S>200 && V > 200 ){
                    hsvImage.put(i, j, 100.0,255.0,255.0);
                }
                //其他
                else{
                    hsvImage.put(i, j, 0.0,0.0,0.0);
                }
            }
        }
        long time2 = System.currentTimeMillis();
        Log.i("*******times", String.valueOf(time2-time1));


        // 计算红色点的距离
        List<Spot> pointRed = getPoints(hsvImage, redcolor);
        Log.i("-----redpoint ", String.valueOf(pointRed.size()));

        Spot red1 = pointRed.get(0);
        Spot red2 = pointRed.get(1);
//        int ROW = hsvImage.rows();
//        for ( int i = 0; i<pointRed.size(); i++ ){
//            if ( (pointRed.get(i).x)<(ROW/2) && ((pointRed.get(i).x) > (ROW/2 * 0.8) )){
//                red1.y = (pointRed.get(i).y);
//                red1.x = pointRed.get(i).x;
//            }
//            if ( (pointRed.get(i).x)>(ROW/2) && (pointRed.get(i).x) < (ROW/2 * 1.2) ){
//                red2.y = (pointRed.get(i).y);
//                red2.x = pointRed.get(i).x;
//            }
//        }
        distance[0] = Math.sqrt( (red1.x - red2.x)*(red1.x - red2.x)
                + (red1.y - red2.y)*(red1.y - red2.y) );


        // 蓝色两条线距离
        List<Spot> pointBlue = getPoints(hsvImage, bluecolor);

        int maxindex = 0;
        for ( int i = 1; i<3; i++ ){
            if ( pointBlue.get(i).x > pointBlue.get(maxindex).x )
                maxindex = i;
        }
        pointBlue.remove(maxindex);

        Spot blue1 = pointBlue.get(0);
        Spot blue2 = pointBlue.get(1);

        distance[1] = Math.sqrt( (blue1.x - blue2.x)*(blue1.x - blue2.x)
                + (blue1.y - blue2.y)*(blue1.y - blue2.y) );


        return distance;


    }


    public static double treeWidth(Bitmap bitmap){

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Log.i("---------size--", String.valueOf(src.size()));

        Mat hsvImage = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));
        Utils.bitmapToMat(bitmap, src);

        Mat roi = new Mat(src, new Rect(src.cols()/4, 0, src.cols()/2, src.rows()));

        Log.i("---------size--", String.valueOf(roi.size()));


        Mat resize = new Mat();
        Imgproc.resize(roi, resize, new Size(roi.width()/2, roi.height()/2));
        Imgproc.cvtColor(resize, hsvImage, 41);

        Log.i("---------size--", String.valueOf(hsvImage.size()));


        long time1 = System.currentTimeMillis();

        double[] distance = redDistance(hsvImage);

        double treeWidth = distance[1]/distance[0] * realDistance;

        long time2 = System.currentTimeMillis();
        Log.i("*******times", String.valueOf(time2-time1));

        return treeWidth;
    }

}
