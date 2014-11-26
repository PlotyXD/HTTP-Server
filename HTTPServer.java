import java.io.*; 
import java.net.*; 
import java.util.*;
import java.text.* ;

/*
 * I have Implemented a Java Server which supports HTTP1.0 with and without Keep-alive, HTTP 1.1, And Multithreaded server
 * References from the web that I used are-
 * Header formats and packet formats - http://www.jmarshall.com/easy/http/#responseline 
 * Basic Socket programming - http://www.tutorialspoint.com/java/java_networking.htm
 * Basic MultiThreading - http://www.tutorialspoint.com/java/java_multithreading.htm
 * Bash commands in ubuntu - http://ubuntuforums.org/showthread.php?t=319735
 * Basic Java Tutorial - http://www.tutorialspoint.com/java/
 */

class HTTPServer implements Runnable{ 
	Socket ephemeral;							// Ephemeral is the name of socket, username is ~xyz
	String userName="";
	
	HTTPServer(Socket mediocre){			//Declaration
		this.ephemeral=mediocre;
	}

	public static void main(String argv[]) throws Exception{	//Main funtion that creates thread for each new connection, If no port is mentioned, its 7777	
			int port=0;
			if(argv.length==0){
				System.out.println("Selected default port. Starting server on port 7777");
				port=7777;
			}
			else {
				port=Integer.parseInt(argv[0]);
				System.out.println("Running server on port "+port);
			}
			ServerSocket eternal = new ServerSocket(port);
			while(true){										//Creating new Thread		
				Socket mediocre = eternal.accept();
				new Thread(new HTTPServer(mediocre)).start();
			}	
	}

	String contentTypeReturn(String a) throws IOException{ //This function gives the content type header of HTTP Response packet 
		String outputType="";
		if(a.endsWith(".jpg")){outputType="image/jpg";}
		else if(a.endsWith(".css")){outputType="text/css";}
		else if(a.endsWith(".php")){outputType="text/php";}
		else if(a.endsWith(".pdf")){outputType="Application/pdf";}
		else if(a.endsWith(".svg")){outputType="image/svg+xml";}
		else if(a.endsWith(".html")){outputType="text/html";}
		else if(a.endsWith(".png")){outputType="image/png";}
		else if(a.endsWith(".jpeg")){outputType="image/jpeg";}
		else if(a.endsWith(".js")){outputType="text/javascript";}
		else if(a.endsWith(".gif")){outputType="image/gif";}
		else if(a.endsWith(".htm")){outputType="text/htm";}
		else if(a.endsWith(".xml")){outputType="text/xml";}
		return outputType;
	}

	String getPath(String userName) throws IOException{		//This gives the path when user name is given. Eg, for ~sgondala, it returns /users/ug12/
		Process proc=Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo " + userName});
	    BufferedReader pathReader=new BufferedReader(new InputStreamReader(proc.getInputStream())); 
 		String path=pathReader.readLine();
		String pathSplit[]=path.split("/");
		String output="/";
		for(int i =1;i<pathSplit.length-1;i++){
			output+=pathSplit[i]+"/";
		}
		return output;
	}
	

