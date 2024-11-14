<?php

function prepareData($data,$conn)
{
    return mysqli_real_escape_string($conn, stripslashes(htmlspecialchars($data)));
}


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
$email = $_POST['email'];

if(isset($_POST['email']) && isset($_POST['username']) && isset($_POST['password'])){
    $user_p = prepareData($user, $conn);
    $pass_p = prepareData($pass, $conn);
    $email_p = prepareData($email, $conn);

    //check if username exists
    $checkUserqry = "SELECT * FROM users WHERE username = '$user_p'";
    $checkUserResult = mysqli_query($conn, $checkUserqry);

    //check if email exists
    $checkEmailqry = "SELECT * FROM users WHERE email = '$email_p'";
    $checkEmailResult = mysqli_query($conn, $checkEmailqry);


    if (mysqli_num_rows($checkUserResult) > 0) {
        echo "user_exists";
    }

    elseif (mysqli_num_rows($checkEmailResult) > 0) {
        echo "email_exists";
    }

    else{
        $sql = "INSERT INTO users (username, password, email) VALUES ('$user_p','$pass_p','$email_p')";
        if (mysqli_query($conn, $sql)) {
            echo "success";
        } 
        else echo "fail";

        mysqli_close($conn);
    }

}

else{
    echo "all fields must be filled";
}
?>