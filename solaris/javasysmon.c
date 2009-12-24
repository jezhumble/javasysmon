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
#include <jni.h>

#define MAXSTRSIZE 80

static int num_cpus;
static int pagesize;
static unsigned long long phys_mem, p_idle_ticks, p_total_ticks;
static float p_cpu_usage;

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
  kstat_ctl_t   *kc;  
  kstat_t       *ksp;  
  cpu_sys_stats_t *cpu_stats;
  int i;
  unsigned long long idle_ticks, total_ticks;

  num_cpus = sysconf(_SC_NPROCESSORS_ONLN);
  pagesize = sysconf(_SC_PAGESIZE);
  phys_mem = sysconf(_SC_PHYS_PAGES) * pagesize;
  idle_ticks = total_ticks = 0;

  kc = kstat_open();
  for (i = 0; i < num_cpus; i++) {
    if ((ksp = kstat_lookup(kc, "cpu", i, "sys")) != NULL) {
      kstat_read(kc, ksp, NULL);
      cpu_stats = (cpu_sys_stats_t *)(ksp->ks_data);
      idle_ticks += cpu_stats->cpu_ticks_idle;
      total_ticks += cpu_stats->cpu_ticks_idle;
      total_ticks += cpu_stats->cpu_ticks_user;
      total_ticks += cpu_stats->cpu_ticks_kernel;
      total_ticks += cpu_stats->cpu_ticks_wait;
    }
  }
  p_idle_ticks = idle_ticks;
  p_total_ticks = total_ticks;
  p_cpu_usage = 0;

  printf("%s\n", "Starting");
  printf("Idle: %llu Total: %llu\n", idle_ticks, total_ticks);

  return JNI_VERSION_1_2;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    cpuUsage
 * Signature: ()J
 */
JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_cpuUsage (JNIEnv *env, jobject obj)
{
  kstat_ctl_t   *kc;  
  kstat_t       *ksp;  
  cpu_sys_stats_t *cpu_stats;
  int i;
  unsigned long long idle_ticks, total_ticks;

  idle_ticks = total_ticks = 0;

  kc = kstat_open();
  for (i = 0; i < num_cpus; i++) {
    if ((ksp = kstat_lookup(kc, "cpu", i, "sys")) != NULL) {
      kstat_read(kc, ksp, NULL);
      cpu_stats = (cpu_sys_stats_t *)(ksp->ks_data);
      idle_ticks += cpu_stats->cpu_ticks_idle;
      total_ticks += cpu_stats->cpu_ticks_idle;
      total_ticks += cpu_stats->cpu_ticks_user;
      total_ticks += cpu_stats->cpu_ticks_kernel;
      total_ticks += cpu_stats->cpu_ticks_wait;
    }
  }

  printf("Idle: %llu Total: %llu\n", idle_ticks, total_ticks);
  if (idle_ticks != p_idle_ticks && total_ticks != p_total_ticks) {
    p_cpu_usage = ((float)1) - ((float)(idle_ticks - p_idle_ticks)) / ((float)(total_ticks - p_total_ticks));
    p_idle_ticks = idle_ticks;
    p_total_ticks = total_ticks;
  }

  return (jfloat) p_cpu_usage;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_totalMemory (JNIEnv *env, jobject obj)
{
  return (jlong) phys_mem;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    freeMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_freeMemory (JNIEnv *env, jobject obj)
{
  return (jlong) sysconf(_SC_AVPHYS_PAGES) * pagesize;
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

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    totalSwap
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_totalSwap (JNIEnv *env, jobject obj)
{
  unsigned long long total_swap, free_swap;
  total_swap = free_swap = 0;
  get_swap_stats(&total_swap, &free_swap);
  return (jlong) total_swap;
}

/*
 * Class:     com_jezhumble_javasysmon_SolarisMonitor
 * Method:    freeSwap
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_SolarisMonitor_freeSwap (JNIEnv *env, jobject obj)
{
  unsigned long long total_swap, free_swap;
  total_swap = free_swap = 0;
  get_swap_stats(&total_swap, &free_swap);
  return (jlong) free_swap;
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

