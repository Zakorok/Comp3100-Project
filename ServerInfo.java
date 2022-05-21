import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Comparator;

public class ServerInfo{
	//From ds-sim formatted
	public String Type;
	public String ID;
	public String State;
	public String StartTime;
	public int TotalCores;
	public int Memory;
	public int Disk;
	public String wJobs;
	public String rJobs;

	//Others
	public int Limit;
	public int currentCores;
	private ArrayList<JobInfo> CurrentJobs = new ArrayList<>();

	public ServerInfo(String[] information){
		this.Type = information[0];
		this.ID = information[1];
		this.State = information[2];
		this.StartTime = information[3];
		this.TotalCores = Integer.parseInt(information[4]);
		this.Memory = Integer.parseInt(information[5]);
		this.Disk = Integer.parseInt(information[6]);
		this.wJobs = information[7];
		this.rJobs = information[8];
		
		this.Limit = 0;
		this.currentCores = this.TotalCores;
	}

	public void ScheduleJob(JobInfo job) throws Exception {
		MyClient.SendMessage("SCHD " + job.JobID + " " + this.Type + " " + this.ID);
		this.CurrentJobs.add(job);
		this.currentCores -= job.Core;
	}

	public int HasJob(JobInfo job){
		int count = 0;
		for (JobInfo jobInList : CurrentJobs) {
			if(jobInList.JobID == job.JobID) {
				return count;
			}
			count ++;
		}
		return -1;
	}
}

class SortByCore implements Comparator<ServerInfo> {
	@Override // Greater cores are to be sorted higher in the list. 
	public int compare(ServerInfo left, ServerInfo right) {
		return right.TotalCores - left.TotalCores;
	}
}