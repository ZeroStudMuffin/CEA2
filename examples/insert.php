<?php
// insert.php - improved version with JSON responses and validation
header('Content-Type: application/json; charset=utf-8');

try {
    // 1) Set up PDO with error mode and UTF8
    $pdo = new PDO(
        'mysql:host=localhost;dbname=q6zurlh45o4fz23v_UETBINLOCATIONS;charset=utf8mb4',
        'q6zurlh45o4fz23v_admin',
        'FCyevaI[(n_r',
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]
    );

    // 2) Validate incoming POST data
    $rollNum    = $_POST['roll_num']    ?? null;
    $customer   = $_POST['customer']    ?? null;
    $bin        = $_POST['bin']         ?? null;
    $releaseNum = $_POST['release_num'] ?? null;

    if (!$rollNum || !$customer || !$bin || !$releaseNum) {
        http_response_code(400);
        echo json_encode(['status' => 'error', 'message' => 'Missing required fields']);
        exit;
    }

    // 3) Prepare and execute insert
    $stmt = $pdo->prepare(
        'INSERT INTO pallet_info (roll_num, customer, bin, release_num)
         VALUES (:roll_num, :customer, :bin, :release_num)'
    );
    $stmt->execute([
        ':roll_num'    => $rollNum,
        ':customer'    => $customer,
        ':bin'         => $bin,
        ':release_num' => $releaseNum,
    ]);

    // 4) Return success with new record ID
    echo json_encode([
        'status' => 'success',
        'id'     => $pdo->lastInsertId(),
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'status'  => 'error',
        'message' => $e->getMessage(),
    ]);
}
?>
