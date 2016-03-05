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

import com.ibm.websphere.dtx.dtxpi.tools.commandline.*;
import com.ibm.websphere.dtx.dtxpi.*;

/**
 * This class encapsulates common adapter/importer command-line options.
 */
public class M4CMISCommandLineParser
{
    public String  iTraceFile = null;
    public boolean isTraceAppend;
    public boolean isTraceError;
    public boolean isTraceVerbatim;
    public String iKey = null;
    public boolean iNewVersion = false;
    public String iName = "";
    public String iUser = "";
    public String iPassword = "";
    public String iRepo = "";
    public String iURL = "";
    public boolean iNow = false;
    public String iDocPath = "";
   
    protected MCommandLineParser m_parser = null;
    protected String m_defaultTraceFile = null;
	public String iComments = "";
	public String iSQLQuery;  
    
    public M4CMISCommandLineParser(String defaultTraceFile) throws MException
    {
        m_parser = new MCommandLineParser();

        // Set the m_parser up
		/*
		 * @param longForm        Long form command option alias
		 * @param shortForm       Short form command option alias
		 * @param description     Command option description
		 * @param isRequired      Controls if the option can be specified multiple times
		 * @param allowsMultiple  Controls if the option must appear at least once
		 * @param reqArgCount     Minimum number of arguments that must be specified
		 * @param optArgCount     Maximum number of optional arguments that can be specified
		 */
	
        /* Key values : ADD, DELETE , UPDATE */
        m_parser.addCommand("-KEY",		 "-K",      "Key",      	   					 false,  false,  1,  1);
        m_parser.addCommand("-NAME",	 "-N",      "Document Name",   					 false,   false, 1,  1);
        m_parser.addCommand("-USR",	 	 "-USR",    "User Name",   						 false,   false,  1,  1);
        m_parser.addCommand("-PWD",		 "-PWD",    "Password",   						 false,   false,  1,  1);
        m_parser.addCommand("-URL", 	 "-URL",    "ATOM PUB URL",						 true,   false,  1,  1);
        m_parser.addCommand("-REPO", 	 "-R",      "Repository",						 true,   false,  1,  1);
        m_parser.addCommand("-PATH", 	 "-P",      "Document Path",					 true,   false,  1,  1);
        m_parser.addCommand("-WHERE", 	 "-W",      "Where Condition",					 true,   false,  1,  1);
        m_parser.addCommand("-VERSION",	 "-V",      "New Version",   					 false,  false,  0,  0);
        m_parser.addCommand("-NOW",		 "-NOW",    "Force Operation", 					 false,  false,  0,  0);
        m_parser.addCommand("-COMMENTS", "-C",    	"Check In Comments",				 false,  false,  1,  1);
        m_parser.addCommand("-T",        "-T",      "Adapter Trace (overwrite)",         false,  false,  0,  1);
        m_parser.addCommand("-T+",       "-T+",     "Adapter Trace (append)",            false,  false,  0,  1);
        m_parser.addCommand("-TE",       "-TE",     "Adapter Trace Error(overwrite)",    false,  false,  0,  1);
        m_parser.addCommand("-TE+",      "-TE+",    "Adapter Trace Error (append)",      false,  false,  0,  1);
        m_parser.addCommand("-TV",       "-TV",     "Adapter Trace Verbatim(overwrite)", false,  false,  0,  1);
        m_parser.addCommand("-TV+",      "-TV+",    "Adapter Trace Verbatim (append)",   false,  false,  0,  1);

        m_defaultTraceFile = defaultTraceFile;

        // Calls the internal version to make sure the overriden forms are not called at this point.
        // It is the responsibility of the subclass constructor to initialize its own elements.
        // This method is called before any other in the subclass constructor.
        setDefaultValuesInternal();
    }

    private void setDefaultValuesInternal()
    {
        iTraceFile = null;
        isTraceAppend = false;
        isTraceError = false;
		
		
    }

