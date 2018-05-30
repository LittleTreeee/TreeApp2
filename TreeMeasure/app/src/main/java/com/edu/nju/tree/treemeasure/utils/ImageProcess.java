package com.edu.nju.tree.treemeasure.utils;

import android.graphics.Bitmap;

import com.edu.nju.tree.treemeasure.Error.NoRedPointsException;
import com.edu.nju.tree.treemeasure.Error.TooMuchRedPointsException;

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

    //在scope范围内算作一致的
    private static int scope = 50;

    private static double realDistance = 10;

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
    public static double treeWidth(Bitmap bitmap){

        Mat src = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1,new Scalar(4));

        Utils.bitmapToMat(bitmap, src);

        Mat hsvImage = new Mat(src.rows(), src.cols(), CvType.CV_8UC1, new Scalar(4));
        Imgproc.cvtColor(src, hsvImage, 41);

        return calculate(hsvImage);

    }

    /**
     * 实际计算函数
     * @param mat
     * @return
     */
    private static double calculate(Mat mat){
        List<TargetArea> areas = new ArrayList<>();
        //把获得的所有点分类
        for ( int i = 0; i<mat.rows(); i++ ){
            for ( int j = 0; j<mat.cols(); j++ ){
                //红色点
                double[] hsv = mat.get(i,j);
                if ( hsv[0] >= 156 && hsv[0] <= 180 && hsv[1] > 150 && hsv[2] > 200 ){
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

        // 检查红色的光点是否只有两个
        if ( points.size() < 2 ){
            throw new NoRedPointsException();
        }else if ( points.size() > 2 ){
            throw new TooMuchRedPointsException();
        }

        int bottom = mat.rows();
        int num = 8;

        //中间这条线是不应该出现红色点的
        int center = (points.get(0).x + points.get(1).x)/2;

        int top1_x = 0;
        int count  = 0;
        for ( int j = 0; j<mat.cols(); j++ ){
            for ( int i = 0; i<center; i++ ){
                double[] hsv = mat.get(i,j);
                if ( hsv[0] >= 156 && hsv[0] <= 180 && hsv[1] > 100 && hsv[2] > 100 ){
                    top1_x += j;
                    count++;
                }
            }
            if ( count >= num ){
                top1_x /= count;
                break;
            }
        }

        int top2_x = 0;
        count  = 0;
        for ( int j = mat.cols()-1; j>=0; j-- ){
            for ( int i = 0; i<center; i++ ){
                double[] hsv = mat.get(i,j);
                if ( hsv[0] >= 156 && hsv[0] <= 180 && hsv[1] > 100 && hsv[2] > 100 ){
                    top2_x += j;
                    count++;
                }
            }
            if ( count >= num ){
                top2_x /= count;
                break;
            }
        }

        int bottom1_x = 0;
        count  = 0;
        for ( int j = 0; j<mat.cols(); j++ ){
            //找num个左边的点
            for ( int i = center; i<bottom; i++ ){
                double[] hsv = mat.get(i,j);
                if ( hsv[0] >= 156 && hsv[0] <= 180 && hsv[1] > 100 && hsv[2] > 100 ){
                    bottom1_x += j;
                    count++;
                }
            }
            if ( count >= num ){
                bottom1_x /= count;
                break;
            }
        }

        int bottom2_x = 0;
        count  = 0;
        for ( int j = mat.cols()-1; j>=0; j-- ){
            //找num个左边的点
            for ( int i = center; i<bottom; i++ ){
                double[] hsv = mat.get(i,j);
                if ( hsv[0] >= 156 && hsv[0] <= 180 && hsv[1] > 100 && hsv[2] > 100 ){
                    bottom2_x += j;
                    count++;
                }
            }
            if ( count >= num ){
                bottom2_x /= count;
                break;
            }
        }

        double treePixel = ( top2_x - top1_x + bottom2_x - bottom1_x )/2;

        double rPixel = Math.sqrt( (points.get(0).x-points.get(1).x)*(points.get(0).x-points.get(1).x)
                + (points.get(0).y - points.get(1).y) * (points.get(0).y - points.get(1).y));

        double treelength = (treePixel*1.0 / rPixel) * realDistance;

        return treelength;

    }

}
