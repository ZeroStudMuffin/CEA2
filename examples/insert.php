<?php
// insert.php - prevent duplicate roll_num/customer, update bin or insert new record
// Returns detailed JSON messages for client-side error handling
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

    // 2) Validate required POST data
    $rollNum    = $_POST['roll_num']    ?? null;
    $customer   = $_POST['customer']    ?? null;
    $bin        = $_POST['bin']         ?? null;
    $releaseNum = $_POST['release_num'] ?? null; // optional

    $missing = [];
    if (!$rollNum)   $missing[] = 'roll_num';
    if (!$customer)  $missing[] = 'customer';
    if (!$bin)       $missing[] = 'bin';

    if (!empty($missing)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Missing required field(s): ' . implode(', ', $missing)
        ]);
        exit;
    }

    // 3) Check for existing record
    $checkStmt = $pdo->prepare(
        'SELECT id FROM pallet_info WHERE roll_num = :roll_num AND customer = :customer'
    );
    $checkStmt->execute([
        ':roll_num' => $rollNum,
        ':customer' => $customer,
    ]);
    $existing = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if ($existing) {
        // 4a) Update existing record's bin (and release_num)
        $updateStmt = $pdo->prepare(
            'UPDATE pallet_info
             SET bin = :bin, release_num = :release_num
             WHERE id = :id'
        );
        $success = $updateStmt->execute([
            ':bin'         => $bin,
            ':release_num' => $releaseNum,
            ':id'          => $existing['id'],
        ]);

        if ($success) {
            echo json_encode([
                'status'  => 'updated',
                'message' => 'Record updated successfully',
                'id'      => $existing['id'],
            ]);
        } else {
            http_response_code(500);
            $errorInfo = $updateStmt->errorInfo();
            echo json_encode([
                'status'  => 'error',
                'message' => 'Update failed: ' . $errorInfo[2]
            ]);
        }

    } else {
        // 4b) Insert new record
        $insertStmt = $pdo->prepare(
            'INSERT INTO pallet_info (roll_num, customer, bin, release_num)
             VALUES (:roll_num, :customer, :bin, :release_num)'
        );
        $success = $insertStmt->execute([
            ':roll_num'    => $rollNum,
            ':customer'    => $customer,
            ':bin'         => $bin,
            ':release_num' => $releaseNum,
        ]);

        if ($success) {
            echo json_encode([
                'status'  => 'inserted',
                'message' => 'Record inserted successfully',
                'id'      => $pdo->lastInsertId(),
            ]);
        } else {
            http_response_code(500);
            $errorInfo = $insertStmt->errorInfo();
            echo json_encode([
                'status'  => 'error',
                'message' => 'Insert failed: ' . $errorInfo[2]
            ]);
        }
    }

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'status'  => 'error',
        'message' => 'Database error: ' . $e->getMessage(),
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status'  => 'error',
        'message' => 'Server error: ' . $e->getMessage(),
    ]);
}
?>
