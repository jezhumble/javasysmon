/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 15 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 *  TODO: Error checking
 */
#include <jni.h>
#include <windows.h>
#include <winbase.h>

static SYSTEM_INFO system_info;
static int num_cpu;
static LONGLONG p_idle, p_kernel, p_user;
static float p_cpu_usage;

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  // Get some system information that won't change that we'll need later on
  GetSystemInfo (&system_info);
  num_cpu = system_info.dwNumberOfProcessors;

  // Initialise ticks
  FILETIME idletime, kerneltime, usertime;
  GetSystemTimes(&idletime, &kerneltime, &usertime);
  p_idle = filetime_to_int64(idletime);
  p_kernel = filetime_to_int64(kerneltime);
  p_user = filetime_to_int64(usertime);
  p_cpu_usage = 0;

  return JNI_VERSION_1_2;
}

JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuUsage (JNIEnv *env, jobject object)
{
  LONGLONG idle, kernel, user, total, p_total;
  float cpu_usage;
  FILETIME idletime, kerneltime, usertime;

  GetSystemTimes(&idletime, &kerneltime, &usertime);
  idle = filetime_to_int64(idletime);
  kernel = filetime_to_int64(kerneltime);
  user = filetime_to_int64(usertime);
  total = idle + kernel + user;
  p_total = p_idle + p_kernel + p_user;
  if (total == p_total || idle == p_idle) {
    cpu_usage = p_cpu_usage;
  } else {
    cpu_usage = ((float)1) - ((float)(idle - p_idle)) / ((float)(total - p_total));
    // Reset counters
    p_idle = idle;
    p_user = user;
    p_kernel = kernel;
  }
  return (jfloat)cpu_usage;
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

static LONGLONG filetime_to_int64 (const FILETIME * filetime)
{
    ULARGE_INTEGER time;

    time.LowPart = filetime->dwLowDateTime;
    time.HighPart = filetime->dwHighDateTime;

    return time.QuadPart;
}
