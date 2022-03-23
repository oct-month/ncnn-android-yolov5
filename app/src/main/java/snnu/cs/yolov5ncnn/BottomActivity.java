package snnu.cs.yolov5ncnn;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BottomActivity extends AppCompatActivity
{
    private static final String TAG = BottomActivity.class.getSimpleName();
    private static final int PERMISSION_STORAGE = 1;
    private static final int INTENT_CAMERA = 2;
    private static final int INTENT_CROP = 3;
    private static final int INTENT_PICK = 4;

    private Uri imageUri = null;
    private Bitmap bitmap = null;
    private Bitmap yourSelectedImage = null;
    private YoloV5Ncnn ncnn = new YoloV5Ncnn();
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);

        // 设置底部nav
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_history:
                        openFragment(HistoryFragment.newInstance(), false);
                        return true;
                    case R.id.navigation_camera:
                        // openCamera();
                        CharSequence[] items = {"拍照", "图库"};
                        new AlertDialog.Builder(BottomActivity.this)
                                .setItems(items, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                openCamera();
                                                break;
                                            case 1:
                                                Intent intent = new Intent(Intent.ACTION_PICK);
                                                intent.setType("image/*");
                                                startActivityForResult(intent, INTENT_PICK);
                                                break;
                                        }
                                    }
                                }).show();
                        // openFragment(CameraFragment.newInstance());
                        return true;
                    case R.id.navigation_about:
                        openFragment(AboutFragment.newInstance(), false);
                        return true;
                }
                return false;
            }
        });
        navView.setSelectedItemId(R.id.navigation_history);
        
        // 加载模型
        if (!ncnn.Init(getAssets())) {
            Toast.makeText(this, "模型加载错误", Toast.LENGTH_SHORT).show();
        }

        // 权限请求
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_STORAGE:
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "本APP需要存储权限才能正常使用", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INTENT_CAMERA:
                if (resultCode == RESULT_OK) {
                    Intent intent = new Intent("com.android.camera.action.CROP");
                    intent.setDataAndType(imageUri, "image/*");
                    intent.putExtra("scale", true);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, INTENT_CROP);
                }
                break;
            case INTENT_CROP:
                if (resultCode == RESULT_OK) {
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        // openFragment(CameraFragment.newInstance(bitmap));
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "相机错误");
                        return;
                    }
                    YoloV5Ncnn.Obj[] objs = ncnn.Detect(yourSelectedImage, ncnn.isGpuAvaliable());
                    bitmap = getObjects(objs);
                    saveBitmap(bitmap);
                    openFragment(CameraFragment.newInstance(bitmap), false);
                }
                break;
            case INTENT_PICK:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    try {
                        bitmap = decodeUri(selectedImage);
                        yourSelectedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.e(TAG, "图片未找到");
                        return;
                    }
                    YoloV5Ncnn.Obj[] objs = ncnn.Detect(yourSelectedImage, ncnn.isGpuAvaliable());
                    bitmap = getObjects(objs);
                    saveBitmap(bitmap);
                    openFragment(CameraFragment.newInstance(bitmap), false);
                }
                break;
        }
    }

    public void saveBitmap(Bitmap bitmap)
    {
        File file = null;
        try {
            file = File.createTempFile("" + System.currentTimeMillis(), ".jpg", getCacheDir());
            String s = "";
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openFragment(Fragment fragment, Boolean canBack)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_fragment, fragment);
        if (canBack) {
            transaction.addToBackStack(null);
        }
        else {
            transaction.disallowAddToBackStack();
        }
        transaction.commit();
    }

    public void historyItemClick(View view)
    {
        ImageView imageView = view.findViewById(R.id.history_img);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        openFragment(ImgFragment.newInstance(bitmap), true);
    }

    private void openCamera()
    {
        int currentApiVersion = Build.VERSION.SDK_INT;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            String filename = format.format(new Date());
            File tempFile = new File(Environment.getExternalStorageDirectory(), filename + ".jpg");
            if (currentApiVersion < 24)
            {
                imageUri = Uri.fromFile(tempFile);
            }
            else
            {
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        }
        startActivityForResult(intent, INTENT_CAMERA);
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
        try {
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
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "ExifInterface IOException");
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap getObjects(YoloV5Ncnn.Obj[] objects)
    {
        if (objects == null)
        {
            return null;
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
            if (objects[i].h != 0) {
                paint.setColor(colors[i % 19]);
                canvas.drawRect(objects[i].x, objects[i].y, objects[i].x + objects[i].w, objects[i].y + objects[i].h, paint);

                // draw filled text inside image
                {
                    String text = objects[i].label + " = " + String.format("%.1f", objects[i].prob * 100) + "%";

                    float text_width = textpaint.measureText(text);
                    float text_height = -textpaint.ascent() + textpaint.descent();

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
        }
        return rgba;
    }

}