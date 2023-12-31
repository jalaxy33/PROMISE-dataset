/*
 * DeferFileOps.cpp - part of jEditLauncher package
 * Copyright (C) 2001 John Gellene
 * jgellene@nyc.rr.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * Notwithstanding the terms of the General Public License, the author grants
 * permission to compile and link object code generated by the compilation of
 * this program with object code and libraries that are not subject to the
 * GNU General Public License, provided that the executable output of such
 * compilation shall be distributed with source code on substantially the
 * same basis as the jEditLauncher package of which this program is a part.
 * By way of example, a distribution would satisfy this condition if it
 * included a working makefile for any freely available make utility that
 * runs on the Windows family of operating systems. This condition does not
 * require a licensee of this software to distribute any proprietary software
 * (including header files and libraries) that is licensed under terms
 * prohibiting redistribution to third parties.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: DeferFileOps.cpp,v 1.3 2001/08/04 17:19:11 jgellene Exp $
 */


#include "stdafx.h"
#include "DeferFileOps.h"

//Implementation of DeferFileOpsImpl

DeferFileOpsImpl::DeferFileOpsImpl()
	: lpBuffer(0) {}

DeferFileOpsImpl::~DeferFileOpsImpl()
{
	delete[] lpBuffer;
}

LPVOID DeferFileOpsImpl::InitBuffer(size_t t)
{
	delete[] lpBuffer;
	size = 8192 * t;
	lpBuffer = new char[size];
	ZeroMemory(lpBuffer, size);
	return lpBuffer;
}

size_t DeferFileOpsImpl::Size()
{
	return size;
}

LPVOID DeferFileOpsImpl::GetBuffer()
{
	return lpBuffer;
}


// Implementation of DeferFileOps95

DeferFileOps95::DeferFileOps95()
	: DeferFileOpsImpl() {}

DeferFileOps95::~DeferFileOps95() {}

HRESULT DeferFileOps95::Read()
{
	TCHAR *pBuf =  (TCHAR*)InitBuffer(sizeof(TCHAR));
	if(pBuf == 0)
		return E_FAIL;
	DWORD dwSize = GetPrivateProfileSection(_T("rename"),
		pBuf, Size(), _T("wininit.ini"));
	if(*(pBuf + dwSize -1) != _T('\n'))
	*(pBuf + dwSize) = _T('\n');
	return S_OK;
}

HRESULT DeferFileOps95::Add(const char* szDest, const char* szSource)
{
	if(szSource == 0 || strlen(szSource) == 0)
		return S_FALSE;
	TCHAR *pBuf =  (TCHAR*)GetBuffer();
	if(szDest == 0 || strlen(szDest) == 0)
	{
		lstrcat(pBuf, _T("NUL="));
	}
	else
	{
		TCHAR *pEnd = pBuf + lstrlen(pBuf);
		lstrcat(pBuf, szDest);
		GetShortPathName(pEnd, pEnd, strlen(pEnd) + 1);
		lstrcat(pBuf, _T("="));
	}
	TCHAR *pEnd = pBuf + lstrlen(pBuf);
	lstrcat(pBuf, szSource);
	GetShortPathName(pEnd, pEnd, strlen(pEnd) + 1);
	lstrcat(pBuf, _T("\n"));
	return S_OK;
}

HRESULT DeferFileOps95::Write()
{
	TCHAR *pBuf = (TCHAR*)GetBuffer();
	BOOL bResult = WritePrivateProfileSection(_T("rename"),
		pBuf, _T("wininit.ini"));
	return bResult ? S_OK : E_FAIL;
}


// Implementation of DeferFileOpsNT

DeferFileOpsNT::DeferFileOpsNT()
	: DeferFileOpsImpl(),
	  strSessionManager(_T("SYSTEM\\CurrentControlSet\\Control\\Session Manager")),
	  dwBufLength(0),
	  szPrefixSource(_T("\\??\\")), szPrefixDest(_T("\\??\\")) {}

DeferFileOpsNT::~DeferFileOpsNT() {}

HRESULT DeferFileOpsNT::Read()
{
	return S_OK;
}

typedef BOOL (WINAPI *MoveFileFunc)(LPCTSTR, LPCTSTR, DWORD);

HRESULT DeferFileOpsNT::Add(const TCHAR* szDest, const TCHAR* szSource)
{
	HINSTANCE hModule = ::LoadLibrary("kernel32.dll");
	if(hModule == 0)
		return E_FAIL;
	TCHAR *szFunc = (sizeof(TCHAR) == sizeof(char))
		? _T("MoveFileExA") : _T("MoveFileExW");
	MoveFileFunc procMoveFile =
		(MoveFileFunc)GetProcAddress(hModule, szFunc);
	if(procMoveFile == 0)
	{
		FreeLibrary(hModule);
		return E_FAIL;
	}
	if(szDest != NULL)
		(procMoveFile)(szDest, NULL, MOVEFILE_DELAY_UNTIL_REBOOT);
	BOOL bRet = (procMoveFile)(szSource, szDest,
		MOVEFILE_DELAY_UNTIL_REBOOT | MOVEFILE_REPLACE_EXISTING);
	::FreeLibrary(hModule);
	return bRet ? S_OK : E_FAIL;
}


HRESULT DeferFileOpsNT::Write()
{
	return S_OK;
}


