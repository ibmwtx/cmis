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

package com.ibm.websphere.dtx.m4cmis;

import org.apache.chemistry.opencmis.client.api.Session;


import com.ibm.websphere.dtx.dtxpi.MAdapter;
import com.ibm.websphere.dtx.dtxpi.MBase;
import com.ibm.websphere.dtx.dtxpi.MConnection;

public class MConnectionImpl extends MConnection
{
	String _userID = null;
	String _passWord = null;
	String _url = null;
	String _repositoryName = null;
	
		
    public MConnectionImpl(long lReserved) throws Exception
	{
        super(lReserved);
    }      
    
    public void connect(MAdapter adapter) throws Exception
    {
    	try
    	{
    		MAdapterImpl adapterImpl = (MAdapterImpl)adapter; 
    		
    		_userID = adapterImpl.getUser();
    		_url = adapterImpl.getURL();
    		_passWord = adapterImpl.getPassword();
    		_repositoryName = adapterImpl.getRepo();
    		
    		Session session = OpenCMISSessionFactory.getCMISSessionFactory().getSession(_url, _userID, _passWord, _repositoryName);
    		
    		if (session == null)
    		{
    			throw new Exception("Unable to create session");
    		}
    	}
    	
    	catch (Exception e)
    	{
    		throw e;
    	}    	    
    }
     

    public void disconnect() throws Exception
    {
    	OpenCMISSessionFactory.getCMISSessionFactory().closeSession(_url);
    }

    
    public void onNotify(int iID, int iParam, MBase Param) throws Exception
    {
    }                    
}
