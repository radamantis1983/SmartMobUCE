<?php

include 'connection.php';
$msg='';
if(isset($_GET['id']) AND isset($_GET['val']))
{
    	$usu_email = $_GET['id'];
		$usu_key = $_GET['val'];

    $c=mysqli_query($connection,"SELECT usu_estado FROM usuarios WHERE usu_email='$usu_email'and usu_key='$usu_key'");

            if(mysqli_num_rows($c) > 0)
            {
                $count=mysqli_query($connection,"SELECT usu_id FROM usuarios WHERE usu_key='$usu_key' and usu_estado='0'");

            if(mysqli_num_rows($count) == 1)
            {
            mysqli_query($connection,"UPDATE usuarios SET usu_estado='1' WHERE usu_email='$usu_email'and usu_key='$usu_key'");
                $msg="Your account is activated"; 
            }
            else
            {
                $msg ="Your account is already active, no need to activate again";
            }

                }
                else
                {
                $msg ="Wrong activation code.";
                }
     
        }


?>

<html>
	<head>
		<title>Registro</title>
	
	</head>
	
	<body>
		
				
				<h1><?php echo $msg; ?></h1>
				
		
	</body>
</html>