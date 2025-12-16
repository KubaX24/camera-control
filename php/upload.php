<?php
header('Content-Type: application/json; charset=utf-8');

$uploadDir = 'upload/';
$allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
$maxFileSize = 20 * 1024 * 1024;

$apiSecret = '';

function generateRandomString($length = 8) {
    return substr(bin2hex(random_bytes($length)), 0, $length);
}

$response = ['success' => false];

$userSecret = $_POST['secret'] ?? $_GET['secret'] ?? '';

if ($userSecret !== $apiSecret) {
    http_response_code(403);
    $response['message'] = 'hmm';
    echo json_encode($response);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['photo'])) {
    $file = $_FILES['photo'];

    if ($file['error'] !== UPLOAD_ERR_OK) {
        $response['message'] = 'Error';
        echo json_encode($response);
        exit;
    }

    if ($file['size'] > $maxFileSize) {
        $response['message'] = 'File is too large';
        echo json_encode($response);
        exit;
    }

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mimeType = $finfo->file($file['tmp_name']);

    if (!in_array($mimeType, $allowedTypes)) {
        $response['message'] = 'Unknown media type.';
        echo json_encode($response);
        exit;
    }

    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);

    do {
        $uniqueId = generateRandomString(24);
        $targetFile = $uploadDir . $uniqueId . '.' . $extension;
    } while (file_exists($targetFile));

    if (move_uploaded_file($file['tmp_name'], $targetFile)) {
        $response['success'] = true;
        $response['id'] = $uniqueId;
    } else {
        $response['message'] = 'Error while saving file';
    }
} else {
    $response['message'] = 'No file was found';
}

echo json_encode($response);
?>
