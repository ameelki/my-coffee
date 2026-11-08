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
    private final SettingRepository settings;
    private final ProductImageRepository images;
    private final PasswordEncoder encoder;
    private final String adminEmail, adminPassword, kitchenEmail, kitchenPassword;
    private final boolean seedDemo;

    public DataSeeder(UserRepository users, CategoryRepository categories, ProductRepository products,
                      DiningTableRepository tables, RoomPackageRepository packages, RoomExtraRepository extras,
                      SettingRepository settings, ProductImageRepository images, PasswordEncoder encoder,
                      @Value("${app.admin.email}") String adminEmail, @Value("${app.admin.password}") String adminPassword,
                      @Value("${app.kitchen.email}") String kitchenEmail, @Value("${app.kitchen.password}") String kitchenPassword,
                      @Value("${app.seed-demo}") boolean seedDemo) {
        this.users = users;
        this.categories = categories;
        this.products = products;
        this.tables = tables;
        this.packages = packages;
        this.extras = extras;
        this.settings = settings;
        this.images = images;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.kitchenEmail = kitchenEmail;
        this.kitchenPassword = kitchenPassword;
        this.seedDemo = seedDemo;
    }

    /** Traductions (portugais | luxembourgeois | allemand) des noms de la carte d'exemple. */
    private static final java.util.Map<String, String> NAMES = new java.util.HashMap<>();

    static {
        String[][] t = {
            {"Cafés", "Cafés", "Kaffi", "Kaffee"}, {"Thés et chocolats", "Chás e chocolates", "Téi a Schockelaten", "Tee und Schokolade"},
            {"Boissons fraîches", "Bebidas frescas", "Frësch Gedrénks", "Kaltgetränke"}, {"Viennoiseries", "Pastelaria", "Viennoiserien", "Gebäck"},
            {"Salé", "Salgados", "Salzeg", "Herzhaft"}, {"Desserts", "Sobremesas", "Dessert", "Desserts"},
            {"Espresso", "Expresso", "Espresso", "Espresso"}, {"Café allongé", "Café longo", "Laange Kaffi", "Lungo"},
            {"Cappuccino", "Cappuccino", "Cappuccino", "Cappuccino"}, {"Latte macchiato", "Latte macchiato", "Latte macchiato", "Latte macchiato"},
            {"Flat white", "Flat white", "Flat white", "Flat White"}, {"Thé en feuilles", "Chá em folhas", "Téi a Blieder", "Blatttee"},
            {"Chocolat chaud maison", "Chocolate quente caseiro", "Hausgemaachte waarme Schockelaut", "Hausgemachte heiße Schokolade"},
            {"Jus d'orange pressé", "Sumo de laranja natural", "Frësch gepresste Orangejus", "Frisch gepresster Orangensaft"},
            {"Limonade maison", "Limonada caseira", "Hausgemaachte Limonad", "Hausgemachte Limonade"},
            {"Eau minérale 50 cl", "Água mineral 50 cl", "Mineralwaasser 50 cl", "Mineralwasser 50 cl"},
            {"Croissant au beurre", "Croissant de manteiga", "Botter-Croissant", "Butter-Croissant"},
            {"Pain au chocolat", "Pão de chocolate", "Pain au chocolat", "Schokoladencroissant"},
            {"Petit-déjeuner du café", "Pequeno-almoço do café", "Frühstück vum Café", "Café-Frühstück"},
            {"Croque-monsieur", "Croque-monsieur", "Croque-monsieur", "Croque Monsieur"},
            {"Salade chèvre chaud", "Salada de queijo de cabra quente", "Zalot mat waarmem Geessekéis", "Salat mit warmem Ziegenkäse"},
            {"Burger du café", "Hambúrguer do café", "Burger vum Café", "Café-Burger"}, {"Quiche du jour", "Quiche do dia", "Quiche vum Dag", "Quiche des Tages"},
            {"Tiramisu", "Tiramisu", "Tiramisu", "Tiramisu"}, {"Cheesecake aux fruits rouges", "Cheesecake de frutos vermelhos", "Cheesecake mat roude Friichten", "Cheesecake mit roten Früchten"},
            {"Fondant au chocolat", "Fondant de chocolate", "Schockelasfondant", "Schokoladenfondant"},
        };
        for (String[] r : t) NAMES.put(r[0], r[1] + "|" + r[2] + "|" + r[3]);
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** JSON de traductions pour un nom français connu, ou null. */
    private static String i18nFor(String frenchName) {
        String v = NAMES.get(frenchName);
        if (v == null) return null;
        String[] p = v.split("\\|");
        return "{\"pt\":{\"name\":\"" + esc(p[0]) + "\"},\"lb\":{\"name\":\"" + esc(p[1]) + "\"},\"de\":{\"name\":\"" + esc(p[2]) + "\"}}";
    }

    /** Complète les traductions manquantes de la carte d'exemple (aussi sur une base existante). */
    private void backfillTranslations() {
        for (Category c : categories.findAll()) {
            if (c.i18nJson == null && i18nFor(c.name) != null) {
                c.i18nJson = i18nFor(c.name);
                categories.save(c);
            }
        }
        for (Product p : products.findAll()) {
            if (p.i18nJson == null && i18nFor(p.name) != null) {
                p.i18nJson = i18nFor(p.name);
                products.save(p);
            }
        }
    }

    @Override
    public void run(String... args) {
        ensureUser("Administrateur", adminEmail, adminPassword, Enums.Role.ADMIN);
        ensureUser("Cuisine", kitchenEmail, kitchenPassword, Enums.Role.KITCHEN);
        if (seedDemo && categories.count() == 0) seedMenu();
        if (seedDemo) backfillTranslations();
        seedChristmasMenu();
        hideRegularMenu();
        attachNoelPhotos();
        seedBrand();
        seedTheme();
        seedAbout();
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

    // ---------------------------------------------------------------------------------------------
    // Menu de Noël & Nouvel An (spécialités portugaises, sur commande) : ajouté une seule fois.
    // Le prix du produit = prix « 2 personnes » ; l'option Portion ajoute le supplément pour 5 ou 10 personnes.
    // Ensuite tout se modifie depuis l'espace admin (prix, photos, disponibilité, traductions).
    // ---------------------------------------------------------------------------------------------
    private static final String NOEL_FLAG = "seed.noel.v1";

    private static String q(String s) {
        return "\"" + esc(s) + "\"";
    }

    /** Option « Portion » : 2 personnes (prix de base), puis suppléments pour 5 et 10 personnes (p5 peut être null). */
    private static String portion(int p2, Integer p5, int p10) {
        StringBuilder sb = new StringBuilder("{\"name\":\"Portion\",\"required\":true,\"multi\":false,\"choices\":[");
        sb.append("{\"name\":\"2 personnes\",\"price\":0}");
        if (p5 != null) sb.append(",{\"name\":\"5 personnes\",\"price\":").append(p5 - p2).append("}");
        sb.append(",{\"name\":\"10 personnes\",\"price\":").append(p10 - p2).append("}]}");
        return sb.toString();
    }

    private static String choice(String name, boolean required, boolean multi, String... values) {
        StringBuilder sb = new StringBuilder("{\"name\":" + q(name) + ",\"required\":" + required + ",\"multi\":" + multi + ",\"choices\":[");
        for (int i = 0; i < values.length; i++) sb.append(i > 0 ? "," : "").append("{\"name\":").append(q(values[i])).append(",\"price\":0}");
        return sb.append("]}").toString();
    }

    private static String opts(String... parts) {
        return "[" + String.join(",", parts) + "]";
    }

    private static String tr(String ptName, String ptDesc, String lbName, String deName, String deDesc) {
        StringBuilder sb = new StringBuilder("{\"pt\":{\"name\":" + q(ptName));
        if (ptDesc != null) sb.append(",\"description\":").append(q(ptDesc));
        sb.append("},\"lb\":{\"name\":").append(q(lbName)).append("},\"de\":{\"name\":").append(q(deName));
        if (deDesc != null) sb.append(",\"description\":").append(q(deDesc));
        return sb.append("}}").toString();
    }

    private Category noelCat(String name, String icon, String description, int pos, String i18n) {
        Category c = cat(name, icon, description, pos);
        c.i18nJson = i18n;
        return categories.save(c);
    }

    private void noelProd(Category c, String name, String description, int p2, String options, int pos, String i18n) {
        Product p = new Product();
        p.category = c;
        p.name = name;
        p.description = description;
        p.price = new BigDecimal(p2);
        p.optionsJson = options;
        p.tags = "sur commande";
        p.position = pos;
        p.i18nJson = i18n;
        products.save(p);
    }

    private void seedChristmasMenu() {
        if (settings.existsById(NOEL_FLAG)) return;
        Setting flag = new Setting();
        flag.key = NOEL_FLAG;
        flag.value = "done";
        settings.save(flag);

        String rice = choice("Accompagnement", true, false, "Riz", "Pommes de terre");

        Category starters = noelCat("Noël · Entrées", "🥟", "Menu de Noël & Nouvel An — spécialités portugaises, sur commande", 20,
                tr("Natal · Entradas", "Menu de Natal e Ano Novo — especialidades portuguesas, por encomenda", "Chrëschtdag · Virspeisen", "Weihnachten · Vorspeisen",
                        "Weihnachts- und Silvestermenü — portugiesische Spezialitäten, auf Bestellung"));
        Category fish = noelCat("Noël · Poissons", "🐟", "Bacalhau et saumon, à partager", 21,
                tr("Natal · Peixes", "Bacalhau e salmão, para partilhar", "Chrëschtdag · Fësch", "Weihnachten · Fisch", "Bacalhau und Lachs zum Teilen"));
        Category meat = noelCat("Noël · Viandes", "🍖", "Rôtis et volaille, avec accompagnement au choix", 22,
                tr("Natal · Carnes", "Assados e aves, com acompanhamento à escolha", "Chrëschtdag · Fleesch", "Weihnachten · Fleisch", "Braten und Geflügel, Beilage nach Wahl"));
        Category dessert = noelCat("Noël · Desserts", "🍮", "Douceurs de fête", 23,
                tr("Natal · Sobremesas", "Doçuras de festa", "Chrëschtdag · Dessert", "Weihnachten · Desserts", "Festtagssüßes"));

        String varieties = choice("Variétés (choisissez-en 6)", true, true, "Rissóis de camarão", "Rissóis de carne", "Croquete de carne",
                "Pastéis de bacalhau", "Coxinhas de frango", "Mini chouriço", "Mini folhados", "Mini croissants salés", "Mini quiches", "Mini pâtés de viande");
        noelProd(starters, "Box apéritif à composer soi-même", "Composez votre box : choisissez 6 variétés de salés parmi notre sélection.", 29,
                opts(portion(29, null, 115), varieties), 1,
                tr("Caixa de aperitivos à sua escolha", "Componha a sua caixa: escolha 6 variedades de salgados.", "Apéro-Box zum Zesummestellen",
                        "Aperitif-Box zum Selbstzusammenstellen", "Stellen Sie Ihre Box zusammen: 6 herzhafte Sorten zur Auswahl."));
        noelProd(starters, "Plateau charcuterie & fromages", "Sélection de charcuteries portugaises, fromages, olives et accompagnements.", 32,
                opts(portion(32, 68, 125)), 2,
                tr("Tábua de enchidos e queijos", "Seleção de enchidos portugueses, queijos, azeitonas e acompanhamentos.", "Wurscht- a Kéisplat",
                        "Wurst- und Käseplatte", "Auswahl portugiesischer Wurstwaren, Käse, Oliven und Beilagen."));
        noelProd(starters, "Plateau de crevettes & saumon fumé", "Crevettes, saumon fumé, citron et accompagnements.", 39,
                opts(portion(39, 85, 160)), 3,
                tr("Tábua de camarão e salmão fumado", "Camarão, salmão fumado, limão e acompanhamentos.", "Crevetten- a Raucherzalmplat",
                        "Garnelen- und Räucherlachsplatte", "Garnelen, Räucherlachs, Zitrone und Beilagen."));

        noelProd(fish, "Bacalhau à Gomes de Sá", "Morue effilochée, pommes de terre, oignons, œufs, olives et huile d'olive.", 44, opts(portion(44, 105, 195)), 1,
                tr("Bacalhau à Gomes de Sá", "Bacalhau desfiado, batata, cebola, ovos, azeitonas e azeite.", "Bacalhau à Gomes de Sá", "Bacalhau à Gomes de Sá",
                        "Zerzupfter Stockfisch, Kartoffeln, Zwiebeln, Eier, Oliven und Olivenöl."));
        noelProd(fish, "Bacalhau com Todos", "Morue, pommes de terre, chou, carottes, œufs et pois chiches.", 46, opts(portion(46, 110, 200)), 2,
                tr("Bacalhau com Todos", "Bacalhau, batata, couve, cenoura, ovos e grão-de-bico.", "Bacalhau com Todos", "Bacalhau com Todos",
                        "Stockfisch, Kartoffeln, Kohl, Karotten, Eier und Kichererbsen."));
        noelProd(fish, "Bacalhau com Natas", "Morue, pommes de terre, oignons et crème, gratinée au four.", 42, opts(portion(42, 100, 185)), 3,
                tr("Bacalhau com Natas", "Bacalhau, batata, cebola e natas, gratinado no forno.", "Bacalhau com Natas", "Bacalhau com Natas",
                        "Stockfisch, Kartoffeln, Zwiebeln und Sahne, im Ofen überbacken."));
        noelProd(fish, "Bacalhau com Broa", "Morue au four, croûte de broa, ail, huile d'olive et herbes.", 45, opts(portion(45, 108, 200)), 4,
                tr("Bacalhau com Broa", "Bacalhau no forno, crosta de broa, alho, azeite e ervas.", "Bacalhau com Broa", "Bacalhau com Broa",
                        "Ofenfrischer Stockfisch mit Broa-Kruste, Knoblauch, Olivenöl und Kräutern."));
        noelProd(fish, "Bacalhau à Braga", "Morue façon Braga, pommes de terre, oignons et sauce traditionnelle.", 45, opts(portion(45, 98, 205)), 5,
                tr("Bacalhau à Braga", "Bacalhau à moda de Braga, batata, cebola e molho tradicional.", "Bacalhau à Braga", "Bacalhau à Braga",
                        "Stockfisch nach Braga-Art, Kartoffeln, Zwiebeln und traditionelle Soße."));
        noelProd(fish, "Saumon rôti au four", "Légumes, pommes de terre grenaille et sauce champagne.", 42, opts(portion(42, 98, 180)), 6,
                tr("Salmão assado no forno", "Legumes, batata pequena e molho de champanhe.", "Gebakene Zalm aus dem Uewen", "Im Ofen gebratener Lachs",
                        "Gemüse, Drillinge und Champagnersoße."));

        noelProd(meat, "Rôti de porc", "Rôti de porc au four, légumes et choux, accompagné de sa sauce.", 34, opts(portion(34, 78, 145), rice), 1,
                tr("Assado de porco", "Assado de porco no forno, legumes e couves, com o seu molho.", "Schweinsbroden", "Schweinebraten",
                        "Ofenbraten vom Schwein, Gemüse und Kohl, mit Soße."));
        noelProd(meat, "Rôti de veau", "Rôti de veau, légumes, riz ou pommes de terre, sauce maison.", 39, opts(portion(39, 88, 165), rice), 2,
                tr("Assado de vitela", "Assado de vitela, legumes, arroz ou batatas, molho da casa.", "Kallefsbroden", "Kalbsbraten",
                        "Kalbsbraten, Gemüse, Reis oder Kartoffeln, Soße des Hauses."));
        noelProd(meat, "Dinde à la portugaise", "Dinde rôtie au four, riz ou pommes de terre, sauce maison.", 36, opts(portion(36, 82, 150), rice), 3,
                tr("Peru à portuguesa", "Peru assado no forno, arroz ou batatas, molho da casa.", "Truthahn op portugisesch", "Pute nach portugiesischer Art",
                        "Im Ofen gebratene Pute, Reis oder Kartoffeln, Soße des Hauses."));

        noelProd(dessert, "Pommes rôties à la cannelle", "Pommes cuites au four, sucre et cannelle.", 14, opts(portion(14, 30, 55)), 1,
                tr("Maçãs assadas com canela", "Maçãs assadas no forno, açúcar e canela.", "Gebake Äppel mat Zimt", "Bratäpfel mit Zimt", "Im Ofen gebackene Äpfel mit Zucker und Zimt."));
        noelProd(dessert, "Rabanadas", "Pain perdu portugais, parfumé à la cannelle.", 14, opts(portion(14, 32, 58)), 2,
                tr("Rabanadas", "Rabanadas tradicionais, perfumadas com canela.", "Rabanadas", "Rabanadas", "Portugiesischer Armer Ritter, mit Zimt verfeinert."));
        noelProd(dessert, "Plateau de fruits frais", "Sélection de fruits frais de saison, joliment présentée.", 16, opts(portion(16, 36, 65)), 3,
                tr("Tábua de fruta fresca", "Seleção de fruta fresca da época, bem apresentada.", "Plat mat frëschem Uebst", "Frische Obstplatte", "Auswahl frischer Früchte der Saison, hübsch angerichtet."));

        setIfEmpty("orders.message", "Menu de Noël & Nouvel An : sur commande, à emporter ou en livraison, du 24 au 31 décembre. "
                + "Réservations ouvertes jusqu'au 18 décembre (Noël) et jusqu'au 28 décembre (Nouvel An).");
        setIfEmpty("orders.message.pt", "Menu de Natal e Ano Novo: por encomenda, para levantar ou entrega ao domicílio, de 24 a 31 de dezembro. "
                + "Reservas abertas até 18 de dezembro (Natal) e até 28 de dezembro (Ano Novo).");
        setIfEmpty("orders.message.de", "Weihnachts- und Silvestermenü: auf Bestellung, zum Mitnehmen oder mit Lieferung, vom 24. bis 31. Dezember. "
                + "Reservierungen bis 18. Dezember (Weihnachten) und bis 28. Dezember (Silvester).");
        log.info("Menu de Noël ajouté (4 catégories, 15 produits).");
    }

    /** Une seule fois : désactive la carte habituelle pour ne garder que le menu de Noël (réactivable depuis l'admin). */
    private void hideRegularMenu() {
        String flag = "seed.noel.only.v1";
        if (settings.existsById(flag) || !settings.existsById(NOEL_FLAG)) return;
        Setting f = new Setting();
        f.key = flag;
        f.value = "done";
        settings.save(f);
        for (Category c : categories.findAll()) {
            if (c.name != null && !c.name.startsWith("Noël")) { c.active = false; categories.save(c); }
        }
        for (Product p : products.findAll()) {
            if (p.category == null || p.category.name == null || !p.category.name.startsWith("Noël")) { p.active = false; products.save(p); }
        }
        log.info("Carte habituelle désactivée : seul le menu de Noël reste visible.");
    }

    /** Photos du menu de Noël (src/main/resources/noel) : rattachées une seule fois aux produits qui n'ont pas encore de photo. */
    private static final String[][] NOEL_PHOTOS = {
        {"Box apéritif à composer soi-même", "box"}, {"Plateau charcuterie & fromages", "charcuterie"}, {"Plateau de crevettes & saumon fumé", "crevettes"},
        {"Bacalhau à Gomes de Sá", "gomes"}, {"Bacalhau com Todos", "todos"}, {"Bacalhau com Natas", "natas"}, {"Bacalhau com Broa", "natas"},
        {"Bacalhau à Braga", "braga"}, {"Saumon rôti au four", "saumon"}, {"Rôti de porc", "porc"}, {"Rôti de veau", "veau"},
        {"Dinde à la portugaise", "dinde"}, {"Pommes rôties à la cannelle", "pommes"}, {"Rabanadas", "rabanadas"}, {"Plateau de fruits frais", "fruits"},
    };

    private void attachNoelPhotos() {
        String flag = "seed.noel.photos.v1";
        if (settings.existsById(flag) || !settings.existsById(NOEL_FLAG)) return;
        Setting f = new Setting();
        f.key = flag;
        f.value = "done";
        settings.save(f);
        for (String[] row : NOEL_PHOTOS) {
            try {
                Product p = products.findAll().stream().filter(x -> row[0].equals(x.name)).findFirst().orElse(null);
                if (p == null || Boolean.TRUE.equals(p.hasImage)) continue;
                byte[] bytes;
                try (java.io.InputStream in = new org.springframework.core.io.ClassPathResource("noel/" + row[1] + ".jpg").getInputStream()) {
                    bytes = in.readAllBytes();
                }
                ProductImage img = new ProductImage();
                img.productId = p.id;
                img.contentType = "image/jpeg";
                img.data = bytes;
                images.save(img);
                p.hasImage = true;
                p.imageVersion = System.currentTimeMillis();
                products.save(p);
            } catch (Exception e) {
                log.warn("Photo non ajoutée pour {} : {}", row[0], e.getMessage());
            }
        }
        log.info("Photos du menu de Noël ajoutées.");
    }

    /** Une seule fois : identité Viva Lounge et traductions du bandeau d'événements (modifiables dans Réglages). */
    private void seedBrand() {
        String flag = "seed.brand.vivalounge.v1";
        if (settings.existsById(flag)) return;
        Setting f = new Setting();
        f.key = flag;
        f.value = "done";
        settings.save(f);
        Setting n = settings.findById("cafe.name").orElse(null);
        if (n == null || n.value == null || n.value.isBlank() || "Coffee Events".equals(n.value)) forceSetting("cafe.name", "Viva Lounge");
        Setting t = settings.findById("cafe.tagline").orElse(null);
        if (t == null || t.value == null || t.value.isBlank() || t.value.startsWith("Café de quartier")) forceSetting("cafe.tagline", "Brasserie · Au goût du Portugal");
        setIfEmpty("banner.items.pt", "Menu de Natal e Ano Novo: especialidades portuguesas, por encomenda\nReservas abertas até 18 de dezembro (Natal) e até 28 de dezembro (Ano Novo)\nPara levantar ou entrega ao domicílio, de 24 a 31 de dezembro");
        setIfEmpty("banner.items.de", "Weihnachts- und Silvestermenü: portugiesische Spezialitäten, auf Bestellung\nReservierungen bis 18. Dezember (Weihnachten) und bis 28. Dezember (Silvester)\nZum Mitnehmen oder mit Lieferung, vom 24. bis 31. Dezember");
        setIfEmpty("banner.items.lb", "Chrëschtdags- an Neijoerschmenü: portugisesch Spezialitéiten, op Bestellung\nReservatioune bis den 18. Dezember (Chrëschtdag) a bis den 28. Dezember (Neijoer)\nZum Metholl oder mat Liwwerung, vum 24. bis 31. Dezember");
        setIfEmpty("banner.linkLabel.pt", "Encomendar");
        setIfEmpty("banner.linkLabel.de", "Bestellen");
        setIfEmpty("banner.linkLabel.lb", "Bestellen");
        log.info("Identité Viva Lounge et bandeau d'événements initialisés.");
    }

    /** Une seule fois : thème rouge / noir assorti au logo Foodelicious Team (modifiable dans Réglages > Couleurs). */
    private void seedTheme() {
        String flag = "seed.theme.foodelicious.v1";
        if (settings.existsById(flag)) return;
        Setting f = new Setting();
        f.key = flag;
        f.value = "done";
        settings.save(f);
        forceSetting("brand.primary", "#d62b2a");
        forceSetting("brand.dark", "#181818");
    }

    /** Une seule fois : texte de présentation de l'accueil en 4 langues (modifiable dans Réglages). */
    private void seedAbout() {
        String flag = "seed.about.v1";
        if (settings.existsById(flag)) return;
        Setting f = new Setting();
        f.key = flag;
        f.value = "done";
        settings.save(f);
        forceSetting("cafe.name", "Viva Lounge");
        forceSetting("cafe.tagline", "Brasserie · Au goût du Portugal");
        forceSetting("cafe.about", "Viva Lounge, brasserie au goût du Portugal : cuisine portugaise et douceurs faites maison, préparées avec soin chaque jour. "
                + "Retrouvez nos spécialités (bacalhau, rôtis, pâtisseries et desserts traditionnels) à savourer sur place, à emporter ou à commander pour vos repas de fête. "
                + "Notre salle privée accueille vos anniversaires, baptêmes et réunions de famille ou d'équipe.");
        forceSetting("cafe.about.pt", "Viva Lounge, brasserie com sabor a Portugal: cozinha portuguesa e doçaria caseira, feitas com carinho todos os dias. "
                + "Descubra as nossas especialidades (bacalhau, assados, pastelaria e sobremesas tradicionais) para saborear no local, levar ou encomendar para as suas refeições de festa. "
                + "A nossa sala privada acolhe aniversários, batizados e encontros de família ou de equipa.");
        forceSetting("cafe.about.de", "Viva Lounge, Brasserie mit portugiesischem Flair: portugiesische Küche und hausgemachte Süßspeisen, jeden Tag mit Liebe zubereitet. "
                + "Entdecken Sie unsere Spezialitäten (Bacalhau, Braten, Gebäck und traditionelle Desserts) zum Genießen vor Ort, zum Mitnehmen oder zum Bestellen für Ihre Festtage. "
                + "In unserem privaten Saal feiern Sie Geburtstage, Taufen sowie Familien- und Teamtreffen.");
        forceSetting("cafe.about.lb", "Viva Lounge, Brasserie mat portugiesesche Goût: portugisesch Kichen an hausgemaachte Séissegkeeten, all Dag mat Léift gemaach. "
                + "Entdeckt eis Spezialitéiten (Bacalhau, Broden, Gebäck an traditionellt Dessert) fir hei ze genéissen, matzehuelen oder fir Är Fester ze bestellen. "
                + "An eisem private Sall kënnt Dir Gebuertsdeeg, Daf a Familljen- an Equipentreffen feieren.");
    }

    private void forceSetting(String key, String value) {
        Setting s = settings.findById(key).orElse(new Setting());
        s.key = key;
        s.value = value;
        settings.save(s);
    }

    private void setIfEmpty(String key, String value) {
        Setting cur = settings.findById(key).orElse(null);
        if (cur != null && cur.value != null && !cur.value.isBlank()) return;
        Setting s = cur != null ? cur : new Setting();
        s.key = key;
        s.value = value;
        settings.save(s);
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
