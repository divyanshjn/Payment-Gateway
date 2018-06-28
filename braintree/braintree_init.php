<?php
session_start();
require_once ('lib/autoload.php');
require_once ('lib/Braintree.php');
if (file_exists(__DIR__. "/../.env")) {
	$dotenv=new Dotenv\Dotenv(__DIR__. "/../");
	$dotenv->load();

}   
Braintree_Configuration::environment('sandbox');
Braintree_Configuration::merchantId('zwhkxm8wdcdhx65w');
Braintree_Configuration::publicKey('jxgkdjns224dgqjg');
Braintree_Configuration::privateKey('983549a0710455230ecbeb9940f28b6d');
?>