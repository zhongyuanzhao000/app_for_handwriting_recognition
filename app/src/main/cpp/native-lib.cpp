#include <jni.h>
#include<iostream>
#include <algorithm>
#include <time.h>
#include <android/bitmap.h>
#include <opencv/cv.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2\dnn.hpp>

#include <stdio.h>
//#include <tchar.h>
#include <fstream>
#include <opencv2/opencv.hpp>

#ifndef __EXTRACT_TABLE_HPP__
#define __EXTRACT_TABLE_HPP__
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
#include <iostream>
#endif

#define V_PROJECT 1
#define H_PROJECT 2

using namespace cv;
using namespace std;
//定义三个结构体
typedef struct a
{
   int line;
   int x;
   int y;
   int width;
   int height;
   int NUM;
   vector<Mat> Character;
};
struct a tid[1500];

typedef struct
{
   int begin;
   int end;

}char_range_t;

typedef struct Array
{
   int NUM;   //每行字符的总个数
   vector<vector<int>> matrices;  //矩阵信息
}Array;

typedef struct picture
{
	int NUM;
	vector<Mat> character;
	vector<Mat> character1;
	vector<vector<int>> matrices;
}picture;

typedef struct cut
{
	int NUM;
	Mat character;

}cut;

typedef struct CutChar1
{
	int NUM;
	vector<Mat> character;

}CutChar1;

//函数声明
void BitmapToMat2(JNIEnv *env, jobject& bitmap, Mat& mat, jboolean needUnPremultiplyAlpha);
void BitmapToMat(JNIEnv *env, jobject& bitmap, Mat& mat);
void MatToBitmap2(JNIEnv *env, Mat& mat, jobject& bitmap, jboolean needPremultiplyAlpha);
void MatToBitmap(JNIEnv *env, Mat& mat, jobject& bitmap);

Mat yy(Mat src1, Mat src2, Mat dst);
Mat yy2(Mat src1, Mat src2, Mat dst);
Mat yy3(Mat src1, Mat src2, Mat dst);
Mat yy4(Mat src1, Mat src2, Mat dst);

void AdaptiveFindThreshold(const cv::Mat src, double *low, double *high, int aperture_size =3 );
void _AdaptiveFindThreshold(CvMat *dx, CvMat *dy, double *low, double *high);
jintArray cutPic( JNIEnv *env, Mat& img );

Mat draw_projection(vector<jint>& pos, jint mode);
Mat GetTextProjection(Mat &src, vector<jint>& pos, jint mode);
vector<int> GetPeekRange(vector<jint> &vertical_pos, vector<char_range_t> &peek_range, jfloat min_thresh , jint min_range );
//inline void save_cut(const Mat& img, jint id);
Mat cut_one_line(const Mat& src, jint begin, jint end);
CutChar1 CutSingleChar(Mat& img, jint total_num, jint mode);
Array CutChar(Mat &img, const vector<char_range_t>& v_peek_range, const vector<char_range_t>& h_peek_range, vector<Mat>& chars_set, jint line_num);

