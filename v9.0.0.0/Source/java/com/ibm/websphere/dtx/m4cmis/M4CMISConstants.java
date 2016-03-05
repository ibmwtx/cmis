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

import com.ibm.websphere.dtx.dtxpi.MConstants;

public class M4CMISConstants 
{
    protected static final int MPIP_ADAPTER_TRACE = MConstants.MPI_PROPBASE_USER + 0;  
	
    protected static final int M4STERLING_EDH_ADAPTER_PROPS[] = 
    {
        MConstants.MPI_PROP_TYPE_TEXT //-T
    };

    private M4CMISConstants()
    {
    }         
    
    public static final int ACTION_ADD					= 0;
	public static final int	ACTION_DELETE				= 1;
	public static final int	ACTION_UPDATE				= 2;	
	public static final int	ACTION_QUERY				= 3;
}
