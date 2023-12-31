/*
 * jedinstl.cpp - part of jEditLauncher package
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
 * $Id: jedinstl.cpp,v 1.3 2001/08/04 17:19:11 jgellene Exp $
 */

 // jedinstl.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "JELInstaller.h"
#include "StringPtr.h"
#include "jedinstl.h"

// dummy map
BEGIN_OBJECT_MAP(ObjectMap)
END_OBJECT_MAP()

CComModule _Module;

extern "C"
BOOL WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	if(dwReason == DLL_PROCESS_ATTACH)
		_Module.Init(NULL, hInstance);
    else if (dwReason == DLL_PROCESS_DETACH)
        _Module.Term();

    return TRUE;
}

void GetModuleDirectory(char* szPath)
{
	GetModuleFileName(_Module.GetModuleInstance(), szPath, MAX_PATH);
	char *pSlash = strrchr(szPath, '\\');
	if(pSlash == 0)
		szPath[2] = 0;
	else *pSlash = 0;
}


extern "C"
HRESULT Install(const char* szJavaHome)
{
	char szInstallPath[MAX_PATH];
	GetModuleDirectory(szInstallPath);
	JELApplicationInstaller installer(szJavaHome, szInstallPath);
	HRESULT hr = installer.Install();
	return hr;
}


extern "C"
HRESULT Uninstall()
{
	char szUninstallPath[MAX_PATH];
	GetModuleDirectory(szUninstallPath);
	// third parameter disables file deletion
	JELApplicationInstaller uninstaller(0, szUninstallPath, FALSE);
	HRESULT hr = uninstaller.Uninstall();
	return hr;
}

extern "C"
HRESULT Unregister()
{
	char szUninstallPath[MAX_PATH];
	GetModuleDirectory(szUninstallPath);
	// third parameter enables file deletion
	JELApplicationInstaller uninstaller(0, szUninstallPath, TRUE);
	HRESULT hr = uninstaller.Uninstall();
	return hr;
}
