package com.eqpos.eqentry.tools;
/*
    Bu class barkod okumak için kullanılan zxing kütüphanesinin
    kamerayı dikay olarak açması için var. Normalde bu kütüphane
    kamerayı yatay olarak açtığından barkodu okutmak için cihazı
    yatay çevirmek gerekiyor. Bu Class ile kamerayı dikey konumda
    açtırtıyorum
 */
import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * Created by dursu on 22.02.2018.
 */

public class CaptureActivityPortrait extends CaptureActivity {
}