    protected void setDefaultValues()
    {
        setDefaultValuesInternal();
    }

    /**
     * Parses the given command-line string.
     *
     * @param cmdLine Command-line string.
     * @exception MException
     */
    public void parseCommandLine(String cmdLine,int iCommonTraceSwitch, String traceFileName) throws MException
    {
        setDefaultValues();
        m_parser.parseCommandLine(cmdLine);
        validateCommandOptionCombinations();
        populateFields(iCommonTraceSwitch,traceFileName);
    }

    /**
     * Validates trace options. It prevents both -T and -T+ to
     * be specified at the same time.
     *
     * @throws MException
     */
    protected void validateTraceOptions() throws MException
    {
        MCommandOption trace = m_parser.getCommandOption("-T");
        MCommandOption traceAppend = m_parser.getCommandOption("-T+");
        if( trace != null && traceAppend != null )
            throw new M4CMISException(-1);

        MCommandOption traceError = m_parser.getCommandOption("-TE");
        MCommandOption traceErrorAppend = m_parser.getCommandOption("-TE+");
        if( traceError != null && traceErrorAppend != null )
        {
            throw new M4CMISException(-1);
        }
        
        MCommandOption traceVerbatimError = m_parser.getCommandOption("-TV");
        MCommandOption traceVerbatimAppend = m_parser.getCommandOption("-TV+");
        if( traceVerbatimError != null && traceVerbatimAppend != null )
        {
            throw new M4CMISException(-1);
        }

        if( (trace != null || traceAppend != null) && (traceError != null || traceErrorAppend != null) )
        {
            throw new M4CMISException(-1);
        }
    }

    protected void validateCommandOptionCombinations() throws MException
    {
        validateTraceOptions();        
    }

    /**
     * Sets traceFile and isTraceAppend fields based on the command line.
     *
     * @throws MException
     */
    protected void populateTraceOptions(int iCommonTraceSwitch, String traceFileName) throws MException
    {
    	 // Get the trace file
        MCommandOption traceOption = m_parser.getCommandOption("-T");
        
        isTraceVerbatim = false;

        if( traceOption != null )
        {
        	 iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
             isTraceAppend = false;
        }
        else
        {
        	 // try trace append
            traceOption = m_parser.getCommandOption("-T+");
            if( traceOption != null )
            {
                iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                isTraceAppend = true;
            }
            else
            {
            	 traceOption = m_parser.getCommandOption("-TE");
            	 
                 if( traceOption == null )
                 {
                     // try trace append
                     traceOption = m_parser.getCommandOption("-TE+");
                     
                     if( traceOption != null )
                     {
                         iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                         isTraceAppend = true;
                         isTraceError = true;
                     }
                     else
                     {
                         traceOption = m_parser.getCommandOption("-TV");
                         
                         if( traceOption == null )
                         {
                             // try trace append
                             traceOption = m_parser.getCommandOption("-TV+");
                             if( traceOption != null )
                             {
                                 iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                                 isTraceAppend = true;
                                 isTraceError = false;
                                 isTraceVerbatim = true;
                             }
                             else
                             {
                             	 iTraceFile = null;
                                  isTraceAppend = false;
                                  isTraceError = false;
                                  isTraceVerbatim = true;
                             }
                         }
                         else
                         {
                         	 iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                              isTraceAppend = false;
                              isTraceVerbatim = true;
                              isTraceError = false;
                         }
                     }
                 }
                 else
                 {
                     iTraceFile = traceOption.getArgument(0, m_defaultTraceFile);
                     isTraceAppend = false;
                     isTraceError = true;
                 }
                 
                 

                 if( iCommonTraceSwitch == MConstants.MPI_TRACE_SWITCH_ON )
                 {
                     iTraceFile = traceFileName;
                     isTraceAppend = true;
                     isTraceError = false;
                 }
            }
        }   
    }

