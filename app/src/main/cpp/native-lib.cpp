#include <jni.h>
#include <string>
#include <vector>

extern "C" JNIEXPORT jstring JNICALL
Java_com_totoprout_torteul_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Torteul Engine Ready";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_totoprout_torteul_NativeLib_getYouTubeLatest(
        JNIEnv* env,
        jobject /* this */) {

    // In a real app, this C++ code would call a networking library
    // or perform complex data processing.
    std::vector<std::string> dummyVideos = {
        "C++ Optimization Techniques - Totoprout",
        "How to build a better YouTube wrapper",
        "Android NDK Basics for C++ Experts"
    };

    jclass stringClass = env->FindClass("java/lang/String");
    jobjectArray result = env->NewObjectArray(dummyVideos.size(), stringClass, env->NewStringUTF(""));

    for (size_t i = 0; i < dummyVideos.size(); ++i) {
        env->SetObjectArrayElement(result, i, env->NewStringUTF(dummyVideos[i].c_str()));
    }

    return result;
}
