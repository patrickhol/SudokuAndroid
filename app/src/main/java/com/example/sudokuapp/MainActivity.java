package com.example.sudokuapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {


    private static final String uriString = "content://media/internal/images/media";
    private Bitmap currentBitmap;
    private Bitmap originalBitmap;
    private Mat originalMat, currentMat;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV Status", "OpenCV loaded successfully");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::onClickLoadImage);

        FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab1.setOnClickListener(this::onClickSudokuGridDetection);

        FloatingActionButton fab2 = findViewById(R.id.fab2);
        fab2.setOnClickListener(this::onClickSudokuSolve);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int idActionBar = item.getItemId();
        if (idActionBar == R.id.action_settings) {
            return true;
        } else if (idActionBar == R.id.open_gallery) onClickLoadImage(null);
        else if (idActionBar == R.id.SGD) {
            SudokuGridDetection();
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadImageToImageView() {
        ImageView imgView = findViewById(R.id.image_view);
        imgView.setImageBitmap(currentBitmap);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap temp = reduceImage(picturePath);

            int orientation = 0;
            try {
                orientation = new ExifInterface(picturePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            } catch (IOException e) {
                e.printStackTrace();
            }


            Matrix rotate90 = new Matrix();
            rotate90(temp, orientation, rotate90);
            convertBitmapToMat();
            currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            loadImageToImageView();
        }
    }

    private Bitmap reduceImage(String picturePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        return BitmapFactory.decodeFile(picturePath, options);
    }

    private void convertBitmapToMat() {
        Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        originalMat = new Mat(tempBitmap.getHeight() / 2, tempBitmap.getWidth() / 2, CvType.CV_32F);
        Utils.bitmapToMat(tempBitmap, originalMat);
    }

    private void rotate90(Bitmap temp, int orientation, Matrix rotate90) {
        rotate90.postRotate(orientation);
        originalBitmap = rotateBitmap(temp, orientation);
    }


    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }

    }

    public void SudokuGridDetection() {
        GridDetection gridDetection = new GridDetection();
        gridDetection.convertImageToGrayscale(gridDetection.getGrayMat(), gridDetection.getBlur1(), originalMat);
        gridDetection.adaptiveThreshold();
        gridDetection.findContours();
        gridDetection.LoopContours();
        gridDetection.setDisplayMat(originalMat);
        gridDetection.setPoints(gridDetection.getBiggest().toArray());

        if (gridDetection.getPoints().length >= 4) {
            perspectiveTransformation(gridDetection.getDisplayMat(), gridDetection.getPoints());
        }


    }


    private void startSudokuSolverProcess() {
        Mat grayMat = new Mat();
        Mat blur1 = new Mat();
        convertImageToGrayscale(grayMat, blur1, currentMat);
        Mat thresh = new Mat();
        Mat tresh2 = new Mat();
        Imgproc.adaptiveThreshold(blur1, thresh, 255, 1, 1, 15, 15);
        makeGridForOCR(thresh);
        Imgproc.threshold(thresh, tresh2, 2, 255, Imgproc.THRESH_BINARY_INV);
        Mat emptyWhitePlaceForOCR = new Mat(158, 158, CvType.CV_8UC1, new Scalar(255));
        Sudoku ocrString = new Sudoku("");
        cutAndReadAllCell(tresh2, emptyWhitePlaceForOCR, ocrString);
        String[][] sudokuGridMutation = fillGridChar0();
        sudokuGridMutation = readLineFindNumber(sudokuGridMutation, ocrString);
        int[][] sudokuPrint = startSudokuSolver(sudokuGridMutation);
        addSolveToThePhoto(tresh2, sudokuPrint, sudokuGridMutation);
        displayImage(currentMat);
    }

    private String[][] fillGridChar0() {
        String[][] sudokuGridMutation = new String[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sudokuGridMutation[j][i] = "0";
            }
        }
        return sudokuGridMutation;
    }

    private void convertImageToGrayscale(Mat grayMat, Mat blur1, Mat currentMat) {
        Imgproc.cvtColor(currentMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayMat, blur1, new Size(5, 5), 0);
    }

    private int[][] startSudokuSolver(String[][] table) {
        SudokuSolver sudokuSolver = new SudokuSolver(table);
        int[][] sudokuPrint = sudokuSolver.getBoadrIntToSolve();
        if (sudokuSolver.solveSudokuRecursive()) {
            System.out.println("SUDOKU SOLVED");
        } else {
            System.out.println("NOT SOLVED");
        }
        return sudokuPrint;
    }

    private void addSolveToThePhoto(Mat tresh2, int[][] sudokuPrint, String[][] table) {
        for (int j = 0; j < 9; j++) {
            for (int i = 0; i < 9; i++) {
                if (table[i][j] == "0") {
                    int x = i != 0 ? (tresh2.width() / 9) * i : 0;
                    int y = j != 0 ? (tresh2.height() / 9) * j : 0;
                    Imgproc.putText(currentMat, Integer.toString(sudokuPrint[i][j]), new Point(x + 15, y + 40), Core.FONT_HERSHEY_DUPLEX, 1, new Scalar(255, 0, 0));

                }
            }
        }
    }

    private String[][] readLineFindNumber(String[][] table, Sudoku ocrString) {
        Scanner scanner = new Scanner(ocrString.getOcrString());
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String lineRead = scanner.nextLine();
                String lineTransform = lineRead.replaceAll("\\D+", "").trim();
                if (lineTransform.length() == 0) {
                    continue;
                } else {
                    table[j][i] = lineTransform;

                }
            }
        }
        scanner.close();
        return table;
    }

    private void cutAndReadAllCell(Mat tresh2, Mat emptyWhitePlaceForOCR, Sudoku ocrString) {
        Rect RR;
        Rect roi;
        Mat cutCell;
        boolean read = false;
        for (int j = 0; j < 9; j++) {
            for (int i = 0; i < 9; i++) {
                int x = i != 0 ? (tresh2.width() / 9) * i : 0;
                int y = j != 0 ? (tresh2.height() / 9) * j : 0;
                RR = new Rect(new Point(x, y), new Point((tresh2.width() / 9) * (i + 1), (tresh2.height() / 9) * (j + 1)));
                cutCell = new Mat(tresh2, RR);
                double heightDiv = cutCell.height() / 3;
                double widthDiv = cutCell.width() / 3;

                for (int h = 0; h < cutCell.height(); h++) {
                    for (int l = 0; l < cutCell.width(); l++) {
                        double[] data = cutCell.get(h, l);

                        boolean blackPixelInside = h > heightDiv && l > widthDiv && h < (heightDiv * 2) && l < (widthDiv * 2) && data[0] == 0;
                        if (blackPixelInside) {
                            read = true;
                            break;

                        }
                    }
                }

                if (read) {
                    int heightCorrection = 12;
                    Imgproc.putText(emptyWhitePlaceForOCR, "tak", new Point(10, cutCell.height() - heightCorrection), Core.FONT_HERSHEY_DUPLEX, 1, new Scalar(0));
                    roi = new Rect(70, 0, cutCell.width(), cutCell.height());
                    cutCell.copyTo(new Mat(emptyWhitePlaceForOCR, roi));
                    // TODO Cut
                    readOcrCell(emptyWhitePlaceForOCR, ocrString);
                    read = false;
                } else {
                    // String for OCR
                    ocrString.setOcrString(ocrString.getOcrString() + "tak \n");
                }

            }
        }
    }


    private Mat makeGridForOCR(Mat thresh) {
        for (int i = 0; i <= 9; i++) {
            Imgproc.line(thresh, new Point((thresh.width() / 9) * i, 0), new Point((thresh.width() / 9) * i, thresh.height()), new Scalar(0, 0, 0), 15);
        }
        for (int i = 0; i <= 9; i++) {
            Imgproc.line(thresh, new Point(0, (thresh.height() / 9) * i), new Point(thresh.width(), (thresh.height() / 9) * i), new Scalar(0, 0, 0), 15);
        }
        return thresh;
    }


    private void perspectiveTransformation(Mat displayMat, Point[] points) {
        double xTopLeft = points[0].x;
        double xBottomLeft = points[1].x;
        double xTopRight = points[2].x;
        double xBottomRight = points[3].x;
        double yTopLeft = points[0].y;
        double yBottomLeft = points[1].y;
        double yBottomRight = points[2].y;
        double yTopRight = points[3].y;


        double top = Math.sqrt(Math.pow(xTopRight - xTopLeft, 2) + Math.pow(yTopRight - yTopLeft, 2));
        double right = Math.sqrt(Math.pow(xTopRight - xBottomRight, 2) + Math.pow(yBottomRight - yTopRight, 2));
        double bottom = Math.sqrt(Math.pow(xBottomRight - xBottomLeft, 2) + Math.pow(yBottomRight - yBottomLeft, 2));
        double left = Math.sqrt(Math.pow(xBottomLeft - xTopLeft, 2) + Math.pow(yBottomLeft - yTopLeft, 2));


        Mat quad = Mat.zeros(new Size(Math.max(top, bottom), Math.max(right, left)), CvType.CV_8UC3);

        ArrayList<Point> result_pts = new ArrayList<>();
        result_pts.add(new Point(0, 0));
        result_pts.add(new Point(quad.cols(), 0));
        result_pts.add(new Point(quad.cols(), quad.rows()));
        result_pts.add(new Point(0, quad.rows()));

        ArrayList<Point> corners = new ArrayList<>();

        corners.add(new Point(points[0].x, points[0].y));
        corners.add(new Point(points[3].x, points[3].y));
        corners.add(new Point(points[2].x, points[2].y));
        corners.add(new Point(points[1].x, points[1].y));


        Mat cornerPts = Converters.vector_Point2f_to_Mat(corners);
        Mat resultPts = Converters.vector_Point2f_to_Mat(result_pts);

        Mat transformation = Imgproc.getPerspectiveTransform(cornerPts, resultPts);
        Imgproc.warpPerspective(displayMat, quad, transformation, quad.size());

        currentMat = quad;
        displayImage(quad);
    }

    private void readOcrCell(Mat col, Sudoku ocrString) {
        Bitmap bitMap = convertToBitmap(col, Bitmap.Config.RGB_565);
        Frame frame = new Frame.Builder().setBitmap(bitMap).build();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        SparseArray<TextBlock> textblock = textRecognizer.detect(frame);
        TextBlock tb = null;
        List<Text> texto = new ArrayList<>();
        for (int i = 0; i < textblock.size(); i++) {
            tb = textblock.get(textblock.keyAt(i));
            texto.addAll(tb.getComponents());
        }
        for (Text t : texto) {
            for (Text t2 : t.getComponents()) {
                ocrString.setOcrString(ocrString.getOcrString() + t2.getValue() + " ");

            }

        }
        ocrString.setOcrString(ocrString.getOcrString() + "\n");
    }


    private void displayImage(Mat image) {
        Bitmap bitMap = convertToBitmap(image, Bitmap.Config.ARGB_8888);
        findTheImageAndDraw(bitMap);
    }

    private void findTheImageAndDraw(Bitmap bitMap) {
        ImageView iv = findViewById(R.id.image_view);
        iv.setImageBitmap(bitMap);
        currentBitmap = bitMap;
    }

    private Bitmap convertToBitmap(Mat image, Bitmap.Config argb8888) {
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(), argb8888);
        Utils.matToBitmap(image, bitMap);
        return bitMap;
    }

    private void onClickSudokuSolve(View view) {
        if (originalBitmap == null) {
            Context context = getApplicationContext();
            CharSequence text = "You need to load an image first!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        } else {
            startSudokuSolverProcess();
        }
    }


    private void onClickSudokuGridDetection(View view) {
        if (originalBitmap == null) {
            Context context = getApplicationContext();
            CharSequence text = "You need to load an image first!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

        } else {
            SudokuGridDetection();
        }
    }

    private void onClickLoadImage(View viev) {
        {
            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse(uriString));
            startActivityForResult(intent, 0);
        }
    }
}
