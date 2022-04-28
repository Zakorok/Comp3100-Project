public class JobInfo {
    public String ServerResponse;
    public int SubmitTime;
    public int JobID;
    public int EstRuntime;
    public int Core;
    public int Memory;
    public int Disk;

    public JobInfo(String[] info){
        ServerResponse = info[0];
        if(!None() && !JobComplete()){
            SubmitTime = Integer.parseInt(info[1]);
            JobID = Integer.parseInt(info[2]);
            EstRuntime = Integer.parseInt(info[3]);
            Core = Integer.parseInt(info[4]);
            Memory = Integer.parseInt(info[5]);
            Disk = Integer.parseInt(info[6]);
        }
    }

    public boolean None(){
        if(ServerResponse.equals("NONE")){
            return true;
        } 
        return false;
    }
    public boolean JobComplete(){
        if(ServerResponse.equals("JCPL")){
            return true;
        } 
        return false;
    }
}
