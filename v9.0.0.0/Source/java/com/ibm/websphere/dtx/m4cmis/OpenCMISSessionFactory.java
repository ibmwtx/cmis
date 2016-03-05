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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

public class OpenCMISSessionFactory {

	private static OpenCMISSessionFactory iCMISFactory = null;
	private SessionFactory iSessionFactory = null;
	private Map<String,Session> sessionMap = new HashMap<String, Session>();
	
	private OpenCMISSessionFactory()
	{
		iSessionFactory = SessionFactoryImpl.newInstance();
	}
	
	public static synchronized OpenCMISSessionFactory getCMISSessionFactory() {
		
		if (iCMISFactory == null)
		{
			iCMISFactory = new OpenCMISSessionFactory();
		}
		
		return iCMISFactory;
	}
	
	public static synchronized void destroySessionFactory() {
		
		if (iCMISFactory != null)
		{
			iCMISFactory.closeSessions();
			iCMISFactory = null;
		}
	}
	
	
	private void closeSessions() {
		// TODO Auto-generated method stub
		
	}
	
	public void closeSession(String url) {
		Session session  = sessionMap.get(url);
		
		if (session != null)
		{
			session.getBinding().close();
			sessionMap.remove(url);
		}
	}

	/**
	 * Creates a new OpenCMIS session with the provided username and
	 * password.
	 */
	public Session createOpenCMISSession(String username, String password, String url) {
		Map<String, String> parameter = new HashMap<String, String>();

		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);

		parameter.put(SessionParameter.ATOMPUB_URL,	url);
		parameter.put(SessionParameter.BINDING_TYPE,
				BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "A1");		

