@echo off
cd /d "%~dp0backend"
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven was not found on PATH.
  echo Install Maven 3.9+ first, then run this file again.
  pause
  exit /b 1
)
mvn spring-boot:run
