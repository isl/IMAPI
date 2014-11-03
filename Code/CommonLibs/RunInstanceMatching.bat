@echo OFF
SET currentPath=%CD%

REM THE FOLLOWING OPTIONS MUST BE SET

REM Make sure that java 7 bin path is correctly set 
SET JAVA_BIN_PATH=C:\Program Files\Java\jre7\bin
REM mode=BM
REM mode=FC
REM mode=CLAROS
REM SET mode=BM

SET BASEPATH=%currentPath%
REM SET UserConfigPath=%currentPath%\UserConfiguration-%mode%.xml
SET UserConfigPath=%currentPath%\UserConfiguration.xml
SET QueryPrototypePath=%currentPath%\ClassesAndLibs\QueryPrototypesConfiguration.xml


IF NOT EXIST "%JAVA_BIN_PATH%" echo "ERROR: Path to the java 7 jre bin folder (JAVA_BIN_PATH) = %JAVA_BIN_PATH% not found " && GOTO EXIT
IF NOT EXIST "%BASEPATH%" echo "ERROR: Base source/target rdf files folder path (BASEPATH) = %BASEPATH% not found " && GOTO EXIT
IF NOT EXIST "%UserConfigPath%" echo "ERROR: User Configuration xml file path (UserConfigPath) = %UserConfigPath% not found " && GOTO EXIT
IF NOT EXIST "%QueryPrototypePath%" echo "ERROR: Query Prototype Configuration xml file path (QueryPrototypePath) = %QueryPrototypePath% not found " && GOTO EXIT

REM SET OUTPUTFILEPATH=%currentPath%\out-%mode%.txt
SET OUTPUTFILEPATH=%currentPath%\out.txt

cd ./ClassesAndLibs
echo.
echo            Path to the java 7 jre bin folder = %JAVA_BIN_PATH%
echo     Base source/target rdf files folder path = %BASEPATH%
echo.
echo.
echo     Using the following configuration file: %UserConfigPath%
echo         Results will be redirected to file: %OUTPUTFILEPATH%
echo         Open this file to see the results.
echo.
echo.
@echo ON
"%JAVA_BIN_PATH%\java" -classpath InstansceMatchingApi-1.0.jar; TestInstansceMatchingApi "%BASEPATH%" "%UserConfigPath%" "%QueryPrototypePath%" >"%OUTPUTFILEPATH%"

:EXIT
pause




