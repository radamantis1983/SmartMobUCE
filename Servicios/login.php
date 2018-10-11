<?php
include_once 'connection.php';
class User {
		
		private $db;
		private $connection;
		
		function __construct() {
			$this -> db = new DB_Connection();
			$this -> connection = $this->db->getConnection();
		}
		
		public function does_user_exist($usu_email,$usu_password)
		{
			$query = "Select usu_id from usuarios where usu_email='$usu_email'and usu_password = '$usu_password' and usu_estado=1";
			$result = mysqli_query($this->connection, $query);
			if(mysqli_num_rows($result)>0){
			 $registro=mysqli_fetch_array($result);
		     $json['usuarios'][]=$registro;
	        
			}	
			else{
				$json['error'] = 'Wrong password o usuario no registrado o cuenta no activada';
			}
			echo json_encode($json);
			mysqli_close($this -> connection);
			
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