@echo off
rem Copyright 2002-2004 The Apache Software Foundation or its licensors,
rem as applicable.
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

echo @prompt set PWD=$p$_ > _temp_A.bat
%comspec% /e:2048 /c _temp_A.bat > _temp_B.bat
call _temp_B.bat
del _temp_?.bat
