@rem Gradle wrapper launcher for Windows
@echo off
set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
java %JAVA_OPTS% %GRADLE_OPTS% -classpath "%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
