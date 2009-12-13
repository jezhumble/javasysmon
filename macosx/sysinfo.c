/*
 *  sysinfo.c
 *  javanativetools
 *
 *  Created by Jez Humble on 12/10/09.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the BSD license.
 */

#include "sysinfo.h"
#include <mach/mach_types.h>
#include <mach/host_info.h>
#include <mach/mach_error.h>
#include <mach/mach_host.h>

JNIEXPORT jfloat JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_cpuUsage (JNIEnv *env, jobject obj)
{
	kern_return_t				error;
	mach_msg_type_number_t		count;
	struct host_cpu_load_info	r_load;
	unsigned long long			userticks, systicks, idleticks, totalticks;
	float						cpu_idle;
	jfloat						retval;
	
	count = HOST_CPU_LOAD_INFO_COUNT;
	error = host_statistics(mach_host_self(), HOST_CPU_LOAD_INFO, (host_info_t)&r_load, &count);
	if (error != KERN_SUCCESS) {
		printf("Error trying to get CPU usage: %s\n", mach_error_string(error));
		retval = (jfloat)0.0;
		goto RETURN;
	}
	userticks = r_load.cpu_ticks[CPU_STATE_USER] + r_load.cpu_ticks[CPU_STATE_NICE];
	systicks = r_load.cpu_ticks[CPU_STATE_SYSTEM];
	idleticks = r_load.cpu_ticks[CPU_STATE_IDLE];
	totalticks = userticks + systicks + idleticks;
	cpu_idle = ((float)(100 * idleticks)) / ((float)totalticks);
	
	retval = (jfloat)(100 - cpu_idle);
RETURN:
	return retval;
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_totalMemory (JNIEnv *env, jobject obj)
{
/*	kern_return_t				error;
	mach_msg_type_number_t		count;
	struct vm_statistics		r_vm_info;
	struct						pagesize;
	jint						retval;
	unsigned long long			mem_wired, mem_active, mem_inactive, mem_used, mem_free;
	
	count = sizeof(r_vm_info) / sizeof(natural_t);
	host_page_size(mach_host_self(), &pagesize);
	error = host_statistics(mach_host_self(), HOST_VM_INFO, (vm_statistics_data_t) &r_vm_info, &count);
	if (error != KERN_SUCCESS) {
		printf("Error trying to get total memory: %s\n", mach_error_string(error));
		retval = (jlong)0;
		goto RETURN;
	}

	mem_wired = (unsigned long long) r_vm_info.wire_count * samp_tsamp->pagesize;
	mem_active = (unsigned long long) r_vm_info.active_count * samp_tsamp->pagesize;
	mem_inactive = (unsigned long long) r_vm_info.inactive_count * samp_tsamp->pagesize;
	mem_used = (unsigned long long) mem_wired + mem_active + mem_inactive;
	mem_free = (unsigned long long) samp_tsamp->vm_stat.free_count * samp_tsamp->pagesize;
	
	retval = (jlong) mem_free + mem_used;
RETURN:
	return retval;
 */
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_freeMemory (JNIEnv *env, jobject obj)
{
}