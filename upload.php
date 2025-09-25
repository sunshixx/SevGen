<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// 响应函数
function sendResponse($success, $message, $data = null) {
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data
    ]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendResponse(false, 'Only POST method allowed');
}

// 检查是否有上传的文件
if (!isset($_FILES['file'])) {
    sendResponse(false, 'No file uploaded');
}

$file = $_FILES['file'];
$filename = $_POST['filename'] ?? basename($file['name']);

// 检查上传错误
if ($file['error'] !== UPLOAD_ERR_OK) {
    sendResponse(false, 'Upload error: ' . $file['error']);
}

// 创建目标目录
$uploadDir = '/var/www/html/audio';
if (!is_dir($uploadDir)) {
    if (!mkdir($uploadDir, 0755, true)) {
        sendResponse(false, 'Failed to create upload directory');
    }
}

// 构建目标文件路径
$targetPath = $uploadDir . '/' . $filename;

// 移动上传的文件
if (move_uploaded_file($file['tmp_name'], $targetPath)) {
    // 设置文件权限
    chmod($targetPath, 0644);
    
    sendResponse(true, 'File uploaded successfully', [
        'filename' => $filename,
        'url' => 'http://111.62.241.115/audio/' . $filename,
        'size' => filesize($targetPath)
    ]);
} else {
    sendResponse(false, 'Failed to move uploaded file');
}
?>