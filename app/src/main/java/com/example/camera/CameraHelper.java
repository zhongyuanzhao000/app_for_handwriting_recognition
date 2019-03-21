package com.example.camera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.example.application.MyApplication;
import com.example.view.MaskSurfaceView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.activity.RectCameraActivity.bitmap;

public class CameraHelper {
    private final String TAG = "CameraHelper";
    private ToneGenerator tone;
    private String filePath;// = "/carchecker/photo";
    private boolean isPreviewing;

    private static CameraHelper helper;
    private Camera camera;
    private MaskSurfaceView surfaceView;

    //	分辨率
    private Size resolution;

    //	照片质量
    private int picQuality = 100;

    //	照片尺寸
    private Size pictureSize;

    //	闪光灯模式(default：自动)
    private String flashlightStatus = Camera.Parameters.FLASH_MODE_OFF;

    public enum Flashlight {
        AUTO, ON, OFF
    }

    private CameraHelper() {
    }

    public static synchronized CameraHelper getInstance() {
        if (helper == null) {
            helper = new CameraHelper();
        }
        return helper;
    }

    /**
     * 设置照片质量
     *
     * @param picQuality
     * @return
     */
    public CameraHelper setPicQuality(int picQuality) {
        this.picQuality = picQuality;
        return helper;
    }

    /**
     * 设置闪光灯模式
     *
     * @param status
     * @return
     */
    public CameraHelper setFlashlight(Flashlight status) {
        switch (status) {
            case AUTO:
                this.flashlightStatus = Camera.Parameters.FLASH_MODE_AUTO;
                break;
            case ON:
                this.flashlightStatus = Camera.Parameters.FLASH_MODE_ON;
                break;
            case OFF:
                this.flashlightStatus = Camera.Parameters.FLASH_MODE_OFF;
                break;
            default:
                this.flashlightStatus = Camera.Parameters.FLASH_MODE_OFF;
        }
        return helper;
    }

    /**
     * 设置文件保存路径(default: /mnt/sdcard/DICM)
     *
     * @param path
     * @return
     */
    public CameraHelper setPictureSaveDictionaryPath(String path) {
        this.filePath = path;
        return helper;
    }

    public CameraHelper setMaskSurfaceView(MaskSurfaceView surfaceView) {
        this.surfaceView = surfaceView;
        return helper;
    }

