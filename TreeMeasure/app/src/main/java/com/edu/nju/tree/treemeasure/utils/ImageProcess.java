package com.edu.nju.tree.treemeasure.utils;

import android.graphics.Bitmap;

import com.edu.nju.tree.treemeasure.Error.NoRedPointsException;
import com.edu.nju.tree.treemeasure.Error.TooMuchRedPointsException;
import com.edu.nju.tree.treemeasure.Error.WrongSizeImage;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loick on 2018/5/7.
 */

public class ImageProcess {

    //在scope范围内算作一致的
    private static int scope = 30;

    private static double realDistance = 9.75;

    // debug image
    private static Bitmap bitmap;
    public static Bitmap getBitMap() {
        return bitmap;
    }

    private static boolean debug = true;

    /**
     * 动态设定实际距离
     * @param realDistance
     */
    public static void setRealDistance(double realDistance) {
        ImageProcess.realDistance = realDistance;
    }

    /**
     * 计算入口
     * @param bitmap
     * @return
     */
    public static double treeWidth(Bitmap bitmap) throws RuntimeException{

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, src);

        Imgproc.GaussianBlur( src, src, new Size(5,5), 0,0);

        Mat hsvImage = new Mat(src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(4));
        Imgproc.cvtColor(src, hsvImage, 41);

        return calculate(hsvImage);

    }

    /**
     * 实际计算函数
     * @param mat
     * @return
     */
    private static double calculate(Mat mat) throws RuntimeException{

        if ( mat.rows() != 800 || mat.cols() != 1500  ){
            throw  new WrongSizeImage();
        }

        List<TargetArea> areas = new ArrayList<>();
        //把获得的所有点分类
        for ( int j = 500; j<1000; j++ ){
            for ( int i = 300; i<500; i++ ){
                //红色点
                double[] hsv = mat.get(i,j);
                if ( hsv[1] < 70 && hsv[2] > 240 ){
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

        System.out.println("areas size "+ areas.size());


        List<Spot> points = new ArrayList<>();
        for ( int i = 0; i<areas.size(); i++){

            List<Spot> spots = areas.get(i).spots;
            int sumx = 0;
            int sumy = 0;
            if ( spots.size() <= 10 )
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

            points.add(spot);

        }

        if ( points.size() < 2 )
            throw new NoRedPointsException();

        //找左右两个点
        Spot leftPoint = points.get(0);
        Spot rightPoint = points.get(points.size()-1);
        if ( points.size() > 2 ){
            int center = mat.cols()/2;
            for ( int i = 1; i < points.size()-1; i++ ){
                if ( points.get(i).y < center ){
                    leftPoint = leftPoint.y > points.get(i).y? leftPoint:points.get(i);
                }
                if ( points.get(i).y > center ){
                    rightPoint = rightPoint.y < points.get(i).y? rightPoint:points.get(i);
                }
            }
        }

        // debug image
        //
        if ( debug )
            for ( int i = 0; i<mat.rows(); i++) {
                mat.put(i,leftPoint.y, 100,255,100);
                mat.put(i,rightPoint.y, 100,255,100);
            }

        int starti = (leftPoint.x+rightPoint.x)/2 - 50;
        int endi = starti+100;

        int top1_x = 0;
        for ( int j = leftPoint.y-1; j > 0; j-=2 ){
            int count  = 0;

            for ( int i = starti; i<endi; i++ ){
                double[] hsv = mat.get(i,j);
                double[] hsv2 = mat.get(i, j-1);
                if ( ( (hsv[0] >= 156 && hsv[0] <= 180) || hsv[0] < 10 ) && hsv[1] > 80 && hsv[2] > 80
                        || ( (hsv2[0] >= 156 && hsv2[0] <= 180) || hsv2[0] < 10 ) && hsv2[1] > 80 && hsv2[2] > 80 ){
                    count++;
                    break;
                }
            }

            if ( count == 0 ) {
                top1_x = j;
                //
                if ( debug )
                    for ( int i = 0; i<mat.rows(); i++) {
                        mat.put(i,j, 100,255,100);
                    }
                break;
            }

        }

        int top2_x = 0;
        for ( int j = rightPoint.y+1; j < mat.cols(); j+=2 ){
            int count  = 0;

            for ( int i = starti; i<endi; i++ ){
                double[] hsv = mat.get(i,j);
                double[] hsv2 = mat.get(i, j+1);
                if ( ( (hsv[0] >= 156 && hsv[0] <= 180) || hsv[0] < 10 )  && hsv[1] > 80 && hsv[2] > 80
                        || ( (hsv2[0] >= 156 && hsv2[0] <= 180) || hsv2[0] < 10 ) && hsv2[1] > 80 && hsv2[2] > 80 ){
                    count++;
                    break;
                }
            }

            if ( count == 0 ) {
                top2_x = j;
                //
                if ( debug )
                    for ( int i = 0; i<mat.rows(); i++) {
                        mat.put(i,j, 100,255,100);
                    }
                break;
            }

        }

        // debug image
        if ( debug )
            Utils.matToBitmap(mat, bitmap);

        double treePixel = top2_x - top1_x;

        double rPixel = rightPoint.y - leftPoint.y;

        double treelength = (treePixel*1.0 / rPixel) * realDistance * 1.02;

        return treelength;

    }

}
