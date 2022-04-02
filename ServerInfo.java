import java.io.*;
import java.net.*;

public class ServerInfo{
	//From ds-sim formatted
	public String Type;
	public String ID;
	public String State;
	public String StartTime;
	public int Cores;
	public int Memory;
	public int Disk;
	public String wJobs;
	public String rJobs;

	//Others
	public int Limit;

	public ServerInfo(String[] information){
		this.Type = information[0];
		this.ID = information[1];
		this.State = information[2];
		this.StartTime = information[3];
		this.Cores = Integer.parseInt(information[4]);
		this.Memory = Integer.parseInt(information[5]);
		this.Disk = Integer.parseInt(information[6]);
		this.wJobs = information[7];
		this.rJobs = information[8];
		
		this.Limit = 0;
	}

}