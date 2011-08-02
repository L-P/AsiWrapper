/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

import java.io.*;
import java.util.*;

/// Provides global access to the configuration of the server and the wrapper.
public class Conf {

private static final String FILE_SERVERCONFIG	= "server.properties";		///< Name of the server configuration file.
private static final String FILE_BLACKLIST		= "items_blacklist.txt";	///< Name of the file containing the blacklisted items.
private static final String FILE_CONFIG			= "wrapper.properties";		///< Name of the wrapper configuration file.
private static final String FILE_ONLINELIST		= "onlinelist.txt";			///< Name of the file in wich the online players list will be written.
private static final String FILE_ITEMS			= "items.txt";				///< Name of the file containing the items aliases.
private static final String FILE_KITS			= "kits.txt";				///< Name of the file containing the kits contents.
private static final String FILE_OPS			= "ops.txt";				///< Name of the file containing the OPs' names.

public static Properties serverConfig;	///< Linked to FILE_SERVERCONFIG.
public static Properties config;		///< Linked to FILE_CONFIG.
public static Properties items;			///< Linked to FILE_ITEMS.
public static Properties kits;			///< Linked to FILE_KITS.


public static ArrayList<Integer> itemsBlacklist;	///< Linked to FILE_BLACKLIST.
public static ArrayList<String>  ops;				///< Linked to FILE_OPS.


/// Checks if the configuration files exists, if not, there are created.
private static void checkAndCreateFiles() throws Exception
{
	final String[] files = {FILE_OPS, FILE_SERVERCONFIG, FILE_ITEMS, FILE_KITS, FILE_BLACKLIST, FILE_CONFIG, FILE_ONLINELIST};
	for(String fileName : files) {
		final File f = new File(fileName);
		if(!f.exists()) {
			f.createNewFile();
			System.err.println("[INFO] " + fileName + " had to be created.");
		}
	}
}


/// Initialises the Properties attributes and loads the corresponding files.
private static void initProperties() throws Exception
{
	serverConfig	= new Properties();
	config			= new Properties();
	items			= new Properties();
	kits			= new Properties();

	serverConfig.load	(new FileReader(FILE_SERVERCONFIG));
	config.load			(new FileReader(FILE_CONFIG));
	items.load			(new FileReader(FILE_ITEMS));
	kits.load			(new FileReader(FILE_KITS));
}


/// Initialises the ArrayList and loads the corresponding files.
private static void initLists() throws Exception
{
	itemsBlacklist	= new ArrayList<Integer>();
	ops				= new ArrayList<String>();

	Scanner sc = null;

	sc = new Scanner(new File(FILE_BLACKLIST));
	while(sc.hasNextInt()) {
		itemsBlacklist.add(sc.nextInt());
	}

	sc = new Scanner(new File(FILE_OPS));
	while(sc.hasNextLine()) {
		ops.add(sc.nextLine());
	}
}


/// Updates the blacklist file with the contents of itemsBlacklist.
public static void writeItemsBlacklist()
{
	writeListToFile(itemsBlacklist, FILE_BLACKLIST);
}


/// Writes the onlinelist file.
public static void writeOnlinelist()
{
	if(config.getProperty("write-onlinelist", "false").equals("true"))
		writeListToFile(Wrapper.players, FILE_ONLINELIST);
}


/** Writes an ArrayList to a text file.
 * \param list list to write.
 * \param fileName name of the file in which the list will be written. */
private static void writeListToFile(final ArrayList<? extends Object> list, final String fileName)
{
	try {
		final FileWriter f = new FileWriter(fileName);
		String contents = "";
		for(Object o : list) {
			contents += o.toString() + "\n";
		}
		f.write(contents);
		f.flush();
		f.close();

	} catch (Exception e) {
		System.err.println("[WARNING] Could not write to " + fileName);
		e.printStackTrace();
	}

}


/// Initialises the class.
public static void init()
{
	try {
		checkAndCreateFiles();
		initProperties();
		initLists();
	} catch(Exception e) {
		e.printStackTrace();
		System.exit(3);
	}
}

}

