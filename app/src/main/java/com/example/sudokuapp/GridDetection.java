package com.example.sudokuapp;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class GridDetection {
    public MatOfPoint2f getBiggest() {
        return biggest;
    }

    public void setBiggest(MatOfPoint2f biggest) {
        this.biggest = biggest;
    }

    public List<MatOfPoint> getContours() {
        return contours;
    }

    public void setContours(List<MatOfPoint> contours) {
        this.contours = contours;
    }

    public Mat getGrayMat() {
        return grayMat;
    }

    public void setGrayMat(Mat grayMat) {
        this.grayMat = grayMat;
    }

    public Mat getBlur1() {
        return blur1;
    }

    public void setBlur1(Mat blur1) {
        this.blur1 = blur1;
    }

    public Mat getThresh() {
        return thresh;
    }

    public void setThresh(Mat thresh) {
        this.thresh = thresh;
    }

    public Mat getHier() {
        return hier;
    }

    public void setHier(Mat hier) {
        this.hier = hier;
    }

    public double getMaxArea() {
        return maxArea;
    }

    public void setMaxArea(double maxArea) {
        this.maxArea = maxArea;
    }
    private MatOfPoint2f biggest;
    private List<MatOfPoint> contours;
    private Mat grayMat;
    private Mat blur1;
    private Mat thresh;
    private Mat hier;
    private double maxArea;
    private Mat displayMat;
    private Point[] points;

    public Point[] getPoints() {
        return points;
    }

    public void setPoints(Point[] points) {
        this.points = points;
    }

    public Mat getDisplayMat() {
        return displayMat;
    }

    public void setDisplayMat(Mat displayMat) {
        this.displayMat = displayMat;
    }

    public GridDetection() {
        this.grayMat = new Mat();
        this.blur1 = new Mat();
        this.thresh = new Mat();
        this.hier = new Mat();
        this.maxArea = 0;
        this.contours = new ArrayList<>();
        this.biggest = new MatOfPoint2f();
        this.displayMat = new Mat();
    }

    public void convertImageToGrayscale(Mat grayMat, Mat blur1, Mat currentMat) {
        Imgproc.cvtColor(currentMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayMat, blur1, new Size(5, 5), 0);
    }

    public void adaptiveThreshold() {
        Imgproc.adaptiveThreshold(blur1, thresh, 255, 1, 1, 11, 2);
    }
    public void findContours() {
        Imgproc.findContours(thresh, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    }

    public void LoopContours() {
        for (MatOfPoint one : contours) {
            double area = Imgproc.contourArea(one);
            if (area > 100) {
                MatOfPoint2f curve = new MatOfPoint2f(one.toArray());
                double peri = Imgproc.arcLength(curve, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(curve, approx, 0.02 * peri, true);
                if (area > maxArea && approx.total() == 4) {
                    biggest = approx;
                    maxArea = area;
                }
            }
        }
    }
}
