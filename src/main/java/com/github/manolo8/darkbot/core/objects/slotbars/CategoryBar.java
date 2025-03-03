package com.github.manolo8.darkbot.core.objects.slotbars;

import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import eu.darkbot.api.game.items.ItemCategory;
import eu.darkbot.api.game.items.SelectableItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.manolo8.darkbot.Main.API;

public class CategoryBar extends MenuBar {
    public final List<Category> categories = new ArrayList<>();

    private final ObjArray categoriesArr = ObjArray.ofVector(true);

    @Override
    public void update() {
        super.update();
        this.categoriesArr.update(API.readMemoryLong(address + 56));
        this.categoriesArr.sync(this.categories, Category::new);
    }

    @Deprecated
    public Category get(CategoryType type) {
        String id = type.getId();
        for (Category category : categories) {
            if (id.equals(category.categoryId)) return category;
        }
        return null;
    }

    public Category get(ItemCategory itemCategory) {
        for (Category category : categories) {
            if (itemCategory == category.itemCategory)
                return category;
        }
        return null;
    }

    public List<Item> getItems(ItemCategory itemCategory)  {
        Category cat = get(itemCategory);
        return cat == null ? Collections.emptyList() : cat.items;
    }

    public Stream<Item> getItemStream(ItemCategory itemCategory) {
        return getItems(itemCategory).stream();
    }

    public boolean hasCategory(ItemCategory type) {
        for (Category category : categories) {
            if (type == category.itemCategory)
                return true;
        }
        return false;
    }

    public Optional<Item> findItemById(String itemId) {
        for (Category cat : categories) {
            for (Item item : cat.items) {
                if (itemId.equals(item.id)) return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public Optional<Item> findItem(SelectableItem item) {
        if (item.getCategory() == null)
            return categories.stream()
                    .map(c -> c.findItem(item))
                    .filter(Objects::nonNull)
                    .findFirst();

        Category category = get(item.getCategory());
        if (category == null)
            return Optional.empty();

        return Optional.ofNullable(category.findItem(item));
    }

    public static class Category extends Auto {
        public String categoryId;
        public List<Item> items = new ArrayList<>();

        private final ObjArray itemsArr = ObjArray.ofVector(true);

        private ItemCategory itemCategory;

        public @Nullable Item findItem(SelectableItem item) {
            return items.stream()
                    .filter(i -> i.equals(item))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public void update() {
            this.itemsArr.update(API.readMemoryLong(address + 40));
            this.itemsArr.sync(this.items, () -> new Item(itemCategory));
        }

        @Override
        public void update(long address) {
            if (this.address != address || categoryId == null || categoryId.isEmpty()) {
                this.categoryId = API.readMemoryString(address, 32);
                this.itemCategory = ItemCategory.of(categoryId);
            }
            super.update(address);
        }
    }

    @Deprecated
    public enum CategoryType {
        LASERS,
        ROCKETS,
        ROCKET_LAUNCHERS,
        SPECIAL_ITEMS,
        MINES,
        CPUS,
        BUY_NOW,
        TECH_ITEMS,
        SHIP_ABILITIES,
        DRONE_FORMATIONS,
        PET;

        public String getId() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}