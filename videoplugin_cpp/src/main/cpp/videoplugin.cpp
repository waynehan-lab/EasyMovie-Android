#include <jni.h>
#include <string>

#include "logutils.h"
#include "Buffer.h"

Buffer *buffer;

extern "C"
JNIEXPORT jint JNICALL
Java_com_pvr_videoplugin_VideoPlugin_createOESTextureID(JNIEnv *env, jobject thiz) {
    LOGI("%s", __func__);
    buffer = new Buffer();
    return buffer->createOESTexture();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pvr_videoplugin_VideoPlugin_renderInit(JNIEnv *env, jobject thiz, jint width, jint height,
                                                jint texture_id) {
    LOGI("%s", __func__);
    buffer->init(width, height, texture_id);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pvr_videoplugin_VideoPlugin_renderDraw(JNIEnv *env, jobject thiz) {
    LOGI("%s", __func__);
    buffer->draw();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_pvr_videoplugin_VideoPlugin_renderRelease(JNIEnv *env, jobject thiz) {
    LOGI("%s", __func__);
    buffer->release();
    delete buffer;
}