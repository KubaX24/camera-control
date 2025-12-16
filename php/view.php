<?php
$uploadDir = 'upload/';

$id = isset($_GET['id']) ? preg_replace('/[^a-zA-Z0-9]/', '', $_GET['id']) : '';

if (empty($id)) {
    die("No ID");
}

$files = glob($uploadDir . $id . '.*');

if ($files && count($files) > 0) {
    $filePath = $files[0];

    $finfo = new finfo(FILEINFO_MIME_TYPE);
    $mimeType = $finfo->file($filePath);

    header("Content-Type: $mimeType");
    header("Content-Length: " . filesize($filePath));

    readfile($filePath);
    exit;
} else {
    http_response_code(404);
    echo "Picture dont exist.";
}
?>