	public void run(){			// This starts executing when we say Thread.start();
		try{
			ephemeral.setSoTimeout(10000);		// Timeout for socket is set for 10 seconds
			BufferedReader helper=new BufferedReader(new InputStreamReader(ephemeral.getInputStream()));	//Establishing input and output streams
			DataOutputStream out=new DataOutputStream(ephemeral.getOutputStream());
			boolean isNew=false;			// These bools ate to check if HTTP request is 1.0/1.1/1.0+Keep-alive
			boolean isNewVersion=false;

			String temp=helper.readLine();
			String[] parts=temp.split("\\s+"); // Splitting 1st line of request header with "/" to know the type of request  
	
			while(temp.length()!=0){			//Checking if the connection is keep-alive
				temp=helper.readLine();
				if(temp.startsWith("Connection")){
					String[] checkingPersistent=temp.split(":");
					if(checkingPersistent[1].equals(" keep-alive")){
						isNew=true||isNew;
					}
				}
			}
			
			if(parts[2].equals("HTTP/1.1")){isNew=true||isNew;isNewVersion=true;}
			if(isNew){				//This is done if connection is persistent
				while(true){	
					try{
					if((parts[0].equals("GET"))||(parts[0].equals("HEAD"))){	//Checking if Request type is GET/HEAD		
						String fileName="";
						String outputType="";
						String[] fileNameWords=parts[1].split("/");							//parts[1] is /~sgondala/temp.php; fileNameWords[1] is ~sgondala
						if(fileNameWords[1].startsWith("~")){userName=fileNameWords[1];}	// Storing username as somerequests after index.html are in form of GET /file without username
						if(fileNameWords[1].equals(userName)){				//userName is in GET request header
							if(fileNameWords.length==2){					// This is case of GET /~username/, 
								fileName=fileNameWords[1].substring(1)+"/public_html/index.html/";outputType="text/html";
							}  // If request is GET /username/, giving out file name = username/public_html/index.html. Eg:sgondala/public_html/index.html
							else{	//THis is requests of form GET /~username/rel-path-to-file
								String tempPathName=fileNameWords[1].substring(1);
								tempPathName+="/public_html/";
								for(int i=2;i<fileNameWords.length;i++){
									tempPathName+=fileNameWords[i]+"/";
								}
							fileName=tempPathName;
							outputType=contentTypeReturn(fileNameWords[fileNameWords.length-1]); //outputType is content type header of response
							}	//FileName is given as /userName/public_html/rel-path-to-file
						}

						else{	//This is the case when there is no userName in HTTP request Header
							String tempPathName="";
							if(!userName.equals("")){tempPathName= userName.substring(1)+"/public_html/";}
							else {tempPathName="";}
							for(int i=1;i<fileNameWords.length;i++){
								tempPathName+=fileNameWords[i]+"/";
							}
							fileName=tempPathName;
							outputType=contentTypeReturn(fileNameWords[fileNameWords.length-1]); //outputType is content type header of Response
							}	//Adding user name and returning the file name	
						try{
							if(outputType.equals("")){										// This is the case when the required file format is not there,
								String output="HTTP/1.1 200 OK"+"\r\n";						// useful for some frivolous requests by browsers such as favicon.ico is requested by chrome
								Date date = new Date();										// Just returning header with length field as 0
								SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
								output+= "Date: "+ dateFormat.format(date) + "\r\n";
			     	            output+= "Content-Type: " + "text/html" + "\r\n";
			        	        output+= "Content-Length: " + 0 + "\r\n" + "\r\n";
								if(parts[0].equals("GET")){out.writeUTF(output);}
							}
							else{
								
								String pathTempo=getPath(userName);
								String pathFinal=pathTempo+fileName;				//pathFinal is total path from root to file. eg: /users/ug12/~sgondala/public_html/index.html/
								File file=new File(pathTempo+fileName);
								byte[] forOutput=new byte[(int) file.length()];		//Reading file and filling headers for response
								FileInputStream fileStream=new FileInputStream(file);
								fileStream.read(forOutput,0,forOutput.length);
								String output="HTTP/1.1 200 OK"+"\r\n";
								Date date = new Date();
								SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
								output+= "Date: "+ dateFormat.format(date) + "\r\n";
				                output+= "Content-Type: " + outputType + "\r\n";
				                output+= "Content-Length: " + forOutput.length + "\r\n" + "\r\n";
								out.writeUTF(output);								//Headers are sent
								if(parts[0].equals("GET")){out.write(forOutput,0,forOutput.length);}	//Data is sent only for GET, not for HEAD
							}
						}
						catch(FileNotFoundException e){							//If file not found, reporting HTTP 404
							String output="HTTP/1.1 404 Not Found \r\n";
							Date date = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
							output+= "Date: "+ dateFormat.format(date) + "\r\n";
							output+="Content-type: text/html \r\n";
							String content="<HTML><HEAD></HEAD><BODY><h1>Error 404, File Not Found</h1></BODY></HTML>";
							final byte[] stringBytes=content.getBytes("UTF-8");
							output+= "Content-Length: " + stringBytes.length + "\r\n" + "\r\n";
							output+="\r\n";
							out.writeUTF(output);
							if(parts[0].equals("GET")){out.write(stringBytes,0,stringBytes.length);}
							
						}
					}
					else{									// This is the case of bad requests 
						String output="HTTP/1.1 400 Bad Request \r\n";
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
						output+= "Date: "+ dateFormat.format(date) + "\r\n";
						output+="Content-type: text/html \r\n";
						String content="<HTML><HEAD></HEAD><BODY><h1>Error 400, Bad Request</h1></BODY></HTML>";
						final byte[] stringBytes=content.getBytes("UTF-8");
						output+= "Content-Length: " + stringBytes.length + "\r\n" + "\r\n";
						output+="\r\n";
						out.writeUTF(output);
						out.write(stringBytes,0,stringBytes.length);
					}

					temp=helper.readLine();				//Reaing the buffered reader for the next request, as the connection is persistent
					parts=temp.split("\\s+");
					
					while(temp.length()!=0){
						temp=helper.readLine();
					}
				}
				catch(SocketTimeoutException e){		//Catching TimeOutException and closing the socket
					ephemeral.close();

				}
				}
			}

			else{		//This is the case of non-persistent connection. Connection closes after sending response to one request. Same logic as above
				if((parts[0].equals("GET"))||(parts[0].equals("HEAD"))){
					String fileName="";
					String outputType="";
					String[] fileNameWords=parts[1].split("/");  //parts[1] is /~sgondala/temp.php; fileNameWords[1] is ~sgondala
					if(fileNameWords[1].startsWith("~")){userName=fileNameWords[1];}			// userName is ~sgondal
					if(fileNameWords[1].equals(userName)){
						if(fileNameWords.length==2){
							fileName=fileNameWords[1].substring(1)+"/public_html/index.html/";outputType="text/html";
						}  
						else{
							String tempPathName=fileNameWords[1].substring(1);
							tempPathName+="/public_html/";
							for(int i=2;i<fileNameWords.length;i++){
								tempPathName+=fileNameWords[i]+"/";
							}
						fileName=tempPathName;
						outputType=contentTypeReturn(fileNameWords[fileNameWords.length-1]);
						}
					}
					
					else{
						String tempPathName="";
						if(!userName.equals("")){tempPathName= userName.substring(1)+"/public_html/";}
						else {tempPathName="";}
						for(int i=1;i<fileNameWords.length;i++){
							tempPathName+=fileNameWords[i]+"/";
						}
						fileName=tempPathName;
						outputType=contentTypeReturn(fileNameWords[fileNameWords.length-1]);
						}	

					try{
						if(outputType.equals("")){
							String output="HTTP/1.1 200 OK"+"\r\n";
							Date date = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
							output+= "Date: "+ dateFormat.format(date) + "\r\n";
			                output+= "Content-Type: " + "text/html" + "\r\n";
			       	        output+= "Content-Length: " + 0 + "\r\n" + "\r\n";
							if(parts[0].equals("GET")){out.writeUTF(output);}
						}
						else{
							String pathFinal="/users/ug12/"+fileName;
							//System.out.println("/users/ug12/"+fileName);
							File file=new File("/users/ug12/"+fileName);
							byte[] forOutput=new byte[(int) file.length()];
							FileInputStream fileStream=new FileInputStream(file);
							fileStream.read(forOutput,0,forOutput.length);
							String output="HTTP/1.1 200 OK"+"\r\n";
							Date date = new Date();
							SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
							output+= "Date: "+ dateFormat.format(date) + "\r\n";
				            output+= "Content-Type: " + outputType + "\r\n";
				            output+= "Content-Length: " + forOutput.length + "\r\n" + "\r\n";
							out.writeUTF(output);
							if(parts[0].equals("GET")){out.write(forOutput,0,forOutput.length);}
						}
					}
					catch(FileNotFoundException e){
						String output="HTTP/1.1 404 Not Found \r\n";
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
						output+= "Date: "+ dateFormat.format(date) + "\r\n";
						output+="Content-type: text/html \r\n";
						String content="<HTML><HEAD></HEAD><BODY><h1>Error 404, File Not Found</h1></BODY></HTML>";
						final byte[] stringBytes=content.getBytes("UTF-8");
						output+= "Content-Length: " + stringBytes.length + "\r\n" + "\r\n";
						output+="\r\n";
						out.writeUTF(output);
						if(parts[0].equals("GET")){out.write(stringBytes,0,stringBytes.length);}
						
					}		
					
				}
				else{
						String output="HTTP/1.1 400 Bad Request \r\n";
						Date date = new Date();
						SimpleDateFormat dateFormat = new SimpleDateFormat("E',' d/M/y HH:mm:ss z");
						output+= "Date: "+ dateFormat.format(date) + "\r\n";
						output+="Content-type: text/html \r\n";
						String content="<HTML><HEAD></HEAD><BODY><h1>Error 400, Bad Request</h1></BODY></HTML>";
						final byte[] stringBytes=content.getBytes("UTF-8");
						output+= "Content-Length: " + stringBytes.length + "\r\n" + "\r\n";
						output+="\r\n";
						out.writeUTF(output);
						out.write(stringBytes,0,stringBytes.length);
					}				
				ephemeral.close();
			}
		}
		catch(IOException e){
		}
		catch(NullPointerException e){
		}
	}
}

