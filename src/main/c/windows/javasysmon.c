/*
 *  javasysmon.c
 *  javanativetools
 *
 *  Created by Jez Humble on 15 Dec 2009.
 *  Copyright 2009 Jez Humble. All rights reserved.
 *  Licensed under the terms of the New BSD license.
 *  TODO: Error checking
 */
#define _WIN64_WINNT 0x0501
#include <jni.h>
#include <windows.h>
#include <winbase.h>
#include <tchar.h>
#include <psapi.h>
#include <tlhelp32.h>
#include <Sddl.h>

static SYSTEM_INFO system_info;
static int num_cpu;
static DWORD current_pid;
static ULONGLONG cpu_frequency;

static ULONGLONG filetime_to_millis (FILETIME* filetime)
{
    ULARGE_INTEGER time;

    time.LowPart = filetime->dwLowDateTime;
    time.HighPart = filetime->dwHighDateTime;

    return time.QuadPart / 10000;
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
  idle = filetime_to_millis(&idletime);
  kernel = filetime_to_millis(&kerneltime) - idle;
  user = filetime_to_millis(&usertime);

  cpu_times_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/CpuTimes");
  cpu_times_constructor = (*env)->GetMethodID(env, cpu_times_class, "<init>", "(JJJ)V");
  cpu_times = (*env)->NewObject(env, cpu_times_class, cpu_times_constructor, (jlong) user, (jlong) kernel, (jlong) idle);
  (*env)->DeleteLocalRef(env, cpu_times_class);
  return cpu_times;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_physical (JNIEnv *env, jobject obj)
{
  PERFORMANCE_INFORMATION perfinfo;
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;
  DWORD			pagesize;

  GetPerformanceInfo (&perfinfo, sizeof(perfinfo));
  pagesize = perfinfo.PageSize;
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor,
	  (jlong) (pagesize * perfinfo.PhysicalAvailable),
	  (jlong) (pagesize * perfinfo.PhysicalTotal));
  (*env)->DeleteLocalRef(env, memory_stats_class);
  return memory_stats;
}

