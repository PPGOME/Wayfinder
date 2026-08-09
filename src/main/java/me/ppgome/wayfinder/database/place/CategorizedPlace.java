package me.ppgome.wayfinder.database.place;

import com.j256.ormlite.field.DatabaseField;

public class CategorizedPlace {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String category;

    @DatabaseField(canBeNull = false)
    private String place;

    //------------------------------------------------------------------------------------------------------------------

    public CategorizedPlace() {}

    public CategorizedPlace(String category, String place) {
        this.category = category;
        this.place = place;
    }

    //------------------------------------------------------------------------------------------------------------------

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Category: " + category + "| Place: " + place;
    }

}
