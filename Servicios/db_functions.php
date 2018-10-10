<?php
ini_set('date.timezone','America/Guayaquil');

class DB_Functions {
   
    private $db;
   private $connection;

    function __construct() {
		include_once 'connection.php';
		$this -> db = new DB_Connection();
		$this -> connection = $this->db->getConnection();
    }

  
    /**
     * Storing gps user
     * returns user details
     */
    
    public function storegps($dat_id,$usu_id,$dat_latitud,$dat_longitud,$dat_precision,$dat_altitud,$dat_velocidad,$dat_proveedor,$dat_fechahora_lectura) {
         $dat_fechahora_sync=date("Y-m-d H:i:s");
        // Insert user into database
		$query ="INSERT INTO datosgps (usu_id,dat_latitud,dat_longitud,dat_precision,dat_altitud,dat_velocidad,dat_proveedor,dat_fechahora_lectura,dat_fechahora_sync) VALUES 
        ($usu_id,$dat_latitud,$dat_longitud,$dat_precision,$dat_altitud,$dat_velocidad,'$dat_proveedor','$dat_fechahora_lectura','$dat_fechahora_sync')";
        $result = mysqli_query($this->connection, $query);
	
        if ($result) {
			return true;
        } else {
			if( mysqli_errno($this->connection) == 1062) {
				// Duplicate key - Primary Key Violation
				return true;
			} else {
				// For other errors
				return false;
			}            
        }
    }

}

?>
