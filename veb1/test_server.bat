@echo off
echo Тестирование FastCGI сервера...
echo.

echo Тест 1: Точка в прямоугольнике (должна попадать)
curl "http://localhost:8080/cgi-bin/point-checker?x=1&y=0.5&r=2"
echo.
echo.

echo Тест 2: Точка в четверти круга (должна попадать)
curl "http://localhost:8080/cgi-bin/point-checker?x=-0.5&y=0.5&r=2"
echo.
echo.

echo Тест 3: Точка в треугольнике (должна попадать)
curl "http://localhost:8080/cgi-bin/point-checker?x=-0.5&y=-0.5&r=2"
echo.
echo.

echo Тест 4: Точка вне области (не должна попадать)
curl "http://localhost:8080/cgi-bin/point-checker?x=2&y=2&r=2"
echo.
echo.

echo Тест 5: Некорректные параметры (должна быть ошибка)
curl "http://localhost:8080/cgi-bin/point-checker?x=10&y=10&r=10"
echo.
echo.

echo Тестирование завершено!
pause
