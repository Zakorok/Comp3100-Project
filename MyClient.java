import java.io.*;
import java.net.*;

public class MyClient{
	private static BufferedReader in;
	private static DataOutputStream out;
	public static void main(String[] args)	{
		try{
			//Set up client. 
			Socket socket = new Socket("localhost",50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());

			SendMessage("HELO");
			ReadServer();

			//Authenticate  
			SendMessage("AUTH cassandra");
			ReadServer();

			SendMessage("REDY");
			String jobData = ReadServer();
			String[] jobInfo = jobData.split(" ");
			
			SendMessage("GETS All");
			String data = ReadServer();
			String[] info = data.split(" ");

			SendMessage("OK");

			//Finding the largest servers
			int mostCores = 0;
			int limit = 0;
			String[] largestServer = new String[1];
			for(int i = 0; i < Integer.parseInt(info[1]); i++){ 
				String line = ReadServer();
				ServerInfo serverInfo = new ServerInfo(line.split(" "));
				int numCores = serverInfo.Cores;

				if(numCores == mostCores && largestServer[0].equals(serverInfo.Type)){
					limit ++;
				}

				if(numCores > mostCores){
					mostCores = numCores;
					largestServer = line.split(" ");
					limit = 0;
				}
			} 

			SendMessage("OK");
			ReadServer();

			int serverInterateNum = 0;
			//SCHD jobID serverType serverID
			while(!jobInfo[0].equals("NONE")){
				SendMessage("SCHD " + jobInfo[2] + " " + largestServer[0] + " " + serverInterateNum);
				ReadServer();

				serverInterateNum ++; // Round Robin
				if(serverInterateNum > limit){
					serverInterateNum = 0;
				}

				
				SendMessage("OK");
				ReadServer();

				SendMessage("REDY"); //TODO: Simply - recurision? (Probably not, would get memory intensive)
				jobData = ReadServer();
				if(jobData.isEmpty()) { //TODO: Understand this issue 
					jobData = ReadServer(); 
				}
				jobInfo = jobData.split(" ");

				while(jobInfo[0].equals("JCPL")){
					SendMessage("REDY");
					jobData = ReadServer();
					if(jobData.isEmpty()) {
						jobData = ReadServer(); 
					}
					jobInfo = jobData.split(" ");
				}
			}

			//Terminate
			SendMessage("QUIT");

			//Close stream
			socket.close();


		} catch (Exception ex){
			System.out.println("Error: \n" + ex);
		}
	}
	
	private static void SendMessage(String input) throws Exception{
		out.write((input + "\n").getBytes());
		out.flush();
	}
	
	private static String ReadServer() throws Exception {
		while(!in.ready()){
			Thread.sleep(100); //TODO: Test timings
		}
 		String str = "" + in.readLine(); 
		System.out.println("Server out: " + str);
		return str;
	}
}