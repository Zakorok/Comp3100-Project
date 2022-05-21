public class JobInfo {
    public String ServerResponse;
    public int SubmitTime;
    public int JobID;
    public int EstRuntime;
    public int Core;
    public int Memory;
    public int Disk;

    //If the job is complete
    public int EndTime;
    public String SeverType;
    public String ServerID;

    public JobInfo(String[] info){
        ServerResponse = info[0];
        if(!None()){
            if(isComplete()) {
                EndTime = Integer.parseInt(info[1]);
                JobID = Integer.parseInt(info[2]);
                ServerID = info[3];
            } else {
                SubmitTime = Integer.parseInt(info[1]);
                JobID = Integer.parseInt(info[2]);
                EstRuntime = Integer.parseInt(info[3]);
                Core = Integer.parseInt(info[4]);
                Memory = Integer.parseInt(info[5]);
                Disk = Integer.parseInt(info[6]);
            }
        }
    }

    public boolean None(){
        if(ServerResponse.equals("NONE")){
            return true;
        } 
        return false;
    }
    
    public boolean isComplete(){
        if(ServerResponse.equals("JCPL")){
            return true;
        } 
        return false;
    }
}
