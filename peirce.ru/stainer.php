<?php
//$file_path = '/tmp/dirty_regions';
$file_path = 'qa_request_log.txt';

function do_dirty() {
	global $file_path;

	$timestamp = time();
	$timestr = strftime("%F %H:%M:%S");
	$code = filter_input(INPUT_GET, "code", FILTER_VALIDATE_REGEXP,
	                     array("options" => array("regexp" => "/^[\w\-]+$/")));
	if (empty($code))
		throw new Exception("'code' param is missed or wrong");

	$fd = fopen($file_path, "a");
	if (empty($fd))
		throw new Exception("Failed to open file for storing dirty-mark", 1);
	fwrite($fd, "$code\t$timestamp\t# $timestr\n");
	fclose($fd);

	echo json_encode(array("status" => "success"));
}


function do_list() {
	$list = array();
	$met = array();
	$last_timestamp = 0;
	global $file_path;

	$from = filter_input(INPUT_GET, "from", FILTER_VALIDATE_INT);
	if (empty($from))
		throw new Exception("'from' parametr is missed or wrong, use epoch seconds (integer)");

	if (!file_exists($file_path))
		throw new Exception("File is missed");


	$fd = fopen($file_path, "r");
	while (($line = fgets($fd, 1024)) != false) {
		list($code, $timestamp) = preg_split("/\t/", $line);
		$last_timestamp = $timestamp;
		if ($list < $from) continue;
		if (isset($met[$code])) continue;

		$list[] = $code;
		$met[$code] = true;
	}
	if (!feof($fd))
		throw new Exception("Something wrong happened during reading the file", 1);

	fclose($fd);

	echo json_encode(array("status" => "success",
	                       "codes" => $list,
	                       "last_timestamp" => $last_timestamp));
}



header("Content-type: application/json;");
try {
	$cmd = $_SERVER['PATH_INFO'];
	switch ($cmd) {
		case '/list':
			do_list();
			break;
		case '/dirty':
			do_dirty();
			break;
		default:
			throw new Exception("Unknown command $cmd", 1);
			break;
	}
} catch (Exception $e) {
	header('HTTP/1.1 500 Internal Server Error');
	echo json_encode(array("status" => "error", "message" => $e->getMessage()));
}
?>