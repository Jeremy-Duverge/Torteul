package com.totoprout.torteul

object NativeLib {
    init {
        System.loadLibrary("torteul")
    }

    external fun getYouTubeLatest(): Array<String>
}
