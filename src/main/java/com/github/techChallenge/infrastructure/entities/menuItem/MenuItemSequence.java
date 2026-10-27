package com.github.techChallenge.infrastructure.entities.menuItem;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("menu_item_sequences")
public class MenuItemSequence {
    @Id private String id;
    private long value;
    public long getValue() { return value; }
}
