/*
 *  sysinfo.c
 *  javanativetools
 *
 *  Created by Jez Humble on 10 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 */

#include "sysinfo.h"
#include <mach/mach_types.h>
#include <mach/host_info.h>
#include <mach/mach_error.h>
#include <mach/mach_host.h>
#include <sys/sysctl.h>

static int pageSize = 0;
static unsigned long long p_userticks, p_systicks, p_idleticks;
static float p_cpuusage;
static mach_port_t sysmonport;

int sample_cpu_ticks(host_info_t host_info)
{
	kern_return_t				error;
	mach_msg_type_number_t		count;
	
	count = HOST_CPU_LOAD_INFO_COUNT;
	error = host_statistics(sysmonport, HOST_CPU_LOAD_INFO, host_info, &count);
	if (error != KERN_SUCCESS) {
		printf("Error trying to get CPU usage: %s\n", mach_error_string(error));
		return 1;
	}
	return 0;
}

JNIEXPORT jint JNICALL JNI_OnLoad (JavaVM * vm, void * reserved)
{
	// Get some system information that won't change that we'll need later on
	int				mib[2];
	size_t			len;
	unsigned long long p_totalticks;
	sysmonport = mach_host_self();
	
	mib[0] = CTL_HW;
	mib[1] = HW_PAGESIZE;
	len = sizeof(pageSize);
	
	if (sysctl(mib, 2, &pageSize, &len, NULL, 0) == -1) {
		perror("sysctl");
	}
	
	// Sample CPU ticks
	host_cpu_load_info_data_t	cpu_stats;
	if (sample_cpu_ticks((host_info_t) &cpu_stats) == 0) {
		p_userticks = cpu_stats.cpu_ticks[CPU_STATE_USER] + cpu_stats.cpu_ticks[CPU_STATE_NICE];
		p_systicks = cpu_stats.cpu_ticks[CPU_STATE_SYSTEM];
		p_idleticks = cpu_stats.cpu_ticks[CPU_STATE_IDLE];
		p_totalticks = p_userticks + p_systicks + p_idleticks;
		p_cpuusage = ((float)1) - ((float)p_idleticks) / ((float)p_totalticks);

	} else {
		p_userticks = p_systicks = p_idleticks = p_cpuusage = 0;
	}
	
	return JNI_VERSION_1_2;
}

JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_cpuUsage (JNIEnv *env, jobject obj)
{
	host_cpu_load_info_data_t	cpu_stats;
	unsigned long long			userticks, systicks, idleticks, totalticks, p_totalticks;
	float						cpuusage;
	
	if (sample_cpu_ticks((host_info_t) &cpu_stats) == 0) {
		// Get current ticks
		userticks = cpu_stats.cpu_ticks[CPU_STATE_USER] + cpu_stats.cpu_ticks[CPU_STATE_NICE];
		systicks = cpu_stats.cpu_ticks[CPU_STATE_SYSTEM];
		idleticks = cpu_stats.cpu_ticks[CPU_STATE_IDLE];
		
		// Calculate difference
		totalticks = userticks + systicks + idleticks;
		p_totalticks = p_userticks + p_systicks + p_idleticks;
		if (totalticks == p_totalticks || idleticks == p_idleticks) {
			cpuusage = p_cpuusage;
		} else {
			cpuusage = ((float)1) - ((float)(idleticks - p_idleticks)) / ((float)(totalticks - p_totalticks));
			
			// Reset counters
			p_userticks = userticks;
			p_systicks = systicks;
			p_idleticks = idleticks;
			p_cpuusage = cpuusage;
		}
		
		return (jfloat)cpuusage;
	} else {
		return (jfloat)0;
	}
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_totalMemory (JNIEnv *env, jobject obj)
{
	int					mib[2];
	size_t				len;
	unsigned long long	totalMemory;
	
	mib[0] = CTL_HW;
	mib[1] = HW_MEMSIZE;
	len = sizeof(totalMemory);

	if (sysctl(mib, 2, &totalMemory, &len, NULL, 0) != 0) {
		perror("sysctl");
		totalMemory = 0;
	}
	
	return (jlong)totalMemory;		
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_freeMemory (JNIEnv *env, jobject obj)
{
	 kern_return_t				error;
	 mach_msg_type_number_t		count;
	 struct vm_statistics		r_vm_info;
	 unsigned long long			mem_free;
	 
	 count = sizeof(r_vm_info) / sizeof(natural_t);

	 error = host_statistics(sysmonport, HOST_VM_INFO, (host_info_t) &r_vm_info, &count);

	if (error != KERN_SUCCESS) {
		 printf("Error trying to get free memory: %s\n", mach_error_string(error));
		 mem_free = 0;
		 goto RETURN;
	 }
	 
	 mem_free = (unsigned long long) r_vm_info.free_count * pageSize;
	 
	 RETURN:
	 return (jlong) mem_free;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_totalSwap (JNIEnv *env, jobject obj)
{
	int					mib[2];
	size_t				len;
	struct xsw_usage	xsu;
	
	mib[0] = CTL_VM;
	mib[1] = VM_SWAPUSAGE;
	len = sizeof(xsu);
	
	if (sysctl(mib, 2, &xsu, &len, NULL, 0) != 0) {
		perror("sysctl");
		return (jlong) 0;
	}
	
	return (jlong) xsu.xsu_total;			
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_freeSwap (JNIEnv *env, jobject obj)
{
	int					mib[2];
	size_t				len;
	struct xsw_usage	xsu;
	
	mib[0] = CTL_VM;
	mib[1] = VM_SWAPUSAGE;
	len = sizeof(xsu);
	
	if (sysctl(mib, 2, &xsu, &len, NULL, 0) != 0) {
		perror("sysctl");
		return (jlong) 0;
	}
	
	return (jlong) xsu.xsu_avail;	
}
