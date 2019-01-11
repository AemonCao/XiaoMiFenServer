package me.aemon.Helper;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

import java.io.File;

import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

/**
 * @创建人 Aemon Cao
 * @创建时间 2019/1/9 18:42
 * @描述
 */
public class VideoHelper {
    public static void CompositeVideo(String imagesPath) throws Exception {
        File file = new File(imagesPath);
        File[] files = file.listFiles();
        String saveMp4Name = files[10].getName().substring(0, 8) + ".flv";
        System.out.println(saveMp4Name);
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(saveMp4Name, 2592, 1944);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_FLV1);
        recorder.setFormat("flv");
        // 每秒多少帧
        recorder.setFrameRate(24);
        recorder.setVideoBitrate(8000000);
        recorder.setPixelFormat(0);
        recorder.start();
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].toString();
            System.out.println(fileName + "\t" + i + "/" + files.length);
            IplImage image = cvLoadImage(fileName); // 非常吃内存！！
            recorder.record(converter.convert(image));
            // 释放内存？
            // cvLoadImage(fileName); // 非常吃内存！！
            opencv_core.cvReleaseImage(image);
        }
        recorder.stop();
        recorder.release();
    }
}
