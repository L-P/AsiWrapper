<?php
/* AsiWrapper by Léo Peltier
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */ 

error_reporting(-1);
define('IN_INDEX', true);

require_once 'funcs.inc.php';
require_once 'logins.inc.php';
require_once 'ips.inc.php';
$ips = unserialize($serializedIps);

$badInput = false;
$loggedIn = false;

$ips = pruneIps($ips);

if(!empty($_GET['check']) AND !empty($_GET['user'])) {
	echo checkIp($_GET['check'], $_GET['user'], $ips);
} else {
	if (!empty($_POST['nick']) OR !empty($_POST['pass'])) {
		if (!empty($_POST['nick']) AND !empty($_POST['pass'])) {
			if(checkLogin($_POST['nick'], $_POST['pass'])) {
				$loggedIn = true;
				$ips[$_SERVER['REMOTE_ADDR']] = array('nick' => $_POST['nick'], 'time' => time());
			}
			$badInput = !$loggedIn;
		} else {
			$badInput = true;
		}
	}
	require_once 'form.tpl.php';
}

writeIpsToFile($ips);