    /**
     * 打开相机并开启预览
     *
     * @param holder       SurfaceHolder
     * @param format       图片格式
     * @param width        SurfaceView宽度
     * @param height       SurfaceView高度
     * @param screenWidth  屏幕宽度
     * @param screenHeight 屏幕高度
     */
    public void openCamera(SurfaceHolder holder, int format, int width, int height, int screenWidth, int screenHeight) {
        if (this.camera != null) {
            this.camera.release();
        }
        this.camera = Camera.open();
        this.initParameters(holder, format, width, height, screenWidth, screenHeight);
        this.startPreview();


        //设置自动聚焦
        surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View arg0) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean arg0, Camera arg1) {
                    }
                });
                return true;
            }
        });
    }

    /**
     * 照相
     */
    public void tackPicture(final OnCaptureCallback callback) {
        this.camera.autoFocus(new AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean flag, Camera camera) {
                camera.takePicture(new ShutterCallback() {
                    @Override
                    public void onShutter() {
                        if (tone == null) {
//						 发出提示用户的声音
                            tone = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
                        }
                        //tone.startTone(ToneGenerator.TONE_PROP_BEEP);
                    }
                }, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        String filepath = savePicture(data);
                        boolean success = false;
                        if (filepath != null) {
                            success = true;
                        }
                        stopPreview();
                        callback.onCapture(success, filepath);
                    }
                });
            }
        });
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void saveBitmap(Bitmap bitmap) {
        File imgFileDir = getImageDir();
        if (!imgFileDir.exists() && !imgFileDir.mkdirs()) {
            return;
        }
        String imgFilePath0 = imgFileDir.getPath() + File.separator + this.generateFileName();
        File imgFile0 = new File(imgFilePath0);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try {
            fos = new FileOutputStream(imgFile0);
            bos = new BufferedOutputStream(fos);
            //按Bitmap.CompressFormat.JPEG图片格式以及100的画质，将图片转换为输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            //通知系统相册更新
            MyApplication.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(imgFilePath0)));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 裁剪并保存照片
     *
     * @param data
     * @return
     */
    private String savePicture(byte[] data) {
        File imgFileDir = getImageDir();
        if (!imgFileDir.exists() && !imgFileDir.mkdirs()) {
            return null;
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        String imgFilePath;

        //文件路径路径
        String imgFilePath0 = imgFileDir.getPath() + File.separator + this.generateFileName();
        imgFilePath = imgFilePath0;
        File imgFile0 = new File(imgFilePath0);
        bitmap = this.cutImage(data, 0, 1); //剪切照片
        try {
            fos = new FileOutputStream(imgFile0);
            bos = new BufferedOutputStream(fos);
            //按Bitmap.CompressFormat.JPEG图片格式以及100的画质，将图片转换为输出流
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            //通知系统相册更新
            MyApplication.getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(imgFilePath0)));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		/*for (int i = 0; i < 6; i++) {
			String imgFilePath1 = imgFileDir.getPath() + File.separator + this.generateFileName();
			File imgFile1 = new File(imgFilePath1);
			Bitmap b1 = this.cutImage(data,5-i,6); //剪切照片
			try {
				fos = new FileOutputStream(imgFile1);
				bos = new BufferedOutputStream(fos);
				b1.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			} catch (Exception e) {
                e.printStackTrace();
				return null;
			} finally {
				try {
					if(fos != null){
						fos.flush();
						fos.close();
					}
					if(bos != null){
						bos.flush();
						bos.close();
					}
				} catch (IOException e) {
				    e.printStackTrace();
                }
			}
		}*/
        return imgFilePath;
    }

    /**
     * 生成图片名称
     *
     * @return
     */
    private String generateFileName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.getDefault());
        String strDate = dateFormat.format(new Date());  //获取当前时间，转化为以上指定格式
        //在文件名后添加当前微秒值
        long nanoTime = System.nanoTime();
        String r = String.valueOf(nanoTime);

        return "img_" + strDate + "_" + r + ".jpg";
    }

    /**
     * 获取图片文件目录
     *
     * @return
     */
    private File getImageDir() {
        String path = null;
        if (this.filePath == null || this.filePath.equals("")) {
            path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        } else {
            path = Environment.getExternalStorageDirectory().getPath() + filePath;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }

    /**
     * 初始化相机参数
     *
     * @param holder       SurfaceHolder
     * @param format       图片格式
     * @param width        SurfaceView宽度
     * @param height       SurfaceView高度
     * @param screenWidth  屏幕宽度
     * @param screenHeight 屏幕高度
     */
    private void initParameters(SurfaceHolder holder, int format, int width, int height, int screenWidth, int screenHeight) {
        try {
            Parameters p = this.camera.getParameters();

            this.camera.setPreviewDisplay(holder);

            if (width > height) {
//				横屏
                this.camera.setDisplayOrientation(0);
            } else {
//				竖屏
                this.camera.setDisplayOrientation(90);
            }

//			照片质量
            p.set("jpeg-quality", picQuality);

//			设置照片格式
            p.setPictureFormat(PixelFormat.JPEG);

//			设置闪光灯
            p.setFlashMode(this.flashlightStatus);

//			设置最佳预览尺寸
            List<Size> previewSizes = p.getSupportedPreviewSizes();
//			设置预览分辨率
            if (this.resolution == null) {
                this.resolution = this.getOptimalPreviewSize(previewSizes, width, height);
            }
            try {
                p.setPreviewSize(this.resolution.width, this.resolution.height);
            } catch (Exception e) {
                Log.e(TAG, "不支持的相机预览分辨率: " + this.resolution.width + " × " + this.resolution.height);
            }

//			设置照片尺寸
            if (this.pictureSize == null) {
                List<Size> pictureSizes = p.getSupportedPictureSizes();
                this.setPicutreSize(pictureSizes, screenWidth, screenHeight);
            }
            try {
                p.setPictureSize(this.pictureSize.width, this.pictureSize.height);
            } catch (Exception e) {
                Log.e(TAG, "不支持的照片尺寸: " + this.pictureSize.width + " × " + this.pictureSize.height);
            }

            this.camera.setParameters(p);
        } catch (Exception e) {
            Log.e(TAG, "相机参数设置错误");
        }
    }

    /**
     * 释放Camera
     */
    public void releaseCamera() {
        if (this.camera != null) {
            if (this.isPreviewing) {
                this.stopPreview();
            }
            this.camera.setPreviewCallback(null);
            isPreviewing = false;
            this.camera.release();
            this.camera = null;
        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {
        if (this.camera != null && this.isPreviewing) {
            this.camera.stopPreview();
            this.isPreviewing = false;
        }
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        if (this.camera != null) {
            this.camera.startPreview();
            this.camera.autoFocus(null);
            this.isPreviewing = true;
        }
    }

    /**
     * 裁剪照片
     *
     * @param data
     * @return
     */
    private Bitmap cutImage(byte[] data, int m, int n) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length); //从数组data的下标0处读取长为length的图片
        if (this.surfaceView.getWidth() < this.surfaceView.getHeight()) {
            //if (bitmap.getWidth() < bitmap.getHeight()) {
//			竖屏旋转照片
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.setRotate(90);   //图片顺时针旋转90°
//			以bitmap为原图，创建新的图片，指定起始坐标以及新图像的宽、高等			
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        if (this.surfaceView == null) {
            return bitmap;
        } else {
            int[] sizes = this.surfaceView.getMaskSize();
            if (sizes[0] == 0 || sizes[1] == 0) {
                return bitmap;
            }
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int x = (w - sizes[0]) / 2 + m * sizes[0] / 6;
            int y = (h - sizes[1]) / 2;
//          以bitmap为原图，取取景框（Mask）的左上角顶点为初始坐标（x,y）及其宽、高；
            return Bitmap.createBitmap(bitmap, x, y, sizes[0] / n, sizes[1]);
        }
    }

    /**
     * 获取最佳预览尺寸
     */
    private Size getOptimalPreviewSize(List<Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) width / height;
        if (sizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double r = size.width * 1.0 / size.height * 1.0;
            if (r != 4 / 3 || r != 3 / 4 || r != 16 / 9 || r != 9 / 16) {
                continue;
            }

            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
//				if (Math.abs(size.height - targetHeight) < minDiff) {
//					optimalSize = size;
//					minDiff = Math.abs(size.height - targetHeight);
//			     }
                if (Math.abs(size.width - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetHeight);
                }

            }
        }
        return optimalSize;
    }

    /**
     * 设置照片尺寸为最接近屏幕尺寸
     *
     * @param list
     * @return
     */
    private void setPicutreSize(List<Size> list, int screenWidth, int screenHeight) {
        int approach = Integer.MAX_VALUE;

        for (Size size : list) {
            int temp = Math.abs(size.width - screenWidth + size.height - screenHeight);
//			System.out.println("approach: "+approach +", temp: "+ temp+", size.width: "+size.width+", size.height: "+size.height);
            if (approach > temp) {
                approach = temp;
                this.pictureSize = size;
            }
        }
//		//降序
//		if(list.get(0).width>list.get(list.size()-1).width){
//			int len = list.size();
//			list = list.subList(0, len/2==0? len/2 : (len+1)/2);
//			this.pictureSize = list.get(list.size()-1);
//		}else{
//			int len = list.size();
//			list = list.subList(len/2==0? len/2 : (len-1)/2, len-1);
//			this.pictureSize = list.get(0);
//		}
    }
}
