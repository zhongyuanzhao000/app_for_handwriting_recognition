package com.example.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.application.MyApplication;
import com.example.camera.CameraHelper;
import com.example.camera.OnCaptureCallback;
import com.example.hccr.ReadLabel;
import com.example.pojo.FishInfo;
import com.example.util.TableUtil;
import com.example.view.MaskSurfaceView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RectCameraActivity extends AppCompatActivity implements OnCaptureCallback {

    static {
        System.loadLibrary("native-lib");
    }

    private MaskSurfaceView surfaceview;
    private ImageView imageView;
    private EditText editView;
    //	拍照
    private Button btn_capture;
    //	重拍
    private Button btn_recapture;
    //	识别
    private Button btn_ok;

    private ReadLabel detector;

    //	拍照后得到的保存的文件路径
    private String filepath;

    public static Bitmap bitmap;
    public Bitmap bitmap1;
    private Bitmap bitmap2;
    public Bitmap bitmapDst;
    File photoFile;
    TableUtil tableUtil;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_rect_camera);
        context = this;

        this.surfaceview = (MaskSurfaceView) findViewById(R.id.surface_view);
        this.imageView = (ImageView) findViewById(R.id.image_view);
        this.editView = (EditText) findViewById(R.id.edit_view);
        btn_capture = (Button) findViewById(R.id.btn_capture);
        btn_recapture = (Button) findViewById(R.id.btn_recapture);
        btn_ok = (Button) findViewById(R.id.btn_ok);

        photoFile = new File(getExternalFilesDir("img"), "scan.jpg");

        //加载pb库
        detector = new ReadLabel("label.txt", "hccr-80000.pb", getAssets());
        try {
            detector.load();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "加载失败", Toast.LENGTH_SHORT).show();
        }



//		设置矩形区域大小
        //this.surfaceview.setMaskSize(480, 970);

//		拍照
        btn_capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_capture.setEnabled(false);
                btn_ok.setEnabled(true);
                btn_recapture.setEnabled(true);
                CameraHelper.getInstance().tackPicture(RectCameraActivity.this);

            }
        });

//		重拍
        btn_recapture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_capture.setEnabled(true);
                btn_ok.setEnabled(false);
                btn_recapture.setEnabled(false);
                imageView.setVisibility(View.GONE);
                editView.setVisibility(View.GONE);
                surfaceview.setVisibility(View.VISIBLE);
                CameraHelper.getInstance().startPreview();
            }
        });

//		识别
        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApplication.resList = extractWord();
                Intent intent = new Intent(context, ResActivity.class);
                startActivity(intent);

