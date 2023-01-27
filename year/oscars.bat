@echo off
echo Deleting any old data
del/q category\* player\* rank\*
echo Starting Oscars
call execute.bat Oscars
