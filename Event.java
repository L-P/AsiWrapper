/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

/// A class representing an event that occured on the server.
public class Event {

/// Represents the types of Event that can exist.
enum Type {
	CHATCMD, GAMECMD, JOIN, QUIT, OP, DEOP, EXCEPTION, INVALID;
}

// Quite ugly but \d would not work… It matches "0000-00-00 00:00:00" and expects it to be the beginning of the line.
private static final String REGEX_TIMESTAMP =	"^\\p{Digit}{4}(-\\p{Digit}{2}){2} \\p{Digit}{2}(:\\p{Digit}{2}){2}"; 

private static final String REGEX_CHATCOMMAND =	REGEX_TIMESTAMP + " \\[INFO\\] <\\w+> !(\\w+ ?)+$";
private static final String REGEX_GAMECOMMAND = REGEX_TIMESTAMP + " \\[INFO\\] \\w+ issued server command: (\\w+ ?)+$";
private static final String REGEX_JOINEVENT =	REGEX_TIMESTAMP + " \\[INFO\\] \\w+ \\[/[0-9.:]+\\] logged in with entity id \\p{Digit}+ at \\(.+\\)$";
private static final String REGEX_QUITEVENT =	REGEX_TIMESTAMP + " \\[INFO\\] \\w+ lost connection: .+$";
private static final String REGEX_KICKEVENT =	REGEX_TIMESTAMP + " \\[INFO\\] CONSOLE: Kicking \\w+$";
private static final String REGEX_OPEVENT =		REGEX_TIMESTAMP + " \\[INFO\\] CONSOLE: Opping \\w+$";
private static final String REGEX_DEOPEVENT =	REGEX_TIMESTAMP + " \\[INFO\\] CONSOLE: De-opping \\w+$";
private static final String REGEX_EXCEPTION =	REGEX_TIMESTAMP + " \\[SEVERE\\] Unexpected exception$";

private String sEvt; ///< Contains the Event string as it was written in the logs.
private Type type; ///< Type of the Event.

private String command; ///< Issued command if the Event is a command.
private String nick; ///< Name of the player that triggered the Event.
private String args; ///< Arguments of the issued command.

public String	getArgs()		{ return args; }		///< Accessor.
public String	getCommand()	{ return command; }		///< Accessor.
public String	getNick()		{ return nick; }		///< Accessor.
public Type		getType()		{ return type; }		///< Accessor.


/** Constructor.
 * \param s the event as it was in the logs. */
public Event(final String s)
{
	sEvt = s;
	type = Type.INVALID;

	parseEvent();
}


/// Parses the event from sEvt.
private void parseEvent()
{
	if(sEvt.matches(REGEX_CHATCOMMAND))
		parseAsChatCommand();
	else if(sEvt.matches(REGEX_GAMECOMMAND))
		parseAsGameCommand();
	else if(sEvt.matches(REGEX_JOINEVENT) || sEvt.matches(REGEX_QUITEVENT))
		parseAsConnectionEvent();
	else if(sEvt.matches(REGEX_KICKEVENT))
		parseAsKickEvent();
	else if(sEvt.matches(REGEX_OPEVENT) || sEvt.matches(REGEX_DEOPEVENT))
		parseAsOppingEvent();
	else if(sEvt.matches(REGEX_EXCEPTION))
			type = Type.EXCEPTION;
}


/// Parse the event as an (de)opping event.
private void parseAsOppingEvent()
{
	// 2010-12-12 22:31:01 [INFO] CONSOLE: Opping machin
	// 2010-12-12 22:31:38 [INFO] CONSOLE: De-opping machin

	final String[] parts = sEvt.split(" ", 6);
	if(parts.length != 6)
		return;

	nick = parts[5];
	if(sEvt.contains("De-opping"))
		type = Type.DEOP;
	else
		type = Type.OP;

}


/// Used only when debugging.
@Override public String toString()
{
	String ret = null;
	switch(type) {
	case CHATCMD:
		ret = "ChatCommand from " + nick + " : " + command + " -- " + args;
		break;
	case GAMECMD:
		ret = "GameCommand from " + nick + " : " + command + " -- " + args;
		break;
	case JOIN:
		ret = "JoinEvent : " + nick + " -- " + args;
		break;
	case QUIT:
		ret = "QuitEvent : " + nick;
		break;
	default:
		ret = "Invalid Event";
	}

	return ret;
}


/** Parses the event as a kick event, this event will be a Type.QUIT.
 * We need this event because the server logs only the kick, not the actual disconnection. */
private void parseAsKickEvent()
{
	// 2011-01-17 19:11:38 [INFO] CONSOLE: Kicking Notch

	final String[] parts = sEvt.split(" ", 6);
	if(parts.length < 6)
		return;

	nick = parts[5];
	type = Type.QUIT;
}


/// Parses the event as a (de)connection event (ie someone has join/quit).
private void parseAsConnectionEvent()
{
	// 2010-12-11 16:34:41 [INFO] Asibasth lost connection: Quitting
	// 2010-12-11 16:21:08 [INFO] Asibasth [/127.0.0.1:40632] logged in with entity id 18 at (0, 0, 0)

	final String[] parts = sEvt.split(" ",  6);
	if(parts.length < 6)
		return;

	nick = parts[3];

	if(sEvt.contains("logged in")) {
		type = Type.JOIN;
		final int colonPos = parts[4].indexOf(':');
		args = parts[4].substring(2, colonPos);
	} else {
		type = Type.QUIT;
	}
}


/// Parses the event as a command issued by an OP.
private void parseAsGameCommand()
{
	// 2010-12-11 15:22:38 [INFO] Asibasth issued server command: blahcmd withargs

	final String[] parts = sEvt.split(" ", 9); // 0-date ; 1-time ; 2-loglevel ; 3-nick ; 4,5,6-junk ; 7-command ; 8-args
	if(parts.length < 8)
		return;

	if(parts.length == 9)
		args = parts[8];

	nick = parts[3];
	command = parts[7];

	type = Type.GAMECMD;
}


/// Parses the event as a command issued by a player.
private void parseAsChatCommand()
{
	// 2010-12-11 15:18:32 [INFO] <Asibasth> !blahcmd withargs

	final String[] parts = sEvt.split(" ", 6); // 0-date ; 1-time ; 2-loglevel ; 3-nick ; 4-command ; 5-args
	if(parts.length < 5)
		return;

	if(parts.length == 6)
		args = parts[5];

	if(parts[3].length() < 3) 
		return; // Nick too short.

	nick = parts[3].substring(1, parts[3].length()-1); // Get rid of the '<>'
	command = parts[4].substring(1); // Get rid of the '!'

	type = Type.CHATCMD;
}


/** Tells if this instance of Event is a valid event.
 * \return true if the event is valid, false otherwise. */
public boolean isValid()
{
	return type != Type.INVALID;
}


}

