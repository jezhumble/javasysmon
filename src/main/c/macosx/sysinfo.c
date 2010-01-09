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
#include <mach/task.h>
#include <sys/sysctl.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <stdbool.h>
#include <pwd.h>
#include <signal.h>

static int pageSize = 0;
static mach_port_t sysmonport;
static unsigned long long total_memory, ticks_per_second;
typedef struct kinfo_proc kinfo_proc;

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
	sysmonport = mach_host_self();
	ticks_per_second = sysconf(_SC_CLK_TCK);
	
	mib[0] = CTL_HW;
	mib[1] = HW_PAGESIZE;
	len = sizeof(pageSize);
	
	if (sysctl(mib, 2, &pageSize, &len, NULL, 0) == -1) {
		perror("sysctl");
	}
	
	mib[0] = CTL_HW;
	mib[1] = HW_MEMSIZE;
	len = sizeof(total_memory);
	
	if (sysctl(mib, 2, &total_memory, &len, NULL, 0) != 0) {
		perror("sysctl");
	}	
	
	return JNI_VERSION_1_2;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_cpuTimes (JNIEnv *env, jobject obj)
{
	host_cpu_load_info_data_t	cpu_stats;
	unsigned long long			userticks, systicks, idleticks;
	jclass		cpu_times_class;
	jmethodID	cpu_times_constructor;
	jobject		cpu_times;
	
	if (sample_cpu_ticks((host_info_t) &cpu_stats) == 0) {
		userticks = cpu_stats.cpu_ticks[CPU_STATE_USER] + cpu_stats.cpu_ticks[CPU_STATE_NICE];
		systicks = cpu_stats.cpu_ticks[CPU_STATE_SYSTEM];
		idleticks = cpu_stats.cpu_ticks[CPU_STATE_IDLE];
	
		cpu_times_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/CpuTimes");
		cpu_times_constructor = (*env)->GetMethodID(env, cpu_times_class, "<init>", "(JJJ)V");
		cpu_times = (*env)->NewObject(env, cpu_times_class, cpu_times_constructor,
									  (jlong) (userticks * 1000) / ticks_per_second,
									  (jlong) (systicks * 1000) / ticks_per_second,
									  (jlong) (idleticks * 1000) / ticks_per_second);
		(*env)->DeleteLocalRef(env, cpu_times_class);
		return cpu_times;
	} else {
		return NULL;
	}
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_physical (JNIEnv *env, jobject obj)
{
	kern_return_t				error;
	mach_msg_type_number_t		count;
	struct vm_statistics		r_vm_info;
	unsigned long long			mem_free;
	jclass		memory_stats_class;
	jmethodID	memory_stats_constructor;
	jobject		memory_stats;
	
	count = sizeof(r_vm_info) / sizeof(natural_t);
	error = host_statistics(sysmonport, HOST_VM_INFO, (host_info_t) &r_vm_info, &count);
	if (error != KERN_SUCCESS) {
		printf("Error trying to get free memory: %s\n", mach_error_string(error));
		return NULL;
	}
	mem_free = (unsigned long long) r_vm_info.free_count * pageSize;
	 
	memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
	memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
	memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) mem_free, (jlong) total_memory);
	(*env)->DeleteLocalRef(env, memory_stats_class);
	return memory_stats;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_swap (JNIEnv *env, jobject obj)
{
	int					mib[2];
	size_t				len;
	struct xsw_usage	xsu;
	jclass		memory_stats_class;
	jmethodID	memory_stats_constructor;
	jobject		memory_stats;
	
	mib[0] = CTL_VM;
	mib[1] = VM_SWAPUSAGE;
	len = sizeof(xsu);
	
	if (sysctl(mib, 2, &xsu, &len, NULL, 0) != 0) {
		perror("sysctl");
		return (jlong) 0;
	}

	memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
	memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
	memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor, (jlong) xsu.xsu_avail, (jlong) xsu.xsu_total);
	(*env)->DeleteLocalRef(env, memory_stats_class);
	return memory_stats;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_numCpus (JNIEnv *env, jobject object)
{
	return (jint) sysconf(_SC_NPROCESSORS_CONF);	
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_cpuFrequencyInHz (JNIEnv *env, jobject object)
{
	int					mib[2];
	size_t				len;
	unsigned int		cpu_freq;
	
	mib[0] = CTL_HW;
	mib[1] = HW_CPU_FREQ;
	len = sizeof(cpu_freq);
	
	if (sysctl(mib, 2, &cpu_freq, &len, NULL, 0) != 0) {
		perror("sysctl");
		return (jlong) 0;
	}
	
	return (jlong) cpu_freq;	
}

JNIEXPORT jlong JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_uptimeInSeconds  (JNIEnv *env, jobject object)
{
	int					mib[2];
	size_t				len;
	struct timeval		secs;
	unsigned long long	uptime;
	
	if (gettimeofday(&secs, NULL) != 0) {
		perror("gettimeofday");
		return (jlong) 0;
	}
	
	uptime = (unsigned long long) secs.tv_sec;
	
	mib[0] = CTL_KERN;
	mib[1] = KERN_BOOTTIME;
	len = sizeof(secs);
	
	if (sysctl(mib, 2, &secs, &len, NULL, 0) != 0) {
		perror("sysctl");
		return (jlong) 0;
	}
	
	uptime -= (unsigned long long) secs.tv_sec; 
	
	return (jlong) uptime;
}

JNIEXPORT jint JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_currentPid (JNIEnv *env, jobject object)
{
	return (jint) getpid();
}

unsigned long long get_millisecs(struct timeval timeval) {
	return timeval.tv_sec * 1000 + timeval.tv_usec / 1000;
}

JNIEXPORT jobjectArray JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_processTable (JNIEnv *env, jobject object)
{
	jclass		process_info_class;
	jmethodID	process_info_constructor;
	jobject		process_info;
	jobjectArray process_info_array;
	kinfo_proc *        result;
	struct task_basic_info tasks_info;
	struct rusage		rusage;
	struct passwd		* passwd;
	bool                done, is_me;
	unsigned int		info_count = TASK_BASIC_INFO_COUNT;
	static const int    name[] = { CTL_KERN, KERN_PROC, KERN_PROC_ALL, 0 };
	size_t              length;
	int count, i, err;
	
	// Thanks to http://developer.apple.com/mac/library/qa/qa2001/qa1123.html
	// This sysctl is the only way to do this on OSX without having elevated privileges
	
	result = NULL;
	done = false;
	do {
		if (result != NULL) {
			return NULL;
		}
			
		// Call sysctl with a NULL buffer.
		
		length = 0;
		err = sysctl( (int *) name, (sizeof(name) / sizeof(*name)) - 1,
					 NULL, &length,
					 NULL, 0);
		if (err == -1) {
			err = errno;
		}
		
		// Allocate an appropriately sized buffer based on the results
		// from the previous call.
		
		if (err == 0) {
			result = malloc(length);
			if (result == NULL) {
				err = ENOMEM;
			}
		}
		
		// Call sysctl again with the new buffer.  If we get an ENOMEM
		// error, toss away our buffer and start again.
		
		if (err == 0) {
			err = sysctl( (int *) name, (sizeof(name) / sizeof(*name)) - 1,
							 result, &length,
						 NULL, 0);
			if (err == -1) {
				err = errno;
			}
			if (err == 0) {
				done = true;
			} else if (err == ENOMEM) {
				free(result);
				result = NULL;
				err = 0;
			}
		}
	} while (err == 0 && ! done);
	
	// This only works for me (since Tiger) unless you have elevated privileges. Lame.
	task_info(mach_task_self(), TASK_BASIC_INFO, (task_info_t) &tasks_info, &info_count);	
	getrusage(RUSAGE_SELF, &rusage);
	
	// if anybody wants to get the actual command that started the process, check out:
	// http://cpansearch.perl.org/src/DURIST/Proc-ProcessTable-0.42/os/darwin.c

	if (err == 0) {
		count = length / sizeof(kinfo_proc);
		process_info_array = (*env)->NewObjectArray(env, count, (*env)->FindClass(env, "com/jezhumble/javasysmon/ProcessInfo"), NULL);
		process_info_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/ProcessInfo");
		process_info_constructor = (*env)->GetMethodID(env, process_info_class, "<init>",
													   "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;JJJJ)V");
		for (i = 0; i < count; i++) {
			is_me = getpid() == result[i].kp_proc.p_pid;
			passwd = getpwuid(result[i].kp_eproc.e_pcred.p_ruid);
			process_info = (*env)->NewObject(env, process_info_class, process_info_constructor,
											 (jint) result[i].kp_proc.p_pid,
											 (jint) result[i].kp_eproc.e_ppid,
											 (*env)->NewStringUTF(env, ""),
											 (*env)->NewStringUTF(env, result[i].kp_proc.p_comm),
											 (*env)->NewStringUTF(env, passwd->pw_name),
											 (jlong) is_me ? get_millisecs(rusage.ru_utime) : 0,
											 (jlong) is_me ? get_millisecs(rusage.ru_stime) : 0,
											 (jlong) is_me ? tasks_info.resident_size : 0,
											 (jlong) is_me ? tasks_info.virtual_size : 0);
			(*env)->SetObjectArrayElement(env, process_info_array, i, process_info);
		}
		(*env)->DeleteLocalRef(env, process_info_class);
	}
		
	if (result != NULL) {
		free(result);
		return process_info_array;
	} 
	
	return NULL;
}

JNIEXPORT void JNICALL Java_com_jezhumble_javasysmon_MacOsXMonitor_killProcess (JNIEnv *env, jobject object, jint pid) {
	kill(pid, SIGTERM);
}
