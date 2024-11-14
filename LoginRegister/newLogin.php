<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "phone app db";

$conn = mysqli_connect($servername, $username, $password, $dbname);

if (!$conn) {
    die ("Connection failed");
}

$user = $_POST['username'];
$pass = $_POST['password'];

$sql = "SELECT * FROM users WHERE username='$user' AND password='$pass'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    echo "success";
} 
else {
    echo "fail";
}
?>