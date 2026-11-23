@echo off
cd /d "%~dp0backend"
where mvn >nul 2>&1
if errorlevel 1 (
  echo Maven was not found on PATH.
  echo Install Maven 3.9+ first, then run this file again.
  pause
  exit /b 1
)
if "%DB_PASSWORD%"=="" (
  if not exist "%~dp0.env" if not exist "%~dp0backend\.env" (
    echo WARNING: DB_PASSWORD is not set and no .env file was found.
    echo Copy .env.example to .env in the project root and set DB_PASSWORD,
    echo or run:  set DB_PASSWORD=your_mysql_password
    echo.
  )
)
mvn spring-boot:run
