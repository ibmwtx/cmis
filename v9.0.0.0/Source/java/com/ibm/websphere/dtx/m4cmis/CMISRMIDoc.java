package com.ibm.websphere.dtx.m4cmis;

/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and other Contributors
*
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM - initial implementation
*******************************************************************************/

import java.io.Serializable;



public class CMISRMIDoc implements Serializable {
	
	private static final long serialVersionUID = 1L;
		
	String mimeType;
	String docName;
	byte[] content;
	
	public String getMimeType() {
		return mimeType;
	}
	
	/**
	 * @param mimeType the mimeType to set
	 */
	
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	/**
	 * @return the docName
	 */
	public String getDocName() {
		return docName;
	}
	
	/**
	 * @param docName the docName to set
	 */
	public void setDocName(String docName) {
		this.docName = docName;
	}
	
	/**
	 * @return the content
	 */
	
	public byte[] getContent() {
		return content;
	}
	/**
	 * @param content the content to set
	 */
	
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	
	@Override
	public String toString(){
		if (content!=null)
			return "name:"+docName+",type:"+mimeType+",len:"+content.length;
		return null;
	}
}
