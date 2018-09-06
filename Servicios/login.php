<?php
include_once './db_connectgps.php';
class User {
		
		private $db;
		
		function __construct() {
		    
			// connecting to database
			$this->db = new DB_Connect();
        	//$this -> connection = $this->db->connect();
        	$this->db->connect();
		}
		
		public function does_user_exist($usu_email,$usu_password)
		{
			$query = "Select usu_id from usuarios where usu_email='$usu_email'and usu_password = '$usu_password'";
			$result = mysql_query($query);
			if(mysql_num_rows($result)>0){
			 $registro=mysql_fetch_array($result);
		     $json['usuarios'][]=$registro;
	        
			}	
			else{
				$json['error'] = 'Wrong password o usuario no registrado';
			}
			echo json_encode($json);
			mysql_close($this->db->connect());
			
		}
		
	}
	
	
$user = new User();
if(isset($_POST['usu_email'],$_POST['usu_password'])){
  
  $usu_email=$_POST['usu_email'];
  $usu_password=$_POST['usu_password'];
  
  if(!empty($usu_email) && !empty($usu_password)){
        $encrypted_password = md5($usu_password);
        $user-> does_user_exist($usu_email,$encrypted_password);
  }else{
			echo json_encode("llenar todos los campos");
		}

 }else
 {
      $json['error'] = 'No service'; 
      echo json_encode($json);
   }

?>  