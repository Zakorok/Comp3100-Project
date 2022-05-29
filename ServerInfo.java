import java.util.ArrayList;
import java.util.List;

/*
This is a class that holds information about servers
It also can calculate when it would be avaliable for the next job
*/

public class ServerInfo {
	// From ds-sim formatted
	public String Type;
	public String ID;
	public String State;
	public String StartTime;
	public int TotalCores;
	public int Memory;
	public int Disk;
	public int wJobs;
	public int rJobs;

	// Others
	public int currentCores;

	public ServerInfo(String[] information) {
		this.Type = information[0];
		this.ID = information[1];
		this.State = information[2];
		this.StartTime = information[3];
		this.TotalCores = Integer.parseInt(information[4]);
		this.Memory = Integer.parseInt(information[5]);
		this.Disk = Integer.parseInt(information[6]);
		this.wJobs = Integer.parseInt(information[7]);
		this.rJobs = Integer.parseInt(information[8]);
		this.currentCores = this.TotalCores;
	}

	// Sends the sechedule job command to the server
	public void ScheduleJob(JobInfo job) throws Exception {
		MyClient.SendMessage("SCHD " + job.JobID + " " + this.Type + " " + this.ID);
	}

	// Calculates the soonest avaliable time that it would be for this server to
	// take another job with those cores.
	public int CalcAvaliableForJob(int requiredCores) {
		try {
			List<JobInfo> runningJobs = new ArrayList<JobInfo>();
			List<JobInfo> waitingJobs = new ArrayList<JobInfo>();

			MyClient.SendMessage("LSTJ " + this.Type + " " + this.ID);
			String[] info = MyClient.SplitRead(" ");
			int numJobs = Integer.parseInt(info[1]);
			MyClient.SendMessage("OK");

			// Split the list of jobs into running and waiting they need to be treated
			// differently,
			for (int i = 0; i < numJobs; i++) {
				JobInfo listedJob = new JobInfo(MyClient.SplitRead(" "), this.ID);
				if (listedJob.StartTime == -1) {
					waitingJobs.add(listedJob);
				} else {
					runningJobs.add(listedJob);
				}
			}
			MyClient.SendMessage("OK");
			MyClient.ReadServer();

			// Calculate each running job's estimated completion time
			int nextAvaliableTime = 0;
			for (JobInfo runningJob : runningJobs) {
				nextAvaliableTime = runningJob.EstCompletedTime(0);
				currentCores += runningJob.Core;

				// Calculate when a waiting job is next going to run
				if (waitingJobs.size() == 0) {
					if (currentCores >= requiredCores) {
						return nextAvaliableTime;
					}
				} else {
					// Preteneds to implement job if they can be on core count.
					// Adds their time to the nextAvaliable time.
					List<JobInfo> future = new ArrayList<JobInfo>();
					for (JobInfo waitingJob : waitingJobs) {
						if (waitingJob.Core <= currentCores) {
							currentCores -= waitingJob.Core;
							nextAvaliableTime = waitingJob.EstCompletedTime(nextAvaliableTime);
							future.add(waitingJob);
						}
					}
					waitingJobs.removeAll(future);
				}
			}

			return nextAvaliableTime;

		} catch (Exception ex) {
			System.out.println(ex);
			// If something goes wrong
			return 1000000000;
		}
	}
}
