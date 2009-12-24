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
static DWORD current_pid;
static ULONGLONG p_idle, p_kernel, p_user, cpu_frequency;
static float p_cpu_usage;

static ULONGLONG filetime_to_int64 (FILETIME* filetime)
{
    ULARGE_INTEGER time;

    time.LowPart = filetime->dwLowDateTime;
    time.HighPart = filetime->dwHighDateTime;

    return time.QuadPart;
}

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  FILETIME idletime, kerneltime, usertime;
  DWORD dwValue;
  HKEY hKey;
  DWORD dwType=REG_DWORD;
  DWORD dwSize=sizeof(DWORD);

  // Get some system information that won't change that we'll need later on
  GetSystemInfo (&system_info);
  num_cpu = system_info.dwNumberOfProcessors;

  current_pid = GetCurrentProcessId();

  if (RegOpenKeyEx(HKEY_LOCAL_MACHINE,
	  "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0",
	  0L, KEY_QUERY_VALUE, &hKey) == 0) {
		  if (RegQueryValueEx(hKey, "~MHz", NULL, &dwType,(LPBYTE)&dwValue, &dwSize) == 0) {
			cpu_frequency = 1000 * 1000 * dwValue;
		  }
		  RegCloseKey(hKey);
  }

  // Initialise ticks
  GetSystemTimes(&idletime, &kerneltime, &usertime);
  p_idle = filetime_to_int64(&idletime);
  p_kernel = filetime_to_int64(&kerneltime);
  p_user = filetime_to_int64(&usertime);
  p_cpu_usage = 0;
  //printf("idle: %llu kernel: %llu user: %llu\n", p_idle / 10000, p_kernel / 10000, p_user / 10000);

  return JNI_VERSION_1_2;
}

JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuUsage (JNIEnv *env, jobject object)
{
  ULONGLONG idle, kernel, user, idle_diff, kernel_diff, user_diff, total_diff;
  float cpu_usage;
  FILETIME idletime, kerneltime, usertime;

  GetSystemTimes(&idletime, &kerneltime, &usertime);
  idle = filetime_to_int64(&idletime);
  kernel = filetime_to_int64(&kerneltime);
  user = filetime_to_int64(&usertime);
  //printf("idle: %llu kernel: %llu user: %llu\n", idle / 10000, kernel / 10000, user / 10000);

  idle_diff = idle - p_idle;
  kernel_diff = kernel - p_kernel;
  user_diff = user - p_user;

  // you'd think that total CPU is idle + kernel + user. But the idle process
  // is part of the kernel time (this is undocumented, but the calculation comes
  // out wrong otherwise.

  total_diff = kernel_diff + user_diff;

  if (idle_diff == 0 || total_diff == 0) {
    cpu_usage = p_cpu_usage;
  } else {
	  //printf("Idle diff: %llu Kernel diff: %llu User diff: %llu Total diff: %llu\n", idle_diff, kernel_diff, user_diff, total_diff);
    cpu_usage = ((float)(total_diff - idle_diff)) / ((float)total_diff);
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
  return (jlong) cpu_frequency;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_uptimeInSeconds (JNIEnv *env, jobject object)
{
  // only works up to 49.7 days. There's GetTickCount64 for Vista / Server 2008.
  return (jlong) GetTickCount() / 1000;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_currentPid (JNIEnv *env, jobject object)
{
  return (jint) current_pid;
}
