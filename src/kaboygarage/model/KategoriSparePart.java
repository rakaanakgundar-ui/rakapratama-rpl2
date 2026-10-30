package kaboygarage.model;

/**
 * Kategori spare part motor yang dijual pada Kaboy Garage.
 */
public enum KategoriSparePart {

    MESIN("Mesin"),
    KELISTRIKAN("Kelistrikan"),
    BODY("Body & Panel"),
    OLI_CAIRAN("Oli & Cairan"),
    AKSESORIS("Aksesoris");

    private final String label;

    KategoriSparePart(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
