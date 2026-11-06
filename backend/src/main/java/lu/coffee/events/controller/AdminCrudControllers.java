package lu.coffee.events.controller;

import lu.coffee.events.entity.*;
import lu.coffee.events.repository.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Un contrôleur CRUD par ressource configurable. */
public class AdminCrudControllers {

    @RestController
    @RequestMapping("/api/admin/categories")
    public static class Categories extends CrudController<Category> {
        private final CategoryRepository r;
        public Categories(CategoryRepository r) { super(r); this.r = r; }
        @Override protected List<Category> all() { return r.findAllByOrderByPositionAscIdAsc(); }
    }

    @RestController
    @RequestMapping("/api/admin/products")
    public static class Products extends CrudController<Product> {
        private final ProductRepository r;
        public Products(ProductRepository r) { super(r); this.r = r; }
        @Override protected List<Product> all() { return r.findAllByOrderByPositionAscIdAsc(); }
    }

    @RestController
    @RequestMapping("/api/admin/tables")
    public static class Tables extends CrudController<DiningTable> {
        private final DiningTableRepository r;
        public Tables(DiningTableRepository r) { super(r); this.r = r; }
        @Override protected List<DiningTable> all() { return r.findAllByOrderByLabelAsc(); }
    }

    @RestController
    @RequestMapping("/api/admin/packages")
    public static class Packages extends CrudController<RoomPackage> {
        private final RoomPackageRepository r;
        public Packages(RoomPackageRepository r) { super(r); this.r = r; }
        @Override protected List<RoomPackage> all() { return r.findAllByOrderByPositionAscIdAsc(); }
    }

    @RestController
    @RequestMapping("/api/admin/extras")
    public static class Extras extends CrudController<RoomExtra> {
        private final RoomExtraRepository r;
        public Extras(RoomExtraRepository r) { super(r); this.r = r; }
        @Override protected List<RoomExtra> all() { return r.findAllByOrderByIdAsc(); }
    }

    @RestController
    @RequestMapping("/api/admin/closures")
    public static class Closures extends CrudController<BlockedDate> {
        private final BlockedDateRepository r;
        public Closures(BlockedDateRepository r) { super(r); this.r = r; }
        @Override protected List<BlockedDate> all() { return r.findAllByOrderByDateAsc(); }
    }
}
