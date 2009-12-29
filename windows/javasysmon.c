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
static ULONGLONG cpu_frequency;

static ULONGLONG filetime_to_int64 (FILETIME* filetime)
{
    ULARGE_INTEGER time;

    time.LowPart = filetime->dwLowDateTime;
    time.HighPart = filetime->dwHighDateTime;

    return time.QuadPart;
}

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
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

  return JNI_VERSION_1_2;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuTimes (JNIEnv *env, jobject obj)
{
  ULONGLONG idle, kernel, user;
  FILETIME idletime, kerneltime, usertime;
  jclass		cpu_times_class;
  jmethodID	cpu_times_constructor;
  jobject		cpu_times;

  GetSystemTimes(&idletime, &kerneltime, &usertime);
  idle = filetime_to_int64(&idletime);
  kernel = filetime_to_int64(&kerneltime) - idle;
  user = filetime_to_int64(&usertime);

  cpu_times_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/CpuTimes");
  cpu_times_constructor = (*env)->GetMethodID(env, cpu_times_class, "<init>", "(JJJ)V");
  cpu_times = (*env)->NewObject(env, cpu_times_class, cpu_times_constructor, (jlong) user, (jlong) kernel, (jlong) idle);
  (*env)->DeleteLocalRef(env, cpu_times_class);
  return cpu_times;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_physical (JNIEnv *env, jobject obj)
{
  MEMORYSTATUS status;
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;

  GlobalMemoryStatus (&status);
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) status.dwAvailPhys, (jlong) status.dwTotalPhys);
  (*env)->DeleteLocalRef(env, memory_stats_class);
  return memory_stats;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_swap (JNIEnv *env, jobject obj)
{
  MEMORYSTATUS status;
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;

  GlobalMemoryStatus (&status);
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) status.dwAvailVirtual, (jlong) status.dwTotalVirtual);
  (*env)->DeleteLocalRef(env, memory_stats_class);
  return memory_stats;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_numCpus (JNIEnv *env, jobject object)
{
  return (jint) num_cpu;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_cpuFrequencyInHz (JNIEnv *env, jobject object)
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
