/* 
* IBM Confidential 
* OCO Source Materials 
* (C) Copyright IBM Corporation 2011 
* The source code for this program is not published or otherwise 
* divested of its trade secrets, irrespective of what has been 
* deposited with the U.S. Copyright Office. 
*/ 

package com.ibm.websphere.dtx.m4cmis;

import com.ibm.websphere.dtx.dtxpi.MAdapter;
import com.ibm.websphere.dtx.dtxpi.MBase;
import com.ibm.websphere.dtx.dtxpi.MCard;
import com.ibm.websphere.dtx.dtxpi.MMap;
import com.ibm.websphere.dtx.dtxpi.MConnection;
import com.ibm.websphere.dtx.dtxpi.MConstants;
import com.ibm.websphere.dtx.dtxpi.MException;
import com.ibm.websphere.dtx.dtxpi.MStream;
import com.ibm.websphere.dtx.dtxpi.MTrace;
import com.ibm.websphere.dtx.dtxpi.MUnexpectedException;
import com.ibm.websphere.dtx.dtxpi.MXDSAdapter;
import java.io.File;
import java.io.IOException;

import java.util.HashMap;



public class MAdapterImpl extends MXDSAdapter
{
    private int m_mapInstance = 0;
    private MTrace  m_trace;               		// Trace
    protected static HashMap s_errLookup;       // Error message lookup      
    private int m_iCommonTraceSwitch = 0;       // Common trace value
    private M4CMISCommandLineParser m_cmdLine;    // Adapter command line parse object.
		
	protected boolean isTraceError = false;
	protected boolean _traceUsedInConnection = false;
	
	private String iKey = null;
	private boolean iNewVersion = false;
    private String iName = null;
    private String iUser = null;
    private String iPassword = null;
    private String iRepo = null;
    private String iURL = null;
    private int iCommand = -1;
    private String iDocPath = null;
    private boolean iNow = false;
    private byte[] iContent = null;
    private String iComments  = null;
	private String iSQLQuery;
	
    // Construction
    protected MAdapterImpl ( MCard card,
                             long lReserved ) throws Exception
    {
        super( lReserved );

        newPropertySet( MConstants.MPI_PROPBASE_USER,
                        M4CMISConstants.M4STERLING_EDH_ADAPTER_PROPS.length,
                        M4CMISConstants.M4STERLING_EDH_ADAPTER_PROPS );
        setIntegerProperty( MConstants.MPIP_ADAPTER_LISTEN_USESAMECONN, 0, 1 );
        setDefaultProperties();
    }

    // Dummy Constructor used for testing
    public MAdapterImpl() throws Exception
    {
        super( 0 );
    }
	
	static
    {
        try
        {
            // initialize error lookup table
            s_errLookup = new HashMap();           
        }
        catch( Exception e )
        {
        }
    }
	
	private void setDefaultProperties()
	{
	}
	
	protected MTrace getTrace()
    {
        return m_trace;
    }

    protected int getMapInstance() throws MException
    {
        if( m_mapInstance == 0 )
        {
            m_mapInstance = getCard().getMap().getIntegerProperty( MConstants.MPIP_MAP_INSTANCE, 0 );
        }
        return m_mapInstance;
    }
	
	public int compareWatches( MAdapter adapter ) throws Exception
    {
        return MConstants.MPIRC_E_ILLEGAL_CALL;
    }
	
	public int compareResources( MAdapter adapter ) throws Exception
    {
        return MConstants.MPI_CMP_DIFFER;
    }
	
	public void endTransaction( MConnection connection, 
                                int iTransAction ) throws Exception
    {
        M4CMISLogger.traceEntry( getTrace(), "endTransaction" );
		
		if( iTransAction == MConstants.MPI_COMMIT )
		{
			M4CMISLogger.tracePrintLn(getTrace(), "COMMIT");
			doPut();			
		}
		else
		{
		    M4CMISLogger.tracePrintLn(getTrace(), "ROLLBACK");			
		}

        M4CMISLogger.traceExit( getTrace(), "endTransaction", 0 );
    }

