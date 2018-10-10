<?php

	require_once 'config.php';
	
	class DB_Connection {
		
		private $connect;
		function __construct() {
			$this->connect = mysqli_connect(db_host, db_user, db_password, db_database)
			or die("Could not connect to db");
			
		}
		
		public function getConnection()
		{
			return $this->connect;
		}
	}
	$connection = @mysqli_connect(db_host,db_user,db_password,db_database);

?>