/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

import java.io.*;
import java.util.*;

/// The server Wrapper, a thread in wich another thread (the server) is started.
public class Wrapper extends Thread {

private Process			serv; ///< The server thread.
private BufferedReader	servout; ///< Server standard output.
private BufferedReader	serverr; ///< Server error output.
public static PrintWriter servin; ///< Server standard input.
// servin is static so we can access it from Cmd. The wrapper is not supposed to run more than one server anyway.

private boolean			stop; ///< Tells the wrapper to quit if the server was stopped from the command-line.
private BufferedReader	stdin; ///< Wrapper standard input.
private EventHandler	handler; ///< Instanciated EventHandler.

public static ArrayList<String> players; ///< List of online players.
// players is static for the same reasons servin is.

/// Contructor, starts the server.
public Wrapper()
{
	stop = false;
	players = new ArrayList<String>();

	try {
		if(System.getProperty("os.name").contains("Windows"))
			serv = Runtime.getRuntime().exec("cmd /q /s /c \"start /b /wait /high java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui\"");
		else
			serv = Runtime.getRuntime().exec("java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui");

		servout	= new BufferedReader(new InputStreamReader (serv.getInputStream()));
		serverr	= new BufferedReader(new InputStreamReader (serv.getErrorStream()));
		servin	= new PrintWriter	(new OutputStreamWriter(serv.getOutputStream()), true);
		stdin	= new BufferedReader(new InputStreamReader (System.in));
	} catch (Exception e) {
		e.printStackTrace();
		System.exit(2);
	}
}


/// Sends the server's stdout to the wrapper's stdout.
private void redirectStdout()
{
	try {
		while(servout.ready()) {
			System.out.println(servout.readLine());
		}
	} catch(IOException e) {
		e.printStackTrace();
	}
}


/// Sends the wrapper's stdin to the server's stdin.
private void redirectStdin()
{
	try {
		String line = null;
		while(stdin.ready()) {
			line = stdin.readLine();
			servin.println(line);
			if(line.equalsIgnoreCase("stop")) {
				stop = true;
				break;
			}

			// Somewhat hack-ish
			if(line.contains("allow") || line.contains("reset") || line.equals("backup")) {
				line = "0000-00-00 00:00:00 [INFO] CONSOLE issued server command: " + line;
				EventHandler.handleGameCommand(new Event(line), true);
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
}


/** Returns true if the server has stopped running.
 * \return true is the server has stopped. */
// TODO : Relying on an exception is ugly, find something else.
public boolean hasStopped()
{
	int val = -1;
	try {
		val = serv.exitValue();
	} catch (Exception e) {}

	return val != -1;
}


/** Fetches the events from the server's stderr.
 * \return one event (line) per List entry. */
private ArrayList<String> getEvents()
{
	ArrayList<String> ret = new ArrayList<String>();
	String line = null;

	for(;;) {
		try {
			if(!serverr.ready())
				break;
			line = serverr.readLine();
		} catch(IOException e) {
			e.printStackTrace();
			break;
		}

		if(line == null)
			continue;

		System.err.println(line); // As if it was redirectStderr
		ret.add(line);
	}

	return ret;
}


/// Gracefully quits the server or kill it after 5 seconds.
private void forceStop()
{
	servin.println("stop");
	System.err.println("[INFO] Waiting for the server to stop.");
	for(int i = 0; i < 10 && !hasStopped(); i++) {
		try { sleep(500); } catch(InterruptedException e) {}
	}
	if(!hasStopped()) {
		System.err.println("[WARNING] Server would not stop, I had to kill it, I'm so sorry.");
		serv.destroy();
	}
}



/// Wrapper's main loop that listen for events and feed them to EventHandler.
@Override public void run()
{
	while(!stop && !hasStopped()) {
		redirectStdin();
		redirectStdout();

		try {
			for(String s : getEvents()) {
				EventHandler.handle(new Event(s));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try { sleep(100); } catch(InterruptedException e) {}
	}

	// If the server did not stop, KILL IT.
	if(!hasStopped()) 
		forceStop();
}


/** Tells if the server was stopped from the command-line.
 * \return true if the server was stopped from the command-line. */
public boolean wasNiceQuit()
{
	/* Stop is set only if the server was stopped via the stop command from stdin, not from within the game.
	 * This way, it is only possible to stop the server with an access to the host. Ops will only be allowed
	 * to restart the server. */
	return stop;
}

}