//变量声明
int total_num = 0;
vector<Mat> lines_set;
vector<Mat> chars_set;
Mat canny,gray,sobel, edge,erod, Blur1;
int nHeight;

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_activity_RectCameraActivity_Bitmap2Grey( JNIEnv *env, jobject  jobj, jobject jsrcBitmap)
{

    Mat drc;
    BitmapToMat(env,jsrcBitmap,drc);
    nHeight = drc.rows / 6.0;

    double or_src_height=drc.cols, or_src_width=drc.rows;
    Mat canny, gray, sobel, edge, erod, Blur1;
    double src_height=drc.cols, src_width=drc.rows;
    //先转为灰度图
    cvtColor(drc, gray, COLOR_BGR2GRAY);
    //腐蚀（黑色区域变大）
    int erodeSize = src_height*0.003;// 300;

    if (erodeSize % 2 == 0)
       erodeSize++;
    Mat element = getStructuringElement(MORPH_RECT, Size(erodeSize, erodeSize));
    erode(gray, erod, element);
    //高斯模糊化
    int blurSize = src_height*0.005; // 200;
    if (blurSize % 2 == 0)
       blurSize++;
    GaussianBlur(erod, Blur1, Size(blurSize, blurSize), 0, 0);
    //封装的二值化
    Mat thresh = gray.clone();
    adaptiveThreshold(~gray, thresh, 255, CV_ADAPTIVE_THRESH_MEAN_C, CV_THRESH_BINARY, 15, -2);

    //这部分的思想是将线条从横纵的方向处理后抽取出来，再进行交叉，矩形的点，进而找到矩形区域的过程

    // Create the images that will use to extract the horizonta and vertical lines
    Mat horizontal = thresh.clone();
    Mat vertical = thresh.clone();
    int scale = 20; //  这个值越大，检测到的直线越多
    // Specify size on horizontal axis
    int horizontalsize = horizontal.cols*0.05;// / scale;
    // Create structure element for extracting horizontal lines through morphology operations
    Mat horizontalStructure = getStructuringElement(MORPH_RECT, Size(horizontalsize, 1));
    // Apply morphology operations
    erode(horizontal, horizontal, horizontalStructure, Point(-1, -1));
    dilate(horizontal, horizontal, horizontalStructure, Point(-1, -1));//449ms
    int verticalsize = vertical.rows*0.165;// / scale;//463
    // Create structure element for extracting vertical lines through morphology operations
    Mat verticalStructure = getStructuringElement(MORPH_RECT, Size(1, verticalsize));
    // Apply morphology operations
    erode(vertical, vertical, verticalStructure, Point(-1, -1));
    dilate(vertical, vertical, verticalStructure, Point(-1, -1));//200
    dilate(horizontal, horizontal, cv::Mat());
    dilate(vertical, vertical, cv::Mat());

    for (int i = 0; i < vertical.rows; i++)//696
    	{
    		for (int j = 0; j < vertical.cols; j++)
    		{
    			if ((vertical.at<uchar>(i, j) == 255))
    			{
    				if (j < 850)
    				{
    					for (int t = 0; t < vertical.rows; t++)
    					{
    						vertical.at<uchar>(t, j) = 255; //panlin
    					}
    				}
    				else
    				{
    					vertical.at<uchar>(i, j) = 0; //panlin
    				}


    			}
    		}

    	}
    	///////
    	int hh = 116;
    	int xx = 3;
    	for (int i = 10; i < 1500; i++)
    	{
    		for (int j = 0; j < 696; j++)
    		{
    			if ((horizontal.at<uchar>(j, i) == 255))
    			{
    				if ((j > hh - xx )&&( j < hh + xx))
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}

    				}else if (j > hh * 2 - xx && j < hh * 2 + xx)
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}
    				}else if (j > hh * 3 - xx && j < hh * 3 + xx)
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}
    				}else if (j > hh * 3 - xx && j < hh * 3 + xx)
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}
    				}else if (j > hh * 4 - xx && j < hh * 4 + xx)
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}
    				}else if ((j > hh * 5 - xx )&&( j < hh * 5 + xx))
    				{
    					for (int t = 0; t < 1500; t++)
    					{
    						horizontal.at<uchar>(j, t) = 255;
    					}
    				}



    				//dst.at<uchar>(i, j) = src2.at<uchar>(i - 5, j);  //panlin

    			}
    			else
    			{
    				horizontal.at<uchar>(j, i) = 0;
    			}
    		}
    	}



    Mat first=gray.clone();
    yy(vertical, gray, first);
    yy2(horizontal, first, first);

    resize(first, first, Size(or_src_height , or_src_width ), 0, 0, INTER_LINEAR);
    threshold(first,first,0,255,CV_THRESH_BINARY | CV_THRESH_OTSU);

    Mat s = first.clone();
    yy3(vertical,s,first);
    yy4(horizontal,first,first);

    //主要是扫描图像四周x个像素，全部赋值255
    for(int i=0;i<first.rows;i++){
        for(int j=0;j<first.cols;j++){
            if( (i>5&&i<first.rows-7) && (j>5&&j<first.cols -7) ){

            }else{
                first.at<uchar>(i,j) = 255;
            }
        }
    }

    MatToBitmap(env,first,jsrcBitmap);

    jintArray jntarray = cutPic(env, first);//切割
    return jntarray;

}

void BitmapToMat(JNIEnv *env, jobject& bitmap, Mat& mat)
{
    BitmapToMat2(env, bitmap, mat, false);
}

