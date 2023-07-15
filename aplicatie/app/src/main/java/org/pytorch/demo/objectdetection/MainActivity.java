package org.pytorch.demo.objectdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements Runnable {
    private int mImageIndex = 0;
    private final String[] mTestImages = {"test1.png", "test2.png", "test3.png", "test4.png", "test5.png", "test6.png", "test7.png", "test8.png", "test9.png"};
    private Uri mPhotoFileUri;
    private ImageView mImageView;
    private ResultView mResultView;
    private Button mButtonDetect;
    private ProgressBar mProgressBar;
    public Bitmap mBitmap = null;
    private Module mModule = null;

    public String getToastMessage() {
        return toastMessage;
    }

    public String toastMessage = "";
    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY, scaleX, scaleY;
    private static final String PHOTO_FILE_URI_KEY = "photo_file_uri_key";

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPhotoFileUri != null) {
            outState.putParcelable(PHOTO_FILE_URI_KEY, mPhotoFileUri);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(PHOTO_FILE_URI_KEY)) {
            mPhotoFileUri = savedInstanceState.getParcelable(PHOTO_FILE_URI_KEY);
        }
    }

    /**
     * Disables screen rotation and locks the orientation to either landscape or portrait based on the current orientation.
     */
    private void disableScreenRotation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    /**
     * Creates a temporary image file with a unique name in the cache directory.
     *
     * @return The created image file.
     * @throws IOException If an I/O error occurs during file creation.
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getCacheDir(); // Change this line

        return File.createTempFile(
                imageFileName,
                ".png",
                storageDir
        );
    }

    /**
     * Retrieves the file path of an asset from the application's internal storage or copies it from assets if it doesn't exist.
     *
     * @param context    The context.
     * @param assetName  The name of the asset file.
     * @return The file path of the asset.
     * @throws IOException If an I/O error occurs during file access or copying.
     */
    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Request READ_EXTERNAL_STORAGE permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // Request CAMERA permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        setContentView(R.layout.activity_main);

        try {
            // Load the bitmap from assets
            mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }

        // Initialize the redButton and redFilter views
        Button redButton = findViewById(R.id.redButton);
        final View redFilter = findViewById(R.id.redFilter);

        // Set click listener for the redButton
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (redFilter.getVisibility() == View.GONE) {
                    redFilter.setVisibility(View.VISIBLE);
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#40FF0000"))); // set the background to a translucent red color
                } else {
                    redFilter.setVisibility(View.GONE);
                    getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // set the background to transparent
                }
            }
        });

        mImageView = findViewById(R.id.imageView);
        mImageView.setImageBitmap(mBitmap);
        mResultView = findViewById(R.id.resultView);
        mResultView.setVisibility(View.INVISIBLE);

        final Button buttonTest = findViewById(R.id.testButton);
        buttonTest.setText(("Test Image 1/9"));
        buttonTest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);
                mImageIndex = (mImageIndex + 1) % mTestImages.length;
                buttonTest.setText(String.format("Test Image %d/%d", mImageIndex + 1, mTestImages.length));

                try {
                    mBitmap = BitmapFactory.decodeStream(getAssets().open(mTestImages[mImageIndex]));
                    mImageView.setImageBitmap(mBitmap);
                } catch (IOException e) {
                    Log.e("Object Detection", "Error reading assets", e);
                    finish();
                }
            }
        });
        Button mButtonClassify = findViewById(R.id.classifyButton);
        mButtonClassify.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBitmap != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    // Create request body with the byte array
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
                    MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

                    // Create Retrofit instance and configure API endpoint
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://192.168.85.240:4000/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);
                    Call<ResponseBody> call = apiService.uploadImage(body);

                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    String jsonResponse = response.body().string();
                                    JSONObject jsonObject = new JSONObject(jsonResponse);
                                    int classId = jsonObject.getInt("class_id");
                                    if (classId == 2) {
                                        Toast.makeText(MainActivity.this, "Class: very good (bortle 1-4)", Toast.LENGTH_LONG).show();
                                        ToastMessageHolder.getInstance().setToastMessage("Class: very good (bortle 1-4)");
                                    } else if (classId == 0) {
                                        Toast.makeText(MainActivity.this, "Class: good (bortle 5-6)", Toast.LENGTH_LONG).show();
                                        toastMessage = "Class: good (bortle 5-6)";
                                    } else {
                                        Toast.makeText(MainActivity.this, "Class: poor (bortle 7-8) ", Toast.LENGTH_LONG).show();
                                        toastMessage = "Class: poor (bortle 7-8) ";
                                    }

                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to get class ID", Toast.LENGTH_SHORT).show();
                                toastMessage = "Failed to get class ID";
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "No image to classify", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final Button buttonSelect = findViewById(R.id.selectButton);
        buttonSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mResultView.setVisibility(View.INVISIBLE);

                final CharSequence[] options = {"Choose from Photos", "Take Picture", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("New Test Image");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take Picture")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            File photoFile = null;
                            try {
                                photoFile = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (photoFile != null) {
                                mPhotoFileUri = FileProvider.getUriForFile(MainActivity.this, "org.pytorch.demo.objectdetection.fileprovider", photoFile);
                                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoFileUri);
                                disableScreenRotation(); // Call the method here
                                startActivityForResult(takePicture, 0);
                            }
                        } else if (options[item].equals("Choose from Photos")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, 1);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        final Button buttonLive = findViewById(R.id.liveButton);
        buttonLive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent intent = new Intent(MainActivity.this, ObjectDetectionActivity.class);
                startActivity(intent);
            }
        });

        mButtonDetect = findViewById(R.id.detectButton);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mButtonDetect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                System.out.println("mImageView.getHeight() -> " + Integer.toString(mImageView.getHeight()));
                System.out.println("mImageView.getWidth() -> " + Integer.toString(mImageView.getWidth()));
                System.out.println("mBitmap.getHeight() -> " + Integer.toString(mBitmap.getHeight()));
                System.out.println("mBitmap.getWidth() -> " + Integer.toString(mBitmap.getWidth()));
                mButtonDetect.setEnabled(false);
                mProgressBar.setVisibility(ProgressBar.VISIBLE);
                mButtonDetect.setText(getString(R.string.run_model));
                // Calculate image scaling factors
                if (mBitmap.getHeight() < mBitmap.getWidth()) {
                    mImgScaleX = (float) mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                    mImgScaleY = (float) mBitmap.getHeight() / PrePostProcessor.mInputHeight;
                    System.out.println("mImgScaleX: " + Float.toString(mImgScaleX));
                    System.out.println("mImgScaleY: " + Float.toString(mImgScaleY));
                    scaleX = (float) mImageView.getWidth() / mBitmap.getWidth();
                    scaleY = (float) mImageView.getHeight() / mBitmap.getHeight();
                    float minScale = Math.min(scaleX, scaleY);
                    mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight() ? (float) mImageView.getWidth() / mBitmap.getWidth() : (float) mImageView.getHeight() / mBitmap.getHeight());
                    mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth() ? (float) mImageView.getHeight() / mBitmap.getHeight() : (float) mImageView.getWidth() / mBitmap.getWidth());
                    mIvScaleX = minScale;
                    mIvScaleY = minScale;
                    System.out.println("mIvScaleX " + Float.toString(mIvScaleX));
                    System.out.println("mIvScaleY: " + Float.toString(mIvScaleY));
                    mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth()) / 2.0f;
                    mStartY = (mImageView.getHeight() - mIvScaleY * mBitmap.getHeight()) / 2.0f;
                    System.out.println("mStartX: " + Float.toString(mStartX));
                    System.out.println("mStartY: " + Float.toString(mStartY));
                    // Adjust the start coordinates to make sure they're positive
                    mStartX = Math.max(0, mStartX);
                    mStartY = Math.max(0, mStartY);
                } else {
                    mImgScaleX = (float) mBitmap.getHeight() / PrePostProcessor.mInputWidth;
                    mImgScaleY = (float) mBitmap.getWidth() / PrePostProcessor.mInputHeight;
                    System.out.println("mImgScaleX: " + Float.toString(mImgScaleX));
                    System.out.println("mImgScaleY: " + Float.toString(mImgScaleY));
                    scaleX = (float) mImageView.getWidth() / mBitmap.getHeight();
                    scaleY = (float) mImageView.getHeight() / mBitmap.getWidth();
                    float minScale = Math.min(scaleX, scaleY);
                    mIvScaleX = (mBitmap.getHeight() > mBitmap.getWidth() ? (float) mImageView.getWidth() / mBitmap.getHeight() : (float) mImageView.getHeight() / mBitmap.getWidth());
                    mIvScaleY = (mBitmap.getWidth() > mBitmap.getHeight() ? (float) mImageView.getHeight() / mBitmap.getWidth() : (float) mImageView.getWidth() / mBitmap.getHeight());
                    mIvScaleX = minScale;
                    mIvScaleY = minScale;
                    System.out.println("mIvScaleX " + Float.toString(mIvScaleX));
                    System.out.println("mIvScaleY: " + Float.toString(mIvScaleY));
                    mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getHeight()) / 2.0f;
                    mStartY = (mImageView.getHeight() - mIvScaleY * mBitmap.getWidth()) / 2.0f;
                    System.out.println("mStartX: " + Float.toString(mStartX));
                    System.out.println("mStartY: " + Float.toString(mStartY));
                    // Adjust the start coordinates to make sure they're positive
                    mStartX = Math.max(0, mStartX);
                    mStartY = Math.max(0, mStartY);
                }

                Thread thread = new Thread(MainActivity.this);
                thread.start();
            }
        });

        try {
            mModule = LiteModuleLoader.load(MainActivity.assetFilePath(getApplicationContext(), "yolov5s.torchscript.ptl"));
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("classes.txt")));
            String line;
            List<String> classes = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                classes.add(line);
            }
            PrePostProcessor.mClasses = new String[classes.size()];
            classes.toArray(PrePostProcessor.mClasses);
        } catch (IOException e) {
            Log.e("Object Detection", "Error reading assets", e);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(mPhotoFileUri);
                            ExifInterface exifInterface = new ExifInterface(inputStream);
                            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                            // Check if the data is not null, which means the camera app returned a thumbnail
                            if (data != null && data.getParcelableExtra("data") != null) {
                                mBitmap = (Bitmap) data.getExtras().get("data");
                            } else {
                                // Read the full image from the file when data is null
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), mPhotoFileUri);
                                mBitmap = ImageDecoder.decodeBitmap(source);
                            }

                            if (mBitmap != null) {
                                Matrix matrix = new Matrix();
                                if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                                    matrix.postRotate(90.0f);
                                } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                                    matrix.postRotate(270.0f);
                                }
                                if (mBitmap.getHeight() > mBitmap.getWidth()) {
                                    matrix.postRotate(90.0f);
                                }
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                if (mBitmap.getWidth() != 1920 || mBitmap.getHeight() != 1080) {
                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, 1920, 1080, true);
                                    mBitmap.recycle(); // Recycle the original bitmap
                                    mBitmap = resizedBitmap; // Use the resized bitmap
                                }
                                mImageView.setImageBitmap(null);
                                mImageView.invalidate();
                                mImageView.setImageBitmap(mBitmap);

                                // Update the scaling factors and start coordinates based on the new image dimensions
                                mImgScaleX = (float) mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                                mImgScaleY = (float) mBitmap.getHeight() / PrePostProcessor.mInputHeight;
                                scaleX = (float) mImageView.getWidth() / mBitmap.getWidth();
                                scaleY = (float) mImageView.getHeight() / mBitmap.getHeight();
                                float minScale = Math.min(scaleX, scaleY);
                                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight()) ? (float) mImageView.getWidth() / mBitmap.getWidth() : (float) mImageView.getHeight() / mBitmap.getHeight();
                                mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth()) ? (float) mImageView.getHeight() / mBitmap.getHeight() : (float) mImageView.getWidth() / mBitmap.getWidth();
                                mIvScaleX = minScale;
                                mIvScaleY = minScale;
                                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth()) / 2.0f;
                                mStartY = (mImageView.getHeight() - mIvScaleY * mBitmap.getHeight()) / 2.0f;
                                mStartX = Math.max(0, mStartX);
                                mStartY = Math.max(0, mStartY);
                            } else {
                                Log.e("MainActivity", "Bitmap is null");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                mBitmap = BitmapFactory.decodeFile(picturePath);
                                Matrix matrix = new Matrix();
                                if (mBitmap.getHeight() > mBitmap.getWidth()) {
                                    matrix.postRotate(90.0f);
                                }
                                mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
                                if (mBitmap.getWidth() != 1920 || mBitmap.getHeight() != 1080) {
                                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, 1920, 1080, true);
                                    mBitmap.recycle(); // Recycle the original bitmap
                                    mBitmap = resizedBitmap; // Use the resized bitmap
                                }
                                mImageView.setImageBitmap(null);
                                mImageView.invalidate();
                                mImageView.setImageBitmap(mBitmap);

                                // Update the scaling factors and start coordinates based on the new image dimensions
                                mImgScaleX = (float) mBitmap.getWidth() / PrePostProcessor.mInputWidth;
                                mImgScaleY = (float) mBitmap.getHeight() / PrePostProcessor.mInputHeight;
                                scaleX = (float) mImageView.getWidth() / mBitmap.getWidth();
                                scaleY = (float) mImageView.getHeight() / mBitmap.getHeight();
                                float minScale = Math.min(scaleX, scaleY);
                                mIvScaleX = (mBitmap.getWidth() > mBitmap.getHeight()) ? (float) mImageView.getWidth() / mBitmap.getWidth() : (float) mImageView.getHeight() / mBitmap.getHeight();
                                mIvScaleY = (mBitmap.getHeight() > mBitmap.getWidth()) ? (float) mImageView.getHeight() / mBitmap.getHeight() : (float) mImageView.getWidth() / mBitmap.getWidth();
                                mIvScaleX = minScale;
                                mIvScaleY = minScale;
                                mStartX = (mImageView.getWidth() - mIvScaleX * mBitmap.getWidth()) / 2.0f;
                                mStartY = (mImageView.getHeight() - mIvScaleY * mBitmap.getHeight()) / 2.0f;
                                mStartX = Math.max(0, mStartX);
                                mStartY = Math.max(0, mStartY);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
    }


    @Override
    public void run() {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(mBitmap, PrePostProcessor.mInputWidth, PrePostProcessor.mInputHeight, true);
        if (resizedBitmap == null) {
            return;
        }

        // Create a software copy of the bitmap
        Bitmap softwareBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, false);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(softwareBitmap, PrePostProcessor.NO_MEAN_RGB, PrePostProcessor.NO_STD_RGB);
        IValue[] outputTuple = mModule.forward(IValue.from(inputTensor)).toTuple();
        final Tensor outputTensor = outputTuple[0].toTensor();
        final float[] outputs = outputTensor.getDataAsFloatArray();
        final ArrayList<Result> results = PrePostProcessor.outputsToNMSPredictions(outputs, mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY);
        if (!results.isEmpty()) {
            OutputHolder.setResults(results.get(0).classIndex);
            Log.d("Object Detection", "resizedBitmap width: " + resizedBitmap.getWidth() + ", height: " + resizedBitmap.getHeight());
            Log.d("Object Detection", "softwareBitmap width: " + softwareBitmap.getWidth() + ", height: " + softwareBitmap.getHeight());
            Log.d("Object Detection", "mImgScaleX: " + mImgScaleX + ", mImgScaleY: " + mImgScaleY);
            Log.d("Object Detection", "mIvScaleX: " + mIvScaleX + ", mIvScaleY: " + mIvScaleY);
        }
        runOnUiThread(() -> {
            mButtonDetect.setEnabled(true);
            mButtonDetect.setText(getString(R.string.detect));
            mProgressBar.setVisibility(ProgressBar.INVISIBLE);
            mResultView.setmImgScaleX(mImgScaleX);
            mResultView.setmImgScaleY(mImgScaleY);
            mResultView.setmIvScaleX(mIvScaleX);
            mResultView.setmIvScaleY(mIvScaleY);
            mResultView.setmStartX(mStartX);
            mResultView.setmStartY(mStartY);
            mResultView.setScaleX(scaleX);
            mResultView.setScaleY(scaleY);
            mResultView.setResults(results);
            mResultView.invalidate();
            mResultView.setVisibility(View.VISIBLE);
        });
    }
}
