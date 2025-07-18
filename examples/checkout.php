<?php
// insert.php – check-in (insert) an item
header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/config.php';   // load API_KEY

// --- API key check ---
$incomingKey = $_SERVER['HTTP_X_API_KEY'] ?? '';
if ($incomingKey !== API_KEY) {
    http_response_code(403);
    echo json_encode([
        'status'  => 'error',
        'message' => 'Invalid API key.'
    ]);
    exit;
}

try {
    // 1) Set up PDO
    $pdo = new PDO(
        'mysql:host=localhost;dbname=q6zurlh45o4fz23v_UETBINLOCATIONS;charset=utf8mb4',
        'q6zurlh45o4fz23v_admin',
        'FCyevaI[(n_r',
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]
    );

    // 2) Grab & validate POST data
    $rollNum    = $_POST['roll_num']    ?? null;
    $customer   = $_POST['customer']    ?? null;
    $bin        = $_POST['bin']         ?? null;
    $releaseNum = $_POST['release_num'] ?? null;
    $pin        = $_POST['pin']         ?? null;

    $missing = [];
    if (!$rollNum)  $missing[] = 'roll_num';
    if (!$customer) $missing[] = 'customer';
    if (!$bin)      $missing[] = 'bin';
    if (!$pin)      $missing[] = 'pin';

    if ($missing) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Missing required field(s): ' . implode(', ', $missing)
        ]);
        exit;
    }

    if (!preg_match('/^\d{4}$/', $pin)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Invalid PIN format. Must be 4 digits.'
        ]);
        exit;
    }

    // 3) Prevent check-in if already checked out
    $checkOutStmt = $pdo->prepare(
        'SELECT id
           FROM pallet_info
          WHERE roll_num    = :roll_num
            AND customer    = :customer
            AND in_warehouse = "NO"'
    );
    $checkOutStmt->execute([
        ':roll_num' => $rollNum,
        ':customer' => $customer,
    ]);

    if ($checkOutStmt->fetch(PDO::FETCH_ASSOC)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Item is checked out, call a manager.'
        ]);
        exit;
    }

    // 4) Normal insert
    $insertStmt = $pdo->prepare(
        'INSERT INTO pallet_info
          (roll_num, customer, bin, release_num, last_user, in_warehouse)
         VALUES
          (:roll_num, :customer, :bin, :release_num, :pin, "YES")'
    );
    $insertStmt->execute([
        ':roll_num'    => $rollNum,
        ':customer'    => $customer,
        ':bin'         => $bin,
        ':release_num' => $releaseNum,
        ':pin'         => $pin,
    ]);

    echo json_encode([
        'status'  => 'success',
        'message' => 'Item checked in.'
    ]);

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
