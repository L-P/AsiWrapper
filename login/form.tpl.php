<?php
/* AsiWrapper by Léo Peltier
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */ 

if(!defined('IN_INDEX'))
	exit('Y U NO OPEN THE RIGHT FILE ?');

?>
<!doctype html>
<html>
	<head>
		<meta charset="UTF-8" />
		<title>AsiWrapper Login</title>
		<style>
			h1 { text-align: center; }
			form { width: 300px; margin: auto; }
			label { float: left; display: block; width: 100px; padding-top: 2px; padding-bottom: 2px; }
			#submit { display:block; margin:auto; text-align: center; margin-top: 15px;}
			#errormsg { color: red; }
			#loggedinmsg { color: green; }
			p { text-align: center; }
		</style>
	</head>
	<body>
		<h1>AsiWrapper Login</h1>
		<form action="" method="POST">
			<label for="nick">Nick&nbsp;:</label>
			<select name="nick" id="nick">
				<?php foreach($logins as $k => $v) { echo '<option value="', $k,'">', $k, '</option>'; } ?>
			</select>
			<br /><label for="pass">Pass&nbsp;:</label>
			<input type="password" name="pass" id="pass" size="8" /><br />
			<?php
				if($badInput) echo '<p id="errormsg">Bad input, check your password.</p>';
				if($loggedIn) echo '<p id="loggedinmsg">You can now join the server.</p>';
			?>
			<input type="submit" value="Log in" id="submit" />
		</form>
	</body>
</html>

