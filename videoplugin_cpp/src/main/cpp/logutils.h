#pragma once

#ifndef LOGUTILS_H
#define LOGUTILS_H

#include <android/log.h>

#define LOG_TAG "PVRFBO"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

#endif //LOGUTILS_H
