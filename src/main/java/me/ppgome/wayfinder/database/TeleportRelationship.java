package me.ppgome.wayfinder.database;

import com.j256.ormlite.field.DatabaseField;

import java.util.UUID;

public class TeleportRelationship {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private UUID playerUuid;

    @DatabaseField(canBeNull = false)
    private UUID targetUuid;

    @DatabaseField(canBeNull = false)
    private String relationshipType;

    //------------------------------------------------------------------------------------------------------------------

    public TeleportRelationship() {}

}
