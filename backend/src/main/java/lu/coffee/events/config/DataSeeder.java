package lu.coffee.events.config;

import lu.coffee.events.entity.*;
import lu.coffee.events.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Crée les comptes admin / cuisine et, au tout premier démarrage, une carte d'exemple. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SIZE = "[{\"name\":\"Taille\",\"required\":true,\"multi\":false,\"choices\":"
            + "[{\"name\":\"Petit\",\"price\":0},{\"name\":\"Grand\",\"price\":0.6}]}]";
    private static final String MILK = "[{\"name\":\"Taille\",\"required\":true,\"multi\":false,\"choices\":"
            + "[{\"name\":\"Petit\",\"price\":0},{\"name\":\"Grand\",\"price\":0.6}]},"
            + "{\"name\":\"Lait\",\"required\":false,\"multi\":false,\"choices\":"
            + "[{\"name\":\"Avoine\",\"price\":0.4},{\"name\":\"Soja\",\"price\":0.4},{\"name\":\"Amande\",\"price\":0.4}]},"
            + "{\"name\":\"Suppléments\",\"required\":false,\"multi\":true,\"choices\":"
            + "[{\"name\":\"Shot supplémentaire\",\"price\":0.5},{\"name\":\"Sirop vanille\",\"price\":0.3},{\"name\":\"Sirop caramel\",\"price\":0.3}]}]";
    private static final String COOKING = "[{\"name\":\"Cuisson\",\"required\":true,\"multi\":false,\"choices\":"
            + "[{\"name\":\"Saignant\",\"price\":0},{\"name\":\"À point\",\"price\":0},{\"name\":\"Bien cuit\",\"price\":0}]},"
            + "{\"name\":\"Accompagnement\",\"required\":true,\"multi\":false,\"choices\":"
            + "[{\"name\":\"Frites maison\",\"price\":0},{\"name\":\"Salade verte\",\"price\":0},{\"name\":\"Patates douces\",\"price\":1.5}]}]";

    private final UserRepository users;
    private final CategoryRepository categories;
    private final ProductRepository products;
    private final DiningTableRepository tables;
    private final RoomPackageRepository packages;
    private final RoomExtraRepository extras;
    private final PasswordEncoder encoder;
    private final String adminEmail, adminPassword, kitchenEmail, kitchenPassword;
    private final boolean seedDemo;

    public DataSeeder(UserRepository users, CategoryRepository categories, ProductRepository products,
                      DiningTableRepository tables, RoomPackageRepository packages, RoomExtraRepository extras,
                      PasswordEncoder encoder,
                      @Value("${app.admin.email}") String adminEmail, @Value("${app.admin.password}") String adminPassword,
                      @Value("${app.kitchen.email}") String kitchenEmail, @Value("${app.kitchen.password}") String kitchenPassword,
                      @Value("${app.seed-demo}") boolean seedDemo) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.tables = tables;
        this.packages = packages;
        this.extras = extras;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.kitchenEmail = kitchenEmail;
        this.kitchenPassword = kitchenPassword;
        this.seedDemo = seedDemo;
    }

    @Override
    public void run(String... args) {
        ensureUser("Administrateur", adminEmail, adminPassword, Enums.Role.ADMIN);
        ensureUser("Cuisine", kitchenEmail, kitchenPassword, Enums.Role.KITCHEN);
        if (seedDemo && categories.count() == 0) seedMenu();
    }

    private void ensureUser(String name, String email, String password, Enums.Role role) {
        if (users.existsByEmailIgnoreCase(email)) return;
        User u = new User();
        u.name = name;
        u.email = email.toLowerCase();
        u.passwordHash = encoder.encode(password);
        u.role = role;
        users.save(u);
        log.warn("Compte {} créé : {} (pensez à changer le mot de passe)", role, email);
    }

    private Category cat(String name, String icon, String description, int pos) {
        Category c = new Category();
        c.name = name;
        c.icon = icon;
        c.description = description;
        c.position = pos;
        return categories.save(c);
    }

    private void prod(Category c, String name, String description, String price, String options, String tags, int prep, int pos) {
        Product p = new Product();
        p.category = c;
        p.name = name;
        p.description = description;
        p.price = new BigDecimal(price);
        p.optionsJson = options;
        p.tags = tags;
        p.prepMinutes = prep;
        p.position = pos;
        products.save(p);
    }

    private void seedMenu() {
        Category coffee = cat("Cafés", "☕", "Torréfaction artisanale", 1);
        prod(coffee, "Espresso", "Court, intense, une vraie base.", "2.20", null, null, 2, 1);
        prod(coffee, "Café allongé", "Un espresso long, tout en douceur.", "2.60", null, null, 2, 2);
        prod(coffee, "Cappuccino", "Mousse de lait onctueuse, espresso double.", "3.80", MILK, null, 4, 3);
        prod(coffee, "Latte macchiato", "Lait chaud, espresso, fine mousse.", "4.20", MILK, null, 4, 4);
        prod(coffee, "Flat white", "Deux shots, micro-mousse de lait.", "4.20", MILK, null, 4, 5);
        Category tea = cat("Thés et chocolats", "🍵", "Chauds et réconfortants", 2);
        prod(tea, "Thé en feuilles", "Vert, noir ou infusion, servi en théière.", "3.50", null, "vegan", 4, 1);
        prod(tea, "Chocolat chaud maison", "Chocolat noir fondu, lait entier.", "4.50", SIZE, null, 5, 2);
        Category cold = cat("Boissons fraîches", "🍋", "Pressées et maison", 3);
        prod(cold, "Jus d'orange pressé", "Pressé à la commande.", "4.80", SIZE, "vegan", 3, 1);
        prod(cold, "Limonade maison", "Citron, menthe, sucre de canne.", "4.20", SIZE, "vegan", 3, 2);
        prod(cold, "Eau minérale 50 cl", "Plate ou pétillante.", "2.80", null, "vegan", 1, 3);
        Category bake = cat("Viennoiseries", "🥐", "Cuites chaque matin", 4);
        prod(bake, "Croissant au beurre", "Pur beurre, croustillant.", "1.90", null, null, 1, 1);
        prod(bake, "Pain au chocolat", "Deux barres de chocolat noir.", "2.10", null, null, 1, 2);
        prod(bake, "Petit-déjeuner du café", "Boisson chaude, jus, viennoiserie, tartine.", "9.90", null, null, 8, 3);
        Category lunch = cat("Salé", "🥪", "Sandwichs, salades et plats", 5);
        prod(lunch, "Croque-monsieur", "Jambon, emmental, béchamel.", "7.50", null, null, 8, 1);
        prod(lunch, "Salade chèvre chaud", "Mesclun, toast de chèvre, miel, noix.", "12.50", null, "végétarien", 10, 2);
        prod(lunch, "Burger du café", "Bœuf, cheddar, oignons confits, pain brioché.", "15.90", COOKING, null, 15, 3);
        prod(lunch, "Quiche du jour", "Servie avec une salade verte.", "9.80", null, "végétarien", 6, 4);
        Category sweet = cat("Desserts", "🍰", "Faits maison", 6);
        prod(sweet, "Tiramisu", "Mascarpone, café, cacao.", "5.90", null, "végétarien", 2, 1);
        prod(sweet, "Cheesecake aux fruits rouges", "Biscuit spéculoos, coulis maison.", "5.90", null, "végétarien", 2, 2);
        prod(sweet, "Fondant au chocolat", "Cœur coulant, servi tiède.", "6.20", null, "végétarien", 8, 3);

        for (int i = 1; i <= 8; i++) {
            DiningTable t = new DiningTable();
            t.label = "T" + i;
            t.seats = i <= 4 ? 2 : 4;
            tables.save(t);
        }
        DiningTable terrace = new DiningTable();
        terrace.label = "Terrasse 1";
        terrace.seats = 4;
        tables.save(terrace);

        pack("Apéritif dînatoire", "Assortiment de bouchées salées et sucrées, boissons softs à volonté.", "24", 10, 1);
        pack("Buffet complet", "Entrées froides, plat chaud, fromages, dessert, café.", "42", 15, 2);
        pack("Brunch de fête", "Buffet sucré-salé, viennoiseries, boissons chaudes et jus.", "29", 10, 3);

        extra("Décoration thématique", "Ballons, guirlandes, table de gâteau.", "60", false);
        extra("Sono et micro", "Enceintes, micro sans fil et playlist.", "40", false);
        extra("Gâteau d'événement", "Gâteau sur mesure pour toute la table.", "4.5", true);
        extra("Ménage de fin de soirée", "Nous nous occupons du rangement.", "50", false);
    }

    private void pack(String name, String description, String price, int minGuests, int pos) {
        RoomPackage p = new RoomPackage();
        p.name = name;
        p.description = description;
        p.pricePerPerson = new BigDecimal(price);
        p.minGuests = minGuests;
        p.position = pos;
        packages.save(p);
    }

    private void extra(String name, String description, String price, boolean perPerson) {
        RoomExtra e = new RoomExtra();
        e.name = name;
        e.description = description;
        e.price = new BigDecimal(price);
        e.perPerson = perPerson;
        extras.save(e);
    }
}