		return iSessionFactory.createSession(parameter);
	}

	public List<String> getRepsitoryNames(String url, String uid, String pwd) throws Exception {	    	
		Map<String, String> parameter = new HashMap<String, String>();		
		parameter.put(SessionParameter.USER, uid);
		parameter.put(SessionParameter.PASSWORD, pwd);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
				
		// result
		List<Repository> reps = iSessionFactory.getRepositories(parameter);
			
		List<String> result = new Vector<String>(reps.size());
		
		for(Repository rep:reps)
			result.add(rep.getName());
		
		return result;
    }
	
	public List<Repository> getRepsitories(String url, String uid, String pwd) throws Exception {	    	
		Map<String, String> parameter = new HashMap<String, String>();		
		parameter.put(SessionParameter.USER, uid);
		parameter.put(SessionParameter.PASSWORD, pwd);

		// connection settings
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
				
		// result
		List<Repository> reps = iSessionFactory.getRepositories(parameter);					
		
		return reps;
    }
	
	public synchronized Session getSession(String url, String uid, String pwd, String repositoryName) 
	{
		Session session = sessionMap.get(url);
    	
    	if (session == null)
    	{    	    	
			Map<String, String> parameter = new HashMap<String, String>();
	
			// user credentials
			parameter.put(SessionParameter.USER, uid);
			parameter.put(SessionParameter.PASSWORD, pwd);
	
			// connection settings
			parameter.put(SessionParameter.ATOMPUB_URL, url);
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.REPOSITORY_ID, repositoryName);
	
			
			// create session
			session = iSessionFactory.createSession(parameter);
			sessionMap.put(url, session);    
    	}
    	
    	return session;
	}
	
	
	 public CMISRMIDoc getDoc(String sessionId, String docPath) throws Exception
	 {
	    	Session session = sessionMap.get(sessionId);
	    	if (session==null) throw new IOException("session null or not valid!");
	    	
	    	CmisObject cmisObject = null;
	    	
			try 
			{
				cmisObject = session.getObjectByPath(docPath);
				
			}
			
			catch (CmisBaseException cbe) 
			{
				throw new Exception("Could not retrieve the document!", cbe);
			}
			
			Document doc = null;
			
			if (cmisObject instanceof Document) 
			{
				doc = (Document) cmisObject;
			}
			else
			{
				throw new Exception("Object is not a document!");
			}
				
			CMISRMIDoc retDoc = new CMISRMIDoc();
			

			retDoc.setDocName(doc.getName());
			retDoc.setMimeType(doc.getContentStreamMimeType());
			InputStream is = doc.getContentStream().getStream();
			byte[] res = new byte[(int)doc.getContentStreamLength()];
			is.read(res);
			retDoc.setContent(res);
			
			return retDoc;
		}
	    
	    public String createDoc(String sessionId,
	    						String docPath, 	    						
	    						String name,
	    						byte[] content) throws Exception{
	    	
	    	Session session = sessionMap.get(sessionId);
	    	String mimeType = "application/octet-stream";
	    	
	    	if (session == null)
	    		throw new Exception("session null or not valid!");
	    	
	    	CmisObject cmisObject = null;
	    	
			try 
			{
				cmisObject = session.getObjectByPath(docPath);
				
			}
			
			catch (CmisBaseException cbe) 
			{
				throw new Exception("Could not retrieve the document!", cbe);
			}
			
			Folder parent = null;
			
			if (cmisObject instanceof Folder) 
			{
				parent = (Folder) cmisObject;
			}
			else
			{
				throw new Exception("Object is not a document!");
			}
										    
	    	
	    	if (parent == null)
	    	{    		
	    		parent = createFolder(session, docPath);
	    	}
	    						
			// properties 
			// (minimal set: name and object type id)
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
			properties.put(PropertyIds.NAME, name);					

			InputStream is = new ByteArrayInputStream(content);
			ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(content.length), mimeType, is);

			// create a major version
			Document doc = parent.createDocument(properties, contentStream, VersioningState.NONE);
			
			return doc.getId();
		}	    	    	  	    
	    
	    private Folder createFolder(Session session, String folderName)
	    {
			// create folder
			Folder root = session.getRootFolder();

			// properties
			// (minimal set: name and object type id)
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
			properties.put(PropertyIds.PATH, folderName);

			return root.createFolder(properties);
		}
	    
	    
	    public List<String> getDocByCondition(	String sessionId, String docPath, String whereCond)
	    		throws InterruptedException , Exception
		{
			// get the query name of cmis:objectId
			Session session = sessionMap.get(sessionId);
			
			if (session==null) throw new Exception("session null or not valid!");
			
			ObjectType type = session.getTypeDefinition("cmis:document");
			PropertyDefinition<?> objectIdPropDef = type.getPropertyDefinitions().get(PropertyIds.OBJECT_ID);
			String objectIdQueryName = objectIdPropDef.getQueryName();
		
			String queryString = "SELECT * FROM cmis:document";
	
			// check where
			if (whereCond != null && whereCond.length() > 0)
			{
				whereCond = whereCond.trim();
				
				if(!whereCond.startsWith("WHERE"))
				{
					whereCond = " WHERE "+whereCond;
				}
				else
					whereCond = " "+ whereCond;
				
				queryString += whereCond;
			}
			else
			{
				queryString = "SELECT * FROM cmis:document where in_tree ('" + docPath +  "')";
			}
				
															
				
			
			// execute query
			ItemIterable<QueryResult> results = session.query(queryString, false);
		
			// result
			List<String> result = new Vector<String>();
			for (QueryResult qResult : results) {
			   String objectId = qResult.getPropertyValueByQueryName(objectIdQueryName);
			   Document doc = (Document) session.getObject(session.createObjectId(objectId));
			   
			   
			   result.add(doc.getId());
			}
			return result;
	    }
	    
	    
	   

	    public void deleteDoc(	String sessionId,									 	    					
								String docPath) throws Exception
		{

	    	Session session = sessionMap.get(sessionId);			
			
			if (session == null)
			{
				throw new Exception("session null or not valid!");
			}
			
	    	CmisObject cmisObject = null;
	    	
	    	try 
			{
				cmisObject = session.getObjectByPath(docPath);
				
			}
			
			catch (CmisBaseException cbe) 
			{
				throw new Exception("Could not retrieve the document!", cbe);
			}
			
			Document doc = null;
			
			if (cmisObject instanceof Document) 
			{
				doc = (Document) cmisObject;
			}
			else
			{
				throw new Exception("Object is not a document!");
			}																				
			
			doc.delete(true);			
		
		}
	    	   
	    public String updateDoc(	String sessionId,
									String docPath, 	    															
									byte[] content,
									boolean newVersion,
									String comments) throws Exception
		{
	    	String mimeType = "application/octet-stream";
			
	    	Session session = sessionMap.get(sessionId);			
			
			if (session == null)
			{
				throw new Exception("session null or not valid!");
			}
			
	    	CmisObject cmisObject = null;
	    	
	    	try 
			{
				cmisObject = session.getObjectByPath(docPath);
				
			}
			
			catch (CmisBaseException cbe) 
			{
				throw new Exception("Could not retrieve the document!", cbe);
			}
			
			Document doc = null;
			
			if (cmisObject instanceof Document) 
			{
				doc = (Document) cmisObject;
			}
			else
			{
				throw new Exception("Object is not a document!");
			}	
																										
			
			if (!newVersion)
			{				
				InputStream is = new ByteArrayInputStream(content);
				ContentStream contentStream = new ContentStreamImpl(doc.getName(), BigInteger.valueOf(content.length), mimeType, is);											
				doc.setContentStream(contentStream, true);											
				return doc.getId();
			}
			else
			{
				ObjectId pwcId = doc.checkOut();
				Document pwc = (Document) session.getObject(pwcId);	
				InputStream is = new ByteArrayInputStream(content);
				
				ContentStream contentStream = new ContentStreamImpl(doc.getName(), BigInteger.valueOf(content.length), mimeType, is);					
				ObjectId newVersionId = 	pwc.checkIn(true, null, contentStream, comments);
				
				Document newDoc = (Document) session.getObject(newVersionId);
				return newDoc.getId();
			}
		}
}
