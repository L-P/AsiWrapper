/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

/// A Cmd represents a command issued by a player.
public class Cmd {

/** Sends a raw command to the server.
 * \param command the command that will be sent to the server. */
public static void raw(final String command)
{
	Wrapper.servin.println(command);
}


/** "Whispers" something to the player, only he will hear it.
 * \param player the player name.
 * \param message the line to say. */
public static void sayToPlayer(final String player, final String message)
{
	raw("tell " + player + " " + message);
}


/** Say something to all players.
 * \param message the line to say. */
public static void sayToAll(final String message)
{
	raw("say " + message);
}


/** Teleports a player to another one.
 * \param player the name of the player to teleport.
 * \param to the name of the "destination". */
public static void teleportPlayer(final String player, final String to)
{
	raw("tp " + player + " " + to);
}


/** Gives an item to a player.
 * \param player the name of the player.
 * \param itemId the ID of the item to give, see Minepedia Data Values.
 * \param count how many items will be given, between 1 and 512. */
public static void giveToPlayer(final String player, final int itemId, int count)
{
	count = Math.max(1, Math.min(512, count));
	if(count >= 64) {
		for(int i = 0; i < count/64; i++) {
		raw("give " + player + " " + itemId + " 64");
		}
	}
	if(count%64 != 0)
	raw("give " + player + " " + itemId + " " + (count%64));
}


/** Kicks a player from the server.
 * \param player name of the player to kick. */
public static void kickPlayer(final String player)
{
	raw("kick " + player);
}


}

