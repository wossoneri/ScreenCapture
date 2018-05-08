package com.softard.wow.screencapture.QRCode;

import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Created by wow on 5/8/18.
 */

public class Utils {

    public static String readBitmap(Bitmap bmp) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        String path = ""; //训练数据路径

        tessBaseAPI.setDebug(true);
        tessBaseAPI.init(path, "eng"); //eng为识别语言
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"); // 识别白名单
        tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{;:'\"\\|~`,./<>?"); // 识别黑名单
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);//设置识别模式

        tessBaseAPI.setImage(bmp); //设置需要识别图片的bitmap
        String inspection = tessBaseAPI.getHOCRText(0);
        tessBaseAPI.end();
        return inspection ;
    }
}
