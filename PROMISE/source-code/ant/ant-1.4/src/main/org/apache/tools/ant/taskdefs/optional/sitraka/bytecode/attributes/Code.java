/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.tools.ant.taskdefs.optional.sitraka.bytecode.attributes;

import java.io.*;

import org.apache.tools.ant.taskdefs.optional.depend.constantpool.ConstantPool;
import org.apache.tools.ant.taskdefs.optional.depend.constantpool.Utf8CPInfo;

/**
 * Code structure.
 *
 * @author <a href="sbailliez@imediation.com">Stephane Bailliez</a>
 */
public class Code extends AttributeInfo {
	
	protected int length;
	
	protected int max_stack;
	
	protected int max_locals;
	
	protected byte[] code;
	
	protected ExceptionInfo[] exceptions;
		
	protected LineNumberTable lineNumberTable;
	
	public Code(int attr_index, ConstantPool pool){
		super(attr_index, pool);
	}
	
	public void read(DataInputStream dis) throws IOException {
		length = dis.readInt();
		max_stack = dis.readShort();
		max_locals = dis.readShort();
		
		// read bytecode...
		int bytecode_len = dis.readInt();
		//code = new byte[bytecode_len];
		//dis.readFully(code);
		dis.skip(bytecode_len);
		
		// read exceptions...
		int exception_count = dis.readShort();
		exceptions = new ExceptionInfo[exception_count];
		for (int i = 0; i < exception_count; i++){
			exceptions[i] = new ExceptionInfo(constantPool);
			exceptions[i].read(dis);
		}
		
		// read attributes...
		AttributeInfoList attributes = new AttributeInfoList(constantPool);
		attributes.read(dis);
		lineNumberTable = (LineNumberTable)attributes.getAttribute(AttributeInfo.LINE_NUMBER_TABLE);
	}
	
	public int getMaxStack(){
		return max_stack;
	}
	
	public int getMaxLocals(){
		return max_locals;
	}
	
	public byte[] getCode(){
		return code;
	}
	
	public ExceptionInfo[] getExceptions(){
		return exceptions;
	}
	
	public LineNumberTable getLineNumberTable(){
		return lineNumberTable;
	}
	
	public static class ExceptionInfo {
		protected ConstantPool constantPool;
		protected int startPC;
		protected int endPC;
		protected int handlerPC;
		protected int catchType;
		public ExceptionInfo(ConstantPool pool){
			constantPool = pool;
		}
		public void read(DataInputStream dis) throws IOException {
			startPC = dis.readShort();
			endPC = dis.readShort();
			handlerPC = dis.readShort();
			catchType = dis.readShort();
		}
		public int getStartPC(){
			return startPC;
		}
		public int getEndPC(){
			return endPC;
		}
		public int getHandlerPC(){
			return handlerPC;
		}
		public int getCatchType(){
			return catchType;
		}
	}
	
}


