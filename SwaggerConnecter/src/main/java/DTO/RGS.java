package DTO;

public class RGS {
    private String id; // Уникальный идентификатор резервуара
    private double volume; // Объем резервуара в литрах

    public RGS(String id, double volume) {
        this.id = id;
        this.volume = volume;
    }

    public String getId() { return id; }
    public double getVolume() { return volume; }
}