    /**
     * Populates command option fields.
     *
     * @exception MException
     */
    protected void populateFields(int iCommonTraceSwitch, String traceFileName) throws MException
    {          
        MCommandOption opt = null;
		
		opt = m_parser.getCommandOption("-K");
        
		if( opt != null )
		{
            iKey = opt.getArgument(0, null);
		}
		else
		{
			opt = m_parser.getCommandOption("-KEY");
			
			if( opt != null )
			{
				iKey = opt.getArgument(0, null);
			}
		}
		

        
        opt = m_parser.getCommandOption("-N");
        if( opt != null )
		{
            iName = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-NAME");
			
			if( opt != null )
			{
				iName = opt.getArgument(0, null);
			}
		}
        
        opt = m_parser.getCommandOption("-V");
        
        if( opt != null )
		{
        	iNewVersion = true;
		}
        else
		{
			opt = m_parser.getCommandOption("-VERSION");
			
			if( opt != null )
			{
				iNewVersion = true;
			}
		}
                     
        
        opt = m_parser.getCommandOption("-USR");
        
		if( opt != null )
		{
            iUser = opt.getArgument(0, null);
		}
			
				    
        
        opt = m_parser.getCommandOption("-PWD");
        
		if( opt != null )
		{
            iPassword = opt.getArgument(0, null);
		}
				        
        
        opt = m_parser.getCommandOption("-URL");
        
		if( opt != null )
		{
            iURL = opt.getArgument(0, null);
		}
		
		opt = m_parser.getCommandOption("-R");
      
		if( opt != null )
		{
            iRepo = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-REPO");
			
			if( opt != null )
			{
				iRepo = opt.getArgument(0, null);
			}
		}
		
		 
        opt = m_parser.getCommandOption("-NOW");
        
		if( opt != null )
		{
			iNow = true;
		}
		
		opt = m_parser.getCommandOption("-COMMENTS");
	      
		if( opt != null )
		{
            iComments = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-C");
			
			if( opt != null )
			{
				iComments = opt.getArgument(0, null);
			}
		}
		
		opt = m_parser.getCommandOption("-PATH");
	      
		if( opt != null )
		{
			iDocPath = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-P");
			
			if( opt != null )
			{
				iDocPath = opt.getArgument(0, null);
			}
		}
		
		opt = m_parser.getCommandOption("-WHERE");
		
		if( opt != null )
		{
			iSQLQuery = opt.getArgument(0, null);
		}
        else
		{
			opt = m_parser.getCommandOption("-W");
			
			if( opt != null )
			{
				iSQLQuery = opt.getArgument(0, null);
			}
		}
		
		
       	// Get trace
        populateTraceOptions(iCommonTraceSwitch,traceFileName);
    }

    /**
     * Dumps the options found on the command line to the specified trace file.
     *
     * @param trace Trace file object.
     */
    public void dumpCommandOptions(MTrace trace)
    {
        if( trace == null )
            return;
           
        try
        {
            if( !isTraceError )
            {
                trace.println(" Trace file      | " + iTraceFile);
                trace.println(" Trace append    | " + isTraceAppend);
                trace.println(" Trace error     | " + isTraceError);
            }
			if	( iKey != null ) trace.println(" Key     | " + iKey);
			trace.println(" New Version     | " + (iNewVersion ? "true" : "false"));			
			if	( iName != null ) trace.println(" Document Name     | " + iName);
			if  (iUser != null) trace.println(" User     | " + iUser);
			if  (iRepo != null)  trace.println(" Respository Name     | " + iRepo);		 
		    if  (iURL != null) trace.println(" URL	| " + iURL);
		    if  (iComments != null) trace.println(" Comments	| " + iComments);
		    trace.println(" Now		| " + (iNow ? "true" : "false"));
		    if (iDocPath != null) trace.println(" Document Path	| " + iDocPath);
		    if (iSQLQuery != null)  trace.println(" Where Condition Path	| " + iSQLQuery);
        }
        catch( MException e )
        {
            // ignore, this is just a trace, there is no corrective action
        }
    }
}
