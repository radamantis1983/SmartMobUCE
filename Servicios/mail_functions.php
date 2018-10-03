<?php
function enviarEmail($email, $asunto, $cuerpo){
		
		require_once 'PHPMailer/PHPMailerAutoload.php';
		
		$mail = new PHPMailer();
		$mail->isSMTP();
		$mail->SMTPAuth = true;
		$mail->SMTPSecure = 'tls';
		$mail->Host = 'smtp.dominio.com';
		$mail->Port = '587';
		
		$mail->Username = 'nombre@dominio.com';
		$mail->Password = 'clave';
		
		$mail->setFrom('nombre@dominio.com', 'Sistema de Usuarios');
		$mail->addAddress($email);
		
		$mail->Subject = $asunto;
		$mail->Body    = $cuerpo;
		$mail->IsHTML(true);
		
		if($mail->send())
		return 1;
		else
		return 0;
		
	}
	
	function generateToken()
	    {
		    $gen = md5(uniqid(mt_rand(), false));	
		    return $gen;
	    }
?>