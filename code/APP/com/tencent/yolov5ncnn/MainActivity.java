// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.yolov5ncnn;


import android.app.ActionBar;
import android.app.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.graphics.Matrix;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.tencent.yolov5ncnn.utils.DatabaseHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;


public class MainActivity extends Activity {
    private static final int SELECT_IMAGE = 1;
    private static final int TAKE_PHOTO = 2;

    private ImageButton buttonDetect;
    private Uri imageUri;
    private ImageView imageView;
    private Bitmap bitmap = null;
    private Bitmap yourSelectedImage = null;
    public static File tempFile;

    public DatabaseHelper dbHelper = new DatabaseHelper(this, "HISTORY.db", null, 5);

    private LocationManager locationManager;
    private String provider;

    private YoloV5Ncnn yolov5ncnn = new YoloV5Ncnn();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Initialize View
        super.onCreate(savedInstanceState);
        setBarColor();
        setContentView(R.layout.activity_main);
        // Initialize Database
        dbHelper.getWritableDatabase();
        // Initialize AI Detection Model
        boolean ret_init = yolov5ncnn.Init(getAssets());
        if (!ret_init) {
            Log.e("MainActivity", "yolov5ncnn Init failed");
        }
        // Initialize components
        imageView = (ImageView) findViewById(R.id.imageView);
        ImageButton buttonImage = (ImageButton) findViewById(R.id.buttonImage);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });

        ImageButton buttonCamera = (ImageButton) findViewById(R.id.buttonCamera);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tempFile = new File(getExternalCacheDir(), "output_image.jpg");
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                try {
                    tempFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(MainActivity.this,
                            "com.tencent.yolov5ncnn.fileprovider", tempFile);
                } else {
                    Uri.fromFile(tempFile);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
            }
        });

        buttonDetect = (ImageButton) findViewById(R.id.buttonDetect);
        buttonDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (yourSelectedImage == null) {
                    return;
                }
                YoloV5Ncnn.Obj[] objects = yolov5ncnn.Detect(yourSelectedImage);
                storeHistory(objects);
                showObjects(objects);
            }
        });
        /*
        // Initialize Location Provider
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, true);
        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "Please check your location service!",
                    Toast.LENGTH_SHORT).show();
            buttonDetect.setEnabled(false);
        }
        */
    }

    private void storeHistory(YoloV5Ncnn.Obj[] objects) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int[] curTime = getCurrentTime();
        double[] curLocation = getCurrentLocation();
        if(curLocation == null) {
            setImageButtonStatus("FAIL");
            Toast.makeText(MainActivity.this,
                    "未获取到当前位置信息，记录存储失败！",
                    Toast.LENGTH_SHORT).show();
            
        }
        else if(objects.length == 0)
        {
            setImageButtonStatus("FAIL");
            Toast.makeText(MainActivity.this,
                    "未检测到BBU设备，记录存储失败！",
                    Toast.LENGTH_SHORT).show();
        }
        else
        {
            ContentValues values = new ContentValues();
            for (int i = 0; i < objects.length; i++) {
                values.put("category", objects[i].label);
                values.put("prob", objects[i].prob);

                values.put("date", curTime[0] + "-"
                        + curTime[1] + "-"
                        + curTime[2]);
                values.put("time", curTime[3] + ":"
                        + curTime[4] + ":"
                        + curTime[5]);
                values.put("longitude", curLocation[0]);
                values.put("latitude", curLocation[1]);
                db.insert("history", null, values);
                values.clear();
            }
            setImageButtonStatus("SUCCESS");
            Toast.makeText(MainActivity.this,
                    "检测到 " + objects.length + " 台BBU设备，历史记录已保存！",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private int[] getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int curTime[] = {calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)};
        return curTime;
    }

    private double[] getCurrentLocation() {
        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Please Grant Location Permission!", Toast.LENGTH_SHORT).show();
            return null;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        while (location == null) {
            location = locationManager.getLastKnownLocation(provider);
        }
        if (location != null) {
            double[] loc = {location.getLongitude(), location.getLatitude()};
            return loc;
        }
        else{
            return null;
        }
        */
        Location newLocation = new Location(LocationManager.GPS_PROVIDER);
        newLocation.setLatitude(39.9624);
        newLocation.setLongitude(116.3571);
        double[] curLoc = {newLocation.getLongitude(), newLocation.getLatitude()};
        return curLoc;
    }

    private void showObjects(YoloV5Ncnn.Obj[] objects)
    {
        if (objects.length == 0)
        {
            imageView.setImageBitmap(bitmap);
            return;
        }

        // draw objects on bitmap
        Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        final int[] colors = new int[] {
            Color.rgb( 54,  67, 244),
            Color.rgb( 99,  30, 233),
            Color.rgb(176,  39, 156),
            Color.rgb(183,  58, 103),
            Color.rgb(181,  81,  63),
            Color.rgb(243, 150,  33),
            Color.rgb(244, 169,   3),
            Color.rgb(212, 188,   0),
            Color.rgb(136, 150,   0),
            Color.rgb( 80, 175,  76),
            Color.rgb( 74, 195, 139),
            Color.rgb( 57, 220, 205),
            Color.rgb( 59, 235, 255),
            Color.rgb(  7, 193, 255),
            Color.rgb(  0, 152, 255),
            Color.rgb( 34,  87, 255),
            Color.rgb( 72,  85, 121),
            Color.rgb(158, 158, 158),
            Color.rgb(139, 125,  96)
        };

        Canvas canvas = new Canvas(rgba);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        Paint textbgpaint = new Paint();
        textbgpaint.setColor(Color.WHITE);
        textbgpaint.setStyle(Paint.Style.FILL);

        Paint textpaint = new Paint();
        textpaint.setColor(Color.BLACK);
        textpaint.setTextSize(26);
        textpaint.setTextAlign(Paint.Align.LEFT);

        for (int i = 0; i < objects.length; i++)
        {
            paint.setColor(colors[i % 19]);

            canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

            // draw filled text inside image
            {
                String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";

                float text_width = textpaint.measureText(text);
                float text_height = - textpaint.ascent() + textpaint.descent();

                float x = objects[i].x;
                float y = objects[i].y - text_height;
                if (y < 0)
                    y = 0;
                if (x + text_width > rgba.getWidth())
                    x = rgba.getWidth() - text_width;

                canvas.drawRect(x, y, x + text_width, y + text_height, textbgpaint);

                canvas.drawText(text, x, y - textpaint.ascent(), textpaint);
            }
        }
        imageView.setImageBitmap(rgba);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null){
            return;
        }
        switch (requestCode)
        {
            case SELECT_IMAGE:
                if(resultCode==Activity.RESULT_OK)
                {
                    imageUri = data.getData();
                    try {
                        resetImageView();
                        setImageButtonStatus("RESET");
                        bitmap = decodeUri(imageUri);
                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    } catch (FileNotFoundException e) {
                        Log.e("MainActivity", "FileNotFoundException");
                    }
                    imageView.setImageBitmap(bitmap);
                }
                break;
            case TAKE_PHOTO:
                if(resultCode==Activity.RESULT_OK)
                {
                    bitmap= null;
                    try {
                        resetImageView();
                        setImageButtonStatus("RESET");
                        bitmap = decodeUri(imageUri);
                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    } catch (FileNotFoundException e) {
                        Log.e("MainActivity", "FileNotFoundException");
                    }
                    imageView.setImageBitmap(rotateIfRequired(bitmap));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId()){
            case R.id.menuHistory:
                Intent intent1 = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent1);
                break;
            case R.id.menuEncyclopedia:
                Intent intent2 = new Intent(MainActivity.this, WebActivity.class);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu)
    {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    private Bitmap rotateIfRequired(Bitmap bitmap)
    {
        String path=tempFile.getPath();
        ExifInterface exif = null;
        try {
            exif=new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation= exif.getAttributeInt(ExifInterface.TAG_EXIF_VERSION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation)
        {
            case ExifInterface.ORIENTATION_ROTATE_90:
                bitmap=rotateBitmap(bitmap,90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                bitmap=rotateBitmap(bitmap,180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                bitmap=rotateBitmap(bitmap,270);
                break;
            default:
                break;
        }
        return bitmap;

    }

    private Bitmap rotateBitmap(Bitmap bitmap,int degree)
    {
        Matrix matrix=new Matrix();
        matrix.postRotate((float)degree);
        Bitmap rotateBitmap=Bitmap.createBitmap(bitmap,0,0,
                bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        bitmap.recycle();
        return bitmap;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException
    {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 640;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

        // Rotate according to EXIF
        int rotate = 0;
        try
        {
            ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(selectedImage));
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (IOException e)
        {
            Log.e("MainActivity", "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void setBarColor()
    {
        getWindow().setNavigationBarColor(getColor(R.color.blue));
        getWindow().setStatusBarColor(getColor(R.color.blue));
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getColor(R.color.blue)));
    }
    
    private void setImageButtonStatus(String status)
    {

        switch (status)
        {
            case "SUCCESS":
                buttonDetect.setBackground(getDrawable(R.drawable.btn_detect_succ_background));
                buttonDetect.setImageResource(R.drawable.check);
                buttonDetect.setEnabled(false);
                break;
            case "FAIL":
                buttonDetect.setBackground(getDrawable(R.drawable.btn_detect_fail_background));
                buttonDetect.setImageResource(R.drawable.error);
                buttonDetect.setEnabled(false);
                break;
            case "RESET":
                buttonDetect.setBackground(getDrawable(R.drawable.btn_detect_background));
                buttonDetect.setImageResource(R.drawable.search);
                buttonDetect.setEnabled(true);
                break;
            default:
                break;
        }
    }

    private void resetImageView()
    {
        yourSelectedImage = null;
        bitmap = null;
        imageView.setImageBitmap(bitmap);

    }
}
