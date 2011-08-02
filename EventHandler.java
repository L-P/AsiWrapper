/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

import java.io.File;

/// Handles the game events.
public class EventHandler {

/** React to an event.
 * \param e event de handle. */
public static void handle(final Event e)
{
	if(!e.isValid())
		return;

	switch(e.getType()) {
	case JOIN:
		handleJoin(e);
		break;
	case QUIT:
		handleQuit(e);
		break;
	case GAMECMD:
		handleGameCommand(e);
		break;
	case CHATCMD:
		handleChatCommand(e);
		break;
	case OP:
		handleOp(e);
		break;
	case DEOP:
		handleDeop(e);
	case EXCEPTION:
		handleException();
	default:
		break;
	}
}


/// Save and restart the server when it crashes.
private static void handleException()
{
	Cmd.raw("save-all");
	Cmd.raw("stop");
}

/** What to do when someone becomes op.
 * \param e event. */
private static void handleOp(final Event e)
{
	if(isOp(e.getNick()))
		return;

	Conf.ops.add(e.getNick().toLowerCase());
}


/** What to do when someone is no longer an op.
 * \param e event. */
private static void handleDeop(final Event e)
{
	if(isOp(e.getNick())) {
		Conf.ops.remove(e.getNick().toLowerCase());
	}
}


/** Tells if a player is Op.
 * \param nick name of the player to check.
 * \return true is the player is an Op. */
private static boolean isOp(final String nick)
{
	return Conf.ops.indexOf(nick.toLowerCase()) != -1;
}


/** Removes a player from the world, initially a workaround for the buggy /kill command.
 * \param nick name of the player to fix. */
private static void resetPlayer(final String nick)
{
	if(!Conf.config.getProperty("enable-reset", "true").equals("true")) {
		Cmd.sayToPlayer(nick, "reset is disabled.");
		return;
	}

	final File f = new File(Conf.serverConfig.getProperty("level-name") + "/players/" + nick + ".dat");
	final long startTime = Util.getTime();
	final long mtime = f.lastModified();

	Cmd.kickPlayer(nick);

	// Wait for a maximum of two seconds, then destroy the file.
	while(mtime == f.lastModified() && (Util.getTime() - startTime) < 2000) {}
	f.delete();
}


/** Tells if the given name is the name of an existing kit.
 * \param s name to check.
 * \return true if s is the name of an existing kit. */
private static boolean isKit(final String s)
{
	return Conf.kits.getProperty(s) != null;
}


/** Get the ID of an item from its name.
 * \param s name of the item.
 * \return ID of the item or 0 if it does not exist. */
private static int getItemIdFromName(final String s)
{
	String res = Conf.items.getProperty(s);
	if(res == null)
		return 0;

	int ret = 0;
	try { ret = Integer.parseInt(res); } catch(Exception e) {}
	return ret;
}


/** Get the ID of an item from its name or from its numeric ID as a string.
 * \param s item name (or ID).
 * \return the item ID or 0 if the given name/ID does not exist. */
private static int getItemIdFromString(final String s)
{
	int ret = 0;

	if(s.matches("^\\p{Digit}+$"))
		try { ret = Integer.parseInt(s); } catch (Exception e) {}
	else if(s.matches("^\\w+$"))
		ret = getItemIdFromName(s);

	return ret;
}


/** Gives a kit to a player.
 * \param nick name of the player to whom the kit will be given.
 * \param kit name of the kit to give. */
private static void giveKit(final String nick, final String kit)
{
	if(!Conf.config.getProperty("enable-kits", "true").equals("true")) {
		Cmd.sayToPlayer(nick, "kits are disabled.");
		return;
	}

	final String[] items = Conf.kits.getProperty(kit).split(" ");
	if(items.length%2 != 0)
		return;

	for(int i=0; i<items.length; i+=2) {
		final int id = getItemIdFromString(items[i]);
		int count = 0;
		try { count = Integer.parseInt(items[i+1]); } catch (Exception e) {}
		if(id != 0)
			Cmd.giveToPlayer(nick, id, count);
		else {
			Cmd.sayToPlayer(nick, "Invalid kit! Tell the admins about it.");
			return;
		}
	}
}


/** Handles a !geta command issued by a player.
 * \param nick name of the player who issued the command.
 * \param args arguments given with that command. */
private static void handleGetaCommand(final String nick, final String args)
{
	final String[] parts = args.split(" ", 2); // In case the player pavlovly let a number in.
	handleGetCommand(nick, parts[0] + " 1");
}


/** Checks if an item is in the blacklist.
 * \param id ID of the item to check.
 * \return true if the given item ID is in the blacklist. */
private static boolean isItemBlacklisted(final int id)
{
	return Conf.itemsBlacklist.indexOf(id) != -1;
}


/** Handles a !get command issued by a player.
 * \param nick name of the player who issued the command.
 * \param args arguments given with that command. */
private static void handleGetCommand(final String nick, final String args)
{
	if(!Conf.config.getProperty("enable-get", "true").equals("true")) {
		Cmd.sayToPlayer(nick, "get is disabled.");
		return;
	}

	int count = 64;
	int id = 0;
	final String[] parts = args.split(" ", 2);
	if(parts.length == 2)  {
		int tmp = 0;
		try { tmp = Integer.parseInt(parts[1]); } catch(Exception e) {}
		if(tmp != 0)
			count = tmp;
		else
			Cmd.sayToPlayer(nick, parts[1] + " is an invalid count.");
	}

	if(isKit(parts[0])) {
		giveKit(nick, parts[0]);
		return;
	} else 
		id = getItemIdFromString(parts[0]);

	if(Conf.config.getProperty("enable-blacklist", "true").equals("true")) {
		if(isItemBlacklisted(id) && !isOp(nick)) {
			Cmd.sayToPlayer(nick, "you are not allowed to get this item.");
			return;
		}
	}

	if(id != 0)
		Cmd.giveToPlayer(nick, id, count);
	else
		Cmd.sayToPlayer(nick, "unknown item.");
}


/** Teleports a player who used the !goto command.
 * \param nick name of the player to teleport.
 * \param dest name of the destination. */
private static void teleportPlayer(final String nick, final String dest)
{
	if(!Conf.config.getProperty("enable-goto", "true").equals("true")) {
		Cmd.sayToPlayer(nick, "goto is disabled.");
		return;
	}

	Cmd.teleportPlayer(nick, dest);
}


/** Outputs the online players to another player.
 * \param nick name of the player who requested the list. */
private static void outputListToPlayer(final String nick)
{
	String list = "";
	for(String s : Wrapper.players) {
		list += s + ", ";
	}

	list = list.substring(0, list.length()-2); // Get rid of the final ", ".
	Cmd.sayToPlayer(nick, list);
}


/** Show the MOTD to a specific player.
 * \param nick name of the player who wants the MOTD */
private static void showMotdToPlayer(final String nick)
{
	final String motd = Conf.config.getProperty("motd", "");
	if(!Conf.config.getProperty("enable-motd", "false").equals("true") || motd.isEmpty())
		return;

	Cmd.sayToPlayer(nick, motd);
}


/** Handles a chat command (!cmd) issued by a player.
 * \param e the command Event. */
private static void handleChatCommand(final Event e)
{
	if(e.getCommand().equals("goto") && e.getArgs() != null)
		teleportPlayer(e.getNick(), e.getArgs());
	else if(e.getCommand().equals("get") && e.getArgs() != null)
		handleGetCommand(e.getNick(), e.getArgs());
	else if(e.getCommand().equals("geta") && e.getArgs() != null)
		handleGetaCommand(e.getNick(), e.getArgs());
	else if(e.getCommand().equals("motd") && e.getArgs() == null)
		showMotdToPlayer(e.getNick());
	else if(e.getCommand().equals("list") && e.getArgs() == null) {
		if(Conf.config.getProperty("enable-list", "true").equals("true"))
			outputListToPlayer(e.getNick());
		else
			Cmd.sayToPlayer(e.getNick(), "list is disabled.");
	} else 
		Cmd.sayToPlayer(e.getNick(), "bad command.");
}


/** Handles a game command issued by a player.
 * \param e the command Event. */
private static void handleGameCommand(final Event e)
{
	handleGameCommand(e, false);
}


/** Removes an item from the blacklist.
 * \param s name/ID of the item to remove. */
private static void removeItemFromBlacklist(final String s)
{
	final int id = getItemIdFromString(s);
	if(id == 0)
		return;

	final int i = Conf.itemsBlacklist.indexOf(id);
	if(i == -1)
		return;

	Conf.itemsBlacklist.remove(i);
	Conf.writeItemsBlacklist();
	Cmd.sayToAll("item " + s + " removed from the blacklist.");
}


/** Adds an item tp the blacklist.
 * \param s name/ID of the item to add. */
private static void addItemToBlacklist(final String s)
{
	final int id = getItemIdFromString(s);
	if(id == 0) 
		return;

	final int i = Conf.itemsBlacklist.indexOf(id);
	if(i != -1)
		return;

	Conf.itemsBlacklist.add(id);
	Conf.writeItemsBlacklist();
	Cmd.sayToAll("item " + s + " added to the blacklist.");
}


/// Backups the world in a single file.
private static void backupWorld()
{
	if(!Conf.config.getProperty("enable-backup", "true").equals("true"))
		return;

	Util.backupWorld();
}


/** Handles a game command (/cmd) issued by a player or the wrapper.
 * \param e the command Event.
 * \param bypass true if the player issued the command, false otherwise. */
public static void handleGameCommand(final Event e, final boolean bypass)
{
	// Only ops can issue server commands (those that starts with '/').
	if(!bypass && !isOp(e.getNick()))
		return;

	if(e.getCommand().equals("backup") && e.getArgs() == null)
		backupWorld();

	// All the commands below requires it.
	if(e.getArgs() == null)
		return;

	if(e.getCommand().equals("allowitem"))
		removeItemFromBlacklist(e.getArgs());
	else if(e.getCommand().equals("disallowitem"))
		addItemToBlacklist(e.getArgs());
	else if(e.getCommand().equals("reset"))
		resetPlayer(e.getArgs());
}


/** Greets the player so he knows whether he is alone or not.
 * \param nick name of the player to greet. */
private static void greetPlayer(final String nick)
{
	if(Conf.config.getProperty("enable-greeting", "true").equals("true")) {
		if(Wrapper.players.size() <= 1)
			Cmd.sayToPlayer(nick, "hello " + nick + ", you are alone.");
		else
			Cmd.sayToPlayer(nick, "hello " + nick + ", you are not alone.");
	}
}


/** What to do when someone joins the server.
 * \param e JOIN Event. */
private static void handleJoin(final Event e)
{
	if(Conf.config.getProperty("enable-login", "false").equals("true")) {
		if(!checkInternalLogin(e.getArgs(), e.getNick())) {
			Cmd.kickPlayer(e.getNick());
			return;
		}
	}

	Wrapper.players.add(e.getNick());
	Conf.writeOnlinelist();

	greetPlayer(e.getNick());
	showMotdToPlayer(e.getNick());
}


/** Checks if an IP is allowed to join the server.
 * \param ip IP to check.
 * \param nick User to check against the IP.
 * \return true if the IP is allowed, false otherwise. */
private static boolean checkInternalLogin(final String ip, final String nick)
{
	final String url = Conf.config.getProperty("login-url", "localhost/")
						+ "?check=" + ip + "&user=" + nick;

	System.out.println("URL : " + url);
	return Util.fetchDataFromUrl(url).equals("true");
}


/** What to do when someone quits the server.
 * \param e QUIT Event. */
private static void handleQuit(final Event e)
{
	Wrapper.players.remove(e.getNick());
	Conf.writeOnlinelist();
}


}

