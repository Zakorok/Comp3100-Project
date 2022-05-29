import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class MyClient {
	private static BufferedReader in;
	private static DataOutputStream out;
	
	public static void main(String[] args) {
		try {
			
			// Get server boot times
			Hashtable<String, Integer> serverBoot = new Hashtable<String, Integer>();
			File xmlFile = new File("ds-system.xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("server");
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				Element eElement = (Element) nNode;
				serverBoot.put(eElement.getAttribute("type"), Integer.parseInt(eElement.getAttribute("bootupTime")));
			}

			// Set up client.
			Socket socket = new Socket("localhost", 50000);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new DataOutputStream(socket.getOutputStream());

			SendMessage("HELO");
			ReadServer();

			// Authenticate
			SendMessage("AUTH cassandra");
			ReadServer();

			// Main ----------------------------------
			SendMessage("REDY");
			JobInfo currentJob = new JobInfo(SplitRead(" "));

			while (!currentJob.None()) {
				SendMessage("GETS Capable " + currentJob.Core + " " + currentJob.Memory + " " + currentJob.Disk);
				String[] info = SplitRead(" ");
				int numServers = Integer.parseInt(info[1]);
				SendMessage("OK");

				ArrayList<ServerInfo> servers = new ArrayList<>();
				for (int i = 0; i < numServers; i++) {
					servers.add(new ServerInfo(SplitRead(" ")));
				}
				SendMessage("OK");
				ReadServer();
				//Sort servers by waiting jobs 
				servers = (ArrayList<ServerInfo>) servers.stream().sorted((left, right) -> {
					int result = left.wJobs - right.wJobs;
					if(result == 0){
						result = left.TotalCores - right.TotalCores;
					}
					return result;
				}).collect(Collectors.toList());

				ServerInfo chosenServer = servers.stream().filter(s -> s.State.equals("idle")).findFirst().orElse(null);
				if (chosenServer == null) {
					final int requiredCores = currentJob.Core;

					var result = servers.stream().filter(s -> s.State.equals("active"))
							.min((s1, s2) -> s1.ListJobs(requiredCores) - s2.ListJobs(requiredCores));
					if (result.isPresent()) {
						chosenServer = result.get();
						// If it would be lest time to boot a new server
						int estimatedCompetedTime = chosenServer.ListJobs(requiredCores);
						for (String key : serverBoot.keySet()) {
							if (serverBoot.get(key) < estimatedCompetedTime) {
								chosenServer = servers.stream()
										.filter(s -> s.Type.equals(key) && s.State.equals("inactive")).findFirst()
										.orElse(null);
										break;
							}
						}
					}
				}
				if (chosenServer == null) {
					chosenServer = servers.get(0);
				}
				chosenServer.ScheduleJob(currentJob);

				SendMessage("OK");
				ReadServer();

				currentJob = ReadInJob();

				while (currentJob.isComplete()) {
					currentJob = ReadInJob();
				}
			}
			// ---------------------------------------

			// Terminate
			SendMessage("QUIT");

			// Close stream
			socket.close();

		} catch (Exception ex) {
			System.out.println("Error: \n" + ex);
		}
	}
	private static JobInfo ReadInJob() throws Exception {
		SendMessage("REDY");
		String[] jobArray = SplitRead(" ");
		while (jobArray[0].equals("OK")) {
			jobArray = SplitRead(" ");
		}
		return new JobInfo(jobArray);
	}

	public static void SendMessage(String input) throws Exception {
		out.write((input + "\n").getBytes());
		out.flush();
	}

	// Use when only when expecting a result.
	public static String ReadServer() throws Exception {
		String str = "" + in.readLine();
		if (str.isEmpty()) {
			str = "" + in.readLine(); // TODO: Understand this issue
		}
		System.out.println("Server out: " + str);
		return str;
	}

	public static String[] SplitRead(String regex) throws Exception {
		return ReadServer().split(regex);
	}
}