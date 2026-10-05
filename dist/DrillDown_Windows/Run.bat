@echo off
chcp 65001 >nul
title Drill Down
start "" "%~dp0jre\bin\javaw.exe" -jar "%~dp0DrillDown.jar" windowed