/*                if (bitmap != null) {
                    String resultAll = extractWord();
                    editView.setText(resultAll);
                }
                btn_capture.setEnabled(false);
                btn_ok.setEnabled(false);
                btn_recapture.setEnabled(true);
                imageView.setVisibility(View.GONE);
                editView.setVisibility(View.VISIBLE);
                surfaceview.setVisibility(View.GONE);*/
            }
        });
    }

    @Override
    public void onCapture(boolean success, String filepath) {
        this.filepath = filepath;
        String message = "拍照成功";
        if (!success) {
            message = "拍照失败";
            CameraHelper.getInstance().startPreview();
            this.imageView.setVisibility(View.GONE);
            this.surfaceview.setVisibility(View.VISIBLE);
        } else {
            //this.imageView.setVisibility(View.VISIBLE);
            this.surfaceview.setVisibility(View.GONE);
            startActivityForResult(CropActivity.getJumpIntent(RectCameraActivity.this, true, photoFile), 100);
            //this.imageView.setImageBitmap(BitmapFactory.decodeFile(filepath));
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == 100 && photoFile.exists()) {
            bitmapDst = BitmapFactory.decodeFile(photoFile.getPath());
            this.imageView.setVisibility(View.VISIBLE);
            this.imageView.setImageBitmap(bitmapDst);
        }
    }

    /**
     * 去表格并提取、识别文字
     *
     * @param
     * @return
     */
    public List<FishInfo> extractWord() {
        String resultAll = "识别内容为：";
        CameraHelper cameraHelper = CameraHelper.getInstance();
        if (bitmapDst.getWidth() < bitmapDst.getHeight()) { //将图片转为横向
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.setRotate(-90);   //图片逆时针旋转90°
            //以bitmap为原图，创建新的图片，指定起始坐标以及新图像的宽、高等
            bitmapDst = Bitmap.createBitmap(bitmapDst, 0, 0, bitmapDst.getWidth(), bitmapDst.getHeight(), matrix, true);
        }
        bitmapDst = grayTwo(bitmapDst); //将原图转化为二值化图片
        //cameraHelper.saveBitmap(bitmapDst);//保存二值化横图
        bitmapDst = zoomImage(bitmapDst, 1500, 696);//将图片设为1500 X 696
        int[] para = Bitmap2Grey(bitmapDst);//传入jni的native(C++)层，传出文字块参数数组
        //cameraHelper.saveBitmap(bitmapDst);//保存 去完表格的 横图
        int arrLen = para.length;
        String result;
        tableUtil = new TableUtil();
        for (int i = 0; i < arrLen; i += 5) {
            int x = para[i];
            int y = para[i + 1];
            int width = para[i + 2];
            int height = para[i + 3];
            int line = para[i + 4];
            bitmap1 = Bitmap.createBitmap(bitmapDst, x, y, width, height); //生成每个文字块的bitmap对象
//            if (x >= 0 && x + width < 838) {
//                bitmap2 = whiteEdgeBitmap(bitmap1);//将数字块居中，并设为96 X 96
//                //result = detector2.detect(bitmap2);//识别数字
//            } else if (x >= 838 && x + width < 1500) {
//                bitmap2 = zoomImage(bitmap1, 96, 96);//将汉字块设为96 X 96
//                //result = detector.detect(bitmap2);//识别文字
//            }
            bitmap2 = zoomImage(bitmap1, 96, 96);//将字块设为96 X 96
            //cameraHelper.saveBitmap(bitmap2);//保存文字块方形图片
            bitmap2 = grayTwo(bitmap2);
            result = detector.detect(bitmap2);//识别文字

            tableUtil.addChar(x, width, line, result);//把识别字符对应表格形式进行分类存储

            resultAll = resultAll + result;
            if ((i + 9) < arrLen && (para[i + 4] != para[i + 9])) {
                resultAll = resultAll + "\n";
            }
        }
        return tableUtil.getFishInfoList();
    }

    /**
     * 先灰度化，再二值化
     *
     * @param bitmap
     * @return bitmap
     */
    public Bitmap grayTwo(Bitmap bitmap) {
        Mat m = new Mat();
        Utils.bitmapToMat(bitmap, m);
        Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2GRAY); //灰度化
        Imgproc.threshold(m, m, 0, 255, Imgproc.THRESH_OTSU); //二值化
        Utils.matToBitmap(m, bitmap);
        return bitmap;
    }

    /**
     * 按固定宽、高缩放bitmap
     *
     * @param
     * @return bitmap
     */
    public Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    /**
     * 剪切成正方形，然后添加白边
     *
     * @param bitmap
     * @return
     */
    public Bitmap whiteEdgeBitmap(Bitmap bitmap) {
        //int size = bitmap.getWidth() < bitmap.getHeight() ? bitmap.getWidth() : bitmap.getHeight();
        int num = 24;
        int size2 = 96;
        Bitmap bitmap2 = zoomImage(bitmap, 80, 80);//将正方形尺寸设为80 X 80
        //Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, size, size);
        // 背图
        Bitmap bitmap3 = Bitmap.createBitmap(size2, size2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap3);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        // 生成白色的
        paint.setColor(Color.WHITE);
        canvas.drawBitmap(bitmap2, num / 2, num / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        // 画正方形的
        canvas.drawRect(0, 0, size2, size2, paint);
        return bitmap3;
    }

    public native int[] Bitmap2Grey(Bitmap bitmap);
}
