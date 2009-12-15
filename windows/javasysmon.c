/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 10 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 */
#include <jni.h>
#include <windows.h>
#include <winbase.h>

static SYSTEM_INFO system_info;
static int num_cpu;

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  // Get some system information that won't change that we'll need later on
  GetSystemInfo (&system_info);
  num_cpu = system_info.dwNumberOfProcessors;

  return JNI_VERSION_1_2;
}

JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuUsage (JNIEnv *env, jobject object)
{
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_totalMemory (JNIEnv *env, jobject object)
{
  MEMORYSTATUS status;
  GlobalMemoryStatus (&status);
  return (jlong) status.dwTotalPhys;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_freeMemory (JNIEnv *env, jobject object)
{
  MEMORYSTATUS status;
  GlobalMemoryStatus (&status);
  return (jlong) status.dwAvailPhys;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_totalSwap (JNIEnv *env, jobject object)
{
  MEMORYSTATUS status;
  GlobalMemoryStatus (&status);
  return (jlong) status.dwTotalVirtual;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_freeSwap (JNIEnv *env, jobject object)
{
  MEMORYSTATUS status;
  GlobalMemoryStatus (&status);
  return (jlong) status.dwAvailVirtual;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_numCpus (JNIEnv *env, jobject object)
{
  return (jint) num_cpu;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuFrequency (JNIEnv *env, jobject object)
{
  return (jlong)0;
}
