<?php
/* AsiWrapper by Léo Peltier
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */ 

if(!defined('IN_INDEX'))
	exit('Y U NO OPEN THE RIGHT FILE ?');


function writeIpsToFile($ips)
{
	$s = serialize($ips);
	$serializedIps = '$serializedIps'; // Yes, this is fucked-up.
	$contents = <<<EOF
<?php
/* AsiWrapper by Léo Peltier
 * As long as you retain this notice you can do whatever you want whis this stuff.
 * If we meet some day, and you think this stuff is worth it, you can buy me a beer in return. */ 

if(!defined('IN_INDEX'))
	exit('Y U NO OPEN THE RIGHT FILE ?');

$serializedIps = '$s';
EOF;

	file_put_contents('ips.inc.php', $contents);
}

function pruneIps($ips)
{
	if(!is_array($ips))
		return array();

	$prunedIps = array();
	$curtime = time();
	foreach($ips as $ip => $v) {
		if($v['time'] + 120 > $curtime) 
			$prunedIps[$ip] = $v;
	}
	return $prunedIps;
}

function checkIp($ip, $user, $ips)
{
	if(!array_key_exists($ip, $ips))
		return 'false';

	if($user != $ips[$ip]['nick'])
		return 'false';

	return 'true';
}

function checkLogin($nick, $pass)
{
	global $logins;

	if(!array_key_exists($nick, $logins))
		return false;

	return hash('sha256', $pass) == $logins[$nick];
}

