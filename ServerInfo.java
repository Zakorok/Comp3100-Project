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

		this.Cores = Integer.parseInt(information[4]);

	}

}