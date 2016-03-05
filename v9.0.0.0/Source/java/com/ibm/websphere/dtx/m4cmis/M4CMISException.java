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

import com.ibm.websphere.dtx.dtxpi.MException;

class M4CMISException extends MException
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an MException with the given error code using 
     * the default lookup table for the error message text. The 
     * default lookup table contains text entries for the error 
     * codes defined in MConstants.
     * 
     * @param rc Error code
     */
    public M4CMISException(int rc)
    {
        super(rc, MAdapterImpl.s_errLookup);
    }

    /**
     * Constructs a MException with the given error code using 
     * the default lookup table for the error message text and 
     * extra message text. The default lookup table contains 
     * text entries for the error codes defined in MConstants.
     * 
     * @param rc Error code
     * @param s Extra message text
     */
    public M4CMISException(int rc, String s)
    {
        super(rc, MAdapterImpl.s_errLookup, s);
    }
}

