/*
This class holds job data and has a few helper functions to describe the job
It handles JOBN, JCPL, NONE and listing jobs from a server.

*/
public class JobInfo {
    // Normal Jobs: JOBN
    public String ServerResponse;
    public int SubmitTime;
    public int JobID;
    public int EstRuntime;
    public int Core;
    public int Memory;
    public int Disk;

    public JobInfo(String[] info) {
        ServerResponse = info[0];
        if (!None()) {
            if (!isComplete()) {
                SubmitTime = Integer.parseInt(info[1]);
                JobID = Integer.parseInt(info[2]);
                EstRuntime = Integer.parseInt(info[3]);
                Core = Integer.parseInt(info[4]);
                Memory = Integer.parseInt(info[5]);
                Disk = Integer.parseInt(info[6]);
            }
        }
    }

    // Notifies if there is no jobs left
    public boolean None() {
        if (ServerResponse.equals("NONE")) {
            return true;
        }
        return false;
    }

    // Notifies if this is just a job complete message
    public boolean isComplete() {
        if (ServerResponse.equals("JCPL")) {
            return true;
        }
        return false;
    }

    // If the Job is being listed from server.
    public String SeverType;
    public String ServerID;
    public String Status;
    public int StartTime;

    public JobInfo(String[] info, String serverID) {
        this.JobID = Integer.parseInt(info[0]);
        this.Status = info[1];
        this.SubmitTime = Integer.parseInt(info[2]);
        this.StartTime = Integer.parseInt(info[3]);
        this.EstRuntime = Integer.parseInt(info[4]);
        this.Core = Integer.parseInt(info[5]);
        this.Memory = Integer.parseInt(info[6]);
        this.Disk = Integer.parseInt(info[7]);
        this.ServerID = serverID;
    }

    // Caluclates the estimated completion time.
    public int EstCompletedTime(int calculatedStartTime) {
        if (StartTime != -1 || calculatedStartTime == 0) {
            return this.StartTime + this.EstRuntime;
        }
        return calculatedStartTime + this.EstRuntime;
    }
}
