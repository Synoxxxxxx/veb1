package org.example;

/**
 * Класс для проверки попадания точки в заданную область
 * 
 * Область состоит из трех частей:
 * 1. Прямоугольник в I квадранте: 0 ≤ x ≤ R, 0 ≤ y ≤ R/2
 * 2. Четверть круга во II квадранте: x² + y² ≤ (R/2)², x ≤ 0, y ≥ 0
 * 3. Треугольник в III квадранте: x ≥ -R/2, y ≥ -R/2, x + y ≥ -R/2, x ≤ 0, y ≤ 0
 */
public class PointChecker {
    
    /**
     * Проверяет, попадает ли точка (x, y) в область с радиусом R
     * 
     * @param x координата X
     * @param y координата Y  
     * @param r радиус области
     * @return true если точка попадает в область, false иначе
     */
    public static boolean isPointInArea(double x, double y, double r) {
        // Проверяем попадание в прямоугольник в I квадранте
        if (isInFirstQuadrantRectangle(x, y, r)) {
            return true;
        }
        
        // Проверяем попадание в четверть круга во II квадранте
        if (isInSecondQuadrantCircle(x, y, r)) {
            return true;
        }
        
        // Проверяем попадание в треугольник в III квадранте
        if (isInThirdQuadrantTriangle(x, y, r)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Проверяет попадание в прямоугольник в I квадранте
     * Условие: 0 ≤ x ≤ R и 0 ≤ y ≤ R/2
     */
    private static boolean isInFirstQuadrantRectangle(double x, double y, double r) {
        return x >= 0 && x <= r && y >= 0 && y <= r / 2;
    }
    
    /**
     * Проверяет попадание в четверть круга во II квадранте
     * Условие: x² + y² ≤ (R/2)² и x ≤ 0 и y ≥ 0
     */
    private static boolean isInSecondQuadrantCircle(double x, double y, double r) {
        if (x > 0 || y < 0) {
            return false;
        }
        
        double radius = r / 2;
        return (x * x + y * y) <= (radius * radius);
    }
    
    /**
     * Проверяет попадание в треугольник в III квадранте
     * Условие: x ≥ -R/2 и y ≥ -R и x + y ≥ -R и x ≤ 0 и y ≤ 0
     */
    private static boolean isInThirdQuadrantTriangle(double x, double y, double r) {
        if (x > 0 || y > 0) {
            return false;
        }
        
        return x >= -r/2 && y >= -r && (x + y) >= -r;
    }
    
    /**
     * Возвращает описание области для отладки
     */
    public static String getAreaDescription(double r) {
        return String.format(
            "Область с R=%.1f:\n" +
            "1. Прямоугольник: 0 ≤ x ≤ %.1f, 0 ≤ y ≤ %.1f\n" +
            "2. Четверть круга: x² + y² ≤ %.2f, x ≤ 0, y ≥ 0\n" +
            "3. Треугольник: x ≥ %.1f, y ≥ %.1f, x + y ≥ %.1f, x ≤ 0, y ≤ 0",
            r, r, r/2, (r/2)*(r/2), -r/2, -r/2, -r/2
        );
    }
    
    /**
     * Тестовый метод для проверки корректности алгоритма
     */
    public static void main(String[] args) {
        System.out.println("Тестирование алгоритма проверки попадания точки в область");
        System.out.println("========================================================");
        
        double r = 2.0;
        System.out.println(getAreaDescription(r));
        System.out.println();
        
        // Тестовые точки
        Object[][] testPoints = {
            {1.0, 0.5, true},   // Прямоугольник
            {-0.5, 0.5, true},  // Четверть круга
            {-0.5, -0.5, true}, // Треугольник
            {2.0, 2.0, false},    // Вне области
            {-2.0, -2.0, false},  // Вне области
            {0.0, 0.0, true},     // Начало координат
            {1.0, 1.0, false},    // Вне прямоугольника
            {-1.0, -1.0, false}   // Вне треугольника
        };
        
        for (Object[] point : testPoints) {
            double x = (Double) point[0];
            double y = (Double) point[1];
            boolean expected = (Boolean) point[2];
            boolean actual = isPointInArea(x, y, r);
            
            String status = (actual == expected) ? "✓" : "✗";
            System.out.printf("%s Точка (%.1f, %.1f): ожидалось %s, получено %s%n", 
                status, x, y, expected, actual);
        }
    }
}