void BitmapToMat2(JNIEnv *env, jobject& bitmap, Mat& mat, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &dst = mat;
    try {
        //LOGD("nBitmapToMat");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        dst.create(info.height, info.width, CV_8UC4);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            //LOGD("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            //LOGD("nBitmapToMat: RGB_565 -> CV_8UC4");
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nBitmapToMat catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nBitmapToMat catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}


void MatToBitmap(JNIEnv *env, Mat& mat, jobject& bitmap) {
    MatToBitmap2(env, mat, bitmap, false);
}

void MatToBitmap2(JNIEnv *env, Mat& mat, jobject& bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void *pixels = 0;
    Mat &src = mat;
    try {
        //LOGD("nMatToBitmap");
        CV_Assert(AndroidBitmap_getInfo(env, bitmap, &info) >= 0);
        CV_Assert(info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                  info.format == ANDROID_BITMAP_FORMAT_RGB_565);
        //CV_Assert(src.dims == 2 && info.height == (uint32_t) src.rows &&
        //         info.width == (uint32_t) src.cols);
        CV_Assert(src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4);
        CV_Assert(AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0);
        CV_Assert(pixels);
        if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if (src.type() == CV_8UC1) {
                //LOGD("nMatToBitmap: CV_8UC1 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if (src.type() == CV_8UC3) {
                //LOGD("nMatToBitmap: CV_8UC3 -> RGBA_8888");
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if (src.type() == CV_8UC4) {
                //LOGD("nMatToBitmap: CV_8UC4 -> RGBA_8888");
                if (needPremultiplyAlpha)
                    cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else
                    src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if (src.type() == CV_8UC1) {
                //LOGD("nMatToBitmap: CV_8UC1 -> RGB_565");
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if (src.type() == CV_8UC3) {
                //LOGD("nMatToBitmap: CV_8UC3 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if (src.type() == CV_8UC4) {
                //LOGD("nMatToBitmap: CV_8UC4 -> RGB_565");
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (const cv::Exception &e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nMatToBitmap catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if (!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //LOGE("nMatToBitmap catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

//自适应阈值的Canny，获取low，high两个参数。
void AdaptiveFindThreshold(const cv::Mat src, double *low, double *high, int aperture_size)
{
   const int cn = src.channels();
   cv::Mat dx(src.rows, src.cols, CV_16SC(cn));
   cv::Mat dy(src.rows, src.cols, CV_16SC(cn));

   cv::Sobel(src, dx, CV_16S, 1, 0, aperture_size, 1, 0);
   cv::Sobel(src, dy, CV_16S, 0, 1, aperture_size, 1, 0);

   CvMat _dx = dx, _dy = dy;
   _AdaptiveFindThreshold(&_dx, &_dy, low, high);

}

// 仿照matlab，自适应求高低两个门限
void _AdaptiveFindThreshold(CvMat *dx, CvMat *dy, double *low, double *high)
{
   CvSize size;
   IplImage *imge = 0;
   int i, j;
   CvHistogram *hist;
   int hist_size = 255;
   float range_0[] = { 0,256 };
   float* ranges[] = { range_0 };
   double PercentOfPixelsNotEdges = 0.7;
   size = cvGetSize(dx);
   imge = cvCreateImage(size, IPL_DEPTH_32F, 1);
   // 计算边缘的强度, 并存于图像中
   float maxv = 0;
   for (i = 0; i < size.height; i++)
   {
      const short* _dx = (short*)(dx->data.ptr + dx->step*i);
      const short* _dy = (short*)(dy->data.ptr + dy->step*i);
      float* _image = (float *)(imge->imageData + imge->widthStep*i);
      for (j = 0; j < size.width; j++)
      {
         _image[j] = (float)(abs(_dx[j]) + abs(_dy[j]));
         maxv = maxv < _image[j] ? _image[j] : maxv;

      }
   }
   if (maxv == 0) {
      *high = 0;
      *low = 0;
      cvReleaseImage(&imge);
      return;
   }

   // 计算直方图
   range_0[1] = maxv;
   hist_size = (int)(hist_size > maxv ? maxv : hist_size);
   hist = cvCreateHist(1, &hist_size, CV_HIST_ARRAY, ranges, 1);
   cvCalcHist(&imge, hist, 0, NULL);
   int total = (int)(size.height * size.width * PercentOfPixelsNotEdges);
   float sum = 0;
   int icount = hist->mat.dim[0].size;

   float *h = (float*)cvPtr1D(hist->bins, 0);
   for (i = 0; i < icount; i++)
   {
      sum += h[i];
      if (sum > total)
         break;
   }
   // 计算高低门限
   *high = (i + 1) * maxv / hist_size;
   *low = *high * 0.4;
   cvReleaseImage(&imge);
   cvReleaseHist(&hist);
}


Mat yy(Mat src1,Mat src2,Mat dst)
{

   for (int i = 0; i< src1.rows; i++)
   {
      for (int j = 10; j< src1.cols; j++)
      {
         if (src1.at<uchar>(i, j) == 0)
         {
            dst.at<uchar>(i, j) = src2.at<uchar>(i, j);

         }
         else if(src1.at<uchar>(i, j) == 255)
         {
            //dst.at<uchar>(i, j) = 255;
            dst.at<uchar>(i, j) = src2.at<uchar>(i, j-5);

         }
      }
   }


   return dst;


}


Mat yy2(Mat src1, Mat src2, Mat dst)
{

   for (int i = 10; i< src1.rows; i++)
   {
      for (int j = 0; j< src1.cols; j++)
      {
         if (src1.at<uchar>(i, j) == 0)
         {
            dst.at<uchar>(i, j) = src2.at<uchar>(i, j);

         }
         else if (src1.at<uchar>(i, j) == 255)
         {
            //dst.at<uchar>(i, j) = 255;
            dst.at<uchar>(i, j) = src2.at<uchar>(i-5, j);
         }
      }
   }
   return dst;
}

//////新加的////////////////////////////////////////////////////////////////////////
Mat yy3(Mat src1,Mat src2,Mat dst)
{

   for (int i = 0; i< src1.rows; i++)
   {
      for (int j = 10; j< src1.cols; j++)
      {
         if (src1.at<uchar>(i, j) == 0)
         {
            dst.at<uchar>(i, j) = src2.at<uchar>(i, j);

         }
         else if(src1.at<uchar>(i, j) == 255)
         {
            dst.at<uchar>(i, j) = 255;
            //dst.at<uchar>(i, j) = src2.at<uchar>(i, j-5);

         }
      }
   }


   return dst;


}


Mat yy4(Mat src1, Mat src2, Mat dst)
{

   for (int i = 10; i< src1.rows; i++)
   {
      for (int j = 0; j< src1.cols; j++)
      {
         if (src1.at<uchar>(i, j) == 0)
         {
            dst.at<uchar>(i, j) = src2.at<uchar>(i, j);

         }
         else if (src1.at<uchar>(i, j) == 255)
         {
            dst.at<uchar>(i, j) = 255;
            //dst.at<uchar>(i, j) = src2.at<uchar>(i-5, j);
         }
      }
   }
   return dst;
}


Mat draw_projection(vector<int>& pos, int mode)
{
   vector<int>::iterator max = std::max_element(std::begin(pos), std::end(pos)); //求最大值
   Mat project;
   if (mode == H_PROJECT)
   {
      int height = pos.size();
      int width = *max;
      project = Mat::zeros(height, width, CV_8UC1);
      for (int i = 0; i < project.rows; i++)
      {
         for (int j = 0; j < pos[i]; j++)
         {
            project.at<uchar>(i, j) = 255;                //project.at<uchar>(i, j)表示i行j列的这个像素
         }
      }

   }
   else if (mode == V_PROJECT)
   {
      int height = *max;
      int width = pos.size();
      project = Mat::zeros(height, width, CV_8UC1);
      for (int i = 0; i < project.cols; i++)
      {
         for (int j = project.rows - 1; j >= project.rows - pos[i]; j--)
         {
            //std::cout << "j:" << j << "i:" << i << std::endl;
            project.at<uchar>(j, i) = 255;
         }
      }

   }
   return project;
}

//获取文本的投影用于分割字符(垂直，水平)
Mat GetTextProjection(Mat &src, vector<int>& pos, int mode)             //raw line
{
    Mat project;
   if (mode == V_PROJECT)
   {
      for (int i = 0; i < src.rows; i++)
      {
         uchar* p = src.ptr<uchar>(i);
         for (int j = 0; j < src.cols; j++)
         {
            if (p[j] == 0)
            {
               pos[j]++;
            }
         }
      }

      project = draw_projection(pos, V_PROJECT);                //水平垂直投影
   }
   else if (mode == H_PROJECT)
   {
      for (int i = 0; i < src.cols; i++)
      {
         for (int j = 0; j < src.rows; j++)
         {
            if (src.at<uchar>(j, i) == 0)
            {
               pos[j]++;
            }
         }
      }
      project = draw_projection(pos, H_PROJECT);
   }
   return project;
}

//获取每个分割字符的范围，min_thresh：波峰的最小幅度，min_range：两个波峰的最小间隔        min_range 用于调整两个字符之间的最小间隔 ***
vector<int> GetPeekRange(vector<int> &vertical_pos, vector<char_range_t> &peek_range, float min_thresh , int min_range )
{
   int begin = 0;
   int end = 0;

   vector<int> label;
   for (int i = 0; i < vertical_pos.size(); i++)
   {
      if (vertical_pos[i] > min_thresh && begin == 0)
      {
         begin = i;
         int y = begin;   //lfs
         label.push_back(y);   //lfs
      }
      else if (vertical_pos[i] > min_thresh && begin != 0)
      {
         continue;
      }
      else if (vertical_pos[i] < min_thresh && begin != 0)
      {
         end = i;
         if (end - begin >= min_range)
         {
            char_range_t tmp;
            tmp.begin = begin;
            tmp.end = end;
            peek_range.push_back(tmp);
            begin = 0;
            end = 0;
         }
      }
      else if (vertical_pos[i] < min_thresh || begin == 0)
      {
         continue;
      }
      else
      {
         //printf("raise error!\n");
      }
   }

   return label;  //lfs   返回每行的y坐标

}


picture findpeak(Mat &srcImage, vector<int>& resultVec, vector<int>& resultVec1,vector<Mat>&chars_set,int line_num)
{
	picture Char;
	vector<vector<int>> Matrix(10000, vector<int>(3));
    vector<Mat>chars;

	Mat verMat;
	Mat resMat = srcImage.clone();
	Mat resMat1 = srcImage.clone();
	Mat resMat2 = srcImage.clone();

	int thresh = 130;
	int threshType = 0;

	const int maxVal = 255;

	threshold(srcImage, srcImage, thresh, maxVal, threshType);

	srcImage.convertTo(srcImage, CV_32FC1);
	reduce(srcImage, verMat, 0, CV_REDUCE_SUM);  //合并成一行，计算所有向量的总和，转化成矩阵

	float* iptr = (float*)verMat.data;

	//cout << verMat << "\n" << endl;               //��ӡ����ֻ��һ��ͨ��������, �������ΪͼƬ�Ļ�����������ͨ��

	vector<int> tempVec(verMat.cols - 1, 0);

	for (int i = 0; i < verMat.cols - 1; ++i, ++iptr)
	{
		if (*(iptr + 1) - *iptr > 0)
		{
			tempVec[i] = 1;
		}
		else if (*(iptr + 1) - *iptr < 0)
		{
			tempVec[i] = -1;
		}
		else
		{
			tempVec[i] = 0;
		}
	}

	for (int i = tempVec.size() - 1; i >= 0; i--)
	{
		if (tempVec[i] == 0 && i == tempVec.size() - 1)
		{
			tempVec[i] = 1;
		}
		else if (tempVec[i] == 0)
		{
			if (tempVec[i + 1] >= 0)
				tempVec[i] = 1;
			else
				tempVec[i] = -1;
		}
	}

	if (verMat.isContinuous())
	{
		for (int i = 0; i < verMat.rows * verMat.cols; i++)
		{
			iptr = (float*)verMat.data;
			//printf("%d:%f \n", i, iptr[i]);                  //iptr��float���͵�����  cout << iptr[i] << endl; verMat.data��һ��ָ�룬��ŵ������ݵ��׵�ַ����������
		}
	}

	for (vector<int>::size_type i = 0; i != tempVec.size() - 1; i++)
	{
		if ((tempVec[i + 1] - tempVec[i] == -2) && (iptr[i + 1] >= 1200))
		{
			resultVec.push_back(i + 1);
		}
	}

	for (vector<int>::size_type i = 0; i != tempVec.size() - 1; i++)
	{
		if ((iptr[i] == 0) && (iptr[i + 1] > 0))
		{
			resultVec1.push_back(i);
		}
		if ((tempVec[i + 1] - tempVec[i] == 2) && (iptr[i + 1] <= 0))
		{
			resultVec1.push_back(i + 1);
		}

	}

	for (int i = 0; i < resultVec.size(); i++)
	{
		//printf("%d:%d\n", i, resultVec[i]);          //��ӡ��������λ��
		for (int ii = 0; ii < resMat.rows; ++ii)
		{
			resMat.at<uchar>(ii, resultVec[i]) = 255;
		}
	}

	for (int i = 0; i < resultVec1.size(); i++)
	{
		for (int ii = 0; ii < resMat1.rows; ++ii)
		{
			resMat1.at<uchar>(ii, resultVec1[i]) = 255;
		}
	}

	int end = 0;
	int begin = 0;
	int count = 0;
	Mat single_char;
	int peek_flag = 0;


	for (int i = 0; i < resultVec1.size() ; i++)        //��ֵ����
	{
		for (int j = 0; j < resultVec.size() ; j++)        //��ֵ����
		{
			if ((resultVec[j] > resultVec1[i]) && ((resultVec[j] < resultVec1[i + 1])))
			{
				begin = resultVec1[i];
				end = resultVec1[i + 1];
				peek_flag = 1;

			}
		}
		if (peek_flag == 1)
		{
			Rect r(begin, 0, end - begin, resMat2.rows); //��ķ�Χ
			rectangle(resMat2, r, Scalar(255, 0, 0), 1);

			Mat single_char = srcImage(r).clone();
			chars_set.push_back(single_char);
			chars.push_back(single_char);

			Matrix[count][1] = end - begin;
			Matrix[count][2] = srcImage.rows;
			Matrix[count][0] = begin;

			count++;
		}
		peek_flag = 0;

	}

	Char.NUM = count;
    Char.matrices = Matrix;
    Char.character = chars_set;
    Char.character1 = chars;

	return Char;
}

//����ַ�
Array CutChar(Mat &img, const vector<char_range_t>& v_peek_range, const vector<char_range_t>& h_peek_range, vector<Mat>& chars_set, int line_num)
{
	Array Char;
	vector<vector<int> > Matrix(50, vector<int>(3));
	int count = 0;
	Mat show_img = img.clone();      //img.clone()�������show_img��img�ĸ�����������ͼƬ��ԭͼ��ȫһ��������Ĳ���Ҳ����������Ӱ��
	cvtColor(show_img, show_img, CV_GRAY2RGB);
	//for (int i = 0; i < v_peek_range.size(); i++)
	//{
	//	int char_gap = v_peek_range[i].end - v_peek_range[i].begin;
	//	int char_gap1 = v_peek_range[i + 1].end - v_peek_range[i + 1].begin;
	//	{
	//		int x = v_peek_range[i].begin - 2>0 ? v_peek_range[i].begin - 2 : 0;
	//		int width = char_gap + 4 <= img.rows ? char_gap + 4 : img.rows + 6;               //0301  lfs
	//
	//		int x_1 = v_peek_range[i+1].begin - 2>0 ? v_peek_range[i+1].begin - 2 : 0;
	//		int width1 = char_gap1 + 4 <= img.rows ? char_gap1 + 4 : img.rows + 6;               //0301  lfs
	//
	//		if ((x_1 - x - width <= 5) && ((width < 33) || (width1 < 35)) && (28 <= x_1 - x <= 80))
	//		{
	//			width = x_1 - x + width1;
	//		}
	//
	//		Rect r(x, 0, width, img.rows);                  //(����1�����Ͻǵ�x���ꣻ����2�����Ͻ�y���ꣻ����3�����ο�����4�����θ�)
	//		rectangle(show_img, r, Scalar(255, 0, 0), 1);
	//		Mat single_char = img(r).clone();
	//		chars_set.push_back(single_char);
	//		save_cut(single_char, count);
	//
	//		Matrix[count][0] = x;              //��������û����������������Ϊʲô��
	//		Matrix[count][1] = width;
	//		Matrix[count][2] = img.rows;
	//		count++;
	//	}
	//}

	for (int i = 0; i < v_peek_range.size(); i++)
	{
		int char_gap = v_peek_range[i].end - v_peek_range[i].begin;
		{
			int x = v_peek_range[i].begin - 2>0 ? v_peek_range[i].begin - 2 : 0;
			int width = char_gap + 4 <= img.rows ? char_gap + 4 : img.rows + 6;               //0301  lfs

			Rect r(x, 0, width, img.rows);                  //(����1�����Ͻǵ�x���ꣻ����2�����Ͻ�y���ꣻ����3�����ο�����4�����θ�)
			rectangle(show_img, r, Scalar(255, 0, 0), 1);
			Mat single_char = img(r).clone();
			chars_set.push_back(single_char);
			//save_cut(single_char, count);

			Matrix[count][0] = x;              //��������û����������������Ϊʲô��
			Matrix[count][1] = width;
			Matrix[count][2] = img.rows;
			count++;
		}
	}

	/*��û��ʲô�ð�*/
	//for (int i = 1; i < v_peek_range.size(); ++i)
	//{
	//	int char_gap = v_peek_range[i].end - v_peek_range[i].begin;
	//	int char_gap1 = v_peek_range[i - 1].end - v_peek_range[i - 1].begin;
	//	{
	//		int x = v_peek_range[i].begin - 2>0 ? v_peek_range[i].begin - 2 : 0;
	//		int width = char_gap + 4 <= img.rows ? char_gap + 4 : img.rows + 6;               //0301  lfs
	//
	//		int x_1 = v_peek_range[i - 1].begin - 2>0 ? v_peek_range[i - 1].begin - 2 : 0;
	//		int width1 = char_gap1 + 4 <= img.rows ? char_gap1 + 4 : img.rows + 6;               //0301  lfs
	//
	//		if ((x - x_1 - width <= 5) && ((width < 33) || (width1 < 33)) && (28 <= x - x_1 <= 80))
	//		{
	//            x = x_1;
	//			width = x - x_1 + width;
	//		}
	//
	//		Rect r(x, 0, width, img.rows);                  //(����1�����Ͻǵ�x���ꣻ����2�����Ͻ�y���ꣻ����3�����ο�����4�����θ�)
	//		rectangle(show_img, r, Scalar(255, 0, 0), 1);
	//		Mat single_char = img(r).clone();
	//		chars_set.push_back(single_char);
	//		imwrite("G:\\characters segmentation\\test0201\\test0201\\save_picture\\�ַ�.jpg", single_char);
	//		save_cut(single_char, count);
	//
	//		Matrix[count][0] = x;              //��������û����������������Ϊʲô��
	//		Matrix[count][1] = width;
	//		Matrix[count][2] = img.rows;
	//		count++;
	//	}
	//}

	Char.NUM = count;
	Char.matrices = Matrix;

	return Char;
}

Mat cut_one_line(const Mat& src, int begin, int end)
{
    Mat line;
   	src(Rect(0, begin, src.cols, end - begin)).copyTo(line);
   	return line;
}

cut picshadowy(Mat binary)
{
	cut Cut;
	//�Ƿ�Ϊ��ɫ���ߺ�ɫ���ݶ�ֵͼ��Ĵ������
	Mat painty(binary.size(), CV_8UC1, Scalar(255)); //��ʼ��Ϊȫ��
													 //ˮƽͶӰ
	int* pointcount = new int[binary.rows]; //�ڶ�ֵͼƬ�м�¼����������ĸ���
	memset(pointcount, 0, binary.rows * 4);//ע��������Ҫ���г�ʼ��

	for (int i = 0; i < binary.rows; i++)
	{
		for (int j = 0; j < binary.cols; j++)
		{
			if (binary.at<float>(i, j) == 0)
			{
				pointcount[i]++; //��¼ÿ���к�ɫ��ĸ��� //ˮƽͶӰ������y���ϵ�ͶӰ
			}
		}
	}

	for (int i = 0; i < binary.rows; i++)
	{
		for (int j = 0; j < pointcount[i]; j++) //����ÿ���к�ɫ��ĸ���������ѭ��
		{
			painty.at<uchar>(i, j) = 0;
		}
	}

	Mat roiImg1;
	int ylabel = 0;
	int startIndex = 0;//��¼�����ַ���������
	int endIndex = 0;//��¼����հ����������
	bool inBlock = false;//�Ƿ���������ַ�����
	for (int i = 0; i <painty.rows; i++)
	{
		if (!inBlock && pointcount[i] != 0)//�����ַ���
		{
			inBlock = true;
			startIndex = i;
			//ylabel = startIndex;
		}
		else if (inBlock && pointcount[i] == 0)//����հ���
		{
			endIndex = i;
			inBlock = false;
			Mat roiImg = painty(Range(startIndex, endIndex + 1), Range(0, painty.cols));//��ԭͼ�н�ȡ��ͼ�������           �ַ��������һ�еĻᱨ���Ҳ���end�ĵط�
			roiImg1 = roiImg;
			ylabel = startIndex;																		//roiList.push_back(roiImg);
		}
		else if ((inBlock == true) && (pointcount[painty.rows - 1] > 0))
		{
			endIndex = painty.rows - 1;
			Mat roiImg = painty(Range(startIndex, endIndex), Range(0, painty.cols));
			roiImg1 = roiImg;
			ylabel = startIndex;
		}
	}

	delete pointcount;

	Cut.character = roiImg1;
	Cut.NUM = ylabel;
	return Cut;
}


CutChar1 CutSingleChar(Mat& img, int total_num, int mode)        // peek��vally����ɾ����  0301
{
	CutChar1 cutchar1;
	Mat show = img.clone();
	cvtColor(show, show, CV_GRAY2RGB);                      //CV_GRAY2RGB��GRAYת�Ҷ�   CV_RGB2GRAY��RGB��gray
	threshold(img, img, 0, 255, CV_THRESH_BINARY | CV_THRESH_OTSU);                                                  //�ڶ���

	//imwrite("G:\\characters segmentation\\test0201\\test0201\\save_picture\\binary.jpg", img);
	//*******************binary ��ֵ��ͼ
	vector<int> horizion_pos(img.rows, 0);
	vector<char_range_t> h_peek_range;
	GetTextProjection(img, horizion_pos, H_PROJECT);              //��ȡ����ͼˮƽͶӰ��������ԭͼ�����Ҳ��ԭͼ��С

	vector<int>y_label = GetPeekRange(horizion_pos, h_peek_range, 8, 40);          //lfs
	vector<Mat> chars_set;

	if (mode == 0)         //��������                          ���������mode  ��һ��mode��ԭ�� 0301
	{
#if 1
		/*��ÿһ�ı����и�*/
		vector<Mat> lines_set;
		//vector<Mat> chars_set;
		picture CHAR;


		for (int j = 0; j < h_peek_range.size(); j++)
		{
			Mat line2 = cut_one_line(img, h_peek_range[j].begin, h_peek_range[j].end);
			lines_set.push_back(line2);
			//imwrite("G:\\characters segmentation\\test0201\\test0201\\save_picture\\�����ַ�.jpg", line2);
			//chars_set.push_back(line2);
		}

		for (int i = 0; i < lines_set.size(); i++)
		{
			Mat line = lines_set[i];
			vector<int> resultVec;
			vector<int> resultVec1;

			Mat srcGray_1;
			threshold(line, srcGray_1, 100, 255, CV_THRESH_BINARY_INV);
			//imshow("G:\\characters segmentation\\test0201\\test0201\\save_picture\\��ֵ��ͼ.jpg", srcGray_1);

			CHAR = findpeak(srcGray_1, resultVec, resultVec1, chars_set, i);             //  peek��vally����ɾ����   0301


			for (int j = 0; j < CHAR.NUM; j++)
			{
				Mat single_char = CHAR.character1[j];
				cut sign;

				threshold(single_char, single_char, 100, 255, CV_THRESH_BINARY_INV);
				//imwrite("G:\\characters segmentation\\test0201\\test0201\\save_picture\\�����ַ�.jpg", single_char);
				sign = picshadowy(single_char);

				tid[total_num].x = CHAR.matrices[j][0];
				tid[total_num].y = y_label[i] + sign.NUM;
				tid[total_num].width = CHAR.matrices[j][1];
				tid[total_num].height = sign.character.rows;
				tid[total_num].line = i;
				total_num = total_num + 1;

			}

		}

#endif
	}
	if (mode == 1)       //������             ȫ������ӵ�mode   0301
	{
#if 2

		/*��ÿһ�ı����и�*/
		vector<Mat> lines_set1;
		for (int i = 0; i < h_peek_range.size(); i++)
		{
			Mat line = cut_one_line(img, h_peek_range[i].begin, h_peek_range[i].end);
			lines_set1.push_back(line);
		}

		Array CHAR;

		//vector<Mat> chars_set;
		for (int i = 0; i < lines_set1.size(); i++)
		{
			Mat line = lines_set1[i];

			vector<int> vertical_pos(line.cols, 0);
			vector<char_range_t> v_peek_range;
			GetTextProjection(line, vertical_pos, V_PROJECT);           //һ���н��д�ֱͶӰ
																		//��ȡÿ���ָ��ַ��ķ�Χ
			GetPeekRange(vertical_pos, v_peek_range, 1, 30);                   //��ֱ�ָ�

			CHAR = CutChar(line, v_peek_range, h_peek_range, chars_set, i);

			int numb_count;

			for (int numb_count = 0; numb_count < CHAR.NUM; numb_count++)
			{
				tid[total_num].x = CHAR.matrices[numb_count][0];
				tid[total_num].y = y_label[i];

				tid[total_num].width = CHAR.matrices[numb_count][1];
				tid[total_num].height = CHAR.matrices[numb_count][2];
				tid[total_num].line = i;
				total_num = total_num + 1;


			}
		}
#endif
	}

	cutchar1.character = chars_set;                   // return�Ľṹ���޸���     0301
	cutchar1.NUM = total_num;

	return cutchar1;

}


jintArray cutPic(JNIEnv *env, Mat& img )
{
    resize(img, img, Size(), 1, 1, INTER_LANCZOS4);

	Mat img_number, img_character;
	img(Rect(0, 0, img.cols * 0.57, img.rows)).copyTo(img_number);
	img(Rect(img.cols * 0.57, 0, (img.cols - img.cols * 0.57), img.rows)).copyTo(img_character);

	CutChar1 chars_number, chars_character;                                                   //�ṹ�����ָ���   0301
	chars_number = CutSingleChar(img_number,0,0);            //���ֲ��ֿ��Խ���������Ϊ0
	chars_character = CutSingleChar(img_character,chars_number.NUM,1);                        //peek��vally����ɾ��   0301

	Mat show2 = img.clone();

	//for (int i = 0; i < chars_character.NUM; i++)
	//{
	//	/*�ַ�ʶ��*/
	//	Rect r(tid[i].x, tid[i].y, tid[i].width, tid[i].height);
	//	rectangle(show2, r, Scalar(0, 0, 0), 1, 8, 0);
	//}

	for (int i = 0; i < chars_number.NUM; i++)                                                    //����ѭ��  0301
	{
		/*�ַ�ʶ��*/
		Rect r(tid[i].x, tid[i].y, tid[i].width, tid[i].height);                  //(����1�����Ͻǵ�x���ꣻ����2�����Ͻ�y���ꣻ����3�����ο�����4�����θ�)
		rectangle(show2, r, Scalar(0, 0, 0), 1, 8, 0);
	}
	for (int i = chars_number.NUM; i < chars_character.NUM; i++)
	{
		tid[i].x = tid[i].x + img.cols * 0.57;

		Rect r(tid[i].x , tid[i].y, tid[i].width, tid[i].height);
		//Rect r(tid[i].x + img.cols * 0.57, tid[i].y , tid[i].width, tid[i].height );
		rectangle(show2, r, Scalar(0, 0, 0), 1, 8, 0);
	}
    total_num=chars_character.NUM;
    //新建一个jintArray对象
    jintArray jntarray = env->NewIntArray(total_num * 5);
    //获取jntarray对象的指针
    jint * jintp = env->GetIntArrayElements(jntarray, NULL);


    for(jint i = 0; i < total_num * 5; i=i+5){
        if(i == 0){
            jintp[i]   = jint(tid[0].x);
            jintp[i+1] = tid[0].y;
            jintp[i+2] = tid[0].width;
            jintp[i+3] = tid[0].height;
            jintp[i+4] = tid[0].line;
        }
        jintp[i]   = tid[i/5].x;
        jintp[i+1] = tid[i/5].y;
        jintp[i+2] = tid[i/5].width;
        jintp[i+3] = tid[i/5].height;
        jintp[i+4] = tid[i/5].line;
    }

   env->ReleaseIntArrayElements(jntarray, jintp, 0);

    return jntarray;

}

