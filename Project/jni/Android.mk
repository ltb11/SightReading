LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := opencv_java
LOCAL_SRC_FILES := opencv_java.cpp

include $(BUILD_SHARED_LIBRARY)
