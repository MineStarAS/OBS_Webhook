@echo off
REM OBS_Webhook 네이티브 이미지 빌드 스크립트
REM 실행하면 GraalVM native-image를 이용해 exe를 생성합니다.

native-image ^
  -jar "C:\Users\band1\IdeaProjects\OBS_Webhook\build\libs\OBS_Webhook-1.0.0.jar" ^
  -H:Name=OBS_Webhook ^
  -H:Path="C:\Users\band1\IdeaProjects\OBS_Webhook\native" ^
  --no-fallback ^
  --enable-native-access=ALL-UNNAMED ^
  -H:ReflectionConfigurationResources=META-INF/native-image/reflect-config.json ^
  --initialize-at-build-time=org.slf4j.simple.SimpleLogger,org.slf4j.simple.SimpleLoggerConfiguration,org.slf4j.simple.OutputChoice,org.slf4j.simple.OutputChoice$OutputChoiceType ^
  "-H:NativeLinkerOption=/link" ^
  "-H:NativeLinkerOption=C:\Users\band1\IdeaProjects\OBS_Webhook\app.res"

echo.
echo 빌드가 완료되었는지 확인하세요. 결과물은 native 폴더에 생성됩니다.
pause
