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
			String[] jobInfo = SplitRead(" ");
			
			SendMessage("GETS All");
			String[] info = SplitRead(" ");
			int numServers = Integer.parseInt(info[1]);

			SendMessage("OK");

			//Finding the largest servers
			int mostCores = 0;
			ServerInfo largestServer = null;
			for(int i = 0; i < numServers; i++){ 
				ServerInfo tempServer = new ServerInfo(SplitRead(" "));

				if(tempServer.Cores == mostCores){
					//This is so that we only select the first one with the largest cores, specific for Stage 1
					if(largestServer.Type.equals(tempServer.Type)){ 
						largestServer.Limit ++;
					}
				}

				if(tempServer.Cores > mostCores){
					mostCores = tempServer.Cores;
					largestServer = tempServer;
				}
			} 

			SendMessage("OK");
			ReadServer();

			int count = 0;
			//SCHD jobID serverType serverID
			while(!jobInfo[0].equals("NONE")){
				SendMessage("SCHD " + jobInfo[2] + " " + largestServer.Type + " " + count);
				ReadServer();

				count ++; // Largest Round Robin
				if(count > largestServer.Limit){
					count = 0;
				}

				SendMessage("OK");
				ReadServer();

				SendMessage("REDY"); //TODO: Simply - recurision? (Probably not, would get memory intensive)
				jobInfo = SplitRead(" ");

				while(jobInfo[0].equals("JCPL")){
					SendMessage("REDY");
					jobInfo = SplitRead(" ");
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
	
	private static void SendMessage(String input) throws Exception {
		out.write((input + "\n").getBytes());
		out.flush();
	}
	
	//Use when only when expecting a result.
	private static String ReadServer() throws Exception {
		while(!in.ready()){
			Thread.sleep(100); //TODO: Test timings
		}
 		String str = "" + in.readLine(); 

		if(str.isEmpty()){
			str = "" + in.readLine();  //TODO: Understand this issue 
		}
		System.out.println("Server out: " + str);
		return str;
	}

	private static String[] SplitRead(String regex) throws Exception {
		return ReadServer().split(regex);
	}
}