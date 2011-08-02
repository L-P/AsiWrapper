/* AsiWrapper by Léo Peltier <contact@leo-peltier.fr>
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/// Program entry Class.
public class Main {

/** Program entry point.
 * \param args Arguments (unused). */
public static void main(String[] args)
{
	Conf.init();

	boolean nicequit = false;
	while(!nicequit) {
		try {
			Wrapper serv = new Wrapper();
			serv.start();

			while(!serv.hasStopped()) {
				try {
					serv.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			nicequit = serv.wasNiceQuit();
			if(!nicequit) {
				System.err.println("[WARNING] The server crashed or was stopped from whithin the game. Restarting…");
			}
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	System.exit(0);
}


}

