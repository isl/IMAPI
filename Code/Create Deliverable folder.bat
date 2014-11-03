REM Copyright 2014 Institute of Computer Science,
REM                Foundation for Research and Technology - Hellas.
REM
REM Licensed under the Apache License, Version 2.0 (the "License");
REM you may not use this file except in compliance with the License.
REM You may obtain a copy of the License at
REM
REM      http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing, software
REM distributed under the License is distributed on an "AS IS" BASIS,
REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM See the License for the specific language governing permissions and
REM limitations under the License.
REM 
REM =============================================================================
REM Contact: 
REM =============================================================================
REM Address: N. Plastira 100 Vassilika Vouton, GR-700 13 Heraklion, Crete, Greece
REM     Tel: +30-2810-391632
REM     Fax: +30-2810-391638
REM  E-mail: isl@ics.forth.gr
REM WebSite: http://www.ics.forth.gr/isl/
REM 
REM =============================================================================
REM Authors: 
REM =============================================================================
REM Elias Tzortzakakis <tzortzak@ics.forth.gr>
REM 

SET sampleDataFolder="C:\Users\tzortzak\Desktop\CultureBrokers\testdata\SampleData"

SET targetDirectory=Deliverable


@echo off
IF exist %targetDirectory% ( rmDir %targetDirectory% /S /Q ) else ( mkdir %targetDirectory%) 

mkDir %targetDirectory%\ClassesAndLibs

mkDir %targetDirectory%\SampleData

mkDir %targetDirectory%\ClassesAndLibs\lib

xcopy InstansceMatchingApi\dist\*.jar %targetDirectory%\ClassesAndLibs

xcopy InstansceMatchingApi\dist\*.txt %targetDirectory%\ClassesAndLibs

xcopy CommonLibs\apache-jena-2.11.2\lib\*.jar %targetDirectory%\ClassesAndLibs\lib

xcopy CommonLibs\Gson2.2.4\gson-2.2.4.jar %targetDirectory%\ClassesAndLibs\lib

copy CommonLibs\RunInstanceMatching.bat %targetDirectory%

xcopy %sampleDataFolder%\*  %targetDirectory%\SampleData /S

copy TestInstansceMatchingApi\build\classes\TestInstansceMatchingApi.class %targetDirectory%\ClassesAndLibs

copy TestInstansceMatchingApi\build\classes\QueryPrototypesConfiguration.xml  %targetDirectory%\ClassesAndLibs

copy TestInstansceMatchingApi\build\classes\UserConfiguration.xml  %targetDirectory%