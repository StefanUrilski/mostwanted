package mostwanted.domain.dtos;

public class ExportRacingTownsDto {

    private String Name;

    private String Racers;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getRacers() {
        return Racers;
    }

    public void setRacers(String racers) {
        Racers = racers;
    }

    @Override
    public String toString() {
        return String.format("" +
                "Name: %s" + System.lineSeparator() +
                "Racers: %s\n\r",
                getName(),
                getRacers()
        );
    }
}
