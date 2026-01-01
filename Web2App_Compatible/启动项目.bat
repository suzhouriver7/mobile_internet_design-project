@echo off
chcp 65001 >nul
echo ====================================
echo 校园约伴系统 - 项目启动脚本
echo ====================================
echo.

echo [1/3] 检查 Node.js 环境...
node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)
echo [成功] Node.js 已安装
node -v
echo.

echo [2/3] 检查依赖...
if not exist "node_modules" (
    echo [提示] 未检测到 node_modules，开始安装依赖...
    echo 这可能需要几分钟，请耐心等待...
    echo.
    call npm install
    if errorlevel 1 (
        echo [错误] 依赖安装失败，请检查网络连接
        pause
        exit /b 1
    )
    echo [成功] 依赖安装完成
) else (
    echo [成功] 依赖已存在
)
echo.

echo [3/3] 启动开发服务器...
echo.
echo 项目将在浏览器中自动打开
echo 如果未自动打开，请访问: http://localhost:5173
echo.
echo 按 Ctrl+C 停止服务器
echo.
call npm run dev:h5

pause

