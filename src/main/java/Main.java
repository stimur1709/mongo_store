import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MongoStore mongoStore = new MongoStore("store", "product");
        Scanner scanner = new Scanner(System.in);
        String stringTeam = "Введите команду";

        for (; ; ) {
            System.out.println(stringTeam);
            String input = scanner.nextLine();
            String[] splitInput = input.split(" ");
            switch (splitInput[0]) {
                case "ДОБАВИТЬ_МАГАЗИН":
                    mongoStore.addStore(splitInput[1]);
                    break;
                case "ДОБАВИТЬ_ТОВАР":
                    mongoStore.addProduct(splitInput[1], splitInput[2]);
                    break;
                case "ВЫСТАВИТЬ_ТОВАР":
                    mongoStore.addProductInStore(splitInput[1], splitInput[2]);
                    break;
                case "СТАТИСТИКА_ТОВАРОВ":
                    mongoStore.printIfo(mongoStore.getAggregate());
                    break;
                case "СПИСОК_МАГАЗИНОВ_И_ТОВАРОВ":
                    mongoStore.list();
                    break;
                case "УДАЛИТЬ":
                    mongoStore.delete();
                    break;
            }
        }
    }
}