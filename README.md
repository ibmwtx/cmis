# CMIS Adapter Overview
The Content Management Interoperability Services (CMIS)  Adapter allows ITX maps to interact with CMIS compliant Repositories. 


## Minimum Requirements : 

ITX v9.0.0.x

## Command Alias : 

Adapter commands can be specified by using a command string on the command line or creating a command file that contains adapter commands. The execution command syntax is:

-IM[alias] card_num <br>
-OM[alias] card_num


where -IM is the Input Source Override execution command and -OM is the Output Target Override execution command, alias is the adapter alias, and card_num is the number of the input or output card. 


The following table shows the adapter alias and its execution command.

Adapter 	:  CMIS <br>
Alias 	        :  CMIS <br>
As Input        :  -**IMCMIS**card_num <br>
As Output       :  -**OMCMIS**card_num <br>    	  


## CMIS Adapter commands

The following table lists valid commands for the CMIS Adapter, the command syntax

KEY (-K)     : Command excution on the Repository (ADD, DELETE , UPDATE)

NAME (-N)	  : Document Name

USR (-U )  : Username


PWD (-P )  : Password

URL (-URL) : Atom Pub URL

REPO (-R) : Repository name

NOW (-NOW) : Force the operation. Supported on Target only. 

COMMENTS (-C) : Check in comments



## CMIS Adapter Command Line Examples
###Inbound Adapter : 


Command Line : -U username -P password  -U url -P documentpath


###Outbound Adapter : 


Command Line  : -U username -P password  -U url -KEY ADD -N input.txt -P documentpath -R repository



## CMIS Adapter Installation Instructions 

### Runtime

a) create a com.ibm.websphere.dtx.m4cmis under jars direcotry. 

b) Drop m4cmis.jar in to "WTX INSTALL/jars/com.ibm.websphere.dtx.m4cmis" on windows and UNIX


b) Edit adapters.xml and add the following line (UNIX, the file is present under config folder)

M4Adapter name="CMIS" alias="CMIS" id="165" type="app" class="com/ibm/websphere/dtx/m4cmis"


d) Download Apache CMIS artifacts from [CMIS](https://chemistry.apache.org/java/download.html) version 0.13.x (OpenCMIS Client with dependencies).  

Copy chemistry-opencmis-client-api-0.13.0.jar,

    chemistry-opencmis-client-bindings-0.13.0.jar, 
     
    chemistry-opencmis-client-impl-0.13.0.jar, 
    
    chemistry-opencmis-commons-api-0.13.0.jar, 
    
    chemistry-opencmis-commons-impl-0.13.0.jar, 
    
    slf4j-api-1.7.5.jar  to "WTX INSTALL DIR/jars/com.ibm.websphere.dtx.m4cmis"
    