    public void beginTransaction( MConnection connection ) throws Exception
    {
        M4CMISLogger.traceEntry( getTrace(), "beginTransaction" );

        int iOnFailure = getIntegerProperty( MConstants.MPIP_ADAPTER_ON_FAILURE, 0 );

        if( iOnFailure == MConstants.MPI_ACTION_ROLLBACK )
        {
            M4CMISLogger.tracePrintLn( getTrace(), "Starting transaction" );
        }
        else
        {
            M4CMISLogger.tracePrintLn( getTrace(), "Transactions disabled" );
        }
        M4CMISLogger.traceExit( getTrace(), "beginTransaction", 0 );
    }

    public int compareConnection( MConnection connection ) throws Exception
    {
        return MConstants.MPI_CMP_DIFFER;
    }
	
	public int listen( MConnection connection ) throws Exception
    {
        // Listener not supported for this adapter
        M4CMISLogger.traceEntry( getTrace(), "listen" );
        M4CMISLogger.tracePrintLn( getTrace(), "Listening for events not supported" );
        M4CMISLogger.traceExit( getTrace(), "validateConnection", MConstants.MPIRC_E_ILLEGAL_CALL );
        return MConstants.MPIRC_E_ILLEGAL_CALL;
    }
	
	public int validateConnection( MConnection connection ) throws Exception
    {
        return MConstants.MPIRC_SUCCESS;
    }
	
	public void validateProperties() throws Exception
    {
        try
        {
            // Get the command-line string
            String cmdLineString = getTextProperty(MConstants.MPIP_ADAPTER_COMMANDLINE, 0);
            String traceFileName = null;

            // Get common trace values
            MCard card = getCard();
            MMap map = card.getMap();
            m_iCommonTraceSwitch = map.getIntegerProperty(MConstants.MPIP_MAP_COMMON_TRACE_SWITCH, 0);

            if( m_iCommonTraceSwitch == MConstants.MPI_TRACE_SWITCH_ON )
            {
                traceFileName = map.getTextProperty(MConstants.MPIP_MAP_COMMON_TRACE_FILE, 0);
            }
            // Init command line parser
            m_cmdLine = new M4CMISCommandLineParser("m4cmis.mtr");

            // Parse the command line
            try
            {
                m_cmdLine.parseCommandLine(cmdLineString, m_iCommonTraceSwitch, traceFileName);
                

            	iKey = m_cmdLine.iKey;
                iNewVersion = m_cmdLine.iNewVersion;
                iName = m_cmdLine.iName;
                iUser = m_cmdLine.iUser;
                iPassword = m_cmdLine.iPassword;
                iRepo = m_cmdLine.iRepo;
                iURL = m_cmdLine.iURL;
                iNow = m_cmdLine.iNow;
                iComments = m_cmdLine.iComments;
                iDocPath = m_cmdLine.iDocPath;
                iSQLQuery = m_cmdLine.iSQLQuery;
                
                if (iKey != null )
				{
                	if (iKey.compareToIgnoreCase("DELETE") == 0)
                		iCommand = M4CMISConstants.ACTION_DELETE;
                	else if (iKey.compareToIgnoreCase("ADD") == 0)
                		iCommand = M4CMISConstants.ACTION_ADD;                	
                	else if (iKey.compareToIgnoreCase("UPDATE") == 0)
                		iCommand = M4CMISConstants.ACTION_UPDATE;    
                	else if (iKey.compareToIgnoreCase("QUERY") == 0)
                		iCommand = M4CMISConstants.ACTION_QUERY;  
                	else	
                		throw new MException(MConstants.MPIRC_E_NULL_ARGUMENT, "Invalid Action command must be specified, The Value must be DELETE, ADD, REPLACE, UPDATE, APPEND");
				}
            }
            catch( Exception e )
            {
            	e.printStackTrace();
            }			 							
			
            traceFileName = m_cmdLine.iTraceFile;

            // Initialize trace
            if( traceFileName != null )
            {
                String expandedPath = expandPathToMapDir(traceFileName);
                if( m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_OFF )
                {
                    m_trace = new MTrace(expandedPath,
                                         m_cmdLine.isTraceAppend ? MConstants.LOG_MODE_APPEND : MConstants.LOG_MODE_DEFAULT,
                                         m_cmdLine.isTraceVerbatim ? MConstants.LOG_LEVEL_MAX : MConstants.LOG_LEVEL_DEFAULT);
                }
            }
            else
                m_trace = null;

            m_cmdLine.dumpCommandOptions(m_trace);
        
        }
        
        catch( MException e )
        {
            reportException( e );
            return;
        }
        catch( Exception e )
        {
        	reportException( e );
        	return;            
        }
    }
	
