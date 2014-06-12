package org.opencb.cellbase.core.common.drug;

/**
 * Created by mbleda on 1/15/14.
 */
public class Interactor {
    private String id;
    private String name;
    private String description;

    public Interactor(String name) {
        this.name = name;
    }

    public Interactor(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
