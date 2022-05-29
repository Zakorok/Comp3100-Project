import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyClient{
	private static BufferedReader in;
	private static DataOutputStream out;
	private static Boolean Running = true;
	public static void main(String[] args)	{
		try{

			if(args.length > 0){
				//TODO: Input arguments change the algorithims 
			}

			//Set up client. 
			Socket socket = new Socket("localhost",50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());

			SendMessage("HELO");
			ReadServer();

			//Authenticate  
			SendMessage("AUTH cassandra");
			ReadServer();

			//Main ----------------------------------
			MyImplementation();
			//---------------------------------------

			//Terminate
			SendMessage("QUIT");

			//Close stream
			socket.close();


		} catch (Exception ex){
			System.out.println("Error: \n" + ex);
		}
	}

	private static void MyImplementation() throws Exception {
		SendMessage("REDY");
		JobInfo currentJob = new JobInfo (SplitRead(" "));
		
		while(!currentJob.None()){
			SendMessage("GETS Capable " + currentJob.Core + " " + currentJob.Memory + " " + currentJob.Disk);
			String[] info = SplitRead(" ");
			int numServers = Integer.parseInt(info[1]);
			SendMessage("OK");
			
			ArrayList<ServerInfo> servers = new ArrayList<>();
			for(int i = 0; i < numServers; i++){ 
				servers.add(new ServerInfo(SplitRead(" ")));
			}
			SendMessage("OK");
			ReadServer();
			try{
				//Get the desired server
				Collections.sort(servers, new Comparator<ServerInfo>(){
					@Override
					public int compare(ServerInfo left, ServerInfo right){		
						int result = 0;
						result = left.wJobs - right.wJobs;
						if(result == 0){
							result = left.TotalCores - left.TotalCores;
						}				
						return result;
					}
				});
		} catch (Exception ex){
			System.out.println(ex.getMessage() + "\r\n" +ex.getStackTrace());
		}
			ServerInfo idleServers = servers.stream()
				.filter(s -> s.State.equals("Idle")).findFirst().orElse(null);
			ServerInfo activeServers = servers.stream()
				.filter(s -> s.State.equals("Active")).findFirst().orElse(null);
			
			ServerInfo chosenServer = null;
			if(idleServers == null){
				chosenServer = idleServers;
			}
			if(chosenServer == null){
				chosenServer = activeServers;;
			}
			if(chosenServer == null){
				chosenServer = servers.get(0);
			}
			chosenServer.ScheduleJob(currentJob);

			SendMessage("OK");
			ReadServer();

			currentJob = ReadInJob();

			while(currentJob.isComplete()) {
				// for (ServerInfo server : servers) {
				// 	int jobIndex = server.HasJob(currentJob);
				// 	if(jobIndex > -1) {
				// 		server.CompleteJob(jobIndex);
				// 	}
				// }
				currentJob = ReadInJob();
			}
		}
	}
 
	private static JobInfo ReadInJob() throws Exception { 
		SendMessage("REDY"); 
		String[] jobArray = SplitRead(" ");
		while (jobArray[0].equals("OK")){
			jobArray = SplitRead(" ");
		}
		return new JobInfo(jobArray);
	}

	private static void FirstCapable() throws Exception {
		SendMessage("REDY");
		JobInfo jobInfo = new JobInfo(SplitRead(" "));

		while(!jobInfo.None()){
			SendMessage("GETS Capable " + jobInfo.Core + " " + jobInfo.Memory + " " + jobInfo.Disk);
			String[] info = SplitRead(" ");
			int numServers = Integer.parseInt(info[1]);
			

			SendMessage("OK");
			ServerInfo firstCapable = new ServerInfo(SplitRead(" "));
			for(int i = 1; i < numServers; i ++){
				ReadServer();
			}
			SendMessage("OK");
			ReadServer();

			SendMessage("SCHD " + jobInfo.JobID + " " + firstCapable.Type + " " + firstCapable.ID);
			ReadServer();

			SendMessage("OK");
			ReadServer();
			
			SendMessage("REDY"); //TODO: Simply - recurision? (Probably not, would get memory intensive)
			jobInfo = new JobInfo(SplitRead(" "));

			while(jobInfo.isComplete()){
				SendMessage("REDY");
				jobInfo = new JobInfo(SplitRead(" "));
			}
		}
	}
	
	private static void LRR() throws Exception {
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

			if(tempServer.TotalCores == mostCores){
				//This is so that we only select the first one with the largest cores, specific for Stage 1
				if(largestServer.Type.equals(tempServer.Type)){ 
					largestServer.Limit ++;
				}
			}

			if(tempServer.TotalCores > mostCores){
				mostCores = tempServer.TotalCores;
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
	}
	
	static void SendMessage(String input) throws Exception {
		out.write((input + "\n").getBytes());
		out.flush();
	}
	
	//Use when only when expecting a result.
	private static String ReadServer() throws Exception {
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