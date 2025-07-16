<?php
// checkout.php - mark one or more items checked out
// Returns JSON messages for client-side handling
header('Content-Type: application/json; charset=utf-8');

try {
    // --- 1) Set up PDO (same credentials as insert.php) ---
    $pdo = new PDO(
        'mysql:host=localhost;dbname=q6zurlh45o4fz23v_UETBINLOCATIONS;charset=utf8mb4',
        'q6zurlh45o4fz23v_admin',
        'FCyevaI[(n_r',
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_EMULATE_PREPARES => false,
        ]
    );

    // --- 2) Grab & validate POST data ---
    $rollNumsRaw = $_POST['roll_num']  ?? null;
    $customersRaw = $_POST['customer'] ?? null;
    $pin = $_POST['pin'] ?? null;

    // Required fields
    $missing = [];
    if (!$rollNumsRaw)   $missing[] = 'roll_num';
    if (!$customersRaw)  $missing[] = 'customer';
    if (!$pin)           $missing[] = 'pin';

    if (!empty($missing)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Missing required field(s): ' . implode(', ', $missing)
        ]);
        exit;
    }

    // PIN must be exactly 4 digits
    if (!preg_match('/^\d{4}$/', $pin)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'Invalid PIN format. Must be 4 digits.'
        ]);
        exit;
    }

    // Normalize to arrays
    $rollNums  = is_array($rollNumsRaw)  ? $rollNumsRaw  : [ $rollNumsRaw ];
    $customers = is_array($customersRaw) ? $customersRaw : [ $customersRaw ];

    if (count($rollNums) !== count($customers)) {
        http_response_code(400);
        echo json_encode([
            'status'  => 'error',
            'message' => 'roll_num and customer counts do not match.'
        ]);
        exit;
    }

    // --- 3) Process each pair ---
    $checkedOut = 0;
    $errors     = [];

    $checkStmt = $pdo->prepare(
        'SELECT id
           FROM pallet_info
          WHERE roll_num    = :roll_num
            AND customer    = :customer
            AND in_warehouse = "YES"'
    );
    $updateStmt = $pdo->prepare(
        'UPDATE pallet_info
           SET in_warehouse  = "NO",
               checkout_date = NOW(),
               bin           = NULL,
               last_user     = :pin
         WHERE id = :id'
    );

    foreach ($rollNums as $i => $rollNum) {
        $cust = $customers[$i];

        // look up a matching "in_warehouse" record
        $checkStmt->execute([
            ':roll_num' => $rollNum,
            ':customer' => $cust,
        ]);
        $row = $checkStmt->fetch(PDO::FETCH_ASSOC);

        if ($row) {
            // perform checkout
            $updateStmt->execute([
                ':pin' => $pin,
                ':id'  => $row['id'],
            ]);
            $checkedOut++;
        } else {
            $errors[] = "$rollNum, $cust not found";
        }
    }

    // --- 4) Build response ---
    if (empty($errors)) {
        echo json_encode([
            'status'  => 'success',
            'message' => "$checkedOut item(s) checked out"
        ]);
    } else {
        echo json_encode([
            'status'      => 'partial',
            'checked_out'=> $checkedOut,
            'errors'      => $errors
        ]);
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