JNIEXPORT jobject JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_swap (JNIEnv *env, jobject obj)
{
  PERFORMANCE_INFORMATION perfinfo;
  jclass		memory_stats_class;
  jmethodID	memory_stats_constructor;
  jobject		memory_stats;
  DWORD			pagesize;

  GetPerformanceInfo (&perfinfo, sizeof(perfinfo));
  pagesize = perfinfo.PageSize;
  memory_stats_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/MemoryStats");
  memory_stats_constructor = (*env)->GetMethodID(env, memory_stats_class, "<init>", "(JJ)V");
  memory_stats = (*env)->NewObject(env, memory_stats_class, memory_stats_constructor,
	  (jlong) (pagesize * perfinfo.CommitTotal),
	  (jlong) (pagesize * perfinfo.CommitLimit));
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

/**
 *	Converts the given UTF-16 string to UTF-8. Returns ZERO upon success, NON-ZERO otherwise.
 *	Upon success, the parameter utf8 points to the UTF-8 string. It is the calling function's responsibility that this resource is freed.
 *  Upon error the parameter utf8 will be set to NULL.
 */
DWORD WideCharToUTF8(wchar_t* utf16, char** utf8, DWORD* out_utf8_len)
{
	int utf8_length;

	*utf8 = NULL;
	*out_utf8_len = 0;

	utf8_length = WideCharToMultiByte(
		CP_UTF8,           // Convert to UTF-8
		0,                 // No special character conversions required
		// (UTF-16 and UTF-8 support the same characters)
		utf16,             // UTF-16 string to convert
		-1,                // utf16 is NULL terminated (if not, use length)
		NULL,              // Determining correct output buffer size
		0,                 // Determining correct output buffer size
		NULL,              // Must be NULL for CP_UTF8
		NULL);             // Must be NULL for CP_UTF8

	if (utf8_length == 0) {
		// Error - call GetLastError for details
		return GetLastError();
	}

	*utf8 = (char*)malloc(sizeof(char) * utf8_length); // Allocate space for UTF-8 string

	utf8_length = WideCharToMultiByte(
		CP_UTF8,           // Convert to UTF-8
		0,                 // No special character conversions required
		// (UTF-16 and UTF-8 support the same characters)
		utf16,             // UTF-16 string to convert
		-1,                // utf16 is NULL terminated (if not, use length)
		*utf8,              // UTF-8 output buffer
		utf8_length,       // UTF-8 output buffer size
		NULL,              // Must be NULL for CP_UTF8
		NULL);             // Must be NULL for CP_UTF8

	if (utf8_length == 0) {
		// Error - call GetLastError for details
		free(*utf8);
		*utf8 = NULL;
		return GetLastError();
	}

	*out_utf8_len = utf8_length;

	return 0;
}

typedef struct _LSA_UNICODE_STRING {
	USHORT Length;
	USHORT MaximumLength;
	PWSTR  Buffer;
} LSA_UNICODE_STRING, *PLSA_UNICODE_STRING, UNICODE_STRING, *PUNICODE_STRING;

typedef NTSTATUS (NTAPI *_NtQueryInformationProcess)(
	HANDLE ProcessHandle,
	DWORD ProcessInformationClass,
	PVOID ProcessInformation,
	DWORD ProcessInformationLength,
	PDWORD ReturnLength
	);

typedef struct _PROCESS_BASIC_INFORMATION
{
	LONG ExitStatus;
	PVOID PebBaseAddress;
	ULONG_PTR AffinityMask;
	LONG BasePriority;
	ULONG_PTR UniqueProcessId;
	ULONG_PTR ParentProcessId;
} PROCESS_BASIC_INFORMATION, *PPROCESS_BASIC_INFORMATION;

/**
 *	Retrieve the original command line from the process environment block (PEB). Returns ZERO upon success, NON-ZERO otherwise.
 *	Upon success, the parameter commandLine points to the original command line string (UTF-16) that was used to start the given process.
 *  It is the calling function's responsibility that this resource is freed.
 *  Upon error the parameter commandLine will be set to NULL.
 */
DWORD GetCommandLineFromPeb(DWORD dwPid, wchar_t** commandLine)
{
	DWORD dw;
#ifdef _WIN64
	SIZE_T read;
#else
	DWORD read;
#endif
	HANDLE hProcess;
	_NtQueryInformationProcess pNtQip;
	PROCESS_BASIC_INFORMATION pbInfo;
	UNICODE_STRING cmdline;
	WCHAR* wcmdLine;

	*commandLine = NULL;

	hProcess = OpenProcess( PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, dwPid );
	if( !hProcess ) {
		return GetLastError();
	}

	pNtQip = (_NtQueryInformationProcess) GetProcAddress(GetModuleHandleA("ntdll.dll"),
		"NtQueryInformationProcess");
	if(!pNtQip) {
		CloseHandle(hProcess);
		return GetLastError();
	}

	pNtQip(hProcess, 0, &pbInfo, sizeof(pbInfo), NULL);

#ifdef _WIN64
	ReadProcessMemory(hProcess, (PCHAR)(pbInfo.PebBaseAddress) + 0x20, &dw, sizeof(dw), &read);
#else
	ReadProcessMemory(hProcess, (PCHAR)(pbInfo.PebBaseAddress) + 0x10, &dw, sizeof(dw),	&read);
#endif

#ifdef _WIN64
	ReadProcessMemory(hProcess, (PCHAR)dw+112, &cmdline, sizeof(cmdline), &read);
#else
	ReadProcessMemory(hProcess, (PCHAR)dw+64, &cmdline, sizeof(cmdline), &read);
#endif

	wcmdLine = (WCHAR *)malloc(sizeof(char)*(cmdline.Length + 2));
	if( !wcmdLine ) {
		CloseHandle(hProcess);
		return -1;
        }

	ReadProcessMemory(hProcess, (PVOID)cmdline.Buffer, wcmdLine,
		cmdline.Length+2, &read);

	*commandLine = wcmdLine;

	CloseHandle(hProcess);

	return 0;
}

/**
 *	Retrieve the original command line from the process environment block (PEB). Returns ZERO upon success, NON-ZERO otherwise.
 *
 *	This function is similar to GetCommandLineFromPeb, but returns an UTF-8 string instead.
 */
DWORD GetCommandLineUTF8(DWORD dwPid, char** utf8CommandLine, DWORD* process_command_len) {
	wchar_t* wcCommandLine;

	if(!GetCommandLineFromPeb(dwPid, &wcCommandLine)) {
		if(!WideCharToUTF8(wcCommandLine, utf8CommandLine, process_command_len)) {
			free(wcCommandLine); wcCommandLine = NULL;
			return 0;
		} else {
			free(wcCommandLine); wcCommandLine = NULL;
		}
	}

	return GetLastError();
}

JNIEXPORT jobjectArray JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_processTable (JNIEnv *env, jobject object)
{
	jclass		process_info_class;
	jmethodID	process_info_constructor;
	jobject		process_info;
	jobjectArray    process_info_array;
	DWORD           processes[1024], buffer_size, count;
	SIZE_T			working_set_size, pagefile_usage;
	unsigned int    i, ppid;
    TCHAR           process_name[MAX_PATH] = TEXT("<unknown>");
    DWORD           process_name_len = 0;
    TCHAR           process_command[MAX_PATH+1] = TEXT("<unknown>");
	char*			process_command_raw;
	DWORD           process_command_len;
    TCHAR			user_name[MAX_PATH] = TEXT("<unknown>");
    TCHAR			domain_name[MAX_PATH] = TEXT("<unknown>");
    FILETIME        created, exit, kernel, user;
    HANDLE          process, snapshot, token;
    PTOKEN_USER     user_token;
	HMODULE         module;
	PROCESS_MEMORY_COUNTERS pmc;
	PROCESSENTRY32  process_entry;
	SID_NAME_USE	sid_name_use;

	snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	process_entry.dwSize = sizeof(PROCESSENTRY32);
	if (!EnumProcesses(processes, sizeof(processes), &buffer_size))
	  return NULL;
        count = buffer_size / sizeof(DWORD);
	process_info_array = (*env)->NewObjectArray(env, count,
		(*env)->FindClass(env, "com/jezhumble/javasysmon/ProcessInfo"), NULL);

	for (i = 0; i <count; i++) {
	  working_set_size = pagefile_usage = ppid = 0;
          process_name_len = 0;
	  process_command_raw = NULL;
	  process_command_len = 0;
	  user_token = NULL;
	  // You can't get ppid from the usual PSAPI calls, so you need to use the ToolHelp stuff
	  // Thanks to http://www.codeproject.com/KB/threads/ParentPID.aspx?msg=1637993 for the tip
	  if (Process32First(snapshot, &process_entry)) {
	    do {
	      if (processes[i] == process_entry.th32ProcessID) {
		ppid = process_entry.th32ParentProcessID;
		break;
	      }
	    } while (Process32Next(snapshot, &process_entry));
	  }
	  // if we can open the process (doesn't work for system idle process and CSRSS without elevated privileges)
          if (processes[i] != 0 && (process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, processes[i])) != NULL) {
	    // get process name
	    if (EnumProcessModules(process, &module, sizeof(module), &buffer_size)) {
	      process_name_len = GetModuleBaseName(process, module, process_name, sizeof(process_name) / sizeof(TCHAR));
	    }
	    // get command name and copy to memory on the stack
		GetCommandLineUTF8(processes[i], &process_command_raw, &process_command_len);
		if (process_command_raw) {
			if (process_command_len > MAX_PATH) {
				process_command_len = MAX_PATH;
			}
			memcpy(process_command, process_command_raw, process_command_len);
			free(process_command_raw); process_command_raw = 0;
		}
	    // get CPU usage
	    GetProcessTimes(process, &created, &exit, &kernel, &user);
	    // get owner (thanks to http://www.codeproject.com/KB/cs/processownersid.aspx)
	    if (OpenProcessToken(process, TOKEN_QUERY, &token)) {
			GetTokenInformation(token, TokenUser, (LPVOID) user_token, 0, &buffer_size);
			if (GetLastError() != ERROR_INSUFFICIENT_BUFFER) {
				goto cleanup;
			}
		  user_token = (PTOKEN_USER) HeapAlloc(GetProcessHeap(), HEAP_ZERO_MEMORY, buffer_size);
		  if (user_token == NULL) {
			  goto cleanup;
		  }
		  if (!GetTokenInformation(token, TokenUser, (LPVOID) user_token, buffer_size, &buffer_size)) {
			  goto cleanup;
		  }
		  if (!LookupAccountSid(NULL, user_token->User.Sid, user_name, &buffer_size, domain_name, &buffer_size, &sid_name_use)) {
			  goto cleanup;
		  }
		  strcat(domain_name, "\\");
		  strcat(domain_name, user_name);
cleanup:
	      CloseHandle(token);
		  if (user_token != NULL) {
			  HeapFree(GetProcessHeap(), 0, (LPVOID) user_token);
		  }
		}
	    // get memory usage
	    if (GetProcessMemoryInfo(process, &pmc, sizeof(pmc))) {
	      working_set_size = pmc.WorkingSetSize;
	      pagefile_usage = pmc.PagefileUsage;
            }
	    CloseHandle(process);
	  }
	  process_info_class = (*env)->FindClass(env, "com/jezhumble/javasysmon/ProcessInfo");
	  process_info_constructor = (*env)->GetMethodID(env, process_info_class, "<init>",
							 "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;JJJJ)V");
	  process_info = (*env)->NewObject(env, process_info_class, process_info_constructor, (jint) processes[i],
					   (jint) ppid, // parent id
					   (*env)->NewStringUTF(env, process_command_len == 0 ? "<unknown>" : process_command), // command
					   (*env)->NewStringUTF(env, process_name_len == 0 ? "<unknown>" : process_name), // name
					   (*env)->NewStringUTF(env, user_token == NULL ? "<unknown>" : domain_name), // owner
					   (jlong) filetime_to_millis(&user), // user millis
					   (jlong) filetime_to_millis(&kernel), // system millis
					   (jlong) working_set_size, // resident bytes
					   (jlong) working_set_size + pagefile_usage); // total bytes
	  (*env)->SetObjectArrayElement(env, process_info_array, i, process_info);
	  (*env)->DeleteLocalRef(env, process_info_class);
	}
	  CloseHandle(snapshot);
	return process_info_array;
}

JNIEXPORT void JNICALL Java_com_jezhumble_javasysmon_WindowsMonitor_killProcess (JNIEnv *env, jobject object, jint pid) {
  HANDLE process;

  process = OpenProcess(PROCESS_TERMINATE, FALSE, pid);
  if (process != NULL) {
    TerminateProcess(process, 1);
    CloseHandle(process);
  }
}
