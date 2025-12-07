@echo off
echo ===== Docker Status Check =====
echo.
echo Docker Version:
docker --version
echo.
echo Docker Containers:
docker ps -a
echo.
echo Docker Networks:
docker network ls
echo.
echo Port Check (3306, 6379, 8848):
netstat -ano | findstr ":3306 :6379 :8848"
echo.
echo ===== Check Complete =====
pause
