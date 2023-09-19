/*
 * ServConn.h - part of the jEdit Launcher package
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
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: ServConn.h,v 1.1 2001/07/30 16:00:40 jgellene Exp $
 */

#if !defined(__SERVCONN_H__)
#define __SERVCONN_H__

class ServerConnection
{
public:
	ServerConnection();
	~ServerConnection();

	/* Attributes */
public:
	unsigned short GetPort() const;
	unsigned long GetKey() const;
	bool IsConnected() const;

	/* Operations */
public:
	HRESULT FindServer();
	HRESULT Connect();
	HRESULT Disconnect();
	HRESULT Clear();
	HRESULT SendData(char* pData, long nLength);

	/* Data */
private:
	unsigned short port;
	unsigned long key;
	SOCKET hSocket;
	bool connected;

private:
	static void MakeErrorInfo(CHAR* szErrorMsg);

	/* No copying */
private:
	ServerConnection(const ServerConnection&);
	ServerConnection& operator=(const ServerConnection&);
};


#endif        //  #if !defined(__SERVCONN_H__)
