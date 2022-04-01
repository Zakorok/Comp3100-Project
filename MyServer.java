import java.io.*;
import java.net.*;

public class MyServer{
	public static void main(String[] args)	{
		try{
			ServerSocket ss = new ServerSocket(6363);
			Socket s = ss.accept();

			DataInputStream dis = new DataInputStream(s.getInputStream());

			String str = (String)dis.readUTF();
			System.out.println("Client Message accepted = " + str);

			ss.close();
		} catch (Exception ex){
			System.out.println(ex);
		}
	}	
}