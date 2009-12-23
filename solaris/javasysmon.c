/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 15 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 */
#include "javasysmon.h"
#include <kstat.h>
#include <jni.h>

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
  return (jint) 0;
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
  return (jlong) 0;
}
