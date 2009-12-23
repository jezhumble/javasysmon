/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 15 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 */
#include "javasysmon.h"
#include <unistd.h>
#include <kstat.h>
#include <jni.h>

static int num_cpus;
static int pagesize;

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  num_cpus = sysconf(_SC_NPROCESSORS_ONLN);
  pagesize = sysconf(_SC_PAGESIZE);
  return JNI_VERSION_1_2;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    cpuUsage
 * Signature: ()J
 */
JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuUsage (JNIEnv *env, jobject obj)
{
  return (jfloat) 0;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_totalMemory (JNIEnv *env, jobject obj)
{
  return (jlong) 0;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    freeMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_freeMemory (JNIEnv *env, jobject obj)
{
  return (jlong) 0;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    totalSwap
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_totalSwap (JNIEnv *env, jobject obj)
{
  return (jlong) 0;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    freeSwap
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_freeSwap (JNIEnv *env, jobject obj)
{
  return (jlong) 0;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    numCpus
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_numCpus (JNIEnv *env, jobject obj)
{
  return (jint) num_cpus;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    cpuFrequency
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuFrequency (JNIEnv *env, jobject obj)
{
  kstat_ctl_t   *kc;  
  kstat_t       *ksp;  
  kstat_named_t *knp;  
  
  kc = kstat_open(); 
  ksp = kstat_lookup(kc, "cpu_info", -1, NULL);  
  kstat_read(kc, ksp, NULL);  
  /* lookup the CPU speed data record */  
  knp = kstat_data_lookup(ksp, "clock_MHz"); 
  return (jlong) 1000 * 1000 * knp->value.ui64;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    uptimeInSeconds
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_uptimeInSeconds (JNIEnv *env, jobject obj)
{
  struct timeval secs;
  kstat_ctl_t   *kc; 
  kstat_t       *ksp; 
  kstat_named_t *knp; 
  unsigned long long uptime;

  if (gettimeofday(&secs, NULL) != 0) {
    return (jlong) 0;
  }
  uptime = (unsigned long long) secs.tv_sec;

  kc = kstat_open();
  if ((ksp = kstat_lookup(kc, "unix", 0, "system_misc")) == NULL) {
    fprintf(stderr, "%s\n", "ERROR: Can't read boot time.");
    return 0;
  }
  if ((kstat_read(kc, ksp, NULL) != -1) &&
  /* lookup the boot time record */
    ((knp = kstat_data_lookup(ksp, "boot_time")) != NULL)) {
      return (jlong) (uptime - knp->value.ui32);
  } else {
    return 0;
  }
}

