import java.util.ArrayList;
import java.util.HashMap;

public class ExpensesManager {
    HashMap<String, ArrayList<Double>> expensesByCategories;

    ExpensesManager() {
        expensesByCategories = new HashMap<>();
    }

    double saveExpense(double moneyBeforeSalary, String category, double expense) {
        moneyBeforeSalary = moneyBeforeSalary - expense;
        System.out.println("Значение сохранено! Ваш текущий баланс в рублях: " + moneyBeforeSalary);
        if (expensesByCategories.containsKey(category)) {
            ArrayList<Double> expenses = expensesByCategories.get(category);
            expenses.add(expense);
        } else {
            ArrayList<Double> expenses = new ArrayList<>();
            expenses.add(expense);
            expensesByCategories.put(category, expenses);
        }
        if (moneyBeforeSalary < 1000) {
            System.out.println("На вашем счету осталось совсем немного. Стоит начать экономить!");
        }
        return moneyBeforeSalary;
    }

    void printAllExpensesByCategories() {
        for (String category : expensesByCategories.keySet()) {
            System.out.println(category);
            ArrayList<Double> expenses = expensesByCategories.get(category);
            for (Double expense : expenses) {
                System.out.println(expense);
            }
        }
    }

    double findMaxExpenseInCategory(String category) {
        double maxExpense = 0;
        if (expensesByCategories.containsKey(category)) {
            ArrayList<Double> expenses = expensesByCategories.get(category);
            for (Double expense : expenses) {
                if (expense > maxExpense) {
                    maxExpense = expense;
                }
            }
        } else {
            System.out.println("Такой категории пока нет.");
        }
        return maxExpense;
    }

    //метод для очистки таблицы с тратами
    void removeAllExpenses() {
        expensesByCategories.clear();
        System.out.println("Траты удалены.");
    }

    //напишите метод для получения суммы всех трат
    double getExpensesSum() {
        double sum = 0;
        for (ArrayList<Double> expense : expensesByCategories.values()) {
            sum += expense.stream().mapToDouble(Double::doubleValue).sum();
        }
        return sum;
    }

    // напишите метод для удаления категории
    void removeCategory(String category) {
        expensesByCategories.remove(category);
    }

    // напишите метод для получения категории, где размер трат больше всего
    // используйте эти переменные для сохранения промежуточных значений
    String getMaxCategoryName() {
        double maxCategorySum = 0;
        String maxCategoryName = "";
        for (String category : expensesByCategories.keySet()) {
            double sumCategory = expensesByCategories.get(category).stream().mapToDouble(Double::doubleValue).sum();
            if (sumCategory > maxCategorySum) {
                maxCategorySum = sumCategory;
                maxCategoryName = category;
            }

        }
        return maxCategoryName;
    }


}