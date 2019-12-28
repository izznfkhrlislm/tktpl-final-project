#include <jni.h>
#include <string>
#include <android/log.h>
#include <string.h>

extern "C" JNIEXPORT jboolean JNICALL
Java_id_ac_ui_cs_mobileprogramming_izzanfi_musicx_MusicService_toggleShuffle(
        JNIEnv* env,
        jobject obj,
        jboolean shuffleStatus) {

    jboolean shuffleRes;
    if (shuffleStatus) {
        shuffleRes = false;
    } else {
        shuffleRes = true;
    }

    return shuffleRes;
}