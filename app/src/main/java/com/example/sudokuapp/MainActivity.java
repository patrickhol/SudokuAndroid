package com.example.sudokuapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private final String uriString = "content://media/internal/images/media";
    private Bitmap currentBitmap;
    private Bitmap originalBitmap;

    //TODO OCR
    ImageView image;
    TextView text;

    private static final String TAG = "MyActivity";

    private Bitmap resizedBitmap;
    private Mat originalMat;
    private Mat cropped, resizedStart;
    // TODO make all comments on the methods


    // TODO Clean code
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

        //TODO OCR
        image = (ImageView) findViewById(R.id.ocr_image_view);
        text = (TextView) findViewById(R.id.ocr_text_view);
        //TODO OCR


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::onClickLoadImage);

        FloatingActionButton fab1 = findViewById(R.id.fab1);

        fab1.setOnClickListener(this::onClickSudokuGridDetection);


    }

    //TODO OCR
    public void getTextFromImage(View view) {
        Bitmap bitmap = currentBitmap;
        Context context = getApplicationContext();
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        if (!textRecognizer.isOperational()|| originalBitmap == null) {
            Toast.makeText(context, "You need to load an image first! OR Cloud not get the Number", Toast.LENGTH_SHORT).show();
        } else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> items = textRecognizer.detect(frame);

            StringBuilder numberToShow = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                TextBlock myItem = items.valueAt(i);
                numberToShow.append(myItem.getValue());
                numberToShow.append("");

            }

            // Display In the window
            Toast.makeText(context, numberToShow.toString(), Toast.LENGTH_LONG).show();

            // Display in place of text
            text.setText(numberToShow.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.open_gallery) onClickLoadImage(null);
        else if (id == R.id.SGD) {
            SudokuGridDetection();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadImageToImageView() {
        ImageView imgView = (ImageView) findViewById(R.id.image_view);
        imgView.setImageBitmap(currentBitmap);

    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("LensActivity", requestCode + " " + data + " " + resultCode + " " + RESULT_OK);
        if (requestCode == 0 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            // String picturePath contains the path of selected Image

            //To speed up loading of image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            Bitmap temp = BitmapFactory.decodeFile(picturePath, options);
            // TODO używać jak najmnniejszy TRY  w  Catch opisać dlaczego
            //Get orientation information
            int orientation = 0;
            try {
                ExifInterface imgParams = new ExifInterface(picturePath);
                orientation = imgParams.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //Rotating the image to get the correct orientation
            Matrix rotate90 = new Matrix();
            rotate90.postRotate(orientation);
            originalBitmap = rotateBitmap(temp, orientation);

            //Convert Bitmap to Mat
            Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            originalMat = new Mat(tempBitmap.getHeight() / 2, tempBitmap.getWidth() / 2, CvType.CV_32F);
            Utils.bitmapToMat(tempBitmap, originalMat);

            //TODO cut Log
            Log.v(TAG, " currentBitmap  ");
            currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            loadImageToImageView();
        }
    }

    //Function to rotate bitmap according to image parameters
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
        Mat grayMat = new Mat();
        Mat blur1 = new Mat();

        // TODO Clean Code
        //Converting the image to grayscale
        Imgproc.cvtColor(originalMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(grayMat, blur1, new Size(5, 5), 0);

        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(blur1, thresh, 255, 1, 1, 11, 2);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hier = new Mat();
        Imgproc.findContours(thresh, contours, hier, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        hier.release();

        MatOfPoint2f biggest = new MatOfPoint2f();
        double max_area = 0;
        for (MatOfPoint i : contours) {
            double area = Imgproc.contourArea(i);
            if (area > 100) {
                MatOfPoint2f m = new MatOfPoint2f(i.toArray());
                double peri = Imgproc.arcLength(m, true);
                MatOfPoint2f approx = new MatOfPoint2f();
                Imgproc.approxPolyDP(m, approx, 0.02 * peri, true);
                if (area > max_area && approx.total() == 4) {
                    biggest = approx;
                    max_area = area;
                }
            }
        }
        Mat displayMat = originalMat;
        Point[] points = biggest.toArray();

        // TODO delete Log
        Log.v(TAG, "width"+String.valueOf(displayMat.width()));
        Log.v(TAG, "height"+String.valueOf(displayMat.height()));
        if (points.length >= 4) {
            // draw the outer box

            Imgproc.circle(displayMat, new Point(points[0].x, points[0].y), 2,new Scalar(0, 255, 0),10);
            Imgproc.circle(displayMat, new Point(points[1].x, points[1].y), 2,new Scalar(0, 255, 0),10);
            Imgproc.circle(displayMat, new Point(points[2].x, points[2].y), 2,new Scalar(0, 255, 0),10);
            Imgproc.circle(displayMat, new Point(points[3].x, points[3].y), 2,new Scalar(0, 255, 0),10);

            // crop the image
            Rect R = new Rect(new Point(points[0].x  , points[0].y  ), new Point(points[2].x  , points[2].y ));
            if (displayMat.width() > 1 && displayMat.height() > 1) {
                cropped = new Mat(displayMat, R);
            }
        }
        // TODO delete Log
        Log.v(TAG, "cropped width "+String.valueOf(cropped.width()));
        Log.v(TAG, "cropped height "+String.valueOf(cropped.height()));


        displayImage(cropped);

    }
    private void displayImage(Mat image)
    {
        // convert to bitmap:
        Bitmap bitMap = Bitmap.createBitmap(image.cols(), image.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(image, bitMap);

        // find the imageview and draw it!
        ImageView iv = (ImageView) findViewById(R.id.image_view);
        iv.setImageBitmap(bitMap);
        currentBitmap = bitMap;
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
