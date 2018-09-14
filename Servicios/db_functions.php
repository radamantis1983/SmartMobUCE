<?php
ini_set('date.timezone','America/Guayaquil');

class DB_Functions {
   
    private $db;
   

    //put your code here
    // constructor
    function __construct() {
        include_once './db_connectgps.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }

    // destructor
    function __destruct() {
        
    }

    /**
     * Storing gps datos
     * returns datos details
     */
    
    public function storeusuario($dat_id,$usu_id,$dat_latitud,$dat_longitud,$dat_precision,$dat_altitud,$dat_velocidad,$dat_proveedor,$dat_fechahora_lectura,$dat_marca,$dat_modelo,$dat_version) {
         $dat_fechahora_sync=date("Y-m-d H:i:s");
        // Insert datos into database
        $result = mysql_query("INSERT INTO datosgps (usu_id,dat_latitud,dat_longitud,dat_precision,dat_altitud,dat_velocidad,dat_proveedor,dat_fechahora_lectura,dat_marca,dat_modelo,dat_version,dat_fechahora_sync) 
        VALUES ($usu_id,$dat_latitud,$dat_longitud,$dat_precision,$dat_altitud,$dat_velocidad,'$dat_proveedor','$dat_fechahora_lectura','$dat_marca','$dat_modelo','$dat_version','$dat_fechahora_sync')");
	
        if ($result) {
			return true;
        } else {
			if( mysql_errno() == 1062) {
				// Duplicate key - Primary Key Violation
				return true;
			} else {
				// For other errors
				return false;
			}            
        }
    }
	 /**
     * Getting all datos
     */
    public function getAllUsers() {
        $result = mysql_query("select * FROM datosgps");
        return $result;
    }
}

?>