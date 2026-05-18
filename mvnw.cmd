@echo off
cd /d "%~dp0backend" || exit /b 1
call mvnw.cmd %*
