<?php
include_once './db_connectgps.php';
class User {
		
		private $db;
		
		function __construct() {
			// connecting to database
			$this->db = new DB_Connect();
        	$this->db->connect();
		}
		
		public function does_user_exist($usu_email,$usu_password,$usu_year,$usu_genero,$usu_facultad,$usu_tipo,$usu_sector,$usu_actividades)
		{
			$query = "Select * from usuarios where usu_email='$usu_email'";
			$result = mysql_query($query);
			if(mysql_num_rows($result)>0){
				$json['error'] = 'usuario ya registrado ';
				echo json_encode($json);
				mysql_close($this->db->connect());
			}else{
				$query = "insert into usuarios (usu_email,usu_password,usu_year,usu_genero,usu_facultad,usu_tipo,usu_sector,usu_actividades) VALUES ('$usu_email','$usu_password',$usu_year,$usu_genero,'$usu_facultad',$usu_tipo,$usu_sector,'$usu_actividades')";
				$inserted = mysql_query($query);
				if($inserted == 1 ){
					$json['success'] = 'Cuenta creada';
					echo json_encode($json);
				}else{
					$json['error'] = 'Error Registro';
				echo json_encode($json);
				}
				
				mysql_close($this->db->connect());
			}
			
		}
		
	}
	
	
$user = new User();
if(isset($_POST['usu_email'],$_POST['usu_password'],$_POST['usu_year'],$_POST['usu_genero'],$_POST['usu_facultad'],$_POST['usu_tipo'],$_POST['usu_sector'],$_POST['usu_actividades'])){
  
  $usu_email=$_POST['usu_email'];
  $usu_password=$_POST['usu_password'];
  $usu_year=$_POST['usu_year'];
  $usu_genero=$_POST['usu_genero'];
  $usu_facultad=$_POST['usu_facultad'];
  $usu_tipo=$_POST['usu_tipo'];
  $usu_sector=$_POST['usu_sector'];
  $usu_actividades=$_POST['usu_actividades'];
  
  
  if(!empty($usu_email) && !empty($usu_password) && !empty($usu_year) && !empty($usu_genero) && !empty($usu_facultad) && !empty($usu_tipo) && !empty($usu_sector) && !empty($usu_actividades)){
        $encrypted_password = md5($usu_password);
        $user-> does_user_exist($usu_email,$encrypted_password,$usu_year,$usu_genero,$usu_facultad,$usu_tipo,$usu_sector,$usu_actividades);
  }else{
        $json['error'] = 'llenar todos los campos';
			echo json_encode($json);
		}

 }else
 {
      $json['error'] = 'No service'; 
      echo json_encode($json);
   }


?>  