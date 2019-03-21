package com.example.hccr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

//import static android.support.v4.graphics.drawable.IconCompat.getResources;

public class ReadLabel {
    private static final int MAX_RESULTS = 3817;//3817;
    //TODO 中文尺寸为96
    public static final int INPUT_SIZE = 96;
    private String labelFilename;
    private String modelFilename;
    private List<String> labels = new ArrayList<>();
    private AssetManager assetManager;
    private TensorFlowInferenceInterface inferenceInterface;

    public ReadLabel(String labelFilename, String modelFilename, AssetManager assetManager) {
        this.labelFilename = labelFilename;
        this.modelFilename = modelFilename;
        this.assetManager = assetManager;
    }

    public void load() throws IOException {
        InputStream labelsInput = assetManager.open(labelFilename);
        //TODO 修改label文件的编码形式
        BufferedReader br = new BufferedReader(new InputStreamReader(labelsInput,"UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            labels.add(line);
        }
        br.close();
        if (inferenceInterface != null) {
            inferenceInterface.close();
        }
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
    }

    public static int getMaxindex(float[] arr) {
        int maxindex = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > arr[maxindex]) {
                maxindex = i;
            }
        }
        return maxindex;
    }

    /**
     * 调用pb库对进行文字识别
     * @param bitmap
     * @return
     */
    public String detect(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE,
                INPUT_SIZE, false);
        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        float[] pixelsFloat = new float[pixels.length];
        for (int i = 0; i < pixels.length; ++i) {
            int pix = pixels[i];
            int b = pix & 0xff;
            //TODO
            pixelsFloat[i] = (float) (b/255.0)*255;
        }
        //TODO input与output中的i/o要改为大写
        inferenceInterface.feed("Input", pixelsFloat, 1, INPUT_SIZE, INPUT_SIZE,1);
        //TODO 韩语专用,注释掉
        //inferenceInterface.feed("keep_prob", new float[] { 1 });
        inferenceInterface.run(new String[]{"Output"});
        float[] classes = new float[MAX_RESULTS];
        inferenceInterface.fetch("Output", classes);
        int index = getMaxindex(classes);
        String result = labels.get(index);

        return result;
    }
}
