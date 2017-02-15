package com.example.divya.intentapp.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.divya.intentapp.Common.BitmapResizer;
import com.example.divya.intentapp.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final String APP_PICTURE_DIRECTORY = "/CaptureImage";
    private static final String MIME_TYPE_IMAGE = "image/";
    private static final String FILE_SUFFIX_JPG = ".jpg";
    private static final int TAKE_PHOTO_REQUEST_CODE = 4;
    private static final int MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE=0;
    private static final String IMAGE_URI_KEY = "IMAGE_URI";
    private static final String BITMAP_WIDTH = "BITMAP_WIDTH";
    private static final String BITMAP_HEIGHT = "BITMAP_HEIGHT";

    private Boolean pictureTaken = false;

    private Uri selectedPhotoPath;

    private ImageView takePictureImageView;
    private TextView lookingGoodTextView;
    private Button nextScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePictureImageView = (ImageView) findViewById(R.id.picture_imageview);
        takePictureImageView.setOnClickListener(this);

        lookingGoodTextView = (TextView) findViewById(R.id.looking_good_textview);

        nextScreenButton = (Button) findViewById(R.id.enter_text_button);
        nextScreenButton.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        checkReceivedIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.picture_imageview:
              takePictureWithCamera();
                break;

            case R.id.enter_text_button:
                moveToNextScreen();
                break;

            default:
                break;
        }
    }

    private void takePictureWithCamera() {
        if (appHasExternalStorageWritePermission()) {
            // create intent to capture image from camera
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File photoFile = createImageFile();
            selectedPhotoPath = Uri.parse(photoFile.getAbsolutePath());

            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            startActivityForResult(captureIntent, TAKE_PHOTO_REQUEST_CODE);
        } else {
            acquireExternalStorageWritePermissionIfNeeded();
        }
    }

    private void moveToNextScreen() {

        if (pictureTaken) {
            Intent nextScreenIntent = new Intent(this, EnterTextActivity.class);
            nextScreenIntent.putExtra(IMAGE_URI_KEY, selectedPhotoPath);
            nextScreenIntent.putExtra(BITMAP_WIDTH, takePictureImageView.getWidth());
            nextScreenIntent.putExtra(BITMAP_HEIGHT, takePictureImageView.getHeight());

            startActivity(nextScreenIntent);
        } else {
            Toast.makeText(this, R.string.select_a_picture, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {
            setImageViewWithImage();
        }
    }
    private boolean appHasExternalStorageWritePermission() {
        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void acquireExternalStorageWritePermissionIfNeeded() {
        // Here, thisActivity is the current activity
        if (!appHasExternalStorageWritePermission()) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    takePictureWithCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void setImageViewWithImage() {
        Bitmap pictureBitmap = BitmapResizer.ShrinkBitmap(selectedPhotoPath.toString(),
                takePictureImageView.getWidth(),
                takePictureImageView.getHeight());
        takePictureImageView.setImageBitmap(pictureBitmap);
        lookingGoodTextView.setVisibility(View.VISIBLE);
        pictureTaken = true;
    }

    private File createImageFile() {

        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES + APP_PICTURE_DIRECTORY);
         storageDir.mkdirs();

        File imageFile = null;

        try {
                    imageFile = File.createTempFile(
                            imageFileName,  /* prefix */
                            FILE_SUFFIX_JPG,         /* suffix */
                            storageDir      /* directory */
                    );

        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    private Uri getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return Uri.parse(result);
    }

    private void checkReceivedIntent() {

        Intent imageRecievedIntent = getIntent();
        String intentAction = imageRecievedIntent.getAction();
        String intentType = imageRecievedIntent.getType();

        if (Intent.ACTION_SEND.equals(intentAction) && intentType != null) {
            if (intentType.startsWith(MIME_TYPE_IMAGE)) {
                Uri contentUri = imageRecievedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                selectedPhotoPath = getRealPathFromURI(contentUri);
                setImageViewWithImage();
            }
        }
    }

}
