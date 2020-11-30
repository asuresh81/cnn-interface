
@echo off

set "pythonversion=Python 3.7"
set "condition=false"

echo. & echo Checking Python version
for /f "delims=" %%i in ('python --version') do set output=%%i
(echo %output% | findstr /i /c:"%pythonversion%" >nul) && (set "condition=true") || (set "condition=false")

if "%condition%" == "true" (

	echo Python version is correct & echo.

	if exist "%~dp0\env\" (
		rd %~dp0\env\ /S /Q
    		md %~dp0\env\
	) else (
		md %~dp0\env\
	)
	
	echo Setting up virtual environment
	python -m venv %~dp0\env\
	echo Virtual environment created & echo.

	echo Activating virtual environment
	%~dp0\env\Scripts\activate
	echo Virtual environment activated & echo.

	echo Installing necessary packages
	if exist "%~dp0\requirements.txt" (
		python -m pip install --upgrade pip
		pip install -r requirements.txt 
		echo Packages installed & echo.
	) else (
		echo Missing requirements.txt or no other packages needed & echo.
	)

	echo SETUP SUCCESSFUL & echo.

	PAUSE

) else (
	echo. & echo Please ensure latest version of Python 3.7 is installed and added to PATH. 
	echo Refer to our user guide for additional help. & echo.
	PAUSE 
)
