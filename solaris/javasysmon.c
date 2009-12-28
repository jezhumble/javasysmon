/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 15 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 *  Thanks to: http://getthegood.com/TechNotes/Papers/ProcStatistics.html
 */
#include "javasysmon.h"
#include <unistd.h>
#include <stdlib.h>
#include <sys/sysinfo.h>
#include <sys/swap.h>
#include <kstat.h>
#include <stdio.h>
#include <string.h>
#include <jni.h>

#define MAXSTRSIZE 80

static int num_cpus;
static int pagesize;
static unsigned long long phys_mem;

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  num_cpus = sysconf(_SC_NPROCESSORS_ONLN);
  pagesize = sysconf(_SC_PAGESIZE);
  phys_mem = sysconf(_SC_PHYS_PAGES) * pagesize;

  return JNI_VERSION_1_2;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuTimes (JNIEnv *env, jobject obj)
{
  kstat_ctl_t   *kc;
  kstat_t       *ksp;
  struct cpu_stat cpu_stats;
  int i;
  unsigned long long userticks, systicks, idleticks;
  jclass		cpu_times_class;
  jmethodID	cpu_times_constructor;
  jobject		cpu_times;

  kc = kstat_open();
  for (i = 0; i < num_cpus; i++) {
    // the next line is wrong: should replace "cpu_stat0" with "cpu_stati"
    // but my C-fu is insufficient to work out how to do this in <10 lines
    if ((ksp = kstat_lookup(kc, "cpu_stat", -1, "cpu_stat0")) != NULL) {
      kstat_read(kc, ksp, &cpu_stats);
      idleticks += cpu_stats.cpu_sysinfo.cpu[CPU_IDLE];
      userticks += cpu_stats.cpu_sysinfo.cpu[CPU_USER];
      systicks += cpu_stats.cpu_sysinfo.cpu[CPU_KERNEL];
      systicks += cpu_stats.cpu_sysinfo.cpu[CPU_WAIT];
    }
  }

  cpu_times_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/CpuTimes");
  cpu_times_constructor = (*env)->GetMethodID(env, cpu_times_class, "<init>", "(JJJ)V");
  cpu_times = (*env)->NewObject(env, cpu_times_class, cpu_times_constructor, (jlong) userticks, (jlong) systicks, (jlong) idleticks);
  (*env)->DeleteLocalRef(env, cpu_times_class);
  return cpu_times;
}


JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_physical (JNIEnv *env, jobject obj)
{
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;
  unsigned long long free_mem;

  free_mem = sysconf(_SC_AVPHYS_PAGES) * pagesize;
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) free_mem, (jlong) phys_mem);
  (*env)->DeleteLocalRef(env, memory_stats_class);
  return memory_stats;
}

int get_swap_stats(unsigned long long *total_swap, unsigned long long *free_swap)
{
  swaptbl_t	*swaptbl;
  swapent_t	*swapent;
  int i, n, num_entries; 
  char *strtab;

  n = num_entries = i = 0;
again:
  if ((num_entries = swapctl(SC_GETNSWP, NULL)) == -1) {
    perror("swapctl: GETNSWP");
    return 1;
  }
  if (num_entries == 0) {
    fprintf(stderr, "No swap devices configures\n");
    return 2;
  }
  if ((swaptbl = (swaptbl_t *) malloc(num_entries * sizeof(swapent_t) +
      sizeof(struct swaptable))) == (void *) 0) {
    fprintf(stderr, "Malloc failed\n");
    return 3;
  }
  /* allocate num+1 string holders */
  if ((strtab = (char *) malloc((num_entries + 1) * MAXSTRSIZE)) == (void *) 0) {
    free(swaptbl);
    fprintf(stderr, "Malloc Failed\n");
    return 4;
  }
  /* initialize string pointers */
  for (i = 0; i < (num_entries + 1); i++) {
    swaptbl->swt_ent[i].ste_path = strtab + (i * MAXSTRSIZE);
  }
  swaptbl->swt_n = num_entries + 1;
  if ((n = swapctl(SC_LIST, swaptbl)) < 0) {
    perror("swapctl");
    free(swaptbl);
    free(strtab);
    return 5;
  }
  if (n > num_entries) {
    free(swaptbl);
    free(strtab);
    goto again;
  }
  for (i = 0; i < n; i++) {
    *total_swap += swaptbl->swt_ent[i].ste_pages * pagesize;
    *free_swap += swaptbl->swt_ent[i].ste_free * pagesize;
  }
  free(swaptbl);
  free(strtab);
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_swap (JNIEnv *env, jobject obj)
{
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;
  unsigned long long total_swap, free_swap;
  total_swap = free_swap = 0;
  get_swap_stats(&total_swap, &free_swap);

  free_mem = sysconf(_SC_AVPHYS_PAGES) * pagesize;
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) free_swap, (jlong) total_swap);
  (*env)->DeleteLocalRef(env, memory_stats_class);
  return memory_stats;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_numCpus (JNIEnv *env, jobject obj)
{
  return (jint) num_cpus;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_cpuFrequencyInHz (JNIEnv *env, jobject obj)
{
  kstat_ctl_t   *kc;  
  kstat_t       *ksp;  
  kstat_named_t *knp;  
  
  kc = kstat_open(); 
  if ((ksp = kstat_lookup(kc, "cpu_info", -1, NULL)) == NULL) {  
    fprintf(stderr, "%s\n", "ERROR: Can't read cpu frequency.");
    return 0;
  }
  if ((kstat_read(kc, ksp, NULL) != -1) &&  
  /* lookup the CPU speed data record */  
    ((knp = kstat_data_lookup(ksp, "clock_MHz")) != NULL)) {
      return (jlong) 1000 * 1000 * knp->value.ui64;
  } else {
    return 0;
  }
}

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

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_currentPid (JNIEnv *env, jobject obj)
{
  return (jint) getpid();
}


