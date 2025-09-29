package org.example;

/**
 * Главный класс для запуска лабораторной работы
 * Для запуска FastCGI сервера используйте FastCGIServer.main()
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Лабораторная работа: FastCGI сервер для проверки попадания точки в область");
        System.out.println("Для запуска сервера выполните: FastCGIServer.main()");
        System.out.println("Или используйте: mvn exec:java -Dexec.mainClass=\"org.example.FastCGIServer\"");
        
        // Запускаем тесты алгоритма
        System.out.println("\nЗапуск тестов алгоритма:");
        PointChecker.main(args);
    }
}