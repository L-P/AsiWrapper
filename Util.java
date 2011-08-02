/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;

/// Global utils class.
public class Util {


/** Returns the current time.
 * \return current time in ms (timestamp-like). */
public static long getTime()
{
	return GregorianCalendar.getInstance().getTime().getTime();
}


/** Pauses (blocks the script).
 * \param ms time to sleep in ms. */
public static void sleep(final long ms)
{
	final long startTime = getTime();
	while(getTime() - startTime < ms) {}
}


/** Creates the unique (dated) name of the current backup file.
 * \return the backup name. */
private static String getBackupName()
{
	final GregorianCalendar cal = new GregorianCalendar();
	String ret = "";

	ret += Conf.serverConfig.getProperty("level-name", "world") + "_";
	ret += cal.get(GregorianCalendar.YEAR) + "-" + (cal.get(GregorianCalendar.MONTH)+1) + "-" + cal.get(GregorianCalendar.DAY_OF_MONTH) + "_";
	ret += cal.get(GregorianCalendar.HOUR_OF_DAY) + "h" + cal.get(GregorianCalendar.MINUTE) + "m" + cal.get(GregorianCalendar.SECOND) + "s.tar.gz";

	return ret;
}


/// Backups the whole world, does not work under Windows.
// TODO : Make it work under Windows. (it will never happen)
public static void backupWorld()
{
	if(System.getProperty("os.name").contains("Windows")) {
		System.err.println("[WARNING] The /backup command is unimplemented under Windows systems.");
		return;
	}

	try {
		if(!(new File("backups")).isDirectory())
			Runtime.getRuntime().exec("mkdir backups");
	} catch(Exception e) {
		e.printStackTrace();
	}

	Cmd.raw("save-all");
	Util.sleep(5000);

	Cmd.raw("save-off");
	Util.sleep(1000);

	boolean hasExited = false;
	final String cmd = "tar czf backups/" +  getBackupName() + " " + Conf.serverConfig.getProperty("level-name", "world") + "/ minecraft_server.jar";
	Process tar = null;

	try {
		tar = Runtime.getRuntime().exec(cmd);
	} catch(Exception e) {
		e.printStackTrace();
	}

	if(tar == null)
		return;

	final long	startTime = Util.getTime();
	while(!hasExited && (Util.getTime() - startTime) < 10000) {
		try { hasExited = tar.exitValue() != -1; } catch(Exception e) {}
	}

	Cmd.raw("save-on");
}

/** Fetches data from an URL.
 * \param url URL to request.
 * \return contents of the requested URL. */
public static String fetchDataFromUrl(final String url)
{
	String ret = "";
	try {
		URL page = new URL(url);	
		HttpURLConnection conn = (HttpURLConnection) page.openConnection();
		conn.connect();
		InputStreamReader in = new InputStreamReader((java.io.InputStream) conn.getContent());
		BufferedReader buff = new BufferedReader(in);
		ret = buff.readLine();
	} catch (Exception e) {
		e.printStackTrace();
	}

	return ret;
}


}

