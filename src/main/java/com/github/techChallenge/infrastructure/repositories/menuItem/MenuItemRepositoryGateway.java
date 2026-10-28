package com.github.techChallenge.infrastructure.repositories.menuItem;

import com.github.techChallenge.application.exceptions.MenuItemNotFoundException;
import com.github.techChallenge.application.repositories.IMenuItemRepository;
import com.github.techChallenge.domain.menuItem.MenuItem;
import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemDocument;
import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemSequence;
import com.github.techChallenge.infrastructure.mappers.MenuItemMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.Locale;

public class MenuItemRepositoryGateway implements IMenuItemRepository {
    private final MenuItemMongoRepository repository;
    private final MongoTemplate mongo;
    private final MenuItemMapper mapper;

    public MenuItemRepositoryGateway(MenuItemMongoRepository repository, MongoTemplate mongo, MenuItemMapper mapper) {
        this.repository = repository; this.mongo = mongo; this.mapper = mapper;
    }

    public MenuItem create(MenuItem item) {
        ensureUniqueIndex();
        MenuItemSequence sequence = mongo.findAndModify(Query.query(Criteria.where("_id").is("menu_items")),
                new Update().inc("value", 1L), FindAndModifyOptions.options().upsert(true).returnNew(true), MenuItemSequence.class);
        if (sequence == null) throw new IllegalStateException("Não foi possível gerar o identificador do item.");
        return mapper.fromDocumentToDomain(repository.insert(new MenuItemDocument(sequence.getValue(), item)));
    }

    public MenuItem update(MenuItem item, Long id) {
        ensureUniqueIndex();
        MenuItemDocument document = findDocument(id);
        document.update(item);
        return mapper.fromDocumentToDomain(repository.save(document));
    }

    public MenuItem findByID(Long id) { return mapper.fromDocumentToDomain(findDocument(id)); }
    public Page<MenuItem> listByRestaurant(Long restaurantId, int page, int offset) {
        return repository.findByRestaurantId(restaurantId, PageRequest.of(page, offset, Sort.by("id")))
                .map(mapper::fromDocumentToDomain);
    }
    public void delete(Long id) { repository.delete(findDocument(id)); }
    public boolean existsByNameAndRestaurantId(String name, Long restaurantId) {
        return repository.existsByNameKeyAndRestaurantId(key(name), restaurantId);
    }
    public boolean existsByNameAndRestaurantIdAndIdNot(String name, Long restaurantId, Long id) {
        return repository.existsByNameKeyAndRestaurantIdAndIdNot(key(name), restaurantId, id);
    }
    public boolean existsById(Long id) { return repository.existsById(id); }
    public boolean existsByRestaurantId(Long id) { return repository.existsByRestaurantId(id); }

    private MenuItemDocument findDocument(Long id) { return repository.findById(id).orElseThrow(() -> new MenuItemNotFoundException(id)); }
    private String key(String name) { return name.trim().toLowerCase(Locale.ROOT); }
    private void ensureUniqueIndex() {
        mongo.indexOps(MenuItemDocument.class).ensureIndex(new Index().on("restaurantId", Sort.Direction.ASC)
                .on("nameKey", Sort.Direction.ASC).unique().named("uk_menu_item_restaurant_name"));
    }
}
