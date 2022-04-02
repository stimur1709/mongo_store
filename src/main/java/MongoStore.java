import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class MongoStore {
    private final MongoCollection<Document> collectionStore;
    private final MongoCollection<Document> collectionProduct;

    public MongoStore(String store, String product) {
        MongoClient mongoClient = new MongoClient("127.0.0.1", 27017);
        MongoDatabase database = mongoClient.getDatabase("local");
        collectionStore = database.getCollection(store);
        collectionProduct = database.getCollection(product);
    }

    public void addStore(String nameStore) {
        Document newScore = new Document().append("name", nameStore)
                .append("product", List.of());
        collectionStore.insertOne(newScore);
        System.out.println("Магазин " + nameStore + " добавлен");
    }

    public void addProduct(String nameProduct, String price) {
        Document newProduct = new Document().append("name", nameProduct)
                .append("price", Integer.parseInt(price));
        collectionProduct.insertOne(newProduct);
        System.out.println("Товар " + nameProduct + " с ценой " + price + " добавлен");
    }

    public void addProductInStore(String nameProduct, String nameStore) {
        Document document = new Document().append("name", nameStore);
        Bson update = Updates.addToSet("product", getProduct(nameProduct).get("name"));
        collectionStore.updateOne(document, update);
        System.out.println("Товар " + nameProduct + " выставлен в магазин " + nameStore);

    }

    private Document getProduct(String name) {
        return collectionProduct.find(new Document("name", name)).first();
    }

    public void printIfo(AggregateIterable<Document> documents) {
        for (Document document : documents) {
            String storeName = (String) document.get("_id");
            System.out.println("Магазин " + storeName);
            System.out.println("Количество товара: " + document.get("count"));
            System.out.println("Средняя цена товара: " + document.get("avg"));
            System.out.println("Самый дорогой товар:  " + document.get("max"));
            System.out.println("Самый дешевый товар:  " + document.get("min"));
            System.out.println("Количество товаров, дешевле 100 рублей: " + getProducts(storeName));
            System.out.println();
        }
    }

    private long getProducts(String name) {
        Document store = getStore(name);
        ArrayList<String> products = (ArrayList<String>) store.get("product");
        return products.stream()
                .filter(p -> (int) getProduct(p).get("price") < 100).count();
    }

    private Document getStore(String name) {
        return collectionStore.find(new Document("name", name)).first();
    }

    public AggregateIterable<Document> getAggregate() {
        return collectionProduct.aggregate(
                Arrays.asList(
                        Aggregates.lookup("store", "name", "product", "store_list"),
                        Aggregates.unwind("$store_list"),
                        Aggregates.group("$store_list.name",
                                Accumulators.sum("count", 1),
                                Accumulators.min("min", "$price"),
                                Accumulators.max("max", "$price"),
                                Accumulators.avg("avg", "$price"))
                )
        );
    }

    public void list() {
        collectionStore.find().forEach((Consumer<? super Document>) System.out::println);
        collectionProduct.find().forEach((Consumer<? super Document>) System.out::println);
    }

    public void delete() {
        collectionStore.drop();
        collectionProduct.drop();
    }
}
