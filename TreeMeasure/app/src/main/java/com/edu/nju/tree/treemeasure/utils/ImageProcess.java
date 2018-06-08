package com.edu.nju.tree.treemeasure.utils;

import android.graphics.Bitmap;

import com.edu.nju.tree.treemeasure.Error.NoRedPointsException;
import com.edu.nju.tree.treemeasure.Error.TooMuchRedPointsException;
import com.edu.nju.tree.treemeasure.Error.WrongSizeImage;

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

    //在scope范围内算作一致的
    private static int scope = 2;

    private static double realDistance = 9.9;

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

        int[] topx = getTopX(mat, 300, 500);

        List<TargetArea> areas = new ArrayList<>();
        //把获得的所有点分类
        for ( int j = topx[0]; j<=topx[1]; j++ ){
            for ( int i = 300; i<500; i++ ){
                //红色点
                double[] hsv = mat.get(i,j);
                if ( hsv[1] < 70 && hsv[2] > 240 ){
                    boolean inserted = false;      //是否被放到一个区域中
                    for ( int k = 0; k<areas.size(); k++ ){
                        if ( i <= areas.get(k).maxX+scope && i >= areas.get(k).minX-scope &&
                                j <= areas.get(k).maxY+scope && j >=areas.get(k).minY-scope){

                            //点在区域内，加入区域
                            Spot spot = new Spot(i, j, 0);
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
            if ( spots.size() <= 40 )
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
            spot.nums = spots.size();

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
                if ( points.get(i).y < center && points.get(i).nums > leftPoint.nums){
                    leftPoint = points.get(i);
                }
                if ( points.get(i).y > center && points.get(i).nums > rightPoint.nums){
                    rightPoint = points.get(i);
                }
            }
        }

        int center = (leftPoint.x + rightPoint.x)/2;
        int top = center-50 < 0? 0:center-50;
        int bottom = center+50 > mat.rows()? mat.rows():center+50;
        topx = getTopX(mat, top, bottom);

        double treePixel = topx[1] - topx[0];

        double rPixel = rightPoint.y - leftPoint.y;

        double treelength = (treePixel*1.0 / rPixel) * realDistance * 1.02;

        return treelength;

    }

    private static int[] getTopX(Mat mat, int left, int right){
        int starti = left;
        int endi = right;

        int top1_x = 0;
        for ( int j = mat.cols()/2; j > 0; j-=2 ) {
            int count = 0;
            for (int i = starti; i < endi; i++) {
                double[] hsv = mat.get(i, j);
                double[] hsv2 = mat.get(i, j - 1);
                if (((hsv[0] >= 156 && hsv[0] <= 180) || hsv[0] < 10) && hsv[1] > 80 && hsv[2] > 80
                        ){
                    count++;
                    break;
                }
            }
            if (count == 0) {
                top1_x = j;

                break;
            }
        }

        int top2_x = 0;
        for ( int j = mat.cols()/2; j < mat.cols(); j+=2 ){
            int count  = 0;

            for ( int i = starti; i<endi; i++ ){
                double[] hsv = mat.get(i,j);
                double[] hsv2 = mat.get(i, j+1);
                if ( ( (hsv[0] >= 156 && hsv[0] <= 180) || hsv[0] < 10 )  && hsv[1] > 80 && hsv[2] > 80
                        ){
                    count++;
                    break;
                }
            }

            if ( count == 0 ) {
                top2_x = j;
                break;
            }

        }
        if ( top2_x == 0 )
            top2_x = mat.cols();

        int[] re = {top1_x, top2_x};

        return re;
    }

    public static Mat getMat(Bitmap bitmap){
        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, src);

        Imgproc.GaussianBlur( src, src, new Size(5,5), 0,0);

        Mat mat = new Mat(src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(4));
        Imgproc.cvtColor(src, mat, 41);

        if ( mat.rows() != 800 || mat.cols() != 1500  ){
            throw  new WrongSizeImage();
        }

        int[] topx = getTopX(mat, 300, 500);

        List<TargetArea> areas = new ArrayList<>();
        //把获得的所有点分类
        for ( int j = topx[0]; j<topx[1]; j++ ){
            for ( int i = 300; i<500; i++ ){
                //红色点
                double[] hsv = mat.get(i,j);
                if ( hsv[1] < 70 && hsv[2] > 240 ){
                    boolean inserted = false;      //是否被放到一个区域中
                    for ( int k = 0; k<areas.size(); k++ ){
                        if ( i <= areas.get(k).maxX+scope && i >= areas.get(k).minX-scope &&
                                j <= areas.get(k).maxY+scope && j >=areas.get(k).minY-scope){

                            //点在区域内，加入区域
                            Spot spot = new Spot(i, j, 0);
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

        System.out.println(" mat2 areas size "+ areas.size());


        List<Spot> points = new ArrayList<>();
        for ( int i = 0; i<areas.size(); i++){

            List<Spot> spots = areas.get(i).spots;
            int sumx = 0;
            int sumy = 0;
            if ( spots.size() <= 40 )
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
            spot.nums = spots.size();

            points.add(spot);

        }

        System.out.println("mat2 points size "+ points.size());

        if ( points.size() < 2 )
            throw new NoRedPointsException();

        //找左右两个点
        Spot leftPoint = points.get(0);
        Spot rightPoint = points.get(points.size()-1);
        if ( points.size() > 2 ){
            int center = mat.cols()/2;
            for ( int i = 1; i < points.size()-1; i++ ){
                if ( points.get(i).y < center && points.get(i).nums > leftPoint.nums){
                    leftPoint = points.get(i);
                }
                if ( points.get(i).y > center && points.get(i).nums > rightPoint.nums){
                    rightPoint = points.get(i);
                }
            }
        }

        int starti = (leftPoint.x+rightPoint.x)/2 - 50;
        int endi = starti+100;

        for ( int i = 0; i<mat.rows(); i++) {
            mat.put(i,leftPoint.y, 255,255,255);
            mat.put(i,rightPoint.y, 255,255,255);
        }

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
                for ( int i = 0; i<mat.rows(); i++) {
                    mat.put(i,j, 255,255,255);
                }
                break;
            }

        }

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
                for ( int i = 0; i<mat.rows(); i++) {
                    mat.put(i,j, 255,255,255);
                }
                break;
            }

        }

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_HSV2BGR);

        return mat;


    }

    public static Mat getMat2(Bitmap bitmap){
        Mat re = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, re);

        Imgproc.GaussianBlur( re, re, new Size(5,5), 0,0);

        Mat hsvImage = new Mat(re.rows(), re.cols(), CvType.CV_8UC1, new Scalar(4));
        Imgproc.cvtColor(re, hsvImage, 41);

        for ( int j = 0; j < re.cols(); j++ ) {
            for (int i = 0; i < re.rows(); i++) {
                double H, S, V = 0;
                H = hsvImage.get(i, j)[0];
                S = hsvImage.get(i, j)[1];
                V = hsvImage.get(i, j)[2];


                if ( ((H >= 156 && H <= 180) || H < 10) && S > 80 && V > 80) {
                    hsvImage.put(i, j, 255,255,255);
                } else if ( S < 70 &&  V > 240 ){
                    hsvImage.put(i, j, 100, 255, 100);
                } else {
                    hsvImage.put(i, j, 0, 0, 0);
                }
            }
        }

        return hsvImage;
    }

}