	 public void SetTraceUsedInConnection()
     {
        _traceUsedInConnection = true;
     }
	
	public void onNotify( int iID, 
                          int iParam, 
                          MBase Param ) throws Exception
    {
		int rc = MConstants.MPIRC_SUCCESS;
        if((m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON) || (iID != MConstants.MPIN_OBJECT_PREPARE_DESTROY))

		{
			if(!isTraceError)
				TRACE(getTrace(), "onNotify");
		}
        
        switch (iID)
        {
        case (MConstants.MPIN_ADAPTER_GETSTART):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for GETSTART");
                break;
            }
        case (MConstants.MPIN_ADAPTER_PUTSTART):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PUTSTART");
                break;
            }
        case (MConstants.MPIN_ADAPTER_GETSTOP):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for GETSTOP");
                break;
            }
        case (MConstants.MPIN_ADAPTER_PUTSTOP):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PUTSTOP");
                break;
            }
        case (MConstants.MPIN_OBJECT_PREPARE_DESTROY):
            {
				if(!isTraceError && (m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON))
					TRACE(getTrace(), "OnNotify called for PREPARE_DESTROY");
                break;
            }
        case (MConstants.MPIN_ADAPTER_MAPABORT):
            {
				if(!isTraceError)
					TRACE(getTrace(), "OnNotify called for PREPARE_MAPABORT");
                break;
            }
        }
        if (iID == MConstants.MPIN_ADAPTER_GETSTOP ||
            iID == MConstants.MPIN_ADAPTER_PUTSTOP)
        {
            // Close the trace
            if (getTrace() != null)
            {
                if (!_traceUsedInConnection)
                {
                    // This trace will not be used any more and
                    // can be closed
                    if(!isTraceError)
                    {
                    	TRACE(getTrace(), "Closing trace file and exiting function");                    	
					}
                    getTrace().close();
					getTrace().finalize();					
                }
                else
                {
                    // The trace will be closed on disconnect
                    if(!isTraceError)
                    	traceExit(getTrace(), "onNotify", rc);
                }
            }
        }
        else
        {
			if((m_iCommonTraceSwitch != MConstants.MPI_TRACE_SWITCH_ON) || (iID != MConstants.MPIN_OBJECT_PREPARE_DESTROY))
			{
				if(!isTraceError)
					traceExit(getTrace(), "onNotify", rc);
			}

        }
	}
	

    static void traceExit(MTrace trace, String functionName, int returnCode)
    {
        try
        {
            if (trace != null)
            {
                trace.exit(functionName, returnCode);
            }
        }
        catch (Throwable e)
        {
            // Nothing can be done
        }
    }

	
	
	public int get( MConnection c ) throws Exception
    {
        M4CMISLogger.traceEntry( getTrace(), "get" );
      
        int rc = MConstants.MPIRC_SUCCESS;

        try
        {        
	    	int adapterContext = getIntegerProperty(MConstants.MPIP_ADAPTER_CONTEXT, 0);
	        
	    	if (adapterContext == MConstants.MPI_CONTEXT_SOURCE_EVENT)
	        {
	        	 M4CMISLogger.tracePrintLn(getTrace(), "CMIS adapter cannot be used as source event on a input card");	        	 
	             rc = MConstants.MPIRC_E_ILLEGAL_CALL;
	        }
	    	else
	    	{            
		    	if (iCommand == M4CMISConstants.ACTION_DELETE)  
				{
					OpenCMISSessionFactory.getCMISSessionFactory().deleteDoc(iURL, iDocPath);
				}
		    	else if (iCommand == M4CMISConstants.ACTION_QUERY)  
				{
		    		OpenCMISSessionFactory.getCMISSessionFactory().getDocByCondition(iURL, iDocPath, iSQLQuery);
				}	    	
		    	else
		    	{
			    	CMISRMIDoc document = OpenCMISSessionFactory.getCMISSessionFactory().getDoc(iURL, iDocPath);	    	
		             
			    	if (document != null)
			    	{
			    		byte[] outdata = document.getContent();												    			
					
						if( outdata != null && outdata.length > 0 )
						{
							MStream out = getOutputStream();
							out.setSize(0);
							out.write(outdata);
							out.flush();
						}		
						else
						{
							MStream out = getOutputStream();
							out.setSize(0);								
						}
		
						setIntegerProperty( MConstants.MPIP_ADAPTER_NUM_MESSAGES, 0, 1 );
			    	    	
			    	}
			    	else
			    		throw new M4CMISException(MConstants.MPIRC_E_FAILED, "ERROR: Failed to read document from the Repository");
		    	}
	    	}
        }
        
        
        catch( Exception e )
        {
            reportException(c, new M4CMISException(MConstants.MPIRC_E_EXCEPTION, e.getMessage()));
            return MConstants.MPIRC_E_EXCEPTION;
        }

        M4CMISLogger.traceExit( getTrace(), "get", rc );

        return rc;
    }
	
	public void put( MConnection conn ) throws Exception
    {
        M4CMISLogger.traceEntry( getTrace(), "put" );
		
		int rc = MConstants.MPIRC_SUCCESS;
				
		
		try
		{
		    // Open the streams
            MStream in = getInputStream();

            // Read the input stream
            long sizeInput = in.getSize();
            iContent = new byte[(int)sizeInput];
            
            in.read(iContent);    
            
            if (iNow)
            {
            	doPut();
            }
		}
			
        catch( MException e )
        {
            reportException(conn, e);
            return;
        }
        
        catch( Exception e )
        {
            reportException(conn, e);
            return;
        }

        M4CMISLogger.traceExit( getTrace(), "put", rc );
	}

	
	private void doPut() throws Exception
	{	
		try	
		{					
				
        		if (iCommand == M4CMISConstants.ACTION_ADD)
        		{
        			OpenCMISSessionFactory.getCMISSessionFactory().createDoc(iURL, 
        																	iDocPath,         							        																	
        																	iName,
        																	iContent);
        		}        	
        		else if (iCommand == M4CMISConstants.ACTION_UPDATE)
        		{
        			OpenCMISSessionFactory.getCMISSessionFactory().updateDoc(iURL, 
																			 iDocPath,																				
																			 iContent,
																			 iNewVersion,
																			 iComments);
        		}        		
		}
		
		catch (Exception e) {
			reportException(e);
			return;
		}
	}
	
	// All Exception Handling from here on
	private void reportException( MConnection c, 
                                  MException e ) throws MException
    {
        M4CMISLogger.traceException( getTrace(), e );
        M4CMISLogger.tracePrintLn( m_trace, "Error code: " + e.getRC() + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        if( c != null )
        {
            c.setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
            c.setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        }
    }

	
    private void reportException( MConnection c, 
                                  Exception e ) throws MException
    {
        M4CMISLogger.traceException( getTrace(), e );
        M4CMISLogger.tracePrintLn( m_trace, "Error code: " + MConstants.MPIRC_E_EXCEPTION + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, MConstants.MPIRC_E_EXCEPTION );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        if( c != null )
        {
            c.setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, MConstants.MPIRC_E_EXCEPTION );
            c.setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
        }
    }

    private void reportException( MException e ) throws MException
    {
        M4CMISLogger.traceException( getTrace(), e );
        M4CMISLogger.tracePrintLn( m_trace, "Error code: " + e.getRC() + " , Error message: " + e.getMessage() );
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC() );
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
    }
    
    private void reportException( Exception e ) throws MException
    {
        M4CMISLogger.traceException( getTrace(), e );       
        setIntegerProperty( MConstants.MPIP_OBJECT_ERROR_CODE, 0,  MConstants.MPIRC_E_EXCEPTION);
        setTextProperty   ( MConstants.MPIP_OBJECT_ERROR_MSG,  0, e.getMessage() );
    }

    protected String expandPathToMapDir(String path) throws MException, IOException
    {
        File pathObj = new File(path);

        // Check if the given path is already absolute
        if( pathObj.isAbsolute() )
        {
            return pathObj.getCanonicalPath();
        }
        else
        {
            // The given path is relative, get the map path
            String mapPath = getMapDirectory();

            // Use the map path as the current working directory
            return new File(mapPath, path).getCanonicalPath();
        }
    }

    protected String getMapDirectory() throws MException
    {
        MCard card = getCard();
        MMap map = card.getMap();
        String mapFilePath = map.getTextProperty(MConstants.MPIP_MAP_MAP_NAME, 0);
        File mapFilePathObj = new File(mapFilePath);
        return mapFilePathObj.getParent();
    }

    static void TRACE(MTrace trace, String text)
    {
        try
        {
            if( trace != null )
                trace.println(text);
        }
        catch( MException e )
        {
            // Ignore this exception, there is no corrective action that can be taken.
            // The trace information will be lost, but the program execution will not
            // be affected.
        }
    }

    protected MException handleAndConvertException(Exception e)
    {
        printStackTrace(m_trace, e);
        MException e1 = convertException(e);
        TRACE(m_trace, "STATUS: Failure! RC: " + e1.getRC() + ", Message: " + e1.getMessage());
        setErrorProperties(e1);
        try
        {
            if( m_trace != null )
            {
                m_trace.close();
                m_trace = null;
            }
        }
        catch( Exception ex )
        {

        }
        return e1;
    }

    static MException convertException(Exception e)
    {
        if( e instanceof MException )
        {
            // No conversion, just return it
            return(MException)e;
        }
        else
        {
            // Unexpected
            return new MUnexpectedException(e);
        }
    }

    static void printStackTrace(MTrace trace, Throwable t)
    {
        if( trace != null && t != null )
        {
            StackTraceElement[] stackFrames = t.getStackTrace();

            TRACE(trace, "EXCEPTION: " + t.toString());

            for( int i = 0; i < stackFrames.length; i++ )
            {
                String frameText = stackFrames[i].toString();
                TRACE(trace, "  at " + frameText);
            }
        }
    }

    protected void setErrorProperties(MException e)
    {
        try
        {
            setTextProperty(MConstants.MPIP_OBJECT_ERROR_MSG, 0, e.getMessage());
            setIntegerProperty(MConstants.MPIP_OBJECT_ERROR_CODE, 0, e.getRC());
        }
        catch( MException e1 )
        {
            // Ignore this exception. These functions set the error code and message
            // reported to the Resource Manger. If they fail there is nothing that
            // can be done.
        }
    }
    
    public String getUser() {
		return iUser;
	}

	public String getPassword() {
		return iPassword;
	}

	public String getRepo() {
		return iRepo;
	}

	public String getURL() {
		return iURL;
	}
}