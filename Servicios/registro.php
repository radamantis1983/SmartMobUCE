<?php
include_once 'connection.php';
class User {
		
		private $db;
		private $connection;
		
		function __construct() {
			$this -> db = new DB_Connection();
			$this -> connection = $this->db->getConnection();
		}
		
		public function does_user_exist($usu_email,$usu_password,$usu_year,$usu_genero,$usu_facultad,$usu_tipo,$usu_sector,$usu_actividades,$usu_marca,$usu_modelo,$usu_version_android)
		{
			$query = "Select * from usuarios where usu_email='$usu_email'";
			$result = mysqli_query($this->connection, $query);
			require 'mail_functions.php';
			if(mysqli_num_rows($result)>0){
				$json['error1'] = 'usuario ya registrado ';
				echo json_encode($json);
				mysqli_close($this->connection);
			}else{
				$usu_key = generateToken();
			    $usu_estado=0;
				$query = "insert into usuarios (usu_email,usu_password,usu_year,usu_genero,usu_facultad,usu_tipo,usu_sector,usu_actividades,usu_marca,usu_modelo,usu_version_android,usu_key,usu_estado) VALUES ('$usu_email','$usu_password',$usu_year,$usu_genero,'$usu_facultad',$usu_tipo,$usu_sector,'$usu_actividades','$usu_marca','$usu_modelo','$usu_version_android','$usu_key',$usu_estado)";
				
				$inserted = mysqli_query($this -> connection, $query);
				if($inserted == 1 ){
					
				$url='http://gmoncayoresearch.com/smartmobuce1/activacion.php?id='.$usu_email.'&val='.$usu_key;
					$asunto='Activacion de cuenta';
					$mensaje="Estimado $usu_email acaba de registrar en la app smartmobuce<br /><br />
                    Debes activar tu cuenta pulsando este enlace:<a href='$url'>Activar cuenta</a>";
					
					if(enviarEmail($usu_email,$asunto,$mensaje) == 1){
						
					
					}
					$json['success'] = 'Cuenta creada';
					echo json_encode($json);
					mysqli_close($this->connection);
					
				}else{
					$json['error2'] = 'Error Registro';
					echo json_encode($json);
				}
				
				mysqli_close($this->connection);
				
			}
			
		}
		
	}
	
	
$user = new User();
if(isset($_POST['usu_email'],$_POST['usu_password'],$_POST['usu_year'],$_POST['usu_genero'],$_POST['usu_facultad'],$_POST['usu_tipo'],$_POST['usu_sector'],$_POST['usu_actividades'],$_POST['usu_marca'],$_POST['usu_modelo'],$_POST['usu_version_android'])){
  
  $usu_email=$_POST['usu_email'];
  $usu_password=$_POST['usu_password'];
  $usu_year=$_POST['usu_year'];
  $usu_genero=$_POST['usu_genero'];
  $usu_facultad=$_POST['usu_facultad'];
  $usu_tipo=$_POST['usu_tipo'];
  $usu_sector=$_POST['usu_sector'];
  $usu_actividades=$_POST['usu_actividades'];
  $usu_marca=$_POST['usu_marca'];
  $usu_modelo=$_POST['usu_modelo'];
  $usu_version_android=$_POST['usu_version_android'];
  
  if(!empty($usu_email) && !empty($usu_password) && !empty($usu_year) && !empty($usu_genero) && !empty($usu_facultad) && !empty($usu_tipo) && !empty($usu_sector) && !empty($usu_actividades)){
        $encrypted_password = md5($usu_password);
        $user-> does_user_exist($usu_email,$encrypted_password,$usu_year,$usu_genero,$usu_facultad,$usu_tipo,$usu_sector,$usu_actividades,$usu_marca,$usu_modelo,$usu_version_android);
  }else{
        $json['error3'] = 'llenar todos los campos';
			echo json_encode($json);
		}

 }else
 {
      $json['error4'] = 'No service'; 
      echo json_encode($json);
   }


?>  